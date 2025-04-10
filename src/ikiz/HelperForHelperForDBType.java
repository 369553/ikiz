package ikiz;

public class HelperForHelperForDBType{
    private final static HelperForMySQL helperMySQL = new HelperForMySQL();
    private final static HelperForMsSQL helperMsSQL = new HelperForMsSQL();
    private final static HelperForPostgreSQL helperPostgreSQL = new HelperForPostgreSQL();
    private final static HelperForSQLite helperSQLite = new HelperForSQLite();
    

    private HelperForHelperForDBType(){}

// ERİŞİM YÖNTEMLERİ:
    public static HelperForDBType getHelper(Cvity.DBType dbType){
        if(dbType == Cvity.DBType.MYSQL)
            return helperMySQL;
        if(dbType == Cvity.DBType.MSSQL)
            return helperMsSQL;
        if(dbType == Cvity.DBType.POSTGRESQL)
            return helperPostgreSQL;
        if(dbType == Cvity.DBType.SQLITE)
            return helperSQLite;
        return null;
    }
    public static Cvity.DBType getDBTypeByProductName(String databaseProductName){
        Cvity.DBType type = null;
        if(databaseProductName != null){
            if(!databaseProductName.isEmpty()){
                if(databaseProductName.equals(helperMySQL.getDatabaseProductName()))
                    type = Cvity.DBType.MYSQL;
                else if(databaseProductName.equals(helperPostgreSQL.getDatabaseProductName()))
                    type = Cvity.DBType.POSTGRESQL;
                else if(databaseProductName.equals(helperMsSQL.getDatabaseProductName()))
                    type = Cvity.DBType.MSSQL;
                else if(databaseProductName.equals(helperSQLite.getDatabaseProductName()))
                    type = Cvity.DBType.SQLITE;
            }
        }
        return type;
    }
}