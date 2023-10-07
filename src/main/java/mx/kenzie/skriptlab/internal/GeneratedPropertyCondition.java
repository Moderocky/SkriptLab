package mx.kenzie.skriptlab.internal;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import mx.kenzie.skriptlab.PatternDigest;
import mx.kenzie.skriptlab.template.DirectPropertyCondition;
import org.jetbrains.annotations.NotNull;

public class GeneratedPropertyCondition<Type> extends PropertyCondition<Type> {
    
    private DirectPropertyCondition<Type> handle;
    private String propertyName;
    
    protected native DirectPropertyCondition<Type> getHandle();
    
    protected native String getPattern();
    
    @Override
    public boolean check(Type type) {
        return this.handle.check(type);
    }
    
    @Override
    protected @NotNull String getPropertyName() {
        return propertyName;
    }
    
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean isDelayed, SkriptParser.@NotNull ParseResult result) {
        final boolean ok = super.init(expressions, matchedPattern, isDelayed, result);
        if (!ok) return false;
        PatternDigest digest = new PatternDigest(this.getPattern());
        this.handle = this.getHandle();
        if (expressions.length > 1) {
            final Object[] objects = new Object[expressions.length - 1];
            for (int i = 0; i < objects.length; i++) objects[i] = expressions[i + 1].toString(null, false);
            this.propertyName = digest.example(objects);
        } else propertyName = digest.example();
        return handle.init(expressions, matchedPattern, isDelayed, result);
    }
    
}
