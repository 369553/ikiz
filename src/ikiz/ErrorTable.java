package ikiz;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ErrorTable{
    private int writer = 1;//Yazma imleci
    private int reader = 1;//Okuma imleci
    private HashMap<Integer, HashMap<String, String>> mapErrors;//Yapısı : <hatâ sıra no, hata bilgi haritası>
    private boolean deleteErrorIfItReaded = false;//Hatâyı gösterdikten sonra otomatik olarak sil
    private boolean showAutomatically = false;//Hatâyı otomatik olarak göster, biti
    private Method showFunction;//Hatâyı göstermek için çalıştırılması gereken fonksiyon
    private Object variableForShowFunction;//Hatâyı göstermek için çalıştırılacak gereken fonksiyonun hangi değişken üzerinden çalıştırılacağı
    private Class showFunctionClassForIsStatic;//Hatâyı göstermek için çalıştırılacak (bi iznillâh) fonksiyonun statik olduğu durum için fonksiyonun olduğu sınıfı tutan değişken
    private boolean isShowFunctionIsStatic;//Hatâyı göstermek için çalıştırılması gereken fonksiyon statik mi, biti
    private HashMap<Integer, String> mapInternalErrors;//İç hatâları barındıran harita. Yapısı : <hatâ sıra no, hatâ yazısı>
    private int writerInternal = 1;//İç hatâlar için yazma imleci

    public ErrorTable(){
        
    }
    public ErrorTable(boolean deleteErrorIfItReaded){
        this();
        this.deleteErrorIfItReaded = deleteErrorIfItReaded;
    }
    public ErrorTable(Object variableForShowFunction, Method showFunction){
        this();
        if(variableForShowFunction != null && showFunction != null){
            this.variableForShowFunction = variableForShowFunction;
            this.showFunction = showFunction;
            this.showAutomatically = true;
        }
    }
    public ErrorTable(Class showFunctionClassForStaticMethod, Method showFunctionAsStatic){
        this();
        if(showFunctionClassForStaticMethod != null && showFunctionAsStatic != null){
            this.showFunctionClassForIsStatic = showFunctionClassForStaticMethod;
            this.showFunction = showFunctionAsStatic;
            this.showAutomatically = true;
        }
    }

//İŞLEM YÖNTEMLERİ:
    public HashMap<String, String> readErrorWithInfo(){
        HashMap<String, String> value = getMapErrors().get(reader);
        if(deleteErrorIfItReaded)
            getMapErrors().remove(reader);
        reader--;
        return value;
    }
    public HashMap<String, String> readPastErrorWithInfo(int numberFromLast){
        if(reader - numberFromLast <= 0)
            return null;
        HashMap<String, String> value = getMapErrors().get(reader - numberFromLast);
        if(deleteErrorIfItReaded)
            getMapErrors().remove(reader - numberFromLast);
        return value;
    }
    public String readError(){
        return readErrorWithInfo().get("text");
    }
    public String readPastError(int numberFromLast){
        return readPastErrorWithInfo(numberFromLast).get("text");
    }
    public void writeError(String functionName, String errorText){
        if(functionName == null || errorText == null)
            return;
        if(functionName.isEmpty() && errorText.isEmpty())
            return;
        HashMap<String, String> errorInfo = new HashMap<String, String>();
        errorInfo.put("text", errorText);
        errorInfo.put("source", functionName);
        //İhtiyârî : errorInfo.put("time", DTService.getService().getTime());
        getMapErrors().put(writer, errorInfo);
        reader = writer;
        writer++;
        if(showAutomatically)
            showError(errorText);
    }
    public void setDeleteErrorIfItReaded(boolean deleteErrorIfItReaded){
        this.deleteErrorIfItReaded = deleteErrorIfItReaded;
    }
    public void setShowFunction(Method showFunction){
        if(showAutomatically)
            this.showFunction = showFunction;
    }
    public void setVariableForShowFunction(Object variableForShowFunction){
        if(showAutomatically)
            this.variableForShowFunction = variableForShowFunction;
    }
    public void setShowAutomatically(boolean showAutomatically){
        this.showAutomatically = showAutomatically;
    }
    public void setShowFunctionClassForIsStatic(Class showFunctionClassForIsStatic){
        if(showAutomatically)
            this.showFunctionClassForIsStatic = showFunctionClassForIsStatic;
    }
    //GİZLİ İŞLEM YÖNTEMLERİ:
    private void writeInternalError(String text){
        getMapInternalErrors().put(writerInternal, text);
        writerInternal++;
    }
    private void showError(String errorText){
        if(showFunction == null){
            System.err.println("Hatâ gösterimi için yöntem atanmamış.\n");
            System.out.println("Gösterilmek istenen hatâ : " + errorText);
            return;
        }
        if(isShowFunctionIsStatic){
            try{
                showFunctionClassForIsStatic.getMethod(showFunction.getName(), showFunction.getParameterTypes()).invoke(showFunctionClassForIsStatic, errorText);
            }
            catch(NoSuchMethodException e){
                writeInternalError("Hatânın gösterileceği sınıfın ilgili statik yöntemi bulunamadı");
                return;
            }
            catch(SecurityException e){
                writeInternalError("Hatânın gösterileceği sınıfın ilgili statik yöntemine erişilirken güvenlik hatâsı alındı");
                return;
            }
            catch(IllegalAccessException e){
                writeInternalError("Hatânın gösterileceği sınıfın ilgili statik yöntemine erişilirken geçersiz erişim hatâsı alındı");
                return;
            }
            catch(IllegalArgumentException e){
                writeInternalError("Hatânın gösterileceği sınıfın ilgili statik yöntemine erişilirken geçersiz fonksiyon girdisi hatâsı alındı");
                return;
            }
            catch(InvocationTargetException e){
                writeInternalError("Hatânın gösterileceği sınıfın ilgili statik yöntemine erişilirken yöntem çalıştırma hatâsı alındı");
                return;
            }
        }
        else{
            try{
                variableForShowFunction.getClass().getMethod(showFunction.getName(), showFunction.getParameterTypes())
                        .invoke(variableForShowFunction, errorText);
            }
            catch(NoSuchMethodException e){
                writeInternalError("Hatânın gösterileceği sınıfın ilgili yöntemi bulunamadı");
            }
            catch(SecurityException e){
                writeInternalError("Hatânın gösterileceği sınıfın ilgili yöntemine erişilirken güvenlik hatâsı alındı");
            }
            catch(IllegalAccessException e){
                writeInternalError("Hatânın gösterileceği sınıfın ilgili yöntemine erişilirken geçersiz erişim hatâsı alındı");
            }
            catch(IllegalArgumentException e){
                writeInternalError("Hatânın gösterileceği sınıfın ilgili yöntemine erişilirken geçersiz fonksiyon girdisi hatâsı alındı");
            }
            catch(InvocationTargetException e){
                writeInternalError("Hatânın gösterileceği sınıfın ilgili yöntemine erişilirken yöntem çalıştırma hatâsı alındı");
            }
        }
    }

//ERİŞİM YÖNTEMLERİ:
    public int getWriter(){
        return writer;
    }
    public int getReader(){
        return reader;
    }
    public HashMap<Integer, HashMap<String, String>> getMapErrors(){
        if(mapErrors == null)
            mapErrors = new HashMap<Integer, HashMap<String, String>>();
        return mapErrors;
    }
    public boolean isDeleteErrorIfItReaded(){
        return deleteErrorIfItReaded;
    }
    public boolean isShowAutomatically(){
        return showAutomatically;
    }
    public Method getShowFunction(){
        return showFunction;
    }
    public Object getVariableForShowFunction(){
        return variableForShowFunction;
    }
    //GİZLİ ERİŞİM YÖNTEMLERİ:
    private HashMap<Integer, String> getMapInternalErrors(){
        if(mapInternalErrors == null)
            mapInternalErrors = new HashMap<Integer, String>();
        return mapInternalErrors;
    }
}