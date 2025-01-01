package ikiz;

public class MatrixFunctions{
    private MatrixFunctions(){}

// İŞLEM YÖNTEMLERİ:
    public static boolean[] produceBooleanArray(int length, boolean value){
        if(length == 0)
            return null;
        boolean[] arr = new boolean[length];
        for(int sayac = 0; sayac < length; sayac++){
            arr[sayac] = value;
        }
        return arr;
    }
}