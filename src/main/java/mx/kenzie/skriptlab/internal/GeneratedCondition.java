package mx.kenzie.skriptlab.internal;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import mx.kenzie.skriptlab.Expressions;
import mx.kenzie.skriptlab.PatternDigest;
import mx.kenzie.skriptlab.error.AbnormalSyntaxCreationError;
import mx.kenzie.skriptlab.template.DirectCondition;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class GeneratedCondition extends Condition {
    
    private Expression<?>[] expressions;
    private DirectCondition handle;
    private PatternDigest digest;
    
    protected native DirectCondition getHandle();
    
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
        if (handle == null) throw new AbnormalSyntaxCreationError("No direct effect executor was provided.");
        final String[] patterns = this.getPatterns();
        if (patterns == null) throw new AbnormalSyntaxCreationError("No syntax patterns were provided.");
        if (patterns.length <= matchedPattern)
            throw new AbnormalSyntaxCreationError("The pattern matched was not one of the provided patterns.");
        this.digest = new PatternDigest(patterns[matchedPattern]);
        return handle.init(expressions, matchedPattern, kleenean, parseResult);
    }
    
    @Override
    public boolean check(@NotNull Event event) {
        final Expressions inputs = Expressions.of(event, expressions);
        return this.handle.check(event, inputs);
    }
    
}
