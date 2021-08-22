# JPA Models transform to SQL views.




## Requirements

- Java 8+
- Maven (build and run integration tool)
## Installation

Installing Maven on Mac OS X. [Here](https://www.baeldung.com/install-maven-on-windows-linux-mac#installing-maven-on-mac-os-x) 
Adding Maven to the Environment Path for macOS Catalina or Higher. [Here](https://www.baeldung.com/install-maven-on-windows-linux-mac#2-adding-maven-to-the-environment-path-for-macos-catalina-or-higher) 

After installation of the maven, the Next step is Building the project.

## The Building Steps. 


- Clone the project.
- Change the directory to the project folder JPAModel-transformer.

```sh
cd JPAModel-transformer
```
- Build the Project using the maven.
 
```sh
mvn clean compile assembly:single
mvn clean install assembly:single  
```


 
```
cd target
```
- Create example dir in the target.

```
mkdir example
```

- Copy all jar files included with JPA Models to example dir.
- Then run the project using the below command. 
```
java -classpath model-tranformer-1.0-SNAPSHOT-jar-with-dependencies.jar:../example/* org.transformer.SchemaGenerator
```
