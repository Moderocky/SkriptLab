package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.annotation.Effect;
import mx.kenzie.skriptlab.error.AbnormalSyntaxCreationError;
import mx.kenzie.skriptlab.error.PatternCompatibilityException;
import mx.kenzie.skriptlab.error.SyntaxCreationException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyntaxExtractor {
    
    protected final SyntaxGenerator generator;
    protected Class<?> source;
    protected Set<MaybeSyntax> syntax;
    protected Set<MaybeEffect> effects;
    
    protected SyntaxExtractor(SyntaxGenerator generator) {this.generator = generator;}
    
    public void prepare(Class<?> source) {
        this.source = source;
    }
    
    public void divine()
        throws PatternCompatibilityException {
        this.syntax = new HashSet<>();
        this.effects = new HashSet<>();
        //<editor-fold desc="Find things that look like effects" defaultstate="collapsed">
        for (final Method method : source.getMethods()) {
            if (!method.isAnnotationPresent(Effect.class)) continue;
            final MaybeEffect effect = new MaybeEffect(method, method.getAnnotation(Effect.class));
            effect.verify();
            this.syntax.add(effect);
            this.effects.add(effect);
        }
        //</editor-fold>
    }
    
    public void makeSyntax(List<Syntax<?>> list) {
        for (final MaybeSyntax syntax : syntax) list.add(syntax.generate());
    }
    
    protected interface MaybeSyntax {
        
        void verify() throws PatternCompatibilityException;
        
        Syntax<?> generate() throws AbnormalSyntaxCreationError, SyntaxCreationException;
        
    }
    
    protected record MaybeEffect(Method method, Effect effect) implements MaybeSyntax {
        
        @Override
        public void verify() throws PatternCompatibilityException {
            //<editor-fold desc="Make sure patterns have right %inputs% for method" defaultstate="collapsed">
            final int expectedInputs;
            if (Modifier.isStatic(method.getModifiers())) expectedInputs = method.getParameterCount();
            else expectedInputs = method.getParameterCount() + 1;
            final String[] strings = effect.value();
            if (strings.length < 1 && expectedInputs > 1)
                throw new PatternCompatibilityException("Unable to invent a pattern for " + method);
            for (final String string : strings) {
                final PatternDigest digest = new PatternDigest(string);
                digest.digest();
                if (expectedInputs > digest.getInputs())
                    throw new PatternCompatibilityException(
                        "Pattern `" + string + "` has too few inputs to invoke " + method);
                if (expectedInputs < digest.getInputs())
                    throw new PatternCompatibilityException(
                        "Pattern `" + string + "` has too many inputs to invoke " + method);
            }
            //</editor-fold>
        }
        
        @Override
        public Syntax<?> generate() throws AbnormalSyntaxCreationError, SyntaxCreationException {
            return null; // todo
        }
        
    }
    
}
