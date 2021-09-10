# JPA Models transform to SQL views.




## Requirements

- Java 8+
- Maven (build and run integration tool)
##  Installing Maven

To install Maven on windows, we head over to the [Apache Maven site](https://maven.apache.org/download.cgi) to download the latest version and select the Maven zip file, for example, unzip apache-maven-3.8.2-bin.zip

Then we unzip it to the folder where we want Maven to live.


## Setup Environment variable for maven in.

Check environment variable value e.g.

```
1.echo %JAVA_HOME% 
2.C:\Program Files\Java\jdk1.7.0_51

```
- Adding to PATH: Add the unpacked distribution’s bin directory to your user PATH environment variable by opening up the system properties (WinKey + Pause), selecting the “Advanced” tab, and the “Environment Variables” button, then adding or selecting the PATH variable in the user variables with the value C:\Program Files\apache-maven-3.8.2\bin. The same dialog can be used to set JAVA_HOME to the location of your JDK, e.g. C:\Program Files\Java\jdk1.7.0_51

- Open a new command prompt (Winkey + R then type cmd) and run mvn -v to verify the installation.


## Setup Environment variable for maven in Unix-based Operating System (Linux, Solaris, and Mac OS X).

- Check environment variable value

```
1. echo $JAVA_HOME
2. /Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home

```

- Adding to PATH

```
export PATH=/opt/apache-maven-3.8.2/bin:$PATH
```
- Open a new terminal and run mvn -v to verify the installation.

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

When running the program you have to give packages of JPA model class separated from space.

> example: org.test.models com.app.models


![Alt Text](https://media.giphy.com/media/dtak0uxjstXEyf3SbG/giphy.gif?cid=790b7611d8bfea2fa633668166fa510399660097af40ea35&rid=giphy.gif&ct=g)

After the run the command you can see the  jpa-view.sql in the scripts dir in the root directory of the project.

The output sql view.
![alt text](https://drive.google.com/uc?export=view&id=1AkzJJUs8qpk71nDpOKIS6WR8GQXHhJT3)


##  Redaction with special column names with SQL file.

If you place the redacted.csv file in the dir of the final jar file according to the following format, you can have the jpa-views-redacted.sql file in the scripts dir.


The fromat of redacted.csv.

![alt text](https://drive.google.com/uc?export=view&id=15sakGEn44MLs6DRfE_gpyg7yk_JDizMd)

The jpa-views-redacted.sql file in scrips dir.

![alt text](https://drive.google.com/uc?export=view&id=1Kr-Ig0UaUBdW1kwZS1t-h8u8c8j8VFlZ)

