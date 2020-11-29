package wxd.qst.mall.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wuxd on 2019/12/13.
 */
public class DateUtil {

    public static boolean isBetween24H(Date startTime) {
        long now=System.currentTimeMillis();

        return (startTime.getTime()-now<= 24*60*60*1000?true:false);
    }

}
