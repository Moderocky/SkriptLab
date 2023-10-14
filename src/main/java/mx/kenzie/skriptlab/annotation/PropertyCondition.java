package mx.kenzie.skriptlab.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates a Skript property condition from a dynamic method.
 * The method must have a boolean return type.
 * <p>
 * The provided strings will be the syntax pattern.
 * If none are provided a pattern will be attempted from the method's name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PropertyCondition {
    
    String pattern() default "";
    
}
