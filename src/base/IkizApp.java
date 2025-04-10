package base;

import ikiz.Cvity;
import ikiz.DBAccessHelper;
import ikiz.IkizTest;
import ikiz.Services.DTService;
import ikiz.Services.Helper;
import ikiz.TableConfiguration;
import ikiz.testClasses.User;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import jsoner.JSONObject;
import jsoner.JSONWriter;
public class IkizApp{
    
    public static void main(String[] args){
        test();
//        other();
//        dd();
    }

//İŞLEM YÖNTEMLERİ:
    static void test(){
        IkizTest t = IkizTest.startIkizTest(IkizTest.connectToDBForMySQL());
//        t.scenario2();// BAŞARILI : MySQL ve MsSQL
        t.scenario7();// BAŞARILI : 
//        t.scenario8();
    }
    static void other(){
        if(false){
            HashMap<String, Object> root = new HashMap<>();
        
            HashMap<String, Boolean> isConfSet = new HashMap<>();
            isConfSet.put("primaryKey", Boolean.TRUE);
            isConfSet.put("uniqueFields", Boolean.FALSE);
            ArrayList<String> notNulls = new ArrayList<String>();
            notNulls.add("serial");
            notNulls.add("fullName");
            root.put("isConfSet", isConfSet);
            root.put("primaryKey", "id");
            root.put("notNulls", notNulls);
            JSONWriter jWrt = new JSONWriter();
            String json = jWrt.produceText("tableConfiguration", root);
            System.out.println("json:\n" + json);
        }
        try{
//            Field fl = User.class.getDeclaredField("gender");
//            Object[] values = fl.getType().getEnumConstants();
//            System.out.println("Enum.class.getName() : " + Enum.class.getName());
//            for(Object val : values)
//                System.err.println("val : " + val.toString());

            // silme denemesi:
            Cvity cv = IkizTest.connectToDBForMySQL();
            DBAccessHelper acc = new DBAccessHelper(cv);
//            acc.getTableNames().forEach(System.out::println);
//            System.out.println("sill var mı : " + acc.checkIsTableInDB("sidll"));
//            String ord = "INSERT INTO za VALUES(?, ?)";
//            PreparedStatement preSt = cv.getConnext().prepareStatement(ord);
//            preSt.setObject(1, "bir");
//            java.sql.Date dt = new java.sql.Date(120, 11, 7);
//            System.out.println("dt : " + dt);
//            preSt.setObject(2, dt);
//            int affected = preSt.executeUpdate();
//            System.out.println("Etkilenen satır sayısı : " + affected);
            //
            
        }
        catch(Exception exc){
            System.err.println("exc...:"  +  exc.toString());
        }
    }
    static void dd(){
        try{
            String sqlOrder = "INSERT INTO tbl VALUES(?, ?, ?, ?);";
            Connection cn = IkizTest.connectToDBForMySQL().getConnext();
            PreparedStatement preSt = cn.prepareStatement(sqlOrder);
            preSt.setInt(1, 17);
            preSt.setObject(2, "Bismillâh");
            preSt.setObject(3, "Ar");
            preSt.setObject(4, "ekbilgi!.");
            int affected = preSt.executeUpdate();
            System.out.println("Şu kadar satır etkilendi : " + affected);
        }
        catch(SQLException exc){
            System.err.println("exc : " + exc.toString());
        }
    }
}