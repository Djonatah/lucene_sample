package analyzers;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExTokenFilter extends TokenFilter {
    private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
    Pattern pattern;

    protected RegExTokenFilter(TokenStream input, String tokenFilterRegExp) {
        super(input);
        this.pattern = Pattern.compile(tokenFilterRegExp);
    }

    @Override
    public boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            String token = termAttr.toString();
            Matcher matcher = pattern.matcher(token);

            if(matcher.find())
                return true;
        }
        return false;
    }
}
