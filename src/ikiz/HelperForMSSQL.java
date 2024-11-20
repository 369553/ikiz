package ikiz;

public class HelperForMSSQL implements HelperForDBType{

// İŞLEM YÖNTEMLERİ:
    @Override
    public String getSentenceForShowTables(){
        return "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES;";
    }
    @Override
    public String getConnectionString(String hostName, int portNumber){
        return "jdbc:sqlserver://" + hostName + ":" + portNumber + ";trustServerCertificate=true;";
    }
    @Override
    public String getConnectionString(String hostName, int portNumber, String dbName){
        return "jdbc:sqlserver://" + hostName + ":" + portNumber + ";trustServerCertificate=true;" + "databaseName=" + dbName + ";";
        //String url = "jdbc:sqlserver://localhost:1434;user=SA;password=LINQSE.1177;trustServerCertificate=true;";
//        jdbc:sqlserver://localhost:1434;trustServerCertificate=true;
    }
    @Override
    public char getStartSymbolOfName(){
        return '[';
    }
    @Override
    public char getEndSymbolOfName(){
        return ']';
    }
}