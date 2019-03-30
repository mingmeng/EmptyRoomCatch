
package model;

import bean.OneCourse;
import config.Classroom;
import redis.clients.jedis.Jedis;
import utils.CURL;
import utils.RedisPool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiThreaded {
    public static void main(String[] args) {
        //初始化连接池
        RedisPool.initialPool();
        //启动1000个线程
        CopyOnWriteArrayList<String> copyOnWriteArrayList = new CopyOnWriteArrayList();
        copyOnWriteArrayList.addAll(Arrays.asList(Classroom.BUILDINGS));
        try {
            Jedis jedis = RedisPool.getConn();
            jedis.flushDB();
        } finally {
            RedisPool.closeConn();
        }
        for (String string : copyOnWriteArrayList) {
            ClientThread clientThread = new ClientThread(string);
            clientThread.start();
        }

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String datetime = simpleDateFormat.format(date);
        System.out.println("在 " + datetime + " 成功抓取空教室数据");
    }
}

//线程类
class ClientThread extends Thread {
    String string = "";

    ClientThread(String str) {
        this.string = str;
    }

    public void run() {
        getData();
    }

    public synchronized void getData() {
        ArrayList<OneCourse> oneCourseList;
        oneCourseList = CURL.getCourseCondition(string);
        if (oneCourseList == null)
            return;
        try {
            Jedis jedis = RedisPool.getConn();
            jedis.sadd("allClassroom", string);
            for (OneCourse oneCourse : oneCourseList) {
                jedis.sadd(oneCourse.getWeek() + "_" + oneCourse.getWeekday() + "_" + oneCourse.getPeriod(), String.valueOf(oneCourse.getNum()));
            }
        } finally {
            RedisPool.closeConn();
        }
    }
}
