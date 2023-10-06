package mx.kenzie.skriptlab;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SyntaxElement;

public record Syntax(Class<? extends SyntaxElement> type, String... patterns) {
    
    @SuppressWarnings("unchecked")
    public void register() {
        if (Section.class.isAssignableFrom(type))
            Skript.registerSection((Class<? extends Section>) type, patterns);
        else if (Effect.class.isAssignableFrom(type))
            Skript.registerEffect((Class<? extends Effect>) type, patterns);
        else if (Condition.class.isAssignableFrom(type))
            Skript.registerCondition((Class<? extends Condition>) type, patterns);
    }
    
}
