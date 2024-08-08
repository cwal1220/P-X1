package com.inspeco.socket.tcp;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;


/**
 * 소켓으로부터 패킷을 읽어 파싱하는 클래스
 * @author beksung
 *
 */
public  class PacketReader {

	private static final String TAG = PacketReader.class.getSimpleName();

	// 패킷 사이즈
	private static final int PACKET_SIZE = 836;

	/**
	 * 데이터를 읽어서 Sock Data
	 * @param is
	 * @return
	 * @throws IOException
	 */
	public static byte[] readData(InputStream is) throws IOException {
		//Log.e("PACKET", "readData : ");
//		byte[] packet = new byte[PACKET_SIZE];

		byte[] msg = read(is, PACKET_SIZE);
//		System.arraycopy(msg, 0, packet, 4, msg.length);

		//Log.w("readData", "recv ==>  " + NetUtil.byteToHexDisplayString(packet, packet.length));
		return msg;
	}


	// ********************************************************************************
	// 공용 함수 들
	// ********************************************************************************


	/**
	 * 원하는 길이만큼 읽어서 string으로 변환한다.
	 * @param is
	 * @param length	읽을 길이(문자 기준)
	 * @return
	 * @throws IOException
	 */
	protected static byte[] read(InputStream is, int length) throws IOException {
		//Log.e("PACKET", "read : " + length);

		byte[] buffer = new byte[length];
		int total = 0;
		while (total < length) {
			int size =  is.read(buffer, total, (length)-total);

			if (size > 0) {
				total += size;

				if (total < length) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						Log.e(TAG, e.getLocalizedMessage());
					}
				}
			} else {
				return null;
			}
		}
//		Log.e(logTag, "packet reader");
		return buffer;
	}

}
