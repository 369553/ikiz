package ikiz.testClasses;

import java.util.List;
import java.util.Map;
import ikiz.testClasses.User.GENDER;
import java.util.ArrayList;
import java.util.HashMap;

public class Customer{
    int id;
    String name;
    List<String> locations;
    String phoneNumber;// 10 uzunluklu olmalı
    Map<String, Boolean> attendedCampaigns;// Katıldığı kampanyalar
    GENDER gender;

    public Customer(){
    }

//İŞLEM YÖNTEMLERİ:
    public static Customer getNewCustomer(){
        Customer c = new Customer();
        c.id = 11;
        c.name = "Mert";
        c.gender = GENDER.MALE;
        List<String> locs = new ArrayList<String>();
        locs.add("Isparta");
        locs.add("Artvin");
        locs.add("İstanbul");
        c.locations = locs;
        c.phoneNumber = "5515551122";
        Map<String, Boolean> attendeds = new HashMap<String, Boolean>();
        attendeds.put("timer", Boolean.TRUE);
        attendeds.put("enter", Boolean.FALSE);
        attendeds.put("claimer", Boolean.TRUE);
        attendeds.put("juster", Boolean.FALSE);
        c.attendedCampaigns = attendeds;
        return c;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Map<String, Boolean> getAttendedCampaigns() {
        return attendedCampaigns;
    }

    public void setAttendedCampaigns(Map<String, Boolean> attendedCampaigns) {
        this.attendedCampaigns = attendedCampaigns;
    }

    public GENDER getGender() {
        return gender;
    }

    public void setGender(GENDER gender) {
        this.gender = gender;
    }
    
}