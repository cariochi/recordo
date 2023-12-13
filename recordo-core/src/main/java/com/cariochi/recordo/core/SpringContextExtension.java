package com.cariochi.recordo.core;

import com.cariochi.recordo.core.utils.Beans;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;

import static org.springframework.test.context.junit.jupiter.SpringExtension.getApplicationContext;

public interface SpringContextExtension extends SpringExtension {

    default boolean isBeanAbsent(Class<?> type, ExtensionContext context) {
        return Beans.of(context).findAll(type).isEmpty();
    }

    default <T> void registerBean(String beanName, T beanInstance, ExtensionContext context) {
        final BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition((Class<T>) beanInstance.getClass(), () -> beanInstance).getBeanDefinition();
        final GenericApplicationContext applicationContext = (GenericApplicationContext) getApplicationContext(context);
        applicationContext.registerBeanDefinition(beanName, beanDefinition);
    }

}
