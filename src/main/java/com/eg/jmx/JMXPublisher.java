package com.eg.jmx;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import net.gescobar.jmx.annotation.Description;
import net.gescobar.jmx.impl.MBeanFactory;

/**
 * A CDI Extension which processes CDI & EJB beans, registering Dynamic MBean Proxies for 
 * beans annotated with @MBean and either @EJB or @ApplicationScoped.
 *
 * @author bvarner
 */
public class JMXPublisher implements Extension {
	private List<Bean<?>> ejbList = new ArrayList<>();
	private List<Bean<?>> cdiList = new ArrayList<>();

	private static final Logger LOG = Logger.getLogger(JMXPublisher.class.getName());

	static final String BASE_NAME = "com.eg.jmx:name=";

	/**
	 * Observes ProcessBean events from the CDI container, inspecting the beans for @MBean markers and keeping track of
	 * the classes internally, for future consideration.
	 *
	 * @param <T>
	 * @param event
	 */
	public <T> void collect(@Observes ProcessBean<T> event) {
		if (event.getAnnotated().isAnnotationPresent(MBean.class)) {
			if (event.getAnnotated().isAnnotationPresent(EJB.class)) {
				LOG.log(Level.INFO, "Adding @MBean EJB: {0}", event.getBean().getBeanClass().getSimpleName());
				ejbList.add(event.getBean());
			} else if (event.getAnnotated().isAnnotationPresent(ApplicationScoped.class)) {
				LOG.log(Level.INFO, "Adding @MBean @ApplicationScoped CDI Bean: {0}", event.getBean().getBeanClass().getSimpleName());
				cdiList.add(event.getBean());
			}
		}
	}


	/**
	 * Observes the CDI Container AfterDeploymentValidation event, creating MBean proxies for all beans discovered
	 * during the ProcessBean phase.
	 *
	 * @param event
	 * @param beanManager
	 */
	public void load(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
		for (Bean<?> bean : ejbList) {
			EJBMBeanProxy proxy = new EJBMBeanProxy(bean.getBeanClass());
			try {
				ManagementFactory.getPlatformMBeanServer().registerMBean(MBeanFactory.createMBean(proxy.getBeanInterface(), proxy),
						                                                        getObjectName(bean.getBeanClass()));
			} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex) {
				LOG.log(Level.SEVERE, "Failed to register @MBean proxy for EJB: " + proxy.getBeanInterface().getSimpleName(), ex);
				throw new IllegalStateException("Unable to register DynamicMBean proxy", ex);
			}
		}

		for (Bean<?> bean : cdiList) {
			try {
				ManagementFactory.getPlatformMBeanServer().registerMBean(
						                                                        MBeanFactory.createMBean(beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean))),
						                                                        getObjectName(bean.getBeanClass()));
			} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException ex) {
				LOG.log(Level.SEVERE, "Failed to register @MBean proxy for @ApplicationScoped CDI Bean: " + bean.getBeanClass().getSimpleName(), ex);
				throw new IllegalStateException("Unable to register DynamicMBean proxy", ex);
			}
		}
	}

	/**
	 * Removes registered mbeans from the MBean server.
	 *
	 * @param event
	 * @param beanManager
	 */
	public void unload(@Observes BeforeShutdown event, BeanManager beanManager) {
		for (Bean<?> bean : ejbList) {
			try {
				ManagementFactory.getPlatformMBeanServer().unregisterMBean(getObjectName(bean.getBeanClass()));
			} catch (MalformedObjectNameException | InstanceNotFoundException | MBeanRegistrationException ex) {
				LOG.log(Level.WARNING, "Failed to unregister DynamicMBeanProxy for " + bean.getBeanClass().getSimpleName(), ex);
			}
		}

		for (Bean<?> bean : cdiList) {
			try {
				ManagementFactory.getPlatformMBeanServer().unregisterMBean(getObjectName(bean.getBeanClass()));
			} catch (MalformedObjectNameException | InstanceNotFoundException | MBeanRegistrationException ex) {
				LOG.log(Level.WARNING, "Failed to unregister DynamicMBeanProxy for " + bean.getBeanClass().getSimpleName(), ex);
			}
		}

		ejbList.clear();
		cdiList.clear();
	}

	/**
	 * Gets the ObjectName generated for a type of <code>Class</class>.
	 *
	 * @param clazz
	 * @return
	 * @throws MalformedObjectNameException
	 */
	static ObjectName getObjectName(final Class<?> clazz) throws MalformedObjectNameException {
		String baseName = BASE_NAME;
		String type = "Internals";

		if (clazz.isAnnotationPresent(MBean.class)) {
			MBean pbean = clazz.getAnnotation(MBean.class);
			baseName = pbean.value();
			type = pbean.type();
		}

		String name = baseName + clazz.getSimpleName();
		if (!"".equals(type)) {
			name += ",type=" + type;
		}

		return new ObjectName(name);
	}
}
