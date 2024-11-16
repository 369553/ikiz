package ikiz;
//Sistem şu anda sadece MySQL ile çalışacak bi iznillâh..

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Cvity{
    private int portNumber;
    private String hostName;
    private String userName;
    private String password;
    private Connection connext;
    private String schemaName;
    private DBType dbType;
    public enum DBType{
        MYSQL,
        MSSQL,
        POSTGRESQL
    }
    public Cvity(Connection connext, String userName, String password, String schemaName){
        this.connext = connext;
        this.schemaName = schemaName;
        this.userName = userName;
        this.password = password;
        this.dbType = Cvity.detectDBType(connext);
        //BURALARIN SONRA DEĞİŞTİRİLMESİ LAZIM:
        /*
        hostName = "localhost";
        portNumber = 3306;
        */
    }

//İŞLEM YÖNTEMLERİ:
    //SINIF YÖNTEMLERİ (ÖN YÖNTEMLER):
    public static Connection connectBase(String userName, String password, String hostname, int portNumber, Cvity.DBType dbType){
        return Cvity.connectDB(userName, password, hostname, portNumber, "", dbType);
    }
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
        if(cn == null)
            return null;
        return cn;
    }
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
    public static String[] getTableNamesOnDB(Connection connection, Cvity.DBType type){
        if(connection == null)
            return null;
        try {
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
    public static Cvity.DBType detectDBType(Connection connext){
        try{
            if(connext.getMetaData().getDatabaseProductName().equals("MySQL"))
                return DBType.MYSQL;
            if(connext.getMetaData().getDatabaseProductName().equals("PostgreSQL"))// Teyyit edilecek
                return DBType.POSTGRESQL;
            if(connext.getMetaData().getDatabaseProductName().equals("Microsoft SQL Server")){// Teyyit edilecek
                return DBType.MSSQL;}
        }
        catch(SQLException exc){
            System.out.println("Veritabanı tipi tespit edilirken hatâ oluştu : " + exc.toString());
        }
        return null;
    }

    public HelperForDBType getHelperForDBType(){
        return HelperForHelperForDBType.getHelper(this.dbType);
    }

//ERİŞİM YÖNTEMLERİ:
    public int getPortNumber(){
        return portNumber;
    }
    public String getHostName(){
        return hostName;
    }
    public Connection getConnext(){
        return connext;
    }
    public String getSchemaName(){
        return schemaName;
    }
    public Cvity.DBType getDBType(){
        return dbType;
    }
}