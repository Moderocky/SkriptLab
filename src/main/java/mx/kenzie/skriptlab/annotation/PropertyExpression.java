package mx.kenzie.skriptlab.annotation;

import mx.kenzie.skriptlab.AccessMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates a Skript expression from a method.
 * <p>
 * The provided strings will be the syntax pattern.
 * If none are provided a pattern will be attempted from the method's name.
 * <p>
 * Each `%input%` in the pattern must correspond to a method parameter.
 * If the method is dynamic, the first input must be the object to call the method on.
 * Array parameters will accept a plural input.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PropertyExpression {
    
    String value() default "";
    
    AccessMode mode() default AccessMode.GET;
    
}
