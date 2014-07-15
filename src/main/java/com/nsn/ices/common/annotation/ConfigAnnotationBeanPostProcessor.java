package com.nsn.ices.common.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.springframework.beans.BeansException;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 *  为ResourceValue 提供支持
 *
 */

@Component
//定义一个匿名Spring组件
public class ConfigAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {
	@Autowired
	//自动注入  ExtendedPropertyPlaceholderConfigurer对象，用于获取配置资源
	private ExtendedPropertyPlaceholderConfigurer propertyConfigurer;

	//创建简单类型转换器
	private SimpleTypeConverter typeConverter = new SimpleTypeConverter();

	@Override
	public boolean postProcessAfterInstantiation(final Object bean, String beanName) throws BeansException {
		ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				ResourceValue cfg = field.getAnnotation(ResourceValue.class);
				if (cfg != null) {
					if (Modifier.isStatic(field.getModifiers()))
						throw new IllegalStateException("@ResourceValue annotation is not supported  on static fields");

					//如果开发者没有设置@ResourceValue的 value，则使用变量域的名称作为键查找配置资源
					String key = cfg.value().length() <= 0 ? field.getName() : cfg.value();
					Object value = propertyConfigurer.getProperty(key);

					if (value != null) {
						//转换配置值成其它非String类型
						Object _value = typeConverter.convertIfNecessary(value, field.getType());
						//使变量域可用，并且转换后的配置值注入其中
						ReflectionUtils.makeAccessible(field);
						field.set(bean, _value);
					}
				}
			}
		});

		//通常情况下返回true即可
		return true;
	}
}