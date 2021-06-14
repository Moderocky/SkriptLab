package mx.kenzie.skriptlab.template;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.SimpleExpression;
import org.bukkit.event.Event;

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
