package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.annotation.Type;
import mx.kenzie.skriptlab.error.PatternCompatibilityException;

import java.util.*;

public class TypeExtractor extends Extractor {
    
    protected List<Class<?>> found;
    
    public TypeExtractor(SyntaxGenerator generator) {
        super(generator);
    }
    
    @Override
    public void prepare(Class<?> source) {
        super.prepare(source);
        this.found = new ArrayList<>();
    }
    
    @Override
    public void divine() throws PatternCompatibilityException {
        if (source == null || found == null) return;
        final Set<Class<?>> classes = new HashSet<>();
        this.checkClass(source, classes);
        this.found.addAll(classes);
    }
    
    @Override
    public void collect(List<Registered> list) {
        for (final Class<?> type : found) {
            final String codeName;
            final String[] patterns;
            final Type meta = type.getAnnotation(Type.class);
            if (meta.codeName().isBlank()) codeName = type.getSimpleName().toLowerCase().replace("_", "");
            else codeName = meta.codeName().trim().toLowerCase().replace("_", "");
            if (meta.value().length == 0)
                patterns = new String[]{new PatternCreator(type.getSimpleName()).getPattern()};
            else patterns = meta.value();
            final TypeInfo info = new TypeInfo(type, codeName, patterns);
            list.add(info);
        }
    }
    
    protected void checkClass(Class<?> type, Collection<Class<?>> list) {
        if (type.isAnnotationPresent(Type.class)) list.add(type);
        for (final Class<?> inner : type.getClasses()) this.checkClass(inner, list);
    }
    
}
