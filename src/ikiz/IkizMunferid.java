package ikiz;

import java.util.HashMap;
/**
 * İkiz'deki tablo verilerinin münferiden ele alınması için kurulmuş bir yapıdır
 * Bu versiyonda sadece birincil anahtara göre indeksleme yapılmaktadır; yanî
 * birincil anahtarı olan bir verinin uygulama içerisinde tek bir nesne olarak
 * ele alınması sağlanmaktadır
 * @author Mehmet Akif SOLAK
 */
public class IkizMunferid implements IkizTeamPlayer{
    private HashMap<String, HashMap<Object, Object>> hashed;// <tabloİsmi, <birincilAnahtar, nesne>> biçiminde nesnelere kolay erişim için kullanılan harita
    private IkizIdare idare;
    private HashMap<String, TableMetadata> metadataOfTables;

/**
 * Bir {@code IkizMunferid} örneği oluşturmak için kullanılan kurucu sınıftır 
 * @param idare Bu sistemin entegre olacağı {@code IkizIdare} örneği
 * @throws IllegalArgumentException Girdi {@code null} ise fırlatılır
 */
    public IkizMunferid(IkizIdare idare) throws IllegalArgumentException{
        if(idare == null)
            throw new IllegalArgumentException("Verilen IkizIdare nesnesi null!");
        this.idare = idare;
//        this.metadataOfTables = idare.getMetadataOfTables(this);// BUNA GEREK YOK GİBİ
        hashed = new HashMap<String, HashMap<Object, Object>>();
    }

//İŞLEM YÖNTEMLERİ:
    //ARKAPLAN İŞLEM YÖNTEMLERİ:
    /**
     * İndekslenmek üzere veri girişi bu yöntemle yapılır
     * Veriler {<birincil anahtar, nesne>} biçiminde olmalıdır
     * Güvenlik amacıyla verilen {@code IkizMunferid} nesnesi bu nesneye eşitse
     * işlem yapılır, aksi hâlde yapılmaz
     * @param ikizMunferid İşlemin uygulanacağı {@code IkizMunferid} nesnesi
     * @param tableName Tablo ismi
     * @param mapOfPrimaryKeyToObject Veriler, birincil anahtar -> nesne
     * @return İşlem başarılıysa {@code true}, aksi hâlde {@code false}
     */
    protected boolean assingToHashed(IkizMunferid ikizMunferid, String tableName, HashMap<Object, Object> mapOfPrimaryKeyToObject){
        if(ikizMunferid == null || tableName == null || mapOfPrimaryKeyToObject == null)
            return false;
        if(tableName.isEmpty())
            return false;
        if(ikizMunferid == this){
            this.hashed.put(tableName, mapOfPrimaryKeyToObject);
            return true;
        }
        return false;
    }
    /**
     * Birincil anahtarı verilen verinin uygulamadaki nesne hâlini döndürür.
     * @param <T>
     * @param target
     * @param valueOfPrimaryKey
     * @return 
     */
    protected <T> T getCurrentObjectWithPrimaryKey(Class<?> target, Object valueOfPrimaryKey){
        if(target == null || valueOfPrimaryKey == null)
            return null;
        HashMap<Object, Object> mapOfPrimaryKeyToObject = hashed.get(target.getSimpleName());
        if(mapOfPrimaryKeyToObject == null)
            return null;
        T value = (T) hashed.get(target.getSimpleName()).get(valueOfPrimaryKey);
        return value;
    }
    /**
     * Verilen sınıfın tablo yapılandırmasına bakarak, münferidliğin sağlanıp,
     * sağlanamayacağı hakkında bilgi verir.
     * İlgili sınıfın tablo yapılandırmasında birincil anahtar varsa verinin
     * uygulama içinde münferid olması sağlanabileceğinden {@code true} verilir
     * @param <T>
     * @param target
     * @see TableConfiguration
     * @return 
     */
    protected boolean isCouldGettingObjectWithPrimaryKey(Class<?> target){
        if(target == null)
            return false;
        this.metadataOfTables = idare.getMetadataOfTables(this);
        TableConfiguration tableConfs = this.metadataOfTables.get(target.getSimpleName()).getConfs();
        if(tableConfs == null)
            return false;
        return (tableConfs.getPrimaryKey() != null);
    }
    /**
     * Verilen tablo daha önce indekslendi mi?
     * @param target
     * @return 
     */
    protected boolean isIndexedBefore(Class<?> target){
        if(target == null)
            return false;
        return (hashed.get(target.getSimpleName()) != null);
    }
    /**
     * Birincil anahtarı kullanarak veriyi önbellekten siler
     * @param tableName Verinin bulunduğu tablo ismi
     * @param valueOfPrimary Verinin birincil anahtar sütunundaki değers
     */
    protected void deleteObjectOnBufferByPrimaryKey(String tableName, Object valueOfPrimary){
        Object obj = hashed.get(tableName).remove(valueOfPrimary);
        this.idare.getBufferTables(this).get(tableName).remove(obj);
    }
    /**
     * Nesne referansını alır, önbellekteki veriyi {@code equals} yöntemiyle
     * arar ve bulduğu veriyi siler
     * @param tableName Silinecek verinin hangi tabloya âit olduğu bilgisi
     * @param entity Önbellekten silinmesi istenen veri
     */
    protected void deleteObjectOnBufferByReference(String tableName, Object entity){
        try{
            HashMap<Object, Object> target = hashed.get(tableName);
            Object delKey = null;
            for(Object key : target.keySet()){
                if(target == entity){
                    delKey = key;
                    break;
                }
            }
            if(delKey != null){
                target.remove(delKey);
                this.idare.getBufferTables(this).get(tableName).remove(entity);
            }
        }
        catch(NullPointerException exc){
            System.err.println("Verilen referanslı nesne silinemedi : " + exc.toString());
        }
    }
    /**
     * Verilen isimdeki tablo için önbellek indeksini siler
     * @param tableName Tablo ismi
     */
    protected void removeTable(String tableName){
        this.hashed.remove(tableName);
        this.idare.getBufferTables(this).remove(tableName);
    }
    /**
     * Verilen nesne -eğer varsa- ilgili indekse koyulur
     * @param <T> Nesnenin tipi
     * @param tableName Tablo ismi
     * @param primaryKeyValue Verinin birincil anahtar değeri
     * @param instanceOfRow Satırdan üretilen ve münferid olması istenen nesne
     */
    protected <T> void putToHashed(String tableName, Object primaryKeyValue, T instanceOfRow){
        if(tableName == null | primaryKeyValue == null | instanceOfRow == null)
            return;
        if(tableName.isEmpty())
            return;
        HashMap<Object, Object> targetIndex = hashed.get(primaryKeyValue);
        if(targetIndex != null)
            hashed.get(tableName).put(primaryKeyValue, instanceOfRow);
    }

//ERİŞİM YÖNTEMLERİ:
}