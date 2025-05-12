package ikiz;

import java.util.HashMap;
import ReflectorRuntime.Reflector.CODING_STYLE;
/**
 * Ikiz sistem yapılandırma sınıfı
 * @author Mehmet Akif SOLAK
 */
public class Confs{// Sistemin yapılandırmalarının belirtildiği yerdir
    private HashMap<String, Boolean> attributesPolicy;// Alan alma usûlü : <erişimBelirteci, alınsınMı?>
    protected boolean alwaysContinue = true;//Eğer kullanıcın alınmasını istediği değer alınamadıysa, kalan değerlerle veritabanına yazmaya çalış
//    BU SÜRÜMDE YOK : protected boolean workWithNonParameterConstructor = true;//
//    BU SÜRÜMDE YOK : private HashMap<String, UpdateMode> updateModeOfTables;//Tabloların tazeleme modunu belirtiyor.
    protected boolean bufferMode = false;//Her veri çekme isteğinde veritabanından veri çekilmemesi için verilerin İkiz'de saklandığı bir çalışma şekli
    //private boolean workWithSpecialBuilder = false;//Yalnızca ikizIdare sınıfından bir nesnenin 'specialCode' kullanarak erişebileceği bir inşâcı ile çalış
    //private String specialCode;//Özel inşâcı ile çalışıldığı durumda özel inşâcının ikizIdare için ilgili sınıftan bir değişken üretmesi için gereken kod
    private POLICY_FOR_LIST_MAP_ARRAY policyForListArrayMapFields = POLICY_FOR_LIST_MAP_ARRAY.TAKE_AS_JSON;// Liste - dizi ve harita alanlarının alım politikası
//    BU SÜRÜMDE YOK : private POLICY_FOR_USER_DEFINED_CLASSES policyForUserDefinedClasses = POLICY_FOR_USER_DEFINED_CLASSES.TAKE_AS_JSON;// Kullanıcı tanımlı alanların alım politikası
    protected CODING_STYLE codingStyleForGetterSetter = CODING_STYLE.CAMEL_CASE;// 'getter' ve 'setter' yöntemlerinin isimlerini bulabilmek için gerekli kodlama biçimi
//.;.

    /**
     * Sınıf içerisindeki liste, dizi ve harita tipindeki verilerin veritabanına
     * nasıl kaydedileceğini belirten bir değer kümesidir
     * {@code TAKE_AS_JSON} : JSON tipinde kaydedilir(veritabanı destekliyorsa)
     * {@code DONT_TAKE} : Bu alanlar veritabanına kaydedilmez
     */
    public enum POLICY_FOR_LIST_MAP_ARRAY{// Bir sınıftaki liste, harita ve dizi türü verilerin nasıl saklanacağıyla ilgili yapılandırma
        TAKE_AS_JSON,// İlgili veri veritabanında JSON biçiminde saklanır (destekleyen veritabanlarında çalışır)
        DONT_TAKE// İlgili veriler sistem tarafından yoksayılır, veritabanına kaydedilmez
    }
    /**
     * Sınıf içerisinde kullanıcı tanımlı tipte bir özellik varsa, bunun nasıl
     * ele alınacağı bu değer kümesindeki parametre ayarıyla belirtilir
     * {@code TAKE_AS_JSON} : JSON tipinde kaydedilir(veritabanı destekliyorsa)
     * {@code DONT_TAKE} : Bu alanlar veritabanına kaydedilmez
     */
//    public enum POLICY_FOR_USER_DEFINED_CLASSES{
//        TAKE_AS_JSON,
//        DONT_TAKE
//    }

