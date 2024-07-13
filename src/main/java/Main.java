import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {
//        FileIndexer indexer = new FileIndexer("index", "data");
//        indexer.index();
        FileSearcher searcher = new FileSearcher("index");
        searcher.search(FileFields.CONTENT,"java");
    }
}
