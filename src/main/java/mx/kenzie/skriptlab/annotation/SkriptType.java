package mx.kenzie.skriptlab.annotation;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SkriptType {
    
    String value() default "";
    
}
