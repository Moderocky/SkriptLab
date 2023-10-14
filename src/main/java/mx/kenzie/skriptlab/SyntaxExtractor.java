package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.annotation.Condition;
import mx.kenzie.skriptlab.annotation.Effect;
import mx.kenzie.skriptlab.annotation.PropertyCondition;
import mx.kenzie.skriptlab.error.AbnormalSyntaxCreationError;
import mx.kenzie.skriptlab.error.PatternCompatibilityException;
import mx.kenzie.skriptlab.error.SyntaxCreationException;
import mx.kenzie.skriptlab.template.DirectCondition;
import mx.kenzie.skriptlab.template.DirectEffect;
import mx.kenzie.skriptlab.template.DirectPropertyCondition;

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SyntaxExtractor {
    
    protected final SyntaxGenerator generator;
    protected Class<?> source;
    protected Set<MaybeSyntax> syntax;
    protected Set<MaybeEffect> effects;
    protected Set<MaybeCondition> conditions;
    
    protected SyntaxExtractor(SyntaxGenerator generator) {this.generator = generator;}
    
    public void prepare(Class<?> source) {
        this.source = source;
        this.syntax = new HashSet<>();
        this.effects = new HashSet<>();
        this.conditions = new HashSet<>();
    }
    
    public void divine() throws PatternCompatibilityException {
        //<editor-fold desc="Find things that look like syntax" defaultstate="collapsed">
        for (final Method method : source.getMethods()) {
            this.divineEffect(method);
            this.divineCondition(method);
            this.divinePropertyCondition(method);
        }
        //</editor-fold>
    }
    
    protected MaybeEffect divineEffect(Method method) {
        //<editor-fold desc="Make this method look like an effect" defaultstate="collapsed">
        if (!method.isAnnotationPresent(Effect.class)) return null;
        final MaybeEffect effect = new MaybeEffect(method, method.getAnnotation(Effect.class));
        effect.verify();
        this.syntax.add(effect);
        this.effects.add(effect);
        return effect;
        //</editor-fold>
    }
    
    protected MaybeCondition divineCondition(Method method) {
        //<editor-fold desc="Make this method look like a condition" defaultstate="collapsed">
        if (!method.isAnnotationPresent(Condition.class)) return null;
        final MaybeCondition condition = new MaybeCondition(method, method.getAnnotation(Condition.class));
        condition.verify();
        this.syntax.add(condition);
        this.conditions.add(condition);
        return condition;
        //</editor-fold>
    }
    
    protected MaybePropertyCondition divinePropertyCondition(Method method) {
        //<editor-fold desc="Make this method look like a condition" defaultstate="collapsed">
        if (Modifier.isStatic(method.getModifiers())) return null;
        if (!method.isAnnotationPresent(PropertyCondition.class)) return null;
        final MaybePropertyCondition condition = new MaybePropertyCondition(method,
            method.getAnnotation(PropertyCondition.class));
        condition.verify();
        this.syntax.add(condition);
        this.conditions.add(condition);
        return condition;
        //</editor-fold>
    }
    
    public void makeSyntax(List<Syntax<?>> list) {
        for (final MaybeSyntax syntax : syntax) list.add(syntax.generate());
    }
    
    protected String makePattern(AnnotatedElement member) {
        if (member instanceof Method method) return new PatternCreator(method.getName()).getPattern();
        else if (member instanceof Field field) return new PatternCreator(field.getName()).getPattern();
        else if (member instanceof Constructor<?> thing) return new PatternCreator(thing.getName()).getPattern();
        else if (member instanceof Class<?> type) return new PatternCreator(type.getSimpleName()).getPattern();
        else throw new SyntaxCreationException("Unable to invent a pattern for " + member);
    }
    
    protected interface MaybeSyntax {
        
        void verify() throws PatternCompatibilityException;
        
        Syntax<?> generate() throws AbnormalSyntaxCreationError, SyntaxCreationException;
        
    }
    
    protected class MaybeEffect implements MaybeSyntax {
        
        protected final Method method;
        protected final Effect effect;
        
        protected MaybeEffect(Method method, Effect effect) {
            this.method = method;
            this.effect = effect;
        }
        
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
                if (expectedInputs > digest.getInputs()) throw new PatternCompatibilityException(
                    "Pattern `" + string + "` has too few inputs to invoke " + method);
                if (expectedInputs < digest.getInputs()) throw new PatternCompatibilityException(
                    "Pattern `" + string + "` has too many inputs to invoke " + method);
            }
            //</editor-fold>
        }
        
        @Override
        public Syntax<?> generate() throws AbnormalSyntaxCreationError, SyntaxCreationException {
            //<editor-fold desc="Create a syntax that calls the method" defaultstate="collapsed">
            final String className = "DirectEffect" + generator.nextClassIndex();
            final String[] patterns;
            patterns = effect.value().length > 0 ? effect.value() : new String[]{makePattern(method)};
            final DirectEffect handler;
            try (final Maker maker = new DirectEffectMaker(className, this, patterns)) {
                final Class<?> type = maker.make(generator);
                final Object object = type.getDeclaredConstructor(Effect.class).newInstance(effect);
                assert object instanceof DirectEffect;
                handler = (DirectEffect) object;
            } catch (Exception ex) {
                throw new SyntaxCreationException("Unable to create syntax link.", ex);
            }
            return generator.createEffect(handler, patterns);
            //</editor-fold>
        }
        
    }
    
    protected class MaybeCondition implements MaybeSyntax {
        
        protected final Method method;
        protected final Condition condition;
        
        protected MaybeCondition(Method method, Condition condition) {
            this.method = method;
            this.condition = condition;
        }
        
        @Override
        public void verify() throws PatternCompatibilityException {
            //<editor-fold desc="Make sure patterns have right %inputs% for method" defaultstate="collapsed">
            if (method.getReturnType() != boolean.class)
                throw new PatternCompatibilityException("Non-boolean return type for " + method);
            final int expectedInputs;
            if (Modifier.isStatic(method.getModifiers())) expectedInputs = method.getParameterCount();
            else expectedInputs = method.getParameterCount() + 1;
            final String[] strings = condition.pattern();
            if (strings.length < 1 && expectedInputs > 1)
                throw new PatternCompatibilityException("Unable to invent a pattern for " + method);
            for (final String string : strings) {
                final PatternDigest digest = new PatternDigest(string);
                digest.digest();
                if (expectedInputs > digest.getInputs()) throw new PatternCompatibilityException(
                    "Pattern `" + string + "` has too few inputs to invoke " + method);
                if (expectedInputs < digest.getInputs()) throw new PatternCompatibilityException(
                    "Pattern `" + string + "` has too many inputs to invoke " + method);
            }
            //</editor-fold>
        }
        
        @Override
        public Syntax<?> generate() throws AbnormalSyntaxCreationError, SyntaxCreationException {
            //<editor-fold desc="Create a syntax that calls the method" defaultstate="collapsed">
            final String className = "DirectCondition" + generator.nextClassIndex();
            final String[] patterns;
            patterns = condition.pattern().length > 0 ? condition.pattern() : new String[]{makePattern(method)};
            final DirectCondition handler;
            try (final Maker maker = new DirectConditionMaker(className, this, patterns)) {
                final Class<?> type = maker.make(generator);
                final Object object = type.getDeclaredConstructor(Condition.class).newInstance(condition);
                assert object instanceof DirectCondition;
                handler = (DirectCondition) object;
            } catch (Exception ex) {
                throw new SyntaxCreationException("Unable to create syntax link.", ex);
            }
            return generator.createCondition(handler, patterns);
            //</editor-fold>
        }
        
    }
    
    protected class MaybePropertyCondition extends MaybeCondition {
        
        protected final Method method;
        protected final PropertyCondition condition;
        
        protected MaybePropertyCondition(Method method, PropertyCondition condition) {
            super(method, null);
            this.method = method;
            this.condition = condition;
        }
        
        @Override
        public void verify() throws PatternCompatibilityException {
            //<editor-fold desc="Make sure the method is ok" defaultstate="collapsed">
            if (method.getReturnType() != boolean.class)
                throw new PatternCompatibilityException("Non-boolean return type for " + method);
            if (Modifier.isStatic(method.getModifiers()))
                throw new PatternCompatibilityException("Property condition " + method + " is static");
            //</editor-fold>
        }
        
        @Override
        @SuppressWarnings({"unchecked", "RawUseOfParameterized"})
        public Syntax<?> generate() throws AbnormalSyntaxCreationError, SyntaxCreationException {
            //<editor-fold desc="Create a syntax that calls the method" defaultstate="collapsed">
            final String className = "DirectPropertyCondition" + generator.nextClassIndex();
            final String pattern;
            if (condition.pattern().isBlank()) {
                final String found = makePattern(method);
                if (found.startsWith("is ")) pattern = found.substring(3).trim();
                else if (found.startsWith("has ")
                    || found.startsWith("was ")
                    || found.startsWith("can ")) pattern = found.substring(4).trim();
                else pattern = found.trim();
            } else pattern = condition.pattern().trim();
            final DirectPropertyCondition handler;
            try (final Maker maker = new DirectPropertyConditionMaker(className, this, pattern)) {
                final Class<?> type = maker.make(generator);
                final Object object = type.getDeclaredConstructor(PropertyCondition.class).newInstance(condition);
                assert object instanceof DirectPropertyCondition;
                handler = (DirectPropertyCondition) object;
            } catch (Exception ex) {
                throw new SyntaxCreationException("Unable to create syntax link.", ex);
            }
            return generator.createPropertyCondition(handler, (Class) method.getDeclaringClass(), pattern);
            //</editor-fold>
        }
        
    }
    
}
