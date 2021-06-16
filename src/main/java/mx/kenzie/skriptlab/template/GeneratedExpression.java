package mx.kenzie.skriptlab.template;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.SimpleExpression;
import org.bukkit.event.Event;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class GeneratedExpression<T> extends SimpleExpression<T> implements Expression<T>, GeneratedMember {
    
    public static Class<?> ensureWrapper(Class<?> cls) {
        if (cls == int.class
            || cls == long.class
            || cls == double.class
            || cls == short.class
            || cls == byte.class
            || cls == float.class
        ) return Number.class;
        if (cls == boolean.class
        ) return Boolean.class;
        return cls;
    }
    
    //region Wrapper Stubs
    protected T[] wrapArray(boolean q) { return wrapArray((Object) q); }
    protected T[] wrapArray(byte q) { return wrapArray((Object) q); }
    protected T[] wrapArray(double q) { return wrapArray((Object) q); }
    protected T[] wrapArray(short q) { return wrapArray((Object) q); }
    protected T[] wrapArray(long q) { return wrapArray((Object) q); }
    protected T[] wrapArray(int q) { return wrapArray((Object) q); }
    protected T[] wrapArray(float q) { return wrapArray((Object) q); }
    //end-region
    
    protected T[] wrapArray(Object object) {
        if (object == null) return null;
        else if (object.getClass().isArray()) {
            if (object.getClass().getComponentType().isPrimitive()) {
                T[] array = (T[]) Array.newInstance(ensureWrapper(getReturnType()), Array.getLength(object));
                System.arraycopy(object, 0, array, 0, array.length);
                return array;
            } else return (T[]) object;
        } else {
            final T[] array = (T[]) Array.newInstance(ensureWrapper(getReturnType()), 1);
            array[0] = (T) object;
            return array;
        }
    }
    
    public Class<?>[] getChangeType(Field field) {
        return new Class[]{ensureWrapper(field.getType())};
    }
    
    public Class<?>[] getChangeType(Changer.ChangeMode mode, Method method) {
        return switch (mode) {
            case SET, ADD, REMOVE, REMOVE_ALL -> method.getParameterTypes().length > 0
                ? new Class[]{ensureWrapper(method.getParameterTypes()[0])}
                : null;
            default -> null;
        };
    }
    
    @Override
    public String toString(Event e, boolean debug) {
        return this.getSyntax(); // TODO: Create a toString implementation
    }
}
