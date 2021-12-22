package main;

import java.util.*;

/**
 * 电脑下棋类
 */
public class AI {

    //★移动的方向: 正向移动)
    public static final int MOVE_TYPE_FRONT = 1;
    //★移动的方向: 逆向移动
    public static final int MOVE_TYPE_BACK = 2;


    //★4个方向(1横向/2纵向/3右捺/4左撇)
    public static final int HENG_XIANG = 1;
    public static final int SHU_XIANG = 2;
    public static final int YOUP_NA = 3;
    public static final int ZUO_PIE = 4;

    //五子连子: 标识
    public static final int WIN = 100;


    /**
     * AI落子下一步
     *
     * @param gamePanel
     * @return
     */
    public static Pointer next(GamePanel gamePanel) {
        //1. 落子: 电脑通过计算走下一步
        Pointer aiPointer = go(gamePanel);
        if (aiPointer != null) {
            return aiPointer;
        }

        //2. 落子: 电脑随机走下一步
        Pointer randowPointer = luoziRandow(gamePanel);
        return randowPointer;
    }

    /**
     * 落子: 电脑随机走下一步
     *
     * @param panel
     * @return
     */
    private static Pointer luoziRandow(GamePanel panel) {
        //随机获取指针的位置
        Pointer aiPointer = getRandomPointer(panel);
        //根据位置来进行落子
        luozi(aiPointer, Qizi.TYPE_WHITE, panel);

        return aiPointer;
    }

    public static Pointer getRandomPointer(GamePanel panel) {
        //随机获取一个棋子指示器位置
        Random random = new Random();
        int i = random.nextInt(panel.ROWS);
        int j = random.nextInt(panel.COLS);
        Pointer pointer = panel.pointers[i][j];

        //如果指示器不是空白, 需要再重新随机获取一次
        if (pointer.getType() != Qizi.TYPE_EMPTY) {
            return getRandomPointer(panel);
        }

        return pointer;
    }


    /**
     * 落子: 电脑通过计算走下一步
     *
     * @param gamePanel
     * @return
     */
    private static Pointer go(GamePanel gamePanel) {
        //★计算结果: AI电脑的落子
        Pointer aiPointer;

        /*  算法逻辑:
            【1.】循环指示器
            【2.】循环4个方向(1横向/2纵向/3右捺/4左撇): 循环找出黑棋，判断此棋子的1横向  2纵向  3右捺  4左撇 是否有4子的情况
            【3.】分别计算从左往右, 从右往左, 并将返回的结果放到集合中(有效才算)
            【4.】对权重分数进行排序处理, 把最大的排到最前面
            【5.】取第一个元素作为落子的地方, 并返回true
         */
        //计算结果集合
        List<Data> datas = new ArrayList<>();

        //所有指示器(已落子位置)
        Pointer[][] pointers = gamePanel.pointers;


        //循环每一个指示器(棋子)
        Pointer pointer;
        Data data;
        for (int i = 0; i < gamePanel.ROWS; i++) {
            for (int j = 0; j < gamePanel.COLS; j++) {
                //=======【1】========数据源: 每一个指示器(棋子)=================
                //每一个指示器(棋子)
                pointer = pointers[i][j];
                //指示器没有棋子. 则跳过
                if (pointer == null || pointer.getType() == Qizi.TYPE_EMPTY) {
                    continue;
                }

                //=======【2】========循环4个方向dir, 计算分数data=================
                //循环4个方向(1横向/2纵向/3右捺/4左撇)
                int[] directionArray = {HENG_XIANG, SHU_XIANG, YOUP_NA, ZUO_PIE };
                for (int direction : directionArray) {
                    //=======【3】计算获取权重分数==>> 正向/逆向移动
                    int[] moveTypeArray = {MOVE_TYPE_FRONT, MOVE_TYPE_BACK};
                    for (int moveType : moveTypeArray) {
                        data = getData(direction, moveType, gamePanel, pointer);
                        if (data.getCount() != -1 && data.getCount() != 0) {//0和-1 的过滤掉
                            //权重分结果添加到集合中
                            datas.add(data);
                        }
                    }
                }

            }
        }


        //=======【4】========按权重分排序处理，从大到小=================
        sortList(datas);


        //=======【5】========取第一个位置落子=================
        if (datas.size() > 0) {//
            Data data2 = datas.get(0);
            aiPointer = pointers[data2.getI()][data2.getJ()];
            luozi(aiPointer, Qizi.TYPE_WHITE, gamePanel);
            return aiPointer;
        }

        return null;
    }


