package mx.kenzie.skriptlab.test;

import mx.kenzie.skriptlab.SyntaxGenerator;
import mx.kenzie.skriptlab.annotation.*;
import mx.kenzie.skriptlab.error.SyntaxCreationException;
import mx.kenzie.skriptlab.template.GeneratedEffect;
import mx.kenzie.skriptlab.template.GeneratedPropertyCondition;
import mx.kenzie.skriptlab.template.GeneratedPropertyExpression;
import mx.kenzie.skriptlab.template.GeneratedSimpleExpression;
import org.junit.Test;

public class GenerationTest {
    
    @Test
    public void sample() {
        new SyntaxGenerator() {
            @Override
            public synchronized Class<GeneratedEffect> generateEffectClass() throws SyntaxCreationException {
                return super.generateEffectClass();
            }
        }.generateEffectClass();
        new SyntaxGenerator() {
            @Override
            protected synchronized Class<GeneratedPropertyCondition<?>> generatePropertyConditionClass() throws SyntaxCreationException {
                return super.generatePropertyConditionClass();
            }
        }.generatePropertyConditionClass();
        new SyntaxGenerator() {
            @Override
            protected synchronized <ExpressionType> Class<GeneratedPropertyExpression<ExpressionType>> generatePropertyExpressionClass() throws SyntaxCreationException {
                return super.generatePropertyExpressionClass();
            }
        }.generatePropertyExpressionClass();
        new SyntaxGenerator() {
            @Override
            protected synchronized <Expr> Class<GeneratedSimpleExpression<Expr>> generateSimpleExpressionClass() throws SyntaxCreationException {
                return super.generateSimpleExpressionClass();
            }
        }.generateSimpleExpressionClass();
    }
    
    //region Stubs
    @Effect
    public static void blob() {
    
    }
    
    
    //end-region
    
}
