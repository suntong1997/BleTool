package example.suntong.bletool.bean;

public class HistoryHRBean {
   int hour;
   int min;
    int HR;
    int DBP;
    int SBP;
    int RR;

    public HistoryHRBean(int hour, int min, int HR, int DBP, int SBP, int RR) {
        this.hour = hour;
        this.min = min;
        this.HR = HR;
        this.DBP = DBP;
        this.SBP = SBP;
        this.RR = RR;
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

    public int getHR() {
        return HR;
    }

    public void setHR(int HR) {
        this.HR = HR;
    }

    public int getDBP() {
        return DBP;
    }

    public void setDBP(int DBP) {
        this.DBP = DBP;
    }

    public int getSBP() {
        return SBP;
    }

    public void setSBP(int SBP) {
        this.SBP = SBP;
    }

    public int getRR() {
        return RR;
    }

    public void setRR(int RR) {
        this.RR = RR;
    }
}
