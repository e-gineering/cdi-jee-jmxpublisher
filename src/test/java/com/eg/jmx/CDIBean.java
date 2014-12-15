package com.eg.jmx;

import com.eg.jmx.MBean;
import net.gescobar.jmx.annotation.Description;
import net.gescobar.jmx.annotation.DescriptorFields;
import net.gescobar.jmx.annotation.Impact;
import net.gescobar.jmx.annotation.ManagedAttribute;
import net.gescobar.jmx.annotation.ManagedOperation;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
@MBean(type = "CDI")
@Description("A simple CDI bean")
public class CDIBean {

	private AtomicLong counter;

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
