package mx.kenzie.skriptlab.template;

import mx.kenzie.skriptlab.Expressions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface DirectCondition extends Direct {
    
    boolean check(@NotNull Event event, Expressions inputs);
    
}
