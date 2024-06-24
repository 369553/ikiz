package ikiz;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DBAccessMySQL implements IDBAccess{
    private String url;// Veritabanı bağlantı metni
    private String username;// Veritabanı bağlantı kullanıcı adı
    private String secret;// Veritabanı bağlantı şifresi
    private int port;// Veritabanı sunucusu bağlantı port numarası
    private boolean isChanged = true;
    private boolean isConnectedToDB = false;// Mevcut bağlantı veritabanı gövdesine mi, yoksa veritabanındaki bir veritabanına mı?
    private Connection connext;// Mevcut bağlantı
    private Statement STquery;//
    private int amountOfDefaultVARCHAR = 50;// 'String' veri tipini MySQL veri tipine ('VARCHAR') çevirirken atanan varsayılan uzunluk

    public DBAccessMySQL(String url, String username, String password){
        this.url = url;
        this.username = username;
        this.secret = password;
    }
    public DBAccessMySQL(){
        this("jdbc:mysql://localhost:3306",
                "root", "LINQSE.1177");
    }

//İŞLEM YÖNTEMLERİ:
    public static String prepareURLString(String host, int port, String dbName){
        StringBuilder con = new StringBuilder();
        con.append("jdbc:mysql:").append(host).append(":").append(port).append("/").
                append(dbName);
        return con.toString();
    }
    public static ArrayList<String> getCatalogNames(Connection connection){
        String order = "SHOW DATABASES;";
        Statement sent = null;
        ResultSet rs = null;
        ArrayList<String> schemaNames = new ArrayList<String>();
        try{
            sent = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = sent.executeQuery(order);
            while(rs.next()){
                schemaNames.add(rs.getString("Database"));
            }
        }
        catch (SQLException DBException){
            DBAccessMySQL.showErrorMessage(DBException);
        }
        if(sent == null)
            return null;
        if(rs == null)
            return null;
        return schemaNames;
    }
    public boolean createTable(Connection con, String tableName, String[] fieldNames, Class[] fieldDataTypes){// 'Connection' parametresinin alınmaması lazım
        boolean isSuccess = false;
        StringBuilder bui = new StringBuilder();
        bui.append("CREATE TABLE ").append(tableName).append(" (");
        for(int sayac = 0; sayac < fieldNames.length; sayac++){
            bui.append(fieldNames[sayac]).append(" ")
                    .append(getMySQLDataType(fieldDataTypes[sayac]));
            if(sayac < fieldNames.length - 1)
                bui.append(", ");
        }
        bui.append(") DEFAULT CHARSET = utf8mb4;");
        try{
            Statement sta = con.createStatement();
            System.err.println("tablo oluşturma kodu : " + bui.toString());
            return sta.execute(bui.toString());
        }
        catch(SQLException exc){
            showErrorMessage(exc);
            return false;
        }
    }
    public static boolean createDatabase(Connection con, String dbName){
        try{
            Statement sta = con.createStatement();
            sta.execute("CREATE DATABASE " + dbName);
            return true;
        }
        catch(SQLException exc){
            showErrorMessage(exc);
            return false;
        }
    }
    public boolean connectToDB(String dbName){
        if(isConnectedToDB){
            int lastPoint = url.lastIndexOf("/");
            url = url.substring(0, lastPoint);
            System.err.println("Yeni url = " + url);
            isChanged = true;
        }
        url += "/" + dbName;
        if(getConnext() != null){
            isConnectedToDB = true;
            return true;
        }
        else
            return false;
    }
    public static String[] getSchemaNames(Connection con){
        String order = "SHOW DATABASES;";
        ResultSet res = null;
        ArrayList<String> schemas = new ArrayList<String>();
        String[] values = null;
        try{
            // Veritabanı isimleri metaData ile de alınabiliyor sanırım, bu daha güvenli, daha doğru bir usûl olabilir : getConnext().getMetaData().getSchemas();
            res = con.createStatement().executeQuery(order);
            while(res.next()){
                schemas.add(res.getString("Database"));
            }
        }
        catch(SQLException exc){
            showErrorMessage(exc);
        }
        if(!schemas.isEmpty()){
            values = new String[schemas.size()];
            schemas.toArray(values);
        }
        return values;
    }
    public Statement produceStatement(){
        Statement stmnt = null;
        try{
            stmnt = getConnext().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        }
        catch(SQLException exc){
            showErrorMessage(exc);
        }
        return stmnt;
    }
    public PreparedStatement producePrepareStatement(String sqlOrder){
        PreparedStatement preSt = null;
        try{
            preSt = getConnext().prepareStatement(sqlOrder);
        }
        catch(SQLException exc){
            showErrorMessage(exc);
        }
        return preSt;
    }
    public ArrayList<HashMap<String, Object>> getAllData(String tblName, String[] fieldsWhichWanted){
        return getData(tblName, fieldsWhichWanted, null, null);
    }
    public ArrayList<HashMap<String, Object>> getAllDataWithAllFields(String tblName){
        return getData(tblName, null, null, null);
    }
    public ArrayList<HashMap<String, Object>> getData(String tblName, String[] fieldsWhichWanted, String whereCondition, Object answerOfWhereCondition){
        /*
            Dönüş tipi : Arraylist<satır<sütun_ismi, sütun_verisi>>
            Veri yoksa, boş 'ArrayList' döndürüyor;
            Hatâ alınırsa 'null' döndürüyor
        */
        ResultSet res;// Gelen sonuçlar almak için
        ArrayList<HashMap<String, Object>> objectsOfRes;// Gelen sonuçları yazılıma aktarmak için
        StringBuilder bui = new StringBuilder();// Sorgu cümlesi hâzırlamak için
        
        bui.append("SELECT ");
        if(fieldsWhichWanted == null || fieldsWhichWanted.length == 0)// İstenen alanlar belirtilmediyse tüm alanları çek
            bui.append("*");
        else{
            for(int sayac = 0; sayac < fieldsWhichWanted.length; sayac++){
                bui.append(fieldsWhichWanted[sayac]);
                if(sayac < fieldsWhichWanted.length - 1){
                    bui.append(",");
                }
            }
        }
        bui.append(" FROM ").append(tblName);
        if(whereCondition != null){// 'WHERE' şartı belirtildiyse
            bui.append(" WHERE ").append("whereCondition = ");
            Class type = answerOfWhereCondition.getClass();
            if(type == String.class || type == Date.class){
                bui.append("\"").append(answerOfWhereCondition.toString()).
                        append("\"");
            }
            else
                bui.append(answerOfWhereCondition.toString());
        }
        bui.append(";");
        PreparedStatement preSta = producePrepareStatement(bui.toString());
        if(preSta == null)
            return null;// Hatâ
        try{
            res = preSta.executeQuery();
        }
        catch(SQLException exc){
            showErrorMessage(exc);
            return null;
        }
        if(res == null)
            return null;
        objectsOfRes = new ArrayList<HashMap<String, Object>>();
        ResultSetMetaData info;
        try{
            info = res.getMetaData();
            if(info == null){System.err.println("Sorgudna dönen metadata = null");
                return null;}
            while(res.next()){// Tüm satırları dolaş
                HashMap<String, Object> row = new HashMap<String, Object>();
                for(int sayac = 1; sayac <= info.getColumnCount(); sayac++){
                    String col = info.getColumnName(sayac);// Sütun ismi
//                    info.getColumnClassName(sayac);
                    row.put(col, res.getObject(col));
                }
                objectsOfRes.add(row);
            }
        }
        catch(SQLException exc){
            showErrorMessage(exc);
            return null;
        }
        return objectsOfRes;
    }
    public ArrayList<HashMap<String, Object>> getDataWithAllFields(String tblName, String whereCondition, Object answerOfWhereCondition){
        return getData(tblName, null, whereCondition, answerOfWhereCondition);
    }
    public boolean addRowToDB(String tableName, String[] fields, Object[] values){
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
        sentence.append("(");
        for(int index = 0; index < values.length; index++){
            sentence.append("?");
            if(index != fields.length - 1)
                sentence.append(", ");
        }
        sentence.append(")");
//        System.err.println("sentence not : " + sentence.toString());
        try{
            PreparedStatement preStatement = getConnext().prepareStatement(sentence.toString());
            for(int index = 1; index <= values.length; index++){
//                    System.out.println("Alan : " + fields[index - 1] + "\nSıra no : " + index + "\nYerleştirilen değer : " + values[index - 1].toString());
                preStatement.setObject(index, values[index - 1]);
            }
            System.out.println("Ekleme cümlesi : " + preStatement.toString());
            changedRowNumber = preStatement.executeUpdate();
        }
        catch(SQLException DBException){
            showErrorMessage(DBException);
        }
        if(changedRowNumber == 0)
            return false;
        return true;
    }
    public boolean delRowToDB(IEntity entity, String tableName, String primaryKeyName){
        String sentence = "DELETE FROM " + tableName + " WHERE " + primaryKeyName + " = ?";
        try{
            PreparedStatement preStatement = getConnext().prepareStatement(sentence);
            preStatement.setInt(1, entity.getID());
            return preStatement.execute();
        }
        catch(SQLException DBException){
            showErrorMessage(DBException);
        }
        return false;
    }
    public boolean updRowToDB(String tableName, String[] fields, Object[] values, String whereCondition, Object conditionAnswer){
        StringBuilder sentence = new StringBuilder("UPDATE " + tableName + " SET ");
        int changedRowNumber = 0;
        int indexOfQuestionMarks = 1;
        for(int index = 0; index < fields.length; index++){
            sentence.append(fields[index] + " = ?" );
//                sentence.append(values[index]);
            if(index != fields.length - 1)
                sentence.append(", ");
        }
        sentence.append(" WHERE " + whereCondition + " = ?");
//                System.out.println("sentence verisiz hâli : " + sentence);
        PreparedStatement preStatement = null;
        try{
            preStatement = getConnext().prepareStatement(sentence.toString());
            for(int index = 1; index <= fields.length; index++){
                preStatement.setObject(index, values[index - 1]);
                indexOfQuestionMarks++;
            }
            if(conditionAnswer.getClass().getName().equals(String.class.getName()))
                preStatement.setString(indexOfQuestionMarks, (String) conditionAnswer);
            else if(conditionAnswer.getClass().getName().equals(Integer.class.getName()))
                preStatement.setInt(indexOfQuestionMarks, (int) conditionAnswer);
            System.out.println("sentence son hâli : " + preStatement.toString());
            changedRowNumber = preStatement.executeUpdate();
            return true;
        }
        catch(SQLException DBException){
            showErrorMessage(DBException);
        }
        return false;
    }
    public boolean updRowToDB(IEntity entity, String tableName, String[] fields, Object[] values){
        return updRowToDB(tableName, fields, values, "id", entity.getID());
    }
    //ARKAPLAN İŞLEM YÖNTEMLERİ:
    private static void showErrorMessage(SQLException DBException){//BURASI GELİŞTİRİLEBİLİR, HATALAR DAHA İYİ YÖNETİLEBİLİR Bİ İZNİLLÂH
        System.out.println("Hatâ kodu : " + DBException.getErrorCode() + "\n" + DBException.getMessage());
    }
    private String getMySQLDataType(Class dType){// Düzenlenmesi lazım!
        dType = wrapPrimitiveType(dType);
        if(dType == Integer.class)
            return "int";
        if(dType == Double.class)
            return "double";
        if(dType == Short.class)
            return "smallint";
        if(dType == Boolean.class)
            return "bit";
        if(dType == String.class ||dType == Character.class)
            return "VARCHAR(" + amountOfDefaultVARCHAR + ")";
        if(dType == Date.class)
            return "DATETIME";
        if(dType == Long.class)
            return "BIGINT";
        if(dType == Byte.class)// ?
            return "BLOB";
        return null;
    }
    private Class wrapPrimitiveType(Class type){
        if(type.equals(int.class))
            return Integer.class;
        else if(type.equals(double.class))
            return Double.class;
        else if(type.equals(boolean.class))
            return Boolean.class;
        else if(type.equals(char.class))
            return Character.class;
        else if(type.equals(byte.class))
            return Byte.class;
        else if(type.equals(long.class))
            return Long.class;
        else if(type.equals(short.class))
            return Short.class;
        else
            return type;
    }

//ERİŞİM YÖNTEMLERİ:
    public Connection getConnext(){
        if(isChanged)
            connext = null;
        if(connext == null){
            try{
                connext = DriverManager.getConnection(url, username, secret);
                System.out.println("Bağlantı kuruldu");
                isChanged = false;
                return connext;
            }
            catch(SQLException DBException){
                showErrorMessage(DBException);
                return null;
            }
        }
        return connext;
    }
    public Statement getSTquery(){
        if(STquery == null){
            STquery = produceStatement();
        }
        return STquery;
    }
}