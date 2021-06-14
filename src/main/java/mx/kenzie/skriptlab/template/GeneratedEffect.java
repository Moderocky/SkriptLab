package mx.kenzie.skriptlab.template;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class GeneratedEffect extends Effect implements GeneratedMember {
    private final Set<Expression<?>> expressions = new HashSet<>();
    
    //region Overridden Stubs
    @Override
    protected void execute(Event event) {
        throw new IllegalStateException("Post-generation class must override 'execute' method.");
    }
    //endregion
    
    protected void execute(Event event, Method method, Object caller) {
        List<Object> list = new ArrayList<>();
        for (Expression<?> expression : expressions) {
            if (expression != null) list.add(expression.getSingle(event));
        }
        Object target = Modifier.isStatic(method.getModifiers())
            ? null
            : list.remove(0);
        try {
            if (list.size() == 0) method.invoke(target);
            else method.invoke(target, list.toArray(new Object[0]));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
    
    @Override
    public String toString(Event event, boolean b) {
        final List<Object> inputs = new ArrayList<>();
        for (final Expression<?> expression : expressions) {
            inputs.add(expression.getSingle(event));
        }
        return String.format(this.getSyntax().replaceAll("%.+?%", "%s"), inputs.toArray());
    }
    
    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        this.expressions.addAll(Arrays.asList(expressions));
        return true;
    }
}
