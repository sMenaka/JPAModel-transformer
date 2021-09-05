package org.transformer;


import org.transformer.utils.SQLUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class SchemaGenerator {

    public static void main(String[] args) throws Exception {
        System.out.print("Enter packages name separating from space : ");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String  lines = br.readLine();
        String packageName[] = lines.trim().split("\\s+");
        Map<String,List<String>> map=readRedactedCSVAsMap();
        for (Class<?> aClass : getClassesInPackage(packageName[0])) {
            String onlyClass = aClass.getName().substring(aClass.getName().lastIndexOf('.') + 1);
            SQLUtils.appendSQLFile(SQLUtils.getSQLView(aClass),"jpa-views.sql");
            SQLUtils.tableDescriptionCSVGeneration(SQLUtils.getTableDescription(aClass));
            Map<String,List<String>> redact = readRedactedCSVAsMap();
            if (redact !=null) {
                SQLUtils.appendSQLFile(SQLUtils.getSQLView(aClass,redact),"jpa-views-redacted.sql");
            }
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

    private static  Map<String,List<String>> readRedactedCSVAsMap() throws IOException {
        if (Files.exists(Paths.get("redacted.csv"))) {
            Map<String,List<String>> redactedMap = new HashMap<>();
            BufferedReader br = new BufferedReader(new FileReader("redacted.csv"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String key = values[0];
                String value = values[1];
                key = key.replaceAll("\\s","");
                value = value.replaceAll("\\s","");
                if (redactedMap.containsKey(key)) {
                    List<String> lst = redactedMap.get(key);
                    lst.add(value);
                    redactedMap.put(key,lst);
                }
                else {
                    List<String> lst = new ArrayList<>();
                    lst.add(value);
                    redactedMap.put(key,lst);
                }

            }
            return redactedMap;
        }else{
            return  null;
        }
    }
}
