package query;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class QueryManager {

    private static final Logger logger = Logger.getLogger(QueryManager.class.toString());
    private static final String INDEX_DIRECTORY = "index";

    public static void executeQuery(String field, String queryString) {
        try {
            Query query;
            if(isPhraseQuery(queryString)) {
                queryString = queryString.replace("\"","");
                PhraseQuery.Builder queryBuilder = new PhraseQuery.Builder();
                for(String s : queryString.split(" "))
                    queryBuilder.add(new Term(field, s.toLowerCase()));
                query = queryBuilder.build();
            }
            else {
                QueryParser parser = new QueryParser(field, new WhitespaceAnalyzer());
                query = parser.parse(queryString);
            }
            try (Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY))) {
                try (IndexReader reader = DirectoryReader.open(directory)) {
                    IndexSearcher searcher = new IndexSearcher(reader);
                    runQuery(searcher, query);
                } finally {
                    directory.close();
                }
            }
        } catch (IOException | ParseException e) {
            logger.severe(e.getMessage());
        }

    }

    private static boolean isPhraseQuery(String query) {
        return (query.startsWith("'") && query.endsWith("'"))||(query.startsWith("\"") && query.endsWith("\""));
    }

    private static void runQuery(IndexSearcher searcher, Query query) throws IOException {
        runQuery(searcher, query, false);
    }

    private static void runQuery(IndexSearcher searcher, Query query, boolean explain) throws IOException {
        TopDocs hits = searcher.search(query, 10);
        for (int i = 0; i < hits.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = hits.scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println("doc"+scoreDoc.doc + ":"+ doc.get("titolo") + " (" + scoreDoc.score +")");
            if (explain) {
                Explanation explanation = searcher.explain(query, scoreDoc.doc);
                System.out.println(explanation);
            }
        }
    }
}
