package mx.kenzie.skriptlab.internal;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import mx.kenzie.skriptlab.Expressions;
import mx.kenzie.skriptlab.PatternDigest;
import mx.kenzie.skriptlab.error.AbnormalSyntaxCreationError;
import mx.kenzie.skriptlab.template.DirectEffect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class GeneratedEffect extends Effect {
    
    private Expression<?>[] expressions;
    private DirectEffect handle;
    private PatternDigest digest;

    protected native DirectEffect getHandle();

    protected native String[] getPatterns();
    
    @Override
    protected void execute(@NotNull Event event) {
        final Expressions inputs = Expressions.of(event, expressions);
        this.handle.execute(event, inputs);
    }
    
    @Override
    public @NotNull String toString(Event event, boolean b) {
        final Object[] objects = new Object[expressions.length];
        for (int i = 0; i < expressions.length; i++) objects[i] = expressions[i].toString(event, b);
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
        return true;
    }
    
}
