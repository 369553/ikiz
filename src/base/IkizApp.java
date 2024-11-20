package base;

import ikiz.IkizIdare;
import ikiz.IkizTest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IkizApp{
    static ArrayList<Integer> list = new ArrayList<>();
    static ArrayList<String> aa = new ArrayList<>();
    static HashMap<Boolean, Integer> harita = new HashMap<Boolean, Integer>();
    static boolean[] boolArray;
    static String[] strArray;
    private static IkizIdare i;
    
    public static void main(String[] args){
//        dtfunction();
//        return;
        //mainFunction();
        //workingAboutListOrMapOrArrayField();
        test();
    }

//İŞLEM YÖNTEMLERİ:
    static void workingAboutListOrMapOrArrayField(){
        {// 1:
//            String[] strArray = new String[2];
//            int[] arrayOfInt = new int[2];
//            Integer[] IntegerArray = new Integer[2];
//            boolean[] arrayOfPrimitiveBoolean = new boolean[2];
//            Boolean[] BooleanArray = new Boolean[2];
//            System.out.println("strArray.class.typeName : " + strArray.getClass().getTypeName());
//            System.out.println("arrayOfInt.class.typeName : " + arrayOfInt.getClass().getTypeName());
//            System.out.println("IntegerArray.class.typeName : " + IntegerArray.getClass().getTypeName());
//            System.out.println("arrayOfPrimitiveBoolean.class.typeName : " + arrayOfPrimitiveBoolean.getClass().getTypeName());
//            System.out.println("BooleanArray.class.typeName : " + BooleanArray.getClass().getTypeName());
        }
        {// 2:
//            try{
//                Field fl = IkizApp.class.getDeclaredField("boolArray");
//                System.out.println("field.name : " + fl.getName());
//                System.out.println("field.type.name : " + fl.getType().getName());
//                System.out.println("field.type.simplename : " + fl.getType().getSimpleName());
//                System.out.println("field.type.typename : " + fl.getType().getTypeName());
//                System.out.println("field.type.isPrimitive : " + fl.getType().isPrimitive());
//                System.out.println("field.type.isArray : " + fl.getType().isArray());
//                System.out.println("field.type.toString : " + fl.getType().toString());
//            }
//            catch(Exception exc){
//                System.err.println("Hatâ oluştu!");
//            }
        }
        {// 3:
//            try{
//                Field fl = IkizApp.class.getDeclaredField("boolArray");
//                String[] splitted = fl.getType().getTypeName().split("\\.");
//                String typeNameWithBrackets = splitted[splitted.length - 1];
//                System.out.println("field.type.typename : " + typeNameWithBrackets);
//                System.out.println("Dizinin veri tipi ismi : " + typeNameWithBrackets.substring(0, typeNameWithBrackets.length() - 2));
//            }
//            catch(Exception exc){
//                System.err.println("Hatâ : " + exc.toString());
//            }
//            try{
//                String[] splitted2 = IkizApp.class.getDeclaredField("strArray").getType().getTypeName().split("\\.");
//                String t2 = splitted2[splitted2.length - 1];
//                System.out.println("field2.type.typename : " + t2);
//                System.out.println("İkinci dizinin veri tipi ismi : " + t2.substring(0, t2.length() - 2));
//            }
//            catch(Exception exc){
//                System.err.println("Hatâ : " + exc.toString());
//            }
        }
        {// 4:
//            try{
//                Class typeOfArray = ClassLoader.getSystemClassLoader().loadClass("java.lang.String");// Integer, int
//            if(typeOfArray == null)
//                System.err.println("İşlem başarısız!");
//            else
//                System.out.println("Sınıf alındı");
//            }
//            catch(Exception exc){
//                System.err.println("Hatâ  :  " + exc.toString());
//            }
        }
        {// 5:
//            try{
//                Class typeOf = IkizApp.class.getDeclaredField("list").getType();
//                System.out.println("type.name : " + typeOf.getName());
//                Class<?>[] allClasses = typeOf.getClass().getDeclaredClasses();
//                if(allClasses != null)
//                    System.out.println("Sınıflar alındı : " + allClasses.length);
//                for(Class cls : allClasses){
//                    System.out.println("field.type.class.name : " + cls.getName());
//                    System.out.println("field.type.class.typename : " + cls.getTypeName());
//                }
//                Class enclosing = typeOf.getEnclosingClass();
//                Class declaring = typeOf.getDeclaringClass();
//                Type genericSuperClass = typeOf.getGenericSuperclass();
//                if(enclosing != null)
//                    System.out.println("field.type.enclosingclass.name : " + enclosing.getName());
//                if(declaring != null)
//                    System.out.println("field.type.declaringclass.name : " + declaring.getName());
//                if(genericSuperClass != null){
//                    System.out.println("field.type.genericsuperclass.string: " + genericSuperClass.toString());
//                    System.out.println("genericSuperClass.str: " + genericSuperClass.toString());
//                    System.out.println("field.type.genericsuperclass.typename : " + genericSuperClass.getClass().getGenericSuperclass().getTypeName());
//                    System.out.println("field.type.genericsuperclass.class.name : " + genericSuperClass.getClass().getGenericSuperclass().getClass().getName());
//                }
//                System.out.println("type.class.name : " + typeOf.getAnnotatedInterfaces());
////                System.out.println("genericSuperClass.typename : " + genericSuperClass.getTypeName());
//                for(AnnotatedType type : typeOf.getAnnotatedInterfaces()){
//                    System.out.println("type.annotatedType.str : " + type.toString());
//                    System.out.println("type.annotatedType.type.name : " + type.getType().getTypeName());
//                    System.out.println("type.annotatedType.type.str : " + type.getType().toString());
//                    System.out.println("------------");
//                }
//            }
//            catch(NoSuchFieldException | SecurityException exc){
//                System.out.println("exc.toString : " + exc.toString());
//            }
        }
        {// 6:
//            try{
//                Field fl = IkizApp.class.getDeclaredField("list");
//                System.out.println("field.type.typename : " + fl.getType().getTypeName());
//            }
//            catch(Exception exc){
//                System.err.println("exc.toString : " + exc.toString());
//            }
        }
        {// 7:
//            try{
//                Class typeOf = IkizApp.class.getDeclaredField("list").getType();
//                System.out.println("type.name : " + typeOf.getName());
//                System.out.println("asASubclassOfList : " + typeOf.asSubclass(List.class));
//                System.out.println("type.genericSuperclass.name : " + typeOf.getGenericSuperclass().getTypeName());
//                for(Type type : typeOf.getGenericInterfaces()){
//                    System.out.println("genericInterface.typename : " + type.getTypeName());
//                    System.out.println("genericInterface.hashCode : " + type.hashCode());
//                    System.out.println("genericInterface.class.genericstr : " + type.getClass().toGenericString());
//                }
////                Class compType = typeOf.getComponentType();
////                if(compType != null){
////                    System.out.println("typeOf.componentType.name : " + typeOf.getComponentType().getName());
////                }
//            }
//            catch(SecurityException | ClassCastException | NoSuchFieldException exc){
//                System.err.println("Hatâ : " + exc.toString());
//            }
        }
        {// 8:
//            try{
//                Class typeOf = IkizApp.class.getDeclaredField("list").getType();
//                Object obj = typeOf.newInstance();
//                if(obj != null)
//                    System.out.println("obj üretildi");
//                List casted = (List) obj;
//                if(casted != null)
//                    System.out.println("İlgili alanın yeni değişkeni 'List' arayüzüne dönüştürüldü");
//                casted.add(17);
//                System.out.println("İlgili alanın generic tipine uygun olarak yapılan ekleme işlemi başarılı : " + casted.get(0));
//                try{// İlgili alanın veri tipine uygun olmayan bir veri eklemek istediğimizde;
//                    casted.add("A");
//                    System.out.println("İlgili alana uygun olmayan veri de maalesef yeni türetilen değişkene eklenmiş! : " + casted.get(1));
//                }
//                catch(Exception e4){
//                    System.err.println("!!!!! . İlgili alana uygun olmayan veri yeni türetilen değişkene eklenemedi : " + e4.toString());
//                }
//            }
//            catch(NoSuchFieldException | SecurityException | ClassCastException exc){
//                System.err.println("exc.toString : " + exc.toString());
//            }
//            catch(InstantiationException ex){
//                System.out.println("İlklendirme hatâsı : " + ex.toString());
//            }
//            catch(IllegalAccessException exc3){
//                System.out.println("Erişim hatâsı : " + exc3.toString());
//            }
        }
        {// 9: BULUNDU : reflect ile generic type'ı alma!
//            try{
//                Field fl = IkizApp.class.getDeclaredField("list");
//                Type genericType = fl.getGenericType();
//                String genTypeName = genericType.getTypeName();
//                System.out.println("genericType.typename : " + genTypeName);
//                String genericTypeFinally = genTypeName.substring(genTypeName.indexOf('<') + 1, genTypeName.indexOf('>'));
//                System.out.println("genericType finally : " + genericTypeFinally);
                // Başka işlem:
//                Object res = fl.getType().asSubclass(List.class);
//                System.out.println("asSubclass(List.class).Dönüş tipi : " + res.getClass().getName());
//                System.out.println("fl.getType().asSubclass(List.class) : " + res);
//                System.out.println("field.type.typename : " + fl.getType().getTypeName());
//            }
//            catch(Exception exc){
//                System.err.println("hatâ  : " + exc.toString());
//            }
            {// 10: Dizinin tipini ele geçirme:
//                try{
//                    Field fl = IkizApp.class.getDeclaredField("strArray");
//                    String genericTypeName = fl.getType().getTypeName();
//                    System.out.println("generic.typename : " + genericTypeName);
//                    String strFullName = genericTypeName.substring(0, genericTypeName.length() - 2);
//                    System.out.println("strFullName : " + strFullName);
//                }
//                catch(Exception exc){
//                    System.err.println("exc.toString : " + exc.toString());
//                }
            }
            {// 11: Haritalarla çalışırken tipleri ele geçirme vs.:
//                try{
//                    Field fl = IkizApp.class.getDeclaredField("harita");
//                    String genericTypeName = fl.getGenericType().getTypeName();
//                    System.out.println("generic.typename : " + genericTypeName);
//                    //generic.typename : java.util.HashMap<java.lang.String, java.lang.String>
//                    String[] splitted = genericTypeName.substring(genericTypeName.indexOf('<') + 1, genericTypeName.length() - 1).split(",");
//                    System.out.println("1. tip : " + splitted[0].trim());
//                    System.out.println("2. tip : " + splitted[1].trim());
//                }
//                catch(NoSuchFieldException | SecurityException exc){
//                    System.err.println("exc.toString : " + exc.toString());
//                }
            }
        }
    }
    static void test(){
        IkizTest test;
        test = IkizTest.startIkizTest(IkizTest.connectToDBForMySQL());
        //test = IkizTest.startIkizTest(IkizTest.connectToDBForMsSQL());
        //test.showTablesFromInterface();// BAŞARILI
        //test.produceNewTableWithPrimaryKey();// BAŞARILI
        //test.produceTableWithListField();// BAŞARILI
        //test.produceTableWithArrayField();// BAŞARILI
        //test.produceTableWithMapField();// BAŞARILI
        
        // VERİ ÇEKME SENARYOLARI:s
        
    }
    static void dtfunction(){
        String path = "C:\\Users\\Yazılım alanı\\Desktop\\DOGUTURK\\Kaynaklar (YAZI) (TAM, 2022 son düzenleme târihli).txt";
        File fl = new File(path);
        if(fl == null){
            System.err.println("Dosya açılamadı!");
            return;
        }
        BufferedReader buf;
        StringBuilder bui = new StringBuilder();
        try{
            buf = new BufferedReader(new FileReader(fl));
            String line;
            ArrayList<String> lines = new ArrayList<String>();
            while((line = buf.readLine()) != null){
//                bui.append(line);
                lines.add(line);
            }
            ArrayList<String> sources = new ArrayList<String>();
            ArrayList<Integer> starts = new ArrayList<Integer>();
//            String all = bui.toString();
            
            for(int sayac = 0; sayac < lines.size(); sayac++){
                String str = lines.get(sayac);
                int index = str.indexOf("url:");
                if(index == -1)
                    continue;
                starts.add(index);
                sources.add(str.substring(index + 4).trim());
            }
            
            System.out.println("url sayısı : " + sources.size());
//            for(int sayac = 0; sayac < sources.size(); sayac++){
//                System.out.println(sources.get(sayac) + "\n");
//            }
//            System.out.println("text:\n\n" + bui.toString());
            for(int sayac = 0; sayac < sources.size(); sayac++){
                for(int s2 = 0; s2 < sources.size() - sayac - 1; s2++){
                    if(sources.get(sayac).equals(sources.get(sayac + 1))){
                        System.err.println("Eşleşme var : " + sayac + " - " + s2);
                    }
                }
            }
        }
        catch(IOException exc){
            System.out.println("exc.toString : " + exc.toString());
        }
    }
    /*static void mainFunction(){
        Cvity connection = new Cvity(Cvity.connectDB("root", "LINQSE.1177", "localhost", "doguturk2", 3306),
                "root", "LINQSE.1177", "doguturk2");
        if(IkizIdare.startIkizIdare(connection) == false){
            System.err.println("Sistem başlatılamadı!..");
            return;
        }
        i = IkizIdare.getIkizIdare();
//        IkizIdare.getIkizIdare().produceTable(new testSinifi());
//        IkizIdare.getIkizIdare().addRowToDB(new testSinifi());
//        i.test("testSinifi");
        Object[] veriler = i.getData(testSinifi.class);
        if(veriler != null){
            for(int sayac = 0; sayac < veriler.length; sayac++){
                System.out.println("\n\n\n");
                testSinifi i = (testSinifi) veriler[sayac];
                System.out.println("veriler[" + sayac + "].getClass().getName(): " + i.numara);
                System.out.println("veriler[" + sayac + "].getNamePublic(): " + i.getNamePublic());
            }
        }
    }*/
}