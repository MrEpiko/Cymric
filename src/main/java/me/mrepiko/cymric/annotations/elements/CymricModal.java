package me.mrepiko.cymric.annotations.elements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark classes as Modals.
 * If a class is annotated with this, it will automatically be registered and available as a modal.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CymricModal {
    String folderPath() default "";
    String moduleId() default "";
}