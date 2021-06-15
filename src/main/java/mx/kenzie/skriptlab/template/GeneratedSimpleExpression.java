package mx.kenzie.skriptlab.template;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GeneratedSimpleExpression<T> extends GeneratedExpression<T> {
    protected final List<Expression<?>> expressions = new ArrayList<>();
    
    @Override
    public boolean init(Expression<?> [] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        expressions.addAll(Arrays.asList(exprs));
        return true;
    }
    
    //region Overridden Stubs
    @Override
    protected T[] get(Event e) {
        throw new IllegalStateException("Post-generation class must override 'get' method.");
    }
    
    @Override
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
        throw new IllegalStateException("Post-generation class must override 'change' method.");
    }
    
    @Override
    public boolean isSingle() {
        throw new IllegalStateException("Post-generation class must override 'isSingle' method.");
    }
    
    @Override
    public Class<? extends T> getReturnType() {
        throw new IllegalStateException("Post-generation class must override 'getReturnType' method.");
    }
    //endregion
    
    @SuppressWarnings("unchecked")
    protected T[] get(Event event, Method method) {
        try {
            final List<Object> objects = new ArrayList<>();
            for (Expression<?> expression : expressions) {
                objects.add(expression.getSingle(event));
            }
            final Object target;
            if (Modifier.isStatic(method.getModifiers()))
                target = null;
            else target = objects.remove(0);
            return wrapArray(method.invoke(target, objects.toArray()));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }
    
    public void change(final Event event, final Object[] objects, final Changer.ChangeMode mode, final Method method) throws UnsupportedOperationException {
        if (expressions.size() < 1) return;
        final Object target;
        if (Modifier.isStatic(method.getModifiers())) target = null;
        else target = expressions.iterator().next().getSingle(event);
        try {
            switch (mode) {
                case SET:
                    if (isSingle()) {
                        method.invoke(target, objects[0]);
                    } else {
                        method.invoke(target, objects);
                    }
                    break;
                case ADD:
                case DELETE:
                case RESET:
                case REMOVE:
                case REMOVE_ALL:
                    break;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    public boolean isSingle(Method method) {
        return method.getParameterTypes().length < 2;
    }
    
    public Class<? extends T> getReturnType(Method method) {
        return (Class<? extends T>) ensureWrapper(method.getReturnType());
    }
    
    @Override
    public String toString(Event event, boolean debug) {
        final List<Object> inputs = new ArrayList<>();
        for (final Expression<?> expression : expressions) {
            inputs.add(expression.getSingle(event));
        }
        return String.format(this.getSyntax().replaceAll("%.+?%", "%s"), inputs.toArray());
    }
    
    @Override
    public List<Object> getConvertedExpressions(Event event) {
        final List<Object> objects = new ArrayList<>();
        for (Expression<?> expression : expressions) {
            objects.add(expression.isSingle() ? expression.getSingle(event) : expression.getArray(event));
        }
        return objects;
    }
}
