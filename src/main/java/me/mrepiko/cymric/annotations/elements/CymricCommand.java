package me.mrepiko.cymric.annotations.elements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark classes as Commands.
 * If a class is annotated with this, it will automatically be registered and available as a command.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CymricCommand {
    String folderPath() default "";
}
