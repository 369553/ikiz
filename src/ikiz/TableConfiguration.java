package ikiz;

import ReflectorRuntime.Reflector;
import ikiz.Services.Helper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;
/**
 * Sınıftan tablo oluştururken gerekli ayarları belirtmek ve tutmak içindir
 * Bu ayarlar tablo oluşturulurken kullanılır
 * Bu ayarların hiçbiri tablo oluşturulduktan sonra değiştirilemez
 * Tablo oluşturulurken verilen ayarın sonra değiştirilmesi hatâya yol açabilir
 * @author Mehmet Akif SOLAK
 */

public class TableConfiguration{// ALAN İSİMLERİ DEĞİŞTİRİLEMEZ!
    private Class cls;
    private boolean isTableCreated = false;// Tablonun oluşturulup, oluşturulmadığı bilgisi
    private HashMap<String, Boolean> isConfSet;// <özellik, değerAtandıMı> : Bir özellik için değer atandıysa o özellik için 'true' olmalıdır
    private TableCharset.CHARSET charsetAsAll;// Tablodaki metîn tabanlı alanlar için genel karakter seti
    private ArrayList<String> uniqueFields;// 'unique' olması istenen alanlar buraya yazılır
    private ArrayList<String> indexes;// 'Birincil anahtar hâricinde ve 'unique' alanlar hâricinde 'indeks' eklenmesi istenen alanlar buraya yazılır
    private String primaryKey = null;// Birincil anahtarın uygulanacağı sütun ismi
    private ArrayList<String> notNulls;// 'Boş olmama' kısıtı eklenen alanlar
    // varchar alanlar için sınır belirleme için alan eklenecek
//    private HashMap<String, TableCharset.CHARSET> charsets;// Karakter seti belirtilmiş alanlar
    private SortedSet<String> fieldNames;// Verilen sınıftaki alanların isimleri
    private HashMap<String, Object> valuesAsDefault;// Sütun için varsayılan değer atandığı durumda..
    private HashMap<String, Integer> specifiedLengths;// Metîn tipindeki verilerde, metîn uzunluğu için özel bir değer belirtildiyse, onu <sütunİsmi, değer> biçiminde tutar.
    private int lengthForStringAsDefault = 500;// Metîn tipindeki veriler için varsayılan metîn uzunluğu
    private boolean isDefaultLengthOfStringChanged = false;

