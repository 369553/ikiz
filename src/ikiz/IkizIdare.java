package ikiz;

import ReflectorRuntime.Reflector;
import jsoner.JSONReader;
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import jsoner.JSONArray;
import jsoner.JSONObject;
import rwservice.RWService;

/**
 * İkiz sisteminin çekirdeğini ifâde eden idâreci sınıftır
 * Ana işlemlerin çoğu bu sınıf üzerinden yapılmakta veyâ başlatılmaktadır
 * @author Mehmet Akif SOLAK
 * @version 1.0.0-beta
 */
public class IkizIdare{
    private static IkizIdare ikiz;
    private Cvity connectivity;
    private Confs confs;// IkizSistemi yapılandırma ayarları
    private ArrayList<String> workingTables;
    private HashMap<String, List<? extends Object>> bufferTables;
    private HashMap<String, Date> lastUpdateTimeOfTables;//Tabloların en son güncellendiği zamânı belirtiyor.
    private HashMap<String, UpdateMode> updateModeOfTables;//Tabloların tazeleme modunu belirtiyor.
//    private ErrorTable errorTable;//Hatâların yazılması ve gösterilmesiyle ilgili bir sistem
    private final Character start, end;// Veritabanı sistemine göre bir özel ismin başlangıç ve sonunu belirten karakter
    private final HashMap<String, String> mapDataTypeToDBDataType;// Sistemin çalıştığı veritabanı sistemine göre veri tipleri eşleştirme haritası
    private HashMap<String, TableMetadata> metadataOfTables;// Tablolar hakkında bilgiler
    private DBAccessHelper dbAccess;// Veritabanındaki bâzı işlemlerin kolayca yapılması için bir sınıf
    private List<Class<?>> loadedClasses;// Veritabanı tablolarından yapılandırma çıkarılması gerektiği durumda sınıflara ihtiyaç var.
    private IkizMunferid ikizMunferid = null;// Verilerin sistem içerisinde tekil olmasını sağlamak için kullanılan yardımcı sınıf
//    private HashMap<String, HashMap<

