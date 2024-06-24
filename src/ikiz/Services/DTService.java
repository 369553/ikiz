package ikiz.Services;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DTService{
    private static DTService serv;
    private boolean isLocaleHere = false;
    private ZoneId zoneID;
    private Calendar cln;
    private DateTimeFormatter frm;
    private TimeZone zone;
    
    public DTService(){
        
    }

//İŞLEM YÖNTEMLERİ:
    public Date getTime(){
        return getCalendar().getTime();
//        return Calendar.getInstance(TimeZone.getTimeZone("GMT+3:00")).getTime();
    }
    public String getLocaltDateTimeAsText(){
       return getZonedDateTime().format(getFormatter()).toString();
    }
//    public Date getLocalDateTime(){
//        return null;
//    }
    public Date getTime(String timeText){
        return null;
    }
    public ZonedDateTime getZonedDateTime(){
        return getInstanceFromCalendar().atZone(getZoneID());
    }
    public Instant getInstanceFromCalendar(){
        return getCalendar().toInstant().now();
    }

//ERİŞİM YÖNTEMLERİ:
    //ANA ERİŞİM YÖNTEMİ:
    public static DTService getService(){
        if(serv == null){
            serv = new DTService();
            Locale.setDefault(Locale.Category.FORMAT, Locale.forLanguageTag("tr"));
        }
        return serv;
    }
    public Calendar getCalendar(){
        if(cln == null){
            cln = Calendar.getInstance(getTimeZone(), Locale.ENGLISH);
        }
        return cln;
    }
    public TimeZone getTimeZone(){
        if(zone == null){
            zone = TimeZone.getTimeZone(getZoneID());
        }
        return zone;
    }
    public ZoneId getZoneID(){
        if(zoneID == null){
            zoneID = TimeZone.getTimeZone("GMT+3:00").toZoneId();
        }
        return zoneID;
    }
    public DateTimeFormatter getFormatter(){
        if(frm == null){
            frm = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT);
        }
        return frm;
    }
}