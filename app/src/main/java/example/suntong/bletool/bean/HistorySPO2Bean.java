package example.suntong.bletool.bean;

public class HistorySPO2Bean {
    int hour;
    int min;
    int data;

    public HistorySPO2Bean(int hour, int min, int data) {
        this.hour = hour;
        this.min = min;
        this.data = data;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }
}
