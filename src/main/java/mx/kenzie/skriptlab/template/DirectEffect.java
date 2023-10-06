package mx.kenzie.skriptlab.template;

import mx.kenzie.skriptlab.Expressions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface DirectEffect extends Direct {
    
    void execute(@NotNull Event event, Expressions inputs);
    
}
