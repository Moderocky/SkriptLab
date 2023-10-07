package mx.kenzie.skriptlab;

import ch.njol.skript.lang.SyntaxElement;
import mx.kenzie.skriptlab.template.DirectCondition;
import mx.kenzie.skriptlab.template.DirectEffect;
import mx.kenzie.skriptlab.template.DirectExpression;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class SyntaxGenerator extends ClassLoader {
    
    private final AtomicInteger generatedNumber;
    
    public SyntaxGenerator() {
        super(SyntaxGenerator.class.getClassLoader());
        this.generatedNumber = new AtomicInteger(0);
    }
    
    public Syntax createEffect(DirectEffect handler, String... patterns) {
        final String name = "GeneratedEffect" + generatedNumber.incrementAndGet();
        return this.createSyntax(new Maker.EffectMaker(name, handler, patterns), handler, patterns);
    }
    
    public Syntax createCondition(DirectCondition handler, String... patterns) {
        final String name = "GeneratedCondition" + generatedNumber.incrementAndGet();
        return this.createSyntax(new Maker.ConditionMaker(name, handler, patterns), handler, patterns);
    }
    
    public <Type> Syntax createExpression(Class<Type> returnType, DirectExpression.Single<Type> handler, String... patterns) {
        return this.createExpression(returnType, (DirectExpression<Type>) handler, patterns);
    }
    
    public <Type> Syntax createExpression(Class<Type> returnType, DirectExpression<Type> handler, String... patterns) {
        final String name = "GeneratedExpression" + generatedNumber.incrementAndGet();
        return this.createSyntax(new Maker.ExpressionMaker(returnType, name, handler, patterns), handler, patterns);
    }
    
    @SuppressWarnings("unchecked")
    private Syntax createSyntax(Maker maker, SyntaxElement handler, String... patterns) {
        try (maker) {
            final Class<? extends SyntaxElement> type = (Class<? extends SyntaxElement>) maker.make(this);
            this.insertData(type, handler, patterns);
            return new Syntax(type, patterns);
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }
    
    private void insertData(Class<?> type, SyntaxElement handle, String... patterns)
        throws NoSuchFieldException, IllegalAccessException {
        final Field handleField = type.getDeclaredField("handle");
        handleField.set(null, handle);
        final Field patternsField = type.getDeclaredField("patterns");
        patternsField.set(null, patterns);
    }
    
    protected Class<?> loadClass(String name, byte[] bytecode) {
        return super.defineClass("mx.kenzie.skriptlab.generated." + name, bytecode, 0, bytecode.length);
    }
    
}

