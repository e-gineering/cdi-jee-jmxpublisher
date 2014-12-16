# cdi-jee-jmxpublisher

A CDI Extension that enables simplified publication of CDI and EJB attributes and methods to JMX MBeanServers.

Based upon an existing jmx-annotations project inspired by the now dormant
[JSR 255 Java Management Extension Specification, v2.0](https://jcp.org/en/jsr/detail?id=255), this CDI extension makes
it trivial to expose `@ApplicationScoped` CDI beans, or `@Singleton` `@EJB` beans in a CDI container.

## Usage

### Building & Installing


This project is based on a fork of jmx-annotations -- with a pending upstream pull request.
You will need to clone https://github.com/bvarner/jmx-annotations and `mvn clean install` that project prior to building
and installing cdi-jee-jmxpublisher.


Use maven to build and install the project locally (or to a local nexus repo)

```
mvn clean install
```

This will execute the built-in arquillian tests in a Wildfly 8.1 container (by default). If you'd like to skip this (lengthy) process,

```
mvn -DskipTests clean install
```


Once you have this project built and installed to a maven repository you can add it to your own project.

```
<dependency>
    <groupId>com.eg</groupId>
    <artifactId>cdi-jee-jmxpublisher</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```


### Example Class
```
@ApplicationScoped
@MBean("foo.bar:name=", type = "YourDiscriminator")
@Description("This description becomes part of the JMX metadata")
public class Foo {

    private Long bar = 0;

    @ManagedAttribute(description = "A description about the 'bar' attribute.")
    public long getBar() {
        return bar;
    }

    public void setBar(long bar) {
        this.bar = bar;
    }

    @ManagedOperation(description = "This describes doing something.", impact = Impact.ACTION_INFO)
    @DescriptorFields({"p0=withThis;Description of the withThis field...",
                       "p1=andThis;Description of the andThis field..."})
    public long doSomethingFun(long withThis, String andThis) {
        ...
    }
}
```

When the class above is added to the CDI context, an MBean will be created with the ObjectName of 'foo.bar:name=Foo,type=YourDiscriminator' and published to the JMX platform mbean server. This MBean will expose the Foo attribute as readable and writable (you can control this with the annotations) and the annotated method.


### Annotation Guide

Add `@MBean` to the class whose instance you'd like to publish in JMX.

Getter / Setters can be annotated with `@ManagedAttribute`. You can override the defaults for `readable`, `writeable`, and provide a `description` which will be added to the MBean metadata.

Methods can be annotated with `@ManagedOperation`, and should include an `Impact` and `description` for the MBean metadata.
To provide documentation (method signature names, descriptions) for method parameters, you'll need to add an `@DescriptorFields` annotation.
`@DescriptorFields` provide a mechanism for mapping default MBean parameter names (p0 - pN, where N is the number of arguments - 1) to more readable parameter names, along with an optional description.
Each element in the String array value for `@DescriptiorFields` is interpreted as a parameter mapping. The general format is:

```
pN=theParameterName;A description of the Parameter
```

So, for example...

```
@ManagedOperation(impact = Impact.ACTION_INFO, description = "Sends all emails for the given username above or equal to the given severity")
@DescriptorFields({"p0=username;The name of the user",
                   "p1=severity;The minimum severity to send."})
public Integer sendEmailsFor(String username, Integer severity) {
    ...
}
```

Without the @DescriptorFields annotation values, JConsole will interpret this as "Integer sendEmailsFor(p0, p1)".
With the proper @DescriptorFields added as above, JConsole will interpret this as "Integer sendEmailsFor(username, severity)".

#### CDI Beans
CDI Beans are the most simple case. The publisher will only work on CDI beans which are `@ApplicationScoped`.

See the [CDIBean.java](/src/test/java/com/eg/jmx/CDIBean.java) example.

#### EJBs
EJBs are a bit more complex, due to the manner in which EJBs can be pooled internally. You'll need to keep this in mind
when exposing methods over EJBs. While you can `@MBean` publish EJBs which are *not* `@Singleton` EJBs, you'll need to be
a bit more careful with what you expose and how you expose it.

To handle publishing EJBs, a type of dynamic proxy is registered with the MBeanServer which exposes the annotated attributes and methods of the `@EJB` class. When the MBeanServer invokes the dynamic proxy, it locates an instance of the EJB, then invokes the attribute or method on the located target.
See the [EJBBean.java](/src/test/java/com/eg/jmx/CDIBean.java) example.

This has a few consequences.

* The `@EJB` must be looked up via JNDI for each attribute or method access, and as such, must have a stable `beanInterface` and `name` defined on the @EJB annotation. The `beanInterface` can be the implementing class.
* Only `@Singleton` EJBs are fully predictable in operation. All attribute and method annotations will be exactly what you expect.
* MBean server invocations (like all JEE components) can have more than one thread invoking them at a time. Consider concurrent access on your EJBs.
* Because The JMXPublisher invokes through the JNDI resolved EJB, the EJB server should (JBoss EAP 6.x and Wildfly do) respect and apply all standard container annotations including Security, Transaction, and ConcurrencyManagement annotations.
* In the situation of remote JMX connections, if the connection is authenticated, the SecurityContext (principal, roles, etc) is




