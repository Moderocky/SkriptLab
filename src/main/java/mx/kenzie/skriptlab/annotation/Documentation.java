package mx.kenzie.skriptlab.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface Documentation {
    
    /**
     * Regex patterns to match this class, e.g. in the expressions loop-[type], random [type] out of ...,
     * or as command arguments.
     * <p>
     * These patterns must be english and match singular and plural.
     */
    String[] user() default {};
    
    /**
     * The name of the syntax element.
     */
    String name();
    
    /**
     * The description of the syntax element.
     */
    String[] description() default {};
    
    /**
     * Examples of the element, used for documentation auto-generation.
     */
    String[] examples() default {};
    
    /**
     * The original version this element was added.
     */
    String since() default "unknown";
    
}
