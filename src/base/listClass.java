package base;

import java.util.ArrayList;
import java.util.List;

public class listClass {
    public int id;
    public List<String> liste;
    public String name;

    public listClass() {
        this.id = 17;
        this.liste = new ArrayList<String>();
        liste.add("Bismillâh");
        this.name = "isim yok";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getListe() {
        return liste;
    }

    public void setListe(List<String> liste) {
        this.liste = liste;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}