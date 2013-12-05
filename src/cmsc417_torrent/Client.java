package cmsc417_torrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class Client {

	final static String peerId = "aaaaabbbbbccccceeeee";
	static byte[] hashedInfo;
	static String urlEncodedHashedInfo;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {		
		
		
		GetPeers getPeersObj = new GetPeers(
				"C:/Users/Azeem/Documents/CMSC417/cmsc417_torrent/cmsc417_torrent/src/ubuntu.torrent");

		urlEncodedHashedInfo = getPeersObj.getUrlEncodedHashedInfo();
		
		// Get ip and port from getPeersObj
		String response = scrapeTracker(getPeersObj.getAnnounceUrl(), getPeersObj.getAnnouncePort(), getPeersObj);
		HashMap<String, Integer> ipToPortMap = Bencoder.parseResponse(response);
		ArrayList<Peer> peerList = generatePeers(ipToPortMap, getPeersObj.getUrlEncodedHashedInfo());

	}

	static ArrayList<Peer> generatePeers(HashMap<String, Integer> ipToPortMap, String hashedInfo) {

		ArrayList<Peer> peerList = new ArrayList<Peer>();

		for (Entry<String, Integer> entry : ipToPortMap.entrySet()) {
			Peer newPeer = new Peer(entry.getKey(), entry.getValue());
			newPeer.handshake();
			System.exit(0);
			peerList.add(newPeer);
		}

		return peerList;

	}

	static String scrapeTracker(String serverHostname, int port, GetPeers getPeersObj) throws IOException {
		
		String getRequest = "GET /announce?info_hash="
				+ urlEncodedHashedInfo
				+ "&peer_id="
				+ peerId
				+ "&port=9627&uploaded=0&downloaded=0&left=0&numwant=300&compact=1&no_peer_id=1&event=started HTTP/1.0\r\n\r\n";

		Socket echoSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;

		try {
			InetAddress address = InetAddress.getByName(new URL(serverHostname).getHost());
			System.out.println("Connecting to " + serverHostname + " at " + address.getHostAddress() + ":" + port);
			echoSocket = new Socket(address.getHostAddress(), port);
			out = new PrintWriter(echoSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + serverHostname);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for " + "the connection to: " + serverHostname);
			System.exit(1);
		}

		out.println(getRequest.toCharArray());
		StringBuilder response = new StringBuilder();

		int newByte;
		while (true) {
			newByte = in.read();

			if (newByte == -1) {
				break;
			}
			response.append((char) (newByte));
		}

		out.close();
		in.close();
		echoSocket.close();

		return response.toString();

	}

}
