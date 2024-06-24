package ikiz;

public class UpdateMode{//Bu sınıf İkiz sistemi üzerinden yapılan veritabanı işlemlerinin hangisinde - 
                        //hangilerinde veritabanı yerel hâfızasının tazeleneceğinin belirtildiği
                        //kuralları ayarlamaya ve idâre etmeye yarar, bi iznillâh.
    boolean updateAllTable = false;//Yerel hâfızadaki tablo tazeleneceği zamân yerel hâfızadaki tüm tablo tazelensin mi, sorusunun cevâbıdır
    boolean updateOnRowAdded = true;//Tabloya yeni satır eklendiğinde yerel hâfızadaki tablo güncellensin mi, sorusunun cevâbıdır
    boolean updateOnRowUpdated = true;//Tablodaki bir satır tazelendiğinde yerel hâfızadaki tablo tazelensin mi, sorusunun cevâbıdır
    boolean updateOnRowdeleted = true;//Tablodaki bir satır silindiğinde yerel hâfızadaki tablo tazelensin mi, sorusunun cevâbıdır
    boolean updateAutomatically = false;//Yerel hâfızadaki tablo belli bir zamânda sürekli olarak tazelensin mi, sorusunun cevâbıdır
    long secondOfUpdatePeriod = 0;
    

    public UpdateMode(){
        
    }

//İŞLEM YÖNTEMLERİ:
    public void setUpdateAllTable(boolean updateAllTable){
        this.updateAllTable = updateAllTable;
    }
    public void setUpdateOnRowAdded(boolean updateOnRowAdded){
        this.updateOnRowAdded = updateOnRowAdded;
    }
    public void setUpdateOnRowUpdated(boolean updateOnRowUpdated){
        this.updateOnRowUpdated = updateOnRowUpdated;
    }
    public void setUpdateOnRowdeleted(boolean updateOnRowdeleted){
        this.updateOnRowdeleted = updateOnRowdeleted;
    }
    public void setUpdateAutomatically(boolean updateAutomatically){
        this.updateAutomatically = updateAutomatically;
        if(updateAutomatically)
            if(secondOfUpdatePeriod == 0)
                secondOfUpdatePeriod = 150;
    }
    public void setSecondOfUpdatePeriod(long secondOfUpdatePeriod){
        if(!updateAutomatically){
            System.out.println("Otomatik güncelleme açık değil");
            return;
        }
        if(secondOfUpdatePeriod < 20){
            System.out.println("Yerel tablonun tazelenmesi süresi bu kadar kısa olamaz");
            return;
        }
        this.secondOfUpdatePeriod = secondOfUpdatePeriod;
    }

//ERİŞİM YÖNTEMLERİ:
    public boolean getIsUpdateAllTable(){
        return updateAllTable;
    }
    public boolean getIsUpdateOnRowAdded(){
        return updateOnRowAdded;
    }
    public boolean getIsUpdateOnRowUpdated(){
        return updateOnRowUpdated;
    }
    public boolean getIsUpdateOnRowdeleted(){
        return updateOnRowdeleted;
    }
    public boolean getIsUpdateAutomatically(){
        return updateAutomatically;
    }
    public long getSecondOfUpdatePeriod(){
        return secondOfUpdatePeriod;
    }
}