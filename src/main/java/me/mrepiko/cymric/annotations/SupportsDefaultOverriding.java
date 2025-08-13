package me.mrepiko.cymric.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark classes that support overriding default fields in the DefaultObject config.
 * If a class is annotated with this, it allows for its default fields to be overridden in the DefaultObject configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SupportsDefaultOverriding { }
