package model;

import java.util.ArrayList;

public interface EmptyRoom {
    ArrayList<Integer> getEmptyRoom(int building, int week, int weekday, ArrayList<String> period);
}
