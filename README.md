The UncertWeb Processing Service is a generic framework for exposing processes on the web. Processes are exposed using two interfaces: SOAP/WSDL and JSON.


## Getting started

If you like to do things the easy way, clone the example project at https://github.com/itszootime/ps-example. This is a pre-configured Maven project complete with sample processes, all ready to package and deploy.

Alternatively, creating your own Maven webapp project is the next easiest way to use the framework. The UncertWeb Maven repository, hosted at the University of Münster, is required to resolve the necessary dependencies. Adding the following snippet to your `pom.xml` file will include the repository in your project.

```xml
<repositories>
  <!-- Other repositories may be here too -->
  <repository>
    <id>UncertWebMavenRepository</id>
    <name>UncertWeb Maven Repository</name>
    <url>http://giv-uw.uni-muenster.de/m2/repo</url>
  </repository>
</repositories>
```

The framework dependency can then be added too.

```xml
<dependencies>
  <!-- Other dependencies may be here too -->
  <dependency>
      <groupId>org.uncertweb</groupId>
      <artifactId>ps-framework</artifactId>
      <version>0.2.3-SNAPSHOT</version>
  </dependency>
</dependencies>
```

The last thing you'll need to do is add the framework servlet classes and mappings to the webapp configuration file, `src/main/webapp/WEB-INF/web.xml`.

```xml
<web-app>
  <!-- Other servlets/mappings/etc may be here too -->
  <servlet>
    <servlet-name>Service</servlet-name>
    <servlet-class>org.uncertweb.ps.ServiceServlet</servlet-class>
  </servlet>
  <servlet>
    <servlet-name>Data</servlet>
    <servlet-class>org.uncertweb.ps.DataServlet
  </servlet>
  <servlet-mapping>
    <servlet-name>Service</servlet-name>
    <url-pattern>/service/*</url-pattern>
  </servlet-mapping>
  <servlet-mapping>
    <servlet-name>Data</servlet-name>
    <url-pattern>/data</url-pattern>
  </servlet-mapping>
</web-app>
```

## Creating a process

A process is created by extending the `AbstractProcess` class. This class defines methods for returning input and output descriptions, metadata, and performing the actual process work.

Once your class has extended `AbstractProcess`, implement the `getInputIdentifiers` and `getOutputIdentifiers` methods. These methods should return a list of unique identifiers for the inputs and outputs of your process. The chosen identifiers will be used throughout the process class.

```java
public List<String> getInputIdentifiers() {
  return Arrays.asList(new String[] { "FirstInput", "SecondInput" });
}
```

The types of these inputs and outputs are defined in the `getInputDataDescription` and `getOutputDataDescription` methods. These methods should each return a `DataDescription` object for a given input or output identifier.

At a minimum, the `DataDescription` object is constructed with one parameter: the class the input or output will be an instance of. This is used by the framework to determine which encoding class to use when parsing and generating data. The `DataDescription` object can also have a minimum and maximum number of occurrences, and a raw flag to tell the framework to bypass encoding classes when handling referenced data. This is most useful when processing large data (such as raster coverages), when you may not want to read the whole file into memory.

```java
public DataDescription getInputDataDescription(String identifier) {
  if (identifier.equals("FirstInput")) {
    // A is a double, minimum and maximum occurrences is 1 (default)
    return new DataDescription(Double.class);
  } else if (identifier.equals("SecondInput")) {
    // B is a double, minimum occurences is 1, but maximum is unbounded
    return new DataDescription(Double.class, 1, Integer.MAX_VALUE);
  }
}
```

Processing work is performed in the run method. When a process request is received, this method is called and passed a `ProcessInputs` instance containing the parsed inputs. Individual inputs can be retrieved using their identifier, after which they can be cast to the relevant class.

```java
// Get first input (single as maximum occurrences is one)
SingleInput firstInput = inputs.get("FirstInput").getAsSingleInput();
Double first = firstInput.getObjectAs(Double.class);

// Get second input (multiple as maximum occurrences is greater than one)
MultipleInput secondInput = inputs.get("SecondInput").getAsMultipleInput();
List<Double> seconds = secondInput.getObjectsAs(Double.class);
```

The method returns an instance of `ProcessOutputs`, which contains the output objects.

```java
ProcessOutputs outputs = new ProcessOutputs();
outputs.add(new SingleOutput("Result", 0.5)); // fixed result for example
return outputs;
```

Once complete, add the fully qualified name of your process class to the framework configuration file, `src/main/resources/config.json`.

```javascript
{ 'encodingClasses': [],
  'gsonTypeAdapterClasses': [],
  'processClasses': [ 'com.example.YourProcessClass' ],
  'additionalProperties': [] }
```

Your project is now ready to be built and packaged into a web application archive (WAR) file.

```console
mvn clean package
```

The resulting WAR file can be deployed using Tomcat.