package ikiz;

import java.util.HashMap;

public class HelperForMySQL implements HelperForDBType{
    private HashMap<String, String> dataTypes = getMapOfDataTypeToDBDataType();

// İŞLEM YÖNTEMLERİ:
    @Override
    public String getSentenceForShowTables(){
        return "SHOW TABLES;";
    }
    @Override
    public String getConnectionString(String hostName, int portNumber){
        return "jdbc:mysql://" + hostName + ":" + portNumber;
    }
    @Override
    public String getConnectionString(String hostName, int portNumber, String dbName){
        return "jdbc:mysql://" + hostName + ":" + portNumber + "/" + dbName;
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
        values.put("int", "INT");
        values.put("java.lang.Integer", "INT");
        values.put("java.lang.String", "VARCHAR(500)");
//        values.put("java.lang.StringBuilder", "");
//        values.put("java.nio.CharBuffer", "");
        values.put("double", "DOUBLE");
        values.put("java.lang.Double", "DOUBLE");
        values.put("float", "FLOAT");
        values.put("java.lang.Float", "FLOAT");
        values.put("boolean", "BIT");
        values.put("java.lang.Boolean", "BIT");
        values.put("char", "CHAR(1)");
        values.put("java.lang.Character", "CHAR(1)");
        values.put("short", "SMALLINT");
        values.put("java.lang.Short", "SMALLINT");
        values.put("long", "BIGINT");
        values.put("java.lang.Long", "BIGINT");
        values.put("byte", "BINARY(1)");
        values.put("java.lang.Byte", "BINARY(1)");
        values.put("java.time.LocalDate", "DATE");// Târih - zamân
        values.put("java.time.LocalDateTime", "DATETIME");// Târih - zamân
        values.put("java.time.LocalTime", "TIME");// Târih - zamân
        values.put("java.util.Date", "DATETIME");// Târih - zamân
        values.put("java.sql.Date", "DATE");// Târih - zamân
        values.put("java.lang.Number", "DEC(19,9)");
        values.put("java.io.File", "MEDIUMBLOB");
        values.put("java.lang.Enum", "ENUM");
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
        return "ENUM";
    }
    @Override
    public String getDatabaseProductName(){
        return "MySQL";
    }
}