package ikiz;

public interface HelperForDBType{// Veritabanları açısından farklılık arz eden kodları tanımlamak için kullanılan arayüzdür
    public String getSentenceForShowTables();// Veritabanındaki tabloları göstermek için kod
    public String getConnectionString(String hostName, int portNumber);// Veritabanı sunucusuna bağlanmak için kod
    public String getConnectionString(String hostName, int portNumber, String dbName);// Veritabanı bağlanmak için kod
}