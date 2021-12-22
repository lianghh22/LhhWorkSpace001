package main;

import common.ImageValue;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 画布类
 */
public class GamePanel extends JPanel implements ActionListener {

    private JMenuBar jmb = null;
    private GameFrame mainFrame = null;
    private GamePanel panel = null;

    //网格线: 15行
    public final int ROWS = 15;
    //网格线: 15列
    public final int COLS = 15;

    public static final String START = "start";//游戏状态: 正在运行

    public String gameFlag = "";//游戏状态

    //指示器所有对象: 二维数组
    public Pointer[][] pointers = new Pointer[ROWS][COLS];

    //棋子所有对象: 二维数组
    public List<Qizi> qizis = new ArrayList<Qizi>();

    /**
     * 【★重点】构造方法
     */
    public GamePanel(GameFrame mainFrame) {
        this.setLayout(null);
        this.setOpaque(false);
        this.mainFrame = mainFrame;
        this.panel = this;

        //图片的加载
        ImageValue.init();
        //创建菜单
        createMenu();
        //创建鼠标监听
        createMouseListener();
        //创建并初始化: 所有指示器参数(二维数组)
        createPointers();

        //游戏开始标识
        gameFlag = START;
    }

    /**
     * 【★重点】重写绘图方法
     *
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //绘制网格
        drwaGrid(g);
        //绘制5个小黑点
        drwa5Point(g);
        //绘制(指示器二维数组), 根据每个指示器参数绘制(在构造方法已设置好参数)
        drawPointer(g);
        //绘制棋子
        drwaQizi(g);
    }


    /**
     * 【★重点】鼠标监听方法
     */
    private void createMouseListener() {
        //java语法: 匿名对象
        MouseAdapter mouseAdapter = new MouseAdapter() {

            /**
             * 【鼠标点击事件】重写父类方法
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                System.err.println("===触发【鼠标点击事件】===");

                //先判断游戏状态
                if (!START.equals(gameFlag)) {
                    System.err.println("===触发【鼠标点击事件】后, 游戏状态不是start, 不能落子===");
                    if ("win".equals(gameFlag)) {
                        gameWin();
                    }
                    if ("end".equals(gameFlag)) {
                        gameOver();
                    }
                    return;
                }

                //获取鼠标坐标
                int x = e.getX();
                int y = e.getY();
                System.err.println("===【鼠标点击事件】=== [x: " + x + "] y: [" + e.getX() + "]");

                //循环所有指示器(二维数组)
                Pointer pointer;
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        //得到每一个指示器对象
                        pointer = pointers[i][j];
                        //鼠标坐标在指示器上, 就将指示器设置为[显示]状态 && 当前位置没有棋子
                        if (pointer.isPoint(x, y) && pointer.getType() == Qizi.TYPE_EMPTY) {
                            System.err.println("===【鼠标点击事件】=== 当前位置没有落子, 画出(棋子!!!)");

                            //★创建[玩家棋子], 玩家鼠标点击: 棋子的中心就是指示器的中心坐标
                            Pointer playerPointer = pointer;
                            Qizi qizi = new Qizi(playerPointer.getX(), playerPointer.getY(), 2);

                            //将[玩家棋子]存放到容器中
                            qizis.add(qizi);

                            //[玩家棋子]指示器, 设置棋子黑白类型
                            playerPointer.setType(Qizi.TYPE_BLACK);

                            //清除棋子旧的最后一步标识
                            clearAllLast();

                            //★【画出[玩家棋子]下一步】==>> 重新绘画 ==>因为上面改变了参数
                            repaint();

                            /**========START=======判断胜利, 或AI落子下一步=============================*/
                            if (AI.has5(playerPointer, panel)) {
                                gameWin();//★★★★★★ 【玩家胜利】==>> 判断是否五子连棋
                            } else {
                                //★【画出[AI棋子]下一步】
                                Pointer aiPointer = AI.next(panel);
                                repaint();

                                if (AI.has5(aiPointer, panel)) {
                                    gameOver();//★★★★★★ 【AI胜利】==>> 判断[AI落子]是否五子连棋
                                }
                            }
                            /**========END=======判断胜利, 或AI落子下一步=============================*/

                            break;
                        }
                    }
                }
            }



            /**
             * 【鼠标移动事件】重写父类方法
             */
            @Override
            public void mouseMoved(MouseEvent e) {
                System.err.println("===触发【鼠标移动事件】===");

                //获取鼠标坐标
                int x = e.getX();
                int y = e.getY();
                System.err.println("===【鼠标移动事件】=== [x: " + x + "] y: [" + e.getX() + "]");

                //循环指示器二位数组
                Pointer pointer;
                for (int i = 0; i < ROWS; i++) {
                    for (int j = 0; j < COLS; j++) {
                        //得到每一个指示器对象
                        pointer = pointers[i][j];
                        //鼠标坐标在指示器上, 就将指示器设置为[显示]状态
                        if (pointer.isPoint(x, y) && pointer.getType() == Qizi.TYPE_EMPTY) {
                            System.err.println("===【鼠标移动事件】=== 当前位置没有棋子, 画出(!!指示器!!)");

                            pointer.setShow(true);
                        } else {
                            pointer.setShow(false);
                        }
                    }
                }

                //★重新绘画(因为上面setShow(true)改变了参数)
                repaint();
            }

        };


        addMouseMotionListener(mouseAdapter);
        addMouseListener(mouseAdapter);
    }


    /**
     * 清除棋子旧的最后一步标识
     */
    private void clearAllLast() {
        for (Qizi qizi : qizis) {
            qizi.setIsLast(false);
        }
    }


    /**
     * 绘制棋子
     */
    private void drwaQizi(Graphics g) {
        for (Qizi qizi : qizis) {
            qizi.draw(g);
        }
    }


    /**
     * 绘制(指示器二维数组)
     */
    private void drawPointer(Graphics g) {
        Pointer pointer;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < ROWS; j++) {
                pointer = pointers[i][j];
                if (pointer != null) {
                    pointer.draw(g);
                }
            }
        }
    }


    /**
     * 创建并初始化: 所有指示器参数(二维数组)
     */
    private void createPointers() {
        int x=0;
        int y=0;
        int start=26;

        Pointer pointer;
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                //i, j: 二位数组的索引
                //x, y: 指示器的坐标
                x = j * 40 + start;
                y = i * 40 + start;

                //设置好每个指示器参数
                pointer = new Pointer(i, j, x, y);

                //将所有指示器对象, 放到二位数组容器中
                pointers[i][j] = pointer;
            }
        }

        System.err.println("===指示器对象(二维数组), 创建完成===");
    }


    /**
     * 绘制5个小黑点
     * @param g
     */
    private void drwa5Point(Graphics g) {
        /*
        绘制第点(左上)
         */
        int yuanBanJing = 8;//圆半径
        int yuanX = 146 - yuanBanJing / 2;//圆左上角坐标 - 圆半径 = 圆在交叉点点中央
        int yuanY = 146 - yuanBanJing / 2;
        g.fillArc(yuanX, yuanY, yuanBanJing, yuanBanJing, 0, 360);
        /*
        绘制第点(右上)
         */
        yuanX = 462;
        g.fillArc(yuanX, yuanY, yuanBanJing, yuanBanJing, 0, 360);
        /*
        绘制第点(左下)
         */
        yuanX = 142;
        yuanY = 462;
        g.fillArc(yuanX, yuanY, yuanBanJing, yuanBanJing, 0, 360);
        /*
        绘制第点(右下)
         */
        yuanX = 462;
        g.fillArc(yuanX, yuanY, yuanBanJing, yuanBanJing, 0, 360);
        /*
        绘制第点(中间)
         */
        yuanX = 302;
        yuanY = 302;
        g.fillArc(yuanX, yuanY, yuanBanJing, yuanBanJing, 0, 360);
    }


    /**
     * 绘制网格
     * @param g
     */
    private void drwaGrid(Graphics g) {
        int start = 26;//起始位置
        //循环画网格线
        int x1 = 26;
        int y1 = 26;
        int x2 = 586;
        int y2 = 26;
        int dis = 40;//线间距

        //绘制15条横向线
        for (int i = 0; i < ROWS; i++) {
            y1 = i * dis + start;
            y2 = y1;
            g.drawLine(x1, y1, x2, y2);
        }
        //绘制15条竖向线
        y1=26;
        y2=586;
        for (int i = 0; i < COLS; i++) {
            x1 = i * dis + start;
            x2 = x1;
            g.drawLine(x1, y1, x2, y2);
        }

    }


    /**
     * 创建字体
     * @return
     */
    private Font createFont() {
        return new Font("思源宋体", Font.BOLD, 18);
    }

    /**
     * 创建菜单
     */
    private void createMenu() {
        //创建JMenuBar
        jmb = new JMenuBar();
        //创建字体
        Font tFont = createFont();
        //创建窗体选项
        JMenu jMenul1 = new JMenu("游戏");
        jMenul1.setFont(tFont);
        JMenu jMenul2 = new JMenu("帮助");
        jMenul2.setFont(tFont);
        jmb.add(jMenul1);
        jmb.add(jMenul2);

        mainFrame.setJMenuBar(jmb);

        //添加菜单选项
        JMenuItem jmi1 = new JMenuItem("新游戏");
        jmi1.setFont(tFont);
        JMenuItem jmi2 = new JMenuItem("退出");
        jmi2.setFont(tFont);
        jMenul1.add(jmi1);
        jMenul1.add(jmi2);

        //添加菜单选项
        JMenuItem jmi3 = new JMenuItem("操作帮助");
        jmi3.setFont(tFont);
        JMenuItem jmi4 = new JMenuItem("胜利条件");
        jmi4.setFont(tFont);
        jMenul2.add(jmi3);
        jMenul2.add(jmi4);


        //菜单按钮增加监听
        jmi1.addActionListener(this);
        jmi2.addActionListener(this);
        jmi3.addActionListener(this);
        jmi4.addActionListener(this);
        //设置指令
        jmi1.setActionCommand("restart");
        jmi2.setActionCommand("exit");
        jmi3.setActionCommand("help");
        jmi4.setActionCommand("win");
    }

    /**
     * 重写监听方法: 监听菜单--按钮点击
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        //传入的监听对象:  按钮名称
        String command = e.getActionCommand();
        System.err.println("传入的监听对象:  按钮名称: " + command);

        //逻辑判断
        UIManager.put("OptionPane.buttonFont", new FontUIResource(new Font("思源宋体", Font.ITALIC, 18)));
        UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("思源宋体", Font.ITALIC, 18)));
        if ("exit".equals(command)) {
            Object[] options = {"确定", "取消"};
            int response = JOptionPane.showOptionDialog(this, "您确认要退出吗", "",
                    JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    options, options[0]);
            if (response == 0) {
                System.exit(0);
            }
        } else if ("restart".equals(command)) {
//            if (!"end".equals(panel.gameFlag)) {
//                JOptionPane.showMessageDialog(null, "正在游戏中无法重新开始！",
//                        "提示！", JOptionPane.INFORMATION_MESSAGE);
//            } else {
                if (panel != null) {
                    restart();
                }
            //}
        } else if ("help".equals(command)) {
            JOptionPane.showMessageDialog(null, "鼠标在指示器位置点下，则落子！",
                    "提示！", JOptionPane.INFORMATION_MESSAGE);
        } else if ("win".equals(command)) {
            JOptionPane.showMessageDialog(null, "五子连珠方获得胜利！",
                    "提示！", JOptionPane.INFORMATION_MESSAGE);
        }

    }


    /**
     * 重新开始: 参数重置
     */
    private void restart() {
        //1. 游戏状态
        gameFlag = START;
        System.err.println("重置: 1. 游戏状态");

        //2. 指示器
        for (Pointer[] pointerArray : pointers) {
            for (Pointer pointer : pointerArray) {
                pointer.setType(Qizi.TYPE_EMPTY);
                pointer.setShow(false);
            }
        }
        System.err.println("重置: 2. 指示器");

        //3. 棋子
        qizis.clear();
        System.err.println("重置: 3. 棋子");
    }


    /**
     * 游戏胜利
     */
    private void gameWin() {
        gameFlag = "win";
        //弹出提示
        UIManager.put("OptionPane.buttonFont", new FontUIResource(new Font("思源宋体", Font.ITALIC, 18)));
        UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("思源宋体", Font.ITALIC, 18)));
        JOptionPane.showMessageDialog(mainFrame, "啦啦啦, 胜利啦~");
    }


    /**
     * 游戏结束
     */
    private void gameOver() {
        gameFlag = "end";
        //弹出提示
        UIManager.put("OptionPane.buttonFont", new FontUIResource(new Font("思源宋体", Font.ITALIC, 18)));
        UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("思源宋体", Font.ITALIC, 18)));
        JOptionPane.showMessageDialog(mainFrame, "你失败了, 请重新再来~");
    }


}
