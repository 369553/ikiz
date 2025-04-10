package ikiz;

import java.util.HashMap;

public class HelperForMsSQL implements HelperForDBType{
    private HashMap<String, String> dataTypes = getMapOfDataTypeToDBDataType();

//İŞLEM YÖNTEMLERİ:
    @Override
    public String getSentenceForShowTables(){
        return "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES;";
    }
    @Override
    public String getConnectionString(String hostName, int portNumber){
        return "jdbc:sqlserver://" + hostName + ":" + portNumber + ";trustServerCertificate=true;";
    }
    @Override
    public String getConnectionString(String hostName, int portNumber, String dbName){
        return "jdbc:sqlserver://" + hostName + ":" + portNumber + ";trustServerCertificate=true;" + "databaseName=" + dbName + ";";
    }
    @Override
    public Character getStartSymbolOfName(){
        return '[';
    }
    @Override
    public Character getEndSymbolOfName(){
        return ']';
    }
    @Override
    public HashMap<String, String> getMapOfDataTypeToDBDataType(){
        HashMap<String, String> values = new HashMap<String, String>();
        values.put("int", "INT");
        values.put("java.lang.Integer", "INT");
        values.put("java.lang.String", "VARCHAR(500)");
        values.put("double", "FLOAT");
        values.put("java.lang.Double", "FLOAT");
        values.put("float", "FLOAT(53)");
        values.put("java.lang.Float", "FLOAT(53)");
        values.put("boolean", "BIT");
        values.put("java.lang.Boolean", "BIT");
        values.put("char", "NCHAR(1)");
        values.put("java.lang.Character", "NCHAR(1)");
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
        values.put("java.sql.Date", "DATETIME");// Târih - zamân
        values.put("java.lang.Number", "DEC(19,9)");
        values.put("java.io.File", "MEDIUMBLOB");
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
        return "NVARCHAR(MAX)";
    }
    @Override
    public String getDataTypeNameForEnum(){
        return "NVARCHAR(50)";
    }
    @Override
    public String getDatabaseProductName(){
        return "Microsoft SQL Server";
    }
}