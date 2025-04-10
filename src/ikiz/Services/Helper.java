package ikiz.Services;

import java.util.ArrayList;

/**
 * Yardımcı fonksiyonlardan oluşan küçük bir sınıftır
 * @author Mehmet Akif SOLAK
 */
public class Helper{
// SINIF FONKSİYONLARI:
    /**
     * Listenin verilen elemanı barındırıp, barındırmadığını bildir
     * @param <T> Listedeki elemanların tipi
     * @param list Liste
     * @param element Aranan eleman 
     * @return Eleman listede varsa {@code true}, aksi hâlde {@code false}
     */
    public static <T> boolean isInTheList(ArrayList<T> list, T element){
        if(list == null)
            return false;
        for(T elm : list){
            if(elm.equals(element))
                return true;
        }
        return false;
    }
}