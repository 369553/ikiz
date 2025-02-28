package ikiz;

import java.lang.reflect.Field;
import java.util.Map;

public class TableMetadata{
    private Class targetClass;// Hedef Java sınıfı
    private TableConfiguration confs;// Tablo yapılandırma bilgileri (birincil anahtar, kısıtlar vs.)
    private Map<String, Field> mapOfTargetFields;// Hedef Java sınıfının tabloya kaydedilen alanları

    protected TableMetadata(Class targetClass, TableConfiguration confs, Map<String, Field> mapOfTargetFields){
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