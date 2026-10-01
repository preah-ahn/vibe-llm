package io.vibe.llm.common.engine.annotation.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @since       2026.10.01
 * @author      preah
 * @description description
 **********************************************************************************************************************/
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {

	String value() default "";
}