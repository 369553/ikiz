package ikiz;

import java.util.HashMap;

public interface HelperForDBType{// Veritabanları açısından farklılık arz eden kodları tanımlamak için kullanılan arayüzdür
    public String getSentenceForShowTables();// Veritabanındaki tabloları göstermek için kod
    public String getConnectionString(String hostName, int portNumber);// Veritabanı sunucusuna bağlanmak için kod
    public String getConnectionString(String hostName, int portNumber, String dbName);// Veritabanı bağlanmak için kod
    // Aşağıdaki iki yöntem veritabanında işlemleri güvenle yapabilmek için, veritabanı alan isimlerinin veritabanı anahtar kelîmeleriyle karışmaması için kullanılıyor.
    public char getStartSymbolOfName();// Özel isim için başlangıç simgesi
    public char getEndSymbolOfName();// Özel isim için bitiş simgesi
    public HashMap<String, String> getMapOfDataTypeToDBDataType();// Java veri tipinin veritabanında hangi veri tipine denk düştüğünü belirten harita. Yapısı : <javaVeriTipiİsmi, veritabanıVeriTipiİsmi>. Güvenlik açısından her seferinde yeni bir harita döndürmelidir
    public boolean isSupported(String dataTypeName);// Verilen veri tipi isminin veritabanı tarafından desteklenip, desteklenmediğini bildirir
    public String getDataTypeNameForJSON();// JSON verilerini tutabilmek için gerekli veri tipi ismi
}