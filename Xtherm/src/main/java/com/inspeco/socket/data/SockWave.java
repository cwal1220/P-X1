package com.inspeco.socket.data;

import java.io.IOException;

/**
 * Wave 데이터 주고 받기 위한 데이터
 */
public class SockWave extends SockBase {

    private  static final String TAG = SockWave.class.getSimpleName();

    public PayloadData mWave;

//    /**
//     * 서버 전송용 패킷 구성하기
//     * @param freq
//     * @param sense
//     * @param mode
//     * @param vol
//     * @param mode_db
//     * @param temp
//     * @param humi
//     * @param language
//     */
//    public SockWave(int freq, int sense, int mode, int vol, int mode_db, float temp, float humi, int language) {
//        super(false);
//        mWave = new PayloadData(new byte[1024], freq, sense, mode, vol, mode_db, temp, humi, language);
//    }


    /**
     * 안드로이드에서 데이터 전송을 위한 패킷 구성
     * @param freq
     * @param sense
     * @param mode
     * @param vol
     * @param language
     */
    public SockWave(int freq, int sense, int mode, int vol, int language) {
        super(true);
        mWave = new PayloadData(freq, sense, mode, vol, language);
    }

    /*
     * 안드로이드에서 TCP에서 전송하는 패킷 구성
     */
//    public SockWave(int freq, int sense, int mode, int vol, int language, int laser) {
//        super(true);
//        mWave = new PayloadData(freq, sense, mode, vol, language, laser);
//    }

    public SockWave(byte[] packet, boolean isClient, int udp0_tcp1) {
        super(packet);

        mWave = new PayloadData(payload, isClient, udp0_tcp1);

    }

//    @Override
//    public byte[] getServerPacket(boolean isTcp, boolean isSync) throws IOException {
//        super.generatePacket(mWave.generateServerPacket(), isTcp, isSync);
//        return super.getServerPacket(isTcp, isSync);
//    }

    @Override
    public byte[] getClientPacket(boolean isTcp, boolean isSync) throws IOException {
        super.generatePacket(mWave.generateClientPacket(), isTcp, isSync);
        return super.getClientPacket(isTcp, isSync);
    }


    //********************************************************************************
    //  Getter / Setter Functions
    //********************************************************************************


    public PayloadData getWave() {
        return mWave;
    }

    public void setWave(PayloadData wave) {
        this.mWave = wave;
    }
}
