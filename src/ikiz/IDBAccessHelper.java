package ikiz;

import java.util.Map;
import java.util.List;

public interface IDBAccessHelper{
//SOYUT İŞLEM YÖNTEMLERİ:
    public List<String> getTableNames();
    public List<Map<String, Object>> getData(String tableName);
    public List<Map<String, Object>> getData(String tableName, List<String> fieldNames);
    public List<Map<String, Object>> getDataForOneWhereCondition(String tableName, List<String> fieldNames, String whereCondition, Object answerOfWhereCondition);
    public List<Map<String, Object>> getData(String tableName, List<String> fieldNames, String[] whereConditions, Object[] answerOfWhereConditions);
    public List<String> getFieldNames(String tableName);
    public boolean deleteRow(String tableName, String whereCondition, Object answerOfWhereCondition);
    public boolean deleteRow(String tableName, String[] whereConditions, Object[] answerOfWhereConditions);
    public boolean updateRow(String tableName, String whereCondition, Object answerOfWhereCondition, Map<String, Object> fieldsToValues);
    public boolean updateRow(String tableName, String[] whereConditions, Object[] answerOfWhereConditions, Map<String, Object> fieldsToValues);
    
}