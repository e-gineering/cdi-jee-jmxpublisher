package com.eg.jmx;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import net.gescobar.jmx.impl.AbstractInstanceResolver;

/**
 * Extends the AbstractInstanceResolver from jmx-annotations for resolving EJBs.
 *
 * In order to resolve the object for invoking an MBean method, this class does a JNDI lookup to retrieve an instance
 * of the proper EJB type.
 */
public class EJBMBeanProxy extends AbstractInstanceResolver {

	private String jndiName;

	private Class<?> ejbClass;

	protected EJBMBeanProxy(Class<?> clazz) {
		if (!clazz.isAnnotationPresent(EJB.class)) {
			throw new IllegalArgumentException("Using EJBMBeanProxy requires an @EJB annotated EJB with a 'beanInterface' class and JNDI 'name' attributes.");
		}
		EJB annot = clazz.getAnnotation(EJB.class);

		if ("".equals(annot.name())) {
			throw new IllegalArgumentException("EJB Annotation for EJBMBeanProxyied class '" + clazz.getSimpleName() + "' has no JNDI 'name' value.");
		}
		if (java.lang.Object.class.equals(annot.beanInterface())) {
			throw new IllegalArgumentException("EJB Annotation for EJBMBeanProxyied class '" + clazz.getSimpleName() + "' has no 'beanInterface' class.");
		}
		ejbClass = annot.beanInterface();
		jndiName = annot.name();
	}

	Class<?> getBeanInterface() {
		return ejbClass;
	}

	/**
	 * Returns an instance to the EJB obtained from JNDI
	 *
	 * @return an EJB instance.
	 */
	@Override
	public Object resolve() {
		try {
			InitialContext ctx = new InitialContext();
			return ctx.lookup(jndiName);
		} catch (NamingException ne) {
			throw new RuntimeException("Unable to resolve JNDI for: " + jndiName + ".", ne);
		}
	}
}
