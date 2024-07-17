package analyzers;

import org.apache.lucene.analysis.Analyzer;

public class TagAttributeValueAnalyzer extends Analyzer {
    private final String attributeName;

    public TagAttributeValueAnalyzer(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        RegExCaptureTokenizer tokenizer = new RegExCaptureTokenizer(attributeName + "\\s*?=\\s*?\".*?\"");
        TagAttributeValueFilter filter = new TagAttributeValueFilter(tokenizer, attributeName);
        return new TokenStreamComponents(tokenizer, filter);
    }
}