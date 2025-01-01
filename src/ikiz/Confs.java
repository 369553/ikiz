package ikiz;

import java.util.HashMap;

public class Confs{// Sistemin yapılandırmalarının belirtildiği yerdir
    private HashMap<String, Boolean> attributesPolicy;// Alan alma usûlü : <erişimBelirteci, alınsınMı?>
    protected boolean takeDateAttributeAsDateTime = true;// 'Date' tipindeki alanları veritabanına aktarırken 'DATETIME' tipinde aktar
    protected boolean alwaysContinue = true;//Eğer kullanıcın alınmasını istediği değer alınamadıysa, kalan değerlerle veritabanına yazmaya çalış
    protected boolean workWithNonParameterConstructor = true;//
    private HashMap<String, UpdateMode> updateModeOfTables;//Tabloların tazeleme modunu belirtiyor.    
    protected boolean bufferMode = false;//Her veri çekme isteğinde veritabanından veri çekilmemesi için verilerin İkiz'de saklandığı bir çalışma şekli
    //private boolean workWithSpecialBuilder = false;//Yalnızca ikizIdare sınıfından bir nesnenin 'specialCode' kullanarak erişebileceği bir inşâcı ile çalış
    //private String specialCode;//Özel inşâcı ile çalışıldığı durumda özel inşâcının ikizIdare için ilgili sınıftan bir değişken üretmesi için gereken kod
    private POLICY_FOR_LIST_MAP_ARRAY methodForListAndMapFields = POLICY_FOR_LIST_MAP_ARRAY.TAKE_AS_JSON;
    
//.;.
    enum POLICY_FOR_LIST_MAP_ARRAY{// Bir sınıftaki liste, harita ve dizi türü verilerin nasıl saklanacağıyla ilgili yapılandırma
        TAKE_AS_JSON,// İlgili veri JSON verisine dönüştürülüp, veritabanında ilgili sütunda metîn olarak saklanır
        //TAKE_LIKE_JSON,// İlgili veri virgül ile ayrılmış şekilde metne dönüştürülüp, veritabanında ilgili sütunda saklanır.
        DONT_TAKE// İlgili veriler sistem tarafından yoksayılır, veritabanına kaydedilmez
    }

    public Confs(){
        
    }

// İŞLEM YÖNTEMLERİ:
    // SINIF FONKSİYONLARI:
    protected static Confs getDefaultConfs(){
        Confs confs = new Confs();
        return setDefault(confs);
    }
    // SINIF ARKAPLAN FONSKSİYONLARI:
    private static Confs setDefault(Confs confs){
        if(confs == null)
            return null;
        confs.takeDateAttributeAsDateTime = true;
        confs.alwaysContinue = true;
        confs.workWithNonParameterConstructor = true;
        confs.bufferMode = false;
        confs.getAttributesPolicy();
        confs.methodForListAndMapFields = POLICY_FOR_LIST_MAP_ARRAY.TAKE_AS_JSON;
        return confs;
    }

    private void setUpdateModeOfTable(String tableName, UpdateMode mode){
        if(!bufferMode)
            return;
        boolean isFound = false;
        for(String table : getUpdateModeOfTables().keySet()){
            if(table.equals(tableName)){
                isFound = true;
                break;
            }
        }
        if(!isFound)
            return;
        UpdateMode currentMode = getUpdateModeOfTables().get(tableName);
        if(currentMode == null)
            return;
    }
    private void setUpdateModeOfTableForAllTables(UpdateMode mode){
        if(mode == null)
            return;
        for(String table : getUpdateModeOfTables().keySet()){
            getUpdateModeOfTables().put(table, mode);
        }
    }
    public void setAlwaysContinue(boolean alwaysContinue){
        this.alwaysContinue = alwaysContinue;
    }
    public void setWorkWithNonParameterConstructor(boolean workWithNonParameterConstructor){
        this.workWithNonParameterConstructor = workWithNonParameterConstructor;
    }
    public void setUpdateModeOfTables(HashMap<String, UpdateMode> updateModeOfTables){
        this.updateModeOfTables = updateModeOfTables;
    }
    public void setBufferMode(boolean bufferMode){
        this.bufferMode = bufferMode;
    }
    public void setAttributesPolicy(boolean takePublicFields, boolean takePrivateFields, boolean takeDefaultFields, boolean takeProtectedFields){
        if(takePrivateFields || takeDefaultFields || takeProtectedFields){
            //Kullanıcıya bu sınıfların getter ve setter yöntemleri içermesi gerektiğini hâtırlat
//            controlGetterAndSetterForHideFields();
        }
        getAttributesPolicy().put("public", takePublicFields);
        attributesPolicy.put("private", takePrivateFields);
        attributesPolicy.put("default", takeDefaultFields);
        attributesPolicy.put("protected", takeProtectedFields);
    }
    public void setTakeDateAttributeAsDateTime(boolean policy){
        this.takeDateAttributeAsDateTime = policy;
    }
    public void setAlwaysContinueAttribute(boolean value){
        this.alwaysContinue = value;
    }
    public void setMethodForListAndMapFields(POLICY_FOR_LIST_MAP_ARRAY methodForListAndMapFields){
        this.methodForListAndMapFields = methodForListAndMapFields;
    }
    
    // ARKAPLAN İŞLEM YÖNTEMLERİ:
    private void setAttributesPolicy(HashMap<String, Boolean> attributesPolicy){
        this.attributesPolicy = attributesPolicy;
    }

// ERİŞİM YÖNTEMLERİ:
    public HashMap<String, Boolean> getAttributesPolicy(){
        if(attributesPolicy == null){
            attributesPolicy = new HashMap<>();
            attributesPolicy.put("public", true);
            attributesPolicy.put("private", true);
            attributesPolicy.put("default", true);
            attributesPolicy.put("protected", true);
        }
        return attributesPolicy;
    }
    public boolean isTakeDateAttributeAsDateTime(){
        return takeDateAttributeAsDateTime;
    }
    public boolean isAlwaysContinue(){
        return alwaysContinue;
    }
    public boolean isWorkWithNonParameterConstructor(){
        return workWithNonParameterConstructor;
    }
    public HashMap<String, UpdateMode> getUpdateModeOfTables(){
        if(updateModeOfTables == null){
            updateModeOfTables = new HashMap<String, UpdateMode>();
        }
        return updateModeOfTables;
    }
    public POLICY_FOR_LIST_MAP_ARRAY getMethodForListAndMapFields(){
        return methodForListAndMapFields;
    }
    
}
/*
    private ArrayList<String> workingTables;
    private boolean bufferMode = false;//Her veri çekme isteğinde veritabanından veri çekilmemesi için verilerin İkiz'de saklandığı bir çalışma şekli
    private HashMap<String, List> bufferTables;
    private HashMap<String, Date> lastUpdateTimeOfTables;//Tabloların en son güncellendiği zamânı belirtiyor.
*/