    public Confs(){
        
    }

// İŞLEM YÖNTEMLERİ:
    // SINIF FONKSİYONLARI:
    /**
     * Ikiz için varsayılan yapılandırma örneği döndürür
     * @return {code Confs} tipinde yapılandırma örneği
     */
    protected static Confs getDefaultConfs(){
        Confs confs = new Confs();
        return setDefault(confs);
    }
    /**
     * Uygulamadaki nesnelerin otomatik tazelenmesi için yapılandırma belirtin
     * Bu ayarı belirtebilmek için önbellekleme modu aktif olmalıdır
     * @param tableName İlgili tablo ismi
     * @param mode Tablo otomatik tazelenme yapılandırması
     */
//    protected void setUpdateModeOfTable(String tableName, UpdateMode mode){
//        if(!bufferMode)
//            return;
//        if(mode == null)
//            return;
//        boolean isFound = false;
//        for(String table : getUpdateModeOfTables().keySet()){
//            if(table.equals(tableName)){
//                isFound = true;
//                break;
//            }
//        }
//        if(!isFound)
//            return;
//        getUpdateModeOfTables().put(tableName, mode);
//    }
    /**
     * Tüm tablolar için otomatik tazelenme yapılandırması belirtin
     * 
     * @param mode Tablo otomatik tazelenme yapılandırması
     */
//    public void setUpdateModeOfTableForAllTables(UpdateMode mode){
//        if(mode == null)
//            return;
//        if(!bufferMode)
//            return;
//        for(String table : getUpdateModeOfTables().keySet()){
//            getUpdateModeOfTables().put(table, mode);
//        }
//    }
    // SINIF ARKAPLAN FONSKSİYONLARI:
    private static Confs setDefault(Confs confs){
        if(confs == null)
            return null;
        confs.alwaysContinue = true;
//        confs.workWithNonParameterConstructor = true;
        confs.bufferMode = true;
        confs.getAttributesPolicy();
        confs.policyForListArrayMapFields = POLICY_FOR_LIST_MAP_ARRAY.TAKE_AS_JSON;
        confs.codingStyleForGetterSetter = CODING_STYLE.CAMEL_CASE;
        return confs;
    }
    /**
     * Veritabanda birden fazla satır ekleme gibi toplu işlem yapıldığında ve
     * işlemlerin birisinde bir hatâ olduğunda işleme devâm edilip, edilmeyeceği
     * kararı bu yapılandırma ayarına göre yapılır
     * {code true} ise işleme devâm edilir,
     * {@code false} ise işlem sonlandırılır
     * @param alwaysContinue İşleme devâm etmeyi zorlama yapılandırması
     */
    public void setAlwaysContinue(boolean alwaysContinue){
        this.alwaysContinue = alwaysContinue;
    }
    /**
     * Verilerin zerk edilerek oluşturulması için parametresiz yapıcı yöntemin
     * kullanılması gerektiğini belirtmek için kullanılan yapılandırma ayarı
     * @param workWithNonParameterConstructor Parametresiz yapıcı yöntemin
     * seçilmesi için {@code true} değerini verin
     */
//    public void setWorkWithNonParameterConstructor(boolean workWithNonParameterConstructor){
//        this.workWithNonParameterConstructor = workWithNonParameterConstructor;
//    }
    /**
     * {@code IkizIdare} tarafından yapılandırma zerki için tutulan bir yöntem
     * @param updateModeOfTables 
     */
//    protected void setUpdateModeOfTables(HashMap<String, UpdateMode> updateModeOfTables){
//        this.updateModeOfTables = updateModeOfTables;
//    }
    /**
     * Önbellekleme modunu simgeleyen yapılandırma ayarıdır
     * Bu mod aktifken uygulamaya gelen istek önbellektekten karşılanmaya
     * çalışılır
     * @param bufferMode {@code true} önbellekme aktif, {@code false} pasif..
     */
    public void setBufferMode(boolean bufferMode){
        this.bufferMode = bufferMode;
    }
    /**
     * Ikiz'in hangi erişim belirtecindeki özellikleri alacağını belirten
     * yapılandırma ayarıdır
     * İlgili tipteki özellik alınmak isteniyorsa [@code true} verilmelidir<br>
     * @param takePublicFields {@code public} tipindekiler alınsın mı?<br>
     * @param takePrivateFields {@code private} tipindekiler alınsın mı?<br>
     * @param takeDefaultFields {@code default} tipindekiler alınsın mı?<br>
     * @param takeProtectedFields  {@code protected} tipindekiler alınsın mı?
     */
    public void setAttributesPolicyOneByOne(boolean takePublicFields, boolean takePrivateFields, boolean takeDefaultFields, boolean takeProtectedFields){
        if(takePrivateFields || takeDefaultFields || takeProtectedFields){
            //Kullanıcıya bu sınıfların getter ve setter yöntemleri içermesi gerektiğini hâtırlat
//            controlGetterAndSetterForHideFields();
        }
        getAttributesPolicy().put("public", takePublicFields);
        attributesPolicy.put("private", takePrivateFields);
        attributesPolicy.put("default", takeDefaultFields);
        attributesPolicy.put("protected", takeProtectedFields);
    }
    public void setPolicyForListArrayMapFields(POLICY_FOR_LIST_MAP_ARRAY methodForListAndMapFields){
        this.policyForListArrayMapFields = methodForListAndMapFields;
    }
//    public void setPolicyForListUserDefinedClasses(POLICY_FOR_USER_DEFINED_CLASSES methodForUserDefinedClasses){
//        this.policyForUserDefinedClasses = methodForUserDefinedClasses;
//    }
    public void setCodingStyleForGetterSetter(CODING_STYLE codingStyle){
        this.codingStyleForGetterSetter = codingStyle;
    }
    public void setAttributesPolicy(HashMap<String, Boolean> attributesPolicy){
        this.attributesPolicy = attributesPolicy;
    }
    // ARKAPLAN İŞLEM YÖNTEMLERİ:

// ERİŞİM YÖNTEMLERİ:
    public HashMap<String, Boolean> getAttributesPolicy(){
        if(attributesPolicy == null){
            attributesPolicy = new HashMap<>();
            attributesPolicy.put("public", true);
            attributesPolicy.put("private", false);
            attributesPolicy.put("default", true);
            attributesPolicy.put("protected", true);
        }
        return attributesPolicy;
    }
    public boolean isAlwaysContinue(){
        return alwaysContinue;
    }
//    public boolean isWorkWithNonParameterConstructor(){
//        return workWithNonParameterConstructor;
//    }
//    public HashMap<String, UpdateMode> getUpdateModeOfTables(){
//        if(updateModeOfTables == null){
//            updateModeOfTables = new HashMap<String, UpdateMode>();
//        }
//        return updateModeOfTables;
//    }
    public POLICY_FOR_LIST_MAP_ARRAY getPolicyForListArrayMapFields(){
        return policyForListArrayMapFields;
    }
//    public POLICY_FOR_USER_DEFINED_CLASSES getPolicyForUserDefinedClasses(){
//        return policyForUserDefinedClasses;
//    }
}