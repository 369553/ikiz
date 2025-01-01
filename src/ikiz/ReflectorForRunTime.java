package ikiz;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectorForRunTime{
    private static ReflectorForRunTime serv;// service
    private HashMap<Class, Class> mapOfPrimitiveToWrapper;

    private ReflectorForRunTime(){}

// İŞLEM YÖNTEMLERİ:
    public <T> T[] produceNewArray(Class<T[]> classOfDataArray, int length){// Verilen dizi tipinde, verilen uzunlukta yeni bir değişken oluştur
        return classOfDataArray.cast(Array.newInstance(classOfDataArray.getComponentType(), length));
    }
    public <T> T produceNewArrayReturnAsObject(Class<T> classOfDataArray, int length){// Yukarıdaki fonksiyonun aynısı; fakat temel veri tiplerinin dizisi için de çalışır, bi iznillâh..
        return classOfDataArray.cast(Array.newInstance(classOfDataArray.getComponentType(), length));
    }
    public <T> T produceNewArrayInjectDataReturnAsObject(Class<T> classOfDataArray, int length, List data){
        Object value = Array.newInstance(classOfDataArray.getComponentType(), length);
        for(int sayac = 0; sayac < data.size(); sayac++){
            Array.set(value, sayac, data.get(sayac));
        }
        return classOfDataArray.cast(value);
    }
    public <T> T produceNewInstance(Class<T> cls){// Arayüz oluşturulurken hatâ vermemesi için kod eklenecek ; generic oluşturmak için kod eklenecek bi iznillâh
        // Temel veri tipleri için tespit, sargılayıcı sınıftan yeni nesne üretme ve temel veri tipine dönüştürme uygulanıyor:
        T obj = null;
        boolean unwrapOnFinal = false;
        boolean isPri = false;
        Class target;
        if(cls.isPrimitive()){// Temel veri tipiyse, işâretlemeleri yap
            isPri = true;
            unwrapOnFinal = true;
        }
        if(cls.isArray()){// Dizi olup, olmadığı sorgulanmalı
            //.;.
        }
        try{
            if(cls.equals(List.class))
                target = ArrayList.class;
            else if(cls.equals(Map.class))
                target = HashMap.class;
            else if(isPri)
                target = getWrapperClassFromPrimitiveClass(cls);
            else
                target = cls;
            // Yeni sınıf örneği (nesne) oluşturmak için yapıcı yöntemlere bak:
            Constructor noParamCs = findConstructorForNoParameter(target);// İlk olarak parametresiz yapıcı yöntem ara:
            if(target.equals(Number.class))// Bu bir soyut sınıf olduğundan dolayı bunun kendi tipinde bir örneği oluşturulamaz; bu sebeple bunun için özel kod yazıyoruz
                obj = (T) ((Integer) 0);
            else if(noParamCs != null){
                obj = (T) noParamCs.newInstance(null);
            }
            else{// Parametresiz yapıcı yöntem yoksa; temel veri tipinin sarmalanmış hâli ise oluşturmaya çalış
                String parameterOfConstructor = getParameterForConstructorOfWrapperBasicClass(target);
                if(parameterOfConstructor != null){
                    obj = (T) target.getConstructor(String.class).newInstance(parameterOfConstructor);
                }
                else if(target.equals(Character.class)){
                    obj = (T) target.getConstructor(char.class).newInstance(' ');
                }
                else// İlgili sınıfın yeni bir örneği oluşturulamadı! : Burası belki geliştirilebilir...
                    return null;
            }
            if(obj != null)
                System.out.println("Reflector.. içerisinde Başarıyla oluşturuldu, bi iznillâh");
        }
        catch(InstantiationException exc){
            System.err.println("Hatâ - InstantiationException (produceNewInstance) : " + exc.toString());
        }
        catch(IllegalAccessException exc){
            System.err.println("Hatâ - IllegalAccessException (produceNewInstance) : " + exc.toString());
        }
        catch(InvocationTargetException exc){
            System.err.println("Hatâ - InvocationTargetException (produceNewInstance) : " + exc.toString());
        }
        catch(NoSuchMethodException exc){
            System.err.println("Hatâ - NoSuchMethodException (produceNewInstance) : " + exc.toString());
        }
        catch(IllegalArgumentException exc){
            System.err.println("Hatâ - IllegalArgumentException (produceNewInstance) : " + exc.toString());
        }
        return obj;
    }
    public Class getPrimitiveClassFromWrapper(Class wrapperClass){
        Class value = null;
        for(Class cls : getMapOfPrimitiveToWrapper().keySet()){
            if(getMapOfPrimitiveToWrapper().get(cls).equals(wrapperClass)){
                value = cls;
                break;
            }
        }
        return value;
    }
    public Class getWrapperClassFromPrimitiveClass(Class primitiveClass){
        return getMapOfPrimitiveToWrapper().get(primitiveClass);
    }
    public Constructor findConstructorForNoParameter(Class cls){
        for(Constructor cs : cls.getConstructors()){
            if(cs.getParameterCount() == 0)
                return cs;
        }
        return null;
    }
    /*İNŞASI YARIM BIRAKILDI:
        public Object getCastedObjectFromString(String data){// Nesneyi temel veri tiplerine dönüştürmeye çalış; içerisinde sayı barındıran String değerler sayı olarak döndürülür
        if(data == null)
            return null;
        if(data.isEmpty())
            return data;
        try{
            Double.valueOf(data);
        }
        catch(ClassCastException exc){
            
        }
    }*/
    public <T> T getCastedObjectFromString(String data, Class<T> target){// Verilen metîndeki veriyi verilen tipte bir nesneye dönü
        if(data == null || target == null)
            return null;
        if(data.isEmpty())
            return null;
        if(target == String.class)
            return ((T) new String(data));
        try{
        Object casted = null;
            if(target == Integer.class || target == int.class)// Tamsayı ise;
                casted = Integer.valueOf(data);
            else if(target == Double.class || target == double.class)
                casted = Double.valueOf(data);
            else if(target == Float.class || target == float.class)
                casted = Float.valueOf(data);///e
            else if(target == Byte.class || target == byte.class)
                casted = Byte.valueOf(data);
            else if(target == Long.class || target == long.class)
                casted = Long.valueOf(data);
            else if(target == Short.class || target == short.class)
                casted = Short.valueOf(data);
            else if(target == Boolean.class || target == boolean.class)
                casted = Boolean.valueOf(data);
            else if(target == Character.class || target == char.class)
                casted = data.charAt(0);// İlk karakter alınıyor
            return (T) casted;
        }
        catch(ClassCastException exc){
            System.err.println("İstenen veri tipine dönüştürülemedi : " + exc.toString());
            return null;
        }
    }
    // ARKAPLAN İŞLEM YÖNTEMLERİ:
    private String getParameterForConstructorOfWrapperBasicClass(Class cls){
        String param = null;
        if(cls.equals(Integer.class))
            param = "0";
        else if(cls.equals(Double.class))
            param = "0.0";
        else if(cls.equals(Long.class))
            param = "0";
        else if(cls.equals(Short.class))
            param = "0";
        else if(cls.equals(Number.class))
            param = "0";
        else if(cls.equals(Boolean.class))
            param = "true";
        else if(cls.equals(Byte.class))
            param = "0";
        return param;
    }

// ERİŞİM YÖNTEMLERİ:
    //ANA ERİŞİM YÖNTEMİ
    public static ReflectorForRunTime getService(){
        if(serv == null)
            serv = new ReflectorForRunTime();
        return serv;
    }
    private HashMap<Class, Class> getMapOfPrimitiveToWrapper(){
        if(mapOfPrimitiveToWrapper == null){
            mapOfPrimitiveToWrapper = new HashMap<Class, Class>();
            mapOfPrimitiveToWrapper.put(int.class, Integer.class);
            mapOfPrimitiveToWrapper.put(double.class, Double.class);
            mapOfPrimitiveToWrapper.put(boolean.class, Boolean.class);
            mapOfPrimitiveToWrapper.put(short.class, Short.class);
            mapOfPrimitiveToWrapper.put(long.class, Long.class);
            mapOfPrimitiveToWrapper.put(char.class, Character.class);
            mapOfPrimitiveToWrapper.put(byte.class, Byte.class);
        }
        return mapOfPrimitiveToWrapper;
    }
}