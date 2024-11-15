package ikiz.Services;

import java.util.Optional;

public class ArrayPrinterService{
   public static void printArray(Object[] array){
       if(array == null)
           return;
       for(Object obj : array){
           if(obj != null)
               System.out.println(String.valueOf(obj));
       }
       /*for(Object obj : array){
           Optional opt = Optional.of(obj);
           opt.ifPresent((element) -> {
               System.out.println(String.valueOf(element));});
       }*/
   }
}