package mx.kenzie.skriptlab.template;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import mx.kenzie.skriptlab.Expressions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public interface DirectCondition extends SyntaxElement {
    
    boolean check(@NotNull Event event, Expressions inputs);
    
    @Override
    default boolean init(Expression<?> @NotNull [] expressions,
                         int matchedPattern,
                         @NotNull Kleenean kleenean,
                         SkriptParser.@NotNull ParseResult result) {
        return true;
    }
    
}
