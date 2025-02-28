package ikiz;

import ReflectorRuntime.Reflector;
import jsoner.JSONReader;
import ikiz.Services.DTService;
import ikiz.Services.Helper;
import jsoner.JSONWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IkizIdare{
    private static IkizIdare ikiz;
    private Cvity connectivity;
    private Confs confs;// IkizSistemi yapılandırma ayarları
    private ArrayList<String> workingTables;
    private HashMap<String, List<Object>> bufferTables;
    private HashMap<String, Date> lastUpdateTimeOfTables;//Tabloların en son güncellendiği zamânı belirtiyor.
    private HashMap<String, UpdateMode> updateModeOfTables;//Tabloların tazeleme modunu belirtiyor.
    private ErrorTable errorTable;//Hatâların yazılması ve gösterilmesiyle ilgili bir sistem
    private final char start, end;// Veritabanı sistemine göre bir özel ismin başlangıç ve sonunu belirten karakter
    private final HashMap<String, String> mapDataTypeToDBDataType;// Sistemin çalıştığı veritabanı sistemine göre veri tipleri eşleştirme haritası
    private HashMap<String, HashMap<String, Field>> mapOfTardggetFields;// Her tablonun "özellik - Java veri tipi" bilgisini burada tut
    private HashMap<String, TableMetadata> metadataOfTables;// Tablolar hakkında bilgiler
    private DBAccessHelper dbAccess;// Veritabanındaki bâzı işlemlerin kolayca yapılması için bir sınıf
    private List<Class> loadedClasses;// Veritabanı tablolarından yapılandırma çıkarılması gerektiği durumda sınıflara ihtiyaç var.
//    private HashMap<String, HashMap<

    private IkizIdare(Cvity connectivity){
        this.connectivity = connectivity;
        this.start = connectivity.getHelperForDBType().getStartSymbolOfName();
        this.end = connectivity.getHelperForDBType().getEndSymbolOfName();
        dbAccess = new DBAccessHelper(connectivity);
        errorTable = new ErrorTable();
        this.confs = Confs.getDefaultConfs();
        this.mapDataTypeToDBDataType = HelperForHelperForDBType.getHelper(this.connectivity.getDBType()).getMapOfDataTypeToDBDataType();
    }

//İŞLEM YÖNTEMLERİ:
    //ANA BAŞLATMA YÖNTEMİ:
    public static boolean startIkizIdare(Cvity connectivity){
        if(connectivity == null)
            return false;
        if(testConnection(connectivity) == false){
            return false;}
        ikiz = new IkizIdare(connectivity);
        return true;
    }
    //SINIF FONKSİYONLARI:
    public static String extractTableName(Class cls){
        if(cls == null)
            return null;
        String[] splitted = cls.getName().split("\\.");
        return splitted[splitted.length - 1];
    }
    public static List<String> getTableNames(Cvity connectivity){
        String showTablesOrder = connectivity.getHelperForDBType().getSentenceForShowTables();
//        System.err.println("sentence : " + showTablesOrder);
        List<String> listOfTables = new ArrayList<String>();
        try{
            ResultSet rs = connectivity.getConnext().createStatement().executeQuery(showTablesOrder);
            if(rs != null){
                while(rs.next()){
                    listOfTables.add(rs.getString(1));
                }
                return listOfTables;
            }
            else
                return null;
        }
        catch(SQLException exc){
            System.err.println("Tablo isimleri alınırken hatâyla karşılaşıldı : " + exc.toString());
        }
        return null;
    }
    public boolean integrateTableToIkiz(String tableName){
        if(tableName == null)
            return false;
        if(tableName.isEmpty())
            return false;
        boolean isIn = Helper.isInTheList(getWorkingTables(), tableName);
        if(isIn)// Tablo zâten sisteme entegre edilmişse işlemi sonlandır;
            return true;
        List<String> listOfTables = getTableNames(connectivity);// Tablo isimlerini al
        Iterator<String> iter = listOfTables.iterator();
        while(iter.hasNext()){
            if(iter.next().equals(tableName)){
                getWorkingTables().add(tableName);// Tabloyu çalışılan tablo isimlerinin arasına ekle
                return true;
            }
        }
        return false;
    }
    public boolean produceTable(Class tableClass){
        return produceTable(tableClass, null);
    }
    public boolean produceTable(Class tableClass, TableConfiguration confsOfTable){
        if(tableClass == null)
            return false;
        String tableName = tableClass.getSimpleName();// Tablo ismi = veritabanı nesnesi (entity) ismi
        StringBuilder query;// Hâzırlanan sorgu
        Field[] fields = tableClass.getDeclaredFields();
        String[] columnNames = new String[fields.length];
        String[] columnTypes = new String[fields.length];
        int takedAttributesCounter = 0;// Alınan özellik sayısı sayacı
        HashMap<String, Field> metadataOnTargetFields = new HashMap<String, Field>();// İkiz için saklanan 'alan ismi - veri tipi' haritası
        // ANA ADIM - 1 : Alan isimlerini ve hedef veri tiplerini belirle:
        for(int sayac = 0; sayac < fields.length; sayac++){
            boolean isTaked = false;// Şu anki döngü çevrimindeki özelliğin alınıp, alınmadığı bilgisini tutuyor
            if(takeThisField(fields[sayac].getModifiers())){// // Kontrol - 1 : Erişim belirteci belirlenen stratejiye uygun mu?
                Class typeOfField = fields[sayac].getType();// Alanın veri tipi alınır
                // Veri tipi kontrolleri:
                if(isBasicType(typeOfField)){// Kontrol - 2.1 : Temel veri tipiyse ilgili alan için verileri al
                    columnNames[takedAttributesCounter] = fields[sayac].getName();
                    columnTypes[takedAttributesCounter] = getTypeNameForDB(fields[sayac].getType().getTypeName());
                    takedAttributesCounter++;
                    isTaked = true;
                }
                else{// Bu alan temel veri tipi değilse;
                    HashMap<String, Boolean> results = isListOrMapOrArray(typeOfField);//Kontrol 2.2 : Liste-harita-dizi olup, olmadığını kontrol et..
                    boolean isArray = results.get("isArray");
                    boolean isMap = results.get("isMap");
                    if(results.get("result")){// Eğer veri tipi List veyâ Map veyâ Array (dizi) ise;
                        if(this.confs.getPolicyForListArrayMapFields() != Confs.POLICY_FOR_LIST_MAP_ARRAY.DONT_TAKE){// Kontrol 2.3 : Bu alanlar 'alınmayacak' olarak işâretlenmemişse;
                            if(this.confs.getPolicyForListArrayMapFields().equals(Confs.POLICY_FOR_LIST_MAP_ARRAY.TAKE_AS_JSON)){// Bu veriler veritabanına JSON olarak kaydedilmek isteniyorsa;
                                columnNames[takedAttributesCounter] = fields[sayac].getName();
                                columnTypes[takedAttributesCounter] = this.connectivity.getHelperForDBType().getDataTypeNameForJSON();
                                takedAttributesCounter++;
                                isTaked = true;
                            }
                        }
                        else{// Bu alan liste veyâ dizi veyâ harita; fakat bu alanlar 'alınmayacak' olarak işâretlenmiş
                            // Bu alanı alma
                        }
                    }
                    else if(typeOfField.isEnum()){// 'enum' biçiminde bir veri tipi ise;
//                        boolean isSupportedEnum = this.getConnectivity().getHelperForDBType().isSupported(Enum.class.getName());
//                        if(isSupportedEnum){
//                            columnTypes[takedAttributesCounter] = "ENUM";// Özel durum !!! : TÜm enum değerlerini parantez içinde belirtmelisin!
//                            takedAttributesCounter++; 
//                        }
//                        else{
//                            
//                        }
                        columnNames[takedAttributesCounter] = fields[sayac].getName();
                        columnTypes[takedAttributesCounter] = getTypeNameForDB(String.class.getTypeName());
                        takedAttributesCounter++;
                        isTaked = true;
                    }
                    else{// Veri tipi kullanıcı tanımlı ise;
//                        if(this.confOfTable.getPolicyForUserDefinedClasses() == ){// v2.0.0 için
//                            
//                        }
                        System.err.println("Üzgünüz! Ikiz henüz doğrudan kullanıcı tanımlı veri tipleriyle çalışamıyor");
                        // Bu alanı alma
                    }
                }
                if(isTaked){
                    metadataOnTargetFields.put(fields[sayac].getName(), fields[sayac]);// Tablo hakkında ön bilgiyi hâzırla
                    if(confsOfTable != null){// Metîn tipindeki veri için özel bir yapılandırma belirtildiyse..
                        if(typeOfField.equals(String.class)){
                            if(confsOfTable.getSpecifiedLength(columnNames[sayac]) != -1 || confsOfTable.getIsDefaultLengthOfStringChanged()){
                                columnNames[sayac] = columnNames[sayac].replaceFirst("500", String.valueOf(confsOfTable.getLengthOfString(columnNames[sayac])));
                            }
                        }
                    }
                }
            }
        }
        System.out.println("tableName : "  + tableName);
        query = new StringBuilder("CREATE TABLE " + start + tableName + end);
            query.append("(");
        for(int sayac = 0; sayac < takedAttributesCounter; sayac++){
            query.append(start).
            append(columnNames[sayac]).append(end).append(" ");
            query.append(columnTypes[sayac]);
            if(confsOfTable != null){
                if(confsOfTable.isNotNull(columnNames[sayac]))// NOT NULL kısıtını ekle
                    query.append(" NOT NULL ");
                Object defValue = confsOfTable.getDefaultValues().get(columnNames[sayac]);// Varsayılan değer kısıtını ekle
                if(defValue != null){
                    query.append(" DEFAULT ? ");
                }
            }
            if(sayac != takedAttributesCounter - 1)
                query.append(", ");
        }
        
        {//KISITLAR(CONSTRAINTS) EKLENECEK: DEFAULT kısıtı yukarıda ekleniyor
            if(confsOfTable != null){// Tablo sınıfı için yapılandırma nesnesi gönderildiyse;
                if(confsOfTable.getClassOfTable().equals(tableClass)){// Gönderilen yapılandırma nesnesinin sınıfı ile tablosu üretilmek istenen sınıf aynı ise;
                    //1. Birincil anahtar ekle:
                    if(confsOfTable.getIsConfSet().get("primaryKey")){// Birincil anahtar ayarı 'belirtildi' olarak işâretliyse
                        query.append(", PRIMARY KEY(").append(start).append(confsOfTable.getPrimaryKey()).append(end).append(")");
                    }
                    for(String field : confsOfTable.getUniqueFields()){
                        query.append(", UNIQUE(").append(start).append(field).append(end).append(")");
                    }
                }
            }
        }
            query.append(");");
        try{
            System.out.println("Gönderilen komut : " + query.toString());
            PreparedStatement prepSt = connectivity.getConnext().prepareStatement(query.toString());// Bu cursor tipinde hatâ veriyor : ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE
            // Varsayılan değerleri koy:
            if(confsOfTable != null){
                int setCounter = 1;
                for(int sayac = 0; sayac < takedAttributesCounter; sayac++){
                    Object value = confsOfTable.getDefaultValues().get(columnNames[sayac]);
                    if(value != null){
                        if(value.getClass().equals(char.class) || value.getClass().equals(Character.class)){
                            prepSt.setObject(setCounter, value, java.sql.JDBCType.CHAR);
                        }
                        else{
                            prepSt.setObject(setCounter, value);
                        }
                        setCounter++;
                    }
                }
            }
            prepSt.execute();
            if(!confirmTableInDB(tableName)){
                System.err.println("Tablo oluşturma işlemi başarısız oldu");
                return false;
            }
            prepSt.clearBatch();
            getLastUpdateTimeOfTables().put(tableName, DTService.getService().getTime());
            getWorkingTables().add(tableName);// Çalışılan tablolara ismini ekle
            TableMetadata tblMetaData = new TableMetadata(tableClass, confsOfTable, metadataOnTargetFields);
            getMetadataOfTables().put(tableName, tblMetaData);
            return true;
        }
        catch(SQLException ex){
            System.out.println("ex.getMessage : (konum : produceTable)" + ex.getMessage());
            return false;
        }
    }
    public boolean addRowToDB(Object entity){
        if(entity == null)
            return false;
        if(!this.confs.alwaysContinue){
            if(!controlGetterAndSetterForHideFields(entity.getClass(), true)){
                System.out.println("Gizli alan(lar)ın getter yöntemi bulunamadı; ikiz yapılandırma ayarında eksik değerlerle işleme devâm etme kapalı olduğu için işlem sonlandırılıyor");
                return false;
            }
        }
        String tableName = entity.getClass().getSimpleName();
        StringBuilder query = new StringBuilder("INSERT INTO " + tableName + "(");
        Map<String, Field> metadata = getMapOfTargetFields(entity.getClass().getSimpleName());
        Field[] taked = new Field[metadata.values().size()];
        metadata.values().toArray(taked);
        int takedAttributeNumber = metadata.size();
        for(Field fl : taked){
            query.append(fl.getName());
            query.append(", ");
        }
        query.delete(query.length() - 2, query.length());// Sona eklenen fazladan ", " karakterlerini sil
        query.append(") VALUES (");
        for(int sayac = 0; sayac < takedAttributeNumber; sayac++){
            query.append("?");
            query.append(", ");
        }
        query.delete(query.length() - 2, query.length());// Sona eklenen fazladan ", " karakterlerini sil
        query.append(");");
        System.out.println("hâzırlanan sorgu cümlesi : " + query.toString());
        PreparedStatement preparing = null;
        
        try{
            preparing = connectivity.getConnext().prepareStatement(query.toString());
        }
        catch(SQLException exc){
            System.err.println("Hatâ, sorgu cümlesi hâzırlama yapısı üretilemedi : " + exc.toString());
            return false;
        }
        Object value;// Nesnenin nesne olarak alınmış hâli (bu durumda liste, dizi, harita da olabilir)
        int writedAttribute = 0;
        for(int sayac = 0; sayac < taked.length; sayac++){
            Object pureValue = null;
            value = null;// Special özelliğe göre alım yap!
            try{
                pureValue = taked[sayac].get(entity);
            }
            catch(IllegalArgumentException ex){
                System.out.println("Hatâ, geçersiz değer : " + ex.getMessage());
            }
            catch(IllegalAccessException exc){
                try{
                    Method getter = entity.getClass().getMethod(Reflector.getService().
                            getMethodNameDependsCodeStyle(taked[sayac].getName(),
                                    this.confs.codingStyleForGetterSetter, Reflector.METHOD_TYPES.GET), null);
                    try{
                        pureValue = getter.invoke(entity, null);
                    }
                    catch(IllegalAccessException exOnInvokingGetter){
                        System.out.println("Hatâ, yetkisiz erişim...");
                        if(!this.confs.alwaysContinue){
                            callIsntCompleted("AddRowToDB", entity, tableName);
                        }
                        value = null;
                    }
                    catch (IllegalArgumentException excFromGetterHaveParameter){
                        System.out.println("Hatâ, geçersiz değer...");
                        return false;
                    }
                    catch(InvocationTargetException excFromTarget){
                        System.out.println("Hatâ, yöntem çalıştırılmasıyla ilgili...");
                        return false;
                    }
                }
                catch(NoSuchMethodException excOnFindingGetter){
                    System.out.println("Hatâ, erişim belirtecinden dolayı okunamayan alan bilgisi için get yöntemi eksik gibi!");
                    if(!this.confs.alwaysContinue)
                        return false;
                    value = null;
                }
                catch(SecurityException excAboutSecurity){
                    System.out.println("Hatâ, güvenlikle alâkalı...");
                }
//                System.err.println("hatâ, yetkisiz erişim : " + ex.getMessage());
            }
            HashMap<String, Boolean> analysis = isListOrMapOrArray(taked[sayac].getType());
            if(analysis.get("result")){
                 value = getJSONStringFromObject(pureValue);// İlgili nesne verisini JSON metni olarak al
            }
            else if(taked[sayac].getType().isEnum())
                value = pureValue.toString();
            else// Çoklu veri barındıran bir veri tipi ve 'enum' değilse;
                value = pureValue;
            try{
                if(taked[sayac].getType().equals(char.class) || taked[sayac].getType().equals(Character.class)){// JConnector'un bir hatâ ('bug') sebebiyle bu şart yazıldı: https://bugs.mysql.com/bug.php?id=59456
                    preparing.setObject(sayac + 1, value, java.sql.JDBCType.CHAR);
                }
                else
                    preparing.setObject(sayac + 1, value);
            }
            catch(SQLException ex){
                System.err.println("Hatâ, veri tipi eklenmesi sorunu, sanırım");
                return false;
            }
        }
        query.append(");");
        try{
            return (preparing.executeUpdate() > 0);
        }
        catch (SQLException ex){
            System.err.println("Hatâ, sorgu çalıştırılmadı : " + ex.getMessage());
            return false;
        }
    }
    public boolean deleteTable(Class cls){// Hangi sınıfla ilişkili tablo silinmek isteniyorsa parametre olarak verilmelidir.
        boolean tableDetected = false;// Tablo üzerinde çalışılan bir tablo ise 'true' olmalıdır
        Iterator<String> iter = getWorkingTables().iterator();
        String tableName = extractTableName(cls);
        while(iter.hasNext()){
            if(iter.next().equals(tableName))
                tableDetected = true;
        }
        if(tableDetected){
            String delOrder = "DROP TABLE " + tableName;
            try{
                this.connectivity.getConnext().createStatement().execute(delOrder);
            }
            catch(SQLException exc){
                System.err.println("Tablo silinirken hatâ oluştu : " + exc.toString());
            }
        }
        return false;
    }
    public void setNullToCol(){//GEÇİCİ FONKSİYON, SONRA SİL
        String sql = "UPDATE testSinifi SET name = null WHERE name=\"boş\"";
        try {
            boolean isSuccess = getConnectivity().getConnext().createStatement().execute(sql);
            System.out.println("Başarılı işlem");
        } catch (SQLException ex) {
            System.err.println("Başarısız işlem!");
        }
    }
    public <T> List<T> getData(Class<T> target){
        return getData(target, null);
    }
    public <T> List<T> getData(Class<T> target, List<String> fieldsNames){
        return (List<T>) getData(target, fieldsNames, true);
    }
    public List<Map<String, Object>> getValuesFromTable(Class<?> target, List<String> fieldNames){
        Object obj = getData(target, fieldNames, false);
        return (obj == null ? null : (List<Map<String, Object>>) obj);
    }
    private <T> List<Object> getDataForOneField(Class<?> target, String field, Class<T> classOfField, boolean cast){
        if(field == null)
            return null;
        List<String> names = new ArrayList<String>();
        names.add(field);
        Object result = getValuesFromTable(target, names);
        if(result != null){
            List<Map<String, Object>> asListOfCast = ((List<Map<String, Object>>) result);
            List<T> values = new ArrayList<T>();
            List<Object> notCastedValues = new ArrayList<Object>();
            for(Map row : asListOfCast){
                Object fieldValue = row.values().iterator().next();
                if(!cast){// Veri dönüştürülmeyecekse dönüştürmeye çalışma
                    notCastedValues.add(fieldValue);
                    continue;
                }
                if(fieldValue != null){
                    try{
                        T casted = classOfField.cast(fieldValue);
                        values.add(casted);
                    }
                    catch(ClassCastException exc){
                        System.err.println("exc. : : " + exc.toString());
                    }
                }
            }
            if(cast)
                return (List<Object>) values;
            else
                return notCastedValues;
        }
        return null;
    }
    /**
     * Veri dönüştürülür ('casting') ve {@code null} veriler getirilmez.
     * @param <T>
     * @param target
     * @param field
     * @param classOfField
     * @return 
     */
    public <T> List<T> getDataForOneField(Class<?> target, String field, Class<T> classOfField){
        return (List<T>) getDataForOneField(target, field, classOfField, true);
    }
    /**
     * Veri dönüştürme ('cast') yapılmaz ve {@code null} veriler de getirilir
     * @param target
     * @param field
     * @return 
     */
    public List<Object> getDataForOneField(Class<?> target, String field){
        return getDataForOneField(target, field, null, false);
    }
    public <T> T getDataById(Class<T> target, Object primaryKeyValue){// İşlem hızı açısından 'List<T> getDataByField' yöntemini tercih etmiyorum.
        if(target == null)
            return null;
        if(primaryKeyValue == null)
            return null;
        String tableName = target.getSimpleName();
        TableMetadata md = getMetadataOfTable(tableName);
        T value = null;
        try{
            TableConfiguration confOfTable = md.getConfs();
            if(confOfTable.getIsConfSet().get("primaryKey")){
                String primaryKey = confOfTable.getPrimaryKey();
                List<T> rows = getDataByField(target, primaryKey, primaryKeyValue);
                if(rows != null)
                    return rows.get(0);
            }
        }
        catch(NullPointerException exc){
            System.err.println("NullPointer exc : " + exc.toString());
        }
        return value;
    }
    public <T> List<T> getDataByField(Class<T> target, String fieldName, Object valueForTheGivenField){
        if(target == null || fieldName == null)
            return null;
        String tableName = target.getSimpleName();
        TableMetadata md = getMetadataOfTable(tableName);
        List<T> values = null;
        try{
            if(md.getMapOfTargetFields().get(fieldName) != null){
                List<Map<String, Object>> dataOfRecords = dbAccess.getDataForOneWhereCondition(tableName, null, fieldName, valueForTheGivenField);
                values = new ArrayList<T>();
                List<Field> specialFields = new ArrayList<Field>();
                for(String s : md.getMapOfTargetFields().keySet()){
                    Field fl = md.getMapOfTargetFields().get(s);
                    Map<String, Boolean> analysis = isListOrMapOrArray(fl.getType());
                    if(analysis.get("result")){
                        specialFields.add(fl);
                    }
                }
                for(Map<String, Object> row : dataOfRecords){
                    for(Field special : specialFields){
                        Object specialData = row.get(special.getName());
                        if(specialData != null){
                            row.put(special.getName(), convertJSONtoTarget(String.valueOf(specialData), special));
                        }
                    }
                    values.add(Reflector.getService().pruduceNewInjectedObject(target, row, this.confs.codingStyleForGetterSetter));
                }
            }
        }
        catch(NullPointerException exc){
            System.err.println("NullPointer exc : " + exc.toString());
        }
        return values;
    }
    /*
    EKLE:
        LIMITLİ VERİ ÇEKME (VERİ ÇEKİLİRKEN DE LİMİTLİ OLMALI),
        ŞARTLI VERİ ÇEKME (WHERE İFÂDESİYLE..)
        
    */
    private <T> Object getData(Class<T> target, List<String> fieldNames, boolean getAsObject){//Tablodaki verilerin büyük olması durumunda verilerin tamâmını almak için bunları bigint gibi sayılarda tutmalıyız
        if(target == null)
            return null;
        if(fieldNames != null)
            if(fieldNames.isEmpty())
                return null;
        String tableName = target.getSimpleName();
        if(this.confs.bufferMode){// Veri tazeleme modu, burada farklılaştırılabilir. Misal, veri istendiğinde belirli bir süredir tazeleme olmadıysa veriyi çek, diğer durumda önbellekteki veriyi getir gibi
            // İlâve lazım..
            List<Object> data = getDataFromBuffer(tableName);
            if(data != null)
                return data;
        }
        if(fieldNames == null){// Eğer sütun isimleri verilmediyse tüm sütun isimlerini al
            fieldNames = new ArrayList<String>();
            for(String s : getMapOfTargetFields(tableName).keySet()){
                fieldNames.add(s);
            }
        }
        boolean keepGo = confirmTableInDB(tableName);// Önce tablonun olup, olmadığına bakılıyor; bu, gereksiz bir işlem sayılabilir. Performansı arttırmak için sorgudan dönen hatâ sonucunu ele al
        if(!keepGo){
            System.err.println("İlgili tablo veritabanında olmadığından işlem sonlandırıldı!");
            return null;
        }
        List<Map<String, Object>>  liDataOfObjectsBeforeInstantiation = dbAccess.getData(tableName, fieldNames);
        if(!getAsObject)
            return liDataOfObjectsBeforeInstantiation;
        //ELDEKİLER:
            //liDataOfObjectsBeforeInstantation : Her bir eleman için özellik, değer çifti içeren harita listesi, tipi : ArrayList<HashMap<String, Object>>
            //Yapılması gereken 1 : Öncelikle parametresiz bir yapıcı fonksiyon ara
            //Yapılması gereken 2 : Eğer parametresiz yapıcı fonksiyon yoksa, ikizIdare parametresiyle çalışan yapıcı fonksiyon aranabilir,
                //bu özellik sonra eklenecek, bi iznillâh
            //Yapılması gereken 3 : Sınıfın tüm yapıcı fonksiyonlarını al
            //Yapılması gereken 4 : Eldeki <özellik, değer> çiftleriyle hangi eleman için hangi yapıcı fonksiyonun kullanılmasının
                //uygun olduğunu tespit edip, o yapıcı yöntemi kullanan
                //bir yönteme <özellik, değer> çiftlerini ve yapıcı fonksiyonları gönder
            //Yapılması gereken 5 : İlgili yöntemden gelen nesneyi listeye ekle
            //Sınıfın yapıcı fonksiyonları (constructors) (css):
        Constructor[] css = target.getConstructors();
        Constructor noParamCs = null;
        for(Constructor cs : css){
            if(cs.getParameterCount() == 0){
                noParamCs = cs;
                break;
            }
        }
        List<T> liData = new ArrayList<T>();
        //Parametresiz yapıcı fonksiyonla elemanları üret:
        if(noParamCs != null){
            for(Map<String, Object> mapOfAttributesOfRow : liDataOfObjectsBeforeInstantiation){
                Object instance;
                try{
                    instance = noParamCs.newInstance(null);
                }
                catch(InstantiationException ex){
                    System.err.println("Parametresiz yöntemle değişken üretilirken hatâ :\n\t-->   " + ex.getMessage());
                    return null;
                }
                catch(IllegalAccessException ex){
                    System.err.println("Parametresiz yöntemle değişken üretilirken hatâ :\n\t-->   " + ex.getMessage());
                    return null;
                }
                catch(IllegalArgumentException ex){
                    System.err.println("Parametresiz yöntemle değişken üretilirken hatâ :\n\t-->   " + ex.getMessage());
                    return null;
                }
                catch(InvocationTargetException ex){
                    System.err.println("Parametresiz yöntemle değişken üretilirken hatâ :\n\t-->   " + ex.getMessage());
                    return null;
                }
                liData.add(assignAttributes(target, mapOfAttributesOfRow));// Verileri ilgili alanlara zerk et
            }
//            System.out.println("Üretilen değişkenin sınıf ismi : " + data[0].getClass().getName());
//            System.out.println("Veri tipi dönüşümü yapılmış değişkenin sınıf ismi : " + target.cast(data[0]).getClass().getName());
            return liData;
        }
        //Eğer parametresiz yapıcı fonksiyon yoksa:
        //.;.
        if(this.confs.workWithNonParameterConstructor && css[0].getParameterCount() != 0){
                System.out.println("Parametresiz yapıcı yöntemle çalışma etkin; lâkin sınıfın böyle bir kurucu yöntemi yok!");
                return null;
        }
        //.;.
        // Uygun yapıcı yöntemi seç, nesneleri oluştur ve ata
        //GEÇİCİ:
        return null;
    }
    public void test(String tableName){
        Statement st = null;
        ResultSet result = null;
        try{
            st = this.getConnectivity().getConnext().createStatement();// Hatâ veriyor : ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE
            result = st.executeQuery("SHOW TABLES");
            while(result.next()){
                String name = result.getString(1);
                System.out.println("tablo ismi : " + name);
            }
        }
        catch(SQLException ex){
            System.out.println("hatâ : " + ex.getMessage());
        }
        
        //DENEME - 2:
        try{
            st = this.getConnectivity().getConnext().createStatement();// Hatâ veriyor : ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE
            result = st.executeQuery("SELECT * FROM " + tableName);
//            System.out.println("resultSet yapılandırma bilgileri : " + analysis.getMetaData().toString());
            while(result.next()){
                int sayac = 1;
                for(;sayac < result.getMetaData().getColumnCount() + 1; sayac++){
                    Object data = result.getObject(sayac);
                    result.getObject(sayac);
                    if(data != null)
                        System.out.println("data.getClass().getName() : " + data.getClass().getName());
                }
                System.out.println("Tablodaki sütun sayısı : " + sayac);
            }
        }
        catch(SQLException ex){
            System.out.println("hatâ : " + ex.getMessage());
            return;
        }
    }
    public boolean updateRowInDB(Object entity){//Belli bir alanının tazelenmesi istenmiyorsa tüm alanları tazele
        return updateRowInDB(entity, null);
    }
    public boolean updateRowInDB(Object entity, List<String> fieldNames){//Belli bir alanının tazelenmesi istenmiyorsa tüm alanları tazele
        if(entity == null)
            return false;
        String tableName = entity.getClass().getSimpleName();
        TableMetadata md = getMetadataOfTable(tableName);
        TableConfiguration tblConfs = md.getConfs();
        boolean applyForceUpdate = false;// Veriyi zor yoldan bulmak gerekiyorsa 'true' olmalı
        if(fieldNames == null){
            fieldNames = new ArrayList<String>();
            for(String s : md.getMapOfTargetFields().keySet())
                fieldNames.add(s);
        }
        Map<String, Object> mapOfData = getValueOfFieldsAsConverted(entity, fieldNames);
        if(tblConfs != null){
            String primary = tblConfs.getPrimaryKey();
            if(primary != null){
                Object valueOfPrimary = getValueOfField(entity, md.getMapOfTargetFields().get(primary));
                if(valueOfPrimary != null){
                    // Tazelenme süreci, önbellek tazelenmesi ...
                    return dbAccess.updateRow(tableName, primary, valueOfPrimary, mapOfData);
                }
                else{
                    return false;// Birincil anahtar alanı boş olamaz
                }
            }
            else{// NOT NULL ve UNIQUE olan alanlara bakmalısın
                String usableField = "";
                for(String s : tblConfs.getUniqueFields()){// NOT NULL + UNIQUE
                    if(tblConfs.isNotNull(s)){
                        usableField = s;
                        break;
                    }
                    else if(getValueOfFieldAsConverted(entity, md.getMapOfTargetFields().get(s)) != null){// UNIQUE ve nesnenin o özelliği NULL değil
                        usableField = s;
                    }
                }
                if(!usableField.isEmpty()){// NOT NULL + UNIQUE bir alan varsa veyâ bu nesnenin değeri alan bir UNIQUE alanı varsa;
                    return dbAccess.updateRow(tableName, usableField, getValueOfFieldAsConverted(entity, md.getMapOfTargetFields().get(usableField)), mapOfData);
                }
            }
        }
        else
            applyForceUpdate = true;
        if(applyForceUpdate){
            System.err.println("Bu sürümde birincil anahtarı veyâ en azından NULL olmayan münferid değeri bulunmayan tablolardan kayıt tazelenemiyor");
            return false;
        }
        return false;
    }
    public boolean deleteRowFromDB(Object entity){
        if(entity == null)
            return false;
        String tableName = entity.getClass().getSimpleName();
        if(!isInDB(tableName))
            return false;// İlgili verinin tutulduğu bir tablo yok
        TableMetadata md = getMetadataOfTable(tableName);
        TableConfiguration tblConfs = md.getConfs();
        boolean applyForceDelete = false;
        if(tblConfs != null){
            // 1) Birincil anahtar üzerinden veriyi silmeye çalış
            // 2) Münferid alan üzerinden veriyi silmeye çalış (önce NULL olmayan münferid alana bak)
            String primaryKey = tblConfs.getPrimaryKey();
            Field primary = md.getMapOfTargetFields().get(primaryKey);
            StringBuilder sqlOrder = new StringBuilder();
            if(primary != null){
                Object valueOfPrimary = getValueOfFieldAsConverted(entity, primary);
                if(valueOfPrimary != null){
                    return dbAccess.deleteRow(tableName, primaryKey, valueOfPrimary);
                }
                else
                    return false;// Böyle bir dallanmaya düşülmemesi lazım normal işleyişte; birincil anahtar NULL ise ne yapılabilir?
            }
            else{// NOT NULL ve UNIQUE kısıtını berâber taşıyan sütun varsa, onları kullanarak silmeye çalış
                String usableField = "";
                for(String s : tblConfs.getUniqueFields()){// NOT NULL + UNIQUE
                    if(tblConfs.isNotNull(s)){
                        usableField = s;
                        break;
                    }
                    else if(getValueOfField(entity, md.getMapOfTargetFields().get(s)) != null){// UNIQUE ve nesnenin o özelliği NULL değil
                        usableField = s;
                    }
                }
                if(!usableField.isEmpty()){// NOT NULL + UNIQUE bir alan varsa veyâ bu nesnenin değeri alan bir UNIQUE alanı varsa;
                    return dbAccess.deleteRow(tableName, usableField, getValueOfFieldAsConverted(entity, md.getMapOfTargetFields().get(usableField)));
                }
            }
        }
        else{
            applyForceDelete = true;
        }
        if(applyForceDelete){
            // Zor yoldan silmeye çalış, veriyi tespit etmeye çalışarak dene!
            System.err.println("Bu sürümde birincil anahtarı veyâ en azından NULL olmayan münferid değeri bulunmayan tablolardan kayıt silinemiyor");
            return false;
        }
        return false;
    }
    public String readLastError(){
        return getErrorTable().readError();
    }
    public ArrayList<String> readAllErrors(){
        ArrayList<String> all = new ArrayList<String>();
        boolean go = true;
        while(go){
            String text = getErrorTable().readError();
            if(text == null){
                go = false;
                 break;
            }
            all.add(text);
        }
        return all;
    }
    public boolean isInDB(String tableName){
        for(String s : getWorkingTables()){
            if(s.equals(tableName))
                return true;
        }
        return false;
    }
    //ARKAPLAN İŞLEM YÖNTEMLERİ:
    private static boolean testConnection(Cvity connectivity){
        if(connectivity == null)
            return false;
           if(Cvity.getTableNamesOnDB(connectivity.getConnext(), connectivity.getDBType()) == null)// Eğer veritabanında hiç tablo yoksa sistemi başlatma
               return false;
        return true;
    }
    private boolean takeThisField(int modifier){
        String strModifier = "";
        switch(modifier){
            case 0 :{
                strModifier = "default";
                break;
            }
            case 1 :{
                strModifier = "public";
                break;
            }
            case 2 :{
                strModifier = "private";
                break;
            }
            case 4 :{
                strModifier = "protected";
                break;
            }
        }
        if(strModifier == "")
            return false;
        return this.confs.getAttributesPolicy().get(strModifier);
    }
    //ARKAPLAN İŞLEM YÖNTEMLERİ:
     private String getTypeNameForDB(String typeName){// Java veri tipini alır; seçilen veritabanı için uygun veri tipini döndürür
        String dType = mapDataTypeToDBDataType.get(typeName);
        return (dType != null ? dType : "");
    }
    private boolean controlGetterAndSetterForHideFields(Class cls, boolean searchForJustGetter){
        Map<String, Field> map = getMapOfTargetFields(cls.getSimpleName());
        for(String name : map.keySet()){
            Field fl = map.get(name);
            if(fl.getModifiers() == 0 || fl.getModifiers() == 2 || fl.getModifiers() == 4){
                try{
                    String methodNameOfGET = Reflector.getService().getMethodNameDependsCodeStyle(name, this.confs.codingStyleForGetterSetter, Reflector.METHOD_TYPES.GET);
                    String methodNameOfSET = Reflector.getService().getMethodNameDependsCodeStyle(name, this.confs.codingStyleForGetterSetter, Reflector.METHOD_TYPES.GET);
                    Method getter = cls.getDeclaredMethod(methodNameOfSET, null);
                    Method setter = null;
                    if(!searchForJustGetter)
                        setter = cls.getDeclaredMethod(methodNameOfGET, null);
                    if(getter != null){
                        if(searchForJustGetter ? true : (setter != null))
                            return true;
                    }
                }
                catch(NoSuchMethodException | SecurityException exc){
                    System.err.println("exc : " + exc.toString());
                }
            }
        }
        return true;
    }
    private void callIsntCompleted(String callFrom, Object entity, String tableName){
        System.out.println(callFrom + " yöntemi çalıştırılırken oluştu;\n" + tableName + "isimli nesneye erişim sağlanamadı;\n");
    }
    private <T> T assignAttributes(Class<T> cls, Map<String, Object> mapAttributes){//Hatâlardan sonra nasıl idâre edildiğiyle ilgili bir şey yok
        mapAttributes = convertJSONTextToMapOfVariables(mapAttributes, extractTableName(cls));
        T instance = Reflector.getService().pruduceNewInjectedObject(cls, mapAttributes, this.confs.codingStyleForGetterSetter);
        return instance;
    }
    private Map<String, Object> convertJSONTextToMapOfVariables(Map<String, Object> map, String tableName){
        if(map == null)
            return null;
        Map<String, Field> mapOfFields = getMapOfTargetFields(tableName);
        for(Field fl : mapOfFields.values()){
            HashMap<String, Boolean> analysis = isListOrMapOrArray(fl.getType());
            if(analysis.get("result")){
                Object value = map.get(fl.getName());
                if(value != null){
                    if(value instanceof String){
                        Object newValue = null;
                        if(analysis.get("isArray") || analysis.get("isList")){
                            List<Object> listOfData = JSONReader.getService().readJSONArray((String) value);
                            if(analysis.get("isArray")){
                                newValue = Reflector.getService().produceNewArrayInjectDataReturnAsObject(fl.getType(), listOfData);
                            }
                            else if(analysis.get("isList")){
                                newValue = Reflector.getService().produceNewInjectedList(listOfData);
                            }
                        }
                        else if(analysis.get("isMap")){
                            Map<String, Object> mapOfData = JSONReader.getService().readJSONObject((String) value);
                            newValue = mapOfData;
                        }
                        //.;.
                        map.put(fl.getName(), newValue);
                    }
                }
            }
            // Sonraki sürümlerde kullanıcı tanımlı veri tipleri için de dönüşüm eklenmesi gerekebilir.
        }
        return map;
    }
    private List<Object> getDataFromBuffer(String tableName){
        if(!this.confs.bufferMode){
            System.err.println("veri saklama modu pasif durumda");
            //Hatâ - uyarı fırlat
            return null;
        }
        if(bufferTables == null){
            System.err.println("Veri saklama alanı boş");
            //Hatâ - uyarı fırlat
            return null;
        }
        //.;.
        return bufferTables.get(tableName);
    }
    private void refreshDataOnLocalArea(List data){//Veri saklama modu (bufferMode) etkin ise verileri verilerin saklandığı yerel alandaki ilgili yeri güncelle
        if(!this.confs.bufferMode)
            return;
        String tableName = data.getClass().getName();
        System.out.println("data.getClass().getName");
        getBufferTables().put(tableName, data);
    }
    private boolean removeDataOnLocalArea(Object[] data){//Veri saklama modu açıkken bir tablo silindiğinde eğer o tablodaki kayıtlar saklanıyor idiyse, onları sil
        if(!this.confs.bufferMode)
            return false;
        String tableName = data.getClass().getName();
        if(getBufferTables().get(tableName) == null){
            System.err.println("İlgili tablo verisi veri saklama alanına eklenmemiş");
            return false;
        }
        getBufferTables().remove(tableName);
        return true;
    }
    private boolean isBasicType(Class type){
        return isBasicType(type.getName());
    }
    private boolean isBasicType(String nameOfClass){
        for(String str : this.mapDataTypeToDBDataType.keySet()){
            if(str.equals(nameOfClass))
                return true;
        }
        return false;
    }
    private HashMap<String, Boolean> isListOrMapOrArray(Class cls){
        HashMap<String, Boolean> res = new HashMap<String, Boolean>();
        res.put("result", Boolean.FALSE);
        res.put("isArray", Boolean.FALSE);
        res.put("isList", Boolean.FALSE);
        res.put("isMap", Boolean.FALSE);
        if(cls.isArray()){
            res.put("result", Boolean.TRUE);
            res.put("isArray", Boolean.TRUE);
            return res;
        }
        try{
            Class casted = cls.asSubclass(List.class);
            res.put("result", Boolean.TRUE);
            res.put("isList", Boolean.TRUE);
        }
        catch(Exception exc){// Eğer List'e dönüştürme işlemi başarısız olursa Map'e dönüştürmeye çalış
            //System.err.println("Hatâ (IkizIdare.isListOrMapOrArrayType()) : " + exc.toString());
            try{
                Class otherCasted = cls.asSubclass(Map.class);
                res.put("result", Boolean.TRUE);
                res.put("isMap", Boolean.TRUE);
            }
            catch(Exception inExc){
                System.out.println("Hatâ (IkizIdare.isListOrMapOrArrayType().catch()) : " + inExc.toString());
            }
        }
        return res;
    }
    private boolean canTakeableThisGenericTypeOfField(String genericTypeName){// Kullanılmıyor
        if(genericTypeName == null)
            return false;
        if(genericTypeName.isEmpty())
            return false;
        return isBasicType(genericTypeName);
    }
    private boolean confirmTableInDB(String tableName){// Büyük küçük harf hassas değil
        try{
            String sql = HelperForHelperForDBType.getHelper(this.connectivity.getDBType()).getSentenceForShowTables();
            ResultSet rs = connectivity.getConnext().createStatement().executeQuery(sql);
            if(rs == null)
                return false;
            while(rs.next()){
                if(rs.getString(1).equalsIgnoreCase(tableName))
                    return true;
            }
        }
        catch(SQLException exc){
            System.err.println("Hatâ (confirmTableInDB)");
        }
        return false;
    }
    private Class loadAndGetClass(String typeName){
        Class cls = null;
        switch(typeName){
            case "int" :{
                cls = int.class;
                break;
            }
            case "double" :{
                cls = double.class;
                break;
            }
            case "float" :{
                cls = float.class;
                break;
            }
            case "char" :{
                cls = char.class;
                break;
            }
            case "byte" :{
                cls = byte.class;
                break;
            }
            case "boolean" :{
                cls = boolean.class;
                break;
            }
            case "short" :{
                cls = short.class;
                break;
            }
            case "long" :{
                cls = short.class;
                break;
            }
        }
        if(cls != null)
            return cls;
        try{
            cls = ClassLoader.getSystemClassLoader().loadClass(typeName);
        }
        catch(ClassNotFoundException exc){
            System.err.println("Yüklenmek istenen sınıf bulunamadı : " + exc.toString());
        }
        return cls;
    }
    public/*private yap*/ String getJSONStringFromObject(Object obj){// Verilen nesne için JSON String üret; JSON'dan bir farkı var: anahtarlar String olmak zorunda değil
        if(obj == null)
            return null;
        Class dType = obj.getClass();
        if(dType == String.class)// Veri tipi metîn ise;
            return "\"" + String.valueOf(obj) + "\"";
        JSONWriter jsonWrt = new JSONWriter();
        String result = null;
        result = jsonWrt.produceText(null, obj);// dizi veyâ harita veyâ liste için JSON metni üret
        System.out.println("Üretilen metîn:\n" + result);
        return result;
    }
    public <T, V> HashMap<T, V> produceMap(T[] columnNames, V[] columnTypes){
        HashMap<T, V> value = new HashMap<T, V>();
        for(int sayac = 0; sayac < columnNames.length; sayac++){
            if(columnTypes[sayac] != null){
                value.put(columnNames[sayac], columnTypes[sayac]);
            }
        }
        return value;
    }
    public static void printMap(Map map){
        if(map == null){
            System.out.println("null");
            return;
        }
        for(Object key : map.keySet()){
            System.out.println(key + " : " + map.get(key));
        }
    }
    private Object getValueOfField(Object entity, Field field){
        Map<String, Object> result = Reflector.getService().getValueOfFields(entity, new Field[]{field}, this.confs.codingStyleForGetterSetter);
        return (result != null ? result.get(field.getName()) : null);
    }
    private Object getValueOfFieldAsConverted(Object entity, Field field){
        List<String> list = new ArrayList<String>();
        list.add(field.getName());
        return getValueOfFieldsAsConverted(entity, list).get(field.getName());
    }
    private Map<String, Object> getValueOfFields(Object entity, Field[] fields){
        return Reflector.getService().getValueOfFields(entity, fields, this.confs.codingStyleForGetterSetter);
    }
    private Map<String, Object> getValueOfFields(Object entity, List<String> fieldNames){
        return Reflector.getService().getValueOfFields(entity, fieldNames, this.confs.codingStyleForGetterSetter);
    }
    private Map<String, Object> getValueOfFieldsAsConverted(Object entity, List<String> fieldNames){
        Map<String, Object> pureValues = getValueOfFields(entity, fieldNames);
        for(String key : pureValues.keySet()){
            Object value = pureValues.get(key);
            if(value == null)// Değer null ise veri tipi tespitine ihtiyaç yok / yapılamaz.
                continue;
            if(!isBasicType(value.getClass())){
                Map<String, Boolean> analysis = isListOrMapOrArray(value.getClass());
                if(analysis.get("result")){// Dizi, liste veyâ harita verisi ise;
                    if(this.confs.getPolicyForListArrayMapFields() != Confs.POLICY_FOR_LIST_MAP_ARRAY.DONT_TAKE){
                        JSONWriter wrt = new JSONWriter();
                        String jsonText = wrt.produceText(null, pureValues.get(key));
                        pureValues.put(key, jsonText);
                    }
                    else
                        pureValues.remove(key);
                }
                else if(value.getClass().isEnum()){
                    //.;. = Doğrudan enum değer verilebilir mi?
                }
                else{
                    System.err.println("Bu sürümde Kullanıcı tanımlı özellik desteği bulunmuyor.");
                }
            }
        }
        return pureValues;
    }
    public boolean loadSystemConfsFromAnalyzingDB(){
        // İkiz yapılandırma tablosunun veri olarak tespîtinin önlenmesini kodla
//        String dbName = this.connectivity.getSchemaName();
        List<String> tables = dbAccess.getTableNames();
        if(tables == null){
            System.err.println("Veritabanı analizi başarısız oldu.");
            return false;
        }
        getWorkingTables().addAll(tables);// İşlem - 1 : Çalışılan veritabanı isimlerini kaydet
        // İşlem - 2 : Tablo önbilgilerini tespit et ve kaydet;
        for(String tbl : getWorkingTables()){
            List<String> fieldNames = dbAccess.getFieldNames(tbl);
            Class cls = getSuitableClassOnTheList(tbl, fieldNames);
            HashMap<String, Field> mapOfTargets = extractMapOfTargetFields(fieldNames, cls);
            TableConfiguration tblConfs = getConfiguresOfTableFromDB(cls);
            TableMetadata md = new TableMetadata(cls, tblConfs, mapOfTargets);
            getMetadataOfTables().put(cls.getSimpleName(), md);// Ayarları İkiz'e aktar
        }
        return true;
    }
    /**
     * Yüklenen sınıf listesi içerisinden verilen basit isimdeki sınıfı getirir
     * Aynı isimde birden fazla sınıf varsa, verilen alan isimlerine bakılır
     * Bu durumda verilen alan isimleriyle en çok uyuşan sınıf döndürülür
     * 
     * @param simpleNameOfClass
     * @param fieldNames
     * @return 
     */
    private Class getSuitableClassOnTheList(String simpleNameOfClass, List<String> fieldNames){// Aynı isimde birden fazla sınıf varsa, uygun olanını getirmek için..
        if(loadedClasses == null)
            loadedClasses = Reflector.getService().getClassesOnTheAppPath();
        List<Class> found = new ArrayList<Class>();
        for(Class cls : loadedClasses){
            if(cls.getSimpleName().equalsIgnoreCase(simpleNameOfClass)){
                found.add(cls);
            }
        }
        if(found.isEmpty())
            return null;
        if(found.size() == 1)
            return found.get(0);
        else{
            Class[] clss = new Class[found.size()];
            found.toArray(clss);
            int[] pairedFieldNumber = new int[clss.length];
            for(int sayac = 0; sayac < clss.length; sayac++){
                pairedFieldNumber[sayac] = 0;
                for(String s : fieldNames){
                    try{
                        Field fl = clss[sayac].getField(s);
                        if(fl != null)
                           pairedFieldNumber[sayac]++;
                    }
                    catch(NoSuchFieldException | SecurityException exc){
                        System.err.println("exc : " + exc.toString());
                    }
                }
            }
            int max = pairedFieldNumber[0];
            int indexOfMaxPaired = 0;
            for(int sayac = 1; sayac < clss.length; sayac++){
                if(pairedFieldNumber[sayac] > max){
                    max = pairedFieldNumber[0];
                    indexOfMaxPaired = sayac;
                }
            }
            return clss[indexOfMaxPaired];
        }
    }
    private HashMap<String, Field> extractMapOfTargetFields(List<String> fieldNames, Class targetClass){
        if(targetClass == null || fieldNames == null)
            return null;
        HashMap<String, Field> value = new HashMap<String, Field>();
        for(String name : fieldNames){
            try{
                Field fl = targetClass.getDeclaredField(name);
                if(fl != null)
                    value.put(name, fl);
            }
            catch(NoSuchFieldException | SecurityException exc){
//                System.err.println("exc : " + exc.toString());
            }
        }
        return value;
    }
    private <T> HashMap<T, Boolean> convertListToMapAsValueIsTrue(List<T> list){
        HashMap<T, Boolean> value = new HashMap<T, Boolean>();
        if(list != null){
            for(T s : list){
                value.put(s, Boolean.TRUE);
            }
        }
        return value;
    }
    /**
     * Veritabanıyla bağlantı kurup, tablo yapılandırmasını çıkarır ve döndürür
     * @param targetClass Yapılandırması istenen tablonun uygulamadaki sınıfı
     */
    private TableConfiguration getConfiguresOfTableFromDB(Class targetClass){
        if(targetClass == null)
            return null;
        String tableName = targetClass.getSimpleName();
        TableConfiguration confsOfTable = new TableConfiguration(targetClass);
        try{
            DatabaseMetaData md = this.connectivity.getConnext().getMetaData();
            ResultSet res = md.getColumns(this.connectivity.getConnext().getCatalog(), this.connectivity.getConnext().getSchema(), tableName, null);
            while(res.next()){
                String colName = res.getString("COLUMN_NAME");
                String isNullable = res.getString("NULLABLE");
                String isAutoIncrement = res.getString("IS_AUTOINCREMENT");
                String isGeneratedColumn = res.getString("IS_GENERATEDCOLUMN");
                String colDefaultValue = res.getString("COLUMN_DEF");
                if(!isNullable.equals("NO"))
                    confsOfTable.addNotNullConstraint(colName);
                if(!isAutoIncrement.equals("NO"))
                    ;//.;.
                if(colDefaultValue != null){
                    if(!colDefaultValue.isEmpty()){
                        ;//.;. Eğer gelen metîn tek tırnak içerisindeyse String tipindedir.
                    }
                }
            }
            ResultSet primaries = md.getPrimaryKeys(this.connectivity.getConnext().getCatalog(), this.connectivity.getConnext().getSchema(), tableName);
            while(primaries.next()){
                String pkInfo = primaries.getString("PK_NAME");
                if(pkInfo != null){
                    if(pkInfo.equals("PRIMARY"))
                        confsOfTable.setPrimaryKey(primaries.getString("COLUMN_NAME"));
                }
            }
            // Al : indeks
//            md.getIndexInfo("", "", "", true, true);
            // Al : charset
            // Şu an desteklenmiyor:
//            md.getExportedKeys("", "", "");
        }
        catch(SQLException exc){
            System.err.println("exc : " + exc.toString());
        }
        return confsOfTable;
    }
    private Object convertJSONtoTarget(String jsonText, Field targetField){
        return convertJSONtoTarget(jsonText, targetField, isListOrMapOrArray(targetField.getType()));
    }
    private Object convertJSONtoTarget(String jsonText, Field targetField, Map<String, Boolean> analysisFromIsListOrMapOrArray){
        Object value = null;
        if(analysisFromIsListOrMapOrArray.get("isMap")){
            Map<String, Object> data = JSONReader.getService().readJSONObject(jsonText);
            value = Reflector.getService().pruduceNewInjectedObject(targetField.getType(), data, this.confs.codingStyleForGetterSetter);
        }
        else{
            List<Object> data = JSONReader.getService().readJSONArray(jsonText);
            if(analysisFromIsListOrMapOrArray.get("isArray"))
                value = Reflector.getService().produceNewArrayInjectDataReturnAsObject(targetField.getType(), data);
            else
                value = Reflector.getService().produceNewInjectedList(data);
        }
        return value;
    }

//ERİŞİM YÖNTEMLERİ:
    public static IkizIdare getIkizIdare(){
        return ikiz;
    }
    public Cvity getConnectivity(){
        return connectivity;
    }
    public ErrorTable getErrorTable(){
        if(errorTable == null)
            errorTable = new ErrorTable();
        return errorTable;
    }
    public String[] getWorkingTablesAsArray(){
        int num = getWorkingTables().size();
        if(num == 0)
            return null;
        String[] list = new String[num];
        getWorkingTables().toArray(list);
        return list;
    }
    //GİZLİ ERİŞİM YÖNTEMLERİ:
    private Map<String, Field> getMapOfTargetFields(String tableName){
        TableMetadata md = getMetadataOfTables().get(tableName);
        return (md == null ? null : md.getMapOfTargetFields());
    }
    private HashMap<String, TableMetadata> getMetadataOfTables(){
        if(metadataOfTables == null)
            metadataOfTables = new HashMap<String, TableMetadata>();
        return metadataOfTables;
    }
    private TableMetadata getMetadataOfTable(String tableName){
        return getMetadataOfTables().get(tableName);
    }
    private HashMap<String, List<Object>> getBufferTables(){
        if(bufferTables == null)
            bufferTables = new HashMap<String, List<Object>>();
        return bufferTables;
    }
    private HashMap<String, Date> getLastUpdateTimeOfTables(){
        if(lastUpdateTimeOfTables == null)
            lastUpdateTimeOfTables = new HashMap<String, Date>();
        return lastUpdateTimeOfTables;
    }
    private ArrayList<String> getWorkingTables(){
        if(workingTables == null)
            workingTables = new ArrayList<String>();
        return workingTables;
    }
}