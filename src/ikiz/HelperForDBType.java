package ikiz;

public interface HelperForDBType{// Veritabanları açısından farklılık arz eden kodları tanımlamak için kullanılan arayüzdür
    public String getSentenceForShowTables();// Veritabanındaki tabloları göstermek için kod
    public String getConnectionString(String hostName, int portNumber);// Veritabanı sunucusuna bağlanmak için kod
    public String getConnectionString(String hostName, int portNumber, String dbName);// Veritabanı bağlanmak için kod
    // Aşağıdaki iki yöntem veritabanında işlemleri güvenle yapabilmek için, veritabanı alan isimlerinin veritabanı anahtar kelîmeleriyle karışmaması için kullanılıyor.
    public char getStartSymbolOfName();// Özel isim için başlangıç simgesi
    public char getEndSymbolOfName();// Özel isim için bitiş simgesi
}