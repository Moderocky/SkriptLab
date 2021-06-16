package mx.kenzie.skriptlab.test;

import ch.njol.skript.util.Getter;
import mx.kenzie.skriptlab.SyntaxGenerator;
import mx.kenzie.skriptlab.annotation.*;
import mx.kenzie.skriptlab.error.SyntaxCreationException;
import mx.kenzie.skriptlab.template.*;
import org.bukkit.event.Event;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GenerationTest {
    
    public boolean test;
    
    @Test
    public void sample() throws Throwable {
        new SyntaxGenerator() {
            @Override
            protected synchronized Class<GeneratedEffect> generateEffectClass(Method binder) throws SyntaxCreationException {
                return super.generateEffectClass(binder);
            }
        }.generateEffectClass(GenerationTest.class.getDeclaredMethod("blob"));
        new SyntaxGenerator() {
            @Override
            protected synchronized Class<GeneratedPropertyCondition<?>> generatePropertyConditionClass() throws SyntaxCreationException {
                return super.generatePropertyConditionClass();
            }
        }.generatePropertyConditionClass();
        new SyntaxGenerator() {
            @Override
            protected synchronized <Expr> Class<GeneratedPropertyExpression<Expr>> generatePropertyExpressionClass(Field binder) throws SyntaxCreationException {
                return super.generatePropertyExpressionClass(binder);
            }
        }.generatePropertyExpressionClass(getClass().getField("test"));
        new SyntaxGenerator() {
            @Override
            protected synchronized <Expr> Class<GeneratedSimpleExpression<Expr>> generateSimpleExpressionClass() throws SyntaxCreationException {
                return super.generateSimpleExpressionClass();
            }
        }.generateSimpleExpressionClass();
        new SyntaxGenerator() {
            @Override
            protected synchronized Class<GeneratedEvent> generateEventClass() throws SyntaxCreationException {
                return super.generateEventClass();
            }
        }.generateEventClass();
        new SyntaxGenerator() {
            @Override
            protected synchronized <EventType extends Event, Value> Class<? extends Getter<Value, EventType>> generateGetterClass(Method binder) throws SyntaxCreationException {
                return super.generateGetterClass(binder);
            }
        }.generateGetterClass(this.getClass().getMethod("blob"));
    }
    
    //region Stubs
    @Effect
    public static void blob() {
    
    }
    
    
    //end-region
    
}
