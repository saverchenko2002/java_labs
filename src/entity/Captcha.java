package entity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;

public class Captcha {
    private String name;
    private String fileWay;
    private BufferedImage image;

    public Captcha(String name, String fileWay){
        this.name = name;
        this.fileWay = fileWay;
//        try {
//            this.image = ImageIO.read(new File(fileWay));
//        }
//        catch (IOException e){
//            e.printStackTrace();
//        }
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getFileWay(){
        return fileWay;
    }

    public void setNewImage(String fileWay){
        this.fileWay = fileWay;
//        try {
//            this.image = ImageIO.read(new File(fileWay));
//        }
//        catch (IOException e){
//            e.printStackTrace();
//        }
    }

//    public Image getImage(){
//        return image;
//    }
}
