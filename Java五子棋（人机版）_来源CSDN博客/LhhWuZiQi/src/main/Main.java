package main;

/**
 * 启动类
 */
public class Main {


    public static void main(String[] args) {
        GameFrame frame = new GameFrame();
        GamePanel panel = new GamePanel(frame);
        frame.add(panel);

        //上面加载完之后, 最后才显示窗体
        frame.setVisible(true);
    }


}
