package mx.kenzie.skriptlab.template;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class GeneratedPropertyExpression<T> extends GeneratedExpression<T> {
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
    
    protected T[] get(Event event, Field field) {
        if (expressions.size() < 1) return null;
        try {
            return wrapArray(field.get(expressions.iterator().next().getSingle(event)));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void change(final Event event, final Object[] objects, final Changer.ChangeMode mode, final Field field) throws UnsupportedOperationException {
        if (expressions.size() < 1) return;
        Object object = expressions.iterator().next().getSingle(event);
        try {
            switch (mode) {
                case SET:
                    if (isSingle()) {
                        field.set(object, objects[0]);
                    } else {
                        if (objects == null || objects.length < 1) field.set(object, new ArrayList<>());
                        else field.set(object, Arrays.asList(objects));
                    }
                    break;
                case ADD:
                    if (isSingle() || objects == null || objects.length < 1) break;
                    List list = new ArrayList<>();
                    if (field.get(object) != null) {
                        list.addAll((Collection) field.get(object));
                    }
                    list.addAll(Arrays.asList(objects));
                    field.set(object, list);
                    break;
                case DELETE:
                    field.set(object, null);
                    break;
                case RESET:
                    if (isSingle()) field.set(object, null);
                    else field.set(object, new ArrayList<>());
                    break;
                case REMOVE:
                case REMOVE_ALL:
                    if (isSingle() || objects == null || objects.length < 1) break;
                    List arrayList = new ArrayList<>();
                    if (field.get(object) != null) {
                        arrayList.addAll((Collection) field.get(object));
                    }
                    arrayList.removeAll(Arrays.asList(objects));
                    field.set(object, arrayList);
                    break;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    
    public boolean isSingle(Field field) {
        return !(field.getType().isAssignableFrom(Collection.class));
    }
    
    public Class<? extends T> getReturnType(Field field) {
        return (Class<? extends T>) ensureWrapper(field.getType());
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
