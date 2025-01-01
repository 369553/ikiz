package ikiz;

public class HelperForHelperForDBType{
    private final static HelperForMySQL helperMySQL = new HelperForMySQL();
    private final static HelperForMsSQL helperMsSQL = new HelperForMsSQL();
    private final static HelperForPostgreSQL helperPostgreSQL = new HelperForPostgreSQL();

    private HelperForHelperForDBType(){}

// ERİŞİM YÖNTEMLERİ:
    public static HelperForDBType getHelper(Cvity.DBType dbType){
        if(dbType == Cvity.DBType.MYSQL)
            return helperMySQL;
        if(dbType == Cvity.DBType.MSSQL)
            return helperMsSQL;
        if(dbType == Cvity.DBType.POSTGRESQL)
            return helperPostgreSQL;
        return null;
    }
}