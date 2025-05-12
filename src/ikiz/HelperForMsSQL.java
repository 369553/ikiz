package ikiz;

import java.util.ArrayList;
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
        values.put("java.time.LocalDate", "DATE");// Târih
        values.put("java.time.LocalDateTime", "DATETIME");// Târih - zamân
        values.put("java.time.LocalTime", "TIME");// >amân
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
    @Override
    public boolean isDefaultStringDataType(String typeName){
        if(typeName == null)
            return false;
        return typeName.equalsIgnoreCase(this.dataTypes.get("java.lang.String").split("\\(")[0]);
    }
    @Override
    public Class<?> getMatchedClassForGivenSqlType(String sqlTypeName){
        if(sqlTypeName == null)
            return null;
        if(sqlTypeName.isEmpty())
            return null;
        ArrayList<String> founds = new ArrayList<String>();
        for(String atJava : dataTypes.keySet()){
            if(dataTypes.get(atJava).split("\\(")[0].equalsIgnoreCase(sqlTypeName))
                founds.add(atJava);
        }
        if(founds.isEmpty())
            return null;
        String found = founds.get(0);
        if(founds.size() > 1){
            for(String type : founds){
                if(type.startsWith("java.lang."))
                    found = type;
                else if(type.startsWith("java.time."))
                    found = type;
            }
        }
        Class<?> asClass = null;
        try{
            asClass = Class.forName(found);
        }
        catch(ClassNotFoundException exc){
            System.err.println("exc : " + exc.toString());
        }
        return asClass;
    }
    @Override
    public String getAutoIncrementKeyword(){
        return "IDENTITY(1,1)";
    }
    @Override
    public String getQueryForLastInsertedID(){
        return "SELECT SCOPE_IDENTITY();";//SELECT SCOPE_IDENTITY() AS lastIdInScope
    }
}