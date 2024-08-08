package com.inspeco.socket.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.inspeco.socket.NetUtil;

/**
 * Socket Header Base Class
 * Created by BekSung on 2018. 4. 18..
 */

public class SockBase {

    private static final String TAG = SockBase.class.getSimpleName();

    private ByteArrayOutputStream packetArray = new ByteArrayOutputStream();

    protected byte[] sof = new byte[2];     //    SOF	    Start Of Frame 	UINT16	0	2	0xF2F2, 고정값
    protected byte[] total = new byte[2];   //    프레임크기	프레임의 전체 크기	UINT16	2	2	(SOF~데이터)구간의 전체 길이
    protected byte seq;                     //    Seq_Num	Sequence number	UINT8	4	1	0->1->… ->255->0->1
    protected byte waveid;                  //    장치 ID	P1  WAVE 장치 ID	    UINT8	5	1	P1 WAVE 장치 ID, default=0
    protected byte sync;                    //    SYNC플래그	동기화 요청 구분	UINT8	6	1	YES=0x01 / NO=0
    protected byte opcode;                  //    OP-CODE	명령 코드	        UINT8	7	1	Default = 0x00

    protected byte[] len = new byte[2];     //    데이터 크기	데이터 필드의 크기	UINT16	8	2	전송 데이터 길이
    protected byte[] payload;               //    데이터	    전송 데이터	    -	    10	N	OP CODE 코드별 전송 데이터


    // 순차적으로 증가할 sequence - 초기값이 255이어야 초기값이 0
    private static int send_seq = 255;

    private int getSend_seq() {
        send_seq++;
        if (send_seq>255) {
            send_seq = 0;
        }
        return send_seq;
    }

    public SockBase(boolean isSync) {
        // SOF
        sof[0] = (byte) 0xF2;
        sof[1] = (byte) 0xF2;
        // LEN
        total[0] = 0x00; //Len
        total[1] = 0x00; //Len
        // sequence
        seq = (byte)getSend_seq();
        // Wave Id
        waveid = 0x00;

        if (isSync) {
            // sync
            sync=(byte)0x01;
            // OP CODE
            opcode = 0x01;
        } else {
            // sync
            sync=(byte)0x00;
            // OP CODE
            opcode = 0x00;
        }

        // LEN
        len[0] = 0x00; //Len
        len[1] = 0x00; //Len
    }

    /**
     * 서버에서 받은 socket 정보 파싱
     * @param packet
     */
    public SockBase(byte[] packet) {
        int pos = 0;
        // SOF
        System.arraycopy(packet, pos, sof, 0, sof.length);
        pos += sof.length;


        // LEN
        System.arraycopy(packet, pos, total, 0, total.length);
        pos += total.length;
        int total_len = NetUtil.byte2LittleInteger(total);
        //Log.e(logTag, "total length : " + total_len);


        // sequence
        this.seq = packet[pos++];
        // Wave Id
        waveid = packet[pos++];
        // sync
        sync = packet[pos++];
        // OP CODE
        opcode = packet[pos++];
        // LEN
        System.arraycopy(packet, pos, len, 0, len.length);
        pos += len.length;

        int payload_len = NetUtil.byte2LittleInteger(len);
        payload = new byte[payload_len];
        System.arraycopy(packet, pos, payload, 0, payload_len);

    }

    public void generatePacket(byte[] packet, boolean isTcp, boolean isSync) throws IOException {

        packetArray.write(sof);
        packetArray.write(total);
        packetArray.write(seq);
        packetArray.write(waveid);

        if (isTcp) {
            if (isSync) {
                packetArray.write((byte) 1);
            } else {
                packetArray.write((byte) 0);
            }
        } else {
            packetArray.write(sync);
        }
        packetArray.write(opcode);

        packetArray.write(NetUtil.intToLittleByte(packet.length));
        packetArray.write(packet);

    }

    /**
     * 전송을 위한 packet Array 요청 => 사이즈
     * @return
     * @throws IOException
     */
    public byte[] getServerPacket(boolean isTcp, boolean isSync) throws IOException {

        byte[] packet_array = packetArray.toByteArray();

        int packet_size = (short) packetArray.size();
        packet_array[2] = (byte) (packet_size & 0xFF);
        packet_array[3] = (byte) ((packet_size >> 8) & 0xFF);

//        Log.e(TAG,  "Send Packet : " + packetArray.size() + " => " + NetUtil.byteToHexDisplayString(packet_array, packetArray.size()));

        return packet_array;
    }


    /**
     * 전송을 위한 packet Array 요청 => 사이즈
     * @return
     * @throws IOException
     */
    public byte[] getClientPacket(boolean isTcp, boolean isSync) throws IOException {

        byte[] packet_array = packetArray.toByteArray();

        int packet_size = (short) packetArray.size();
        packet_array[2] = (byte) (packet_size & 0xFF);
        packet_array[3] = (byte) ((packet_size >> 8) & 0xFF);

//        Log.e(TAG,  "Send Packet : " + packetArray.size() + " => " + NetUtil.byteToHexDisplayString(packet_array, packetArray.size()));

        return packet_array;
    }


    public int getTotal() {
        return NetUtil.byte2LittleInteger(total);
    }

    public int getSeq() {
        return seq & 0xFF;
    }

    public int getLen() {
        return NetUtil.byte2LittleInteger(len);
    }
}
