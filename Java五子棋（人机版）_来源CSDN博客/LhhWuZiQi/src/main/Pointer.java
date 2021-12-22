package main;

import java.awt.*;

/**
 * 指示器类
 */
public class Pointer {

//    private GamePanel panel = null;
//    private Color color = null;
//    private int qizi = 0;//棋子类型 0：无  1：白棋  2：黑棋

    private int i = 0;//二维下标i
    private int j = 0;//二维下标j

    private int x = 0;//坐标X
    private int y = 0;//坐标Y

    private int h = 36;//指示的大小

    private boolean isShow = false;//是否展示指示器, 默认false不显示

    private int type = 0;//0: 空白, 1: 白棋子, 2: 黑棋子


    //构造方法
    public Pointer(int i, int j, int x, int y) {
        this.i = i;
        this.j = j;
        this.x = x;
        this.y = y;
    }


    /**
     * 绘制指示器的方法(红色正方形)
     *
     * @return
     */
    public void draw(Graphics g) {
        //绘制指示器--颜色
        g.setColor(Color.RED);

        if (isShow) {
            ////绘制指示器--位置坐标
            //g.drawRect(x - h / 2, y - h / 2, h, h);
            //绘制指示器的样式
            drawPointer(g);
        }
    }


    /**
     * 绘制指示器的样式
     * @param g
     */
    private void drawPointer(Graphics g) {
        //转换成2d画笔
        Graphics2D g2d = (Graphics2D) g;
        //设置画笔的粗细
        g2d.setStroke(new BasicStroke(2.0f));

        int x1 = 0;
        int y1 = 0;
        int x2 = 0;
        int y2 = 0;

        /*
        指示器瞄准红色框框, 画出来
                ┍    ┑

                ┕    ┙
         */
        //【左上原点】
        x1 = this.x - h / 2;
        y1 = this.y - h / 2;
        //左上(横线)
        x2 = x1 + h / 4;
        y2 = y1;
        g2d.drawLine(x1, y1, x2, y2);
        //左上(竖线)
        x2 = x1;
        y2 = y1 + h / 4;
        g2d.drawLine(x1, y1, x2, y2);

        //【右上原点】
        x1 = this.x + h / 2;
        y1 = this.y - h / 2;
        //右上(横线)
        x2 = x1 - h/4;
        y2 = y1;
        g2d.drawLine(x1, y1, x2, y2);
        //右上(竖线)
        x2 = x1;
        y2 = y1 + h / 4;
        g2d.drawLine(x1, y1, x2, y2);


        //【右下原点】
        x1 = this.x + h / 2;
        y1 = this.y + h / 2;
        //右下(横线)
        x2 = x1 - h / 4;
        y2 = y1;
        g2d.drawLine(x1, y1, x2, y2);
        //右下(竖线)
        x2 = x1;
        y2 = y1 - h / 4;
        g2d.drawLine(x1, y1, x2, y2);


        //【左下原点】
        x1 = this.x - h / 2;
        y1 = this.y + h / 2;
        //左下(横线)
        x2 = x1 + h / 4;
        y2 = y1;
        g2d.drawLine(x1, y1, x2, y2);
        //左下(竖线)
        x2 = x1;
        y2 = y1 - h / 4;
        g2d.drawLine(x1, y1, x2, y2);
    }


    /**
     * 判断鼠标坐标是否在指示器的范围内
     *
     * @param x  鼠标的坐标
     * @param y  鼠标的坐标
     * @return
     */
    boolean isPoint(int x, int y) {
        //指示器(左上角)
        int x1 = this.x - h / 2;
        int y1 = this.y - h / 2;

        //指示器(右上角)
        int x2 = this.x + h / 2;
        int y2 = this.y + h / 2;

        //传入的鼠标坐标(x, y): 大于[指示器坐标]左上角，小于[指示器坐标]右下角的坐标, 证明鼠标则肯定在[指示器坐标]范围内
        if (x > x1 && y > y1 && x < x2 && y < y2) {
            return true;
        }

        return false;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }

//    public int getQizi() {
//        return qizi;
//    }
//
//    public void setQizi(int qizi) {
//        this.qizi = qizi;
//    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public void setJ(int j) {
        this.j = j;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
