package com.eg.jmx;

import net.gescobar.jmx.Management;
import net.gescobar.jmx.annotation.Impact;
import net.gescobar.jmx.impl.AbstractInstanceResolver;
import net.gescobar.jmx.util.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

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

	@Test
	public void should_register_mbean() throws Exception {
		Assert.assertNotNull(cdiBean);



	}
}
