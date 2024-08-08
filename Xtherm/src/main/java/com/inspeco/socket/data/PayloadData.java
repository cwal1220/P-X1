package com.inspeco.socket.data;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

import com.inspeco.socket.NetUtil;

/**
 * 데이터
 */
public class PayloadData implements Serializable {

    private static final String TAG = "bobopro "+PayloadData.class.getSimpleName();

    public static final int MODE_REAL = 0;
    public static final int MODE_MAX = 1;
    public static final int MODE_AVG = 2;

    public static final int LAN_ENG = 0;
    public static final int LAN_KOR = 1;
    public static final int LAN_CHA = 2;


    ///////////////////////////////////////// 패킷

    protected byte[] wave = new byte[1024]; // Wav 데이터	Wav 데이터	                INT16	10	1024	512x2 = 1024바이트
    protected byte freq;                    // 주파수(KHz)	10~150 	                    UINT8	1034	1	KHz 단위
    protected byte[] sens = new byte[2];    // 감도(dB)	    0~300 / Sensitivity 조정값	UINT16	1035	2
    protected byte mode;                    // 모드	        실시간/최대치/평균	            UINT8	1037	1
    protected byte vol;                     // 음량	        레벨(1~47)	                UINT8	1038	1	볼륨레벨

    protected byte[] year = new byte[2];    // 년	        2018~2099	                UINT16	1039	2
    protected byte month;                   // 월	        1~12	                    UINT8	1041	1
    protected byte day;                     // 일	        1~31	                    UINT8	1042	1
    protected byte hour;                    // 시간	        0~23	                    UINT8	1043	1
    protected byte minute;                  // 분	        0~63	                    UINT8	1044	1

    protected byte[] raw_db = new byte[2]; // 모드_DB	    모드 선택에 따른 DB값	        INT16	1045	2	*1 아래 설명 참조*
    public int rev_db = 0; // 보정 DB
    protected byte[] temp = new byte[4];    // 온도	        ‘C 단위	                    FLOAT	1047	4
    protected byte[] humi = new byte[4];    // 상대 습도	    RH  %	                    FLOAT	1051	4
    protected byte lang;                    // 언어	        0=ENG,1=KOR,2=CHA	        UINT8	1055	1

    protected byte laser;                    // 레이저 거리측정	On/Off 	                    UINT8	32	1	1=ON, 0=OFF
    protected byte[] distance = new byte[4]; // 레이저 거리	단위 mm	                        UINT32	33	4

    ///////////////////////////////////////// 패킷

    private boolean isTcp = false;

    /**
     * 안드로이드에서 보내는 패킷
     * @param in_freq
     * @param in_sens
     * @param in_mode
     * @param in_vol
     * @param in_lan
     */
    public PayloadData(int in_freq, int in_sens, int in_mode, int in_vol, int in_lan ) {

        this.freq = (byte) in_freq;                         // 주파수(KHz) , UINT8 , 10~150
        this.sens = NetUtil.intToLittleByte(in_sens);// 감도(dB) , UINT8 , 0~300 / Sensitivity 조정값
        this.mode = (byte) 0x00;                         // 모드 , UINT8 , 실시간/최대치/평균
//        Log.e(logTag, "mode :" + this.mode);
        this.vol = (byte) in_vol;                          // 음량 , UINT8 , 레벨(1~47)

        Calendar cal = Calendar.getInstance();

        this.year = NetUtil.intToLittleByte(cal.get(Calendar.YEAR));     // 년도 - UINT16 - 2018~2099
        this.month = (byte) (cal.get(Calendar.MONTH)+1);                     // 월 , UINT8 , 1~12
        this.day = (byte) cal.get(Calendar.DAY_OF_MONTH);                       // 일 , UINT8 , 1~31
        this.hour = (byte) cal.get(Calendar.HOUR_OF_DAY);                      // 시간 , UINT8 , 0~23
        this.minute = (byte) cal.get(Calendar.MINUTE);                    // 분 , UINT8 , 0~59

        this.lang = (byte) in_lan;                    // 언어 , UINT8 , 0=ENG,1:KOR,2:CHA
    }

