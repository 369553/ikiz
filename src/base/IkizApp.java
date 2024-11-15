package base;

import ikiz.Cvity;
import ikiz.IkizIdare;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class IkizApp{
    private static IkizIdare i;
    
    public static void main(String[] args){
//        dtfunction();
//        return;
        //mainFunction();
        testSystemWithtestSinifiClass();
    }

//İŞLEM YÖNTEMLERİ:
    static void testSystemWithtestSinifiClass(){
        // Yeni veritabanı oluştur:
        /*
        Connection connection = Cvity.connectBase("root", "LINQSE.1177", "localhost", 3306);
        
        boolean isSuccessful = Cvity.createDB(connection, "ikizTest2");
        if(!isSuccessful){
            System.err.println("Veritabanı oluşturulamadı!");
            return;
        }
        */
        Connection conToDB = Cvity.connectDB("root", "LINQSE.1177", "localhost", "ikizTest", 3306);
        Cvity cVity = new Cvity(conToDB, "root", "LINQSE.1177", "ikizTest");
        //Cvity.getTableNamesOnDB(cVity.getConnext());
        if(!IkizIdare.startIkizIdare(cVity)){
            System.err.println("Sistem başlatılamadı!");
            return;
        }
        i = IkizIdare.getIkizIdare();
        //IkizIdare.getIkizIdare().produceTable(new testSinifi());
        //IkizIdare.getIkizIdare().addRowToDB(new testSinifi());
        //testSinifi entity = new testSinifi("Bu yeni bir eleman");
        //entity.setAge(17);
        //i.addRowToDB(entity);
        
        //i.test("testSinifi");
        
        //i.setNullToCol();
        List<testSinifi> data = i.getData(testSinifi.class);
        if(data != null){
            System.out.println("data.size : " + data.size());
            data.forEach((element) -> {System.out.println("element.name : " + element.name);});
        }
    }
    static void test(){
        System.out.println("");
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