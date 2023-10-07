package mx.kenzie.skriptlab.template;

@FunctionalInterface
public interface DirectPropertyCondition<Type> extends Direct {
    
    boolean check(Type thing);
    
}
