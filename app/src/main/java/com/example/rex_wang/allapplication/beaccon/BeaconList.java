package com.example.rex_wang.allapplication.beaccon;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Rex_Wang on 2017/4/24.
 */

public class BeaconList {
    public static List<BeaconFormat> mList = new ArrayList<>();
    private static String result;

    public static String decode(BluetoothDevice bluetoothDevice, int rssi, byte[] scanBytes) {

        if (bluetoothDevice == null || bluetoothDevice.getAddress() == null) {
            result = "搜尋不到適合的裝置...";
            return result;
        }

        final long now = System.currentTimeMillis();

        Boolean contains = false;
        if (mList != null) {
            for (BeaconFormat device : mList) {
                // 如果設備跟 List設備一樣時
                if (bluetoothDevice.getAddress().equals(device.getAddress())) {
                    contains = true;
//                    // update
//                    device.setRssi(rssi);
//                    device.setLastUpdatedMs(now);
//                    device.setScanRecord(scanBytes);
                    return "success";
                }
            }
        }
        if (!contains) {
            mList.add(new BeaconFormat(bluetoothDevice, rssi, scanBytes, now));
        }


        // 排序
        Collections.sort(mList, new Comparator<BeaconFormat>() {
            @Override
            public int compare(BeaconFormat lhs, BeaconFormat rhs) {
                if (lhs.getRssi() == 0) {
                    return 1;
                } else if (rhs.getRssi() == 0) {
                    return -1;
                }
                if (lhs.getRssi() > rhs.getRssi()) {
                    return -1;
                } else if (lhs.getRssi() < rhs.getRssi()) {
                    return 1;
                }
                return 0;
            }
        });

        int totalCount = 0;
        int iBeaconCount = 0;
        if (mList != null) {
            totalCount = mList.size();
            for (BeaconFormat device : mList) {
                if (device.isIBeacon()) {
                    iBeaconCount++;
                }
            }
        }
        return "有 " + totalCount + " 個 , i 有 " + iBeaconCount + " 個";
    }

    public static void cleanList() {
        mList.clear();
    }
}
