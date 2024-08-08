package com.inspeco.data;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;
import android.util.Log;

import com.inspeco.X1.XTerm.ByteUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class WaveRecorder {
    /**
     * UDP로 붙었을 때는 16,000, TCP로 붙을 때는 4,000
     * FIXME 조건에 따라 수정해야 함
     */
    private int RECORDER_BPP = 16;
    private int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_8BIT;
    private int bufferSize = 0;

    public String saveFileName;

    public WaveRecorder(boolean isUdp, String path, int sample_rate) {
        // 입력 조건에 따라 변경해야 함
        RECORDER_SAMPLERATE = sample_rate;

        if (isUdp) {
            RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
            RECORDER_BPP = 16;
        } else {
            RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
            RECORDER_BPP = 16;
        }

        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING) * 3;

        startRecord();
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File rootFolder = new File(filepath, Consts.ROOT_FOLDER);

        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }

        File audioFolder = new File(rootFolder.getPath(), Consts.AUDIO_RECORDER_FOLDER);
        if (!audioFolder.exists()) {
            audioFolder.mkdirs();
        }

        File tempFile = new File(filepath, saveFileName);

        if (tempFile.exists())
            tempFile.delete();

        return (audioFolder.getAbsolutePath() + "/" + saveFileName);
    }

    public String getTempFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File rootFolder = new File(filepath, Consts.ROOT_FOLDER);
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }

        File audioFolder = new File(rootFolder.getPath(), Consts.AUDIO_RECORDER_FOLDER);
        if (!audioFolder.exists()) {
            audioFolder.mkdirs();
        }

//        File tempFile = new File(filepath, Consts.AUDIO_RECORDER_TEMP_FILE);

