package base;

public class OtherArrayClass {
    int id;
    String[] metinler;
    String name;

    public OtherArrayClass(){
        this.id = 17;
        metinler = new String[]{"Bismillâh!", "Her hayrın başıdır"};
        this.name = "harita ismi";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String[] getMetinler() {
        return metinler;
    }

    public void setMetinler(String[] metinler) {
        this.metinler = metinler;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}