    /**
     * ★核心逻辑: 获取权重分数
     *
     * @param direction     ★方向
     * @param moveType      ★移动的方向(正向/逆向移动)
     * @param panel
     * @param inputPointer  ★需计算分数的有棋子的指示器
     * @return
     */
    private static Data getData(int direction, int moveType, GamePanel panel, Pointer inputPointer) {
        //=======【11111111】========最后的结果=================
        Data resData = new Data();

        //★需计算分数的有棋子的指示器
        int inputI = inputPointer.getI();
        int inputJ = inputPointer.getJ();

        //所有全量指示器
        Pointer[][] allPointers = panel.pointers;
        //所有全量指示器中--某一个临时指示器
        Pointer tempPointer;

        /** **********************计算权重分: 公共参数 *********************************/
        int num = 1;//计算分数用的num
        int num2 = 1;//累计相同的num
        boolean breakFlag = false;//移动过程中，是否出现棋子间隔形态(是否有空白子, 如: ●●空●)
        boolean lClosed = false;//左关闭(左边不能下子)
        boolean rClosed = false;//右关闭(右边不能下子)


        //=======【222222】========4个方向(1横向/2纵向/3右捺/4左撇): 判断每个格子(指示器)周围的形态=================
        //==============================================
        //横向 1
        if (direction == HENG_XIANG) {
            if (moveType == MOVE_TYPE_FRONT) {
                /** **********************
                 * 横向: 从左往右
                 *
                 * 横向下标 i 是一样的，循环从当前位置 j 加1开始。
                 * 当碰到和当前子一样的就计数器 +1。
                 * 当碰到不一样的就退出循环，表示堵住 。
                 * 如果碰到空子，是第一次计数器 +1，第二次退出循环。
                 * 判断左开和右开的状态。
                 * 根据计数器和左右开的状态，计算出分数
                 *
                 * *********************************/
                //=======【从左往右循环】========判断能与当前pointer 相同的棋子连续多少个。=================
                for (int nextIndex = inputJ + 1; nextIndex < panel.COLS; nextIndex++) {
                    //★所有全量指示器中--某一个临时指示器
                    tempPointer = allPointers[inputI][nextIndex];

                    //1. 向右连续子： 向右的临时指示器, 和传入需要计算的指示器, 相同类型棋子(如: ○->○)
                    if (tempPointer.getType() == inputPointer.getType()) {
                        num++;
                        num2++;
                        //如果达到最右边界
                        if (nextIndex == panel.COLS - 1) {
                            //右关闭(右边不能下子)
                            rClosed = true;
                        }

                     //2. 向右空白子: 向右的临时指示器, 是空白子(如: ○->空)
                    } else if (tempPointer.getType() == Qizi.TYPE_EMPTY) {
                        //之前向右移动过程中, 有一次breakFlag为true, 证明向右移动时已有一个空白子
                        if (breakFlag) {
                            //判断前一个是否空子，如果是空白, 证明这个是第二个空白子(如: ○空->空)
                            if (allPointers[inputI][nextIndex - 1].getType() == Qizi.TYPE_EMPTY) {
                                //要设置成不是中断的
                                breakFlag = false;
                            }
                            break;
                        }
                        num++;
                        //如果是空白子是边界则需要跳出
                        if (nextIndex == 0 || nextIndex == panel.COLS - 1) {
                            break;
                        }

                        breakFlag = true;
                        //[[[★出现棋子间隔形态, 这里就是AI设定落子位置(I, J)]]]
                        resData.setI(inputI);
                        resData.setJ(nextIndex);

                    //3. 向右是对立子： 向右的临时指示器, 是对立子(如: ○->●)
                    } else {
                        //右关闭(右边不能下子)
                        rClosed = true;
                        break;
                    }
                }

                //4. 【向左移动一格】========判断能与当前pointer 相同的棋子连续多少个。=================
                //当前子, 如果达到最左边界
                if (inputJ == 0) {
                    //左关闭(左边不能下子)
                    lClosed = true;
                } else {
                    if (allPointers[inputI][inputJ - 1].getType() != Qizi.TYPE_EMPTY) {
                        //左关闭(左边不能下子)
                        lClosed = true;
                    }
                }

            } else {
                /** **********************
                 * 横向: 从右往左
                 *
                 * 横向下标 i 是一样的，循环从当前位置 j 减1开始。
                 * 当碰到和当前子一样的就计数器 +1。
                 * 当碰到不一样的就退出循环，表示堵住 。
                 * 如果碰到空子，是第一次计数器 +1，第二次退出循环。
                 * 判断左开和右开的状态。
                 * 根据计数器、左右开的状态，计算出分数和落子的位置
                 *
                 * *********************************/
                //=======【从右往左循环】========判断能与当前pointer 相同的棋子连续多少个。=================
                for (int nextIndex = inputJ - 1; nextIndex >= 0; nextIndex--) {
                    //★所有全量指示器中--某一个临时指示器
                    tempPointer = allPointers[inputI][nextIndex];

                    //1. 向左连续子： 向左的临时指示器, 和传入需要计算的指示器, 相同类型棋子(如: ○<-○)
                    if (tempPointer.getType() == inputPointer.getType()) {
                        num++;
                        num2++;
                        //如果达到最左边界
                        if (nextIndex == 0) {
                            //左关闭(左边不能下子)
                            lClosed = true;
                        }

                        //2. 向左空白子: 向左的临时指示器, 是空白子(如: 空<-○)
                    } else if (tempPointer.getType() == Qizi.TYPE_EMPTY) {
                        //之前向左移动过程中, 有一次breakFlag为true, 证明向左移动时已有一个空白子
                        if (breakFlag) {
                            //判断前一个是否空子，如果是空白, 证明这个是第二个空白子(如: 空<-空○)
                            if (allPointers[inputI][nextIndex + 1].getType() == Qizi.TYPE_EMPTY) {
                                //要设置成不是中断的
                                breakFlag = false;
                            }
                            break;
                        }
                        num++;
                        //如果是空白子是边界则需要跳出
                        if (nextIndex == 0 || nextIndex == panel.COLS - 1) {
                            break;
                        }

                        breakFlag = true;
                        //【★AI落子==>>】是中断的那种，这里设定好落子位置
                        resData.setI(inputI);
                        resData.setJ(nextIndex);

                        //3. 向左是对立子： 向左的临时指示器, 是对立子(如: ●<-○)
                    } else {
                        //左关闭(左边不能下子)
                        lClosed = true;
                        break;
                    }
                }

                //4. 【向右移动一格】========判断能与当前pointer 相同的棋子连续多少个。=================
                //当前子, 如果达到最右边界
                if (inputJ == panel.COLS - 1) {
                    //右关闭(右边不能下子)
                    rClosed = true;
                } else {
                    //向右是对立子： 向右的临时指示器, 是对立子(如: ○->●)
                    if (allPointers[inputI][inputJ + 1].getType() != Qizi.TYPE_EMPTY) {
                        //右关闭(右边不能下子)
                        rClosed = true;
                    }
                }

            }


            //==============================================
            //竖向 2
        } else if (direction == SHU_XIANG) {
            if (moveType == MOVE_TYPE_FRONT) {
                /** **********************
                 * 竖向: 从上往下
                 * ***********************/
                //=======【从上往下循环】========判断能与当前pointer 相同的棋子连续多少个。=================
                for (int nextIndex = inputI + 1; nextIndex < panel.ROWS; nextIndex++) {
                    //★所有全量指示器中--某一个临时指示器
                    tempPointer = allPointers[nextIndex][inputJ];

                    //1.
                    if (tempPointer.getType() == inputPointer.getType()) {
                        num++;
                        num2++;
                        if (nextIndex == panel.ROWS - 1) {
                            rClosed = true;
                        }

                    //2.
                    } else if (tempPointer.getType() == Qizi.TYPE_EMPTY) {
                        if (breakFlag) {
                            if (allPointers[nextIndex - 1][inputJ].getType() == Qizi.TYPE_EMPTY) {
                                breakFlag = false;
                            }
                            break;
                        }
                        num++;
                        //如果是空白子是边界则需要跳出
                        if (nextIndex == 0 || nextIndex == panel.ROWS - 1) {
                            break;
                        }

                        breakFlag = true;
                        resData.setI(nextIndex);
                        resData.setJ(inputJ);

                    //3.
                    } else {
                        //右关闭(右边不能下子)
                        rClosed = true;
                        break;
                    }
                }

                //4. 当前子, 如果达到最左边界
                if (inputI == 0) {
                    lClosed = true;
                } else {
                    if (allPointers[inputI - 1][inputJ].getType() != Qizi.TYPE_EMPTY) {
                        lClosed = true;
                    }
                }

            } else {
                /** **********************
                 * 竖向: 从下往上
                 * ***********************/
                //=======【从下往上】========判断能与当前pointer 相同的棋子连续多少个。=================
                for (int nextIndex = inputI - 1; nextIndex >= 0; nextIndex--) {
                    //★所有全量指示器中--某一个临时指示器
                    tempPointer = allPointers[nextIndex][inputJ];

                    if (tempPointer.getType() == inputPointer.getType()) {
                        num++;
                        num2++;
                        //如果达到最左边界
                        if (nextIndex == 0) {
                            //左关闭(左边不能下子)
                            lClosed = true;
                        }

                        //2. 向左空白子: 向左的临时指示器, 是空白子(如: 空<-○)
                    } else if (tempPointer.getType() == Qizi.TYPE_EMPTY) {
                        //之前向左移动过程中, 有一次breakFlag为true, 证明向左移动时已有一个空白子
                        if (breakFlag) {
                            //判断前一个是否空子，如果是空白, 证明这个是第二个空白子(如: 空<-空○)
                            if (allPointers[nextIndex + 1][inputJ].getType() == Qizi.TYPE_EMPTY) {
                                //要设置成不是中断的
                                breakFlag = false;
                            }
                            break;
                        }
                        num++;
                        //如果是空白子是边界则需要跳出
                        if (nextIndex==0 || nextIndex==panel.ROWS-1) {
                            break;
                        }

                        breakFlag = true;
                        //【★AI落子==>>】是中断的那种，这里设定好落子位置
                        resData.setI(nextIndex);
                        resData.setJ(inputJ);

                        //3. 向左是对立子： 向左的临时指示器, 是对立子(如: ●<-○)
                    } else {
                        //左关闭(左边不能下子)
                        lClosed = true;
                        break;
                    }
                }

                //=======【向右移动一格】========判断能与当前pointer 相同的棋子连续多少个。=================
                //当前子, 如果达到最右边界
                if (inputI == panel.ROWS - 1) {
                    //右关闭(右边不能下子)
                    rClosed = true;
                } else {
                    //向右是对立子： 向右的临时指示器, 是对立子(如: ○->●)
                    if (allPointers[inputI + 1][inputJ].getType() != Qizi.TYPE_EMPTY) {
                        //右关闭(右边不能下子)
                        rClosed = true;
                    }
                }

            }


            //==============================================
            //右撇 3
        } else if (direction == YOUP_NA) {
            int tempi = inputI;
            if (moveType == MOVE_TYPE_FRONT) {
                /** **********************
                 * 右撇: 从左上往右下 (I, J 坐标两个都在增加)
                 * ***********************/
                //=======【从上往下循环】========判断能与当前pointer 相同的棋子连续多少个。=================
                for (int nextIndex = inputJ + 1; nextIndex < panel.COLS; nextIndex++) {
                    tempi++;
                    //超出边界, 设置为关闭
                    if (tempi > panel.COLS - 1) {
                        rClosed = true;
                        break;
                    }
                    //★所有全量指示器中--某一个临时指示器
                    tempPointer = allPointers[tempi][nextIndex];
                    //[1]
                    if (tempPointer.getType() == inputPointer.getType()) {
                        num++;
                        num2++;
                        if (nextIndex == panel.COLS - 1) {
                            rClosed = true;
                        }

                        //[2]
                    } else if (tempPointer.getType() == Qizi.TYPE_EMPTY) {
                        if (breakFlag) {
                            if (allPointers[tempi - 1][nextIndex - 1].getType() == Qizi.TYPE_EMPTY) {
                                breakFlag = false;
                            }
                            break;
                        }
                        num++;
                        //如果是空白子是边界则需要跳出
                        if (tempi == 0 || tempi == panel.ROWS - 1 || nextIndex == 0 || nextIndex == panel.COLS - 1) {
                            break;
                        }

                        breakFlag = true;
                        resData.setI(tempi);
                        resData.setJ(nextIndex);

                    } else {
                        //右关闭(右边不能下子)
                        rClosed = true;
                        break;
                    }
                }

                //当前子, 如果达到最左边界
                if (inputI == 0 || inputJ == 0) {
                    lClosed = true;
                } else {
                    if (allPointers[inputI - 1][inputJ - 1].getType() != Qizi.TYPE_EMPTY) {
                        lClosed = true;
                    }
                }

            } else {
                /** **********************
                 * 右撇: 从右下 往左上(I, J 坐标两个都在减少)
                 * *********************************/
                for (int nextIndex = inputJ - 1; nextIndex >= 0; nextIndex--) {
                    tempi--;
                    if (tempi < 0) {
                        lClosed = true;
                        break;
                    }
                    //★所有全量指示器中--某一个临时指示器
                    tempPointer = allPointers[tempi][nextIndex];

                    //1.
                    if (tempPointer.getType() == inputPointer.getType()) {
                        num++;
                        num2++;
                        //如果达到最左边界
                        if (nextIndex == 0) {
                            //左关闭(左边不能下子)
                            lClosed = true;
                        }

                        //2. 向左空白子: 向左的临时指示器, 是空白子(如: 空<-○)
                    } else if (tempPointer.getType() == Qizi.TYPE_EMPTY) {
                        //之前向左移动过程中, 有一次breakFlag为true, 证明向左移动时已有一个空白子
                        if (breakFlag) {
                            //判断前一个是否空子，如果是空白, 证明这个是第二个空白子(如: 空<-空○)
                            if (allPointers[tempi + 1][nextIndex + 1].getType() == Qizi.TYPE_EMPTY) {
                                //要设置成不是中断的
                                breakFlag = false;
                            }
                            break;
                        }
                        num++;
                        //如果是空白子是边界则需要跳出
                        if (tempi == 0 || tempi == panel.ROWS - 1 || nextIndex == 0 || nextIndex == panel.COLS - 1) {
                            break;
                        }

                        breakFlag = true;
                        //【★AI落子==>>】是中断的那种，这里设定好落子位置
                        resData.setI(tempi);
                        resData.setJ(nextIndex);

                        //3. 向左是对立子： 向左的临时指示器, 是对立子(如: ●<-○)
                    } else {
                        //左关闭(左边不能下子)
                        lClosed = true;
                        break;
                    }
                }

                //4.【向右移动一格】========判断能与当前pointer 相同的棋子连续多少个。=================
                //当前子, 如果达到最右边界
                if (inputJ == panel.COLS - 1 || inputI == panel.ROWS - 1) {
                    //右关闭(右边不能下子)
                    rClosed = true;
                } else {
                    //向右是对立子： 向右的临时指示器, 是对立子(如: ○->●)
                    if (allPointers[inputI + 1][inputJ + 1].getType() != Qizi.TYPE_EMPTY) {
                        //右关闭(右边不能下子)
                        rClosed = true;
                    }
                }

            }


            //==============================================
            //左撇 4
        } else if (direction == ZUO_PIE) {
            int tempi = inputI;
            if (moveType == MOVE_TYPE_FRONT) {
                /** **********************
                 * 左撇: 从右上往左下(I在减少,  J在增加)
                 * *********************************/
                //=======【从右上往左下】========判断能与当前pointer 相同的棋子连续多少个。=================
                for (int nextIndex = inputJ + 1; nextIndex < panel.ROWS; nextIndex++) {
                    tempi--;
                    //超出边界, 设置为关闭
                    if (tempi < 0) {
                        rClosed = true;
                        break;
                    }
                    //★所有全量指示器中--某一个临时指示器
                    tempPointer = allPointers[tempi][nextIndex];

                    //1.
                    if (tempPointer.getType() == inputPointer.getType()) {
                        num++;
                        num2++;
                        if (nextIndex == panel.COLS - 1) {
                            rClosed = true;
                        }

                        //2.
                    } else if (tempPointer.getType() == Qizi.TYPE_EMPTY) {
                        if (breakFlag) {
                            if (allPointers[tempi + 1][nextIndex - 1].getType() == Qizi.TYPE_EMPTY) {
                                breakFlag = false;
                            }
                            break;
                        }
                        num++;
                        //如果是空白子是边界则需要跳出
                        if (tempi == 0 || tempi == panel.ROWS - 1 || nextIndex == 0 || nextIndex == panel.COLS - 1) {
                            break;
                        }

                        breakFlag = true;
                        resData.setI(tempi);
                        resData.setJ(nextIndex);

                        //3.
                    } else {
                        //右关闭(右边不能下子)
                        rClosed = true;
                        break;
                    }
                }

                //4. 当前子, 如果达到最左边界
                if (inputI == 0 || inputJ == 0) {
                    lClosed = true;
                } else {
                    if (inputI == panel.ROWS - 1) {
                        lClosed = true;
                    } else {
                        tempPointer = allPointers[inputI + 1][inputJ - 1];
                        if (tempPointer.getType() != Qizi.TYPE_EMPTY) {
                            lClosed = true;
                        }
                    }
                }

            } else {
                /** **********************
                 * 左撇: 从左下往右上(J在减少,  I在增加)
                 * *********************************/
                for (int nextIndex = inputJ - 1; nextIndex >= 0; nextIndex--) {
                    tempi++;
                    if (tempi > panel.ROWS - 1) {
                        lClosed = true;
                        break;
                    }
                    //★所有全量指示器中--某一个临时指示器
                    tempPointer = allPointers[tempi][nextIndex];

                    //1.
                    if (tempPointer.getType() == inputPointer.getType()) {
                        num++;
                        num2++;
                        //如果达到最左边界
                        if (nextIndex == 0) {
                            //左关闭(左边不能下子)
                            lClosed = true;
                        }

                        //2. 向左空白子: 向左的临时指示器, 是空白子(如: 空<-○)
                    } else if (tempPointer.getType() == Qizi.TYPE_EMPTY) {
                        //之前向左移动过程中, 有一次breakFlag为true, 证明向左移动时已有一个空白子
                        if (breakFlag) {
                            //判断前一个是否空子，如果是空白, 证明这个是第二个空白子(如: 空<-空○)
                            if (allPointers[tempi - 1][nextIndex + 1].getType() == Qizi.TYPE_EMPTY) {
                                //要设置成不是中断的
                                breakFlag = false;
                            }
                            break;
                        }
                        num++;
                        //如果是空白子是边界则需要跳出
                        if (tempi == 0 || tempi == panel.ROWS - 1 || nextIndex == 0 || nextIndex == panel.COLS - 1) {
                            break;
                        }

                        breakFlag = true;
                        //【★AI落子==>>】是中断的那种，这里设定好落子位置
                        resData.setI(tempi);
                        resData.setJ(nextIndex);

                        //3. 向左是对立子： 向左的临时指示器, 是对立子(如: ●<-○)
                    } else {
                        //左关闭(左边不能下子)
                        lClosed = true;
                        break;
                    }
                }

                //4. 【向右移动一格】========判断能与当前pointer 相同的棋子连续多少个。=================
                //当前子, 如果达到最右边界
                if (inputJ == panel.COLS - 1 || inputI == panel.ROWS - 1) {
                    //右关闭(右边不能下子)
                    rClosed = true;
                } else {
                    if (inputI == 0) {
                        rClosed = true;
                    } else {
                        //向右是对立子： 向右的临时指示器, 是对立子(如: ○->●)
                        if (allPointers[inputI - 1][inputJ + 1].getType() != Qizi.TYPE_EMPTY) {
                            //右关闭(右边不能下子)
                            rClosed = true;
                        }
                    }
                }

            }


        }


        //=======【3333333333333】=======根据上面的格子周围形态分析: 设置分数==================
        setCount(resData, inputI, inputJ, direction, moveType, num, num2, breakFlag, lClosed, rClosed, panel);


        //=======【4444444444444】=======返回==================
        return resData;
    }


