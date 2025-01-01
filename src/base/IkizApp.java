package base;

import ikiz.IkizIdare;
import ikiz.IkizTest;
import ikiz.ReflectorForRunTime;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.reflect.annotation.AnnotatedTypeFactory;
import sun.reflect.annotation.TypeAnnotation;

public class IkizApp{
    static ArrayList<Integer> list = new ArrayList<>();
    static ArrayList<String> aa = new ArrayList<>();
    static HashMap<Boolean, Integer> harita = new HashMap<Boolean, Integer>();
    static boolean[] boolArray = {true, false, true, true};
    static boolean[][] bool2Array = {{true, false, true, true}, {true, false, true, true}};
    static String[] strArray;
    static int sayi;
    static Map justMap;
    static Integer wrappedSayi;
//    private static IkizIdare i;
    
    public static void main(String[] args){
//        dtfunction();
//        return;
        //mainFunction();
        
        
        
        test();
        //reflectionSearching();
        //testReflectorForRunTime();
    }

//İŞLEM YÖNTEMLERİ:
    static void test(){
        IkizTest test;
//        test = IkizTest.startIkizTest(IkizTest.connectToDBForMySQL());
        test = IkizTest.startIkizTest(IkizTest.connectToDBForMsSQL());
//        test.showTablesFromInterface();// BAŞARILI
//        test.produceNewTableWithPrimaryKey();// BAŞARILI
//        test.produceTableWithListField();// BAŞARILI
//        test.produceTableWithArrayField();// BAŞARILI
//        test.produceTableWithMapField();// BAŞARILI
        
//        test.addDataForArrayField();// BAŞARILI
//        test.addDataForMapField();// BAŞARILI
//        test.addDataForCheckListField();
        // VERİ ÇEKME SENARYOLARI:
        //        test.getDataFromEmptyTable();// Boş tablodan veri çekme durumu
        
        
//        List<ArrayClass> list = test.getDataFromArrayClass();// BAŞARILI
        List<listClass> list = test.getDataFromListClass();
        list.forEach(System.out::println);
//        produceJSONText(test);
        // Hatâ durumlarındaki standartlamayı keşfetmek:
        //test.fetchTableWhichIsnotInDB();
        
    }
    static void produceJSONText(IkizTest test){
        Map<Object, Object> map = new HashMap<>();
        map.put("Görev - 1", "z....");
        map.put("kaynak", 11);
        map.put("os", "pathVar");
        test.testProduceJSONTextFromMap(map);
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