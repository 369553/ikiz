package ikiz;

import Base.JSONReader;
import ikiz.Services.DTService;
import ikiz.Services.Helper;
import Base.JSONWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IkizIdare{
    private static IkizIdare ikiz;
    private Cvity connectivity;
    private Confs confs;// IkizSistemi yapılandırma ayarları
    private ArrayList<String> workingTables;
    private HashMap<String, List> bufferTables;
    private HashMap<String, Date> lastUpdateTimeOfTables;//Tabloların en son güncellendiği zamânı belirtiyor.
    private HashMap<String, UpdateMode> updateModeOfTables;//Tabloların tazeleme modunu belirtiyor.
    private ErrorTable errorTable;//Hatâların yazılması ve gösterilmesiyle ilgili bir sistem
    private final char start, end;

    private IkizIdare(Cvity connectivity){
        this.connectivity = connectivity;
        this.start = connectivity.getHelperForDBType().getStartSymbolOfName();
        this.end = connectivity.getHelperForDBType().getEndSymbolOfName();
        errorTable = new ErrorTable();
        this.confs = Confs.getDefaultConfs();
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
    public boolean produceTable(Class tableClass, TableConfiguration confs){//ArrayList ve kullanıcı tanımlı tipler için çalışmaz
        if(tableClass == null)
            return false;
        String tableName = tableClass.getSimpleName();
        StringBuilder query;
        Field[] fields = tableClass.getDeclaredFields();
        String[] columnNames = new String[fields.length];
        Class[] columnTypes = new Class[fields.length];
        int takedAttributesNumber = 0;
        boolean result = false;
        for(int sayac = 0; sayac < fields.length; sayac++){// Alan isimleri ve veri tiplerini belirleme;
            if(takeThisField(fields[sayac].getModifiers())){
                Class typeOfField = fields[sayac].getType();// Alanın veri tipi alınır
//                System.out.println("Verinin tipi : " + typeOfField);
                // Kontrol - 1 : Tipi nedir?
                if(isBasicType(typeOfField)){// Temel veri tipiyse ilgili alan için verileri al
                    columnNames[takedAttributesNumber] = fields[sayac].getName();
                    columnTypes[takedAttributesNumber] = fields[sayac].getType();
                    takedAttributesNumber++;
                }
                else{// Bu alan temel veri tipi değilse;
                    HashMap<String, Boolean> results = isListOrMapOrArray(typeOfField);
                    boolean isArray = results.get("isArray");
                    boolean isMap = results.get("isMap");
                    if(results.get("result")){// Eğer veri tipi List veyâ Map veyâ Array (dizi) ise;
                        if(this.confs.getMethodForListAndMapFields() != Confs.POLICY_FOR_LIST_MAP_ARRAY.DONT_TAKE){// bu alanlar 'alınmayacak' olarak işâretlenmemişse;
                            String[] genericTypes = new String[2];
                            if(isArray){// Dizi ise;
                                String typeName = typeOfField.getSimpleName();
                                genericTypes[0] = typeName.substring(0, typeName.length() - 2);
                                System.out.println("Dizinin veri tipi : " + genericTypes[0]);
                                if(!canTakeableThisGenericTypeOfField(genericTypes[0])){// Bu alan
                                    System.err.println("Dizinin veri tipi temel veri tiplerinden olmadığında dizi alınamıyor");
                                    continue;// Bu alanı alma
                                }
                            }
                            else{// Harita veyâ liste ise;
                                Class generalType = fields[sayac].getType();
                                if(results.get("isList")){// İlgili alan 'List' tipinde ise;
                                    String typeNameOfGenericType = getGenericTypeNameOfField(fields[sayac]);
                                    if(typeNameOfGenericType == null){
                                        continue;// İlgili alanın 'generic' tipi alınamadığı için bu alanı alma
                                    }
                                    System.out.println("generalType : " + generalType.getName());
                                    System.out.println("genericType : " + typeNameOfGenericType);
                                    if(!canTakeableThisGenericTypeOfField(typeNameOfGenericType)){// 'generic type' yanî listevâri alanın veri tipi istenilen tip değilse;
                                        continue;// Bu alanı alma!
                                    }
                                    genericTypes[0] = typeNameOfGenericType;
                                }
                                else{// İlgili alan 'Map' tipinde ise;
                                    genericTypes = getGenericTypeNamesOfField(fields[sayac]);
                                    if(genericTypes == null)
                                        continue;
                                    boolean keepGo = true;
                                    for(String str : genericTypes){// İlgili alanlar destekleniyor mu?
                                        if(str.isEmpty()){
                                            keepGo = false;
                                            break;
                                        }
                                        if(!canTakeableThisGenericTypeOfField(str)){
                                            keepGo = false;
                                            break;
                                        }
                                    }
                                    if(!keepGo)// İlgili generic tipler gerekli şartları sağlamıyorlarsa bu alanı alma
                                        continue;
                                }
                            }
                            columnNames[takedAttributesNumber] = fields[sayac].getName();
                            columnTypes[takedAttributesNumber] = String.class;// Bu alanda, ilgili veriler metîn ile saklanacağından sütun veri tipi String olmalı
                            takedAttributesNumber++;
                        }
                        else{// Bu alan liste veyâ dizi veyâ harita; fakat bu alanlar 'alınmayacak' olarak işâretlenmiş
                            continue;// Bu alanı alma
                        }
                    }
                    else{// Bu alan temel veri tipi veyâ liste veyâ harita veyâ dizi değilse;
                        System.err.println("Üzgünüz! Ikiz henüz kullanıcı tanımlı veri tipleriyle çalışamıyor");
                        continue;// Bu alanı alma
                    }
                }
            }
        }
        System.out.println("tableName : "  +tableName);
        query = new StringBuilder("CREATE TABLE " + start + tableName + end);
            query.append("(");
        for(int sayac = 0; sayac < takedAttributesNumber; sayac++){
            query.append(start).
            append(columnNames[sayac]).append(end).append(" ");
            query.append(getTypeNameForDB(columnTypes[sayac].getTypeName()));
            if(sayac != takedAttributesNumber - 1)
                query.append(", ");
        }
        
        {//BAĞIMLILIKLAR EKLENECEK:
            if(confs != null){// Tablo sınıfı için yapılandırma nesnesi gönderildiyse;
                if(confs.getClassOfTable().equals(tableClass)){// Gönderilen yapılandırma nesnesinin sınıfı ile tablosu üretilmek istenen sınıf aynı ise;
                    //1. Birincil anahtar ekle:
                    if(confs.getIsConfSet().get("primaryKey")){// Birincil anahtar ayarı 'belirtildi' olarak işâretliyse
                        query.append(", PRIMARY KEY(").append(start).append(confs.getPrimaryKey()).append(end).append(")");
                    }
                }
            }
        }
            query.append(");");
        try{
            System.out.println("Gönderilen komut : " + query.toString());
            Statement st = connectivity.getConnext().createStatement();// Bu cursor tipinde hatâ veriyor : ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE
            st.execute(query.toString());
            if(!confirmTableInDB(tableName)){
                System.err.println("Tablo oluşturma işlemi başarısız oldu");
                return false;
            }
            st.clearBatch();
            getLastUpdateTimeOfTables().put(tableName, DTService.getService().getTime());
            getWorkingTables().add(tableName);
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
        if(!controlGetterAndSetterForHideFields(entity, true)){
            if(!this.confs.alwaysContinue){
                System.out.println("Gizli bir alanın getter yöntemi bulunamadı; veritabanı ayarında eksik değerlerle işleme devâm etme kapalı olduğu için işlem sonlandırılıyor");
                return false;
            }
        }
        String tableName = entity.getClass().getSimpleName();
        StringBuilder query = new StringBuilder("INSERT INTO " + tableName + "(");
        Field[] fields = entity.getClass().getDeclaredFields();
        int takedAttributes = 0;
        ArrayList<Field> liTakedFields = new ArrayList<>();
        HashMap<String, Boolean> specialFields = new HashMap<String, Boolean>();// Özel alanlar (dizi, liste, harita) işâretlenmeli
        HashMap<String, HashMap<String, Boolean>> specialises = new HashMap<String, HashMap<String, Boolean>>();
        for(int sayac = 0; sayac < fields.length; sayac++){
            if(takeThisField(fields[sayac].getModifiers())){// attributesPolicy'e göre ilgili alanın alınmasının uygunluğunu değerlendirir
                HashMap<String, Boolean> resultsOfAnalyzing = takeThisFieldForDataType(fields[sayac]);// İlgili alanın alınıp, alınmayacağı veri tipine göre değerlendiriliyor
                if(resultsOfAnalyzing.get("finalResult")){// İlgili alanın tipine ve ilgili listType polikasına göre alınıp, alınmayacağını tespit et
                    liTakedFields.add(fields[sayac]);
                    specialFields.put(fields[sayac].getName(), Boolean.TRUE);// Özel alan olarak işâretle
                    specialises.put(fields[sayac].getName(), resultsOfAnalyzing);
                    takedAttributes++;
                }
            }
        }
        Field[] taked = new Field[liTakedFields.size()];// Listeyi diziye çevirmek gerekli miydi?
        liTakedFields.toArray(taked);
        for(int sayac = 0; sayac < taked.length; sayac++){
            query.append(taked[sayac].getName());
            if(sayac != taked.length - 1){
                query.append(", ");
            }
        }
        query.append(") VALUES (");
        for(int sayac = 0; sayac < takedAttributes; sayac++){
            query.append("?");
            if(sayac != taked.length - 1){
                query.append(", ");
            }
        }
        query.append(")");
        PreparedStatement preparing = null;
        try{
            preparing = connectivity.getConnext().prepareStatement(query.toString());
        }
        catch(SQLException ex){
            System.err.println("Hatâ, sorgu cümlesi hâzırlama yapısı üretilemedi");
            return false;
        }
        Object value;// Nesnenin nesne olarak alınmış hâli (bu durumda liste, dizi, harita da olabilir)
        int writedAttribute = 0;
        System.out.println("hâzırlanan sorgu cümlesi : " + query.toString());
        for(int sayac = 0; sayac < taked.length; sayac++){
            Object pureValue = null;
            value = null;// Special özelliğe göre alım yap!
            try{
                pureValue = taked[sayac].get(entity);
            }
            catch(IllegalArgumentException ex){
                System.out.println("Hatâ, geçersiz değer : " + ex.getMessage());
            }
            catch(IllegalAccessException ex){
                try{
                    Method getter = entity.getClass().getMethod("get" + convertFirstLetterToUpper(taked[sayac].getName()), null);
                    try{
                        pureValue = getter.invoke(entity, null);
                    }
                    catch(IllegalAccessException ex1){
                        System.out.println("Hatâ, yetkisiz erişim...");
                        if(!this.confs.alwaysContinue){
                            callIsntCompleted("AddRowToDB", entity, tableName);
                        }
                        value = null;
                    }
                    catch (IllegalArgumentException ex1){
                        System.out.println("Hatâ, geçersiz değer...");
                        return false;
                    }
                    catch(InvocationTargetException ex1){
                        System.out.println("Hatâ, yöntem çalıştırılmasıyla ilgili...");
                        return false;
                    }
                }
                catch(NoSuchMethodException ex1){
                    System.out.println("Hatâ, erişim belirtecinden dolayı okunamayan alan bilgisi için get yöntemi eksik gibi!");
                    if(!this.confs.alwaysContinue)
                        return false;
                    value = null;
                }
                catch(SecurityException ex1){
                    System.out.println("Hatâ, güvenlikle alâkalı...");
                }
//                System.err.println("hatâ, yetkisiz erişim : " + ex.getMessage());
            }
            if(specialFields.get(taked[sayac].getName())){// Kabûl edilen özel bir veri tipi (liste, harita, dizi) ise
                value = getJSONStringFromObject(pureValue);// İlgili nesne verisini JSON metni olarak al
            }
            else// Temel veri tipi ise;
                value = pureValue;
            try{
                preparing.setObject(sayac + 1, value);
            }
            catch(SQLException ex){
                System.err.println("Hatâ, veri tipi eklenmesi sorunu, sanırım");
                return false;
            }
        }
        try{
            preparing.execute();
        }
        catch (SQLException ex){
            System.err.println("Hatâ, sorgu çalıştırılmadı : " + ex.getMessage());
        }
        query.append(");");
        return true;
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
    public <T> List<T> getData(Class<T> target){//Tablodaki verilerin büyük olması durumunda verilerin tamâmını almak için bunları bigint gibi sayılarda tutmalıyız
        String tableName = target.getSimpleName();
        if(target == null)
            return null;
        if(this.confs.bufferMode){// Veri tazeleme modu, burada farklılaştırılabilir. Misal, veri istendiğinde belirli bir süredir tazeleme olmadıysa veriyi çek, diğer durumda önbellekteki veriyi getir gibi
            List data = getDataFromBuffer(tableName);
            if(data != null)
                return data;
        }
        int number = -1;
        Statement st = null;
        ResultSet result = null;
        Field[] fields = target.getDeclaredFields();
        HashMap<String, Field> mapFields = new HashMap<>();
        for(Field f : fields){
            mapFields.put(f.getName(), f);
        }
        boolean keepGo = confirmTableInDB(tableName);// Önce tablonun olup, olmadığına bakılıyor; bu, gereksiz bir işlem sayılabilir. Performansı arttırmak için sorgudan dönen hatâ sonucunu ele al
        if(!keepGo){
            System.err.println("İlgili tablo veritabanında olmadığından işlem sonlandırıldı!");
            return null;
        }
//        System.out.println("Aşama 2...");
        ArrayList<T> liData;
        result = null;
        ArrayList<HashMap<String, Object>> liDataOfObjectsBeforeInstantiation = null;
        try{
            st = this.getConnectivity().getConnext().createStatement();// Hatâ veriyor : ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE
            result = st.executeQuery("SELECT * FROM " + tableName);
            liData = new ArrayList<T>();
            if(!result.next()){//Tabloda hiç satır yoksa;
                return liData;
            }
            int columnCount = result.getMetaData().getColumnCount();
            liDataOfObjectsBeforeInstantiation = new ArrayList<>();
            do{// Daha evvel result.next() çalıştırıldığından ilk satır için result.next() çalıştırılmamalı; bunun için do-while kullanılıyor
                HashMap<String, Object> map = new HashMap<String, Object>();
                for(int sayac = 1; sayac < columnCount + 1; sayac++){
                    map.put(result.getMetaData().getColumnLabel(sayac), result.getObject(sayac));
                }
                liDataOfObjectsBeforeInstantiation.add(map);
            }
            while(result.next());
        }
        catch(SQLException ex){
            System.out.println("hatâ : " + ex.getMessage());
            return null;
        }
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
        //Parametresiz yapıcı fonksiyonla elemanları üret:
        if(noParamCs != null){
            for(HashMap<String, Object> mapOfAttributesOfRow : liDataOfObjectsBeforeInstantiation){
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
                liData.add((T) assignAttributes(instance, target, mapOfAttributesOfRow, mapFields));// Verileri ilgili alanlara zerk et
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
//            System.out.println("resultSet yapılandırma bilgileri : " + result.getMetaData().toString());
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
        return false;
    }
    public boolean updateRowInDB(Object entity, Field[] fields){//Belli bir alanının tazelenmesi istenmiyorsa tüm alanları tazele
        return false;
    }
    public boolean deleteRowFromDB(Object entity){
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
    private String getTypeNameForDB(String typeName){
        String dbType = "";
        switch(typeName){
            case "java.lang.String" :{
                dbType = "text";
                break;
            }
            case "int" :{
                dbType = "int";
                break;
            }
            case "java.lang.Integer" :{
                dbType = "int";
                break;
            }
            case "double" :{
                dbType = "int";
                break;
            }
            case "float" :{
                dbType = "int";
                break;
            }
            case "java.lang.Double" :{
                dbType = "int";
                break;
            }
            case "java.lang.Float" :{
                dbType = "int";
                break;
            }
            case "boolean" :{
                dbType = "int";
                break;
            }
            case "java.lang.Boolean" :{
                dbType = "int";
                break;
            }
            case "java.lang.Byte" :{
                dbType = "bit";
                break;
            }
            case "char" :{
                dbType = "bit";
                break;
            }
            case "java.lang.Character" :{
                dbType = "bit";
                break;
            }
            case "java.util.Date" :{
                if(this.confs.takeDateAttributeAsDateTime)
                    dbType = "DATETIME";
                else
                    dbType = "DATE";
                break;
            }
        }
        return dbType;
    }
    private boolean controlGetterAndSetterForHideFields(Object entity, boolean searchForJustGetter){
        Class cl = entity.getClass();
        for(Field f : cl.getDeclaredFields()){
            if(f.getModifiers() == 0 || f.getModifiers() == 2 || f.getModifiers() == 4){
                try{
                    Method m = cl.getMethod("get" + convertFirstLetterToUpper(f.getName()), null);
//                    System.out.println("yöntem ismi : " + m.getName());
//                    System.out.println("yöntem.toString() : " + m.toString());
//                    System.out.println("yöntem dönüş değerinin tipi : " + m.getReturnType().getSimpleName());
                    if(m == null)
                        return false;
//                    System.out.println("f.getClass().getName() : " + f.getType().getTypeName());
                    if(!searchForJustGetter){
                        Method m2 = cl.getMethod("set" + convertFirstLetterToUpper(f.getName()), f.getType());
                    if(m2 == null)
                        return false;
//                        System.out.println("m2.getName : " + m2.getName());
////                    System.out.println(m2.getName() + "yönteminin girdisinin veri tipi : " + m2.getParameterTypes()[0].getSimpleName());
                    }
                }
                catch(NoSuchMethodException ex){
                     System.out.println("Hatâ, ilgili getter veyâ setter yöntemi bulunamadı");
                     return false;
                }
                catch(SecurityException ex){
                     System.out.println("Hatâ, güvenlikle ilgili");
                     return false;
                }
            }
        }
        return true;
    }
    private String convertFirstLetterToUpper(String s){
        String firstLetter = s.substring(0, 1);
        firstLetter = firstLetter.toUpperCase(Locale.ENGLISH);
        return (firstLetter + s.substring(1));
    }
    private void callIsntCompleted(String callFrom, Object entity, String tableName){
        System.out.println(callFrom + " yöntemi çalıştırılırken oluştu;\n" + tableName + "isimli nesneye erişim sağlanamadı;\n");
    }
    private Object assignAttributes(Object instance, Class cl, HashMap<String, Object> mapAttributes, HashMap<String, Field> mapFields){//Hatâlardan sonra nasıl idâre edildiğiyle ilgili bir şey yok
        for(String colName : mapAttributes.keySet()){
            Field f = mapFields.get(colName);
            if(f == null)//Ola ki ilgili özellik sınıfta yoksa, atama yapmaya çalışma
                continue;
            Object data = null;
            // Öncelikle dizi, liste ve harita alanlarını tespit etmeliyiz:
            HashMap<String, Boolean> results = isListOrMapOrArray(f.getType());
            if(results.get("result")){// İlgili alan dizi veyâ harita veyâ liste imiş
                Confs.POLICY_FOR_LIST_MAP_ARRAY policy = confs.getMethodForListAndMapFields();
                if(policy.equals(Confs.POLICY_FOR_LIST_MAP_ARRAY.DONT_TAKE)){
                    continue;// Liste ve harita ve dizi alanları sistemden hâriç tutulmak isteniyorsa, bu alanı atla
                }
                data = produceObjectFromStringForListMapArray((String) mapAttributes.get(colName), f, policy);
            }
            else{// İlgili alan liste veyâ harita veyâ dizi değilse değeri doğrudan tablodan al
                data = mapAttributes.get(colName);
            }
            if(f.getModifiers() != 1){
                try{
                    Method setter = cl.getDeclaredMethod("set" + convertFirstLetterToUpper(f.getName()), new Class[]{f.getType()});
                    try{
                        if((data == null && !f.getType().isPrimitive()) || data != null)// Veri null değilse veyâ hedef alanın veri tipi temel veri tipi değilse atamayı yap; //Not : Temel veri tipleri Object'ten kalıtım almadığı için onlara null değeri atanamıyor; amacımız şu : İlgili alan temel veri tipinde değilse 'null' değerini atamak (Başlangıçlandırma (instantiation) yapılmadığından hatâ alınmaması için. Java'da bu gerekli görünmüyor; ama yine de tedbirimizi alalım
                            setter.invoke(instance, data);
                    }
                    catch(IllegalAccessException ex){
                        System.err.println("hatâ : " + ex.getMessage());
                    }
                    catch(IllegalArgumentException ex){
                        System.err.println("hatâ : " + ex.getMessage());
                    }
                    catch(InvocationTargetException ex){
                        System.err.println("hatâ : " + ex.getMessage());
                    }
                }
                catch(NoSuchMethodException ex){
                    System.err.println("hatâ : " + ex.getMessage());
                }
                catch(SecurityException ex){
                    System.err.println("hatâ : " + ex.getMessage());
                }
            }
            else{
                try{
                    if((data == null && !f.getType().isPrimitive()) || data != null)// Bu şartın konulmasının îzahı 'setter' çalıştırılması satırında var
                        f.set(instance, data);
                }
                catch(IllegalArgumentException ex){
                    System.err.println("hatâ : " + ex.getMessage());
                }
                catch(IllegalAccessException ex){
                    System.err.println("hatâ : " + ex.getMessage());
                }
            }
        }
        return instance;
    }
    private List getDataFromBuffer(String tableName){
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
        boolean isSuccess = false;
        switch(nameOfClass){
            case "int" : {
                isSuccess = true;
                break;
            }
            case "java.lang.Integer" : {
                isSuccess = true;
                break;
            }
            case "double" : {
                isSuccess = true;
                break;
            }
            case "java.lang.Double" : {
                isSuccess = true;
                break;
            }
            case "float" : {
                isSuccess = true;
                break;
            }
            case "java.lang.Float" : {
                isSuccess = true;
                break;
            }
            case "java.lang.String" : {
                isSuccess = true;
                break;
            }
            case "char" : {
                isSuccess = true;
                break;
            }
            case "java.lang.Character" : {
                isSuccess = true;
                break;
            }
            case "boolean" : {
                isSuccess = true;
                break;
            }
            case "java.lang.Boolean" : {
                isSuccess = true;
                break;
            }
            /*case "byte" : {
                isSuccess = true;
                break;
            }
            case "java.lang.Byte" : {
                isSuccess = true;
                break;
            }*/
        }
        return isSuccess;
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
    private Object produceObjectFromStringForListMapArray(String data, Field field, Confs.POLICY_FOR_LIST_MAP_ARRAY policy){
        if(policy == Confs.POLICY_FOR_LIST_MAP_ARRAY.DONT_TAKE)
            return null;
        if(policy == Confs.POLICY_FOR_LIST_MAP_ARRAY.TAKE_AS_JSON){
            HashMap<String, Boolean> res = isListOrMapOrArray(field.getType());
            if(!res.get("result")){
                System.err.println("Verilen alan liste & harita & dizi değil (produceObjectFromStringForListMapArray): " + field.getName());
                return null;
            }
            Object value = null;
            if(res.get("isList") || res.get("isArray")){// İlgili alan dizi ise;
                ArrayList<Object> list = JSONReader.getService().readJSONArray(data);
                if(res.get("isArray")){
                    value = ReflectorForRunTime.getService().produceNewArrayInjectDataReturnAsObject(field.getType(), list.size(), list);
                    System.out.println("value.type : " + value.getClass().getName());
                }
                else{
                    value = ReflectorForRunTime.getService().produceNewInstance(field.getType());
                    try{
                        ((List)value).addAll(list);
                    }
                    catch(UnsupportedOperationException | ClassCastException | NullPointerException | IllegalArgumentException exc){
                        System.err.println("JSON metni verisi ilgili List değişkenine aktarılamadı : " + exc.toString());
                        return null;
                    }
                }
            }
            else if(res.get("isMap")){
                value = ReflectorForRunTime.getService().produceNewInstance(field.getType());
                Map map = JSONReader.getService().readJSONObj(data);
                try{
                    ((Map)value).putAll(map);
                }
                catch(UnsupportedOperationException | ClassCastException | NullPointerException | IllegalArgumentException exc){
                    System.err.println("JSON metni verisi ilgili Map değişkenine aktarılamadı : " + exc.toString());
                    return null;
                }
            }
            return value;
        }
        return null;
    }
    private String getGenericTypeNameOfField(Field field){// Verilen 'field' alanının generic tipini belirlemek için.. (tek generic tip barındırıyorsa)
        try{
            String genTypeName = field.getGenericType().getTypeName();
            String absoluteName = genTypeName.substring(genTypeName.indexOf('<') + 1, genTypeName.indexOf('>'));
            System.out.println("absoluteName : " + absoluteName);
            return absoluteName;
        }
        catch(Exception exc){
            System.err.println("Verilen tipin generic değil! exc.toString : " + exc.toString());
        }
        return null;
    }
    private String[] getGenericTypeNamesOfField(Field field){// İki generic tip barındıran veri tipleri için;
        String[] values = new String[2];
        String genType = field.getGenericType().getTypeName();
        String[] splitted = genType.substring(genType.indexOf('<') + 1, genType.length() - 1).split(",");
        values[0] = splitted[0].trim();
        values[1] = splitted[1].trim();
        return values;
    }
    private boolean canTakeableThisGenericTypeOfField(String genericTypeName){
        if(genericTypeName == null)
            return false;
        if(genericTypeName.isEmpty())
            return false;
        boolean take = false;
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
    private HashMap<String, Boolean> takeThisFieldForDataType(Field field){// Bir alanın alınıp, alınmayacağını veri tipine cihetinden değerlendir
        Class type = field.getType();
        // finalResult bu yöntemin sonucunu bildiriyor
        HashMap<String, Boolean> results = new HashMap<String, Boolean>();
        results.put("finalResult", Boolean.FALSE);
        results.put("isMap", Boolean.FALSE);
        results.put("isList", Boolean.FALSE);
        results.put("isArray", Boolean.FALSE);
        if(isBasicType(type)){// Temel veri tiplerini kabûl et
            results.put("finalResult", Boolean.TRUE);
        }
        else{
            results = isListOrMapOrArray(type);
            if(results.get("result")){// Liste - harita - dizi veri tipleri için generic veri tipini kontrol et
                if(type.isArray()){// İlgili alan dizi ise;
                    String genTypeStr = field.getGenericType().getTypeName();
                    genTypeStr = genTypeStr.substring(0, genTypeStr.length() - 2);
                    results.put("finalResult", isBasicType(genTypeStr));
                }
                else if(results.get("isList")){
                    results.put("finalResult", isBasicType(getGenericTypeNameOfField(field)));
                }
                else if(results.get("isMap")){
                    String[] genTypes = getGenericTypeNamesOfField(field);
                    if(genTypes != null){
                        boolean confirmed = true;
                        for(int sayac = 0; sayac < genTypes.length; sayac++){
                            if(!canTakeableThisGenericTypeOfField(genTypes[sayac]))
                                confirmed = false;
                        }
                        if(confirmed)
                            results.put("finalResult", Boolean.TRUE);
                    }
                }
            }
        }
        return results;
    }
    public/*private yap*/ String getJSONStringFromObject(Object obj){// Verilen nesne için JSON String üret; JSON'dan bir farkı var: anahtarlar String olmak zorunda değil
        // BURADA KALIDI
        if(obj == null)
            return "";
        Class dType = obj.getClass();
        if(dType == String.class)// Veri tipi metîn ise;
            return "\"" + String.valueOf(obj) + "\"";
        Object[] asArray = null;
        JSONWriter jsonWrt = new JSONWriter();
        String result = null;
        result = jsonWrt.produceText(null, obj);// dizi veyâ harita veyâ liste için JSON metni üret
        System.out.println("Üretilen metîn:\n" + result);
        return result;
    }

//ERİŞİM YÖNTEMLERİ:
    public static IkizIdare getIkizIdare(){
        return ikiz;
    }
    public Cvity getConnectivity(){
        return connectivity;
    }
    //GİZLİ ERİŞİM YÖNTEMLERİ:
    private HashMap<String, List> getBufferTables(){
        if(bufferTables == null)
            bufferTables = new HashMap<String, List>();
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
    public ErrorTable getErrorTable(){
        if(errorTable == null)
            errorTable = new ErrorTable();
        return errorTable;
    }
}

/*
MySqlDB'den gelen bir yöntem:
    private boolean addRowToDB(Object entity, String tableName, String[] fields, Object[] values){
            int changedRowNumber = 0;
            StringBuilder sentence = new StringBuilder("INSERT INTO " + tableName + " ");
            sentence.append("(");
            for(int index = 0; index < fields.length; index++){
                sentence.append(fields[index]);
                if(index != fields.length - 1)
                    sentence.append(", ");
            }
            sentence.append(") ");
            sentence.append("VALUES ");
            sentence.append("( ");
            for(int index = 0; index < values.length; index++){
                sentence.append("?");
                if(index != fields.length - 1)
                    sentence.append(", ");
            }
            sentence.append(")");
            System.err.println("sentence not : " + sentence.toString());
            try{
                PreparedStatement preStatement = connectivity.getConnext().prepareStatement(sentence.toString());
                for(int index = 1; index <= values.length; index++){
//                    System.out.println("Alan : " + fields[index - 1] + "\nSıra no : " + index + "\nYerleştirilen değer : " + values[index - 1].toString());
                    preStatement.setObject(index, values[index - 1]);
                }
                changedRowNumber = preStatement.executeUpdate();
            }
            catch(SQLException DBException){
                System.out.println("ex.Message() (konum : private addRowToDB): " + DBException.getMessage());
            }
            if(changedRowNumber == 0)
                return false;
            return true;
        }
*/



/*

    private Constructor[] findConstructor(Class cl, HashMap<String, Object> mapAttributes, boolean lookingAgain){
        for(Constructor cs : cl.getConstructors()){
            String[] parameterNames = new String[cs.getParameterCount()];
            Parameter[] prs = cs.getParameters();
            if(prs.length != 0 && workWithNonParameterConstructor)
                continue;
            if(prs.length == 0){
                return new Constructor[]{cs};
            }
            for(int sayac = 0; sayac < parameterNames.length; sayac++){
//                System.out.println("Integer sınıfından misal:\n" + String.class.getConstructors()[0].getParameters()[0].toString());;
                //BURADA KALINDI:
                //Seçenekler:
                //Parametresiz yapıcı yöntem olmasını şart koşma
                //Parametresiz yapıcı yöntemi sınıfa ekleme
                //Yapıcı yöntem parametrelerinin isimlerini kullanıcıdan alma
                //Yapıcı yöntemin parametrelerinin isimlerinin tanımlandığı bir yer varsa, bulma
                System.err.println("veri : " + prs[sayac].toString());
                System.out.println("Parametre ismi : " + prs[sayac].toString());
            }
        }
        return null;
    }
*/