    /**
     * 안드로이드에서 TCP 패킷
     */
//    public PayloadData(int in_freq, int in_sens, int in_mode, int in_vol, int in_lan, int in_laser ) {
//        isTcp = true;
//
//        this.freq = (byte) in_freq;                         // 주파수(KHz) , UINT8 , 10~150
//        this.sens = NetUtil.intToLittleByte(in_sens);// 감도(dB) , UINT8 , 0~300 / Sensitivity 조정값
//        //Log.e(logTag, "sens:" + NetUtil.byte2LittleInteger(this.sens));
//        this.mode = (byte) in_mode;                         // 모드 , UINT8 , 실시간/최대치/평균
//        this.vol = (byte) in_vol;                          // 음량 , UINT8 , 레벨(1~47)
//
//        Calendar cal = Calendar.getInstance();
//
//        this.year = NetUtil.intToLittleByte(cal.get(Calendar.YEAR));     // 년도 - UINT16 - 2018~2099
//        this.month = (byte) (cal.get(Calendar.MONTH)+1);                     // 월 , UINT8 , 1~12
//        this.day = (byte) cal.get(Calendar.DAY_OF_MONTH);                       // 일 , UINT8 , 1~31
//        this.hour = (byte) cal.get(Calendar.HOUR_OF_DAY);                      // 시간 , UINT8 , 0~23
//        this.minute = (byte) cal.get(Calendar.MINUTE);                    // 분 , UINT8 , 0~59
//
//        this.lang = (byte) in_lan;                    // 언어 , UINT8 , 0=ENG,1:KOR,2:CHA
//
//        this.laser = (byte) in_laser;
//    }

    /**
     * 네트워크 수신한 패킷 파싱
     * @param packet
     */
    public PayloadData(byte[] packet, boolean isClient, int udp0_tcp1) {

//        isTcp = udp0_tcp1 == 1;

        int pos = 0;

        // x1은 upd 만 사용
//        if (udp0_tcp1 == 0) {
            System.arraycopy(packet, pos, wave, 0, 1024);
            pos += 1024;
//        } else {
//            System.arraycopy(packet, pos, wave, 0, 800);
//            pos += 800;
//        }

        this.freq = packet[pos++];                        // 주파수(KHz) , UINT8 , 10~150
        System.arraycopy(packet, pos, sens, 0, sens.length);                       // 감도(dB) , UINT8 , 0~300 / Sensitivity 조정값
//        Log.d("jtyoo", "payloadData sens = " + NetUtil.byte2LittleInteger(sens));
        pos += sens.length;
        this.mode = packet[pos++];                        // 모드 , UINT8 , 실시간/최대치/평균
        this.vol = packet[pos++];                        // 음량 , UINT8 , 레벨(1~47)
//        Log.d("jtyoo", "payloadData mode = " + mode);

        System.arraycopy(packet, pos, year, 0, year.length);
        pos += year.length;
//        int int_year = ByteBuffer.wrap(year, 0, 2).order(ByteOrder.LITTLE_ENDIAN).getInt();

        this.month = packet[pos++];                       // 월 , UINT8 , 1~12
        this.day = packet[pos++];                          // 일 , UINT8 , 1~31
        this.hour = packet[pos++];                        // 시간 , UINT8 , 0~23
        this.minute = packet[pos++];                      // 분 , UINT8 , 0~59

        // 클라이언트에서만 서버에서 받는 것만 정보가 있다.
        //if (isClient) {
            System.arraycopy(packet, pos, raw_db, 0, 2);
            pos += 2;

            this.rev_db = convMode_db();
//            if ((mode_db[0]&0xff) == 255) {
//                mode_db[0] = 0;
//            }
//            if ((mode_db[1]&0xff) == 255) {
//                mode_db[1] = 0;
//            }

            // 온도 `C 단위 - FLOAT 4byte
            System.arraycopy(packet, pos, temp, 0, temp.length);
            pos += temp.length;
            // 습 RH % - FLOAT 4byte
            System.arraycopy(packet, pos, humi, 0, humi.length);
            pos += humi.length;
        //}

        this.lang = packet[pos++];                    // 언어 , UINT8 , 0=ENG,1:KOR,2:CHA

//        if (udp0_tcp1 == 1) {
//
////            this.laser = packet[pos++];
//
//            if (isClient) {
//                System.arraycopy(packet, pos, distance, 0, distance.length);
//                pos += distance.length;
//            }
//        }

//        Log.e("jtyoo", "recv payload : " + packet.length + " => "  +
//                "sens = " + NetUtil.byte2LittleInteger(sens) + " , freq = " + this.freq + " , vol = " + this.vol + " , mode = " + this.mode + " , mode_db = " + NetUtil.byte2LittleInteger(mode_db) + " , temp = " + NetUtil.byteArrayToFloat(temp) + " , humi = " + NetUtil.byteArrayToFloat(humi));
    }