    /**
     * AI计算, 并设置分数, 并设置AI落子坐标(I, J)
     */
    public static void setCount(Data resData, int i, int j, int direction, int moveType, int num, int num2,
                                boolean breakFlag, boolean lClosed, boolean rClosed, GamePanel panel) {
        //★最后计算的得分
        int count = 0;

        //【只计算3个子以上的分数】
        if (num <= 1) { //TODO: 自己发现这里的数字越小, AI越聪明
            return;
        }

        /*
            【得分分数的初步设定】

            类型 	3子左开	  3子	3子右开
            得分	  32	  30	31

            类型 	4子左开	  4子	4子右开
            得分	  42	  40	41

            类型	 5子左开	  5子	5子右开
            得分	  52	  50	51
         */
        //设定默认分
        if (num == 3) {
            count = 30;
        } else if (num == 4) {
            count = 40;
        } else if (num == 5) {
            count = 50;
        }


        //★★★★★★ 五子的情况处理, 如果五子设定分数为100 ★★★★★★
        if (num2 >= 5 && !breakFlag) {
            resData.setCount(WIN);
            return;
        }
        //★★★★★★ 五子的情况处理, 如果五子设定分数为100 ★★★★★★


        /** 111111【间隔形态】===================移动过程中，是否出现棋子间隔形态(是否有空白子, 如: ○○空○)*************/
        //[[[★出现棋子间隔形态, 这里就是AI设定落子位置(I, J)]]]==>> 前面AI.getData()方法已赋值落子(I, J)
        if (breakFlag) {
            if (lClosed && rClosed) { //【当前为空白格, 但左右都关闭封堵住了】
                count = -1; //不需要落子的分数
            }

        /** 222222【连续形态】===================*************/
        } else {

            /** 333333【左右都关闭】===================*************/
            if (lClosed && rClosed) {
                count = -1; //不需要落子的分数

                /** 444444【左开放】===================*************/
            } else if (!lClosed) {
                count += 2;//加2分

                //方向: 【★AI落子==>>】
                if (direction == HENG_XIANG) {
                    if (moveType == MOVE_TYPE_FRONT) {
                        //【★AI落子==>>】在左边一个子
                        resData.setI(i);
                        resData.setJ(j - 1);
                    } else {
                        //【★AI落子==>>】在左边一个子
                        resData.setI(i);
                        resData.setJ(j - num + 1);
                    }

                    //方向: 【★AI落子==>>】
                } else if (direction == SHU_XIANG) {
                    if (moveType == MOVE_TYPE_FRONT) {
                        //【★AI落子==>>】
                        resData.setI(i - 1);
                        resData.setJ(j);
                    } else {
                        //【★AI落子==>>】在左边一个子
                        resData.setI(i - num + 1);
                        resData.setJ(j);
                    }

                    //方向: 【★AI落子==>>】
                } else if (direction == YOUP_NA) {
                    if (moveType == MOVE_TYPE_FRONT) {
                        //【★AI落子==>>】
                        resData.setI(i - 1);
                        resData.setJ(j - 1);
                    } else {
                        //【★AI落子==>>】在左边一个子
                        resData.setI(i - num + 1);
                        resData.setJ(j - num + 1);
                    }

                    //方向: 【★AI落子==>>】
                } else if (direction == ZUO_PIE) {
                    if (moveType == MOVE_TYPE_FRONT) {
                        //【★AI落子==>>】
                        resData.setI(i + 1);
                        resData.setJ(j - 1);
                    } else {
                        //【★AI落子==>>】在左边一个子
                        resData.setI(i + num - 1);
                        resData.setJ(j - num + 1);
                    }
                }


                /** 555555【右边开放】===================*************/
            } else if (!rClosed) {
                count += 1;//加1分

                //方向: 【★AI落子==>>】
                if (direction == HENG_XIANG) {
                    if (moveType == MOVE_TYPE_FRONT) {
                        resData.setI(i);
                        resData.setJ(j + num - 1);
                    } else {
                        resData.setI(i);
                        resData.setJ(j + 1);
                    }

                    //方向: 【★AI落子==>>】
                } else if (direction == SHU_XIANG) {
                    if (moveType == MOVE_TYPE_FRONT) {
                        resData.setI(i + num - 1);
                        resData.setJ(j);
                    } else {
                        resData.setI(i + 1);
                        resData.setJ(j);
                    }

                    //方向: 【★AI落子==>>】
                } else if (direction == YOUP_NA) {
                    if (moveType == MOVE_TYPE_FRONT) {
                        resData.setI(i + num - 1);
                        resData.setJ(j + num - 1);
                    } else {
                        resData.setI(i + 1);
                        resData.setJ(j + 1);
                    }

                    //方向: 【★AI落子==>>】
                } else if (direction == ZUO_PIE) {
                    if (moveType == MOVE_TYPE_FRONT) {
                        resData.setI(i - num + 1);
                        resData.setJ(j + num - 1);
                    } else {
                        resData.setI(i - 1);
                        resData.setJ(j + 1);
                    }
                }

            }

        }

        resData.setCount(count);
    }


