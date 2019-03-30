package utils;

import bean.Course;
import bean.OneCourse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CURL {
    private static final String URL = "http://jwzx.cqupt.edu.cn/kebiao/kb_room.php";

    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
//            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.out.println(key + "--->" + map.get(key));
//            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取教室课程情况
     *
     * @param roomNum
     * @return ArrayList
     */
    public static ArrayList<OneCourse> getCourseCondition(String roomNum) {
        String pageContent = sendGet(URL, "room=" + roomNum);

        pageContent = pageContent.replaceAll("\t", "").replaceAll("\r", "").replaceAll("\n", "");

        String tmpResult = pageContent;
        Pattern pattern = Pattern.compile("<tbody>(.*?)</tbody>");
        Matcher matcher = pattern.matcher(tmpResult);

        String result = "";
        while (matcher.find()) {
            result = matcher.group();
        }


        //解决不标准html标签问题
        //2018/10/17 by 杨瑞鑫
        result = result.replaceAll("<tr >", "<tr>").replaceAll("<td >", "<td>");

        pattern = Pattern.compile("<tr>(.*?)</tr>");
        matcher = pattern.matcher(result);
        ArrayList<String> resultList = new ArrayList<>();
        while (matcher.find()) {
            resultList.add(matcher.group());
        }
        //确认这教室有课没有
        //没有返回null
        if (resultList.size() == 0)
            return null;
        ArrayList<String> weekList = new ArrayList<>();

        for (String tmpStr : resultList) {
            tmpStr = tmpStr.replaceAll("<tr>", "").replaceAll("</tr>", "").replaceAll(" align='center'", "");
            pattern = Pattern.compile("<td>(.*?)</td>");
            matcher = pattern.matcher(tmpStr);
            String timestr = "";
            int n = 0;
            while (matcher.find()) {
                if (n == 1) {
                    timestr = matcher.group(1);
                    break;
                }
                n++;
            }
            //解决傻逼教务在线的傻逼申请时间
            //by 杨瑞鑫 2018/10/22
            if (timestr.contains("中午") || timestr.contains("下午") || timestr.contains("无安排"))
                continue;
            else
                weekList.add(timestr);
        }
        ArrayList<OneCourse> oneCourses = new ArrayList<>();
        ArrayList<Course> courseTimes = getCourseTime(weekList, roomNum);
        if (courseTimes != null) {
            for (Course course : courseTimes) {
                for (int j = course.getStartPeriod(); j <= course.getEndPeriod(); j++) {
                    for (int week : course.getWeek()) {
                        OneCourse oneCourse = new OneCourse();
                        oneCourse.setNum(course.getClassroom());
                        oneCourse.setPeriod(j);
                        oneCourse.setWeek(week);
                        oneCourse.setWeekday(course.getWeekday());
                        oneCourses.add(oneCourse);
                    }
                }
            }
            return oneCourses;
        } else
            return null;
    }

    //周数字符串的解析
    public static ArrayList<Course> getCourseTime(ArrayList arrayList, String num) {
        ArrayList<Course> courses = new ArrayList<>();
        for (Object anArrayList : arrayList) {
            String timeStr = (String) anArrayList;
            timeStr = timeStr.replace(" ", "").replace("星期", "").replace("第", "|").replace("节", "|");

            try {
                Course course = new Course();
                course.setClassroom(Integer.parseInt(num));
                String[] courseStr = timeStr.split("\\|");
                course.setWeekday(Integer.parseInt(courseStr[0]));
                String[] periodArr = courseStr[1].split("-");
                course.setStartPeriod(Integer.parseInt(periodArr[0]));
                course.setEndPeriod(Integer.parseInt(periodArr[1]));


                ArrayList<Integer> weekList = new ArrayList<>();

                if (courseStr[2].contains(",")) {
                    String[] weekItems = courseStr[2].split(",");
                    for (String weekItem : weekItems) {
                        weekList.addAll(getWeekArr(weekItem));
                    }
                } else {
                    weekList.addAll(getWeekArr(courseStr[2]));
                }
                course.setWeek(weekList);
                courses.add(course);
            } catch (Exception e) {
                System.out.println("有问题的教室是：" + num);
            }
        }

        return courses;
    }

    //对于周数字符串的解析
    public static ArrayList<Integer> getWeekArr(String s) {
        ArrayList<Integer> result = new ArrayList<>();
        s = s.replace(" ", "");
        if (s.contains("单周")) {
            String[] startAndEnd = s.replace("单周", "").replace("周", "").split("-");
            int start = Integer.parseInt(startAndEnd[0]);
            int end = Integer.parseInt(startAndEnd[1]);
            for (int i = 0, length = end - start; i <= length; i++) {
                if (!isOdd(start + i))
                    result.add(start + i);
            }
        } else if (s.contains("双周")) {
            String[] startAndEnd = s.replace("双周", "").replace("周", "").split("-");
            int start = Integer.parseInt(startAndEnd[0]);
            int end = Integer.parseInt(startAndEnd[1]);
            for (int i = 0, length = end - start; i <= length; i++) {
                if (isOdd(start + i))
                    result.add(start + i);
            }
        } else if (s.contains("-")) {
            String[] startAndEnd = s.replace("周", "").split("-");
            int start = Integer.parseInt(startAndEnd[0]);
            int end = Integer.parseInt(startAndEnd[1]);
            for (int i = 0, length = end - start; i <= length; i++) {
                result.add(start + i);
            }
        } else {
            s = s.replace("周", "");
            result.add(Integer.parseInt(s));
        }
        return result;
    }

    private static boolean isOdd(int a) {
        return (a & 1) != 1;
    }

//    public static void main(String[] args) {
//        System.out.println(getCourseCondition("数据通信网实验室YF315"));
//    }
}

