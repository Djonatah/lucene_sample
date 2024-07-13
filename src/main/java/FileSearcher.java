import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileSearcher {
    String indexPath;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private Directory dirReader;

    public FileSearcher(String indexPath) throws IOException {
        dirReader = FSDirectory.open(Paths.get(indexPath));
        indexReader = DirectoryReader.open(dirReader);
        indexSearcher = new IndexSearcher(indexReader);
    }

    public void search(FileFields field, String queryStr) throws ParseException, IOException {

        QueryParser queryParser = new QueryParser(field.name,  new StandardAnalyzer());
        Query query = queryParser.parse(queryStr);

        TopDocs docs = indexSearcher.search(query,10);

        List<Document> result = Arrays.stream(docs.scoreDocs).map(doc-> {
            try {
                Explanation e = indexSearcher.explain(query, doc.doc);
                return indexSearcher.doc(doc.doc);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        result.forEach(this::printDocProperties);
    }

    private void printDocProperties(Document doc) {
        var filename = doc.getField(FileFields.FILENAME.name).stringValue();
        var size = doc.getField(FileFields.SIZE.name).stringValue();
        var creation = doc.getField(FileFields.DATE.name).stringValue();

        var msg = String.format("file: %s - Creation: %s - Size: %s", filename, creation, size);
        System.out.println(msg);
    }
}
