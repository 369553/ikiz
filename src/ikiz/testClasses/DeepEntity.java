package ikiz.testClasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeepEntity{
    String name;
    List<List<String>> listInList;
    Map<String, List<String>> listInMap;

    public DeepEntity(){
        
    }

// İŞLEM YÖNTEMLERİ:
    public static DeepEntity produceData(){
        DeepEntity d = new DeepEntity();
        d.name = "confs";
        d.listInList = new ArrayList<List<String>>();
        List<String> subList = new ArrayList<String>();
        subList.add("os");
        subList.add("runtime");
        subList.add("encoder");
        subList.add("scheduler");
        d.listInList.add(subList);
        
        List<String> sub2 = new ArrayList<String>();
        sub2.add("user.seeming");
        sub2.add("user.data");
        sub2.add("user.dir");
        sub2.add("user.identity");
        sub2.add("user.userName");
        d.listInList.add(sub2);
        
        Map<String, List<String>> m = new HashMap<String, List<String>>();
        m.put("confs", subList);
        m.put("user", sub2);
        List<String> algs = new ArrayList<String>();
        algs.add("roundRobin");
        algs.add("FIFO");
        algs.add("mixed");
        algs.add("ioFirst");
        m.put("scheduling", algs);
        
        return d;
    }
}