    /**
     * 패킷 생성하기
     * @return
     * @throws IOException
     */
//    public byte[] generateServerPacket() throws IOException {
//
//        ByteArrayOutputStream packetArray = new ByteArrayOutputStream();
//
////        if (isServer) {
////            packetArray.write(wave);
////        }
//
//        packetArray.write(freq);
//        packetArray.write(sens);
//        packetArray.write(mode);
//        packetArray.write(vol);
//
//        packetArray.write(year);
//        packetArray.write(month);
//        packetArray.write(day);
//        packetArray.write(hour);
//        packetArray.write(minute);
//
//        packetArray.write(mode_db);
//        packetArray.write(temp);
//        packetArray.write(humi);
//
//        packetArray.write(lang);
//
//        return packetArray.toByteArray();
//    }

    /**
     * 패킷 생성하기
     * @return
     * @throws IOException
     */
    public byte[] generateClientPacket() throws IOException {

        ByteArrayOutputStream packetArray = new ByteArrayOutputStream();

        packetArray.write(freq);
        packetArray.write(sens);
        packetArray.write(mode);
        packetArray.write(vol);

        packetArray.write(year);
        packetArray.write(month);
        packetArray.write(day);
        packetArray.write(hour);
        packetArray.write(minute);

        packetArray.write(lang);

        if (isTcp) {
            packetArray.write(laser);
        }

        return packetArray.toByteArray();
    }


    public byte[] getWave() {

            return wave;
    }

    public int getFreq() {
        return freq & 0xFF;
    }

    public void setFreq(int freq) {
        this.freq = (byte) freq;
    }

    public int getSens() {
        return NetUtil.byte2LittleInteger(sens);
    }

    public int getMode() {
        return mode & 0xFF;
    }

    public void setMode(int mode) {
        this.mode = (byte) mode;
    }

    public int getVol() {
        return vol & 0xFF;
    }

    public int getYear() {
        return NetUtil.byte2LittleInteger(year);
    }

    public int getMonth() {
        return month & 0xFF;
    }

    public int getDay() {
        return day & 0xFF;
    }

    public int getHour() {
        return hour & 0xFF;
    }

    public int getMinute() {
        return minute & 0xFF;
    }

    public int convMode_db() {
        int db = NetUtil.byte2LittleShort(raw_db);
        //Log.e(TAG, "payload recv db "+Integer.toString(db));
//        if (db>32767) {
//            db =  -(65536-db);
//        }
        if (db==-1) db = 0;
        //if (db>65530) db=41000;
        if (db>4100) db=4100;
        if (db<0) db = db * 10;
        return db;
    }

    public float getTemp() {
        return NetUtil.byteArrayToFloat(temp);
    }

    public float getHumi() {
        return NetUtil.byteArrayToFloat(humi);
    }

    public int getLang() {
        return lang & 0xFF;
    }

    public int getLaser() {
        return laser & 0xFF;
    }

    public void setLaser(int laser) {
        this.laser = (byte) laser;
    }

    public float getDistance() {
        return NetUtil.byte4Integer(distance); // 단위가 mm 이므로 m 으로 변환한다
    }

    public void setDistance(byte[] distance) {
        this.distance = distance;
    }

    public String getDBStr() {

        int modeDot = (rev_db%100) / 10;
        if (modeDot<0) {
            modeDot = - ( modeDot );
        }

//        Log.e(logTag, String.format("%d.%d", modeVal/100, modeVal%100) + ", mode_db = " + NetUtil.byte2LittleInteger(mode_db));
        String dbStr = String.format(Locale.getDefault(), "%d.%d", rev_db/100, modeDot );
        if (rev_db == 0) {
            // 65535 값 일 경우
            dbStr = "--.-";
        }
        return dbStr;
    }

//    public int getDBLevel() {
//        int level = getMode_db()/100;
//        level += 10;
//        if (level < -10) {
//            level = -10;
//        } else if (level > 40) {
//            level = 40;
//        }
//        return level;
//    }
}
