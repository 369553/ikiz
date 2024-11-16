package ikiz;

public class HelperForPostgreSQL implements HelperForDBType{

// İŞLEM YÖNTEMLERİ:
    @Override
    public String getSentenceForShowTables(){
        return "...";
    }
    @Override
    public String getConnectionString(String hostName, int portNumber){
        return "jdbc:postgresql://" + hostName + ":" + portNumber;
    }
    @Override
    public String getConnectionString(String hostName, int portNumber, String dbName){
        return "jdbc:postgresql://" + hostName + ":" + portNumber + "/" + dbName;
    }
}