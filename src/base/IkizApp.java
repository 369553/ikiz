package base;

import ikiz.IkizTest;

public class IkizApp{
    
    public static void main(String[] args){
        test();
    }

//İŞLEM YÖNTEMLERİ:
    static void test(){
        IkizTest t = IkizTest.startIkizTest(IkizTest.connectToDBForMySQL());
//        t.scenario2();
//        t.scenario7();// BAŞARILI
    }
}