package com.eg.jmx;


import net.gescobar.jmx.annotation.Impact;
import net.gescobar.jmx.annotation.ManagedAttribute;
import net.gescobar.jmx.annotation.ManagedOperation;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@MBean(type = "EJB")
@EJB(beanInterface = EJBBean.class, name = "java:global/EJBBean")
public class EJBBean {

	private AtomicLong counter = new AtomicLong(0);

	@ManagedAttribute(writable = true, readable = true, description = "The internal counter.")
	public long getCounter() {
		return counter.get();
	}

	public void setCounter(long value) {
		counter.set(value);
	}



	@ManagedOperation(description = "Increments and returns the new counter value.", impact = Impact.ACTION_INFO)
	public long increment() {
		return counter.incrementAndGet();
	}

	@ManagedOperation(description = "Decrements and returns the new counter value.", impact = Impact.ACTION_INFO)
	public long decrement() {
		return counter.decrementAndGet();
	}
}
