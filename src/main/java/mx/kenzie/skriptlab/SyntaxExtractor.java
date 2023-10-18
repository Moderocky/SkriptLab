package mx.kenzie.skriptlab;

import mx.kenzie.skriptlab.annotation.*;
import mx.kenzie.skriptlab.error.AbnormalSyntaxCreationError;
import mx.kenzie.skriptlab.error.PatternCompatibilityException;
import mx.kenzie.skriptlab.error.SyntaxCreationException;
import mx.kenzie.skriptlab.template.DirectCondition;
import mx.kenzie.skriptlab.template.DirectEffect;
import mx.kenzie.skriptlab.template.DirectExpression;
import mx.kenzie.skriptlab.template.DirectPropertyCondition;

import java.lang.reflect.*;
import java.util.*;

public class SyntaxExtractor {
    
    protected final SyntaxGenerator generator;
    protected Class<?> source;
    protected Set<MaybeSyntax> syntax;
    protected Set<MaybeEffect> effects;
    protected Set<MaybeCondition> conditions;
    protected Set<MaybeExpression> expressions;
    
    protected SyntaxExtractor(SyntaxGenerator generator) {this.generator = generator;}
    
    public void prepare(Class<?> source) {
        this.source = source;
        this.syntax = new HashSet<>();
        this.effects = new HashSet<>();
        this.conditions = new HashSet<>();
        this.expressions = new HashSet<>();
    }
    
    public void divine() throws PatternCompatibilityException {
        //<editor-fold desc="Find things that look like syntax" defaultstate="collapsed">
        for (final Method method : source.getMethods()) {
            this.divineEffect(method);
            this.divineCondition(method);
            this.divinePropertyCondition(method);
            this.divineExpression(method);
            this.divinePropertyExpression(method);
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
    
    protected MaybeExpression divineExpression(Method method) {
        //<editor-fold desc="Make this method look like an expression" defaultstate="collapsed">
        if (!method.isAnnotationPresent(Expression.class)) return null;
        final MaybeExpression expression = this.getExpression(method, method.getAnnotation(Expression.class));
        expression.verify();
        this.syntax.add(expression);
        this.expressions.add(expression);
        return expression;
        //</editor-fold>
    }
    
    protected MaybeExpression divinePropertyExpression(Method method) {
        //<editor-fold desc="Make this method look like an expression" defaultstate="collapsed">
        if (!method.isAnnotationPresent(PropertyExpression.class)) return null;
        if (Modifier.isStatic(method.getModifiers()))
            throw new PatternCompatibilityException("Property expressions can't be generated from static methods.");
        final PropertyExpression initial = method.getAnnotation(PropertyExpression.class);
        final Expression meta = Expression.Converted.converted(initial, method);
        final MaybeExpression expression = this.getExpression(method, meta);
        expression.verify();
        this.syntax.add(expression);
        this.expressions.add(expression);
        return expression;
        //</editor-fold>
    }
    
    private MaybeExpression getExpression(Method method, Expression expression) {
        final AccessMode mode = expression.mode();
        final String[] patterns = this.makePattern(method, expression.value());
        for (final MaybeExpression maybe : expressions) {
            if (!Arrays.equals(maybe.pattern, patterns)) continue;
            if (maybe.changers.containsKey(mode)) throw new PatternCompatibilityException(
                "Expression `" + patterns[0] + "` already has a " + mode + " accessor.");
            maybe.changers.put(mode, new MaybeExpression.Pair(method, expression));
            return maybe;
        }
        return new MaybeExpression(method, expression);
    }
    
    public void makeSyntax(List<Syntax<?>> list) {
        for (final MaybeSyntax syntax : syntax) list.add(syntax.generate());
    }
    
    protected String[] makePattern(AnnotatedElement member, String... alternate) {
        if (alternate != null && alternate.length > 0) return alternate;
        return new String[]{this.makePattern(member)};
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
                else if (found.startsWith("has ") || found.startsWith("was ") || found.startsWith("can "))
                    pattern = found.substring(4).trim();
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
    
    protected class MaybeExpression implements MaybeSyntax {
        
        protected final String[] pattern;
        protected final Map<AccessMode, Pair> changers = new HashMap<>();
        protected Pair base;
        
        protected MaybeExpression(Method method, Expression expression) {
            this.pattern = makePattern(method, expression.value());
            this.changers.put(expression.mode(), new Pair(method, expression));
        }
        
        @Override
        public void verify() throws PatternCompatibilityException {
            //<editor-fold desc="Make sure patterns have right %inputs% for method" defaultstate="collapsed">
            for (final Map.Entry<AccessMode, Pair> entry : changers.entrySet()) {
                final Pair value = entry.getValue();
                final Method method = value.method;
                final AccessMode mode = entry.getKey();
                final int expectedInputs;
                if (Modifier.isStatic(method.getModifiers())) expectedInputs = method.getParameterCount();
                else expectedInputs = method.getParameterCount() + 1;
                if (mode == AccessMode.GET) {
                    if (method.getReturnType() == void.class)
                        throw new PatternCompatibilityException("No value is returned from getter " + method);
                    for (final String string : pattern) {
                        final PatternDigest digest = new PatternDigest(string);
                        digest.digest();
                        if (expectedInputs > digest.getInputs()) throw new PatternCompatibilityException(
                            "Pattern `" + string + "` has too few inputs to invoke " + method);
                        if (expectedInputs < digest.getInputs()) throw new PatternCompatibilityException(
                            "Pattern `" + string + "` has too many inputs to invoke " + method);
                    }
                } else {
                    final boolean needsSource = !Modifier.isStatic(method.getModifiers());
                    if (needsSource && mode.expectArguments && expectedInputs < 2)
                        throw new PatternCompatibilityException(mode + " handler has no input parameter in " + method);
                    else if ((mode.expectArguments && expectedInputs > 2) || (!mode.expectArguments && expectedInputs > 1))
                        throw new PatternCompatibilityException(
                            mode + " handler has too many input parameters in " + method);
                    if (mode.expectReturn && method.getReturnType() == void.class)
                        throw new PatternCompatibilityException(
                            "No value is returned from " + mode + " handler " + method);
                }
            }
            //</editor-fold>
        }
        
        @Override
        @SuppressWarnings({"unchecked", "RawUseOfParameterized"})
        public Syntax<?> generate() throws AbnormalSyntaxCreationError, SyntaxCreationException {
            //<editor-fold desc="Create a syntax that calls the method" defaultstate="collapsed">
            this.base = changers.get(AccessMode.GET);
            if (base == null)
                throw new SyntaxCreationException("No getter was provided for expression `" + pattern[0] + "`");
            final String className = "DirectExpression" + generator.nextClassIndex();
            final Class returnType = base.method.getReturnType();
            final DirectExpression handler;
            try (final Maker maker = new DirectExpressionMaker(className, this, pattern)) {
                final Class<?> type = maker.make(generator);
                final Object object = type.getDeclaredConstructor(Expression.class).newInstance(base.meta);
                assert object instanceof DirectExpression;
                handler = (DirectExpression) object;
            } catch (Exception ex) {
                throw new SyntaxCreationException("Unable to create syntax link.", ex);
            }
            return generator.createExpression(returnType, handler, pattern);
            //</editor-fold>
        }
        
        protected record Pair(Method method, Expression meta) {}
        
    }
    
}
