package main;

import javax.swing.*;
import java.awt.*;

/**
 * 窗体类
 */
public class GameFrame extends JFrame {

    //构造方法
    public GameFrame() {
        setTitle("LHH五子棋");//设置标题
        setSize(620, 670);//设置窗体大小
        getContentPane().setBackground(new Color(209, 146, 17));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//关闭后进程退出
        setLocationRelativeTo(null);//居中
        setResizable(false);//不允变大变小
    }


}