    private IkizIdare(Cvity connectivity){
        this.connectivity = connectivity;
        this.start = connectivity.getHelperForDBType().getStartSymbolOfName();
        this.end = connectivity.getHelperForDBType().getEndSymbolOfName();
        dbAccess = new DBAccessHelper(connectivity);
//        errorTable = new ErrorTable();
        this.confs = Confs.getDefaultConfs();
//        this.confs.bufferMode = true;
        this.mapDataTypeToDBDataType = HelperForHelperForDBType.getHelper(this.connectivity.getDBType()).getMapOfDataTypeToDBDataType();
        getIkizMunferid();
    }
    /**
     * İkiz'i oluşturur
     * @param connectivity İkiz'in üzerinde çalışması istenen veritabanı
     * bağlantısını barındıran {@code CVity} nesnesi
     * @param useBufferMode Önbellekleme modunun aktif olma durumu
     */
    private IkizIdare(Cvity connectivity, boolean useBufferMode){
        this(connectivity);
        this.confs.bufferMode = true;
    }

//İŞLEM YÖNTEMLERİ:
    //ANA BAŞLATMA YÖNTEMİ:
    /**
     * Verilen bağlantı test edilir ve uygunsa sistem başlatılır
     * @param connectivity Sistemi başlatmak için gerekli veritabanı bağlantısı
     * @return Sistemin başarıyla başlatılıp, başlatılmadığına dâir bilgi
     */
    public static boolean startIkizIdare(Cvity connectivity){
        if(connectivity == null)
            return false;
        if(testConnection(connectivity) == false){
            return false;}
        ikiz = new IkizIdare(connectivity);
        return true;
    }
    //SINIF FONKSİYONLARI:
    /**
     * Verilen bağlantı ile hedef veritabanındaki tablo isimleri getirilir
     * Bağlantı hedefi müşahhas bir veritabanı değilse, başka sorunlar varsa
     * veyâ işlem başarısız olursa {@code null} döndürülür
     * @param connectivity Bağlantı
     * @return İşlem başarılıysa tablo isim listesi veyâ {@code null}
     */
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
    /**
     * Verilen sınıf ile aynı isimde bir veritabanı tablosu oluşturur
     * Tablo oluşturulurken {@code IkizIdare}'nin {@code Confs} tipindeki
     * parametre ayarlarına bakılır
     * Bu ayarlar, sınıfın hangi erişim belirteciyle tanımlanmış özelliklerinin
     * alınacağını, sınıfın liste, dizi ve harita biçimindeki özelliklerinin
     * nasıl ele alınacağını belirtir
     * @param tableClass Veritabanı tablosuna dönüştürülmek istenen sınıf
     * @return İşlem başarılıysa {@code true}, değilse {@code false} döndürülür
     */
    public boolean produceTable(Class<?> tableClass){
        return produceTable(tableClass, null);
    }
    /**
     * Verilen sınıf ile aynı isimde bir veritabanı tablosu oluşturur
     * Tablo oluşturulurken IkizIdare parametre ayarları kullanılır
     * İkinci parametreyle oluşturulacak veritabanı tablosu yapılandırılabilir
     * Yapılandırmalar ve veri tipleri veritabanı desteğine göre belirlenir
     * @param tableClass Veritabanı tablosuna dönüştürülmek istenen sınıf
     * @param tableConfs Tablo yapılandırması
     * @return İşlem başarılıysa {@code true}, değilse {@code false} döndürülür
     * @see {@code ikiz.ikiz.TableConfiguration}
     */
    public boolean produceTable(Class<?> tableClass, TableConfiguration tableConfs){
        if(tableClass == null)
            return false;
        String tableName = tableClass.getSimpleName();// Tablo ismi = veritabanı nesnesi (entity) ismi
        for(String tbl : getWorkingTables()){
            if(tbl.equals(tableName))// Daha evvel oluşturulmuş; not düşülebilir
                return false;
        }
        StringBuilder query;// Hâzırlanan sorgu
        if(tableConfs == null){
            tableConfs = new TableConfiguration(tableClass);
        }
        Field[] fields = tableConfs.getTakeableFields();// Tüm aday sütunları al
        String[] columnNames = new String[fields.length];
        String[] columnTypes = new String[fields.length];
        Map<String, String> constraintTextsToProvideDataType = new HashMap<String, String>();// Bâzı veritabanları için hedef tipin sağlanması kısıt eklenmesi yoluyla olmaktadır. Bu, bunun içindir.
        int takedAttributesCounter = 0;// Alınan özellik sayısı sayacı
        HashMap<String, Field> metadataOnTargetFields = new HashMap<String, Field>();// İkiz için saklanan 'alan ismi - veri tipi' haritası
        // ANA ADIM - 1 : Alan isimlerini ve hedef veri tiplerini belirle:
        for(int sayac = 0; sayac < fields.length; sayac++){
            boolean isTaked = false;// Şu anki döngü çevrimindeki özelliğin alınıp, alınmadığı bilgisini tutuyor
            if(takeThisField(fields[sayac].getModifiers())){// // Kontrol - 1 : Erişim belirteci belirlenen stratejiye uygun mu?
                Class<?> typeOfField = fields[sayac].getType();
                // Veri tipi kontrolleri:
                if(isBasicType(typeOfField)){// Kontrol - 2.1 : Temel veri tipiyse ilgili alan için verileri al
                    columnNames[takedAttributesCounter] = fields[sayac].getName();
                    columnTypes[takedAttributesCounter] = getTypeNameForDB(fields[sayac].getType().getTypeName());
                    takedAttributesCounter++;
                    isTaked = true;
                }
                else{// Bu alan temel veri tipi değilse;
                    HashMap<String, Boolean> results = isArrayOrCollection(typeOfField);//Kontrol 2.2 : Liste-harita-dizi olup, olmadığını kontrol et..
                    if(results.get("result")){// Eğer veri tipi List veyâ Map veyâ Array (dizi) ise;
                        if(this.confs.getPolicyForListArrayMapFields() != Confs.POLICY_FOR_LIST_MAP_ARRAY.DONT_TAKE){// Kontrol 2.3 : Bu alanlar 'alınmayacak' olarak işâretlenmemişse;
                            if(this.confs.getPolicyForListArrayMapFields().equals(Confs.POLICY_FOR_LIST_MAP_ARRAY.TAKE_AS_JSON)){// Bu veriler veritabanına JSON olarak kaydedilmek isteniyorsa;
                                String fieldName = fields[sayac].getName();
                                columnNames[takedAttributesCounter] = fieldName;
                                columnTypes[takedAttributesCounter] = this.connectivity.getHelperForDBType().getDataTypeNameForJSON();
                                takedAttributesCounter++;
                                if(this.connectivity.getDBType().equals(Cvity.DBType.MSSQL)){
                                    String textOfConst = "CHECK(ISJSON(" + fieldName + ") = 1)";
                                    constraintTextsToProvideDataType.put("cons_" + fieldName, textOfConst);
                                }
                                isTaked = true;
                            }
                        }
                        else{// Bu alan liste veyâ dizi veyâ harita; fakat bu alanlar 'alınmayacak' olarak işâretlenmiş
                            // Bu alanı alma
                        }
                    }
                    else if(typeOfField.isEnum()){// 'enum' biçiminde bir veri tipi ise;
                        String fieldName = fields[sayac].getName();
                        boolean isSupportedEnum = this.getConnectivity().getHelperForDBType().isSupported(Enum.class.getName());
                        StringBuilder colDataTypeText = new StringBuilder(this.getConnectivity().getHelperForDBType().getDataTypeNameForEnum());
                        Object[] values = typeOfField.getEnumConstants();
                        if(isSupportedEnum){// Özel durum !!! : TÜm enum değerlerini parantez içinde belirtmelisin, MySQL için
                            colDataTypeText.append("(");
                            for(Object obj : values){
                                colDataTypeText.append("'").append(obj.toString()).
                                        append("'").append(", ");
                            }
                            if(values.length > 0)
                                colDataTypeText.delete(colDataTypeText.length() - 2, colDataTypeText.length());
                            colDataTypeText.append(")");
                        }
                        else{
                            StringBuilder textOfConst = new StringBuilder("CHECK(" + fieldName + " IN(");
                            for(Object obj : values){
                                textOfConst.append("'").append(obj.toString()).append("'");
                                textOfConst.append(", ");
                            }
                            if(values.length > 0)
                                textOfConst.delete(textOfConst.length() - 2, textOfConst.length());
                            textOfConst.append("))");
                            constraintTextsToProvideDataType.put("cons_" + fieldName, textOfConst.toString());
                        }
                        columnNames[takedAttributesCounter] = fieldName;
                        columnTypes[takedAttributesCounter] = colDataTypeText.toString();
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
                    // Metîn tipindeki veri için özel bir yapılandırma belirtildiyse..
                    if(typeOfField.equals(String.class)){
                        if(tableConfs.getSpecifiedLength(columnNames[sayac]) != -1 || tableConfs.getIsDefaultLengthOfStringChanged()){
                            columnNames[sayac] = columnNames[sayac].replaceFirst("500", String.valueOf(tableConfs.getLengthOfString(columnNames[sayac])));
                        }
                    }
                }
            }
        }
        query = new StringBuilder("CREATE TABLE ").append((start != null ? start : "")).
                append(tableName).append((end != null ? end : ""));
            query.append("(");
        for(int sayac = 0; sayac < takedAttributesCounter; sayac++){
            query.append((start != null ? start : "")).
            append(columnNames[sayac]).append((end != null ? end : "")).append(" ");
            query.append(columnTypes[sayac]);
            if(tableConfs.isNotNull(columnNames[sayac]))// NOT NULL kısıtı varsa, ekle:
                query.append(" NOT NULL ");
            if(tableConfs.getIsPrimaryKeyAutoIncremented()){
                if(tableConfs.getPrimaryKey().equals(columnNames[sayac])){
                    query.append(" ").append(this.connectivity.getHelperForDBType().getAutoIncrementKeyword()).append(" ");
                }
            }
            Object defValue = tableConfs.getDefaultValues().get(columnNames[sayac]);// Varsayılan değer kısıtı varsa, ekle
            if(defValue != null){
                query.append(" DEFAULT ? ");
            }
            if(sayac != takedAttributesCounter - 1)
                query.append(", ");
        }
        
        {//KISITLAR(CONSTRAINTS) EKLENECEK: DEFAULT kısıtı yukarıda ekleniyor, NOT NULL kısıtı yukarıda ekleniyor
            if(tableConfs.getClassOfTable().equals(tableClass)){// Gönderilen yapılandırma nesnesinin sınıfı ile tablosu üretilmek istenen sınıf aynı ise;
                //1. Birincil anahtar ekle:
                if(tableConfs.getIsConfSet().get("primaryKey")){// Birincil anahtar ayarı 'belirtildi' olarak işâretliyse
                    query.append(", PRIMARY KEY(").append((start != null ? start : "")).append(tableConfs.getPrimaryKey()).append((end != null ? end : "")).append(")");
                }
                for(String field : tableConfs.getUniqueFields()){
                    query.append(", UNIQUE(").append((start != null ? start : "")).append(field).append((end != null ? end : "")).append(")");
                }
            }
            for(String nameOfConst : constraintTextsToProvideDataType.keySet()){// MsSQL'deki JSON ve ENUM veri tipleri sağlanması için kısıt eklenmesi gerekiyor
                String textOfConst = constraintTextsToProvideDataType.get(nameOfConst);
                query.append(", CONSTRAINT ").append((start != null ? start : "")).append(nameOfConst).append((end != null ? end : "")).
                        append(" ").append(textOfConst);
            }
        }
            query.append(");");
        try{
            System.out.println("Gönderilen komut : " + query.toString());
            PreparedStatement prepSt = connectivity.getConnext().prepareStatement(query.toString());// Bu cursor tipinde hatâ veriyor : ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE
            // Varsayılan değerleri koy:
            int setCounter = 1;
            Object[] defValsForRawQuery = new Object[takedAttributesCounter];
            for(int sayac = 0; sayac < takedAttributesCounter; sayac++){
                Object value = tableConfs.getDefaultValues().get(columnNames[sayac]);
                if(value != null){
                    if(this.connectivity.getDBType() == Cvity.DBType.MSSQL)
                         defValsForRawQuery[setCounter - 1] = value;
                    else{
                        if(value.getClass().equals(char.class) || value.getClass().equals(Character.class)){
                            prepSt.setObject(setCounter, value, java.sql.JDBCType.CHAR);
                        }
                        else if(value.getClass().isEnum()){
                            prepSt.setObject(setCounter, String.valueOf(value));
                        }
                        else{
                            prepSt.setObject(setCounter, value);
                        }
                    }
                    setCounter++;
                }
            }
            // Sorguyu çalıştır:
//            System.out.println("Gönderilen komut : " + prepSt);// MsSQL desteklemiyor, konfordan uzak!..
            if(this.connectivity.getDBType() != Cvity.DBType.MSSQL)
                prepSt.execute();
            else{
                String fullRawQuery = dbAccess.getFinalSQLQuery(query.toString(), defValsForRawQuery);
                System.out.println("fullRawQuery : " + fullRawQuery);
                this.connectivity.getConnext().createStatement().execute(fullRawQuery);
            }
            if(!dbAccess.checkIsTableInDB(tableName)){
                System.err.println("Tablo oluşturma işlemi başarısız oldu");
                return false;
            }
            prepSt.clearBatch();
//            getLastUpdateTimeOfTables().put(tableName, DTService.getService().getTime());
            getWorkingTables().add(tableName);// Çalışılan tablolara ismini ekle
            TableMetadata tblMetaData = new TableMetadata(tableClass, tableConfs, metadataOnTargetFields);
            getMetadataOfTables().put(tableName, tblMetaData);
            tableConfs.setIsTableCreated(this, true);// Tablo yapılandırmasını değişikliklere karşı kilitle
            return true;
        }
        catch(SQLException ex){
            System.err.println("(konum : produceTable), ex.getMessage : " + ex.getMessage());
            return false;
        }
    }
    /**
     * Veritabanı tablosua bir satır verisi ekler
     * Gönderilen verinin sınıfı için daha önce tablo oluşturulmul olmalıdır
     * Gönderilen verinin özellikleri, tablo oluşturulurken kullanılan
     * {@code IkizIdare} yapılandırmasına göre alınır, yapılandırmada sonradan
     * yapılan değişiklikler bu fonksiyonun çalışmasını etkilemez, sadece
     * verinin alınması için gerekmesi durumunda 'getter' yönteminin hangi
     * isimle aranacağı durumu değişebilir, bu, {@code Reflector.CODING_STYLE}
     * tipiyle belirlenebilir
     * Veri özellikleri alınırken {@code IllegalAccessException} hatâsıyla
     * karşılaşılırsa ilgili alan verisini almak için getter yöntemi aranır
     * Bu yöntemin hangi isimle aranacağı {@code IkizIdare}'nin kodlama biçimi
     * ayarına göre belirlenir. Bunu belirlemek için {@code setCodingStyle()}
     * yöntemini kullanabilirsiniz
     * Bu yöntem, İkiz sistemine kaydedilmeyen tablolar için çalışmaz
     * @param entity Veritabanına satır olarak eklenmek istenen veri
     * @return İşlem başarılıysa {@code true}, değilse {@code false} döndürülür
     * @see {@code ReflectorRuntime.Reflector.CODING_TYPE}
     */
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
        if(!isInIkiz(tableName))// Sistem İkiz'de kayıtlı değilse işlemi sonlandır
            return false;
        StringBuilder query = new StringBuilder("INSERT INTO ").append((start != null ? start : ""))
                .append(tableName).append((end != null ? end : "")).append("(");
        Map<String, Field> mapOfTargets = new HashMap<String, Field>();// Ayrı bir nesne olması sonraki işlem sebebiyle önemli!
        mapOfTargets.putAll(getMapOfTargetFields(entity.getClass().getSimpleName()));
        TableMetadata md = getMetadataOfTable(tableName);
        boolean pkIsAutoIncremented = false;// Birincil anahtarın otomatik artan olup, olmadığı bilgisi
        Statement st = null;
        if(md != null){
            if(md.getConfs().getIsPrimaryKeyAutoIncremented()){
                mapOfTargets.remove(md.getConfs().getPrimaryKey());
                pkIsAutoIncremented = true;
            }
        }
        Field[] taked = new Field[mapOfTargets.values().size()];
        mapOfTargets.values().toArray(taked);
        int takedAttributeNumber = mapOfTargets.size();
        for(Field fl : taked){
            query.append((start != null ? start : "")).append(fl.getName()).
                    append((end != null ? end : "")).append(", ");
        }
        query.delete(query.length() - 2, query.length());// Sona eklenen fazladan ", " karakterlerini sil
        query.append(") VALUES (");
        for(int sayac = 0; sayac < takedAttributeNumber; sayac++){
            query.append("?");
            query.append(", ");
        }
        query.delete(query.length() - 2, query.length());// Sona eklenen fazladan ", " karakterlerini sil
        query.append(");");
        System.out.println("Hâzırlanan sorgu cümlesi : " + query.toString());
        PreparedStatement preparing = null;
        
        try{
            int takeGeneratedKeysAttr = (pkIsAutoIncremented ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
            preparing = connectivity.getConnext().prepareStatement(query.toString(), takeGeneratedKeysAttr);
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
            HashMap<String, Boolean> analysis = isArrayOrCollection(taked[sayac].getType());
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
        try{
            boolean result = preparing.executeUpdate() > 0;
            if(result && pkIsAutoIncremented){
                try{
                    HashMap<String, Object> mapOf = new HashMap<String, Object>();
                    String pkColName = md.getConfs().getPrimaryKey();
                    ResultSet resSetForPKValue = preparing.getGeneratedKeys();
                    resSetForPKValue.next();
                    Object pkValue = resSetForPKValue.getObject(1);
                    if(pkValue != null){
                        mapOf.put(pkColName, pkValue);
                        Reflector.getService().injectData(entity, mapOf, Reflector.CODING_STYLE.CAMEL_CASE);// Yeni gelen değeri nesneye zerk et
                    }
                    else
                        throw new NullPointerException();
                }
                catch(ClassCastException | NullPointerException | IndexOutOfBoundsException excOnFetchPK){
                    System.err.println("Yeni eklenen verinin otomatik artan alan değeri uygulamadaki nesneye aktarılamadı : " + excOnFetchPK.toString());
                }
            }
            return result;
        }
        catch (SQLException ex){
            System.err.println("Hatâ, sorgu çalıştırılmadı : " + ex.getMessage());
            return false;
        }
    }
    /**
     * Tabloyu veritabanından ve ikiz sisteminden kaldırmak için kullanılır
     * @param cls Kaldırılmak istenen tablonun uygulamadaki karşılığı olan sınıf
     * @return İşlem başarılıysa {@code true}, değilse {@code false} döndürülür
     */
    public boolean deleteTable(Class<?> cls){// Hangi sınıfla ilişkili tablo silinmek isteniyorsa parametre olarak verilmelidir.
        if(cls == null)
            return false;
        boolean tableDetected = false;
        Iterator<String> iter = getWorkingTables().iterator();
        String tableName = cls.getSimpleName();
        if(!isInIkiz(tableName))// Sistem İkiz'de kayıtlı değilse işlemi sonlandır
            return false;
        while(iter.hasNext()){
            if(iter.next().equals(tableName))
                tableDetected = true;
        }
        if(tableDetected){
            String delOrder = new StringBuilder("DROP TABLE ").append((start != null ? start : ""))
                    .append(tableName).append((end != null ? end : ""))
                    .append(";").toString();
            try{
                this.connectivity.getConnext().createStatement().execute(delOrder);
                boolean isDeleted = !checkIsTableInDB(cls);
                if(isDeleted){
                    getWorkingTables().remove(tableName);
                    getBufferTables().remove(tableName);
                    getIkizMunferid().removeTable(tableName);
                    getMetadataOfTables().remove(tableName);
                    return true;
                }
                else{
                    System.err.println("Tablo veritabanından silinemediği için sistemden de silinmedi!");
                    return false;
                }
            }
            catch(SQLException exc){
                System.err.println("Tablo silinirken hatâ oluştu : " + exc.toString());
            }
        }
        return false;
    }
    /**
     * Bir tablo verisinin tümünü veritabanından çekmek için kullanılır;
     * fakat eğer önbellekleme modu {@code bufferMode} açıksa, önbellekteki
     * veriler getirilir
     * Veri önbellekte yoksa, veritabanından getirilir ve önbelleğe kaydedilir
     * Hedef tablonun İkiz'de yapılandırması olmadığı durumda veri getirilir,
     * fakat önbellekleme hizmetlere dâhil edilmez
     * Önbellekleme modunun açık olduğu ve tablonun birincil anahtarının olduğu
     * durumda veri için yeni nesne oluşturulmaz; uygulamadaki nesneye yeni
     * veriler zerk edilir
     * @param <T> Veritabanı tablosunun karşılığı olan tip
     * @param target Veritabanı nesnesinin sınıfı
     * @return Nesne biçiminde veri listesi veyâ {@code null}
     */
    public <T> List<T> getData(Class<T> target){
        return getData(target, null);
    }
    /**
     * İkiz'e kayıtlı olsun, olmasın veri çekilir; Tablo İkiz'e kayıtlı ise ve
     * önbellekleme modu açıksa veri önbellekten getirilir
     * Eğer önbellekte yoksa, veritabanından getirilir
     * Verinin verilen alan değerleri çekilir ve hedef nesnelere zerk edilir
     * İkiz'e kaydedilmeyen tablodaki dizi-liste-harita JSON olarak ele alınıyor
     * @param <T> Veritabanı tablosunun karşılığı olan tip
     * @param target Veritabanı nesnesinin sınıfı
     * @param fieldsNames Verisi istenen sütun isimleri
     * @return Nesne biçiminde veri listesi veyâ {@code null}
     */
    public <T> List<T> getData(Class<T> target, List<String> fieldsNames){
        return (List<T>) getDataMain(target, fieldsNames, true, false);
    }
    /**
     * Verinin istenen alanlarının değerleri ham olarak getirilir
     * @param target Veritabanı nesnesinin sınıfı
     * @param fieldNames Verisi istenen sütun isimleri
     * @return Harita barındıran liste biçiminde veriler veyâ {@code null}
     */
    public List<Map<String, Object>> getValuesFromTable(Class<?> target, List<String> fieldNames){
        Object obj = getDataMain(target, fieldNames, false, false);
        return (obj == null ? null : (List<Map<String, Object>>) obj);
    }
    /*
    EKLE:
        LIMITLİ VERİ ÇEKME (VERİ ÇEKİLİRKEN DE LİMİTLİ OLMALI),
        ŞARTLI VERİ ÇEKME (WHERE İFÂDESİYLE..)
        
    */
    /**
     * Hedef tablodan sadece istenen sütunun verileri liste biçiminde döndürülür
     * Veri dönüştürülür ('casting') ve {@code null} veriler getirilmez.
     * @param <T> Verinin uygulamada dönüştürülmek istenen muadil tipi
     * @param target Hedef tablonun uygulamadaki karşılığı olan sınıf
     * @param columnName Sütun ismi
     * @param classOfColumn Sütunun dönüştürüleceği hedef sınıf
     * @return {@code T} tipinde verileri barındıran liste veyâ {@code null}
     */
    public <T> List<T> getColumnValues(Class<?> target, String columnName, Class<T> classOfColumn){
        return (List<T>) getColumnValuesMain(target, columnName, classOfColumn, true);
    }
    /**
     * Hedef tablodan sadece istenen sütunun verileri liste biçiminde döndürülür
     * Veri dönüştürme ('cast') yapılmaz ve {@code null} veriler de getirilir
     * @param target Hedef tablonun uygulamadaki karşılığı olan sınıf
     * @param field Sütun ismi
     * @return Veritabanından döndürülen tipte veri listesi veyâ {@code null}
     */
    public List<Object> getColumnValues(Class<?> target, String field){
        return getColumnValuesMain(target, field, null, false);
    }
    /**
     * Bir tablodan birincil anahtar kullanarak veri çekmek için kullanılır
     * İkiz'e kayıtlı olmayan tablo için çalışmaz
     * Önbellekleme modu açıksa ve veri önbelleklenmişse, önbellekten getirilir
     * Önbellekte olmayan veri veritabanından getirilir, hedefe zerk edilir
     * Önbellekleme modu açıksa, mevcut veriye zerk işlemi yapılır
     * Önbellekleme modu kapalıysa veri, yeni bir nesne olarak getirilir
     * @param <T> Verinin uygulamadaki karşılığı olan tip
     * @param target Hedef tablonun uygulamadaki karşılığı olan sınıf
     * @param primaryKeyValue Birincil anahtar değeri
     * @return Veri varsa ve hatâ yoksa istenen veri, aksi hâlde {@code null}
     */
    public <T> T getDataById(Class<T> target, Object primaryKeyValue){
        if(target == null)
            return null;
        if(primaryKeyValue == null)
            return null;
        String tableName = target.getSimpleName();
        if(!isInIkiz(tableName))// Sistem İkiz'de kayıtlı değilse işlemi sonlandır
            return null;
        TableMetadata md = getMetadataOfTable(tableName);
        T value = null;
//        System.err.println("]$ Tampon mod etkin mi : " + this.confs.bufferMode);
        if(this.confs.bufferMode && getIkizMunferid().isIndexedBefore(target)){
//            System.err.println("]$ Evvelde İndekslenmiş; tampondan getiriliyor..");
            return getIkizMunferid().getCurrentObjectWithPrimaryKey(target, primaryKeyValue);
        }
        try{
            TableConfiguration confOfTable = md.getConfs();
            if(confOfTable.getIsConfSet().get("primaryKey")){
                String primaryKey = confOfTable.getPrimaryKey();
                List<T> rows = getDataByFieldMain(target, primaryKey, primaryKeyValue, true);
                System.err.println("]$ tersi, alınan verilerin ilki : " + rows.get(0));
                if(rows != null){
                    if(!rows.isEmpty())
                        value = rows.get(0);
                }
            }
        }
        catch(NullPointerException exc){
            System.err.println("NullPointer exc : " + exc.toString());
        }
        return value;
    }
    /**
     * Verilen sütun ismine göre 'WHERE' şartıyla veri çekmek için kullanılır
     * Önbellekleme modu aktifse ve veri önbelleklenmişse, önbellekten getirilir
     * @param <T> Verinin uygulamadaki karşılığı olan tip
     * @param target Hedef tablonun uygulamadaki karşılığı olan sınıf
     * @param fieldName Eşitlik içeren WHERE şartındaki sütun ismi
     * @param valueForTheGivenField WHERE şartındaki sütunun değeri
     * @return Nesne biçiminde veri veyâ {@code null}
     */
    public <T> List<T> getDataWithOneWhereCondition(Class<T> target, String fieldName, Object valueForTheGivenField){
        return getDataByFieldMain(target, fieldName, valueForTheGivenField, false);
    }
    /**
     * Verilen sınıfın isminde veritabanı tablosu olup, olmadığı sorgulanır
     * @param cls Hedef sınıf
     * @return Tablonun varlığı doğrulanırsa {@code true}, aksi hâlde {@code false}
     */
    public boolean checkIsTableInDB(Class<?> cls){// DEĞİŞTİR
        if(cls == null)
            return false;
        return dbAccess.checkIsTableInDB(cls.getSimpleName());
    }
    /**
     * Verilen isimdeki tablonun veritabanındaki varlığını kontrol eder
     * @param tableName Tablo ismi
     * @return Tablo mevcutsa {@code true}, aksi hâlde {@code false}
     */
    public boolean checkIsTableInDB(String tableName){
        return dbAccess.checkIsTableInDB(tableName);
    }
    /**
     * Veritabanında vâr olan ve İkiz'e kayıtlı olmayan tabloyu İkiz'e ekler
     * Verilen sınıfı İkiz sistemine dâhil ederek, sorgulama yapıldığında
     * önbellekleme modu, hızlı veri çekimi tablo yapılandırmasının sistemde
     * saklanması gibi ilâve özellikler kazandırılır
     * @param cls İkiz sistemine kaydedilmek istenen veritabanı tablo sınıfı
     * @return İşlem başarılıysa {@code true}, değilse {@code false} döndürülür
     */
    public boolean integrateTableClassToIkiz(Class<?> cls){
        if(cls == null)
            return false;
        if(Helper.isInTheList(workingTables, cls.getSimpleName())){
            System.err.println("İlgili isimde bir kayıt sistemde yer alıyor");
            return false;
        }
        if(!checkIsTableInDB(cls)){// Veritabanı tablosunun varlığını kontrol et
            System.err.println("Verilen sınıfla aynı isimde bir veritabanı tablosu bulunamadı!");
            return false;
        }
        String tableName = cls.getSimpleName();
        // Veritabanından tabloyla ilgili önverileri çek:
        List<String> fieldNames = dbAccess.getFieldNames(tableName);// Sütun isimlerini al
        HashMap<String, Field> mapOfTargets = extractMapOfTargetFields(fieldNames, cls);// Karşılığı olan alanları bul
        TableConfiguration tblConfs = getConfiguresOfTableFromDB(cls, mapOfTargets);// Tablo yapılandırma nesnesini oluştur
        TableMetadata md = new TableMetadata(cls, tblConfs, mapOfTargets);// Tablo önveri nesnesini oluştur
        getMetadataOfTables().put(cls.getSimpleName(), md);// Bu yapılandırmayı kaydet
        getWorkingTables().add(tableName);// Tabloyu çalışılan tablo listesinin arasına kaydet
        return true;
    }
    /**
     * İkiz'de kayıtlı olan bir tabloyu yalnızca İkiz sisteminden kaldırır
     * @param cls Hedef tablonun uygulamadaki karşılığı olan sınıf
     * @return İşlem başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean discardTableFromIkiz(Class<?> cls){
        if(cls == null)
            return false;
        String tableName = cls.getSimpleName();
        if(!isInIkiz(tableName))
            return false;
        getWorkingTables().remove(tableName);// Tabloyu İkiz listesinden kaldır
        getMetadataOfTables().remove(tableName);// Tablo ön bilgilerini kaldır
        getIkizMunferid().removeTable(tableName);// İndeksten ve önbelleklemeden kaldır
        return true;
    }
    /**
     * Verilen nesne İkiz sisteminde kayıtlıysa, veri değişikliği veritabanına
     * iletilir
     * Bunun için tabloda, verilen nesnenin diğer satırlardan ayırt
     * edilmesini sağlayan birincil anahtar veyâ 'NOT NULL + UNIQUE' kısıtlarını
     * taşıyan bir sütun olmalıdır
     * Birincil anahtarı veyâ 'NOT NULL + UNIQUE' kısıtlarını taşıyan sütun
     * yoksa veri değişikliği veritabanına gönderilemez
     * @param entity Veritabanı nesnesi
     * @return Etkilenen satır varsa {@code true}, diğer durumlarda {@code false}
     */
    public boolean updateRowInDB(Object entity){//Belli bir alanının tazelenmesi istenmiyorsa tüm alanları tazele
        return updateRowInDB(entity, null);
    }
    /**
     * Birincil anahtarı veyâ 'NOT NULL + UNIQUE' kısıtı olan ve İkiz'e kayıtlı
     * olan bir tablonun satırının belirli değerlerini tazelemek için kullanılır
     * Bu yöntem, İkiz sistemine kaydedilmeyen tablolar için çalışmaz
     * Otomatik artan birincil anahtar sütununun verisi tazelenmez
     * @param entity Veritabanı nesnesi
     * @param fieldNames Hedef tabloda hangi verilerin tazeleneceği bilgisi
     * @return Etkilenen satır varsa {@code true}, diğer durumlarda {@code false}
     */
    public boolean updateRowInDB(Object entity, List<String> fieldNames){//Belli bir alanının tazelenmesi istenmiyorsa tüm alanları tazele
        if(entity == null)
            return false;
        String tableName = entity.getClass().getSimpleName();
        if(!isInIkiz(tableName))// Sistem İkiz'de kayıtlı değilse işlemi sonlandır
            return false;
        TableMetadata md = getMetadataOfTable(tableName);
        TableConfiguration tblConfs = md.getConfs();
        boolean applyForceUpdate = false;// Veriyi zor yoldan bulmak gerekiyorsa 'true' olmalı
        if(fieldNames == null){
            fieldNames = new ArrayList<String>();
            fieldNames.addAll(md.getMapOfTargetFields().keySet());
            if(tblConfs.getIsPrimaryKeyAutoIncremented()){
                fieldNames.remove(tblConfs.getPrimaryKey());
            }
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
    /**
     * Bir satırı diğerlerinden ayıran birincil anahtar veyâ 'NOT NULL + UNIQUE'
     * kısıtlı ve İkiz'e kayıtlı olan tablodaki bir satırın silinmesi içindir
     * Bu yöntem, İkiz sistemine kaydedilmeyen tablolar için çalışmaz
     * @param entity Veritabanı nesnesi
     * @return Etkilenen satır varsa {@code true}, diğer durumlarda {@code false}
     */
    public boolean deleteRowFromDB(Object entity){
        if(entity == null)
            return false;
        String tableName = entity.getClass().getSimpleName();
        if(!isInIkiz(tableName))// Sistem İkiz'de kayıtlı değilse işlemi sonlandır
            return false;
        TableMetadata md = getMetadataOfTable(tableName);
        TableConfiguration tblConfs = md.getConfs();
        boolean applyForceDelete = false;
        if(tblConfs != null){
            boolean isSuccess = false;// İşlemin başarılıysa 'true' olmalıdır
            // 1) Birincil anahtar üzerinden veriyi silmeye çalış
            // 2) Münferid alan üzerinden veriyi silmeye çalış (önce NULL olmayan münferid alana bak)
            String primaryKey = tblConfs.getPrimaryKey();
            Field primary = md.getMapOfTargetFields().get(primaryKey);
            StringBuilder sqlOrder = new StringBuilder();
            if(primary != null){
                Object valueOfPrimary = getValueOfFieldAsConverted(entity, primary);
                if(valueOfPrimary != null){
                    isSuccess = dbAccess.deleteRow(tableName, primaryKey, valueOfPrimary);
                    if(isSuccess && this.confs.bufferMode){// Silme işlemi başarılıysa;
                        getIkizMunferid().deleteObjectOnBufferByPrimaryKey(tableName, valueOfPrimary);// İlgili nesneyi önbellekteki indeksten de sil
                    }
                    return isSuccess;
                }
                else
                    return false;// Böyle bir dallanmaya düşülmemesi lazım normal işleyişte; birincil anahtar NULL ise ne yapılabilir?
            }
            else{// NOT NULL ve UNIQUE kısıtını berâber taşıyan bir sütun varsa, onları kullanarak silmeye çalış
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
                    Object valueOfUsableField = getValueOfFieldAsConverted(entity, md.getMapOfTargetFields().get(usableField));
                    isSuccess = dbAccess.deleteRow(tableName, usableField, valueOfUsableField);
//                    if(isSuccess && this.confs.bufferMode)// Silme işlemi başarılıysa; Şu an sadece birincil anahtara göre indeksleme yapıldığından gerek yok
//                        getIkizMunferid().deleteObjectOnBufferByReference(entity);// İlgili nesneyi önbellekten sil
                }
            }
            return isSuccess;
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
//    public String readLastError(){
//        return getErrorTable().readError();
//    }
//    public ArrayList<String> readAllErrors(){
//        ArrayList<String> all = new ArrayList<String>();
//        boolean go = true;
//        while(go){
//            String text = getErrorTable().readError();
//            if(text == null){
//                go = false;
//                 break;
//            }
//            all.add(text);
//        }
//        return all;
//    }
    /**
     * Bir sınıfın İkiz'e entegre olup, olmadığını sorgulamak için kullanılır
     * @param cls Varlığı sorgulanmak istenen tablonun karşılığı olan sınıf
     * @return Sınıf İkiz'e entegreyse {@code true}, aksi hâlde {@code false}
     */
    public boolean isInIkiz(Class<?> cls){
        return (cls == null ? false : isInIkiz(cls.getSimpleName()));
    }
    /**
     * Veritabanındaki verileri çekerek tampon bölgedeki verileri tazeler
     * @return İşlemin tümü başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean syncDataByPullFromDB(){
        boolean isSuccessfulWhole = true;
        for(String str : getWorkingTables()){
            TableMetadata md = getMetadataOfTables().get(str);
            boolean isSuccessful = syncDataByPullFromDB(md.getTargetClass());
            if(!isSuccessful){
                System.err.println("Şu tablonun veritabanı eşleşmesi başarısız oldu! : " + str);
                isSuccessfulWhole = false;
            }
        }
        return isSuccessfulWhole;
    }
    /**
     * 
     * @param <T> Hedef sınıfı simgeleyen tip
     * @param target Verilen sınıfın karşılığı veritabanı tablosundaki veriler
     * çekilir ve tampon bölgedeki (önbellekteki) veriler tazelenir
     * @return İşlem başarılıysa {@code true}, değilse {@code false} döndürülür
     */
    public <T> boolean syncDataByPullFromDB(Class<T> target){
        if(!this.confs.bufferMode){
            System.err.println("Önbellekleme modu aktif değil!");
            return false;
        }
        if(target == null)
            return false;
        Object dataAsObj = getDataMain(target, null, true, true);
        return true;
    }
    /**
     * İkiz'deki tablo yapılandırmalarını veritabanını analiz ederek yükler
     * Bu, İkiz'in kendi yapılandırmalarını içermez
     * İkiz'in kendi yapılandırmasını {@code importIkizConfigurationsFromFile()} ile içe
     * aktarabilirsiniz
     * @return Yükleme işlemi başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean loadSystemByAnalyzingDB(){
        // İkiz yapılandırma tablosunun veri olarak tespîtinin önlenmesini kodla
//        String Cvity = this.connectivity.getSchemaName();
        List<String> tables = dbAccess.getTableNames();
        if(tables == null){
            System.err.println("Veritabanı analizi başarısız oldu.");
            return false;
        }
        getWorkingTables().addAll(tables);// İşlem - 1 : Çalışılan veritabanı isimlerini kaydet
        // İşlem - 2 : Tablo önbilgilerini tespit et ve kaydet;
        for(String tbl : getWorkingTables()){
            List<String> fieldNames = dbAccess.getFieldNames(tbl);
            Class<?> cls = getSuitableClassOnTheList(tbl, fieldNames);
            if(cls == null){
                System.err.println("Şu sınıfın uygulamadaki karşılığı bulunamadı : " + tbl);
                continue;
            }
            HashMap<String, Field> mapOfTargets = extractMapOfTargetFields(fieldNames, cls);
            TableConfiguration tblConfs = getConfiguresOfTableFromDB(cls, mapOfTargets);
            TableMetadata md = new TableMetadata(cls, tblConfs, mapOfTargets);
            getMetadataOfTables().put(cls.getSimpleName(), md);// Ayarları İkiz'e aktar
        }
        return true;
    }
    /**
     * Sadece İkiz'in yapılandırmasını içe aktarır
     * Verilen yapılandırma metîn dosyasındaki diğer ayarlar yüklenmez
     * @param jsonText Yapılandırma metîn dosyası
     * @return İçe aktarma başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean importIkizConfigurations(String jsonText){
        if(jsonText == null)
            return false;
        if(jsonText.isEmpty())
            return false;
        return loadSystemFromConfigurationFile(jsonText, true);
    }
    /**
     * Verilen dizindeki 'ikizconfs.json' yapılandırma dosyasındaki sadece
     * İkiz yapılandırmasını yükler
     * Hedef dizinde bu dosya yoksa, veyâ başarısız olunursa {@code false} döner
     * @param path Kaynağın bulunduğu dizin
     * @return İçe aktarma başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean importIkizConfigurationsFromFile(String path){
        if(path == null)
            return false;
        if(path.isEmpty())
            return false;
        String jsonText = RWService.getService().readDataAsText(path, "ikizconfs.json");
        return importIkizConfigurations(jsonText);
    }
    /**
     * Verilen tüm sistem yapılandırma metniyle sistemi yükler
     * Bu yükleme, İkiz yapılandırması + tablo yapılandırmalarını kapsar
     * @param jsonText Sistem yapılandırma JSON metni
     * @return Yükleme işlemi başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean loadSystemFromAllConfigurations(String jsonText){
        if(jsonText == null)
            return false;
        if(jsonText.isEmpty())
            return false;
        return loadSystemFromConfigurationFile(jsonText, false);
    }
    /**
     * Verilen dizindeki 'ikizconfs.json' yapılandırma dosyasını yükler
     * Bu yükleme, İkiz yapılandırması + tablo yapılandırmalarını kapsar
     * Hedef dizinde bu dosya yoksa, veyâ başarısız olunursa {@code false} döner
     * @param path Kaynağın bulunduğu dizin
     * @return Yükleme işlemi başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean loadSystemFromAllConfigurationsFile(String path){
        if(path == null)
            return false;
        if(path.isEmpty())
            return false;
        String jsonText = RWService.getService().readDataAsText(path, "ikizconfs.json");
        return loadSystemFromAllConfigurations(jsonText);
    }
    /**
     * İkiz yapılandırmasını verilen dizine kaydetmek için kullanılır
     * Bu yapılandırma dosyası sistem yeniden başlatıldığında ayarların içe
     * aktarılması için kullanılabilir
     * Hedef dizinde bu isimde bir dosya varsa, üzerine yazılır
     * @param path Yapılandırma dosyası için hedef dizin
     * @return İşlem başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean saveAllConfiguration(String path){
        if(path == null)
            return false;
        if(path.isEmpty())
            return false;
        String content = backupAllConfs().getJSONText();// Tüm yapılandırma JSON metni
        return RWService.getService().produceAndWriteFile(content, "ikizConfs.json", path);
    }
    /**
     * Sadece İkiz yapılandırma ayarını dışa aktarmak için kullanılır
     * Hedef dizinde 'ikizConfs.json' ismiyle yeni dosya oluşturulur
     * Hedef dizinde bu isimde bir dosya varsa, üzerine yazılır
     * @param path Hedef dizin
     * @return İşlem başarılıysa {@code true}, aksi hâlde {@code false}
     */
    public boolean saveJustIkizConfiguration(String path){
        if(path == null)
            return false;
        if(path.isEmpty())
            return false;
        String content = backupIkizConfs().getJSONText();// Sadece İkiz yapılandırma JSON metni
        return RWService.getService().produceAndWriteFile(content, "ikizConfs.json", path);
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
    private boolean isInIkiz(String tableName){
        for(String s : getWorkingTables()){
            if(s.equalsIgnoreCase(tableName))
                return true;
        }
        return false;
    }
     private String getTypeNameForDB(String typeName){// Java veri tipini alır; seçilen veritabanı için uygun veri tipini döndürür
        String dType = mapDataTypeToDBDataType.get(typeName);
        return (dType != null ? dType : "");
    }
    private boolean controlGetterAndSetterForHideFields(Class<?> cls, boolean searchForJustGetter){
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
    private <T> T assignAttributes(Class<T> cls, Map<String, Object> mapAttributes, boolean useGivenInstance, T instance, boolean convertData){
        // convertData : İkiz tarafından uygulanan JSON veri saklama gibi özellikler
        // gereği JSON'dan diziye, listeye vs. dönüşüm gibi işlemlerin uygulanması
        // gerekip, gerekmediğini belirten parametredir.
        if(convertData)
            mapAttributes = convertJSONTextToMapOfVariables(mapAttributes, cls.getSimpleName());
        T injected = null;
        if(useGivenInstance && instance != null){
            injected = Reflector.getService().injectData(instance, mapAttributes, this.confs.codingStyleForGetterSetter);
        }
        else{
            injected = Reflector.getService().produceInjectedObject(cls, mapAttributes, this.confs.codingStyleForGetterSetter);
        }
        return injected;
    }
    private <T> T assignAttributes(Class<T> cls, Map<String, Object> mapAttributes, boolean useGivenInstance, T instance){//Hatâlardan sonra nasıl idâre edildiğiyle ilgili bir şey yok
        return assignAttributes(cls, mapAttributes, useGivenInstance, instance, true);
    }
    /*[$  yap */private Map<String, Object> convertJSONTextToMapOfVariables(Map<String, Object> data, Map<String, Class<?>> targetFields){
        if(data == null)
            return null;
        if(targetFields == null)
            return null;
        Map<String, Object> result = new HashMap<String, Object>();
        Iterator<Class<?>> iterOnValues = targetFields.values().iterator();
        for(String fieldName : targetFields.keySet()){
            Object fieldData = data.get(fieldName);
            Class<?> fieldType = iterOnValues.next();
            if(fieldData == null){
                result.put(fieldName, fieldData);
                continue;
            }
            HashMap<String, Boolean> analysis = isArrayOrCollection(fieldType);
            if(analysis.get("result")){
                if(fieldData instanceof String){
                    Object newValue = null;
                    if(analysis.get("isArray") || analysis.get("isList")){
                        List<Object> listOfData = JSONReader.getService().readJSONArray((String) fieldData);
                        if(analysis.get("isArray")){
                            newValue = Reflector.getService().produceInjectedArray(fieldType, listOfData, true, false);
                        }
                        else if(analysis.get("isList") || analysis.get("isCollection")){
                            newValue = Reflector.getService().produceInjectedList(listOfData);
                            if(analysis.get("isCollection")){// Liste dışında bir koleksiyon ise;
                                Collection cn = null;
                                Object instance = Reflector.getService().produceInstance(fieldType);
                                if(instance != null){
                                    try{
                                        cn = (Collection) instance;
                                        cn.addAll((List) newValue);
                                        newValue = cn;
                                    }
                                    catch(ClassCastException exc){
                                        System.err.println("Koleksiyon oluşturma / zerk hatâsı : " + exc.toString());
                                    }
                                }
                            }
                        }
                    }
                    else if(analysis.get("isMap")){
                        Map<String, Object> mapOfData = JSONReader.getService().readJSONObject((String) fieldData);
                        newValue = mapOfData;
                    }
                    //.;.
                    result.put(fieldName, newValue);
                }
            }
            else
                result.put(fieldName, fieldData);
            // Sonraki sürümlerde kullanıcı tanımlı veri tipleri için de dönüşüm eklenmesi gerekebilir.
        }
        return result;
    }
    private Map<String, Object> convertJSONTextToMapOfVariables(Map<String, Object> map, String tableName){
        if(map == null)
            return null;
        Map<String, Field> mapOfFields = getMapOfTargetFields(tableName);
        Map<String, Class<?>> mapOfTargetFields = new HashMap<String, Class<?>>();
        for(Field fl : mapOfFields.values()){
            mapOfTargetFields.put(fl.getName(), fl.getType());
        }
        return convertJSONTextToMapOfVariables(map, mapOfTargetFields);
    }
    private boolean isBasicType(Class<?> type){
        return isBasicType(type.getName());
    }
    private boolean isBasicType(String nameOfClass){
        for(String str : this.mapDataTypeToDBDataType.keySet()){
            if(str.equals(nameOfClass))
                return true;
        }
        return false;
    }
    private HashMap<String, Boolean> isArrayOrCollection(Class<?> cls){
        HashMap<String, Boolean> res = new HashMap<String, Boolean>();
        res.put("result", Boolean.FALSE);
        res.put("isArray", Boolean.FALSE);
        res.put("isList", Boolean.FALSE);
        res.put("isMap", Boolean.FALSE);
        res.put("isCollection", Boolean.FALSE);
        if(cls.isArray()){
            res.put("result", Boolean.TRUE);
            res.put("isArray", Boolean.TRUE);
            return res;
        }
        try{
            Class<?> casted = cls.asSubclass(List.class);
            res.put("result", Boolean.TRUE);
            res.put("isList", Boolean.TRUE);
        }
        catch(ClassCastException exc){// Eğer List'e dönüştürme işlemi başarısız olursa Map'e dönüştürmeye çalış
            //System.err.println("Hatâ (IkizIdare.isListOrMapOrArrayType()) : " + exc.toString());
            try{
                Class<?> otherCasted = cls.asSubclass(Map.class);
                res.put("result", Boolean.TRUE);
                res.put("isMap", Boolean.TRUE);
            }
            catch(ClassCastException inExc){
                try{
                    Class<?> collection = cls.asSubclass(Collection.class);
                    res.put("result", Boolean.TRUE);
                    res.put("isCollection", Boolean.TRUE);
                }
                catch(ClassCastException lastExc){
//                        System.out.println("Hatâ (IkizIdare.isListOrMapOrArrayType().catch()) : " + inExc.toString());
                }
            }
        }
        return res;
    }
    private String getJSONStringFromObject(Object obj){// Verilen nesne için JSON String üret; JSON'dan bir farkı var: anahtarlar String olmak zorunda değil
        if(obj == null)
            return null;
        Class<?> dType = obj.getClass();
        if(dType == String.class)// Veri tipi metîn ise;
            return "\"" + String.valueOf(obj) + "\"";
        JSONWriter jsonWrt = new JSONWriter();
        String result = null;
        result = jsonWrt.produceText(null, obj);// dizi veyâ harita veyâ liste için JSON metni üret
//        System.out.println("Üretilen metîn:\n" + result);
        return result;
    }
    private Object getValueOfField(Object entity, Field field){
        if(entity == null || field == null)
            return null;
        Map<String, Object> result = Reflector.getService().getValueOfFields(entity, new Field[]{field}, this.confs.codingStyleForGetterSetter);
        return (result != null ? result.get(field.getName()) : null);
    }
    private Object getValueOfField(Object entity, String fieldName){
        if(entity == null || fieldName == null)
            return null;
        if(fieldName.isEmpty())
            return null;
        Class<?> cls = entity.getClass();
        try{
            Field fl = cls.getDeclaredField(fieldName);
            return getValueOfField(entity, fl);
        }
        catch(NoSuchFieldException | SecurityException exc){
            System.err.println("İlgili alan bulunamadı : " + exc.toString());
            return null;
        }
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
                Map<String, Boolean> analysis = isArrayOrCollection(value.getClass());
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
    private <T> List<T> getDataByFieldMain(Class<T> target, String fieldName, Object valueForTheGivenField, boolean lookForOneResult){
        if(target == null || fieldName == null)
            return null;
        String tableName = target.getSimpleName();
        TableMetadata md = getMetadataOfTable(tableName);
        boolean isInIkiz = (md != null);// Hedef tablo İkiz'de kayıtlı mı?
        List<T> values = null;
        if(this.confs.bufferMode && isInIkiz){// Önbellek modu etkin ise;
//            getIkizMunferid().getCurrentObjectWithSpecifiedField()// Alana göre indeksleme özelliği gelirse, burası değişmeli
            List<T> inBuffer = (List<T>) getBufferTables().get(target.getSimpleName());
            if(inBuffer != null && inBuffer.size() > 0){
                values = new ArrayList<T>();
                for(T row : inBuffer){
                    if(row == null)
                        continue;
                    Object valueInTheGivenField = getValueOfField(row, fieldName);
                    if(valueInTheGivenField.equals(valueForTheGivenField)){
                        values.add(row);
                        if(lookForOneResult)
                            break;
                    }
                }
                if(values.size() > 0){
                    return values;
                }
            }
        }
        try{
            if((isInIkiz && md.getMapOfTargetFields().get(fieldName) != null)){
                List<Map<String, Object>> dataOfRecords = dbAccess.getDataForOneWhereCondition(tableName, null, fieldName, valueForTheGivenField);
//                System.out.println("gelen satır sayısı : " + dataOfRecords.size());
                values = new ArrayList<T>();
                List<Field> specialFields = new ArrayList<Field>();
                if((isInIkiz && this.confs.getPolicyForListArrayMapFields() == Confs.POLICY_FOR_LIST_MAP_ARRAY.TAKE_AS_JSON)){
                    Map<String, Field> targetFields = (isInIkiz ? md.getMapOfTargetFields() : convertListOfFieldsToMapOfFields(Reflector.getService().getSpecifiedFields(target, null)));
                    for(String s : targetFields.keySet()){// Liste - Map - Dizi alanlarını keşfet
                        Field fl = targetFields.get(s);
                        Map<String, Boolean> analysis = isArrayOrCollection(fl.getType());
                        if(analysis.get("result")){
                            specialFields.add(fl);
                        }
                    }
                }
                for(Map<String, Object> row : dataOfRecords){
                    if((isInIkiz && this.confs.getPolicyForListArrayMapFields() == Confs.POLICY_FOR_LIST_MAP_ARRAY.TAKE_AS_JSON)){
                        for(Field special : specialFields){
                            Object specialData = row.get(special.getName());// Ham veriyi (JSON metni) al
                            if(specialData != null){
                                row.put(special.getName(), convertJSONtoTarget(String.valueOf(specialData), special));// Hedef tipe çevir
                            }
                        }
                    }
                    boolean isCouldBeMunferid = false;
                    if(this.confs.bufferMode){
                        isCouldBeMunferid = isCouldBeMunferid(target);
                    }
                    T instance = getSuitableInstance(target, row);
                    values.add(Reflector.getService().injectData(instance, row, this.confs.codingStyleForGetterSetter));
                    if(isCouldBeMunferid){
                        String primaryKeyField = getMetadataOfTable(tableName).getConfs().getPrimaryKey();
                        if(primaryKeyField != null)
                            getIkizMunferid().putToHashed(target.getSimpleName(), row.get(primaryKeyField), instance);
                    }
                }
            }
        }
        catch(NullPointerException exc){
            System.err.println("NullPointer exc : " + exc.toString());
        }
        return values;
    }
    private <T> List<Object> getColumnValuesMain(Class<?> target, String field, Class<T> classOfField, boolean cast){
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
                        // [$ hedef tipe verinin zerk edilmesini engelleyici durum
                        // olabilir, misal, hedef tip dizi ve veri de JSON ise..
                        values.add(casted);
                    }
                    catch(ClassCastException exc){
//                        System.err.println("exc. : : " + exc.toString());
                        Map<String, Object> raw = new HashMap<String, Object>();
                        raw.put(field, fieldValue);
                        Map<String, Class<?>> targetAsMap = new HashMap<String, Class<?>>();
                        targetAsMap.put(classOfField.getSimpleName(), classOfField);
                        Map<String, Object> converted = convertJSONTextToMapOfVariables(raw, targetAsMap);
                        if(converted != null){
                            try{
                                values.add((T) converted.values().iterator().next());
                            }
                            catch(ClassCastException | NullPointerException excOnSpecialField){
                                System.err.println("(JSON olarak ele alınan) veri özel alana (harita (Map), liste, koleksiyon veyâ dizi) çevrilememiş:\t" + excOnSpecialField);
                            }
                        }
                        //[$ Yukarıdaki kod eklendi, sistemde istenen sağlandı mı?
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
    private <T> boolean isCouldBeMunferid(Class<T> target){
        return getIkizMunferid().isCouldGettingObjectWithPrimaryKey(target);// Veri münferid olabilir mi (sadece birincil anahtara bakılarak)?
    }
    private <T> T getSuitableInstance(Class<T> target, Map<String, Object> valuesOfAttributes){// İstenen sınıfın elemanı önbellekte varsa o döndürülür; aksi hâlde yeni bir örnek döndürülür
        T instance = null;
        if(this.confs.bufferMode){
            if(isCouldBeMunferid(target)){
                String primaryKeyField = getMetadataOfTable(target.getSimpleName()).getConfs().getPrimaryKey();
                instance = getIkizMunferid().getCurrentObjectWithPrimaryKey(target, valuesOfAttributes.get(primaryKeyField));
            }
        }
        if(instance == null){
            Constructor<T> noParamCs = Reflector.getService().findConstructorForNoParameter(target);
            if(noParamCs != null){
                try{
                    instance = noParamCs.newInstance(null);
                }
                catch(InstantiationException ex){
                    System.err.println("Parametresiz yöntemle değişken üretilirken hatâ :\n\t-->   " + ex.getMessage());
                }
                catch(IllegalAccessException ex){
                    System.err.println("Parametresiz yöntemle değişken üretilirken hatâ :\n\t-->   " + ex.getMessage());
                }
                catch(IllegalArgumentException ex){
                    System.err.println("Parametresiz yöntemle değişken üretilirken hatâ :\n\t-->   " + ex.getMessage());
                }
                catch(InvocationTargetException ex){
                    System.err.println("Parametresiz yöntemle değişken üretilirken hatâ :\n\t-->   " + ex.getMessage());
                }
            }
        }
        return instance;
    }
    private <T> Object getDataMain(Class<T> target, List<String> fieldNames, boolean getAsObject, boolean forcePullFromDB){//Tablodaki verilerin büyük olması durumunda verilerin tamâmını almak için bunları bigint gibi sayılarda tutmalıyız
        if(target == null)
            return null;
        if(fieldNames != null)
            if(fieldNames.isEmpty())
                return null;
        String tableName = target.getSimpleName();
        boolean isInIkiz = isInIkiz(target);
        if(this.confs.bufferMode && !forcePullFromDB && isInIkiz){// Bu şart kaldırıldı : && getIkizMunferid().isIndexedBefore(target)
            if(fieldNames == null){// Bu şart, parçalı veri çekme durumunda önbellekten veri çekilmemesini sağlamak için var
                List<? extends Object> data = getBufferTables().get(tableName);
                if(data != null)
                    return data;
            }
            // [$ Tampona alınan veriden istenen verileri döndür (dönüştürülmüş olarak)
        }
        if(fieldNames == null){// Eğer sütun isimleri verilmediyse tüm sütun isimlerini al
            fieldNames = new ArrayList<String>();
            if(isInIkiz){// İkiz'de kayıtlı bir tabloysa, kaydedilen alan isimlerini al
                for(String s : getMapOfTargetFields(tableName).keySet()){
                    fieldNames.add(s);
                }
            }
            else{// İkiz'de kayıtlı olmayan tablodan veri çekilmek isteniyorsa;
                Field[] fields = null;
                try{
                    fields = target.getDeclaredFields();
                    if(fields == null){
                        // İkiz'e kayıtlı olmayan tablonun alanları alınamadığından işlem sonlandırılıyor
                        return null;
                    }
                    for(Field fl : fields){
                        fieldNames.add(fl.getName());
                    }
                }
                catch(SecurityException exc){
                    System.err.println("Verilen tablonun alanları alınırken güvenlik hâtasıyla karşılaşıldı");
                    return null;
                }
            }
        }
        boolean keepGo = dbAccess.checkIsTableInDB(tableName);// Önce tablonun olup, olmadığına bakılıyor; bu, gereksiz bir işlem sayılabilir. Performansı arttırmak için sorgudan dönen hatâ sonucunu ele al
        if(!keepGo){
            System.err.println("İlgili tablo veritabanında olmadığından işlem sonlandırıldı!");
            return null;
        }
        List<Map<String, Object>> liDataOfObjectsBeforeInstantiation = null;
        if(getMapOfTargetFields(tableName) == null){// İkiz'e kaydedilmemiş bir veritabanı nesnesi için gelindiyse;
            liDataOfObjectsBeforeInstantiation = dbAccess.getData(tableName);
            if(!getAsObject)
                return liDataOfObjectsBeforeInstantiation;
            List<T> dataAsObject = new ArrayList<T>();
            for(Map<String, Object> mapAttributesOfRow : liDataOfObjectsBeforeInstantiation){
                T ready = assignAttributes(target, mapAttributesOfRow, false, null);
                dataAsObject.add(ready);
            }
            return dataAsObject;
        }
        else
            liDataOfObjectsBeforeInstantiation = dbAccess.getData(tableName, fieldNames);
        if(!getAsObject)
            return liDataOfObjectsBeforeInstantiation;
        // Aşağı kısım:
        // Birincil anahtarı olan verilerde, verinin uygulama içerisinde
        // tek nesne (münferid)biçiminde olmasını sağlamak için;..:
        boolean isCouldBeMunferid = isCouldBeMunferid(target);// Veri münferid olabilir mi?
        // PERFORMANS İÇİN YUKARIDAKİ SATIR YERİNE DAHA EVVEL IkizMunferid'e dâhil olup, olmadığı sorgulanabilir
        
        Constructor[] css = target.getConstructors();
        Constructor<T> noParamCs = null;
        for(Constructor cs : css){
            if(cs.getParameterCount() == 0){
                noParamCs = cs;
                break;
            }
        }
        List<T> liData = new ArrayList<T>();
        String primaryKey = null;
        HashMap<Object, Object> hashed = null;// Verilere münferiden erişebilmek için gereken 'hash' haritası
        if(isCouldBeMunferid){
            primaryKey = getMetadataOfTable(tableName).getConfs().getPrimaryKey();
            hashed = new HashMap<Object, Object>();
        }
        //Parametresiz yapıcı fonksiyonla elemanları üret:
        if(noParamCs != null){
            for(Map<String, Object> mapOfAttributesOfRow : liDataOfObjectsBeforeInstantiation){
                T instance = getSuitableInstance(target, mapOfAttributesOfRow);
                if(instance == null)// Sınıfın yeni bir örneği oluşturulamadıysa;
                    return null;
                T ready = assignAttributes(target, mapOfAttributesOfRow, true, instance);
                liData.add(ready);// Testlerden sonra, yukarıdaki satırla birleştir
                if(isCouldBeMunferid){// Veri, münferid olarak saklanabiliyorsa;
                    Object key = mapOfAttributesOfRow.get(primaryKey);
                    if(key != null)
                        hashed.put(key, ready);
                }
                // ESKİ YÖNTEM, testlerden sonra kaldır : liData.add(assignAttributes(target, mapOfAttributesOfRow));// Verileri ilgili alanlara zerk et
            }
//            System.out.println("Üretilen değişkenin sınıf ismi : " + data[0].getClass().getName());
//            System.out.println("Veri tipi dönüşümü yapılmış değişkenin sınıf ismi : " + target.cast(data[0]).getClass().getName());
            if(this.confs.bufferMode){// Veriyi önbelleğe yazmalısın, fakat IkizMunferid açısından gereken var mı vs. kontrol et
                addToBufferTables(tableName, hashed, liData);
            }
            return liData;
        }
        //Eğer parametresiz yapıcı fonksiyon yoksa:
        //.;.
        if(css[0].getParameterCount() != 0){
            System.err.println("Bu sürüm parametresiz yapıcı yöntemle çalışmayı desteklemiyor! Hedef "
                + tableName + " sınıfının parametresiz kurucu yöntemi olmadığından işlem sonlandırılıyor.");
            return null;
        }
        // Sonraki sürümde istenen:
//        if(this.confs.workWithNonParameterConstructor && css[0].getParameterCount() != 0){
//            System.out.println("Parametresiz yapıcı yöntemle çalışma etkin; lâkin sınıfın böyle bir kurucu yöntemi yok!");
//            return null;
//        }
        //.;.
        // Uygun yapıcı yöntemi seç, nesneleri oluştur ve ata
        //GEÇİCİ:
        return null;
    }
    private Class<?> getSuitableClassOnTheList(String simpleNameOfClass, List<String> fieldNames){// Aynı isimde birden fazla sınıf varsa, uygun olanını getirmek için..
        if(loadedClasses == null)
            loadedClasses = Reflector.getService().getClassesOnTheAppPath();
        List<Class<?>> found = new ArrayList<Class<?>>();
        for(Class<?> cls : loadedClasses){
            if(cls.getSimpleName().equalsIgnoreCase(simpleNameOfClass)){
                found.add(cls);
            }
        }
        if(found.isEmpty())
            return null;
        if(found.size() == 1)
            return found.get(0);
        else{
            Class<?>[] clss = new Class<?>[found.size()];
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
    private HashMap<String, Field> extractMapOfTargetFields(List<String> fieldNames, Class<?> targetClass){
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
    private TableConfiguration getConfiguresOfTableFromDB(Class<?> targetClass, Map<String, Field> mapOfTargets){
        if(targetClass == null)
            return null;
        String tableName = targetClass.getSimpleName();
        String autoIncrementedColName = null;//Otomatik artan alan tespit edildiğinde, sütun ismi bu değişkende saklanır
        TableConfiguration confsOfTable = new TableConfiguration(targetClass);
        try{
            DatabaseMetaData md = this.connectivity.getConnext().getMetaData();
            String catalog = this.connectivity.getConnext().getCatalog();
            String schema = this.connectivity.getConnext().getSchema();
            ResultSet res = md.getColumns(catalog, schema, tableName, null);
            HashMap<String, Integer> maxLengthOfStrings = new HashMap<String, Integer>();// Metîn sütunlarının azamî uzunluğu
            HashMap<Integer, Integer> frequencyOfLens = new HashMap<Integer, Integer>();// Metîn sütun uzunluklarının frekansı
            HashMap<String, Object> mapRealDefaultValues = new HashMap<String, Object>();// Varsayılan değerlerin gerçek hâli
            while(res.next()){
                String colName = res.getString("COLUMN_NAME");
                {// Metîn tipindeki alanlar için uzunluğu alma işlemi:
                    if(this.getConnectivity().getHelperForDBType().isDefaultStringDataType(res.getString("TYPE_NAME"))){// Birden fazla metîn tipi desteklenmesi durumunda burası değiştirilmeli
                        Integer maxLen = res.getInt("COLUMN_SIZE");
                        if(maxLen != 0){
                            maxLengthOfStrings.put(colName, maxLen);
                            Integer frequency = frequencyOfLens.get(maxLen);
                            if(frequency != null){
                                frequencyOfLens.put(maxLen, frequency + 1);
                            }
                            else
                                frequencyOfLens.put(maxLen, 1);
                        }
                    }
                }
                String isNullable = res.getString("NULLABLE");
                String isAutoIncrement = res.getString("IS_AUTOINCREMENT");
                String isGeneratedColumn = res.getString("IS_GENERATEDCOLUMN");
                Object colDefaultValue = res.getObject("COLUMN_DEF");
                if(!isNullable.equals("NO"))
                    confsOfTable.addNotNullConstraint(colName);
                if(!isAutoIncrement.equalsIgnoreCase("NO"))// Otomatik artan alan tespit edildiyse
                    autoIncrementedColName = colName;
                
                if(colDefaultValue != null){// Varsayılan değer ataması yapılmışsa..
                    String typeName = res.getString("TYPE_NAME");
//                    Class<?> target = this.connectivity.getHelperForDBType().getMatchedClassForGivenSqlType(typeName);// Hedef tip bu şekilde alınmamalı!
                    Class<?> target = mapOfTargets.get(colName).getType();
//                    System.out.println("\tVarsayılan değer, ilk karşılama : " + colDefaultValue
//                    + "\t(" + target.getName() + " tipine çevrilmeli)");
                    Object casted = Reflector.getService().getCastedObject(target, colDefaultValue);
                    if(casted != null){
                        mapRealDefaultValues.put(colName, casted);
                    }
//                    else{// Bilhassa dinamik ('generated') varsayılan değerler için ele alınmalı
//                        
//                    }
                }
            }
            
            {// Çekilebilen varsayılan değerleri ekle:
                for(String col : mapRealDefaultValues.keySet()){
                    confsOfTable.addDefaultValue(col, mapRealDefaultValues.get(col));
                }
            }
            
            {// Metîn uzunluklarına bakarak varsayılan metîn uzunluğunu,
             // bunun değişip, değişmediğini çıkarmaya çalış
                int lenAsDefault = 500;// Varsayılan metîn uzunluğu
                if(frequencyOfLens.size() > 0){
                    if(frequencyOfLens.size() == 1)// Tüm sütunların uzunluğu eşitse, varsayılan uzunluk odur
                        confsOfTable.setDefaultLengthOfString(frequencyOfLens.keySet().iterator().next());
                    else{
                        for(String field : maxLengthOfStrings.keySet())
                            confsOfTable.setLengthOfString(field, maxLengthOfStrings.get(field));
                    }
                }
            }
            
            ResultSet primaries = md.getPrimaryKeys(catalog, schema, tableName);
            while(primaries.next()){
                String pkInfo = primaries.getString("PK_NAME");
                if(pkInfo != null){
                    String pkName = primaries.getString("COLUMN_NAME");
                    confsOfTable.setPrimaryKey(primaries.getString("COLUMN_NAME"));
                    if(autoIncrementedColName != null){
                        if(pkName.equals(autoIncrementedColName))
                            confsOfTable.setPrimaryKeyAutoIncremented();
                    }
                }
            }
            ResultSet uniques = md.getIndexInfo(catalog, schema, tableName, false, false);
            HashMap<String, Integer> listOfAdding = new HashMap<String, Integer>();// <indeksIsmi, sonuç setinde bulunma sayısı>
            HashMap<String, String> uniqueIndexes = new HashMap<String, String>();// <indeksIsmi, indeksin bulunduğu sütun>
            while(uniques.next()){
                String colName = uniques.getString("COLUMN_NAME");
                String indexName = uniques.getString("INDEX_NAME");
//                boolean nonUnique = uniques.getBoolean("NON_UNIQUE");// Zâten yukarıda UNIQUE olanlar alınıyor
                Integer count = listOfAdding.get(indexName);
                if(count != null)
                    listOfAdding.put(indexName, count + 1);
                else
                    listOfAdding.put(indexName, 1);
                uniqueIndexes.put(indexName, colName);
//                System.out.println("\tindeksIsmi : " + indexName + "\t" + "sütun:" + colName + "\tNonUnique : " + nonUnique);
            }
            for(String indexName : uniqueIndexes.keySet()){
                if(listOfAdding.get(indexName) == 1){
                    confsOfTable.addUniqueConstraint(uniqueIndexes.get(indexName));
                }
            }
            {// Uyumsuz (sorgu metni biçiminde) olabilecek varsayılan değerleri veritabanına gönder
             // gelen sonuçları ilgili veri tipine çevir ve ata
                
            }
            // Al : charset
            // Şu an desteklenmiyor:
//            md.getExportedKeys("", "", "");
        }
        catch(SQLException exc){
            System.err.println("exc : " + exc.toString());
        }
        confsOfTable.setIsTableCreated(this, true);
        return confsOfTable;
    }
    private Object convertJSONtoTarget(String jsonText, Field targetField){
        return convertJSONtoTarget(jsonText, targetField, isArrayOrCollection(targetField.getType()));
    }
    private Object convertJSONtoTarget(String jsonText, Field targetField, Map<String, Boolean> analysisFromIsListOrMapOrArray){
        Object value = null;
        if(analysisFromIsListOrMapOrArray.get("isMap")){
            Map<String, Object> data = JSONReader.getService().readJSONObject(jsonText);
            value = Reflector.getService().produceInjectedObject(targetField.getType(), data, this.confs.codingStyleForGetterSetter);
        }
        else{
            List<Object> data = JSONReader.getService().readJSONArray(jsonText);
            if(analysisFromIsListOrMapOrArray.get("isArray"))
                value = Reflector.getService().produceInjectedArray(targetField.getType(), data, true, false);
            else if(analysisFromIsListOrMapOrArray.get("isArray") || analysisFromIsListOrMapOrArray.get("isCollection"))
                value = Reflector.getService().produceInjectedList(data);
            if(analysisFromIsListOrMapOrArray.get("isCollection")){
                Collection cn = null;
                try{
                    cn = (Collection) Reflector.getService().produceInstance(targetField.getType());
                    if(cn != null){
                        cn.addAll((Collection) value);
                        value = cn;
                    }
                }
                catch(ClassCastException | NullPointerException exc){
                    System.err.println("Koleksiyona veri zerk edilirken hatâ alındı : " + exc.toString());
                }
            }
        }
        return value;
    }
    private <T> void addToBufferTables(String tableName, HashMap<Object, Object> mapOfPrimaryKeyToObject, List<T> data){
        getBufferTables().put(tableName, data);
        // Zamân damgası bas
        getIkizMunferid().assingToHashed(getIkizMunferid(), tableName, mapOfPrimaryKeyToObject);
    }
    private JSONObject backupAllConfs(){
        JSONObject jsonRoot = new JSONObject();
        JSONObject jsonTableConfs = new JSONObject();
        for(TableMetadata md : getMetadataOfTables().values()){
            JSONObject metadata = new JSONObject();
            String targetClass = md.getTargetClass().getName();
            metadata.addString("targetClass", targetClass);
            JSONObject jsonCurrentTableConfs = new JSONObject(md.getConfs().exportConfigurations());
            metadata.addJSONObject("tableConfiguration", jsonCurrentTableConfs);
            ArrayList<String> liFieldNames = new ArrayList<String>();
            for(String s : md.getMapOfTargetFields().keySet()){
                liFieldNames.add(s);
            }
            metadata.addJSONArray("targetFieldNames", new JSONArray(liFieldNames));
            jsonTableConfs.addJSONObject(md.getTargetClass().getSimpleName(), metadata);
        }
        jsonRoot.addJSONObject("tableConfigurations", jsonTableConfs);// Tablo yapılandırmalarını ekle
        jsonRoot.addJSONObject("IkizConfigurations", backupIkizConfs());// İkiz yapılandırmasını ekle
        return jsonRoot;
    }
    private JSONObject backupIkizConfs(){
        JSONObject jsonIkizConfs = new JSONObject();
        jsonIkizConfs.addBoolean("alwaysContinue", this.confs.alwaysContinue);
        jsonIkizConfs.addJSONObject("attributesPolicy", new JSONObject(this.confs.getAttributesPolicy()));
//            jsonIkizConfs.addBoolean("workWithNonParameterConstructor", this.confs.workWithNonParameterConstructor);
        // updateModeOfTables eklenecek
        jsonIkizConfs.addBoolean("bufferMode", this.confs.bufferMode);
        jsonIkizConfs.addString("policyForListArrayMapFields", this.confs.getPolicyForListArrayMapFields().toString());
//            jsonIkizConfs.addString("policyForListUserDefinedClasses", this.confs.getPolicyForUserDefinedClasses().toString());
        jsonIkizConfs.addString("codingStyleForGetterSetter", this.confs.codingStyleForGetterSetter.toString());
        return jsonIkizConfs;
    }
    private boolean loadSystemFromConfigurationFile(String jsonText, boolean loadJustIkizConfs){
        try{
            Map<String, Object> raw = JSONReader.getService().readJSONObject(jsonText);
            JSONObject root = new JSONObject(raw);
            JSONObject jIkizConfs = root.getJSONObject("IkizConfigurations");
            JSONObject jTableConfs = root.getJSONObject("tableConfigurations");
            Confs produced = jIkizConfs.getThisObjectAsTargetType(Confs.class, Reflector.CODING_STYLE.CAMEL_CASE);
            if(produced != null){
                this.confs = produced;
                if(loadJustIkizConfs && jTableConfs != null){
                    for(Map<String, Object> current : jTableConfs){
                        String key = current.keySet().iterator().next();
                        TableMetadata md = extractTableMetadataFromJSON(key, (Map<String, Object>) current.get(key));
                        if(md == null)
                            return false;
                        getMetadataOfTables().put(key, md);
                        getWorkingTables().add(key);
                    }
                }
                return true;
            }
        }
        catch(Exception exc){
            System.err.println("Sistem yüklemesi başarısız oldu!!");
        }
        return false;
    }
    private TableMetadata extractTableMetadataFromJSON(String tableName, Map<String, Object> data){
        TableMetadata md = null;
        try{
            Class<?> targetClass = Class.forName((String) data.get("targetClass"));
            TableConfiguration confsAsObj = new TableConfiguration(targetClass);// TableConfiguration nesnesini oluştur:
            confsAsObj.importConfigurations((Map<String, Object>) data.get("tableConfiguration"));
            List<Field> targetFields = Reflector.getService().getSpecifiedFields(targetClass, (List<String>) data.get("targetFieldNames"));// Hedef özelliklerin listesini al
            Map<String, Field> mapOfTargetFields = convertListOfFieldsToMapOfFields(targetFields);
            mapOfTargetFields = (mapOfTargetFields == null ? new HashMap<String, Field>() : mapOfTargetFields);
            md = new TableMetadata(targetClass, confsAsObj, mapOfTargetFields);
            return md;
        }
        catch(ClassNotFoundException | NullPointerException exc){
            System.err.println("Veri içe aktarılamadı!!");
        }
        return md;
    }
    private Map<String, Field> convertListOfFieldsToMapOfFields(List<Field> fields){
        if(fields == null)
            return null;
        Map<String, Field> mapOf = new HashMap<String, Field>();
        for(Field fl : fields){
            mapOf.put(fl.getName(), fl);
        }
        return mapOf;
    }

//ERİŞİM YÖNTEMLERİ:
    /**
     * İşlemleri yürütebilmek için oluşturulan {@code IkizIdare}'yi döndürür
     * @return Sistem başlatıldıysa {@code IkizIdare}, aksi hâlde {@code null}
     */
    public static IkizIdare getIkizIdare(){
        return ikiz;
    }
    /**
     * Bağlantıyı ve bağlantıyla ilgili diğer nesneleri, işlemleri ifâde eden
     * {@code Cvity} nesnesini döndürür.
     * @return İkiz'in kullandığı bağlantıyı barındıran {@code Cvity} nesnesi
     */
    public Cvity getConnectivity(){
        return connectivity;
    }
//    public ErrorTable getErrorTable(){
//        if(errorTable == null)
//            errorTable = new ErrorTable();
//        return errorTable;
//    }
    /**
     * İkiz sistemine dâhil edilen tabloların isimlerini dizi olarak döndürür.
     * @return İkiz'in üzerinde çalıştığı tablo isimleri
     */
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
    protected HashMap<String, TableMetadata> getMetadataOfTables(IkizTeamPlayer player){
        if(player.getClass().equals(IkizMunferid.class)){
            if(getIkizMunferid() != null){
                if(player == ikizMunferid)
                    return getMetadataOfTables();
            }
        }
        return null;
    }
    /*]$ burayı private yap*/private HashMap<String, TableMetadata> getMetadataOfTables(){
        if(metadataOfTables == null)
            metadataOfTables = new HashMap<String, TableMetadata>();
        return metadataOfTables;
    }
    private TableMetadata getMetadataOfTable(String tableName){
        return getMetadataOfTables().get(tableName);
    }
    private HashMap<String, List<? extends Object>> getBufferTables(){
        if(bufferTables == null)
            bufferTables = new HashMap<String, List<? extends Object>>();
        return bufferTables;
    }
    protected HashMap<String, List<? extends Object>> getBufferTables(IkizTeamPlayer player){
        if(player == null)
            return null;
        if(player != this.getIkizMunferid())
            return null;
        return getBufferTables();
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
    private IkizMunferid getIkizMunferid(){
        if(ikizMunferid == null){
            ikizMunferid = new IkizMunferid(this);
        }
        return ikizMunferid;
    }
}