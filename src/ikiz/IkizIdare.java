package ikiz;

import ikiz.Services.DTService;
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
import java.util.Locale;

public class IkizIdare{
    private Cvity connectivity;
    private static IkizIdare ikiz;
    private HashMap<String, Boolean> attributesPolicy;
    private boolean takeDateAttributeAsDateTime = true;
    protected boolean alwaysContinue = true;//Eğer kullanıcın alınmasını istediği değer alınamadıysa, kalan değerlerle veritabanına yazmaya çalış
    protected boolean workWithNonParameterConstructor = true;//
    private boolean workWithSpecialBuilder = false;//Yalnızca ikizIdare sınıfından bir nesnenin 'specialCode' kullanarak erişebileceği bir inşâcı ile çalış
    private String specialCode;//Özel inşâcı ile çalışıldığı durumda özel inşâcının ikizIdare için ilgili sınıftan bir değişken üretmesi için gereken kod
    private ArrayList<String> workingTables;
    private boolean bufferMode = false;//Her veri çekme isteğinde veritabanından veri çekilmemesi için verilerin İkiz'de saklandığı bir çalışma şekli
    private HashMap<String, Object[]> bufferTables;
    private HashMap<String, Date> lastUpdateTimeOfTables;//Tabloların en son güncellendiği zamânı belirtiyor.
    private HashMap<String, UpdateMode> updateModeOfTables;//Tabloların tazeleme modunu belirtiyor.
    private ErrorTable errorTable;//Hatâların yazılması ve gösterilmesiyle ilgili bir sistem

