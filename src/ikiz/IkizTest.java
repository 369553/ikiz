package ikiz;

import ikiz.testClasses.User;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class IkizTest{
    private static IkizIdare idare;

    private IkizTest(IkizIdare idare){
        
    }

// İŞLEM (TEST) YÖNTEMLERİ:
    public static IkizTest startIkizTest(Cvity connection){
        if(!IkizIdare.startIkizIdare(connection)){
            System.err.println("Sistem başlatılamadı!");
            return null;
        }
        idare = IkizIdare.getIkizIdare();
        return new IkizTest(idare);
    }
    public void scenario1(){
        
    }
    public void scenario2(){// User sınıfı üzerinde...
        TableConfiguration confs = new TableConfiguration(User.class);
        // Kısıtlar:
            confs.setPrimaryKey("id");
            confs.addUniqueConstraint("name");
            confs.addNotNullConstraint("name");
        // Tablo oluştur:
            idare.produceTable(User.class, confs);
        // Verileri al:
            List<User> data = User.produceData();
        // Verileri ekle:
            data.forEach((element) -> {idare.addRowToDB(element);});
        // Verileri çek:
            List<User> fetcht = idare.getData(User.class);
        // Verileri yazdır:
//            fetcht.forEach((row) -> {System.out.println("name : " + row.name + "\tid : " + row.id + "\tgender : " + row.gender + "\tnotes[0] : " + (row.notes == null ? "null" : row.notes[0]));});
            fetcht.forEach(System.out::println);
        // Müşahhas bir veri üzerinde değişiklik yap:
            User choosed = fetcht.get(0);
            choosed.name = "Târık";
        // Değişikliği veritabanına gönder:
            idare.updateRowInDB(choosed);
        // Tazelenen veriyi çek ve yazdır:
            User refreshed = idare.getDataById(User.class, choosed.id);
            System.out.println("Tazelenen veri : " + refreshed);
        // Bu veriyi sil:
            int id = refreshed.id;
            idare.deleteRowFromDB(refreshed);
        // Veriyi sorgula ('null' gelmesi lazım)
            Object value = idare.getDataById(User.class, id);
            if(value == null)
                System.out.println("Silinen veri çağrıldığında 'null' döndü; sistem doğru çalışıyor");
            else
                System.err.println("Silinen veri sistemden silinememiş");
        // Başka bir veriyi sil:
            int otherRowId = fetcht.get(2).id;
            idare.deleteRowFromDB(fetcht.get(2));
        // Tüm verileri çekip, yazdırarak son verinin silindiğini ilk kez, diğer verinin silindiğini ikinci kez teyyit et:
            List<User> fetchtAgain = idare.getData(User.class);
            fetchtAgain.forEach((row) -> {System.out.println(((row.id == otherRowId || row.id == id) ? "Hatâ VAR!!:" : "") + row);});
    }
    public void scenario3(){
        
    }
    public void scenario4(){
        
    }
    public void scenario7(){
        // Sistemi veritabanı verileri analiz ederek başlat:
            System.out.println("Sistem yüklemesi : " + idare.loadSystemByAnalyzingDB());
//            boolean res = idare.loadSystemFromConfigurationFile(getConfigurationText());
//            System.out.println("res : " + res);
//            return;
        // Veritabanında vâr olan bir veriyi çek (User daha evvel oluşturulmuş olmalı):
            List<User> data = idare.getData(User.class);
        // Sonucu incele:
            System.out.println("Veri çekimi sonucu : " + (data == null ? "başarısız" : data.size()));
            data.forEach(System.out::println);
        // Vâr olan bir birincil anahtar ile müşahhas veri çekmeye çalış:
            User u = idare.getDataById(User.class, data.get(0).id);
            System.out.println("Müşahhas veri çekimi : " + (u == null ? "başarısız!" : u.name + "\tu.id : " + u.id));
    }
    public void scenario8(){// Sistemi vt'den yükle, yapılandırmayı dışa aktar
        idare.loadSystemByAnalyzingDB();
        idare.saveAllConfiguration("C:\\Şimdilik bunları kullan");
    }
    public void showTablesFromInterface(){
        List<String> listOfTables = IkizIdare.getTableNames(getIdare().getConnectivity());
        listOfTables.forEach(System.out::println);
    }
    public static Cvity connectToDBForMySQL(){
        Connection conToDB = Cvity.connectDB("root", "LINQSE.1177", "localhost", 3306, "ikizTestNew", Cvity.DBType.MYSQL);
        return new Cvity(conToDB, "root", "LINQSE.1177", "ikizTestNew");
    }
    public static Cvity connectToDBForMsSQL(){
        Connection cn = Cvity.connectDB("SA", "LINQSE.1177", "localhost", 1434, "vt1", Cvity.DBType.MSSQL);
        return new Cvity(cn, "SA", "LINQSE.1177", "vt1");
    }
    public static Cvity connectToDBForPostgreSQL(){
        Connection cn = Cvity.connectDB("postgres", "Kfe1858.", "localhost", 5432, "ikizTestNew", Cvity.DBType.POSTGRESQL);
        return new Cvity(cn, "postgres", "Kfe1858.", "ikizTestNew");
    }
    public static Cvity connectToDBForSQLite(){
        Connection cn = Cvity.connectDB(null, null, null, 0, "C:\\Users\\Yazılım alanı\\Documents\\SQLite vt\\vt1vt1.db", Cvity.DBType.SQLITE);
        return new Cvity(cn, null, null, "vt1");
    }
    public void testBLOB(){
        try{
            File file = new File("C:\\ProgramData\\MySQL\\MySQL Server 8.0\\resim.png");
//            if(file.exists())
//                System.out.println("Başarılı");
            FileInputStream fStream = new FileInputStream(file);
            String firstOrder = "CREATE TABLE veri(dosya MEDIUMBLOB);";
            Statement st = idare.getConnectivity().getConnext().createStatement();
            st.execute(firstOrder);
            String sqlOrder = "INSERT INTO veri(dosya) VALUES(?)";
            PreparedStatement stDosya = idare.getConnectivity().getConnext().prepareStatement(sqlOrder);
            stDosya.setObject(1, fStream);
            stDosya.execute();
        }
        catch(IOException | SQLException exc){
            System.err.println("exc : " + exc.toString());
        }
    }
    public String getConfigurationText(){
        return rwservice.RWService.getService().readDataAsText("C:\\'Notlar - çizimler' - taze\\İkiz", "ikizConf.json");
    }

// ERİŞİM YÖNTEMLERİ:
    public IkizIdare getIdare(){
        return idare;
    }
}