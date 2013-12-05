package cmsc417_torrent;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class GetPeers {

	private File torrentFile = null;
	private String urlEncodedHashedInfo = "";
	private String url = "";
	private int port = 0;

	public GetPeers(String torrentFileIn) {
		torrentFile = new File(torrentFileIn);

		// Parse from the .torrent file
		parseAnnounceUrlPort();
		// Hash info section from .torrent file
		urlEncodedHashedInfo = Bencoder.hashInfo(torrentFile);
	}

	public String getUrlEncodedHashedInfo() {
		return urlEncodedHashedInfo;
	}

	public String getAnnounceUrl() {
		return url;
	}

	public int getAnnouncePort() {
		return port;
	}

	/**
	 * 
	 */
	public void parseAnnounceUrlPort() {
		// Might need to add support for announce lists
		try {
			BufferedReader br = new BufferedReader(new FileReader(torrentFile));
			String line = br.readLine();

			line = line.substring(line.indexOf("announce") + 8);
			line = line.substring(line.indexOf(":") + 1);
			line = line.substring(0, line.indexOf("announce") + 8);
			// Should be something like
			// "http://torrent.ubuntu.com:6969/announce"

			// Get the base url
			String baseUrl = line.substring(0, line.lastIndexOf(":"));
			String portString = line.substring(line.lastIndexOf(":") + 1, line.lastIndexOf("/announce"));

			url = baseUrl;
			port = Integer.parseInt(portString);

			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