//        if (tempFile.exists())
//            tempFile.delete();

        return (audioFolder.getAbsolutePath() + "/" + Consts.AUDIO_RECORDER_TEMP_FILE);
    }


    FileOutputStream outs = null;
    private void startRecord( ) {
        String filename = getTempFilename();

        try {
            //Log.v("bobopro", "start Temp 1 :"+filename);
            outs = new FileOutputStream(filename);
            //Log.v("bobopro", "start Temp 2 :"+filename);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void writeAudioDataToFile(byte[] data, int len ) {
        if (null != outs) {
            try {
                //outs.write(data);
                outs.write(data, 0, len);
                //Log.v("bobopro", "write : "+Integer.toString(len));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

//
//    public void writeAudioDataToFile4to8(byte[] data, int len ) {
//        if (null != outs) {
//            try {
//
//                byte[] mSamples = new byte[len * 2];
//                int j = 0;
//                for (int i = 0; i < len - 1; i++) {
//
//                    byte iVal = data[i];
//                    int nValue = iVal;
//                    if (iVal < 0)
//                        nValue = (int) iVal + 256;
//
////                    byte iVal2 = data[i + 1];
////                    int nValue2 = iVal2;
////                    if (iVal2 < 0)
////                        nValue2 = (int) iVal2 + 256;
////
////                    byte iDiff = (byte) (nValue + ((nValue - nValue2) / 2));
//                    mSamples[j++] = iVal;
//                    mSamples[j++] = iVal;
////                    if (i==5) {
////                        Log.i("chkim", Integer.toString(iVal)+" "+Integer.toString(iDiff)
////                                + " "+Integer.toString( data[i+1]));
////                    }
//
//                }
//                byte iVal = data[len - 1];
//                mSamples[j++] = iVal;
//                mSamples[j++] = iVal;
//
//                outs.write(mSamples, 0, len * 2);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }



//    public void writeAudioDataToFile8to16bit(byte[] data, int len ) {
//        if (null != outs) {
//            try {
//
//                byte[] mSamples = new byte[len * 2];
//                int j = 0;
//                for (int i = 0; i < len - 1; i++) {
//
//                    byte iVal = data[i];
//                    mSamples[j++] = 0;
//                    mSamples[j++] = (byte)(iVal - 0x80);
//
//                }
//                byte iVal = data[len - 1];
//                mSamples[j++] = 0;
//                mSamples[j++] = (byte)(iVal - 0x80);
//
//                outs.write(mSamples, 0, len * 2);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public void writeAudioDataToFile8to16bit4x(byte[] data, int len ) {
        if (null != outs) {
            try {

                byte[] mSamples = new byte[len * 8];
                int j = 0;
                for (int i = 0; i < len - 1; i++) {

                    byte iVal = data[i];
                    mSamples[j++] = 0;
                    mSamples[j++] = (byte)(iVal - 0x80);
                    mSamples[j++] = 0;
                    mSamples[j++] = (byte)(iVal - 0x80);
                    mSamples[j++] = 0;
                    mSamples[j++] = (byte)(iVal - 0x80);
                    mSamples[j++] = 0;
                    mSamples[j++] = (byte)(iVal - 0x80);

                }
                byte iVal = data[len - 1];
                mSamples[j++] = 0;
                mSamples[j++] = (byte)(iVal - 0x80);
                mSamples[j++] = 0;
                mSamples[j++] = (byte)(iVal - 0x80);
                mSamples[j++] = 0;
                mSamples[j++] = (byte)(iVal - 0x80);
                mSamples[j++] = 0;
                mSamples[j++] = (byte)(iVal - 0x80);

                outs.write(mSamples, 0, len * 8);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
//
//
//    public void writeAudioDataToFile4to16(byte[] data, int len ) {
//        if (null != outs) {
//            try {
//
//                byte[] mSamples = new byte[len*4];
//                int j = 0;
//                for (int i = 0; i < len-1; i++) {
//
//                    byte iVal = data[i];
//                    int nValue = iVal;
//                    if (iVal < 0)
//                        nValue = (int)iVal + 256;
//
//                    byte iVal2 = data[i+1];
//                    int nValue2 = iVal2;
//                    if (iVal2 < 0)
//                        nValue2 = (int)iVal2 + 256;
//
//                    byte iDiff = (byte)(nValue + ( (nValue - nValue2) / 2));
//                    mSamples[j++] = iVal;
//                    mSamples[j++] = iVal;
//
//                    mSamples[j++] = iVal;
//                    mSamples[j++] = iVal;
//
//                }
//                byte iVal = data[len-1];
//                mSamples[j++] = iVal;
//                mSamples[j++] = iVal;
//                mSamples[j++] = iVal;
//                mSamples[j++] = iVal;
//                outs.write(mSamples, 0, len * 4);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//




    public void stopRecording(String dis, String temp, String hum, String realdB) {

        if (null == outs) {
            return;
        }

        try {
            Log.d("bobopro", "stopRecording 1");
            outs.close();
            Log.d("bobopro", "stopRecording 2");
        } catch (IOException e) {
            e.printStackTrace();
        }

        copyWaveFile(getTempFilename(), getFilename(), dis, temp, hum, realdB);
        deleteTempFile();
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    private void copyWaveFile(String inFilename, String outFilename, String dis, String temp, String hum, String realdB) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen;
        long totalDataLen;


        byte[] data = new byte[bufferSize];

        try {
            Log.d("bobopro", "Copy Wave File 1: "+inFilename+", "+outFilename);
            in = new FileInputStream(inFilename);
            //Log.d("bobopro", "Copy Wave File 2: "+inFilename+", "+outFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;  // Header 44에서 8Byte

            // Header Size : 44;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    RECORDER_SAMPLERATE);

            while (in.read(data) != -1) {
                out.write(data);
            }


            byte[] buffer = new byte[4];
            P1 p1 = P1.getInstance();

            // -38
            ByteUtil.putFloat(buffer, p1.fLati, 0);
            out.write(buffer);

            //-34
            ByteUtil.putFloat(buffer, p1.fLongi, 0);
            out.write(buffer);

            // 16+14 : 30;
            // 거리 정보 저장 ( 8자리, DIS00.00 )
            byte[] d1 = ("DIS" + dis).getBytes();
            out.write(d1);

            // 온도 정보 저장 ( 7자리, TEM00.0 )
            byte[] t1 = ("TEM" + temp).getBytes();
            out.write(t1);

            // 습도 정보 저장 ( 7자리, HUM00.0 )
            byte[] h1 = ("HUM" + hum).getBytes();
            out.write(h1);

            // real dB 정보 저장 ( 8자리, RDB00.00)
            byte[] rdB1 = ("RDB" + realdB).getBytes();
            out.write(rdB1);

            in.close();
            out.close();
            Log.d("bobopro", "Copy Wave File Done: "+inFilename+", "+outFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate)
            throws IOException {
        byte[] header = new byte[44];

        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE / 8;

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8]  = 'W';
        header[9]  = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1 (PCM)
        header[21] = 0;
        header[22] = 1; // channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = 0;
        header[27] = 0;
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        header[32] = (byte) ( RECORDER_BPP / 8); // block align
        header[33] = 0;
        header[34] = (byte) RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }
}