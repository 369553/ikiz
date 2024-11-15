package ikiz;
//Sistem şu anda sadece MySQL ile çalışacak bi iznillâh..

import ikiz.Services.ArrayPrinterService;
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

    public Cvity(Connection connext, String userName, String password, String schemaName){
        this.connext = connext;
        this.schemaName = schemaName;
        this.userName = userName;
        this.password = password;
        //BURALARIN SONRA DEĞİŞTİRİLMESİ LAZIM:
        hostName = "localhost";
        portNumber = 3306;
    }

//İŞLEM YÖNTEMLERİ:
    //SINIF YÖNTEMLERİ (ÖN YÖNTEMLER):
    public static Connection connectBase(String userName, String password, String hostname, int portNumber){
        return Cvity.connectDB(userName, password, hostname, "", portNumber);
    }
    public static Connection connectDB(String userName, String password, String hostName, String dbName, int portNumber){
        Connection cn = null;
        try{
            if(dbName != null)
                if(!dbName.isEmpty())
                cn = DriverManager.getConnection("jdbc:mysql://" + hostName + ":" + portNumber + "/" + dbName, userName, password);
            else
                cn = DriverManager.getConnection("jdbc:mysql://" + hostName + ":" + portNumber, userName, password);
        }
        catch(SQLException ex){
            showErrorMessage(ex);
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
    public static String[] getTableNamesOnDB(Connection connection){
        if(connection == null)
            return null;
        try {
            Statement testStatement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ArrayList<String> liTableNames = new ArrayList<>();
            testStatement.execute("SHOW TABLES");
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
}