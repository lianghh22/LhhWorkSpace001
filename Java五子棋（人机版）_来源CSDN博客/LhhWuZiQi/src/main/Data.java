package main;

/**
 * 返回的数据
 */
public class Data {

    //权重方向
    private int count = 0;
    //下标i
    private int i = 0;
    //下标j
    private int j = 0;


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    @Override
    public String toString() {
        return "Data{" +
                "count=" + count +
                '}';
    }
}
