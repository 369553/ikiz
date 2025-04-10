package ikiz;

import java.util.Map;
import java.util.List;

/**
 * Veritabanındaki benzer yapıdaki işlemlerinin daha kolay şekilde yapılması
 * için yazılmış veri erişim arayüzüdür.
 * @author Mehmet Akif SOLAK
 */
public interface IDBAccessHelper{
//SOYUT İŞLEM YÖNTEMLERİ:
    /**
     * Veritabanındaki tablo isimlerini getirir
     * @return Hedef veritabanının tablo isimleri veyâ {@code null}
     */
    public List<String> getTableNames();
    /**
     * Verilen isimdeki tablodaki tüm kayıtlar getirilir. Verileranahtar - değer
     * biçimindeki {@code Map} sınıfıyla temsil edilir
     * Anahtarlar sütun isimleri, değerler de ilgili sütun değerleridir
     * @param tableName Hedef tablonun ismi
     * @return Verilerden oluşan bir liste veyâ {@code null}
     */
    public List<Map<String, Object>> getData(String tableName);
    /**
     * Hedef tablonun, istenilen alanlarının verilerini döndürür
     * @param tableName Hedef tablo ismi
     * @param fieldNames İstenen alan isimleri, {@code null} ise tüm alanlar
     * @return Hedef tabloda istenen alan verileri
     */
    public List<Map<String, Object>> getData(String tableName, List<String> fieldNames);
    /**
     * Hedef tablodan bir eşitlik içeren 'WHERE' şartıyla veri çekmek içindir
     * @param tableName Hedef tablo ismi
     * @param fieldNames İstenen alan isimleri, {@code null} ise tüm alanlar
     * @param whereCondition 'WHERE' şartında belirtilmek istenen sütun ismi
     * @param answerOfWhereCondition 'WHERE' şartında belirtilen sütunun değeri
     * @return İstenen veri veyâ {@code null}
     */
    public List<Map<String, Object>> getDataForOneWhereCondition(String tableName, List<String> fieldNames, String whereCondition, Object answerOfWhereCondition);
    /**
     * Hedef tablodan birden fazla eşitlik içeren 'WHERE' şartılyla veri çekmek
     * için kullanılır, WHERE şartındakiler VE ('AND') bağlacıyla bağlanır
     * @param tableName Hedef tablo ismi
     * @param fieldNames İstenen alan isimleri, {@code null} ise tüm alanlar
     * @param whereConditions 'WHERE' şartındaki sütun isimleri
     * @param answerOfWhereConditions 'WHERE' şartında yerine koymak üzere,
     * {@code whereConditions} ile aynı sırada olması gereken sütun değerleri
     * @return İstenen veri veyâ {@code null}
     */
    public List<Map<String, Object>> getData(String tableName, List<String> fieldNames, String[] whereConditions, Object[] answerOfWhereConditions);
    /**
     * Hedef tablonun sütun isimlerini almak için kullanılır
     * @param tableName Hedef tablo
     * @return Sütun isimleri
     */
    public List<String> getFieldNames(String tableName);
    /**
     * Verilen tablodan, verilen bir eşitlikli 'WHERE' şartıyla veri silinir
     * @param tableName Hedef tablo ismi
     * @param whereCondition Eşitlik içeren 'WHERE' şartındaki sütun ismi
     * @param answerOfWhereCondition 'WHERE' şartındaki sütun değeri
     * @return İşlem başarılı ise {@code true}, değilse {@code false}
     */
    public boolean deleteRow(String tableName, String whereCondition, Object answerOfWhereCondition);
    /**
     * Birden fazla eşitlik içeren 'WHERE' şartıyla veri silmek için kullanılır
     * @param tableName Hedef tablo ismi
     * @param whereConditions VE('AND') bağlacıyla bağlananacak sütun isimleri
     * @param answerOfWhereConditions 'WHERE' şartında yerine koymak üzere,
     * {@code whereConditions} ile aynı sırada olması gereken sütun değerleri
     * @return İşlem başarılı ise {@code true}, değilse {@code false}
     */
    public boolean deleteRow(String tableName, String[] whereConditions, Object[] answerOfWhereConditions);
    /**
     * Veri tazelemek (güncellemek) için kullanılır
     * @param tableName Hedef tablo ismi
     * @param whereCondition 'WHERE' şartındaki sütun ismi (eşitlik kısıtı)
     * @param answerOfWhereCondition 'WHERE' şartında yerine konan sütun değeri
     * @param fieldsToValues Tazelenmek istenen veriler, anahtar olarak verilen
     * sütun ismindeki alanın değeri, o anahtarla ifâde edilen değer yapılır
     * @return İşlem başarılı ise {@code true}, değilse {@code false}
     */
    public boolean updateRow(String tableName, String whereCondition, Object answerOfWhereCondition, Map<String, Object> fieldsToValues);
    /**
     * Birden fazla eşitlik kısıtıyla veri tazelemek (güncellemek) içindir.
     * @param tableName Hedef tablo ismi
     * @param whereConditions 'AND' bağlacıyla bağlanan eşitlik kısıtlarındaki
     * sütun isimleri
     * @param answerOfWhereConditions 'WHERE' şartında yerine koymak üzere,
     * {@code whereConditions} ile aynı sırada olması gereken sütun değerleri
     * @param fieldsToValues Tazelenmek istenen veriler, anahtar olarak verilen
     * sütun ismindeki alanın değeri, o anahtarla ifâde edilen değer yapılır
     * @return İşlem başarılı ise {@code true}, değilse {@code false}
     */
    public boolean updateRow(String tableName, String[] whereConditions, Object[] answerOfWhereConditions, Map<String, Object> fieldsToValues);
    /**
     * Verilen isimdeki tablonun varlığını kontrol eder
     * @param tableName Tablo ismi
     * @return Tablo varsa {@code true}, yoksa {@code false} döndürülür
     */
    public boolean checkIsTableInDB(String tableName);
}