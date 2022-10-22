package lucenex;


import index.FileIndexer;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.junit.Test;

public class SamplesTest {

    @Test
    public void testFileIndexer() {
        FileIndexer.createIndex(new SimpleTextCodec());
    }

}