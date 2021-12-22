package common;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * 图片加载
 */
public class ImageValue {

    //白棋子图片
    public static BufferedImage whiteImage = null;
    //黑棋子图片
    public static BufferedImage blackImage = null;
    //图片路径
    private static String path = "/images/";


    /**
     * 初始化图片方法
     */
    public static void init(){
        try {
            whiteImage = ImageIO.read(ImageValue.class.getResource(path + "white.png"));
            blackImage = ImageIO.read(ImageValue.class.getResource(path + "black.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
