package analyzers;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexCaptureTokenizer extends Tokenizer {
    private final CharTermAttribute termAttr = addAttribute(CharTermAttribute.class);
    private final Pattern pattern;
    private final StringBuilder buffer = new StringBuilder();
    private Matcher matcher;
    private boolean done = false;

    public RegexCaptureTokenizer(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (matcher == null) {
            fillBuffer();
            matcher = pattern.matcher(buffer);
        }

        while (!done) {
            if (matcher.find()) {
                String match = matcher.group();
                clearAttributes();
                termAttr.append(match);
                return true;
            } else {
                done = true;
            }
        }

        return false;
    }

    private void fillBuffer() throws IOException {
        char[] charBuffer = new char[1024];
        int length;
        while ((length = input.read(charBuffer)) != -1) {
            buffer.append(charBuffer, 0, length);
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        buffer.setLength(0);
        matcher = null;
        done = false;
    }
}