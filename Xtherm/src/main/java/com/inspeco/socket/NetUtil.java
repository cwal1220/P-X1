package com.inspeco.socket;


import android.text.TextUtils;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class NetUtil {
    private static final String TAG = NetUtil.class.getSimpleName();
    /**
     * Byte Array를 화면 표시를 위한 Hex String으로 표시하는 코드
     * 중간에 공백이 있음
     * @param buffer
     * @param length
     * @return
     */
    public static String byteToHexDisplayString(byte[] buffer, int length) {
        String str = "";
        for (int c = 0; c < length; c++) {
            str += String.format("%02X ", (int) (buffer[c] & 0xff));
        }

        return str;
    }

    public static String byteToHexDisplayString(byte[] buffer, int start, int length) {
        String str = "";
        for (int c = start; c < start+length; c++) {
            str += String.format("%02X ", (int) (buffer[c] & 0xff));
        }

        return str;
    }

    public static String byteToBinaryString(byte[] buffer, int lenght) {
        String str = "";
        for (int c = 0; c < lenght; c++) {
            str += Integer.toBinaryString(buffer[c]) + " ";
        }

        return str;
    }

    //********************************************************************************
    //  int to byte
    //********************************************************************************


    /**
     * int를 2byte로 변환하
     * @param input
     * @return
     */
    public static byte[] intToLittleByte(int input) {
        byte[] result = new byte[2];

        result[0] = (byte) (input & 0xFF);
        result[1] = (byte) ((input >> 8) & 0xFF);
        return result;
    }

    public static byte[] intToBigByte(int input) {
        byte[] result = new byte[2];

        result[0] = (byte) ((input >> 8) & 0xFF);
        result[1] = (byte) (input & 0xFF);
        return result;
    }


    /**
     * 2 byte를 int로 변화하기
     * @param buf
     * @return
     */
    public static int byte2LittleInteger(byte[] buf) {
        int result = (buf[0] & 0xFF) + ((buf[1] & 0xFF) << 8);
        return result;
    }

    public static short byte2LittleShort(byte[] buf) {
        short result = (short)((buf[0] & 0xFF) + ((buf[1] & 0xFF) << 8));
        return result;
    }


    public static int byte2BigInteger(byte[] buf) {
        int result = ((buf[0] & 0xFF) << 8) + (buf[1] & 0xFF);
        return result;
    }


    public static byte[] floatToByte(float input) {
        byte[] b = ByteBuffer.allocate(4).putFloat(input).array();  //[12, 24, 19, 17]
        return  b;
    }

    public static byte[] floatToBytes_LE(float input, int offset  ) {
        byte[] output = new byte[4];
        int bits = Float.floatToIntBits(input);
        for(int i = 0; i < 4; i++) {
            output[i+offset] = (byte)( (bits >> ( i * 8) ) & 0xff);
        }
        return output;
    }

    public static float byteArrayToFloat(byte[] bytes) {
        int value =  byte4Integer(bytes);
        return Float.intBitsToFloat(value);
    }

    public static int byte4Integer(byte[] buf) {
        int MASK = 0xFF;
        int result = 0;
        result = buf[0] & MASK;
        result += ((buf[1] & MASK) << 8);
        result += ((buf[2] & MASK) << 16);
        result += ((buf[3] & MASK) << 24);
        return result;
    }

    public static byte[] longToBytes_LE(long input ) {
        byte[] output = new byte[8];
        for(int cnt = 0;  cnt<8; cnt++){
            output[cnt] = (byte) (input   % (0xff + 1));
            input   = input   >> 8;
        }
        return Arrays.copyOfRange(output, 0, 4);
    }




    public static String getIp() {
        String localAddress = "";
        List<String> addrs = getLocalAddresses();
        for (String string : addrs) {
            Log.e(TAG, "my ip : " + string);

            if (string.equals("127.0.0.1")) {
                continue;
            }
            if (TextUtils.isEmpty(localAddress) && string.startsWith("192.168")) {
                localAddress = string;
                break;
            }
            localAddress = string;
            break;
        }
        return localAddress;
    }

    public static List<String> getLocalAddresses(){
        List<String> addrs = new ArrayList<String>();
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while(nis.hasMoreElements()){
                NetworkInterface ni = nis.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while(addresses.hasMoreElements()){
                    InetAddress address = addresses.nextElement();
                    if( address instanceof Inet4Address) {	// IPv6 무시
                        addrs.add(address.getHostAddress());
                        byte[] macBytes = ni.getHardwareAddress();
                        if (macBytes != null) {
                            StringBuilder res1 = new StringBuilder();
                            for (byte b : macBytes) {
                                res1.append(String.format("%02X:",b));
                            }

                            if (res1.length() > 0) {
                                res1.deleteCharAt(res1.length() - 1);
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return addrs;
    }
}
