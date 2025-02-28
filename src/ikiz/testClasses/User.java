package ikiz.testClasses;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String name;
    public int id;
    public String[] lessons;
    public int[] notes;
    public GENDER gender;
    public enum GENDER{
        MALE,
        FEMALE
    }

// TEST İÇİN YÖNTEMLER:
    public static List<User> produceData(){
        User u = new User();
        u.id = 17;
        u.name = "Ahmed";
        u.gender = User.GENDER.MALE;

        User u2 = new User();
        u2.id = 19;
        u2.name = "Mehmed";
        u2.gender = User.GENDER.MALE;

        User u3 = new User();
        u3.id = 21;
        u3.name = "Sâdık";
        u3.gender = User.GENDER.MALE;
        
        User u4 = new User();
        u4.id = 934;
        u4.name = "Kâsım";
        u4.gender = User.GENDER.MALE;
        u4.notes = new int[]{12, 45};
        List<User> li = new ArrayList<User>();
        li.add(u);
        li.add(u2);
        li.add(u3);
        li.add(u4);
        return li;
    }
    @Override
    public String toString(){
        StringBuilder bui = new StringBuilder();
        bui.append("name : " + name + "\tid : " + id + "\tgender : " + gender + "\tnotes[0] : " + (notes == null ? "NULL" : notes[0]));
        return bui.toString();
    }
}