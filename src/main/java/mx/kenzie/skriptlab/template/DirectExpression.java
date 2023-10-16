package mx.kenzie.skriptlab.template;

import ch.njol.skript.classes.Changer;
import mx.kenzie.skriptlab.Expressions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;

import java.lang.reflect.Array;

@FunctionalInterface
public interface DirectExpression<Type> extends Direct {
    
    Class<?>[] noChangers = new Class[0];
    
    Type[] get(@NotNull Event event, Expressions inputs);
    
    default boolean isSingle() {
        return true;
    }
    
    default Class<? extends Type> getReturnType() {
        return null;
    }
    
    default Class<?> @Nullable [] acceptChange(Changer.@NotNull ChangeMode mode) {
        return noChangers;
    }
    
    default void change(Object source, Object[] delta, Changer.ChangeMode mode) {
    }
    
    default <Thing> Thing getSingle(Class<Thing> type, Object... delta) {
        if (delta == null || delta.length < 1) return null;
        return Converters.convert(delta[0], type);
    }
    
    @SuppressWarnings("unchecked")
    default <Thing> Thing[] getArray(Class<Thing> type, Object... delta) {
        if (delta == null || delta.length < 1) return (Thing[]) Array.newInstance(type, 0);
        return Converters.convert(delta, type);
    }
    
    interface Single<Type> extends DirectExpression<Type> {
        
        @Override
        @SuppressWarnings("unchecked")
        default Type[] get(@NotNull Event event, Expressions inputs) {
            final Type type = this.getSingle(event, inputs);
            final Type[] array = (Type[]) Array.newInstance(this.getArrayType(), 1);
            array[0] = type;
            return array;
        }
        
        Type getSingle(@NotNull Event event, Expressions inputs);
        
        @SuppressWarnings("unchecked")
        default Class<Type> getArrayType(Type... array) {
            return (Class<Type>) array.getClass().getComponentType();
        }
        
    }
    
}
