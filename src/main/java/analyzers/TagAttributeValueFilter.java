package analyzers;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagAttributeValueFilter extends TokenFilter {
    private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
    String attributeName;
    private Pattern attributePattern;

    protected TagAttributeValueFilter(TokenStream input, String attributeName) {
        super(input);
        this.attributeName = attributeName;
        this.attributePattern = Pattern.compile(attributeName + "\\s*?=\\s*?\"(.*)\"");
    }

    @Override
    public boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            String token = termAttr.toString();
            if(token.contains(attributeName)){
                termAttr.setEmpty().append(stripAttributeName(token));
                return true;
            }
        }
        return false;
    }
    private String stripAttributeName(String token) {
        Matcher matcher = attributePattern.matcher(token);
        return matcher.replaceAll("$1");
    }
}
