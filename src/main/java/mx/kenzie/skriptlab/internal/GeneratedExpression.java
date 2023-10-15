package mx.kenzie.skriptlab.internal;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import mx.kenzie.skriptlab.Expressions;
import mx.kenzie.skriptlab.PatternDigest;
import mx.kenzie.skriptlab.error.AbnormalSyntaxCreationError;
import mx.kenzie.skriptlab.template.DirectExpression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GeneratedExpression<Type> extends SimpleExpression<Type> {
    
    private Expression<?>[] expressions;
    private DirectExpression<Type> handle;
    private PatternDigest digest;
    
    protected native DirectExpression<Type> getHandle();
    
    protected native String[] getPatterns();
    
    @Override
    public @NotNull String toString(Event event, boolean debug) {
        final Object[] objects = new Object[expressions.length];
        for (int i = 0; i < expressions.length; i++) objects[i] = expressions[i].toString(event, debug);
        return digest.example(objects);
    }
    
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull Kleenean kleenean, SkriptParser.@NotNull ParseResult parseResult) {
        this.expressions = expressions;
        this.handle = this.getHandle();
        if (handle == null) throw new AbnormalSyntaxCreationError("No direct expression getter was provided.");
        final String[] patterns = this.getPatterns();
        if (patterns == null) throw new AbnormalSyntaxCreationError("No syntax patterns were provided.");
        if (patterns.length <= matchedPattern)
            throw new AbnormalSyntaxCreationError("The pattern matched was not one of the provided patterns.");
        this.digest = new PatternDigest(patterns[matchedPattern]);
        return handle.init(expressions, matchedPattern, kleenean, parseResult);
    }
    
    @Override
    protected Type @NotNull [] get(@NotNull Event event) {
        final Expressions inputs = Expressions.of(event, expressions);
        return handle.get(event, inputs);
    }
    
    @Override
    public boolean isSingle() {
        return handle.isSingle();
    }
    
    @Override
    public @NotNull Class<? extends Type> getReturnType() {
        return handle.getReturnType();
    }
    
    @Override
    public Class<?> @Nullable [] acceptChange(Changer.@NotNull ChangeMode mode) {
        final Class<?>[] changers = handle.acceptChange(mode);
        if (changers == DirectExpression.noChangers) return super.acceptChange(mode);
        return changers;
    }
    
    @Override
    public void change(@NotNull Event event, Object @Nullable [] delta, Changer.@NotNull ChangeMode mode) {
        final Class<?>[] changers = handle.acceptChange(mode);
        if (changers == DirectExpression.noChangers) super.change(event, delta, mode);
        else handle.change(event, delta, mode);
    }
    
}