    /**
     * AI: 棋子落子
     *
     * @return
     */
    private static void luozi(Pointer pointer, int type, GamePanel panel) {
        //创建一个棋子对象
        Qizi qizi = new Qizi(pointer.getX(), pointer.getY(), type);
        //设置棋子最后一步标识
        qizi.setIsLast(true);
        //将棋子添加到集合中
        panel.qizis.add(qizi);
        //棋子的类型
        pointer.setType(type);

        ////重新绘制
        //panel.repaint();
    }



    /**
     * 判断五子连珠
     *
     * @param nowPointer  ★当前指示器位置(当前的棋子)
     * @param panel
     * @return
     */
    public static boolean has5(Pointer nowPointer, GamePanel panel) {
        List<Data> datas = new ArrayList<>();

        //循环找出黑棋, 判断棋子 4个方向(1横向/2纵向/3右捺/4左撇) 的情况
        Pointer pointer;
        for (int i = 0; i < panel.ROWS; i++) {
            for (int j = 0; j < panel.COLS; j++) {

                //=======【1】========数据源: 每一个指示器(棋子)=================
                //每一个指示器(棋子)
                pointer = panel.pointers[i][j];
                //指示器没有棋子. 则跳过
                if (pointer == null || pointer.getType() == Qizi.TYPE_EMPTY) {
                    continue;
                }
                if (nowPointer.getType() != pointer.getType()) {
                    continue;
                }

                //=======【2】========循环4个方向=================
                //循环4个方向(1横向/2纵向/3右捺/4左撇)
                int[] directionArray = {HENG_XIANG, SHU_XIANG, YOUP_NA, ZUO_PIE};
                for (int direction : directionArray) {
                    Data data = getData(direction, 1, panel, pointer);
                    if (data.getCount() != -1 && data.getCount() != 0) {//0和-1 的过滤掉
                        datas.add(data);
                    }
                }

            }
        }


        //=======【3】========按权重分排序处理，从大到小=================
        sortList(datas);


        //=======【4】========取第一个位置落子=================
        if (datas.size() > 0) {
            Data data = datas.get(0);
            //★★★★★★ 五子的情况处理, 如果五子设定分数为100 ★★★★★★
            if (data.getCount() == WIN) {
                return true;
            }
            //★★★★★★ 五子的情况处理, 如果五子设定分数为100 ★★★★★★
        }

        return false;
    }


    /**
     * 排序: 倒序, 从大到小
     * @param datas
     */
    private static void sortList(List<Data> datas) {
        Collections.sort(datas, new Comparator<Data>() {
            @Override
            public int compare(Data o1, Data o2) {
                return o2.getCount() - o1.getCount();
            }
        });
        System.out.println("计算分数, 排序后datas: " + datas.toString());
    }


}
