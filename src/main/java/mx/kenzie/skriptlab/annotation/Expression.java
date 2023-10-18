package mx.kenzie.skriptlab.annotation;

import mx.kenzie.skriptlab.AccessMode;
import mx.kenzie.skriptlab.PatternCreator;
import mx.kenzie.skriptlab.SyntaxGenerator;

import java.lang.annotation.*;
import java.lang.reflect.Method;

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
public @interface Expression {
    
    String[] value() default {};
    
    AccessMode mode() default AccessMode.GET;
    
    record Converted(AccessMode mode, String... value) implements Expression {
        
        public static Expression converted(PropertyExpression expression, Method method) {
            final String[] values = new String[2];
            final String value = expression.value(), property, fromType;
            if (value.isBlank()) {
                final PatternCreator creator = new PatternCreator(method.getName());
                property = creator.getPattern();
            } else property = value;
            fromType = SyntaxGenerator.getTypeName(method.getDeclaringClass());
            values[0] = "[the] " + property + " of %" + fromType + "%";
            values[1] = "%" + fromType + "%'[s] " + property;
            return new Converted(expression.mode(), values);
        }
        
        @Override
        public Class<? extends Annotation> annotationType() {
            return Expression.class;
        }
        
    }
    
}
