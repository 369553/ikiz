package ikiz;

import java.util.HashMap;

public class HelperForSQLite implements HelperForDBType{
    private HashMap<String, String> dataTypes = getMapOfDataTypeToDBDataType();

//İŞLEM YÖNTEMLERİ:
    @Override
    public String getSentenceForShowTables(){
        return "";
    }
    @Override
    public String getConnectionString(String hostName, int portNumber){
        return null;
    }
    @Override
    public String getConnectionString(String hostName, int portNumber, String dbName){
        return "jdbc:sqlite:" + dbName;
    }
    @Override
    public Character getStartSymbolOfName(){
        return '`';
    }
    @Override
    public Character getEndSymbolOfName(){
        return '`';
    }
    @Override
    public HashMap<String, String> getMapOfDataTypeToDBDataType(){
        HashMap<String, String> values = new HashMap<String, String>();
        values.put("int", "");
        values.put("java.lang.Integer", "");
        values.put("java.lang.String", "");
        values.put("double", "");
        values.put("java.lang.Double", "");
        values.put("float", "");
        values.put("java.lang.Float", "");
        values.put("boolean", "");
        values.put("java.lang.Boolean", "");
        values.put("char", "");
        values.put("java.lang.Character", "");
        values.put("short", "");
        values.put("java.lang.Short", "");
        values.put("long", "");
        values.put("java.lang.Long", "");
        values.put("byte", "");
        values.put("java.lang.Byte", "");
        values.put("java.time.LocalDate", "");// Târih - zamân
        values.put("java.time.LocalDateTime", "");// Târih - zamân
        values.put("java.time.LocalTime", "");// Târih - zamân
        values.put("java.util.Date", "");// Târih - zamân
        values.put("java.sql.Date", "");// Târih - zamân
        values.put("java.lang.Number", "");
        return values;
    }
    @Override
    public boolean isSupported(String dataTypeName){
        for(String d : this.dataTypes.keySet()){
            if(d.equalsIgnoreCase(dataTypeName))
                return true;
        }
        return false;
    }
    @Override
    public String getDataTypeNameForJSON(){
        return "JSON";
    }
    @Override
    public String getDataTypeNameForEnum(){
        return "";
    }
    @Override
    public String getDatabaseProductName(){
        return "SQLite";
    }
}