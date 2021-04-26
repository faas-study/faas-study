package fudan.se.util;

import java.util.HashMap;
import java.util.Map;


public class StationInfo {
    public static final Map<String, String> stationsInfo = new HashMap<>();

    {
        stationsInfo.put("Shang Hai", "shanghai");
        stationsInfo.put("Shang Hai Hong Qiao", "shanghaihongqiao");
        stationsInfo.put("Tai Yuan", "taiyuan");
        stationsInfo.put("Bei Jing", "beijing");
        stationsInfo.put("Nan Jing", "nanjing");
        stationsInfo.put("Shi Jia Zhuang", "shijiazhuang");
        stationsInfo.put("Xu Zhou", "xuzhou");
        stationsInfo.put("Ji Nan", "jinan");
        stationsInfo.put("Hang Zhou", "hangzhou");
        stationsInfo.put("Jia Xing Nan", "jiaxingnan");
        stationsInfo.put("Zhen Jiang", "zhenjiang");
        stationsInfo.put("Wu Xi", "wuxi");
        stationsInfo.put("Su Zhou", "suzhou");
    }
}
