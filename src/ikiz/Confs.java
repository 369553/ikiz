package ikiz;

import java.util.HashMap;

public class Confs{// Sistemin yapılandırmalarının belirtildiği yerdir
    private HashMap<String, Boolean> attributesPolicy;// Alan alma usûlü
    private boolean takeDateAttributeAsDateTime = true;// 'Date' tipindeki alanları veritabanına aktarırken 'DATETIME' tipinde aktar
    protected boolean alwaysContinue = true;//Eğer kullanıcın alınmasını istediği değer alınamadıysa, kalan değerlerle veritabanına yazmaya çalış
    //.;.
    

    public Confs(){
    }

// İŞLEM YÖNTEMLERİ:
    

// ERİŞİM YÖNTEMLERİ:
    
}