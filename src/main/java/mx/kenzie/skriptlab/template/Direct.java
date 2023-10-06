package mx.kenzie.skriptlab.template;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;

public interface Direct extends SyntaxElement {
    
    @Override
    default boolean init(Expression<?> @NotNull [] expressions,
                         int matchedPattern,
                         @NotNull Kleenean kleenean,
                         SkriptParser.@NotNull ParseResult result) {
        return true;
    }
    
}
