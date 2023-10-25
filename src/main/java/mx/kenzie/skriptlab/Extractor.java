package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.error.PatternCompatibilityException;

import java.util.List;

public abstract class Extractor {
    
    protected final SyntaxGenerator generator;
    protected Class<?> source;
    
    public Extractor(SyntaxGenerator generator) {
        this.generator = generator;
    }
    
    public void prepare(Class<?> source) {
        this.source = source;
    }
    
    public abstract void divine() throws PatternCompatibilityException;
    
    public abstract void collect(List<Registered> list);
    
}
