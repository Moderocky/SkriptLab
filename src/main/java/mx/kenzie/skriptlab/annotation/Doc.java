package mx.kenzie.skriptlab.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface Doc {
    
    /**
     * Regex patterns to match this class, e.g. in the expressions loop-[type], random [type] out of ...,
     * or as command arguments.
     * <p>
     * These patterns must be english and match singular and plural.
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.FIELD, ElementType.METHOD})
    @interface User {
        String[] value();
    }
    
    /**
     * The name of the syntax element.
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.FIELD, ElementType.METHOD})
    @interface Name {
        String value();
    }
    
    /**
     * The description of the syntax element.
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.FIELD, ElementType.METHOD})
    @interface Description {
        String[] value();
    }
    
    /**
     * Examples of the element, used for documentation auto-generation.
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.FIELD, ElementType.METHOD})
    @interface Examples {
        String[] value();
    }
    
    /**
     * The original version this element was added.
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.PACKAGE, ElementType.FIELD, ElementType.METHOD})
    @interface Since {
        String value();
    }
    
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.TYPE, ElementType.TYPE_PARAMETER, ElementType.TYPE_USE})
    @interface Syntax {
    }
    
}
