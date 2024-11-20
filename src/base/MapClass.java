package base;

import java.util.Map;
import java.util.HashMap;

public class MapClass {
    int id;
    Map<Integer, String> harita;
    String name;

    public MapClass() {
        this.id = 17;
        this.harita = new HashMap<Integer, String>();
        harita.put(1, "ilkDeğer");
        this.name = "harita ismi";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<Integer, String> getHarita() {
        return harita;
    }

    public void setHarita(Map<Integer, String> harita) {
        this.harita = harita;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}