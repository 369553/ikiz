package ikiz;

public class HelperForMySQL implements HelperForDBType{

// İŞLEM YÖNTEMLERİ:
    @Override
    public String getSentenceForShowTables(){
        return "SHOW TABLES;";
    }
    @Override
    public String getConnectionString(String hostName, int portNumber){
        return "jdbc:mysql://" + hostName + ":" + portNumber;
    }
    @Override
    public String getConnectionString(String hostName, int portNumber, String dbName){
        return "jdbc:mysql://" + hostName + ":" + portNumber + "/" + dbName;
    }
}