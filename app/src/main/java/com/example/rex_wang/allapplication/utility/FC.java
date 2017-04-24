package com.example.rex_wang.allapplication.utility;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Rex_Wang on 2017/4/24.
 */

public class FC {
    public static String LongToTime(long time) {
        SimpleDateFormat formatTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return formatTime.format(time);
    }

    public static String DubleToString(double distance) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);						// 若小數點超過四位，則第五位~四捨五入
        nf.setMinimumFractionDigits(2);
        return nf.format(distance);
    }
}
