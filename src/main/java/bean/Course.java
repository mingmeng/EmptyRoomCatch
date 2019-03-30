package bean;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Course {
    private int classroom;
    private int weekday;
    private int startPeriod;
    private int endPeriod;
    private ArrayList<Integer> week;

    @Override
    public String toString() {
        return "教室：" + classroom + " 周几：" + weekday + " 开始节数：" + startPeriod + " 结束节数：" + endPeriod + " 周数：" + week.toString();
    }
}
