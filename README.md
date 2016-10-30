## wsdl2plsql

Generates PL/SQL client code for SOAP Services from its WSDL.

### Requirements

You need to install in your environment: 

* Java SE 1.6+
* Maven 3.0+

### How to run it?

##### 1. Clone dev-root branch

`git clone -b dev-root https://github.com/pcmnac/wsdl2plsql.git` 

##### 2. Navigate to source code folder

`cd code/wsdl2plsql` (Linux)  
`cd code\wsdl2plsql` (Windows)

##### 3. Create an executable jar

`mvn clean compile assembly:single`

##### 4. Run the code generator

`java -jar target/wsdl2psql.jar -n <plsql-package-name> -i <wsdl-url-or-file>` (Linux)  
`java -jar target\wsdl2psql.jar -n <plsql-package-name> -i <wsdl-url-or-file>` (Windows)

### Notes

To see all command line options run with -h or --help option.

`java -jar target/wsdl2psql.jar -h` (Linux)  
`java -jar target\wsdl2psql.jar -h` (Windows)

That's all!
