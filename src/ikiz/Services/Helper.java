package ikiz.Services;

import java.util.ArrayList;

public class Helper{

// SINIF FONKSİYONLARI:
    public static <T> boolean isInTheList(ArrayList<T> list, T element){// Listenin verilen elemanı barındırıp, barındırmadığını bildir
        for(T elm : list){
            if(elm.equals(element))
                return true;
        }
        return false;
    }
}