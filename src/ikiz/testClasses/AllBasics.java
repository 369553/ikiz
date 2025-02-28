package ikiz.testClasses;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class AllBasics{// Tüm temel veri tiplerini barındıran bir sınıf
    // Kısaltmalar:
    /*
    dec : decimal
    obj : object
    pri : primitive
    lc : local
    bool : boolean
    */
    // Sayı için veri tipleri:
    public int decAsPri;
    public Integer decAsObj;
    
    public float floatAsPri;
    public Float floatAsObj;
    
    public double doubleAsPri;
    public Double doubleAsObj;
    
    public short shortAsPri;
    public Short shortAsObj;
    
    public long longAsPri;
    public Long longAsObj;
    
    public byte byteAsPri;
    public Byte byteAsObj;
    
    public Number numAsObj;
    
    // Metîn için veri tipleri:
    public String str;
    
    public char characterAsPri;
    public Character characterAsObj;
    
    // Zamân târih:
    public Date dateFromUtil;
    public java.sql.Date dateFromSql;
    
    public LocalDate lcDate;
    
    public LocalDateTime lcDatetime;
    
    public LocalTime lcTime;
    
    // Diğer:
    private File file;
    
    public boolean boolAsPri;
    public Boolean boolAsObj;

    public AllBasics() {
        assignValues();
    }

// İŞLEM YÖNTEMLERİ:
    private void assignValues(){
        decAsPri = 378;
        decAsObj = 379;
        
        floatAsPri = (float) 515.10;
        floatAsObj = (float) 515.11;
        
        doubleAsPri = 12432.5643;
        doubleAsObj = 124.34;
        
        shortAsPri = 111;
        shortAsObj = 122;
        
        longAsPri = 123243343L;
        longAsObj = 171717171L;
        
        byteAsPri = (byte) 5;
        byteAsObj = new Byte("9");
        
        numAsObj = 343.33;
        
        // Metînsel:
        str = "Bismillâh";
        
        characterAsPri = 'c';
        characterAsObj = 'C';
        
        //Zamân - târih:
        dateFromUtil = Calendar.getInstance(TimeZone.getTimeZone("Turkey")).getTime();
        dateFromSql = java.sql.Date.valueOf(LocalDate.of(2024, 07, 22));
        
        lcDate = LocalDate.of(2024, 01, 17);
        
        lcDatetime = LocalDateTime.parse("2024-01-17T11:17:19");
        lcTime = LocalTime.of(11, 17, 19);
        
        // Diğer:
        file = new File("C:\\ProgramData\\MySQL\\MySQL Server 8.0\\resim.png");
        
        boolAsPri = true;
        boolAsObj = Boolean.FALSE;
    }
}