    private IkizIdare(Cvity connectivity){
        this.connectivity = connectivity;
        getAttributesPolicy();
        errorTable = new ErrorTable();
    }

//İŞLEM YÖNTEMLERİ:
    //ANA BAŞLATMA YÖNTEMİ:
    public static boolean startIkizIdare(Cvity connectivity){
        if(connectivity == null)
            return false;
        if(testConnection(connectivity) == false)
            return false;
        ikiz = new IkizIdare(connectivity);
        return true;
    }
    public boolean produceTable(Object object){//ArrayList ve kullanıcı tanımlı tipler için çalışmaz
        if(object == null)
            return false;
        Class cl = object.getClass();
        String tableName = cl.getSimpleName();
        StringBuilder query;
        Field[] fields = cl.getDeclaredFields();
        String[] columnNames = new String[fields.length];
        Class[] columnTypes = new Class[fields.length];
        int takedAttributesNumber = 0;
        boolean result = false;
        for(int sayac = 0; sayac < fields.length; sayac++){
            if(takeThisField(fields[sayac].getModifiers())){
                columnNames[takedAttributesNumber] = fields[sayac].getName();
                columnTypes[takedAttributesNumber] = fields[sayac].getType();
                takedAttributesNumber++;
            }
        }
        System.out.println("tableName : "  +tableName);
        query = new StringBuilder("CREATE TABLE " + tableName);
            query.append("(");
        for(int sayac = 0; sayac < takedAttributesNumber; sayac++){
            query.append(columnNames[sayac] + " ");
            query.append(getTypeNameForDB(columnTypes[sayac]));
            if(sayac != takedAttributesNumber - 1)
                query.append(", ");
        }
            query.append(");");
        try{
            System.out.println("Gönderilen komut : " + query.toString());
            Statement st = connectivity.getConnext().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            result = st.execute(query.toString());
            if(result == false)
                return false;
            st.clearBatch();
            boolean confirming = st.execute("SELECT * FROM " + tableName);
            ResultSet rs = st.getResultSet();
//            return null != rs.getObject(columnNames[0], columnTypes[0]);
            if(rs.getObject(columnNames[0], columnTypes[0]) != null){
                getLastUpdateTimeOfTables().put(tableName, DTService.getService().getTime());
                getWorkingTables().add(tableName);
                return true;
            }
            else
                return false;
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
            if(!alwaysContinue){
                System.out.println("Gizli bir alanın getter yöntemi bulunamadı; veritabanı ayarında eksik değerlerle işleme devâm etme kapalı olduğu için işlem sonlandırılıyor");
                return false;
            }
        }
        String tableName = entity.getClass().getSimpleName();
        StringBuilder query = new StringBuilder("INSERT INTO " + tableName + "(");
        Field[] fields = entity.getClass().getDeclaredFields();
        int takedAttributes = 0;
        ArrayList<Field> liTakedFields = new ArrayList<>();
        for(int sayac = 0; sayac < fields.length; sayac++){
            if(takeThisField(fields[sayac].getModifiers())){
                liTakedFields.add(fields[sayac]);
                takedAttributes++;
            }
        }
        Field[] taked = new Field[liTakedFields.size()];
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
        Object value;
        int writedAttribute = 0;
        System.out.println("hâzırlanan sorgu cümlesi : " + query.toString());
        for(int sayac = 0; sayac < taked.length; sayac++){
            value = null;
            try{
                value = taked[sayac].get(entity);
            }
            catch(IllegalArgumentException ex){
                System.out.println("Hatâ, geçersiz değer : " + ex.getMessage());
            }
            catch(IllegalAccessException ex){
                try{
                    Method getter = entity.getClass().getMethod("get" + convertFirstLetterToUpper(taked[sayac].getName()), null);
                    try{
                        value = getter.invoke(entity, null);
                    }
                    catch(IllegalAccessException ex1){
                        System.out.println("Hatâ, yetkisiz erişim...");
                        if(!alwaysContinue){
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
                    if(!alwaysContinue)
                        return false;
                    value = null;
                }
                catch(SecurityException ex1){
                    System.out.println("Hatâ, güvenlikle alâkalı...");
                }
//                System.err.println("hatâ, yetkisiz erişim : " + ex.getMessage());
            }
            try{
//                    System.out.println("alınan değer : " + value.toString());
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
    public Object[] getData(Class target){//Tablodaki verilerin büyük olması durumunda verilerin tamâmını almak için bunları bigint gibi sayılarda tutmalıyız
        String tableName = target.getSimpleName();
        if(target == null)
            return null;
        if(bufferMode){
            Object[] data = getDataFromBuffer(tableName);
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
        boolean go = false;
        try{
            st = this.getConnectivity().getConnext().createStatement(
                    ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            result = st.executeQuery("SHOW TABLES");
//            st.close();
        }
        catch(SQLException ex){
            System.out.println("hatâ : " + ex.getMessage());
            return null;
        }
        if(st == null)
            return null;
        if(result == null)
            return null;
        try{
            while(result.next()){
                String name = result.getString("Tables_in_" + connectivity.getSchemaName());
//                System.out.println("tablo ismi : " + name);
                if(name != null)
                    if(name.equalsIgnoreCase(tableName)){
                        go = true;
                        break;
                    }
            }
        }
        catch(SQLException ex){
            System.out.println("hatâ : " + ex.getMessage());
            return null;
        }
        if(!go)
            return null;
//        System.out.println("Aşama 2...");
        Object[] data;
        ArrayList<Object> liData;
        result = null;
        ArrayList<HashMap<String, Object>> liDataBeforeInstance = null;
        try{
            st = this.getConnectivity().getConnext().createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            result = st.executeQuery("SELECT * FROM " + tableName);
            if(result == null){//Tabloda hiç satır yoksa
                data = new Object[1];
                data[0] = null;
                return data;
            }
            liData = new ArrayList<Object>();
            int columnCount = result.getMetaData().getColumnCount();
            liDataBeforeInstance = new ArrayList<>();
            while(result.next()){
                HashMap<String, Object> map = new HashMap<>();
                for(int sayac = 1; sayac < columnCount + 1; sayac++){
                    map.put(result.getMetaData().getColumnLabel(sayac), result.getObject(sayac));
                }
                liDataBeforeInstance.add(map);
            }
        }
        catch(SQLException ex){
            System.out.println("hatâ : " + ex.getMessage());
            return null;
        }
        //ELDEKİLER:
            //liDataBeforeInstance : Her bir eleman için özellik, değer çifti içeren harita listesi, tipi : ArrayList<HashMap<String, Object>>
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
            for(HashMap<String, Object> mapOfAttributes : liDataBeforeInstance){
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
                liData.add(assignAttributes(instance, target, mapOfAttributes, mapFields));
            }
            data = new Object[liData.size()];
            liData.toArray(data);
//            System.out.println("Üretilen değişkenin sınıf ismi : " + data[0].getClass().getName());
//            System.out.println("Veri tipi dönüşümü yapılmış değişkenin sınıf ismi : " + target.cast(data[0]).getClass().getName());
            return data;
        }
        //Eğer parametresiz yapıcı fonksiyon yoksa:
        //.;.
        if(workWithNonParameterConstructor && css[0].getParameterCount() != 0){
                System.out.println("Parametresiz yapıcı yöntemle çalışma etkin; lâkin sınıfın böyle bir kurucu yöntemi yok!");
                return null;
        }
        //.;.
        //GEÇİCİ:
        return null;
    }
    public void test(String tableName){
        Statement st = null;
        ResultSet result = null;
        try{
            st = this.getConnectivity().getConnext().createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
            result = st.executeQuery("SHOW TABLES");
            while(result.next()){
                String name = result.getString("Tables_in_" + this.getConnectivity().getSchemaName());
                System.out.println("tablo ismi : " + name);
            }
        }
        catch(SQLException ex){
            System.out.println("hatâ : " + ex.getMessage());
        }
        
        //DENEME - 2:
        try{
            st = this.getConnectivity().getConnext().createStatement(
                ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
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
                System.out.println("Alan sayısı : " + sayac);
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
           if(Cvity.getTableNamesOnDB(connectivity.getConnext()) == null)
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
        return getAttributesPolicy().get(strModifier);
    }
    private String getTypeNameForDB(Class cl){
//        System.err.println("typeName : " + cl.getTypeName());
        String typeName = "";
        switch(cl.getTypeName()){
            case "java.lang.String" :{
                typeName = "text";
                break;
            }
            case "int" :{
                typeName = "int";
                break;
            }
            case "boolean" :{
                typeName = "bit";
                break;
            }
            case "java.util.Date" :{
                if(takeDateAttributeAsDateTime)
                    typeName = "DATETIME";
                else
                    typeName = "DATE";
                break;
            }
        }
        return typeName;
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
        for(String s : mapAttributes.keySet()){
            Field f = mapFields.get(s);
            if(f == null)//Ola ki ilgili özellik sınıfta yoksa, atama yapmaya çalışma
                continue;
            if(f.getModifiers() != 1){
                try{
                    Method setter = cl.getDeclaredMethod("set" + convertFirstLetterToUpper(f.getName()), new Class[]{f.getType()});
                    try{
                        setter.invoke(instance, mapAttributes.get(s));
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
                    f.set(instance, mapAttributes.get(s));
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
    private Object[] getDataFromBuffer(String tableName){
        if(!bufferMode){
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
    private void refreshDataOnLocalArea(Object[] data){//Veri saklama modu (bufferMode) etkin ise verileri verilerin saklandığı yerel alandaki ilgili yeri güncelle
        if(!bufferMode)
            return;
        String tableName = data.getClass().getName();
        System.out.println("data.getClass().getName");
        getBufferTables().put(tableName, data);
    }
    private boolean removeDataOnLocalArea(Object[] data){//Veri saklama modu açıkken bir tablo silindiğinde eğer o tablodaki kayıtlar saklanıyor idiyse, onları sil
        if(!bufferMode)
            return false;
        String tableName = data.getClass().getName();
        if(getBufferTables().get(tableName) == null){
            System.err.println("İlgili tablo verisi veri saklama alanına eklenmemiş");
            return false;
        }
        getBufferTables().remove(tableName);
        return true;
    }
    private void setUpdateModeOfTable(String tableName, UpdateMode mode){
        if(!bufferMode)
            return;
        boolean isFound = false;
        for(String table : getWorkingTables()){
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
        
    }

//ERİŞİM YÖNTEMLERİ:
    public static IkizIdare getIkizIdare(){
        return ikiz;
    }
    public Cvity getConnectivity(){
        return connectivity;
    }
    public HashMap<String, Boolean> getAttributesPolicy(){//Varsayılan alan alma formülü = hepsini al
        if(attributesPolicy == null){
            attributesPolicy = new HashMap<>();
            attributesPolicy.put("public", true);
            attributesPolicy.put("private", true);
            attributesPolicy.put("default", false);
            attributesPolicy.put("protected", false);
        }
        return attributesPolicy;
    }
    public HashMap<String, UpdateMode> getUpdateModeOfTables(){
        if(updateModeOfTables == null){
            updateModeOfTables = new HashMap<String, UpdateMode>();
        }
        return updateModeOfTables;
    }
    //GİZLİ ERİŞİM YÖNTEMLERİ:
    private HashMap<String, Object[]> getBufferTables(){
        if(bufferTables == null)
            bufferTables = new HashMap<String, Object[]>();
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