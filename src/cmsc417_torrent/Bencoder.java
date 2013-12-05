package cmsc417_torrent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Bencoder {

	static String hashInfo(File torrentFile) {
		MessageDigest sha1 = null;
		try {
			sha1 = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		InputStream input = null;

		try {
			input = new FileInputStream(torrentFile);
			StringBuilder builder = new StringBuilder();
			while (!builder.toString().endsWith("4:info")) {
				builder.append((char) input.read()); // It's ASCII anyway.
			}
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			for (int data; (data = input.read()) > -1; output.write(data))
				;
			sha1.update(output.toByteArray(), 0, output.size() - 1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException ignore) {
				}
		}

		byte[] hash = sha1.digest(); // Here's your hash. Do your thing with it.
		Client.hashedInfo = hash;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < hash.length; i++) {
			sb.append("%");
			sb.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}

	static HashMap<String, Integer> parseResponse(String responseData) {

		HashMap<String, Integer> ipToPeerMap= new HashMap<String, Integer>();

		Integer interval = -1;
		if (responseData.indexOf("intervali") != -1) {
			String intervalStr = responseData.substring(responseData.indexOf("intervali") + 9);
			intervalStr = intervalStr.substring(0, intervalStr.indexOf("e"));
			interval = new Integer(intervalStr);
			System.out.println("Interval: " + interval);
		}

		Integer peers = -1;
		if (responseData.indexOf("peers") != -1) {
			String peersStr = responseData.substring(responseData.indexOf("peers") + 5);
			peersStr = peersStr.substring(0, peersStr.indexOf(":"));
			peers = new Integer(peersStr);
			System.out.println("Peers: " + peers);
		}

		// Peers could be dictionary or binary (which is what is implemented
		// below)

		byte[] ip = new byte[4];
		byte[] port = new byte[4];
		// Convert is a byte array containing a list of ip (4 bytes) and ports
		// (2 bytes) ie. 192.168.1.16969/224.200.1.59000/100.168.76.18000
		byte[] convert = responseData.substring(
				responseData.indexOf("peers" + peers.toString() + ":") + 6 + peers.toString().length()).getBytes();

		int byteIndex = 0;

		ArrayList<String> ipList = new ArrayList<String>();
		ArrayList<Integer> portList = new ArrayList<Integer>();

		while (byteIndex + 6 < convert.length) {
			for (int i = 0; i < 4; i++) {
				ip[i] = convert[i + byteIndex];
			}
			byteIndex += 4;

			for (int i = 0; i < 2; i++) {
				port[i] = convert[i + byteIndex];
			}

			byteIndex += 2;

			try {

				// Add IP string to list
				String decodedIp = InetAddress.getByAddress(ip).toString();
				String parsedIp = decodedIp.substring(1);
				ipList.add(parsedIp);

				// Add port to list
				Integer parsedPort = ByteBuffer.wrap(port).order(ByteOrder.LITTLE_ENDIAN).getInt();
				portList.add(parsedPort);
				
				ipToPeerMap.put(parsedIp, parsedPort);
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}

		}


		return ipToPeerMap;
	}

}
