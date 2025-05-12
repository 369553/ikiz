package ikiz;

import ReflectorRuntime.Reflector;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class DBAccessHelper implements IDBAccessHelper{
    private Cvity connectivity;
    private Character start;
    private Character end;

    public DBAccessHelper(Cvity connectivity){
        this.connectivity = connectivity;
        start = connectivity.getHelperForDBType().getStartSymbolOfName();
        end = connectivity.getHelperForDBType().getEndSymbolOfName();
    }

//İŞLEM YÖNTEMLERİ:
    @Override
    public List<String> getTableNames(){
        String sqlOrder = this.connectivity.getHelperForDBType().getSentenceForShowTables();
        Statement sentence = null;
        ResultSet res = null;
        List<String> tableNames = new ArrayList<String>();
        try{
            sentence = this.connectivity.getConnext().createStatement();
            res = sentence.executeQuery(sqlOrder);
            if(res != null){
                while(res.next()){
                    tableNames.add(res.getString(1));
                }
                return tableNames;
            }
            else{
                System.err.println("Veritabanı isimleri alınamadı!");
                return null;
            }
        }
        catch(SQLException exc){
            System.err.println("exc : " + exc.toString());
        }
            return null;
    }
    @Override
    public List<String> getFieldNames(String tableName){
        List<String> fieldNames = new ArrayList<String>();
        try{
            Statement st = this.connectivity.getConnext().createStatement();
            DatabaseMetaData md = this.connectivity.getConnext().getMetaData();
            ResultSet res = md.getColumns(this.connectivity.getConnext().getCatalog(), this.connectivity.getConnext().getSchema(), tableName, null);
            while(res.next()){
                fieldNames.add(res.getString("COLUMN_NAME"));
            }
        }
        catch(SQLException exc){
            System.err.println("exc : " + exc.toString());
        }
        return fieldNames;
    }
    /**
     * {@code java.sql.PreparedStatement} için hâzırlanan, parametrelerin
     * yerine '?' konulmuş SQL metnindeki '?' yerine değeri konur;
     * Parametreler sorgudaki sırayla uyumlu olmalıdır; çünkü sırasıyla konur
     * Bu fonksiyon, yerine konulacak parametreleri SQL'e uygun biçimde koyar
     * - Parametre {@code null} ise {@code NULL} değeri konur
     * - Metînler tek tırnak içerisine alınır
     * - {@code Character} verisi tek tırnak içerisine alınır
     * - {@code Boolean} değerler 1 ve 0 olarak konur
     * - Târih - saat SQL uyumlu formata çevrilir
     * - {@code Enum} verisi ({@code toString()}) tek tırnak içerisine alınır
     * - Diğer durumlarda nesnenin {@code toString()} değeri alınır (tırnak eklenmez)
     * NOT: Dizi biçimindeki değerler ayrıştırılmaz
     * NOT: Zamân verisi şu tiplerden birisi olabilir:
     * - {@code java.time.LocalDateTime}
     * - {@code java.time.LocalDate}
     * - {@code java.time.LocalTime}
     * - {@code java.util.Date}
     * NOT: Zamân verisi uyumsuzsa, yerine NULL değeri konur
     * NOT: Fonksiyon, parametre konulmamış sorgu metînleri için uygundur;
     * Eğer sorgu metni içerisinde '?' varsa bu fonksiyon hatâlı sonuç üretir!
     * @param queryAsPrepared {@code PreparedStatement} biçiminde SQL sorgu metni
     * @param parameters Sorguya zerk (enjekte) edilmek istenen değişkenler
     * @return Parametrelerin zerk edildiği saf SQL metni
     */
    public String getFinalSQLQuery(String queryAsPrepared, Object[] parameters){
        List<String> paramsAsSuitable = new ArrayList<String>();
        for(int sayac = 0; sayac < parameters.length; sayac++){
            if(parameters[sayac] == null){
                parameters[sayac] = "NULL";
                continue;
            }
            Class<?> src = parameters[sayac].getClass();
            if(src.equals(String.class) || src.equals(Character.class) || src.isEnum()){
                parameters[sayac] = ("'"
                        + addEscapeCharacterForQuotes(String.valueOf(parameters[sayac]))
                        + "'");
            }
            else if(src.getName().equals("java.time.LocalDateTime")
                || src.getName().equals("java.time.LocalDate")
                || src.getName().equals("java.time.LocalTime")
                || src.getName().equals("java.util.Date")){
                String asSQLStyle = Reflector.getService().getDateTimeTextAsSQLStyle(parameters[sayac]);
                if(asSQLStyle == null)
                    parameters[sayac] = "NULL";
                else
                    parameters[sayac] = ("'"
                        + asSQLStyle
                        + "'");
            }
            else if(src.equals(Boolean.class)){
                parameters[sayac] = (((boolean) parameters[sayac]) ? "1" : "0");
            }
        }
        StringBuilder bui = new StringBuilder();
        char[] tx = queryAsPrepared.toCharArray();
        int sayac = 0;
        for(char c : tx){
            if(c == '?'){
                if(sayac < parameters.length){
                    bui.append(String.valueOf(parameters[sayac]));
                    sayac++;
                }
            }
            else
                bui.append(c);
        }
        return bui.toString();
    }
    /**
     * Saf SQL sorgusu çalıştırır
     * Desteklenen yöntemler JDBC {@code Statement} nesnesinin şu metodlarıdır:<br>
     * - executeQuery(String sql) : ResultSet<br>
     * - executeUpdate(String sql) : int<br>
     * - execute(String sql) : boolean<br>
     * SQL sorgusunun hangi yöntemle çalıştırıldığında nasıl tepki verdiğini
     * bilmeniz gerekiyor
     * Misal, "TRUNCATE TABLE tablo;" komutu tüm tabloyu boşaltmasına rağmen
     * {@code executeUpdate()} ile çalıştırıldığında etkilenen satır sayısı
     * sıfır olarak döndürülür
     * Çekilen verinin sütun ismi yoksa, sütun ismi sütun sıra numarası yapılır
     * @param query Çalıştırılmak istenen SQL sorgusu
     * @param execToFetchData Veri çekmek isteniyorsa {@code true} verilmelidir
     * @param executeUpdate Sorgu, veri çekmek için değilse, çalıştırılacak
     * fonksiyon bu parametreye bakılarak belirlenir
     * Bu parametre {@code true} ise {@code executeUpdate()} çalıştırılır,
     * aksi hâlde {@code execute()} fonksiyonu çalıştırılır
     * @return {@code execToFetchData} {@code true} ise ve işlem başarılıysa
     * verileri {@code ArrayList} olarak döndürür, her veri bir {@code HashMap}
     * içerisindedir; anahtar sütun ismidir, değer o satırdaki sütun değeridir
     * {@code null} veriler de bu özellik haritasına eklenir
     * Veri çekme işlemi başarısızsa {@code false} döndürülür
     * İşlem başarılı; ama veri yoksa boş bir {@code ArrayList} döndürülür
     * {@code execToFetchData} {@code false} ve {@code executeUpdate} {@code true} ise
     * işlemden etkilenen satır varsa {@code true}, yoksa {@code false} döndürülür
     * {@code execToFetchData} {@code false} ve {@code executeUpdate} {@code false} ise
     * sorgunun çalıştırılma başarısını döndürür
     * Hatâlı parametre gönderilirse {@code null} döndürülür
     */
    protected Object execRawQuery(String query, boolean execToFetchData, boolean executeUpdate){
        return execRawQuery(query, execToFetchData, executeUpdate, false, null);
    }
    /**
     * Saf SQL sorgusunu verilen {@code Statement} nesnesiyle çalıştırır
     * Desteklenen yöntemler JDBC {@code Statement} nesnesinin şu metodlarıdır:<br>
     * - executeQuery(String sql) : ResultSet<br>
     * - executeUpdate(String sql) : int<br>
     * - execute(String sql) : boolean<br>
     * SQL sorgusunun hangi yöntemle çalıştırıldığında nasıl tepki verdiğini
     * bilmeniz gerekiyor
     * Misal, "TRUNCATE TABLE tablo;" komutu tüm tabloyu boşaltmasına rağmen
     * {@code executeUpdate()} ile çalıştırıldığında etkilenen satır sayısı
     * sıfır olarak döndürülür
     * Çekilen verinin sütun ismi yoksa, sütun ismi sütun sıra numarası yapılır
     * @param query Çalıştırılmak istenen SQL sorgusu
     * @param execToFetchData Veri çekmek isteniyorsa {@code true} verilmelidir
     * @param executeUpdate Sorgu, veri çekmek için değilse, çalıştırılacak
     * fonksiyon bu parametreye bakılarak belirlenir
     * Bu parametre {@code true} ise {@code executeUpdate()} çalıştırılır,
     * aksi hâlde {@code execute()} fonksiyonu çalıştırılır
     * @param st Sorgunun çalıştırılması istenen {@code Statement} nesnesi
     * @return {@code execToFetchData} {@code true} ise ve işlem başarılıysa
     * verileri {@code ArrayList} olarak döndürür, her veri bir {@code HashMap}
     * içerisindedir; anahtar sütun ismidir, değer o satırdaki sütun değeridir
     * {@code null} veriler de bu özellik haritasına eklenir
     * Veri çekme işlemi başarısızsa {@code false} döndürülür
     * İşlem başarılı; ama veri yoksa boş bir {@code ArrayList} döndürülür
     * {@code execToFetchData} {@code false} ve {@code executeUpdate} {@code true} ise
     * işlemden etkilenen satır varsa {@code true}, yoksa {@code false} döndürülür
     * {@code execToFetchData} {@code false} ve {@code executeUpdate} {@code false} ise
     * sorgunun çalıştırılma başarısını döndürür
     * Hatâlı parametre gönderilirse {@code null} döndürülür
     */
    public Object execRawQuery(String query, boolean execToFetchData, boolean executeUpdate, Statement st){
        return execRawQuery(query, execToFetchData, executeUpdate, true, st);
    }
    /**
     * Metîn içerisindeki tek tırnak ve çift tırnak işâretinin öncesine kaçış
     * karakteri konur
     * @param value Metîn
     * @return Tırnak işâretlerinden önce kaçış karakteri konulmuş metîn
     */
    public String addEscapeCharacterForQuotes(String value){
        if(value == null)
            return null;
        char[] tx = value.toCharArray();
        StringBuilder bui = new StringBuilder();
        for(char c : tx){
            if(c == '\'' || c == '"')
                bui.append("\\");
            bui.append(c);
        }
        return bui.toString();
    }
    @Override
    public boolean deleteRow(String tableName, String whereCondition, Object answerOfWhereCondition){
        if(tableName == null)
            return false;
        StringBuilder sqlOrder = new StringBuilder();
        sqlOrder.append("DELETE FROM ").append((start != null ? start : "")).append(tableName).append((end != null ? end : ""));
        if(whereCondition != null && answerOfWhereCondition != null){
            sqlOrder.append(" WHERE ").append((start != null ? start : "")).append(whereCondition).append((end != null ? end : "")).append(" = ").append("?");
        }
        sqlOrder.append(";");
        try{
            PreparedStatement preSt = connectivity.getConnext().prepareStatement(sqlOrder.toString());
            if(answerOfWhereCondition != null){
                if(answerOfWhereCondition.getClass().equals(char.class) || answerOfWhereCondition.getClass().equals(Character.class)){
                    preSt.setObject(1, answerOfWhereCondition, java.sql.JDBCType.CHAR);
                }
                else{
                    preSt.setObject(1, answerOfWhereCondition);
                }
            }
            return (preSt.executeUpdate() > 0);
        }
        catch(SQLException exc){
            System.err.println("exc : " + exc.toString());
            return false;
        }
    }
    @Override
    public boolean deleteRow(String tableName, String[] whereConditions, Object[] answerOfWhereConditions){
        if(tableName == null)
            return false;
        StringBuilder sqlOrder = new StringBuilder();
        sqlOrder.append("DELETE FROM ").append((start != null ? start : "")).append(tableName).append((end != null ? end : ""));
        int put = 0;
        if(whereConditions != null && answerOfWhereConditions != null){
            sqlOrder.append(" WHERE ");
            for(int sayac = 0; sayac < whereConditions.length; sayac++){
                if(whereConditions[sayac] == null)
                    continue;
                if(put > 0)
                    sqlOrder.append(" AND ");
                sqlOrder.append((start != null ? start : "")).append(whereConditions[sayac]).append((end != null ? end : "")).append("=").append("?");
                put++;
            }
        }
        sqlOrder.append(";");
        try{
            PreparedStatement preSt = connectivity.getConnext().prepareStatement(sqlOrder.toString());
            for(int sayac = 0; sayac < put; sayac++){
                if(whereConditions[sayac] == null)
                    continue;
                Object value = answerOfWhereConditions[sayac];
                if(value == null)// Bu satır boş yere yazılmadı!
                    preSt.setObject(sayac + 1, value);
                if(answerOfWhereConditions[sayac].getClass().equals(char.class) || answerOfWhereConditions[sayac].getClass().equals(Character.class))
                    preSt.setObject(sayac + 1, value, java.sql.JDBCType.CHAR);
                else if(answerOfWhereConditions[sayac].getClass().isEnum())
                    preSt.setObject(sayac + 1, value.toString());
                else
                    preSt.setObject(sayac + 1, value);
            }
            System.out.println("Hâzırlanan sorgu : " + sqlOrder.toString());
            return (preSt.executeUpdate() > 0);
        }
        catch(SQLException exc){
            System.err.println("exc : " + exc.toString());
            return false;
        }
    }
    @Override
    public boolean updateRow(String tableName, String whereCondition, Object answerOfWhereCondition, Map<String, Object> fieldsToValues){
        String[] conds = new String[]{whereCondition};
        Object[] answers = new Object[]{answerOfWhereCondition};
        return updateRow(tableName, conds, answers, fieldsToValues);
    }
    @Override
    public boolean updateRow(String tableName, String[] whereConditions, Object[] answerOfWhereConditions, Map<String, Object> fieldsToValues){
        if(tableName == null || fieldsToValues == null)
            return false;
        if(fieldsToValues.isEmpty())
            return false;
        StringBuilder sqlOrder = new StringBuilder();
        sqlOrder.append("UPDATE ").append((start != null ? start : "")).append(tableName).append((end != null ? end : "")).append(" SET ");
        int valuePut = 0;
        for(String key : fieldsToValues.keySet()){
            if(key == null)
                continue;
            if(key.isEmpty())
                continue;
            sqlOrder.append((start != null ? start : "")).append(key).append((end != null ? end : "")).append(" = ?").append(", ");
            valuePut++;
        }
        if(valuePut > 0)// Herhangi bir değer konmuşsa;
            sqlOrder.delete(sqlOrder.length() - 2, sqlOrder.length());// Sona koyulan fazla virgülü sil
        int put = 0;
        if(whereConditions != null && answerOfWhereConditions != null){
            sqlOrder.append(" WHERE ");
            for(int sayac = 0; sayac < whereConditions.length; sayac++){
                if(whereConditions[sayac] == null)
                    continue;
                if(put > 0)
                    sqlOrder.append(" AND ");
                sqlOrder.append((start != null ? start : "")).append(whereConditions[sayac]).append((end != null ? end : "")).append("=").append("?");
                put++;
            }
        }
        sqlOrder.append(";");
//        System.err.println("Sorgu : " + sqlOrder.toString());
        try{
            PreparedStatement preSt = connectivity.getConnext().prepareStatement(sqlOrder.toString());
            int injected = 0;
            for(String key : fieldsToValues.keySet()){
                if(key == null)
                    continue;
                if(key.isEmpty())
                    continue;
                Object value = fieldsToValues.get(key);
                if(value == null)// Bu satır boş yere yazılmadı!
                    preSt.setObject(injected + 1, value);
                else if(value.getClass().equals(char.class) || value.getClass().equals(Character.class))
                    preSt.setObject(injected + 1, value, java.sql.JDBCType.CHAR);
                else if(value.getClass().isEnum())
                    preSt.setObject(injected + 1, value.toString());
                else
                    preSt.setObject(injected + 1, value);
                injected++;
            }
            for(int sayac = 0; sayac < put; sayac++){
                if(whereConditions[sayac] == null)
                    continue;
                Object value = answerOfWhereConditions[sayac];
                if(value == null)// Bu satır boş yere yazılmadı!
                    preSt.setObject(injected + sayac + 1, value);
                else if(answerOfWhereConditions[sayac].getClass().equals(char.class) || answerOfWhereConditions[sayac].getClass().equals(Character.class))
                    preSt.setObject(injected + sayac + 1, value, java.sql.JDBCType.CHAR);
                else if(answerOfWhereConditions[sayac].getClass().isEnum())
                    preSt.setObject(injected + sayac + 1, value.toString());
                else
                    preSt.setObject(injected + sayac + 1, value);
            }
            int numberOfUpdated = preSt.executeUpdate();
//            System.out.println("Şu kadar satır tazelendi : " + numberOfUpdated);
            return (numberOfUpdated > 0);
        }
        catch(SQLException exc){
            System.err.println("exc : " + exc.toString());
            return false;
        }
    }
    @Override
    public List<Map<String, Object>> getData(String tableName){
       return getData(tableName, null, null, null);
    }
    @Override
    public List<Map<String, Object>> getData(String tableName, List<String> fieldNames){
        return getData(tableName, fieldNames, null, null);
    }
    @Override
    public List<Map<String, Object>> getDataForOneWhereCondition(String tableName, List<String> fieldNames, String whereCondition, Object answerOfWhereCondition){
        return getData(tableName, fieldNames, new String[]{whereCondition}, new Object[]{answerOfWhereCondition});
    }
    @Override
    public List<Map<String, Object>> getData(String tableName, List<String> fieldNames, String[] whereConditions, Object[] answerOfWhereConditions){
        if(tableName == null)
            return null;
        if(tableName.isEmpty())
            return null;
        StringBuilder query = new StringBuilder();
        int whereCounter = 0;
        query.append("SELECT ");
        if(fieldNames == null)
            query.append("*");
        else{
            for(String s : fieldNames){
                if(s != null)
                    query.append((start != null ? start : "")).append(s).append((end != null ? end : "")).append(", ");
            }
            query.delete(query.length() - 2, query.length());// En sona eklenen ', ' karakterlerini sil.
        }
        query.append(" FROM ").append((start != null ? start : "")).append(tableName).append((end != null ? end : ""));
        if(whereConditions != null && answerOfWhereConditions != null){
            for(String s : whereConditions){
                if(s == null)
                    continue;
                if(whereCounter == 0)
                    query.append(" WHERE ");
                query.append((start != null ? start : "")).append(s).append((end != null ? end : "")).append(" = ?").append(" AND ");
                whereCounter++;
            }
            query.delete(query.length() - 5, query.length());
        }
        query.append(";");
        System.err.println("Sorgu metni : " + query.toString());
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        try{
            PreparedStatement preSt = connectivity.getConnext().prepareStatement(query.toString());
            for(int sayac = 0; sayac < whereCounter; sayac++){
                Object value = answerOfWhereConditions[sayac];
                if(value == null)// Bu satır boş yere yazılmadı!
                    preSt.setObject(sayac + 1, value);
                else if(value.getClass().equals(char.class) || value.getClass().equals(Character.class))
                    preSt.setObject(sayac + 1, value, java.sql.JDBCType.CHAR);
                else if(value.getClass().isEnum())
                    preSt.setObject(sayac + 1, value.toString());
                else if(value.getClass().equals(String.class))
                    preSt.setString(sayac + 1, (String) value);
                else
                    preSt.setObject(sayac + 1, value);
            }
            ResultSet res = preSt.executeQuery();
            if(res == null)
                return null;
            int colNumber = res.getMetaData().getColumnCount();
            while(res.next()){
                Map<String, Object> row = new HashMap<String, Object>();
                for(int sayac = 0; sayac < colNumber; sayac++){
                    row.put(res.getMetaData().getColumnName(sayac + 1), res.getObject(sayac + 1));
                }
                data.add(row);
            }
        }
        catch(SQLException exc){
            System.err.println("exc : " + exc.toString());
        }
        return data;
    }
    public boolean checkIsTableInDB(String tableName){
        if(tableName == null)
            return false;
        if(tableName.isEmpty())
            return false;
        try{
            Statement st = connectivity.getConnext().createStatement();
            ResultSet rs = st.executeQuery(this.connectivity.getHelperForDBType().getSentenceForShowTables());
            if(rs != null){
                while(rs.next()){
                    if(rs.getString(1).equalsIgnoreCase(tableName))
                        return true;
                }
            }
        }
        catch(SQLException exc){
            System.err.println("exc : " + exc.toString());
        }
        return false;
    }
    // ARKAPLAN İŞLEM YÖNTEMLERİ:
    private Object execRawQuery(String query, boolean execToFetchData, boolean executeUpdate, boolean useGivenStatement, Statement st){
        if(query == null)
            return null;
        if(query.isEmpty())
            return null;
        if(useGivenStatement){
            if(st == null)
                return null;
//            else{// Güvenlik amaçlı düşünülmüştü; fakat uygun değil, farklı bir yöntem bul
//                try{
//                    if(st.getConnection() != this.connectivity)
//                    return null;
//                }
//                catch(SQLException exc){
//                    System.err.println("Bağlantı denetimi yapılırken hatâ oluştu; sorgu çalıştırılmıyor: " + exc.toString());
//                }
//            }
        }
        try{
            st = (useGivenStatement ? st : this.connectivity.getConnext().createStatement());
            if(execToFetchData){
                ArrayList<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
                ResultSet res = st.executeQuery(query);
                int colCount = res.getMetaData().getColumnCount();
                while(res.next()){
                    HashMap<String, Object> values = new HashMap<String, Object>();
                    for(int sayac = 0; sayac < colCount; sayac++){
                        boolean takeByOrderNumber = false;
                        String key = res.getMetaData().getColumnName(sayac + 1);
                        if(key == null)
                            takeByOrderNumber = true;
                        else if(key.isEmpty())
                            takeByOrderNumber = true;
                        if(!takeByOrderNumber)
                            values.put(key, res.getObject(key));
                        else
                            values.put(String.valueOf(sayac + 1), res.getObject(sayac + 1));
                    }
                    rows.add(values);
                }
                return rows;
            }
            else if(executeUpdate){
                return (st.executeUpdate(query) > 0);
            }
            else{
                st.execute(query);
                return true;
            }
        }
        catch(SQLException | NullPointerException exc){
            System.err.println("Saf sorgu çalıştırılırken hatâ oluştu : " + exc.toString());
        }
        return false;
    }

//ERİŞİM YÖNTEMLERİ:
    
    
}