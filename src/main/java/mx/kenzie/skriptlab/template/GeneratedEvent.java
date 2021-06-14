package mx.kenzie.skriptlab.template;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GeneratedEvent extends SkriptEvent implements GeneratedMember {
    protected Literal<?>[] args;
    
    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
        this.args = Arrays.copyOf(args, args.length);
        return true;
    }
    
    @Override
    public boolean check(Event e) {
        return this.getEventClass().isInstance(e);
    }
    
    @Override
    public String toString(Event event, boolean debug) {
        final List<Object> inputs = new ArrayList<>();
        for (Literal<?> arg : args) {
            inputs.add(arg.toString(event, debug));
        }
        return String.format(this.getSyntax().replaceAll("%.+?%", "%s"), inputs.toArray());
    }
    
    protected abstract Class<? extends Event> getEventClass();
    
}
