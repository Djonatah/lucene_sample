package analyzers;

import org.apache.lucene.analysis.Analyzer;

public class HTMLAttributeValueAnalyzer extends Analyzer {
    private final String attributeName;

    public HTMLAttributeValueAnalyzer(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        RegExCaptureTokenizer tokenizer = new RegExCaptureTokenizer(attributeName + "\\s*?=\\s*?\".*?\"");
        HTMLAttributeValueFilter filter = new HTMLAttributeValueFilter(tokenizer, attributeName);
        return new TokenStreamComponents(tokenizer, filter);
    }
}