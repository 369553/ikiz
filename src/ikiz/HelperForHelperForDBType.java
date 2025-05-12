package ikiz;

public class HelperForHelperForDBType{
    private final static HelperForMySQL helperMySQL = new HelperForMySQL();
    private final static HelperForMsSQL helperMsSQL = new HelperForMsSQL();
//    private final static HelperForPostgreSQL helperPostgreSQL = new HelperForPostgreSQL();
//    private final static HelperForSQLite helperSQLite = new HelperForSQLite();
    

    private HelperForHelperForDBType(){}

// ERİŞİM YÖNTEMLERİ:
    /**
     * Verilen veri tipine göre veritabanı yardımcı sınıfını döndürür
     * @param dbType Veritabanı tipini ifâde eden {@code enum} değeri
     * @return {@code HelperForDBType} tipinde veritabanı yardımcı nesnesi
     */
    public static HelperForDBType getHelper(Cvity.DBType dbType){
        if(dbType == Cvity.DBType.MYSQL)
            return helperMySQL;
        if(dbType == Cvity.DBType.MSSQL)
            return helperMsSQL;
//        if(dbType == Cvity.DBType.POSTGRESQL)
//            return helperPostgreSQL;
//        if(dbType == Cvity.DBType.SQLITE)
//            return helperSQLite;
        return null;
    }
    /**
     * Veritabanı 'connector' nesnesi tarafından verilen vt ürün ismine bakarak
     * veritabanının tipini döndürür
     * @param databaseProductName Veritabanı ürün ismi
     * @return Veritabanı tipini ifâde eden {@code enum} değeri veyâ {@code null}
     */
    public static Cvity.DBType getDBTypeByProductName(String databaseProductName){
        Cvity.DBType type = null;
        if(databaseProductName != null){
            if(!databaseProductName.isEmpty()){
                if(databaseProductName.equals(helperMySQL.getDatabaseProductName()))
                    type = Cvity.DBType.MYSQL;
//                else if(databaseProductName.equals(helperPostgreSQL.getDatabaseProductName()))
//                    type = Cvity.DBType.POSTGRESQL;
                else if(databaseProductName.equals(helperMsSQL.getDatabaseProductName()))
                    type = Cvity.DBType.MSSQL;
//                else if(databaseProductName.equals(helperSQLite.getDatabaseProductName()))
//                    type = Cvity.DBType.SQLITE;
            }
        }
        return type;
    }
}