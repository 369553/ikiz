package ikiz;

import ReflectorRuntime.Reflector;
import ikiz.Services.Helper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
/**
 * Bu sınıf bir tablo oluştururken gerekli yapılandırmaları tutmak
 * ve idâre etmek içindir.
 * @author Mehmed Âkif SOLAK
 */
// Not : İndeksler için isim ataması şu an yapılmıyor
public class TableConfiguration{// ALAN İSİMLERİ DEĞİŞTİRİLEMEZ!
    private Class cls;
    private HashMap<String, Boolean> isConfSet;// <özellik, değerAtandıMı> : Bir özellik için değer atandıysa o özellik için 'true' olmalıdır
    private TableCharset.CHARSET charsetAsAll;// Tablodaki metîn tabanlı alanlar için genel karakter seti
    private ArrayList<String> uniqueFields;// 'unique' olması istenen alanlar buraya yazılır
    private ArrayList<String> indexes;// 'Birincil anahtar hâricinde ve 'unique' alanlar hâricinde 'indeks' eklenmesi istenen alanlar buraya yazılır
    private String primaryKey;// Birincil anahtarın uygulanacağı sütun ismi
    private ArrayList<String> notNulls;
    // varchar alanlar için sınır belirleme için alan eklenecek
    // karakter setinin alan özelinde olması için yapılar eklenecek, bi iznillâh
    private SortedSet<String> fieldNames;// Verilen sınıftaki alanların isimleri
    private HashMap<String, Object> valuesAsDefault;// Sütun için varsayılan değer atandığı durumda..
    private HashMap<String, Integer> specifiedLengths;// Metîn tipindeki verilerde, metîn uzunluğu için özel bir değer belirtildiyse, onu <sütunİsmi, değer> biçiminde tutar.
    private int lengthForStringAsDefault = 500;// Metîn tipindeki veriler için varsayılan metîn uzunluğu
    private boolean isDefaultLengthOfStringChanged = false;

    public TableConfiguration(Class classOfTable){// cls : Yapılandırma yapılması istenen sınıf
        this.cls = classOfTable;
        assignFieldNames();// Sınıfın alan isimlerini al; bu, olmayan alan için yapılandırma eklenememesi içindir
    }

// İŞLEM YÖNTEMLERİ:
    public boolean setPrimaryKey(String primaryKey){// Birincil anahtar ataması yap
        if(primaryKey == null)
            return false;
        if(!isInFields(primaryKey))
            return false;
        this.primaryKey = primaryKey;
        getIsConfSet().put("primaryKey", Boolean.TRUE);
        return true;
    }
    public boolean addUniqueConstraint(String fieldName){// Bir alan için 'münferid' olma özelliği ata
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
    public boolean addIndex(String fieldName){
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
    public boolean addNotNullConstraint(String fieldName){
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
    public boolean addDefaultValue(String fieldName, Object value){
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
    public boolean setCharsetAsAll(TableCharset.CHARSET charset){
        if(charset == null)
            return false;
        this.charsetAsAll = charset;
        getIsConfSet().put("charsetAsAll", Boolean.TRUE);
        return true;
    }
    public boolean isUnique(String fieldName){
        for(String s : getUniqueFields()){
            if(s.equalsIgnoreCase(fieldName))
                return true;
        }
        return false;
    }
    public boolean isNotNull(String fieldName){
        for(String s : getNotNulls()){
            if(s.equalsIgnoreCase(fieldName))
                return true;
        }
        return false;
    }
    public boolean isSetSpecifiedLength(String fieldName){
        if(fieldName == null)
            return false;
        Object value = getSpecifiedLengths().get(fieldName);
        return (value != null);
    }
    public int getSpecifiedLength(String fieldName){
        Integer value = getSpecifiedLengths().get(fieldName);
        return (value != null ? value : 0);
    }
    // ARKAPLAN İŞLEM YÖNTEMLERİ:
    private void fillIsConfSetMap(){// Özellik haritasını başlat
        isConfSet = new HashMap<String, Boolean>();
        for(Field f : this.getClass().getDeclaredFields()){
            isConfSet.put(f.getName(), Boolean.FALSE);
        }
    }
    private void assignFieldNames(){
        for(Field f : this.cls.getDeclaredFields()){
            getFieldNames().add(f.getName());
        }
    }
    // 'TableConfiguration' nesnesini zerk etmek için kullanılan bâzı arkaplan işlemleri:
    private void setIsConfSet(HashMap<String, Boolean> isConfSet){
        if(isConfSet == null)
            return;
        this.isConfSet = isConfSet;
    }
    private void setUniqueFields(ArrayList<String> uniqueFields){// Bu yöntemler 'tehlikelidir'; çünkü diğerleriyle olan uyumluluğu sağlamıyor; misal, 'münferid' olma özelliği eklenen alanlar için getIsConfSet() yapılandırılmıyor
        if(uniqueFields == null)
            return;
        this.uniqueFields = uniqueFields;
    }
    private void setIndexes(ArrayList<String> indexes){
        if(indexes == null)
            return;
        this.indexes = indexes;
    }
    private boolean isInFields(String fieldName){
        Iterator<String> iter = getFieldNames().iterator();
        while(iter.hasNext()){
            if(iter.next().equals(fieldName))
                return true;
        }
        return false;
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
    public boolean setDefaultLengthOfString(int length){
        if(length <= 0 || length >= 12000 || length == lengthForStringAsDefault)// Azamî veri uzunluğu 20000 olabilir, olarak işâretleniyor
            return false;
        lengthForStringAsDefault = length;
        isDefaultLengthOfStringChanged = true;
        return true;
    }
    public boolean setLengthOfString(String fieldName, int length){
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
    public HashMap<String, Boolean> getIsConfSet(){
        if(isConfSet == null){
            fillIsConfSetMap();
        }
        return isConfSet;
    }
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
    public String getPrimaryKey(){
        return primaryKey;
    }
    public String[] getFieldNamesAsArray(){// Alanların isimlerini liste olarak döndür
        String[] names = new String[getFieldNames().size()];
        getFieldNames().toArray(names);
        return names;
    }
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
    public boolean getIsDefaultLengthOfStringChanged(){
        return isDefaultLengthOfStringChanged;
    }
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
    //GİZLİ ERİŞİM YÖNTEMLERİ:
    private SortedSet<String> getFieldNames(){
        if(fieldNames == null){
            fieldNames = new TreeSet<String>();// ? 
        }
        return fieldNames;
    }
}