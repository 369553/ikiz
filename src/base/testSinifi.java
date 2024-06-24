package base;

public class testSinifi{
    public int numara;
    public String name;
    int age;
    protected boolean korunmusSir;

    public testSinifi(){
        numara = 5;
        name = "isim";
        age = 45;
        korunmusSir = true;
    }
    public testSinifi(String name){
        numara = 5;
        this.name = "boş";
        age = 45;
        korunmusSir = true;
    }
    public int getNumara(){
        return numara;
    }
    public void setNumara(int numara) {
        this.numara = numara;
    }
    private String getName() {
        return name;
    }
    public String getNamePublic(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public int getAge(){
        return age;
    }
    public void setAge(int age){
        this.age = age;
    }
    public boolean getKorunmusSir(){
        return korunmusSir;
    }
    public void setKorunmusSir(boolean korunmusSir){
        this.korunmusSir = korunmusSir;
    }
}