package ikiz.Services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RWService{
    private static RWService rw;
    private RWService(){}

//İŞLEM YÖNTEMLERİ:
    public boolean canReadable(File file) {
        if(file == null)
            return false;
        FileInputStream  fIStream = null;
        try{
            fIStream = new FileInputStream(file);
        }
        catch(IOException exc){
            //Hatâyı nereye yansıtmalı?
            return false;
        }
        if(fIStream != null)
            return true;
        return false;
    }
    public boolean canReadableForData(File file){
        return canReadable(file);
    }
    public String getExtension(File file){
        if(!file.isFile())
            return null;
        String[] splitted = file.getPath().split("\\.");
        if(splitted == null)
            return null;
        if(splitted.length < 2)
            return null;
        return splitted[splitted.length - 1].toLowerCase();
    }
    public String readDataAsText(File file){
        if(file == null)
            return null;
        if(!file.isFile())
            return null;
        try{
            FileReader fRead = new FileReader(file);
            BufferedReader buf = new BufferedReader(fRead);
            StringBuilder content = new StringBuilder();
            String line;
            int sayac = 0;
            while((line = buf.readLine()) != null){
                if(sayac != 0)
                    content.append("\n");
                content.append(line);
                sayac++;
            }
            buf.close();
            fRead.close();
            return content.toString();
        }
        catch(IOException exc){
            System.out.println("Hatâ (RWService . readDataAsText) : " + exc.getLocalizedMessage());
        }
        return null;
    }
    public String readDataAsText(String path, String fileName){
        File f = new File(path + "\\" + fileName);
        return readDataAsText(f);
    }
    public String readDataAsText(String filePath){
        if(filePath == null)
            return null;
        if(filePath.isEmpty())
            return null;
        File f = new File(filePath);
        return readDataAsText(f);
    }
    public File produceFile(String fileName, String path){
        File saveLoc = new File(path);
        if(!saveLoc.canWrite())
            return null;
        if(!saveLoc.isDirectory())
            return null;
        File ff = new File(saveLoc.getPath(), fileName);
        try{
            ff.createNewFile();
        }
        catch (IOException ex){
            System.err.println(path + " dizininde " + fileName + " isminde dosya oluşturulamadı!");
            return null;
        }
        return ff;
    }
    public boolean produceAndWriteFile(String content, String fileName, String path){
        return writeFile(produceFile(fileName, path), content);
    }
    public File produceTempFile(String fileNameExtension){
        if(fileNameExtension != null)
            fileNameExtension = "." + fileNameExtension;
        else
            fileNameExtension = "";
        try{
            File work = java.io.File.createTempFile("temp", fileNameExtension);
            return work;
        }
        catch(IOException exc){
            //Hatâyı ele al
            System.out.println("Bir hatâ ile karşılaşıldı (RWService.produceFile) : " + exc.getLocalizedMessage());
            return null;
        }
    }
    public File produceAndWriteTempFile(String fileNameWithExtension, String content){
        File tmp = produceTempFile(fileNameWithExtension);
        boolean isSuccess = writeFile(tmp, content);
        if(!isSuccess){
            //Hatâyı ele al
            return null;
        }
        return tmp;
    }
    public boolean writeFile(File file, String content){
        long space = file.length();
        FileWriter wrt;
        BufferedWriter bufwrt;
        try{
            wrt = new FileWriter(file);//DİSK SEÇİLDİĞİNDE HATÂ VERİYOR
            bufwrt = new BufferedWriter(wrt);
            bufwrt.write(content);
            bufwrt.flush();
            bufwrt.close();
            if(file.length() > space){// Dosya boyutu artmış; yanî veri yazılmış olmalı
                return true;
            }
            else{
                //Hatâyı ele al
                return false;
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
            //Hatâyı ele al
            System.err.println("Veriler dosyaya yazılamadı");
            return false;
        }
    }
//    public boolean writeFile(String path, String fileName, String content){
//        
//    }
//    public boolean appendFile(File f, String content){
//        
//    }
//    public boolean appendFile(String path, String fileName, String content){
//        
//    }
    public String[] getFileList(String path){
        File file = new File(path);
        if(!file.isDirectory())
            return null;
        return file.list();
    }
    public boolean checkFilePositionForRW(String path){
        File file = new File(path);
        if(file == null)
            return false;
        if(!file.isDirectory())
            return false;
        if(file.canRead() && file.canWrite())
            return true;
        else
            return false;
    }
    public boolean checkFileIsExist(String path, String fileName){// Verilen dizinde verilen dosya isminde bir dosya olup, olmadığına bak
        File file = new File(path);
        for(String str : file.list()){
            if(str.equals(fileName))
                return true;
        }
        return false;
    }
    public boolean deleteFile(String path, String fileName){
        File file = new File(path + "\\" + fileName);
        if(!file.canWrite())
            return false;
        if(file.isDirectory())
            return false;
        return file.delete();
    }

//ERİŞİM YÖNTEMLERİ:
    //ANA ERİŞİM YÖNTEMİ:
    public static RWService getService(){
        if(rw == null){
            rw = new RWService();
        }
        return rw;
    }
}