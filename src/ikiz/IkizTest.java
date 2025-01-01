package ikiz;

import base.ArrayClass;
import base.MapClass;
import base.OtherArrayClass;
import base.listClass;
import base.testSinifi;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
    public void produceTableWithListField(){
        boolean isSuccess = getIdare().produceTable(listClass.class);
        if(isSuccess)
            System.out.println("Liste alanlı sınıf oluşturuldu");
        else
            System.err.println("Liste alanlı sınıf oluşturulamadı");
    }
    public void produceTableWithArrayField(){
        boolean isSuccess = getIdare().produceTable(ArrayClass.class);
        if(isSuccess)
            System.out.println("Dizi alanlı sınıf oluşturuldu");
        else
            System.err.println("Dizi alanlı sınıf oluşturulamadı");
    }
    public void produceTableWithOtherArrayField(){
        boolean isSuccess = getIdare().produceTable(OtherArrayClass.class);
        if(isSuccess)
            System.out.println("Diğer dizi alanlı sınıf oluşturuldu");
        else
            System.err.println("Diğer dizi alanlı sınıf oluşturulamadı");
    }
    public void produceTableWithMapField(){
        boolean isSuccess = getIdare().produceTable(MapClass.class);
        if(isSuccess)
            System.out.println("Harita alanlı sınıf oluşturuldu");
        else
            System.err.println("Harita alanlı sınıf oluşturulamadı");
    }
    public void addDataForCheckListField(){
        boolean isSuccess = getIdare().addRowToDB(new listClass());
        if(isSuccess)
            System.out.println("Liste alanlı tablo için ekleme başarılı");
        else
            System.err.println("Liste alanlı tablo için ekleme başarısız");
    }
    public void addDataForArrayField(){
        boolean isSuccess = getIdare().addRowToDB(new ArrayClass());
        if(isSuccess)
            System.out.println("Dizi alanlı tablo için ekleme başarılı");
        else
            System.err.println("Dizi alanlı tablo için ekleme başarısız");
    }
    public void addDataForOtherArrayField(){
        boolean isSuccess = getIdare().addRowToDB(new OtherArrayClass());
        if(isSuccess)
            System.out.println("Dizi alanlı diğer tablo için ekleme başarılı");
        else
            System.err.println("Dizi alanlı diğer tablo için ekleme başarısız");
    }
    public void addDataForMapField(){
        boolean isSuccess = getIdare().addRowToDB(new MapClass());
        if(isSuccess)
            System.out.println("Harita alanlı tablo için ekleme başarılı");
        else
            System.err.println("Harita alanlı tablo için ekleme başarısız");
    }
    public void fetchData(){
        List<testSinifi> data = getIdare().getData(testSinifi.class);
        if(data != null){
            System.out.println("data.size : " + data.size());
            data.forEach((element) -> {System.out.println("element.name : " + element.name);});
        }
    }
    public void fetchTableWhichIsnotInDB(){
        String sql = "SELECT * FROM olmayanTablo";
        try{
            getIdare().getConnectivity().getConnext().createStatement().executeQuery(sql);
        }
        catch(SQLException exc){
            System.err.println("Hatâ (fetchTableWhichIsnotInDB) : " + exc.toString());
            System.err.println("Hatâ kodu : " + exc.getErrorCode());
        }
    }
    public void showTablesFromInterface(){
        List<String> listOfTables = IkizIdare.getTableNames(getIdare().getConnectivity());
        listOfTables.forEach(System.out::println);
    }
    public void getDataFromEmptyTable(){// İki satır için de denemek için kapatılan yeri aç
        createEmptyTable("emptyTable");
        String sql = "SELECT * FROM emptyTable"; 
        try{
            getIdare().getConnectivity().getConnext().createStatement().execute("INSERT INTO emptyTable (id) VALUES (1);");
            ResultSet rs = getIdare().getConnectivity().getConnext().createStatement().executeQuery(sql);
            if(rs == null)
                System.err.println("Gelen resultSet nesnesi = null");
            else
                System.out.println("Gelen resultSet nesnesi != null");
            if(rs.next())
                System.out.println("resultSet.next() = true");
            else
                System.err.println("resulSet.next() = false");
            
            
            if(rs.next())
                System.out.println("İkinci resultSet.next() komutu = true");
            else
                System.err.println("İkinci resulSet.next() komutu = false");
        }
        catch(SQLException exc){
            System.err.println("Hatâ (getDataFromEmptyTable) : " + exc.toString());
            System.err.println("Hatâ kodu : " + exc.getErrorCode());
        }
    }
    public List getDataFromArrayClass(){
        List<ArrayClass> list = getIdare().getData(ArrayClass.class);
        if(list != null)
            System.out.println("Veri çekme işlemi başarılı : " + list.size());
        else
            System.err.println("Veri çekme işlemi başarısız!");
        return list;
    }
    public List getDataFromListClass(){
        List<listClass> list = getIdare().getData(listClass.class);
        if(list != null)
            System.out.println("Veri çekme işlemi başarılı : " + list.size());
        else
            System.err.println("Veri çekme işlemi başarısız!");
        return list;
    }
    public void createEmptyTable(String tableName){
        String order = "CREATE TABLE " + tableName + "(id int);";
        String order2 = "TRUNCATE TABLE " + tableName + ";";
        try{
            getIdare().getConnectivity().getConnext().createStatement().execute(order);
            getIdare().getConnectivity().getConnext().createStatement().execute(order2);
        }
        catch(SQLException exc){
            System.err.println("Hatâ (createEmptyTable) : " + exc.toString());
            System.err.println("Hatâ kodu : " + exc.getErrorCode());
        }
    }
    public void testProduceJSONTextFromMap(Map<Object, Object> map){
        String text = idare.getJSONStringFromObject(map);
        if(text != null){
            System.err.println("text != null");
            if(!text.isEmpty()){
                System.err.println("!text.isEmpty");
                System.out.println("text:\n\n" + text);
            }
        }
    }

// ERİŞİM YÖNTEMLERİ:
    public IkizIdare getIdare(){
        return idare;
    }
}