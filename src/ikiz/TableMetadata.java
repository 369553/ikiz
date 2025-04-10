package ikiz;

import java.lang.reflect.Field;
import java.util.Map;

public class TableMetadata{
    private Class targetClass;// Hedef Java sınıfı
    private TableConfiguration confs;// Tablo yapılandırma bilgileri (birincil anahtar, kısıtlar vs.)
    private Map<String, Field> mapOfTargetFields;// Hedef Java sınıfının tabloya kaydedilen alanları

    public TableMetadata(Class targetClass, TableConfiguration confs, Map<String, Field> mapOfTargetFields){
        this.targetClass = targetClass;
        this.confs = confs;
        this.mapOfTargetFields = mapOfTargetFields;
    }

//İŞLEM YÖNTEMLERİ:
//    protected void setTargetClass(Class targetClass){
//        this.targetClass = targetClass;
//    }
//    protected void setConfs(TableConfiguration confs){
//        this.confs = confs;
//    }
//    protected void setMapOfTargetFields(Map<String, Field> mapOfTargetFields){
//        this.mapOfTargetFields = mapOfTargetFields;
//    }
    @Override
    public String toString(){
        StringBuilder bui = new StringBuilder();
        bui.append("targetClass : ").append(targetClass.getName()).append("\n");
        bui.append("tableName : ").append(confs.getTableName()).append("\n");
        bui.append("uniqueFields : ").append(confs.getUniqueFields()).append("\n");
        bui.append("indexes : ").append(confs.getIndexes()).append("\n");
        bui.append("primaryKey : ").append(confs.getPrimaryKey()).append("\n");
        bui.append("notNulls : ").append(confs.getNotNulls()).append("\n");
        bui.append("lengthForStringAsDefault : ").append(confs.getDefaultLengthOfString()).append("\n");
        bui.append("mapOfTargetFields : ").append(mapOfTargetFields);
        return bui.toString();
    }

//ERİŞİM YÖNTEMLERİ:
    protected Class getTargetClass(){
        return targetClass;
    }
    protected TableConfiguration getConfs(){
        return confs;
    }
    protected Map<String, Field> getMapOfTargetFields(){
        return mapOfTargetFields;
    }
}