package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.error.PatternDigestException;

import java.util.ArrayList;
import java.util.List;

public class PatternDigest {
    
    public final String pattern;
    private boolean digested;
    private int inputs;
    private Object[] parts;
    
    public PatternDigest(String pattern) {this.pattern = pattern;}
    
    protected static Object[] interpolations(String pattern) {
        final List<Object> parts = new ArrayList<>(8);
        StringBuilder builder = new StringBuilder();
        int skip = 0, split = 0;
        boolean ignore = false, input = false;
        for (final char c : pattern.toCharArray()) {
            switch (c) {
                case '%':
                    if (ignore || skip > 0) break;
                    if (input) parts.add(new Input());
                    else {
                        final String current = builder.toString();
                        if (!current.isBlank()) parts.add(current);
                        builder = new StringBuilder();
                    }
                    input = !input;
                    break;
                case '[':
                    ++skip;
                    break;
                case ']':
                    --skip;
                    break;
                case '(':
                    if (ignore || skip > 0) break;
                    ++split;
                    break;
                case ')':
                    if (ignore || skip > 0) break;
                    --split;
                    if (split < 1) ignore = false;
                    break;
                case '|':
                    if (ignore) break;
                    if (split > 0) ignore = true;
                    break;
                case ' ':
                    if (builder.isEmpty() || builder.charAt(builder.length() - 1) == ' ') break;
                default:
                    if (skip > 0 || ignore || input) break;
                    builder.append(c);
            }
        }
        final String current = builder.toString();
        if (!current.isBlank()) parts.add(current);
        return parts.toArray();
    }
    
    public void digest() throws PatternDigestException {
        this.parts = interpolations(pattern);
        this.digested = true;
        int count = 0;
        for (final Object part : parts) if (part instanceof Input) ++count;
        this.inputs = count;
    }
    
    public String example(Object... inputs) throws PatternDigestException {
        if (inputs.length < this.inputs)
            throw new PatternDigestException("Too few inputs provided! Expected " +
                this.inputs + " and got " + inputs.length);
        if (!digested) this.digest();
        int input = 0;
        final StringBuilder builder = new StringBuilder();
        for (final Object part : parts) {
            if (part instanceof Input) builder.append(inputs[input++]);
            else builder.append(part);
        }
        return builder.toString().trim();
    }
    
    protected static class Input {}
    
}
