package mx.kenzie.skriptlab.template;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import mx.kenzie.skriptlab.Expressions;
import mx.kenzie.skriptlab.error.AbnormalSyntaxCreationError;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface DirectEffect extends SyntaxElement {
    
    void execute(@NotNull Event event, Expressions inputs);
    
    @Override
    default boolean init(Expression<?> @NotNull [] expressions,
                         int matchedPattern,
                         @NotNull Kleenean kleenean,
                         SkriptParser.@NotNull ParseResult result) {
        throw new AbnormalSyntaxCreationError("Effect created without an initialiser.");
    }
    
}
