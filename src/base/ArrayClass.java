package base;


public class ArrayClass {
    int id;
    int[] sayilar;
    String name;

    public ArrayClass() {
        this.id = 17;
        sayilar = new int[]{11, 71, 17};
        this.name = "harita ismi";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int[] getSayilar() {
        return sayilar;
    }

    public void setSayilar(int[] sayilar) {
        this.sayilar = sayilar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
