package ikiz;

import java.util.HashMap;

/**
 * Farklı çeşit veritabanlarını desteklemek için her veritabanının kendine özgü
 * özelliklerinin hangilerinin İkiz için gerekli olduğunu belirten arayüzdür
 * @author Mehmet Akif SOLAK
 */
public interface HelperForDBType{// Veritabanları açısından farklılık arz eden kodları tanımlamak için kullanılan arayüzdür
    /**
     * Veritabanındaki tabloların isimlerini gösteren komutu döndürür
     * @return İlgili SQL komutu
     */
    public String getSentenceForShowTables();// Veritabanındaki tabloları göstermek için kod
    /**
     * Verilen alan ismi ve port numarasına göre bağlantı metnini döndürür
     * Bu bağlantı metni veritabanı sunucusunun temeline bağlanmak içindir
     * {@code Cvity.DBType.SQLITE} gibi sunucusuz veritabanı için değildir
     * @param hostName Alan ismi
     * @param portNumber Veritabanı sunucusunun yayında olduğu port numarası
     * @return Veritabanı sunucusu temeline bağlanmak için bağlantı metni
     */
    public String getConnectionString(String hostName, int portNumber);// Veritabanı sunucusuna bağlanmak için kod
    /**
     * Verilen alan ismi ve port numarasına göre bağlantı metnini döndürür
     * Bu bağlantı verilen veritabanına bağlanmak içindir
     * {@code Cvity.DBType.SQLITE} gibi sunucusuz veritabanları yalnızca
     * {@code dbName} parametresini vermeli ve bu, hedef dosyayı göstermeli
     * @param hostName Alan ismi
     * @param portNumber Veritabanı sunucusunun yayında olduğu port numarası
     * @param dbName Sunucuda yer alan ve bağlanılmak istenen veritabanının ismi
     * @return Veritabanına bağlanmak için bağlantı metni
     */
    public String getConnectionString(String hostName, int portNumber, String dbName);// Veritabanı bağlanmak için kod
    /**
     * Seçilen tablo veyâ alan isminin anahtar kelîmesi olması ihtimaline karşı
     * bâzı veritabanlarında özel isim başlangıç - bitiş karakterleri vardır
     * Bağlanılan veritabanında böyle bir başlangıç karakteri varsa onu döndürür
     * Olmayan veritabanları {@code null} döndürmelidir.
     * @return Özel isim başlangıç karakteri veyâ {@code null}
     */
    public Character getStartSymbolOfName();
    /**
     * Seçilen tablo veyâ alan isminin anahtar kelîmesi olması ihtimaline karşı
     * bâzı veritabanlarında özel isim başlangıç - bitiş karakterleri vardır
     * Bağlanılan veritabanında böyle bir bitiş karakteri varsa onu döndürür
     * Olmayan veritabanları {@code null} döndürmelidir.
     * @return Özel isim bitiş karakteri veyâ {@code null}
     */
    public Character getEndSymbolOfName();// Özel isim için bitiş simgesi
    /** Java veri tipinin hangi veritabanı tipine denk düştüğünü belirten harita
     * Yapısı, "javaVeriTipiIsmi , veritabaniVeriTipiIsmi" biçimindedir
     * Güvenlik açısından her seferinde yeni bir harita nesnesi döndürülür
     * @return Java - veritabanı veri tipi eşleşme haritası
     */
    public HashMap<String, String> getMapOfDataTypeToDBDataType();
    /**
     * Verilen Java veri tipi için veritabanı desteği olup, olmadığını bildirir
     * @param dataTypeName Java veri tipi ismi
     * @return Destek sağlanıyorsa {@code true}, aksi hâlde {@code false}
     */
    public boolean isSupported(String dataTypeName);
    /**
     * Veritabanında JSON verisi için kullanılan veri tipi ismini döndürür
     * @return JSON verisi için kullanılan veritabanı veri tipi ismi
     */
    public String getDataTypeNameForJSON();
    /**
     * Veritabanının JDBC üzerindeki ürün ismini döndürür
     * @return Veritabanı bağlantı arayüzü tarafından döndürülen isim
     */
    public String getDatabaseProductName();
    /**
     * Veritabanında {@code java.lang.Enum} tipi için uygun veri tipini döndürür
     * Veritabanı desteği varsa karşılığı olan veri tipi ismi, yoksa en uygunu..
     * @return Enum verisi için en uygun veritabanı veri tipi ismi
     */
    public String getDataTypeNameForEnum();
    /**
     * Verilen veritabanı veri tipi isminin varsayılan metîn tipi olup,
     * olmadığı bilgisini döndürür
     * Bu, veritabanları arasında farklılık gösterebilir
     * {@code CVity.DBType.MSSQL} ve {@code CVity.DBType.MYSQL} için "VARCHAR"dır
     * @param sqlTypeName Veritabanı veri tipi ismi
     * @return Girdi, varsayılan metîn veri tipi ismi ise {@code true}, değilse {@code false}
     */
    public boolean isDefaultStringDataType(String sqlTypeName);
    /**
     * Verilen SQL veri tipinin Java'daki karşılığı olan sınıfı döndürür
     * Temel veri tiplerinin kendi sınıfını değil, sarmalayıcı sınıfını döndürür
     * @param sqlTypeName Veritabanındaki veri tipi ismi (SQL veri tipi ismi)
     * @return SQL tipinin karşılığı olan Java sınıfı veyâ {@code null}
     */
    public Class<?> getMatchedClassForGivenSqlType(String sqlTypeName);
    /**
     * Sütun tanımının sonunda kullanılmak üzere, alanın otomatik artan olması
     * için yazılması gereken anahtar kelîmeyi döndürür
     * @return 'Otomatik artan' özelliğinin anahtar kelîmesi
     */
    public String getAutoIncrementKeyword();
    /**
     * Bu fonksiyon bir oturum içerisinde en son eklenen verinin otomatik artan
     * alanının değerini öğrenmek için çalıştırılması gereken sorguyu döndürür
     * @return Son eklenen otomatik artan alan değeri için SQL sorgu metni
     */
    public String getQueryForLastInsertedID();
}