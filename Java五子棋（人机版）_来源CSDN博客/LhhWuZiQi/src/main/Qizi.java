package main;

import common.ImageValue;
import java.awt.*;

/**
 * 棋子类
 */
public class Qizi {

    private int x = 0;//x坐标
    private int y = 0;//y坐标
    private int r = 36;//高
//    private GamePanel panel = null;
//    private Color color = null;

    /**
     * 棋子类型 0: 空白 1：白棋 2：黑棋
     */
    private int type = 1;
    public static final int TYPE_EMPTY = 0;
    public static final int TYPE_WHITE = 1;
    public static final int TYPE_BLACK = 2;

    private boolean isLast = false;//是否最后一步棋子


    //构造方法
    public Qizi(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    //图片绘制方法
    void draw(Graphics g) {
        //选择棋子的图片
        if (type == TYPE_WHITE) {// 白色
            g.drawImage(ImageValue.whiteImage, x - r / 2, y - r / 2, r, r, null);
        } else {// 黑色
            g.drawImage(ImageValue.blackImage, x - r / 2, y - r / 2, r, r, null);
        }

        //是否最后一步棋子
        if (isLast) {
            //转换成2d画笔
            Graphics2D g2d = (Graphics2D) g;
            //设置画笔的粗细
            g2d.setStroke(new BasicStroke(2.0f));

            //绘制小方形
            int h = 6;
            g2d.drawRect(x - h / 2, y - h / 2, h, h);
        }

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getIsLast() {
        return isLast;
    }

    public void setIsLast(boolean last) {
        this.isLast = last;
    }

}
