package ikiz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Veritabanı bağlantı işlemlerinin ve veritabanına özgü ayarların kolay erişimi
 * için kullanışlı bir sınıf
 * @author Mehmet Akif SOLAK
 */
public class Cvity{
    private int portNumber;
    private String hostName;
    private String userName;
    private String password;
    private Connection connext;
    private String schemaName;
    private DBType dbType;
    /**
     * Veritabanı tipini belirten bir seçili değer listesi (@code enum)'dir.
     */
    public enum DBType{
        MYSQL,
        MSSQL,
        POSTGRESQL,
        SQLITE
    }
    /**
     * Sınıf örneği yapıcı fonksiyonu
     * @param connext Veritabanı bağlantı nesnesi
     * @param userName Kullanıcı adı
     * @param password Kullanıcı şifresi
     * @param schemaName 
     */
    public Cvity(Connection connext, String userName, String password, String schemaName){
        this.connext = connext;
        this.schemaName = schemaName;
        this.userName = userName;
        this.password = password;
        this.dbType = Cvity.detectDBType(connext);
    }

//İŞLEM YÖNTEMLERİ:
    //SINIF YÖNTEMLERİ (ÖN YÖNTEMLER):
    /**
     * Sunucu bazlı veritabanlarında veritabanı sunucusuna bağlantıyı oluşturur
     * @param userName Veritabanı kullanıcı adı
     * @param password İlgili kullanıcının şifresi
     * @param hostname Sunucu alan ismi
     * @param portNumber Veritabanı sunucusunun yayım yaptığı port numarası
     * @param dbType Veritabanı sunucu tipi
     * @return Oluşturulan bağlantı nesnesi veyâ {@code null}
     */
    public static Connection connectBase(String userName, String password, String hostname, int portNumber, Cvity.DBType dbType){
        return Cvity.connectDB(userName, password, hostname, portNumber, "", dbType);
    }
    /**
     * Verilen bilgilere göre hedef veritabanına bağlanır, sunucu bazlı olmayan
     * {@code Cvity.DBType.SQLITE} veritabanı için {@code dbName} parametresinin
     * hedef dosyanın adresini gösterecek şekilde verilmesi kâfîdir
     * @param userName Veritabanı kullanıcı adı
     * @param password İlgili kullanıcının şifresi
     * @param hostName Sunucu alan ismi
     * @param portNumber Veritabanı sunucusunun yayım yaptığı port numarası
     * @param dbName Veritabanı ismi
     * @param dbType Veritabanı sunucu tipi
     * @return Oluşturulan bağlantı nesnesi veyâ {@code null}
     */
    public static Connection connectDB(String userName, String password, String hostName, int portNumber, String dbName, Cvity.DBType dbType){
        Connection cn = null;
        if(dbType == null){
            System.err.println("Veritabanı tipi belirtilmemiş!");
            return null;
        }
        boolean connectToBase = false;
        try{
            String connectionString = "";
            if(dbName == null)
                connectToBase = true;
            else if(dbName.isEmpty())
                connectToBase = true;
            
            if(connectToBase){
                connectionString = HelperForHelperForDBType.getHelper(dbType).getConnectionString(hostName, portNumber);
            }
            else{
                connectionString = HelperForHelperForDBType.getHelper(dbType).getConnectionString(hostName, portNumber, dbName);
            }
            //System.out.println("Hâzırlanan bağlantı metni : " + connectionString);
            cn = DriverManager.getConnection(connectionString, userName, password);
        }
        catch(SQLException exc){
            //System.err.println("\'connectDB\' yöntemi çalıştırılırken hatâ : " +exc.toString());
            showErrorMessage(exc);
        }
        return cn;
    }
    /**
     * Verilen bağlantı kullanılarak verilen isimde bir veritabanı oluşturulur
     * @param connection Veritabanı bağlantısı
     * @param dbName Oluşturulmak istenen veritabanının ismi
     * @return İşlem başarılıysa {@code true}, değilse {@code false}
     */
    public static boolean createDB(Connection connection, String dbName){
        if(connection == null)
            return false;
        if(dbName == null)
            return false;
        if(dbName.isEmpty())
            return false;
        String order = "CREATE DATABASE IF NOT EXISTS " + dbName + ";";
        
        try{
            Statement query = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            query.execute(order);
            //VERİTABANININ OLUŞUP, OLUŞMADIĞINI KONTROL ET
            query.close();
            return true;
        }
        catch(SQLException ex){
            showErrorMessage(ex);
        }
        return false;
    }
    /**
     * Verilen bağlantı kullanılarak veritabanındaki tablo isimleri döndürülür
     * Eğer veritabanında hiçbir tablo yoksa bir adet boş metîn içeren 
     * {@code String[]} dizisi döndürülür, hatâ alınırsa {@code null} döndürülür
     * @param connection Veritabanı bağlantısı
     * @param type Veritabanı çeşidi
     * @return Veritabanı tablo isimleri, {@code null} veyâ tek elemanlı boş
     * metîn dizisi
     */
    public static String[] getTableNamesOnDB(Connection connection, Cvity.DBType type){
        if(connection == null)
            return null;
        try{
            Statement testStatement = connection.createStatement();// Şu kombinasyon SQL Server'da desteklenmiyormuş : Rconnext.getMetaData().getDatabaseProductName()
            ArrayList<String> liTableNames = new ArrayList<>();
            String order = HelperForHelperForDBType.getHelper(type).getSentenceForShowTables();
            testStatement.execute(order);
            ResultSet rs = testStatement.getResultSet();
            String[] tableNames;
            for(int sayac = 0; rs.next() == true; sayac++){
                liTableNames.add(rs.getString(1));
            }
            if(liTableNames.size() == 0)
                return new String[]{""};
            tableNames = new String[liTableNames.size()];
            liTableNames.toArray(tableNames);
            return tableNames;
        }
        catch(SQLException ex){
            System.out.println("hatâ  : " + ex.getMessage());
            return null;
        }
    }
    public static void showErrorMessage(SQLException DBException){//BURASI GELİŞTİRİLEBİLİR, HATALAR DAHA İYİ YÖNETİLEBİLİR Bİ İZNİLLÂH
        System.out.println("Hatâ kodu : " + DBException.getErrorCode() + "\n" + DBException.getMessage());
    }
    /**
     * Verilen bağlantı üzerinden veritabanı çeşidini {@code CVity.DBTYpe}
     * biçiminde döndürür
     * Desteklenmeyen veritabanı için {@code null} döndürülür
     * @param connext Veritabanı bağlantısı
     * @return {@code CVity.DBTYpe} nesnesi veyâ {@code null}
     */
    public static Cvity.DBType detectDBType(Connection connext){
        DBType type = null;
        try{
            type = HelperForHelperForDBType.getDBTypeByProductName(connext.getMetaData().getDatabaseProductName());
        }
        catch(SQLException exc){
            System.out.println("Veritabanı tipi tespit edilirken hatâ oluştu : " + exc.toString());
        }
        return type;
    }
    /**
     * Veritabanı işlemleri için kılavuz mâhiyetindeki veritabanı yardımcı
     * sınıfını döndürür
     * @return {@code HelperForDBType} nesnesi
     */
    public HelperForDBType getHelperForDBType(){
        return HelperForHelperForDBType.getHelper(this.dbType);
    }

//ERİŞİM YÖNTEMLERİ:
    /**
     * Sunucu bazlı veritabanlarında bağlanılan port numarasını verir
     * @return Sunucuda bağlanılan port numarası
     */
    public int getPortNumber(){
        return portNumber;
    }
    /**
     * Sunucu bazlı veritabanlarında bağlanılan sunucunun ismini verir
     * @return Bağlanılan sunucunun ismi
     */
    public String getHostName(){
        return hostName;
    }
    /**
     * Bağlantının kendisini ifâde eder. İşlemler bu bağlantı üzerinden yapılır
     * @return Veritabanı bağlantısı
     */
    public /*protected yap*/ Connection getConnext(){
        return connext;
    }
    /**
     * Bağlanılan veritabanı tablo ismini verir
     * @return Tablo ismi
     */
    public String getSchemaName(){
        return schemaName;
    }
    /**
     * Veritabanının çeşidini simgeleyen {@code Cvity.DBTYpe} değeri verilir
     * @return Bağlanılan veritabanı çeşidi
     */
    public Cvity.DBType getDBType(){
        return dbType;
    }
}