package mx.kenzie.skriptlab.annotation;

import ch.njol.skript.classes.Changer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Expression {
    
    String[] value() default {};
    
    Changer.ChangeMode[] allowedModes() default {Changer.ChangeMode.SET};
    
}
