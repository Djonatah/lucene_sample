package analyzers;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class HTMLAttributeValueFilter extends TokenFilter {
    private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
    String attributeName;

    protected HTMLAttributeValueFilter(TokenStream input, String attributeName) {
        super(input);
        this.attributeName = attributeName;
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
        return token.replaceAll(attributeName + "\\s*?=\\s*?\"(.*)\"", "$1");
    }
}
