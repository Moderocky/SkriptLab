package mx.kenzie.skriptlab;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.*;
import mx.kenzie.skriptlab.template.DirectExpression;

public record Syntax
    <Handler extends SyntaxElement>
    (Class<? extends SyntaxElement> type, Handler handler, String... patterns) implements Registered {
    
    @Override
    @SuppressWarnings("unchecked")
    public void register() {
        if (Section.class.isAssignableFrom(type))
            Skript.registerSection((Class<? extends Section>) type, patterns);
        else if (Effect.class.isAssignableFrom(type))
            Skript.registerEffect((Class<? extends Effect>) type, patterns);
        else if (Condition.class.isAssignableFrom(type))
            Skript.registerCondition((Class<? extends Condition>) type, patterns);
        else if (Expression.class.isAssignableFrom(type))
            this.registerExpression(this.holderType(), this.expressionReturnType(), patterns);
    }
    
    private <Holder extends Expression<Type>, Type> void registerExpression(Class<Holder> holder, Class<Type> type, String... patterns) {
        Skript.registerExpression(holder, type, ExpressionType.SIMPLE, patterns);
    }
    
    @SuppressWarnings("unchecked")
    <Type extends SyntaxElement> Class<Type> holderType() {
        return (Class<Type>) type;
    }
    
    @SuppressWarnings("unchecked")
    <Type> Class<Type> expressionReturnType() {
        return handler instanceof DirectExpression<?> expression ? (Class<Type>) expression.getReturnType() : null;
    }
    
}
