package org.transformer.utils;

import com.google.common.base.CaseFormat;

import javax.persistence.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLUtils {


    //---------------- Sql view Generation method. ----------------------
    public static String getSQLView(Class<?> cl){
        String sql = "";
        String afterSql = "";
        String onlyClass = cl.getName().substring(cl.getName().lastIndexOf('.') + 1);
        if (cl.isAnnotationPresent(Table.class)) {
            Table t = cl.getAnnotation(Table.class);
            if (t.name() != null) {
                sql = sql.concat("--SQL view of " + t.name() + "\n");
                sql = sql.concat("DROP VIEW IF EXISTS "+t.name()+";\n");
                sql = sql.concat("CREATE VIEW " + onlyClass + " AS\n" + "SELECT ");
            }else {
                sql = sql.concat("--SQL view of " + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,onlyClass) + "\n");
                sql = sql.concat("DROP VIEW IF EXISTS "+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,onlyClass)+";\n");
                sql = sql.concat("CREATE VIEW " + onlyClass + " AS\n" + "SELECT ");
            }


            for (Field field : cl.getDeclaredFields()) {
                //Id field
                if (field.isAnnotationPresent(Id.class)) {
                    sql = sql.concat(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName())+ " AS "+field.getName()+", ");
                }

                //Usual column name
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    if (column.name() != null) {
                        String columnName = column.name();
                        sql = sql.concat(columnName + " AS "+ field.getName()+", ");
                    }else {
                        sql = sql.concat(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName())+ " AS "+field.getName() + " AS "+ field.getName()+", ");
                    }

                }

                //OneToOne column name
                if (field.isAnnotationPresent(OneToOne.class) && field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        String columnName = joinColumn.name();
                        sql = sql.concat(columnName + " AS "+ field.getName()+", ");
                    }else {
                        sql = sql.concat(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName())+ " AS "+field.getName() + " AS "+ field.getName()+", ");
                    }
                    Class<?> another = field.getType();
                    afterSql = afterSql.concat(joinTableViewCreation(cl,another)+"\n\n");

                }
                 //OneToMany column name
                if(field.isAnnotationPresent(ManyToOne.class)){
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        String columnName = joinColumn.name();
                        sql = sql.concat(columnName + " AS "+ field.getName()+", ");
                    }else {
                        sql = sql.concat(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName())+ " AS "+field.getName() + " AS "+ field.getName()+", ");
                    }
                    Class<?> another = field.getType();
                    afterSql = afterSql.concat(joinTableViewCreation(cl,another)+"\n\n");
                }
                // ManyToMany relationship.
                if (field.isAnnotationPresent(ManyToMany.class) && field.isAnnotationPresent(JoinTable.class)) {
                    String manyTOMany = "";
                    JoinTable table = field.getAnnotation(JoinTable.class);
                    manyTOMany = manyTOMany.concat("--SQL view of " + table.name() + "\n");
                    manyTOMany = manyTOMany.concat("DROP VIEW IF EXISTS "+table.name()+";\n");
                    manyTOMany = manyTOMany.concat("CREATE VIEW " + table.name() + " AS\n" + "SELECT ");

                    for (JoinColumn joinColumn : table.joinColumns()) {
                            String columnName = joinColumn.name();
                            manyTOMany = manyTOMany.concat(columnName + " AS "+ columnName+", ");

                    }

                    for (JoinColumn joinColumn : table.inverseJoinColumns()) {

                            String columnName = joinColumn.name();
                            manyTOMany = manyTOMany.concat(columnName + " AS "+ columnName+", ");

                    }
                    manyTOMany = manyTOMany.substring(0,manyTOMany.length()-2);
                    manyTOMany = manyTOMany.concat("\nFROM " + table.name()+";");
                    afterSql = afterSql.concat(manyTOMany+"\n\n");
                }

            }

            sql = sql.substring(0,sql.length()-2);
            if (t.name() != null) {
                sql = sql.concat("\nFROM " +t.name()+";");
            }else {
                sql = sql.concat("\nFROM " + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,onlyClass)+";");
            }

        }
        if (afterSql.equals("")) {
            return sql;
        }else {
            return sql.concat("\n\n"+afterSql);
        }

    }








    //---------------- Sql join table View Generation method. ----------------------
    public static String joinTableViewCreation(Class<?> table1,Class<?> table2){
        String sql = "";
        String table1Class = table1.getName().substring(table1.getName().lastIndexOf('.') + 1);
        String table2Class = table2.getName().substring(table2.getName().lastIndexOf('.') + 1);
        if (table1.isAnnotationPresent(Table.class) && table2.isAnnotationPresent(Table.class)) {
            Table t1 = table1.getAnnotation(Table.class);
            Table t2 = table2.getAnnotation(Table.class);
            sql = sql.concat("--SQL view of " +table1Class+"_"+table2Class+ "\n");
            sql = sql.concat("DROP VIEW IF EXISTS "+table1Class+"_"+table2Class+";\n");
            sql = sql.concat("CREATE VIEW "+table1Class+"_"+table2Class+" AS \nSELECT ");

            for (Field field : table1.getDeclaredFields()) {
                //Id field
                if (field.isAnnotationPresent(Id.class)) {
                    sql.concat("ta."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName())+ " AS "+field.getName()+", ");
                }

                //Usual column name
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String columnName = column.name();
                    if (column.name() != null) {
                        sql = sql.concat("ta."+columnName + " AS "+ field.getName()+", ");
                    }else {
                        sql = sql.concat("ta."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()) + " AS "+ field.getName()+", ");
                    }

                }

                //OneToOne column name
                if (field.isAnnotationPresent(OneToOne.class) && field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        sql = sql.concat("ta."+joinColumn.name() + " AS "+ field.getName()+", ");
                    }else {
                        sql = sql.concat("ta."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()) + " AS "+ field.getName()+", ");
                    }


                }
                // //OneToMany column name
                if(field.isAnnotationPresent(ManyToOne.class)){
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        sql = sql.concat("ta."+joinColumn.name() + " AS "+ field.getName()+", ");
                    }else {
                        sql = sql.concat("ta."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()) + " AS "+ field.getName()+", ");
                    }

                }
            }
            sql = sql.concat("\n");
            for (Field field : table2.getDeclaredFields()) {
                //Id field
                if (field.isAnnotationPresent(Id.class)) {
                    sql.concat("tb."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName())+ " AS "+field.getName()+", ");
                }

                //Usual column name
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String columnName = column.name();
                    if (column.name() != null) {
                        sql = sql.concat("tb."+columnName + " AS "+ field.getName()+", ");
                    }else {
                        sql = sql.concat("tb."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()) + " AS "+ field.getName()+", ");
                    }
                }

                //OneToOne column name
                if (field.isAnnotationPresent(OneToOne.class) && field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        sql = sql.concat("tb."+joinColumn.name() + " AS "+ field.getName()+", ");
                    }else {
                        sql = sql.concat("tb."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()) + " AS "+ field.getName()+", ");
                    }

                }
                // //OneToMany column name
                if(field.isAnnotationPresent(ManyToOne.class)){
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        sql = sql.concat("tb."+joinColumn.name() + " AS "+ field.getName()+", ");
                    }else {
                        sql = sql.concat("tb."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()) + " AS "+ field.getName()+", ");
                    }
                }
            }
            sql = sql.substring(0,sql.length()-2);

            if (t1.name() != null) {
                sql = sql.concat("\nFROM " + t1.name()+" ta,");
            }else {
                sql = sql.concat("\nFROM " + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,table1Class)+" ta,");
            }

            if (t2.name() != null) {
                sql = sql.concat(t2.name()+" tb;");
            }else {
                sql = sql.concat(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,table2Class) +" tb;");
            }
        }
        return sql;

    }








    //---------------- Table Description Generation method. ----------------------

    public static List<List<String>>  getTableDescription(Class<?> cl){

        List<List<String>> sql = new ArrayList<>();
        List<List<String>> afterSql = new ArrayList<>();
        String onlyClass = cl.getName().substring(cl.getName().lastIndexOf('.') + 1);
        if (cl.isAnnotationPresent(Table.class)) {
            Table t = cl.getAnnotation(Table.class);
            if (t.name() != null) {
                List<String> subSql = new ArrayList<>();
                subSql.add("TABLE");
                subSql.add(t.name());
                subSql.add(onlyClass);
                sql.add(subSql);
            }else {
                List<String> subSql = new ArrayList<>();
                subSql.add("TABLE");
                subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,onlyClass));
                subSql.add(onlyClass);
                sql.add(subSql);
            }


            for (Field field : cl.getDeclaredFields()) {
                //Id field
                if (field.isAnnotationPresent(Id.class)) {
                    List<String> subSql = new ArrayList<>();
                    subSql.add("COLUMN");
                    subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()));
                    subSql.add(field.getName());
                    sql.add(subSql);
                }

                //Usual column name
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    if (column.name() != null) {
                        String columnName = column.name();
                        List<String> subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(columnName);
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }else {
                        List<String> subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()));
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }
                }

                //OneToOne column name
                if (field.isAnnotationPresent(OneToOne.class) && field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);

                    if (joinColumn.name() != null) {
                        String columnName = joinColumn.name();
                        List<String> subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(columnName);
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }else {
                        List<String> subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()));
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }

                    Class<?> another = field.getType();
                    afterSql.addAll(getJoinTableDescription(cl,another));

                }
                // //OneToMany column name
                if(field.isAnnotationPresent(ManyToOne.class)){
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        String columnName = joinColumn.name();
                        List<String> subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(columnName);
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }else {
                        List<String> subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()));
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }
                    Class<?> another = field.getType();
                    afterSql.addAll(getJoinTableDescription(cl,another));

                }

            }

        }
        if (!afterSql.isEmpty()) {
            sql.addAll(afterSql);
        }

        return sql;

    }








    //---------------- Join Table Description Generation method. ----------------------

    public static List<List<String>> getJoinTableDescription(Class<?> table1,Class<?> table2){
       List<List<String>> sql = new ArrayList<>();
        String table1Class = table1.getName().substring(table1.getName().lastIndexOf('.') + 1);
        String table2Class = table2.getName().substring(table2.getName().lastIndexOf('.') + 1);
        if (table1.isAnnotationPresent(Table.class) && table2.isAnnotationPresent(Table.class)) {
            Table t1 = table1.getAnnotation(Table.class);
            Table t2 = table2.getAnnotation(Table.class);
            List<String> subSql = new ArrayList<>();
            subSql.add("TABLE");
            subSql.add(table1Class+"_"+table2Class);
            subSql.add(table1Class+"_"+table2Class);
             sql.add(subSql);


            for (Field field : table1.getDeclaredFields()) {
                //Id field
                if (field.isAnnotationPresent(Id.class)) {
                    subSql = new ArrayList<>();
                    subSql.add("COLUMN");
                    subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()));
                    subSql.add(field.getName());
                    sql.add(subSql);
                }

                //Usual column name
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String columnName = column.name();
                    if (column.name() != null) {
                        subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(columnName);
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }else {
                        subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()));
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }

                }

                //OneToOne column name
                if (field.isAnnotationPresent(OneToOne.class) && field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(joinColumn.name());
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }else {
                        subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()));
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }


                }
                // //OneToMany column name
                if(field.isAnnotationPresent(ManyToOne.class)){
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(joinColumn.name());
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }else {
                        subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()));
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }

                }
            }

            for (Field field : table2.getDeclaredFields()) {
                //Id field
                if (field.isAnnotationPresent(Id.class)) {
                    subSql = new ArrayList<>();
                    subSql.add("COLUMN");
                    subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()));
                    subSql.add(field.getName());
                    sql.add(subSql);
                }

                //Usual column name
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String columnName = column.name();
                    if (column.name() != null) {
                        subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(columnName);
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }else {
                        subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()));
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }
                }

                //OneToOne column name
                if (field.isAnnotationPresent(OneToOne.class) && field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(joinColumn.name());
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }else {
                        subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()));
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }

                }
                // //OneToMany column name
                if(field.isAnnotationPresent(ManyToOne.class)){
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(joinColumn.name());
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }else {
                        subSql = new ArrayList<>();
                        subSql.add("COLUMN");
                        subSql.add(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()));
                        subSql.add(field.getName());
                        sql.add(subSql);
                    }
                }
            }
        }
        return sql;
    }









    //---------------- Table Description file Creation method. ----------------------

    public static void tableDescriptionCSVGeneration(List<List<String>> text) throws IOException {
        if (!text.isEmpty()) {
            if (!Files.exists(Paths.get("../scripts/meta-data.csv"))) {
                String[] headers = {"TYPE","SQL_NAME","JPA_NAME"};
                Files.write(Paths.get("../scripts/meta-data.csv"),(String.join(",", headers)+System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            }
            File theDir = new File("../scripts");
            File file = new File("../scripts/meta-data.csv");
            FileWriter csvWriter = new FileWriter(file,true);
            if (!theDir.exists()){
                theDir.mkdirs();
                for (List<String> strings : text) {
                    Files.write(Paths.get("../scripts/meta-data.csv"),(String.join(",", strings)+System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                }
            }
            else {
                for (List<String> strings : text) {
                    Files.write(Paths.get("../scripts/meta-data.csv"),(String.join(",", strings)+System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                }
            }

            csvWriter.flush();
            csvWriter.close();
        }

    }









  //     ---------------- Sql view Generation with redact fields method. ----------------------

    public static String getSQLView(Class<?> cl, Map<String,List<String>> redact){
        String sql = "";
        String afterSql = "";
        String onlyClass = cl.getName().substring(cl.getName().lastIndexOf('.') + 1);
            List<String> toRedactFields = redact.get(onlyClass);
            boolean isTableRedacted = redact.containsKey(onlyClass);
            if (cl.isAnnotationPresent(Table.class)) {
                Table t = cl.getAnnotation(Table.class);
                if (t.name() != null) {
                    sql = sql.concat("--SQL view of " + t.name() + "\n");
                    sql = sql.concat("DROP VIEW IF EXISTS "+t.name()+";\n");
                    sql = sql.concat("CREATE VIEW " + onlyClass + " AS\n" + "SELECT ");
                }else {
                    sql = sql.concat("--SQL view of " + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,onlyClass) + "\n");
                    sql = sql.concat("DROP VIEW IF EXISTS "+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,onlyClass)+";\n");
                    sql = sql.concat("CREATE VIEW " + onlyClass + " AS\n" + "SELECT ");
                }


                for (Field field : cl.getDeclaredFields()) {
                    //Id field
                    if (field.isAnnotationPresent(Id.class)) {
                        if (isTableRedacted && toRedactFields.contains(field.getName())) {
                            sql = sql.concat("\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName())+ " AS "+field.getName()+", ");
                        }
                    }

                    //Usual column name
                    if (field.isAnnotationPresent(Column.class)) {
                        Column column = field.getAnnotation(Column.class);
                        if (column.name() != null) {

                            String columnName = column.name();
                            if (isTableRedacted && toRedactFields.contains(field.getName())) {
                                sql = sql.concat("\'redacted\'"+ " AS "+field.getName()+", ");
                            }else {
                                sql = sql.concat(columnName + " AS "+ field.getName()+", ");
                            }
                        }else {
                            if (isTableRedacted && toRedactFields.contains(field.getName())) {
                                sql = sql.concat("\'redacted\'"+ " AS "+field.getName()+", ");
                            }else {
                                sql = sql.concat(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName())+ " AS "+field.getName() + " AS "+ field.getName()+", ");
                            }
                        }

                    }

                    //OneToOne column name
                    if (field.isAnnotationPresent(OneToOne.class) && field.isAnnotationPresent(JoinColumn.class)) {
                        JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                        if (joinColumn.name() != null) {
                            String columnName = joinColumn.name();
                            if (isTableRedacted && toRedactFields.contains(field.getName())) {
                                sql = sql.concat("\'redacted\'"+ " AS "+field.getName()+", ");
                            }else {
                                sql = sql.concat(columnName + " AS "+ field.getName()+", ");
                            }
                        }else {
                            if (isTableRedacted && toRedactFields.contains(field.getName())) {
                                sql = sql.concat("\'redacted\'"+ " AS "+field.getName()+", ");
                            }else {
                                sql = sql.concat(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName())+ " AS "+field.getName() + " AS "+ field.getName()+", ");
                            }
                        }
                        Class<?> another = field.getType();
                        afterSql = afterSql.concat(joinTableViewCreation(cl,another,redact)+"\n\n");

                    }
                    //OneToMany column name
                    if(field.isAnnotationPresent(ManyToOne.class)){
                        JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                        if (joinColumn.name() != null) {
                            String columnName = joinColumn.name();
                            if (isTableRedacted && toRedactFields.contains(field.getName())) {
                                sql = sql.concat("\'redacted\'"+ " AS "+field.getName()+", ");
                            }else {
                                sql = sql.concat(columnName + " AS "+ field.getName()+", ");
                            }
                        }else {
                            if (isTableRedacted && toRedactFields.contains(field.getName())) {
                                sql = sql.concat("\'redacted\'"+ " AS "+field.getName()+", ");
                            }else {
                                sql = sql.concat(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName())+ " AS "+field.getName() + " AS "+ field.getName()+", ");
                            }
                        }
                        Class<?> another = field.getType();
                        afterSql = afterSql.concat(joinTableViewCreation(cl,another,redact)+"\n\n");
                    }

                    // ManyToMany relationship.
                    if (field.isAnnotationPresent(ManyToMany.class) && field.isAnnotationPresent(JoinTable.class)) {
                        String manyTOMany = "";
                        JoinTable table = field.getAnnotation(JoinTable.class);

                            List<String> toManyTManyRedacted = redact.get(table.name());
                            manyTOMany = manyTOMany.concat("--SQL view of " + table.name() + "\n");
                            manyTOMany = manyTOMany.concat("DROP VIEW IF EXISTS "+table.name()+";\n");
                            manyTOMany = manyTOMany.concat("CREATE VIEW " + table.name() + " AS\n" + "SELECT ");

                            for (JoinColumn joinColumn : table.joinColumns()) {
                                String columnName = joinColumn.name();
                                if (redact.containsKey(table.name()) && toManyTManyRedacted.contains(columnName)) {
                                    manyTOMany = manyTOMany.concat("\'redacted\'" + " AS "+ columnName+", ");
                                }else {
                                    manyTOMany = manyTOMany.concat(columnName + " AS "+ columnName+", ");
                                }


                            }

                            for (JoinColumn joinColumn : table.inverseJoinColumns()) {

                                String columnName = joinColumn.name();
                                if (redact.containsKey(table.name()) && toManyTManyRedacted.contains(columnName)) {
                                    manyTOMany = manyTOMany.concat("\'redacted\'" + " AS "+ columnName+", ");
                                }else {
                                    manyTOMany = manyTOMany.concat(columnName + " AS "+ columnName+", ");
                                }

                            }
                            manyTOMany = manyTOMany.substring(0,manyTOMany.length()-2);
                            manyTOMany = manyTOMany.concat("\nFROM " + table.name()+";");
                            afterSql = afterSql.concat(manyTOMany+"\n\n");

                    }

                }

                sql = sql.substring(0,sql.length()-2);
                if (t.name() != null) {
                    sql = sql.concat("\nFROM " +t.name()+";");
                }else {
                    sql = sql.concat("\nFROM " + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,onlyClass)+";");
                }

            }
            if (afterSql.equals("")) {
                return sql;
            }else {
                return sql.concat("\n\n"+afterSql);
            }


    }








    //---------------- Sql join table View Generation with redact method. ----------------------
    public static String joinTableViewCreation(Class<?> table1,Class<?> table2,Map<String,List<String>> redact){
        String sql = "";
        String table1Class = table1.getName().substring(table1.getName().lastIndexOf('.') + 1);
        String table2Class = table2.getName().substring(table2.getName().lastIndexOf('.') + 1);
        List<String> toRedactFieldsTable1 = redact.get(table1Class);
        List<String> toRedactFieldsTable2= redact.get(table2Class);
        boolean isTable1Redact = redact.containsKey(table1Class);
        boolean isTable2Redact = redact.containsKey(table2Class);
        if (table1.isAnnotationPresent(Table.class) && table2.isAnnotationPresent(Table.class)) {
            Table t1 = table1.getAnnotation(Table.class);
            Table t2 = table2.getAnnotation(Table.class);
            sql = sql.concat("--SQL view of " +table1Class+"_"+table2Class+ "\n");
            sql = sql.concat("DROP VIEW IF EXISTS "+table1Class+"_"+table2Class+";\n");
            sql = sql.concat("CREATE VIEW "+table1Class+"_"+table2Class+" AS \nSELECT ");
            for (Field field : table1.getDeclaredFields()) {
                //Id field
                if (field.isAnnotationPresent(Id.class)) {
                    if (isTable1Redact && toRedactFieldsTable1.contains(field.getName())) {
                        sql.concat("ta."+"\'redacted\'"+ " AS "+field.getName()+", ");
                    }else {
                        sql.concat("ta."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName())+ " AS "+field.getName()+", ");
                    }

                }
                //Usual column name
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String columnName = column.name();
                    if (column.name() != null) {
                        if (isTable1Redact && toRedactFieldsTable1.contains(field.getName())) {
                            sql.concat("ta."+"\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat("ta."+columnName + " AS "+ field.getName()+", ");
                        }

                    }else {
                        if (isTable1Redact && toRedactFieldsTable1.contains(field.getName())) {
                            sql.concat("ta."+"\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat("ta."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()) + " AS "+ field.getName()+", ");
                        }
                    }
                }

                //OneToOne column name
                if (field.isAnnotationPresent(OneToOne.class) && field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        if (isTable1Redact && toRedactFieldsTable1.contains(field.getName())) {
                            sql.concat("ta."+"\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat("ta."+joinColumn.name() + " AS "+ field.getName()+", ");
                        }
                    }else {
                        if (isTable1Redact && toRedactFieldsTable1.contains(field.getName())) {
                            sql.concat("ta."+"\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat("ta."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()) + " AS "+ field.getName()+", ");
                        }
                    }


                }
                // //OneToMany column name
                if(field.isAnnotationPresent(ManyToOne.class)){
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        if (isTable1Redact && toRedactFieldsTable1.contains(field.getName())) {
                            sql.concat("ta."+"\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat("ta."+joinColumn.name() + " AS "+ field.getName()+", ");
                        }

                    }else {

                        if (isTable1Redact && toRedactFieldsTable1.contains(field.getName())) {
                            sql.concat("ta."+"\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat("ta."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()) + " AS "+ field.getName()+", ");
                        }

                    }

                }
            }
            sql = sql.concat("\n");
            for (Field field : table2.getDeclaredFields()) {
                //Id field
                if (field.isAnnotationPresent(Id.class)) {
                    if (isTable2Redact && toRedactFieldsTable2.contains(field.getName())) {
                        sql.concat("tb."+"\'redacted\'"+ " AS "+field.getName()+", ");
                    }else {
                        sql.concat("tb."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName())+ " AS "+field.getName()+", ");
                    }
                }

                //Usual column name
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String columnName = column.name();
                    if (column.name() != null) {
                        if (isTable2Redact && toRedactFieldsTable2.contains(field.getName())) {
                            sql.concat("tb."+"\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat("tb."+columnName + " AS "+ field.getName()+", ");
                        }
                    }else {
                        if (isTable2Redact && toRedactFieldsTable2.contains(field.getName())) {
                            sql.concat("tb."+"\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat("tb."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()) + " AS "+ field.getName()+", ");
                        }
                    }
                }

                //OneToOne column name
                if (field.isAnnotationPresent(OneToOne.class) && field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        if (isTable2Redact && toRedactFieldsTable2.contains(field.getName())) {
                            sql.concat("tb."+"\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat("tb."+joinColumn + " AS "+ field.getName()+", ");
                        }
                    }else {
                        if (isTable2Redact && toRedactFieldsTable2.contains(field.getName())) {
                            sql.concat("tb."+"\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat("tb."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()) + " AS "+ field.getName()+", ");
                        }
                    }

                }
                // //OneToMany column name
                if(field.isAnnotationPresent(ManyToOne.class)){
                    JoinColumn joinColumn =field.getAnnotation(JoinColumn.class);
                    if (joinColumn.name() != null) {
                        if (isTable2Redact && toRedactFieldsTable2.contains(field.getName())) {
                            sql.concat("tb."+"\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat("tb."+joinColumn + " AS "+ field.getName()+", ");
                        }
                    }else {
                        if (isTable2Redact && toRedactFieldsTable2.contains(field.getName())) {
                            sql.concat("tb."+"\'redacted\'"+ " AS "+field.getName()+", ");
                        }else {
                            sql = sql.concat("tb."+CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,field.getName()) + " AS "+ field.getName()+", ");
                        }                    }
                }
            }
            sql = sql.substring(0,sql.length()-2);

            if (t1.name() != null) {
                sql = sql.concat("\nFROM " + t1.name()+" ta,");
            }else {
                sql = sql.concat("\nFROM " + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,table1Class)+" ta,");
            }

            if (t2.name() != null) {
                sql = sql.concat(t2.name()+" tb;");
            }else {
                sql = sql.concat(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,table2Class) +" tb;");
            }
        }
        return sql;

    }








    //---------------- Sql file creation method. ----------------------

    public static void appendSQLFile(String script,String fileName) throws IOException {
        if (script!=null && !script.isEmpty()) {
            File theDir = new File("../scripts");
            if (!theDir.exists()){
                theDir.mkdirs();
                Files.write(Paths.get("../scripts/"+fileName),(script+System.lineSeparator()+System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            }
            else {
                Files.write(Paths.get("../scripts/"+fileName),(script+System.lineSeparator()+System.lineSeparator()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            }
        }
    }


    //------------- End of the Class. ---------------

}
