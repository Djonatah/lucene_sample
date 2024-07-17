import analyzers.TagAttributeValueAnalyzer;
import analyzers.RegExCaptureAnalyzer;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, ParseException {

        Path indexPath = Paths.get("index");
        Directory directory = FSDirectory.open(indexPath);
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        if(indexPath.toFile().list().length == 0) {
            IndexWriter writer = new IndexWriter(directory, config);

            var html =
                    """
                        <html>
                            <div><span>test</span></div>
                            <div edesc=
                                    "test"
                                    eamount="10.00">test</div>
                            <div sometestext eamount = "10.00" edesc="test &amp;
                                        description" eamount="25.00">
                                <span>Text block 10.00</span>
                            </div>
                            <div sometestext eamount="30.00" edesc="test description" eamount="25.00" >
                                <span>Amount 30.00</span>
                            </div>
                            <div sometestext eamount="10.00" edesc="test description" eamount="25.00" >
                                <span>another 10.00</span>
                            </div>
                        </html>
                    """;


            Analyzer patternAnalyzer = new RegExCaptureAnalyzer("<([a-z]+)(?![^>]*\\/>)[^>]*>", ".*(eamount|edesc|enbr|edate).*");
            try (TokenStream tokenStream = patternAnalyzer.tokenStream("content", new StringReader(html))) {
                CharTermAttribute charTermAttr = tokenStream.addAttribute(CharTermAttribute.class);
                tokenStream.reset();
                while (tokenStream.incrementToken()) {
                    String transaction = charTermAttr.toString();
//                    System.out.println("Transaction Token: " + transaction);
                    Document doc = new Document();

                    List<String> amount = getTagAttributeValues(transaction, "eamount");
                    amount.forEach(a -> doc.add(new DoublePoint("amount", Double.parseDouble(a))));
                    amount.forEach(a -> doc.add(new TextField("amountStr", a, Field.Store.YES)));

                    List<String> desc = getTagAttributeValues(transaction, "edesc");
                    desc.forEach(d -> doc.add(new TextField("desc", d, Field.Store.YES)));

                    List<String> checkNbr = getTagAttributeValues(transaction, "enbr");
                    checkNbr.forEach(c -> doc.add(new LongPoint("checkNbrStr", Long.parseLong(c))));
                    checkNbr.forEach(c -> doc.add(new TextField("checkNbrStr", c, Field.Store.YES)));

                    List<String> date = getTagAttributeValues(transaction, "edate");
                    date.forEach(d -> doc.add(new TextField("date", d, Field.Store.YES)));

                    writer.addDocument(doc);
                }
                tokenStream.end();
            }
            writer.close();
        }


        var reader = DirectoryReader.open(directory);
        var searcher = new IndexSearcher(reader);

        var rangeQuery = DoublePoint.newRangeQuery("amount_double", 20, 30);
        var results = searcher.search(rangeQuery, 10);


        for (var scoreDoc : results.scoreDocs) {
            Document resultDoc = searcher.doc(scoreDoc.doc);
            System.out.println("Found: ");
            Arrays.stream(resultDoc.getFields("amount")).forEach(field -> System.out.println(field.stringValue()));
        }
        directory.close();

        Files.list(indexPath).forEach(f->f.toFile().delete());
        Files.delete(indexPath);

    }

    private static List<String> getTagAttributeValues(String token, String attributeName) throws IOException {
        List<String> list = new ArrayList<>();
        try(Analyzer htmlAttributeAnalyzer = new TagAttributeValueAnalyzer(attributeName)) {
            try (TokenStream htmlAttributeTokenStream = htmlAttributeAnalyzer.tokenStream("content", new StringReader(token))) {
                CharTermAttribute htmlAttributeCharTermAttr = htmlAttributeTokenStream.addAttribute(CharTermAttribute.class);
                htmlAttributeTokenStream.reset();
                while (htmlAttributeTokenStream.incrementToken()) {
                    String attrValue = htmlAttributeCharTermAttr.toString();
                    list.add(attrValue);
//                System.out.println("\tHTML Attr value: " + attrValue);
                }
                htmlAttributeTokenStream.end();
            }
        }
        return list;
    }
}
