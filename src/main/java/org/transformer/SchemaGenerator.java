package org.transformer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.reflections.Reflections;

import javax.persistence.Table;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.logging.Logger;
public class SchemaGenerator {

    private Configuration cfg;

    static Logger log =  java.util.logging.Logger.getLogger(SchemaGenerator.class.getName());

    public static void main(String[] args) throws Exception {


        String currentUsersHomeDir = System.getProperty("user.home");
        String directory = currentUsersHomeDir+ "/upwork/";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String  lines = br.readLine();
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);
        String packageName[] = lines.trim().split("\\s+");
        for (Class<?> aClass : getClassesInPackage(packageName[0])) {
           log.info(getSQLView(aClass));

        }

    }




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
                    // Silence is gold
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
                    // Silence is gold
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
            sql = sql.concat("--SQL view of "+t.name()+"\n");
            sql = sql.concat("CREATE VIEW "+onlyClass+" AS\n"+t.name()+" SELECT");
        }
        
        return sql;
    }


}
