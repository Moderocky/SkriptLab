package mx.kenzie.skriptlab.template;

import ch.njol.skript.conditions.base.PropertyCondition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class GeneratedPropertyCondition<T> extends PropertyCondition<T> {
    
    //region Overridden Stubs
    @Override
    public boolean check(T t) {
        throw new IllegalStateException("Post-generation class must override 'check' method.");
    }
    //endregion
    
    protected boolean check(T t, Object target) {
        try {
            if (target instanceof Method method) {
                return (boolean) method.invoke(t);
            } else if (target instanceof Field field) {
                return (boolean) field.get(t);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return true;
    }
    
}
