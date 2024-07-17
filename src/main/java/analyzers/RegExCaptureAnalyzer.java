package analyzers;

import org.apache.lucene.analysis.Analyzer;

public class RegExCaptureAnalyzer extends Analyzer {
    private final String captureRegExp;
    private final String filterRegex;

    public RegExCaptureAnalyzer(String captureRegExp, String filterRegex) {
        this.captureRegExp = captureRegExp;
        this.filterRegex = filterRegex;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        RegExCaptureTokenizer tokenizer = new RegExCaptureTokenizer(captureRegExp);
        RegExTokenFilter filter = new RegExTokenFilter(tokenizer, filterRegex);
        return new TokenStreamComponents(tokenizer, filter);
    }
}