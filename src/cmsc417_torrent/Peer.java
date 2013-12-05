package cmsc417_torrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

public class Peer {
	
	private String ip;
	private Integer port;
	private boolean connected;
	private byte[] hashedInfo;
	private String peerId;
	
	public Peer(String ipIn, Integer portIn){
		this.ip = ipIn;
		this.port = portIn;
		this.hashedInfo = Client.hashedInfo;
		this.peerId = Client.peerId;
		connected = false;
		
		//212.182.155.182:51413
		this.ip = "212.182.155.182";
		this.port = 51413;
	}
	
	public String getIp() {
		return ip;
	}

	public Integer getPort() {
		return port;
	}

	
	public String toString(){
		return (ip + ":" + port);
	}

	public boolean handshake(){
		
		
		byte[] handShakeData =  new byte[68];
		
		int index = 0;
		// pstr length (1 byte)
		handShakeData[index++] = 023;
		
		// pstr string (19 bytes)
		// BitTorrent protocol
		
		for(byte x : ((String)"BitTorrent Protocol").getBytes()){
			handShakeData[index++] = x;
		}
		
		// Reserved 8 bytes section
		for(int i = 0; i < 8; i++){
			handShakeData[index++] = 00;
		}
		
		// Infohash
		for(byte x : hashedInfo){
			handShakeData[index++] = x;
		}
		
		for(byte x : peerId.getBytes()){
			handShakeData[index++] = x;
		}
		
		
		sendBytes(handShakeData);

		
		return connected;
	}
	
	
	public BufferedReader sendBytes(byte[] sendData){
		
		Socket echoSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;

		try {
			System.out.println("Connecting to " + ip + ":" + port);
			InetAddress address = InetAddress.getByName(ip);
			echoSocket = new Socket(address.getHostAddress(), port);
			out = new PrintWriter(echoSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Error Unknown Host");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for " + "the connection to: " + ip);
			System.exit(1);
		}
		
		System.out.println("Writing to" + ip + ":" + port);
		// Maybe Byte Array
		out.println(sendData);
		System.out.println("Data Sent to " + ip + ":" + port);

		
		try {
			System.out.println(in.readLine());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return in;
	}
}
