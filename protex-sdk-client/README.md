# Black Duck Software Protex SDK Client

The Protex SDK client provides Java API wrappers around SOAP message handling between client-side programs and the server-side WSDL end points

## Building

The Protex SDK client Java files and jar are built from WSDL source files provided by Black Duck, which match the WSDL files hosted as end points on the application server.

Java wrappers are generated via the [CXF WSDL to Java](http://cxf.apache.org/docs/wsdl-to-java.html) tool, and use the [CXF](http://cxf.apache.org/) library for the underlying SOAP message handling

The wsdltojava command which generates the client Java files has been linked into the compileJava Gradle target