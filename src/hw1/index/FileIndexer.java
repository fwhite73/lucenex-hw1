package index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class FileIndexer {

    private static final Logger logger = Logger.getLogger(FileIndexer.class.toString());
    private static final String FILES_TO_INDEX_DIRECTORY = "files";
    private static final String INDEX_DIRECTORY = "index";

    private static int docsCounter = 0;

    public static void createIndex(Codec codec) {
        try {
            Directory indexDir = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
            Path docDir = Paths.get(FILES_TO_INDEX_DIRECTORY);

            Map<String, Analyzer> perFieldAnalyzers = new HashMap<>();

            // Per inserire le stop words di default dell'ItalianAnalyzer() nel CustomAnalyzer
            // salvo la lista in un file che verrà utilizzato successivamente
            String stopWords = ItalianAnalyzer.getDefaultStopSet().toString();
            stopWords = stopWords.substring(0, stopWords.length()-1);
            saveArrayToFile("stopWords.txt", stopWords.split(","));

            // analyzer per il campo titolo
            Analyzer analyzer_name = CustomAnalyzer.builder()
                    .withTokenizer(WhitespaceTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .addTokenFilter(WordDelimiterGraphFilterFactory.class)
                    .build();
            perFieldAnalyzers.put("titolo", analyzer_name);

            // è necessario definire la cartella
            // da cui prendere i file nei parametri (in questo caso le stop words)
            Path resources = Paths.get("");

            // analyzer per il campo contenuto
            Analyzer analyzer_content = CustomAnalyzer.builder(resources)
                    .withTokenizer(WhitespaceTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .addTokenFilter(WordDelimiterGraphFilterFactory.class)
                    // definisco lo stop filter con la mia lista di stop words
                    // per definire parametri è necessario utilizzare coppie di parametri
                    // nome parametro, valore parametro
                    .addTokenFilter(StopFilterFactory.NAME,
                            "ignoreCase", "false",
                                    "words", "stopWords.txt",
                                    "format", "wordset")
                    .build();
            perFieldAnalyzers.put("contenuto", analyzer_content);

            // definisco l'analyzer utilizzando le precedenti inizializzazioni
            Analyzer analyzer = new PerFieldAnalyzerWrapper(new ItalianAnalyzer(), perFieldAnalyzers);

            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            if (codec != null)
                iwc.setCodec(codec);

            IndexWriter indexWriter = new IndexWriter(indexDir, iwc);
            indexWriter.deleteAll();

            // verifico la presenza dei file nella cartella specificata (se è una cartella)
            if (Files.isDirectory(docDir)) {
                Files.walkFileTree(docDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        try {
                            // indicizzazione del file
                            indexDoc(indexWriter, file);
                        } catch (IOException e) {
                            logger.severe("Error while visiting file");
                            logger.severe(e.getMessage());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else
                // altrimenti è un file da indicizzare
                indexDoc(indexWriter, docDir);
            indexWriter.commit();
            indexWriter.close();
            System.out.println("Documenti indicizzati: " + docsCounter);
        } catch (IOException e) {
            logger.severe("Error during index creation");
            logger.severe(e.getMessage());
        }
    }

    //funzione per l'indicizzazione del singolo documento
    static void indexDoc(IndexWriter indexWriter, Path docPath) throws IOException {
        Document doc = new Document();

        // indicizzazione del campo titolo (il nome del file)
        doc.add(new TextField("titolo", docPath.getFileName().toString(), Field.Store.YES));

        // indicizzazione del campo contenuto
        String contenuto = new String(Files.readAllBytes(docPath));
        doc.add(new TextField("contenuto", contenuto, Field.Store.YES));

        logger.info("adding " + docPath);

        // aggiungo il documento all'indexWriter
        indexWriter.addDocument(doc);
        docsCounter++;
    }

    // funzione per il salvataggio di un array su file
    private static void saveArrayToFile(String filename, String[] x) throws IOException {
        BufferedWriter outputWriter;
        outputWriter = new BufferedWriter(new FileWriter(filename));
        for (String s : x) {
            outputWriter.write(s.substring(1));
            outputWriter.newLine();
        }
        outputWriter.flush();
        outputWriter.close();
        logger.info("File: " + filename + " created.");
    }
}