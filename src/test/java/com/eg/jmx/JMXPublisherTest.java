package com.eg.jmx;

import net.gescobar.jmx.Management;
import net.gescobar.jmx.annotation.Impact;
import net.gescobar.jmx.impl.AbstractInstanceResolver;
import net.gescobar.jmx.util.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.remotingjmx.MBeanServerLocator;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Created by bvarner on 12/15/14.
 */
@RunWith(Arquillian.class)
public class JMXPublisherTest {

	@Deployment
	public static JavaArchive createDeployment() {

		JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "cdi-jee-jmxpublisher.jar")
				                     .addPackage(JMXPublisher.class.getPackage())
				                     .addPackage(Impact.class.getPackage())
				                     .addPackage(AbstractInstanceResolver.class.getPackage())
				                     .addPackage(Management.class.getPackage())
				                     .addClass(StringUtils.class)
				                     .addClass(CDIBean.class)
				                     .addAsResource("META-INF/services/javax.enterprise.inject.spi.Extension")
				                     .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
		System.out.println(archive);

		return archive;

	}

	@Inject
	private CDIBean cdiBean;


	@Inject
	private EJBBean ejbBean;

	@Test
	public void should_register_mbeans() throws Exception {
		Assert.assertNotNull(cdiBean);
		Assert.assertNotNull(ejbBean);
	}

	@Test
	public void cdi_invokable() throws Exception {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = ObjectName.getInstance("com.eg.jmx:name=CDIBean,type=CDI");

		Object counter = mBeanServer.getAttribute(name, "counter");
		Assert.assertEquals(counter, new Long(0));

		mBeanServer.invoke(name, "increment", null, null);

		counter = mBeanServer.getAttribute(name, "counter");
		Assert.assertEquals(counter, new Long(1));

		mBeanServer.setAttribute(name, new Attribute("counter", 5l));

		counter = mBeanServer.getAttribute(name, "counter");
		Assert.assertEquals(counter, new Long(5));
	}

	@Test
	public void ejb_invokable() throws Exception  {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = ObjectName.getInstance("com.eg.jmx:name=EJBBean,type=EJB");

		Object counter = mBeanServer.getAttribute(name, "counter");
		Assert.assertEquals(counter, new Long(0));

		mBeanServer.invoke(name, "increment", null, null);

		counter = mBeanServer.getAttribute(name, "counter");
		Assert.assertEquals(counter, new Long(1));

		mBeanServer.setAttribute(name, new Attribute("counter", 5l));

		counter = mBeanServer.getAttribute(name, "counter");
		Assert.assertEquals(counter, new Long(5));
	}
}
