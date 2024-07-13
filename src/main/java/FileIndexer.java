import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileIndexer {

    private String indexPath;
    private String filesPath;

    public FileIndexer(String indexPath, String filesPath) {
        this.indexPath = indexPath;
        this.filesPath = filesPath;
    }

    public void index() throws IOException, ParseException {
        Files.list(Paths.get(filesPath))
                .filter(path -> ! path.toFile().isDirectory())
                .forEach(file -> {
                    try {
                        indexFile(getMetadata(file), getContent(file));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private List<Field> getMetadata(Path file) throws IOException {
        var creationTime = Files.getAttribute(file, "creationTime");

        var filename = new TextField(FileFields.FILENAME.name, file.getFileName().toString(), Field.Store.YES);
        var dateStr = new TextField(FileFields.DATE.name, creationTime.toString(), Field.Store.YES);
        var fileSize = new LongField(FileFields.SIZE.name, file.toFile().length(), Field.Store.YES);

        return List.of(filename, dateStr, fileSize);
    }

    private TextField getContent(Path file) throws IOException {
        return new TextField(FileFields.CONTENT.name, Files.readString(file), Field.Store.NO);
    }

    private void indexFile(List<Field> metadata, Field content) throws IOException {
        var dir = FSDirectory.open(Paths.get(indexPath));
        var analyzer = new StandardAnalyzer();
        var indexWriterConfig = new IndexWriterConfig(analyzer);

        try (IndexWriter writer = new IndexWriter(dir, indexWriterConfig)) {
            var document = new Document();
            metadata.forEach(document::add);
            document.add(content);
            writer.addDocument(document);
        }
    }
}
