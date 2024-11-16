package ikiz;

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
 * @author Mehmet Âkif
 */
// Not : İndeksler için isim ataması şu an yapılmıyor
public class TableConfiguration{// ALAN İSİMLERİ DEĞİŞTİRİLEMEZ!
    private Class cls;
    private HashMap<String, Boolean> isConfSet;// <özellik, değerAtandıMı> : Bir özellik için değer atandıysa o özellik için 'true' olmalıdır
    private TableCharset.CHARSET charsetAsAll;// Tablodaki metîn tabanlı alanlar için genel karakter seti
    private ArrayList<String> uniqueFields;// 'unique' olması istenen alanlar buraya yazılır
    private ArrayList<String> indexes;// 'Birincil anahtar hâricinde ve 'unique' alanlar hâricinde 'indeks' eklenmesi istenen alanlar buraya yazılır
    private String primaryKey;// Birincil anahtarın uygulanacağı sütun ismi
    // varchar alanlar için sınır belirleme için alan eklenecek
    // karakter setinin alan özelinde olması için yapılar eklenecek, bi iznillâh
    private SortedSet<String> fieldNames;// Verilen sınıftaki alanların isimleri

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
    public boolean addUniqueField(String fieldName){// Bir alan için 'münferid' olma özelliği ata
        if(fieldName == null)
            return false;
        if(!isInFields(primaryKey))
            return false;
        if(Helper.isInTheList(uniqueFields, fieldName))// İlgili alan için zâten 'münferid' özelliği atandıysa işlemi sonlandır
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
    public boolean setCharsetAsAll(TableCharset.CHARSET charset){
        if(charset == null)
            return false;
        this.charsetAsAll = charset;
        getIsConfSet().put("charsetAsAll", Boolean.TRUE);
        return true;
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
        if(uniqueFields == null){
            uniqueFields = new ArrayList<String>();
        }
        return uniqueFields;
    }
    public ArrayList<String> getIndexes(){
        if(indexes == null){
            indexes = new ArrayList<String>();
        }
        return indexes;
    }
    public String getPrimaryKey(){
        return primaryKey;
    }
    private SortedSet<String> getFieldNames(){
        if(fieldNames == null){
            fieldNames = new TreeSet<String>();// ? 
        }
        return fieldNames;
    }
    public String[] getFieldNamesAsArray(){// Alanların isimlerini liste olarak döndür
        String[] names = new String[getFieldNames().size()];
        getFieldNames().toArray(names);
        return names;
    }
}