package ru.classes;

import java.io.*;
import java.io.File;
import java.lang.Integer;
import java.lang.String;
import java.lang.Throwable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ru.objects.ImageInfo;

import javax.xml.bind.DatatypeConverter;
import java.util.Random;


/**
 * Created by Aferon on 04.09.2015.
 */
public class Banners {
    private static Banners _instance;

    public static Banners getInstance()
    {

        if (_instance == null) {
            _instance = new Banners();
            //System.out.println("Banners instance");
        }
        return _instance;
    }

    public static Map<String, ArrayList<ImageInfo>>images = new HashMap<String, ArrayList<ImageInfo>>();

    public static Map<String, Integer> format = new HashMap<String, Integer>();

    public static String jsonFormat = "[]";
    public String getJsonFormat() {

        return this.jsonFormat;
    }

    public void loadImages() {
        File myFolder = new File("banners");
        File[] files = myFolder.listFiles();
        Integer nImg = 0;
        Map<String, String> unic = new HashMap<String, String>();

        for(File item: files) {
            String dir = item.toString();
            String equalIndex = dir.substring(8, dir.length());

            File indexBannerFolder = new File(dir);
            File[] images = indexBannerFolder.listFiles();

            unic.clear();

            for(File img: images) {
                nImg++;
                String imgPath = img.toString();

                ImageInfo info = this.analizeImage(imgPath);
                info.index = equalIndex;

                String index_unique = info.width + "x" + info.heigh;
                this.format.put(index_unique, 1);

                if(unic.get(index_unique) == null) {
                    //System.out.println("isUnicue ");
                    unic.put(index_unique, imgPath);
                    ArrayList<ImageInfo> tmp = this.images.get(index_unique);
                    if(tmp == null) {
                        ArrayList<ImageInfo> list = new ArrayList<ImageInfo>();
                        list.add(info);
                        this.images.put(index_unique, list);
                    } else {
                        this.images.get(index_unique).add(info);
                    }
                } else {
                    try {
                        File removeFile = new File(imgPath);
                        //removeFile.delete();
                        if(!removeFile.delete())
                            System.out.println("Delete failed");
                    } catch (Throwable e) {
                        System.out.println(e.toString());
                    }

                    System.out.println("Remove: "+imgPath);
                }


                //System.out.println(tmp);
                //this.images.put(index_unique, info);
            }
            unic.clear();
        }

        this.jsonFormat = "[";
        for(String form: format.keySet()) {
            this.jsonFormat += "\""+form+"\",";
        }
        this.jsonFormat = this.jsonFormat.substring(0, this.jsonFormat.length()-1)+"]";
        System.out.println("Loaded " + nImg + " banners");

    }
    final Random random = new Random();

    public ImageInfo getRandomBanner(String x, String y) {
        String key = x+"x"+y;
        ArrayList<ImageInfo> list = this.images.get(key);
        if(list == null) {
            return null;
            //ImageInfo info = this.analizeImage("banners/lamoda.ru/144975377037.gif");
        }
        ImageInfo info = this.images.get(key).get(random.nextInt(list.size()));

        return info;
    }


    private FileInputStream inputStream;

    // Класс для работы потоком ввода в файл
    private FileOutputStream outputStream;

    // полный путь к файлу
    private String path;

    public byte[] getFile(String path) {

        byte[] fileByte = new byte[0];
        Path wiki_path = Paths.get(path);
        try {
            fileByte = Files.readAllBytes(wiki_path);
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        return fileByte;

    }

    public ImageInfo analizeImage(String path) {
        BufferedImage bImage = null;
        Integer height = 0;
        Integer width = 0;
        String base64 = null;

        try {
            File initialImage = new File(path);
            bImage = ImageIO.read(initialImage);

            height = bImage.getHeight();
            width = bImage.getWidth();

            base64 = DatatypeConverter.printBase64Binary(this.getFile(path));
            //content = this.getFile(path);//baos.toByteArray();


        } catch (IOException e) {
            System.out.println("Exception occured :" + e.getMessage());
        }

        ImageInfo result = new ImageInfo(base64, path, width, height);
        return result;

    }

}
