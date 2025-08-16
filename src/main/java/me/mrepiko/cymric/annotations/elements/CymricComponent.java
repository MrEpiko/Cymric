package me.mrepiko.cymric.annotations.elements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark classes as Components (button, string select menu, entity select menu).
 * If a class is annotated with this, it will automatically be registered and available as a component.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CymricComponent {
    String folderPath() default "";
}
