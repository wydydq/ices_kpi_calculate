package com.nsn.ices.common.annotation;

import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * 为支持   \@ResourceValue 对PropertyPlaceholderConfigurer类进行扩展
 * 在使用的时候类似
 * <per>
 * <bean id="propertyConfigurer"
		class="com.inspur.spring.annotation.ExtendedPropertyPlaceholderConfigurer">
		<property name="locations">
			<list>
				<value>classpath:aaa.properties</value>
			</list>
		</property>
	</bean>
 * </per>
 *
 */
public class ExtendedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

	private Properties properties;

	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {

		super.processProperties(beanFactoryToProcess, props);
		properties = props;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
}
