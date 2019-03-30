package model.Impl;

import config.Classroom;
import model.EmptyRoom;
import utils.RedisPool;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EmptyRoomImpl implements EmptyRoom {
    @Override
    public ArrayList<Integer> getEmptyRoom(int building, int week, int weekday, ArrayList<String> period) {
        RedisPool.initialPool();
        ArrayList<Integer> resultList = new ArrayList<>();
        try {
            Jedis jedis = RedisPool.getConn();
            for (int i = period.size(); i < 12; i++) {
                period.add(period.get(period.size() - 1));
            }
            for (int i = 0; i < 12; i++) {
                period.set(i, week + "_" + weekday + "_" + (Integer.parseInt(period.get(i))));
            }
            Set<String> classroomSet = jedis.sunion(period.get(0), period.get(1), period.get(2), period.get(3), period.get(4), period.get(5), period.get(6), period.get(7), period.get(8), period.get(9), period.get(10), period.get(11));
            Set<String> allClassroomSet = new HashSet<>(Arrays.asList(Classroom.BUILDINGS));

            allClassroomSet.removeAll(classroomSet);
            for (String string : allClassroomSet) {
                if (Integer.parseInt(string) / 1000 == building)
                    resultList.add(Integer.parseInt(string));
            }
        } finally {
            RedisPool.closeConn();
        }
        return resultList;
    }

}
