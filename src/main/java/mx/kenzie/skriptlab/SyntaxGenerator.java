package mx.kenzie.skriptlab;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.registrations.Classes;
import mx.kenzie.skriptlab.error.SyntaxCreationException;
import mx.kenzie.skriptlab.template.DirectCondition;
import mx.kenzie.skriptlab.template.DirectEffect;
import mx.kenzie.skriptlab.template.DirectExpression;
import mx.kenzie.skriptlab.template.DirectPropertyCondition;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SyntaxGenerator extends ClassLoader {
    
    private final AtomicInteger generatedNumber;
    
    public SyntaxGenerator() {
        super(SyntaxGenerator.class.getClassLoader());
        this.generatedNumber = new AtomicInteger(0);
    }
    
    protected int nextClassIndex() {
        return generatedNumber.incrementAndGet();
    }
    
    /**
     * Reads all special syntax annotations from a class and generates their syntax handlers.
     *
     * @param source the source class to read
     * @return a collection of the unregistered syntax objects
     */
    public Collection<Syntax<?>> generateSyntaxFrom(Class<?> source) {
        final List<Syntax<?>> list = new ArrayList<>();
        final SyntaxExtractor extractor = new SyntaxExtractor(this);
        extractor.prepare(source);
        extractor.divine();
        extractor.makeSyntax(list);
        return list;
    }
    
    public Syntax<DirectEffect> createEffect(DirectEffect handler, String... patterns) {
        final String name = "GeneratedEffect" + this.nextClassIndex();
        return this.createSyntax(new EffectMaker(name, handler, patterns), handler, patterns);
    }
    
    public Syntax<DirectCondition> createCondition(DirectCondition handler, String... patterns) {
        final String name = "GeneratedCondition" + this.nextClassIndex();
        return this.createSyntax(new ConditionMaker(name, handler, patterns), handler, patterns);
    }
    
    public <Type> Syntax<DirectPropertyCondition<Type>> createPropertyCondition(DirectPropertyCondition<Type> handler, Class<Type> holder, String property) {
        return this.createPropertyCondition(handler, holder, property, PropertyCondition.PropertyType.BE);
    }
    
    public <Type> Syntax<DirectPropertyCondition<Type>> createPropertyCondition(DirectPropertyCondition<Type> handler, Class<Type> holder, String property, PropertyCondition.PropertyType propertyType) {
        final String name = "GeneratedCondition" + this.nextClassIndex();
        final ClassInfo<?> info = Classes.getExactClassInfo(holder); // we do our best if this isn't registered yet
        final String type;
        if (info == null) type = holder.getSimpleName().toLowerCase();
        else type = info.getCodeName();
        final String pattern, negated;
        pattern = switch (propertyType) {
            case BE -> "%" + type + "% (is|are) " + property;
            case CAN -> "%" + type + "% can " + property;
            case HAVE -> "%" + type + "% (has|have) " + property;
        };
        negated = switch (propertyType) {
            case BE -> "%" + type + "% (isn't|is not|aren't|are not) " + property;
            case CAN -> "%" + type + "% (can't|cannot|can not) " + property;
            case HAVE -> "%" + type + "% (doesn't|does not|do not|don't) have " + property;
        };
        return this.createSyntax(new PropertyConditionMaker(name, handler, property), handler, pattern, negated);
    }
    
    public <Type> Syntax<DirectExpression<Type>> createExpression(Class<Type> returnType, DirectExpression.Single<Type> handler, String... patterns) {
        return this.createExpression(returnType, (DirectExpression<Type>) handler, patterns);
    }
    
    public <Type> Syntax<DirectExpression<Type>> createExpression(Class<Type> returnType, DirectExpression<Type> handler, String... patterns) {
        final String name = "GeneratedExpression" + this.nextClassIndex();
        return this.createSyntax(new ExpressionMaker(returnType, name, handler, patterns), handler, patterns);
    }
    
    @SuppressWarnings("unchecked")
    private <Any extends SyntaxElement> Syntax<Any> createSyntax(Maker maker, SyntaxElement handler, String... patterns) {
        try (maker) {
            final Class<? extends SyntaxElement> type = (Class<? extends SyntaxElement>) maker.make(this);
            this.insertData(type, handler, patterns);
            return (Syntax<Any>) new Syntax<>(type, handler, patterns);
        } catch (Exception ex) {
            throw new SyntaxCreationException("Unable to create syntax holder.", ex);
        }
    }
    
    private void insertData(Class<?> type, SyntaxElement handle, String... patterns)
        throws NoSuchFieldException, IllegalAccessException {
        final Field handleField = type.getDeclaredField("handle");
        handleField.set(null, handle);
        try {
            final Field patternsField = type.getDeclaredField("patterns");
            patternsField.set(null, patterns);
        } catch (NoSuchFieldException ignored) {
            // properties don't need to know their patterns
        }
    }
    
    protected Class<?> loadClass(String name, byte[] bytecode) {
        return super.defineClass("mx.kenzie.skriptlab.generated." + name, bytecode, 0, bytecode.length);
    }
    
}

