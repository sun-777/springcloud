package com.sun.server.context;

import com.sun.common.util.StringUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

import static com.sun.common.util.Assertions.isTrueArgument;
import static com.sun.common.util.Assertions.notNull;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

/**
 * @description:
 * @author: Sun Xiaodong
 */
@Scope(value = SCOPE_SINGLETON)
@Component
public class BeanUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (null != applicationContext) {
            BeanUtils.applicationContext = applicationContext;
        }
    }


    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    public static Object getBean(String name) {
        isTrueArgument("name", !StringUtil.isBlank(name));
        return applicationContext.getBean(name);
    }


    public static <T> T getBean(Class<T> clazz) {
        notNull("clazz", clazz);
        return applicationContext.getBean(clazz);
    }
    
    
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    
    public static void autowiredBean(Object bean, ServletContext context){
        // ServletContext context = httpServletRequest.getServletContext();
        // ServletContext context = serveletContextEvent.getServletContext()
        // final ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
        final ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(bean);

    }

}
