package org.transformer;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;


import javax.persistence.Column;
import javax.persistence.Table;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
public class SchemaGenerator {

    public static void main(String[] args) throws Exception {
        System.out.print("Enter packages name separating from space : ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String  lines = br.readLine();
        String packageName[] = lines.trim().split("\\s+");
        for (Class<?> aClass : getClassesInPackage(packageName[0])) {
            appendSQLFile(getSQLView(aClass));

        }

        File file = new File("../scripts/jpa-views.sql");
        if (file.exists()) {
            System.out.println("Successfully Created SQL file in the scripts folder.");
        }

    }



// Grab class from package
    public static final List<Class<?>> getClassesInPackage(String packageName) {
        String path = packageName.replaceAll("\\.", File.separator);
        List<Class<?>> classes = new ArrayList<>();
        String[] classPathEntries = System.getProperty("java.class.path").split(
                System.getProperty("path.separator")
        );

        String name;
        for (String classpathEntry : classPathEntries) {
            if (classpathEntry.endsWith(".jar")) {
                File jar = new File(classpathEntry);
                try {
                    JarInputStream is = new JarInputStream(new FileInputStream(jar));
                    JarEntry entry;
                    while((entry = is.getNextJarEntry()) != null) {
                        name = entry.getName();
                        if (name.endsWith(".class")) {
                            if (name.contains(path) && name.endsWith(".class")) {
                                String classPath = name.substring(0, entry.getName().length() - 6);
                                classPath = classPath.replaceAll("[\\|/]", ".");
                                classes.add(Class.forName(classPath));
                            }
                        }
                    }
                } catch (Exception ex) {
                }
            } else {
                try {
                    File base = new File(classpathEntry + File.separatorChar + path);
                    for (File file : base.listFiles()) {
                        name = file.getName();
                        if (name.endsWith(".class")) {
                            name = name.substring(0, name.length() - 6);
                            classes.add(Class.forName(packageName + "." + name));
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }

        return classes;
    }

//Sql view Generation method.
    private static String getSQLView(Class<?> cl){
        String sql="";
        String onlyClass = cl.getName().substring(cl.getName().lastIndexOf('.') + 1);
        if (cl.isAnnotationPresent(Table.class)) {
            Table t = cl.getAnnotation(Table.class);
            sql = sql.concat("--SQL view of " + t.name() + "\n");
            sql = sql.concat("CREATE VIEW " + onlyClass + " AS\n" + "SELECT ");

            for (Field field : cl.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String columnName = column.name();
                    sql = sql.concat(columnName + " ,");
                }
            }
            sql = sql.substring(0,sql.length()-2);
            sql = sql.concat("\nFROM " + t.name()+";");
        }
        return sql;
    }
// Create and Append  script to the sql file
    private static void appendSQLFile(String script) throws IOException {
        if (!script.isEmpty()) {
            File theDir = new File("../scripts");
            if (!theDir.exists()){
                theDir.mkdirs();
                Files.write(Paths.get("../scripts/jpa-views.sql"),(script+System.lineSeparator()+System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            }
            else {
                Files.write(Paths.get("../scripts/jpa-views.sql"),(script+System.lineSeparator()+System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            }

        }

    }
}
