package com.inspeco.socket.udp;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;


/**
 * UDP Protocol Broadcast for UMasterInfo-E Search
 */
public class UdpSocket {

    private String TAG = "UdpSocket bobopro";

    public static final int CMD_MY_IP = 0;        // 자신의 아이피 업데이트
    public static final int CMD_RECV_DATA = 2;    // 데이터 수신
    public static final int CMD_TIME_OUT = 3;     // 네트워크 타임아웃
    public static final int CMD_WAITING = 4;     // 연결 대기중

    private String dest_addr = "";
    private int dest_port = -1;

    private DatagramSocket socket;  //  소켓

    private long lastLogTime = 0;
    private long lastPlayTime = 0;
    private boolean running = true; // 스레드 Running
    private Queue<byte[]> queue = new LinkedList<>();
    private byte[] lastBuf = new byte[1200];
    private int lastBufCnt = 0;

    private Handler mHandler;       // 수신 메시지 회신을 위한 핸들러



    public UdpSocket(Handler hd, String ip, int port) {
        this.mHandler = hd;
        this.dest_addr = ip;
        this.dest_port = port;
        this.lastLogTime = System.currentTimeMillis();
        this.lastPlayTime = System.currentTimeMillis();
        queue.clear();
        try {
            Log.e(TAG, "UDP Datagram");
            socket = new DatagramSocket(port);
            //socket = new DatagramSocket(port,  InetAddress.getByName(ip));
            Log.e(TAG, "UDP setBroadcast");
            socket.setBroadcast(true);
        } catch (Exception e) {
            Log.e(TAG, "UDP Connection failed. " + e.getMessage());
        }

        Log.e(TAG, "UDP listen");
        listen();
        bufProcThread();

    }

    public void close() {
        Log.e(TAG, "UDP Close ");
        running = false;
        socket.close();
    }

//    private void receiveMsg(byte[] msg) {
//
//        long timeDiff = System.currentTimeMillis() - lastPlayTime;
//        if (timeDiff>3) {
//            Message message = Message.obtain(mHandler);
//            message.what = CMD_RECV_DATA;
//            message.obj = msg;
//            message.arg1 = 0;
//            message.sendToTarget();
//            lastPlayTime = System.currentTimeMillis();
//        } else {
//            //Log.e(TAG, Long.toString(timeDiff));
//        }
//    }


    private void listen() {
        new Thread() {
            public void run() {
                while (running) {
                    try {
                        byte[] buf = new byte[1200];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.setSoTimeout(3500);
                        socket.receive(packet);
                        //lastRecvTime = System.currentTimeMillis();

                        //receiveMsg(buf);
                        queue.add(buf);
                        System.arraycopy(buf, 0, lastBuf,0,1200);
                        lastBufCnt = 2;

                        //Log.e(TAG, Integer.toString(packet.getLength()));
                    } catch (SocketTimeoutException e) {
                        Log.e(TAG,"Timeout Exception UDP Connection");
                        running = false;
                        socket.close();
                        mHandler.sendEmptyMessage(CMD_TIME_OUT);
                    } catch (Exception e) {
                        Log.e(TAG, "UDP Exception ");
                        System.err.println(e.getMessage());
                        running = false;
                    }
                }
                Log.e(TAG, "UDP socket.closed~~!!");
            }
        }.start();
    }


    private void bufProcThread() {
        new Thread() {
            public void run() {
                while (running) {
                    try {
                        long timeDiff = System.currentTimeMillis() - lastPlayTime;
                        if (timeDiff>34) {

                            if (queue.size()>0) {
                                lastPlayTime = System.currentTimeMillis();
                                lastLogTime = lastPlayTime;
                                Message message = Message.obtain(mHandler);
                                message.what = CMD_RECV_DATA;
                                //message.obj = msg;
                                message.obj = queue.poll();
                                message.arg1 = 0;
                                message.sendToTarget();
                                Thread.sleep(22);
 //                               if (queue.size()>2) {
                                //queue.remove();
                                if (queue.size()>2) {
                                    queue.remove();
                                    if (queue.size()>2) {
                                        queue.remove();
                                    }
                                }

//                                }
                            } else {
                                if  ( (lastBufCnt>0) && (timeDiff>40) ){
                                    lastBufCnt--;
                                    lastPlayTime = System.currentTimeMillis();
                                    Message message = Message.obtain(mHandler);
                                    message.what = CMD_RECV_DATA;
                                    //message.obj = msg;
                                    message.obj = lastBuf;
                                    message.arg1 = 0;
                                    message.sendToTarget();
                                    Thread.sleep(22);
                                }
//                                else {
//                                    long logDiff = System.currentTimeMillis() - lastLogTime;
//                                    if (logDiff>100) {
//                                        lastLogTime = System.currentTimeMillis();
//                                        //Log.e(TAG, "Recv Udp Diff "+Long.toString(logDiff));
//                                    }
//                                    Thread.sleep(1);
//                                }
                            }
                            Thread.sleep(1);
                        } else {
                            Thread.sleep(1);
                        }


                        //receiveMsg(buf);
                        //Log.e(TAG, Integer.toString(packet.getLength()));

                    } catch (Exception e) {
                        Log.e(TAG, "Udp Proc Exception "+e.getMessage());
                        //System.err.println(e.getMessage());
                    }
                }
            }
        }.start();
    }

    /**
     * @param message
     */
    public boolean sendMessageTarget(String message) {
        if (TextUtils.isEmpty(dest_addr) || dest_port == -1) {
            return false;
        }

        byte[] buf = message.getBytes();
        return  sendMessageTarget(dest_addr, dest_port, buf);
    }

    public boolean sendMessageTarget(byte[] buf) {
        if (TextUtils.isEmpty(dest_addr) || dest_port == -1) {
            return false;
        }

        return  sendMessageTarget(dest_addr, dest_port, buf);
    }

    public boolean sendMessageTarget(String address, int port, String message) {
        byte[] buf = message.getBytes();
        return  sendMessageTarget(address, port, buf);
    }

    public boolean sendMessageTarget(String address, int port, byte[] buf) {

        try {
            InetAddress local = InetAddress.getByName(address);
            DatagramPacket p = new DatagramPacket(buf, buf.length, local, port);
            socket.send(p);
        } catch (Exception e) {
//            e.printStackTrace();
//            System.err.println("Sending failed. " + e.getMessage());
            Log.e(TAG, "Sending failed. " + e.getMessage());
            return false;
        }
        return true;
    }

}