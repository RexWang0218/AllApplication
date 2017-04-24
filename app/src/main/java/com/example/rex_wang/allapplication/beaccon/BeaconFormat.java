package com.example.rex_wang.allapplication.beaccon;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.example.rex_wang.allapplication.utility.FC;

/**
 * Created by Rex_Wang on 2017/4/24.
 */

public class BeaconFormat {
    private final String UNKNOWN = "Unknown";
    private String beacon;
    private String address;
    private int rssi;
    private String LastUpdatedMs;
    private byte[] mScanRecord;
    private int txPower;
    private int major;
    private int minor;
    private String UUID;
    private boolean IBeacon;
    private double Distance;

    public BeaconFormat(BluetoothDevice bluetoothDevice, int Rssi, byte[] scanBytes, long now) {
        if (bluetoothDevice == null) {
            throw new IllegalArgumentException("BluetoothDevice is null");
        }
        mScanRecord = scanBytes;

        if (bluetoothDevice.getName() == null) {
            beacon = UNKNOWN;
        } else {
            beacon = bluetoothDevice.getName();
        }

        address = bluetoothDevice.getAddress();
        rssi = Rssi;
        LastUpdatedMs = FC.LongToTime(now);
        checkIBeacon();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setScanRecord(byte[] scanRecord) {
        mScanRecord = scanRecord;
        checkIBeacon();
    }

    public void setLastUpdatedMs(long lastUpdatedMs) {
        LastUpdatedMs = FC.LongToTime(lastUpdatedMs);
    }

    public void setRssi(int Rssi) {
        rssi = Rssi;
    }


    public String getName() {
        return beacon;
    }

    public String getAddress() {
        return address;
    }

    public int getRssi() {
        return rssi;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getTxPower() {
        return txPower;
    }

    public String getUUID() {
        return UUID;
    }

    public String getLastUpdatedMs() {
        return LastUpdatedMs;
    }

    public boolean isIBeacon() {
        return IBeacon;
    }

    public String getDistance() {
        String aaa = FC.DubleToString(Distance);
        return aaa;
    }
    public Double getDistanceDouble(){
        return Distance;
    }

    private void checkIBeacon() {
        if (mScanRecord != null) {
            int startByte = 0;
            boolean patternFound = false;

            while (startByte <= 5) {

                //((int)mScanRecord[startByte] & 0xff) == 0x4c &&
                //((int)mScanRecord[startByte+1] & 0xff) == 0x00 &&

                if (((int) mScanRecord[startByte + 2] & 0xff) == 0x02 &&
                        ((int) mScanRecord[startByte + 3] & 0xff) == 0x15) {
                    // yes!  This is an iBeacon
                    patternFound = true;
                    IBeacon = true;
                    break;

                }
                startByte++;
            }

            if (patternFound) {
                major = (mScanRecord[startByte + 20] & 0xff) * 0x100 + (mScanRecord[startByte + 21] & 0xff);
                minor = (mScanRecord[startByte + 22] & 0xff) * 0x100 + (mScanRecord[startByte + 23] & 0xff);
                txPower = (int) mScanRecord[startByte + 24]; // this one is signed


                // AirLocate:
                // 02 01 1a 1a ff 4c 00 02 15  # Apple's fixed iBeacon advertising prefix
                // e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 # iBeacon profile uuid
                // 00 00 # major
                // 00 00 # minor
                // c5 # The 2's complement of the calibrated Tx Power
                // Estimote:
                // 02 01 1a 11 07 2d 24 bf 16
                // 394b31ba3f486415ab376e5c0f09457374696d6f7465426561636f6e00000000000000000000000000000000000000000000000000

                byte[] proximityUuidBytes = new byte[16];
                System.arraycopy(mScanRecord, startByte + 4, proximityUuidBytes, 0, 16);
                String hexString = bytesToHex(proximityUuidBytes);

                StringBuilder sb = new StringBuilder();
                sb.append(hexString.substring(0, 8));
                sb.append("-");
                sb.append(hexString.substring(8, 12));
                sb.append("-");
                sb.append(hexString.substring(12, 16));
                sb.append("-");
                sb.append(hexString.substring(16, 20));
                sb.append("-");
                sb.append(hexString.substring(20, 32));
                UUID = sb.toString();
                Distance = calculateAccuracy(txPower, rssi);
            } else {
                major = 0;
                minor = 0;
                UUID = "00000000-0000-0000-0000-000000000000";
                txPower = -0;
                Distance = 0;

                // This is not an iBeacon
                Log.d("----------", "This is not an iBeacon advertisment (no 4c000215 seen in bytes 2-5).  The bytes I see are: " + bytesToHex(mScanRecord));
            }
        }
    }

    final private char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0;
        }

        double ratio = rssi * 1.0 / txPower;

        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }
}