    /**
     * {@code TableConfiguration} örneği oluşturmak için kurucu fonksiyon
     * @param classOfTable Tablosu oluşturulmak istenen sınıf
     * @throws NullPointerException {@code null} sınıf verilirse fırlatılır
     */
    public TableConfiguration(Class classOfTable) throws NullPointerException{// cls : Yapılandırma yapılması istenen sınıf
        if(classOfTable == null)
            throw new NullPointerException("Verilen sınıf null!");
        this.cls = classOfTable;
        assignFieldNames();// Sınıfın alan isimlerini al; bu, olmayan alan için yapılandırma eklenememesi içindir
    }

// * Veritabanı indeksleri için isim atanması bu sürümde desteklenmiyor
// İŞLEM YÖNTEMLERİ:
    /**
     * {@code IkizIdare} tarafından ayarların içe aktarılması için kullanılır
     * @param data Veriler, ilgili alan ismiyle aynı isimde olmalıdır
     * @return İşlem başarılıysa {@code true}, aksi hâlde {@code false}
     */
    protected boolean importConfigurations(Map<String, Object> data){
        if(data == null)
            return false;
        Object isConfSetFrom = data.get("isConfSet");
        Object uniqueFieldsFrom = data.get("uniqueFields");
        Object indexesFrom = data.get("indexes");
        Object primaryKeyFrom = data.get("primaryKey");
        Object notNullsFrom = data.get("notNulls");
        Object valuesAsDefaultFrom = data.get("valuesAsDefault");
        Object specifiedLengthsFrom = data.get("specifiedLengths");
        Object lengthForStringAsDefaultFrom = data.get("lengthForStringAsDefault");
        Object isDefaultLengthOfStringChangedFrom = data.get("isDefaultLengthOfStringChanged");
        Object isTableCreatedFrom = data.get("isTableCreated");
        Object fieldNamesFrom = data.get("fieldsNames");
        try{
            this.isConfSet = (isConfSetFrom != null ? (HashMap<String, Boolean>) isConfSetFrom : this.isConfSet);
            this.uniqueFields = (uniqueFieldsFrom != null ? (ArrayList<String>) uniqueFieldsFrom : this.uniqueFields);
            // charsetAsAll eklenecek
            this.indexes = (indexesFrom != null ? (ArrayList<String>) indexesFrom : this.indexes);
            this.notNulls = (notNullsFrom != null ? (ArrayList<String>) notNullsFrom : this.notNulls);
            
            this.primaryKey = (primaryKeyFrom != null ? (String) primaryKeyFrom : this.primaryKey);
            
            this.valuesAsDefault = (valuesAsDefaultFrom != null ? (HashMap<String, Object>) valuesAsDefaultFrom : this.valuesAsDefault);
            this.specifiedLengths = (specifiedLengthsFrom != null ? (HashMap<String, Integer>) specifiedLengthsFrom : this.specifiedLengths);
            this.isConfSet = (isConfSetFrom != null ? (HashMap<String, Boolean>) isConfSetFrom : this.isConfSet);
            
            this.lengthForStringAsDefault = (lengthForStringAsDefaultFrom != null ? (Integer) lengthForStringAsDefaultFrom : this.lengthForStringAsDefault);
            this.isDefaultLengthOfStringChanged = (isDefaultLengthOfStringChangedFrom != null ? (Boolean) isDefaultLengthOfStringChangedFrom : this.isDefaultLengthOfStringChanged);
            this.isTableCreated = (isTableCreatedFrom != null ? (boolean) isTableCreatedFrom : false);
            this.fieldNames = new TreeSet<String>();
            if(fieldNamesFrom != null){// Aday sütunları içe aktar
               this.fieldNames = new TreeSet<String>();
               this.fieldNames.addAll((List<String>) fieldNamesFrom);
            }
            return true;
        }
        catch(ClassCastException exc){
            System.err.println("TableConfiguration içe aktarma işlemi başarısız!");
            return false;
        }
    }
    /**
     * Tablo yapılandırmasını dış dosyaya aktarabilmek için bir haritada toplar
     * @return Tablo yapılandırma haritası, ("özellik ismi -> yapılandırma")
     */
    protected Map<String, Object> exportConfigurations(){
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("isConfSet", getIsConfSet());
        // charsetAsAll eklenecek
        root.put("uniqueFields", getUniqueFields());
        // indexes eklenecek
        root.put("primaryKey", getPrimaryKey());
        root.put("notNulls", getNotNulls());
        ArrayList<String> fieldNamesAsList = new ArrayList<String>();
        fieldNamesAsList.addAll(getFieldNames());
        root.put("fieldNames", fieldNamesAsList);
        root.put("valuesAsDefault", getDefaultValues());
        root.put("specifiedLengths", getSpecifiedLengths());
        root.put("lengthForStringAsDefault", lengthForStringAsDefault);
        root.put("isDefaultLengthOfStringChanged", getIsDefaultLengthOfStringChanged());
        root.put("isTableCreated", isTableCreated);
        return root;
    }
    /**
     * Bu yöntem {@code IkizIdare} tarafından tablo varlığını bildirmek içindir
     * Tablo yapılandırması tablo oluşturulduktan sonra değiştirilememektedir
     * Bu, güvenlik zafiyyetinin önüne geçmek için tasarlanmış bir kilittir
     * @param idare İkiz idârecisi
     * @param isCreated Tablonun oluşturulup, oluşturulmadığı bilgisi
     */
    protected void setIsTableCreated(IkizIdare idare, boolean isCreated){
        if(idare != null){
            if(isCreated == idare.isInIkiz(cls))
                this.isTableCreated = isCreated;
        }
    }
    /**
     * Oluşturulmak istenen tablonun birincil anahtarı olacak sütunu belirtin
     * {@code null} verilirse mevcut birincil anahtar iptal edilir
     * Bu ayar tablo oluşturulmadan evvel belirtilebilir
     * Verilen sütun isminin belirttiği sütunun birincil anahtar olabilirliği
     * kullanıcının sorumluluğundadır
     * Burada, bu kısıt veyâ bunun veritabanında çalışabilirliği denetlenmez
     * @param primaryKey Birincil anahtar sütun ismi
     */
    public void setPrimaryKey(String primaryKey){
        if(isTableCreated)
            return;
        boolean set = false;
        if(primaryKey != null){
            if(isInFields(primaryKey)){
                this.primaryKey = primaryKey;
                set = true;
            }
        }
        getIsConfSet().put("primaryKey", set);
    }
    /**
     * Münferid ('UNIQUE', tekil) veri barındırma kısıtı eklemek için kullanılır
     * Burada, bu kısıt veyâ bunun veritabanında çalışabilirliği denetlenmez
     * 'UNIQUE' kısıtının çalışma biçimi veritabanlarında farklılık gösterebilir
     * Bu, MySQL'de 1+ satırın 'NULL' olmasına engel değilken MsSQL'de engeldir
     * @param fieldName Sütun ismi
     * @return İşlem başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean addUniqueConstraint(String fieldName){
        if(isTableCreated)
            return false;
        if(fieldName == null)
            return false;
        if(!isInFields(fieldName))
            return false;
        if(Helper.isInTheList(getUniqueFields(), fieldName))// İlgili alan için zâten 'münferid' özelliği atandıysa işlemi sonlandır
            return false;
        if(getIsConfSet().get("primaryKey")){// Birincil anahtar atandıysa
            if(getPrimaryKey().equals(fieldName))// ve bu alan için münferid olma özelliği eklenmeye çalışılıyorsa işlemi sonlandır
                return false;
        }
        getUniqueFields().add(fieldName);
        getIsConfSet().put("uniqueFields", Boolean.TRUE);
        return true;
    }
    /**
     * Tabloya bir veritabanı indeksi eklemek için kullanılır
     * Burada, bu kısıt veyâ bunun veritabanında çalışabilirliği denetlenmez
     * Bu sürümde indeksler için isim ataması yapılmıyor
     * @param fieldName Üzerinde indeks oluşturulmak istenen sütunun ismi
     * @return İşlem başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean addIndex(String fieldName){
        if(isTableCreated)
            return false;
        if(fieldName == null)
            return false;
        if(!isInFields(primaryKey))
            return false;
        if(getIsConfSet().get("primaryKey")){// Eğer birincil anahtar belirtilmişse
            if(primaryKey.equals(fieldName))// Birincil anahtar olan alana yeni indeks eklenmeye çalışılıyorsa işlemi sonlandır
                return false;
        }
        if(Helper.isInTheList(getIndexes(), fieldName))// Eğer ilgili alan için daha evvel bir indeks zâten varsa işlemi sonlandır ; bu işlemin performansı arttırılacak
            return false;
        getIndexes().add(fieldName);
        getIsConfSet().put("indexes", Boolean.TRUE);
        return true;
    }
    /**
     * Bir sütuna boş veri barındırmama ('NOT NULL') kısıtı eklemek içindir
     * Burada, bu kısıt veyâ bunun veritabanında çalışabilirliği denetlenmez
     * @param fieldName Sütun ismi
     * @return İşlem başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean addNotNullConstraint(String fieldName){
        if(isTableCreated)
            return false;
        if(fieldName == null)
            return false;
        if(!isInFields(fieldName))
            return false;
        if(Helper.isInTheList(getNotNulls(), fieldName))// Bu isimde bir alan yoksa;
            return false;
        getNotNulls().add(fieldName);
        getIsConfSet().put("notNulls", Boolean.TRUE);
        return true;
    }
    /**
     * Bir sütuna varsayılan değer eklemek için kullanılır
     * Burada, bu kısıt veyâ bunun veritabanında çalışabilirliği denetlenmez
     * İlâveten, verilen değerin ilgili veritabanı veri tipine uygunluğu da
     * denetlenmez(bu sürümde); bunun sebebi, veritabanında JSON, ENUM gibi veri
     * tiplerinin de metînsel veri olarak ifâde edilmesinden gibi durumların
     * kontrolünün -bilhassa veritabanı oluşmadan- zor olmasıdır
     * @param fieldName Hedef sütunun ismi
     * @param value Hedef sütun için varsayılan değeri ifâde eden nesne
     * @return İşlem başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean addDefaultValue(String fieldName, Object value){
        if(isTableCreated)
            return false;
        if(fieldName == null)
            return false;
        if(value == null)
            return false;
        if(!isInFields(fieldName))
            return false;
        if(checkDataType(fieldName, value)){
            getIsConfSet().put("defaultValues", Boolean.TRUE);
            getDefaultValues().put(fieldName, value);
            return true;
        }
        return false;
    }
//    /**
//     * Tablodaki tüm metînsel alanların varsayılan karakter setini belirtin
//     * Eğer sütun için müşahhas bir karakter seti belirtildiyse, o geçerlidir
//     * @param charset Karakter seti
//     * @return İşlem başarılıysa {@code true}, aksi hâlde {@code false}
//     */
//    public boolean setCharsetAsAll(TableCharset.CHARSET charset){
//        if(isTableCreatedFrom)
//            return false;
//        if(charset == null)
//            return false;
//        this.charsetAsAll = charset;
//        getIsConfSet().put("charsetAsAll", Boolean.TRUE);
//        return true;
//    }
    /**
     * Metînsel alanlar varsayılan metîn uzunluğunu belirtmek için kullanılır
     * Aksi belirtilmedikçe bu yapılandırma tüm metînsel alanlar için uygulanır
     * Burada, bu ayarın veritabanında çalışabilirliği denetlenmez
     * Veritabanlarındaki pek çok metîn tipinin azamî uzunluğu, diğer sütunlara
     * ve karakter setine bağımlı olduğundan dikkat edilmelidir
     * @param length Varsayılan metîn uzunluğu, [0-12000] aralığında olabilir
     * @return İşlem başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean setDefaultLengthOfString(int length){
        if(isTableCreated)
            return false;
        if(length <= 0 || length >= 12000 || length == lengthForStringAsDefault)// Azamî veri uzunluğu 20000 olabilir, olarak işâretleniyor
            return false;
        lengthForStringAsDefault = length;
        isDefaultLengthOfStringChanged = true;
        return true;
    }
    /**
     * Metînsel veri tutan sütunun metîn uzunluğunu belirtmek için kullanılır
     * Verilen isimdeki sütunun tipi metîn ise, sütun uzunluğu ayarlanır
     * Burada, bu ayarın veritabanında çalışabilirliği denetlenmez
     * @param fieldName Hedef sütun ismi
     * @param length İstenen metîn uzunluğu
     * @return İşlem başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean setLengthOfString(String fieldName, int length){
        if(isTableCreated)
            return false;
        if(fieldName == null)
            return false;
        if(fieldName.isEmpty())
            return false;
        if(!isInFields(fieldName))
            return false;
        if(length <= 0 || length >= 12000 || length == lengthForStringAsDefault)
            return false;
        if(isDataTypeString(fieldName)){// Hedef veri tipi varsa ve String ise;
            getSpecifiedLengths().put(fieldName, length);
            return true;
        }
        return false;
    }
    /**
     * Veritabanına kaydedilmesi istenmeyen alan bu yöntemle dışlanabilir
     * Dışlanan bir alanın veritabanına kaydedilmesi mümkün değildir
     * Bu, alanın sistem tarafından değerlendirmeye alınmayacağını gösterir
     * Bir alan dışlandığında, bağlı olduğu tüm kısıt ve yapılandırma silinir
     * @param fieldName Dışlanmak istenen alanın ismi
     */
    public void discardField(String fieldName){
        if(isTableCreated)
            return;
        if(fieldName == null)
            return;
        if(fieldName.isEmpty())
            return;
        getFieldNames().remove(fieldName);// Alanı listeden kaldır
        {// Sütun üzerinde kısıt ve yapılandırma varsa, kaldır:
            getUniqueFields().remove(fieldName);// Münferid kısıtı varsa, kaldır
            getIndexes().remove(fieldName);// İndeks varsa, kaldır
            getNotNulls().remove(fieldName);// 'Boş olmama' kısıtı varsa, kaldır
            getDefaultValues().remove(fieldName);// Varsayılan değer kısıtı varsa, kaldır
            getSpecifiedLengths().remove(fieldName);// Müşahhas uzunluk belirtimi kısıtı varsa, kaldır
            if(getIsConfSet().get("primaryKey")){
                if(getPrimaryKey().equals(fieldName)){
                    this.setPrimaryKey(null);
                }
            }
        }
    }
    /**
     * Evvelce dışlanmış olan bir alan yeniden değerlendirme listesine eklenir
     * @param fieldName Dışlanmış alanın ismi
     */
    public void addDiscardedField(String fieldName){
        if(isTableCreated)
            return;
        if(fieldName == null)
            return;
        if(fieldName.isEmpty())
            return;
        getFieldNames().add(fieldName);// Alanı listeden kaldır
    }
    // ARKAPLAN İŞLEM YÖNTEMLERİ:
    private void fillIsConfSetMap(){// Özellik haritasını başlat
        isConfSet = new HashMap<String, Boolean>();
        for(Field f : this.getClass().getDeclaredFields()){
            isConfSet.put(f.getName(), Boolean.FALSE);
        }
        isConfSet.remove("cls");
    }
    private void assignFieldNames(){
        for(Field f : this.cls.getDeclaredFields()){
            getFieldNames().add(f.getName());
        }
    }
    private boolean checkDataType(String fieldName, Object value){
        try{
            Class dataType = cls.getDeclaredField(fieldName).getType();
            if(value.getClass().equals(dataType))
                return true;
            else{
                try{
                    if(Reflector.getService().isPairingAutomatically(dataType, value.getClass()))
                        return true;
                    if(dataType.cast(value) != null)
                        return true;
                }
                catch(ClassCastException excOnCasting){
                    System.err.println("excOnCasting : " + excOnCasting.toString());
                    return false;
                }
            }
        }
        catch(NoSuchFieldException | SecurityException exc){
            System.err.println("exc : " + exc.toString());
        }
        return false;
    }
    private boolean isDataTypeString(String fieldName){
        try{
            Field fl = cls.getDeclaredField(fieldName);
            if(fl != null){
                if(fl.getType().getTypeName().equals("java.lang.String"))
                    return true;
            }
        }
        catch(NoSuchFieldException | SecurityException exc){
            System.err.println("Hedef alan bir metîn veri tipi olmadığından veyâ erişim izni olmadığından veri tipinin String olduğu teyyit edilemedi");
        }
        return false;
    }

// ERİŞİM YÖNTEMLERİ:
    /**
     * İlgili sütunun münferid ('UNIQUE') kısıtı olup, olmadığını sorgular
     * @param fieldName Sütun ismi
     * @return Hedef kısıt varsa {@code true}, aksi hâlde {@code false}
     */
    public boolean isUnique(String fieldName){
        if(fieldName == null)
            return false;
        if(fieldName.isEmpty())
            return false;
        for(String s : getUniqueFields()){
            if(s.equalsIgnoreCase(fieldName))
                return true;
        }
        return false;
    }
    /**
     * İlgili sütunun boş olmama ('NOT NULL') kısıtı olup, olmadığını sorgular
     * @param fieldName Sütun ismi
     * @return Hedef kısıt varsa {@code true}, aksi hâlde {@code false}
     */
    public boolean isNotNull(String fieldName){
        if(fieldName == null)
            return false;
        if(fieldName.isEmpty())
            return false;
        for(String s : getNotNulls()){
            if(s.equalsIgnoreCase(fieldName))
                return true;
        }
        return false;
    }
    /**
     * Metînsel sütun için, karakter sayısı belirtip, belirtilmediğini sorgular
     * @param fieldName Hedef sütun ismi
     * @return Karakter sayısı belirtilmişse {@code true}, aksi hâlde {@code false}
     */
    public boolean isSetSpecifiedLength(String fieldName){
        if(fieldName == null)
            return false;
        if(fieldName.isEmpty())
            return false;
        Object value = getSpecifiedLengths().get(fieldName);
        return (value != null);
    }
    /**
     * Metînsel sütunlar için, ilgili sütun uzunluğunu döndürür
     * @param fieldName Hedef sütun ismi
     * @return Varsa ilgili sütun uzunluğu, diğer durumlarda {@code 0}
     */
    public int getSpecifiedLength(String fieldName){
        if(fieldName == null)
            return -1;
        if(fieldName.isEmpty())
            return -1;
        Integer value = getSpecifiedLengths().get(fieldName);
        return (value != null ? value : 0);
    }
    /**
     * Yapılandırılma belirtim haritasını döndürür.
     * Haritada {@code true} olan değerler o yapılandırmanın 
     * kullanıcı tarafından belirtildiğini gösterir
     * @return 
     */
    protected HashMap<String, Boolean> getIsConfSet(){
        if(isConfSet == null){
            fillIsConfSetMap();
        }
        return isConfSet;
    }
    /**
     * Tablo yapılandırmasının belirtildiği hedef sınıf
     * @return Hedef sınıf
     */
    public Class getClassOfTable(){
        return cls;
    }
    public TableCharset.CHARSET getCharsetAsAll(){
        return charsetAsAll;
    }
    public ArrayList<String> getUniqueFields(){
        if(uniqueFields == null)
            uniqueFields = new ArrayList<String>();
        return uniqueFields;
    }
    public ArrayList<String> getIndexes(){
        if(indexes == null)
            indexes = new ArrayList<String>();
        return indexes;
    }
    public ArrayList<String> getNotNulls(){
        if(notNulls == null)
            notNulls = new ArrayList<String>();
        return notNulls;
    }
    /**
     * Birincil anahtar belirtilmişse, o özelliğin (sütunun) ismi döndürülür
     * @return Birincil anahtar sütununun ismi veyâ {@code null}
     */
    public String getPrimaryKey(){
        if(isConfSet.get("primaryKey"))
            return primaryKey;
        return null;
    }
    /**
     * Sınıfın alınabilecek sütun isimlerini döndürür
     * Burada ismi olmayan sütunun veritabanına kaydedilmesi mümkün değildir
     * Sistem, ismi olanları inceler; yapılandırma ve sistem desteğine göre alır
     * Yanî burada ismi olanların veritabanına kaydedileceği kesin değildir
     * @return Sınıfın aday sütun isimleri dizisi
     */
    public String[] getFieldNamesAsArray(){// Alanların isimlerini liste olarak döndür
        String[] names = new String[getFieldNames().size()];
        getFieldNames().toArray(names);
        return names;
    }
    /**
     * Sınıfın karşılığı olabilecek tablonun ismini döndürür
     * @return 
     */
    public String getTableName(){
        return this.cls.getSimpleName();
    }
    public HashMap<String, Object> getDefaultValues(){
        if(valuesAsDefault == null)
            valuesAsDefault = new HashMap<String, Object>();
        return valuesAsDefault;
    }
    public Object getDefaultValueOfField(String fieldName){
        return getDefaultValues().get(fieldName);
    }
    public HashMap<String, Integer> getSpecifiedLengths(){
        if(specifiedLengths == null)
            specifiedLengths = new HashMap<String, Integer>();
        return specifiedLengths;
    }
    /**
     * Varsayılan metîn uzunluğunun değiştirilme durumunu belirtir
     * @return Varsayılan değiştirilmişse {@code true}, aksi hâlde {@code false}
     */
    public boolean getIsDefaultLengthOfStringChanged(){
        return isDefaultLengthOfStringChanged;
    }
    /**
     * Verilen isimdeki özellik metîn ise, metîn uzunluğunu döndürür
     * Sütun için metîn uzunluğu belirtilmemişse, varsayılan uzunluk döndürülür
     * Özellik metîn değilse veyâ böyle bir alan yoksa {@code -1} döndürülür
     * @param fieldName Özellik (alan) ismi
     * @return Sütunun metîn uzunluğu veyâ {@code -1}
     */
    public int getLengthOfString(String fieldName){
        if(!isInFields(fieldName))// İlgili alan yoksa '-1' döndür
            return -1;
        Integer value = getSpecifiedLengths().get(fieldName);
        if(value != null)// Bu alan için özel bir uzunluk değeri belirtildiyse bunu döndür
            return (value);
        if(isDataTypeString(fieldName))
            return lengthForStringAsDefault;// İlgili alanın veri tipi 'String' ise varsayılan uzunluğu döndür
        return -1;// Diğer durumlarda -1 döndür
    }
    /**
     * Metînsel sütunlar için varsayılan uzunluğu döndürür
     * @return Varsayılan metîn uzunluğu
     */
    public int getDefaultLengthOfString(){
        return lengthForStringAsDefault;
    }
    /**
     * Yapılandırması belirtilen tablonun oluşturulup, oluşturulmadığı belirtir
     * @return Tablo oluşturulmuşsa {@code true}, aksi hâlde {@code false}
     */
    public boolean getIsTableCreated(){
        return isTableCreated;
    }
    //KORUNAN ERİŞİM YÖNTEMLERİ:
    /**
     * Tüm aday sütunlarını isimlerini döndürür
     * Tüm özellikler varsayılan olarak aday sütundur
     * {@code discardField()} ile bir alan aday sütun olmaktan çıkarılır
     * @return Aday sütunları tutan bir sıralı set ({@code SortedSet})
     */
    protected SortedSet<String> getFieldNames(){
        if(fieldNames == null){
            fieldNames = new TreeSet<String>();// ? 
        }
        return fieldNames;
    }
    /**
     * Verilen alanın aday sütun olup, olmadığı sorgulanır
     * Aday sütun, sistem tarafından dikkate alınan sütun demektir
     * Aday olmayan bir sütun veritabanına kaydedilemez
     * Varsayılan olarak, tüm alanlar (özellikler, 'fields') aday sütundur
     * {@code discardField()} yöntemi bir alanı aday sütun olmaktan çıkarır
     * @param fieldName Sütun ismi
     * @return Sütun aday sütun ise {@code true}, değilse {@code false}
     */
    protected boolean isInFields(String fieldName){
        Iterator<String> iter = getFieldNames().iterator();
        while(iter.hasNext()){
            if(iter.next().equals(fieldName))
                return true;
        }
        return false;
    }
    /**
     * Bu yöntem dışlanan alanların kaldırılmış olduğu alan listesini döndürür
     * @return {@code IkizIdare} tarafından alınabilecek alanların listesi
     */
    protected Field[] getTakeableFields(){
        Field[] fields = cls.getDeclaredFields();
        ArrayList<Field> filtered = new ArrayList<Field>();
        for(Field fl : fields){
            if(isInFields(fl.getName()))
                filtered.add(fl);
        }
        Field[] arrFiltered = new Field[filtered.size()];
        filtered.toArray(arrFiltered);
        return arrFiltered;
    }
}