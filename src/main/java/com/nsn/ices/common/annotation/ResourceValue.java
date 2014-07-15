package com.nsn.ices.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 提供在annotation 中使用xml方式配置bean 时 对properties ${username} 这样的支持
 * 如：
 * 			@ResourceValue(value="jdbc.url")
			String jdbcUrl;
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ResourceValue {
	String value() default "";
}
