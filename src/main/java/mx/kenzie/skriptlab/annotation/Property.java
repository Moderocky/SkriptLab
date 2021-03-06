package mx.kenzie.skriptlab.annotation;

import mx.kenzie.skriptlab.Changer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    
    String value() default "";
    
    Changer[] mode() default {};
    
}
