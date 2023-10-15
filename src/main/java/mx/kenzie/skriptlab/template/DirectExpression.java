package mx.kenzie.skriptlab.template;

import ch.njol.skript.classes.Changer;
import mx.kenzie.skriptlab.Expressions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        private Class<Type> getArrayType(Type... array) {
            return (Class<Type>) array.getClass().getComponentType();
        }
        
    }
    
}
