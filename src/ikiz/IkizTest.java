package ikiz;

import base.testSinifi;
import java.sql.Connection;
import java.util.List;

public class IkizTest{
    private static IkizIdare idare;

    private IkizTest(IkizIdare idare){
        
    }

// İŞLEM (TEST) YÖNTEMLERİ:
    public void createNewDB(){// Yeni veritabanı oluştur:
        Connection connection = Cvity.connectBase("root", "LINQSE.1177", "localhost", 3306, Cvity.DBType.MYSQL);
        
        boolean isSuccessful = Cvity.createDB(connection, "ikizTest");
        if(!isSuccessful){
            System.err.println("Veritabanı oluşturulamadı!");
            return;
        }
    }
    public static Cvity connectToDBForMySQL(){
        Connection conToDB = Cvity.connectDB("root", "LINQSE.1177", "localhost", 3306, "ikizTest", Cvity.DBType.MYSQL);
        return new Cvity(conToDB, "root", "LINQSE.1177", "ikizTest");
    }
    public static Cvity connectToDBForMsSQL(){
        Connection con = Cvity.connectDB("SA", "LINQSE.1177", "localhost", 1434, "vt1", Cvity.DBType.MSSQL);
        if(con != null)
            System.out.println("Veritabanı temeline bağlanıldı");
        return new Cvity(con, "SA", "LINQSE.1177", "vt1");
    }
    public static IkizTest startIkizTest(Cvity connection){
        if(!IkizIdare.startIkizIdare(connection)){
            System.err.println("Sistem başlatılamadı!");
            return null;
        }
        idare = IkizIdare.getIkizIdare();
        return new IkizTest(idare);
    }
    public void produceNewTable(){
        boolean isSuccessful = getIdare().produceTable(testSinifi.class);
        if(!isSuccessful){
            System.err.println("Tablo oluşturulamadı!");
            return;
        }
    }
    public void produceNewTableWithPrimaryKey(){
        TableConfiguration confs = new TableConfiguration(testSinifi.class);
        if(!confs.setPrimaryKey("numara"))
            System.err.println("İlgili alan birincil anahtar olarak atanamadı!");
        boolean isSuccessful = getIdare().produceTable(testSinifi.class, confs);
        if(!isSuccessful){
            System.err.println("Tablo oluşturulamadı!");
            return;
        }
    }
    public void fetchData(){
        List<testSinifi> data = getIdare().getData(testSinifi.class);
        if(data != null){
            System.out.println("data.size : " + data.size());
            data.forEach((element) -> {System.out.println("element.name : " + element.name);});
        }
    }
    public void showTablesFromInterface(){
        List<String> listOfTables = IkizIdare.getTableNames(getIdare().getConnectivity());
        listOfTables.forEach(System.out::println);
    }

// ERİŞİM YÖNTEMLERİ:
    public IkizIdare getIdare(){
        return idare;
    }
}