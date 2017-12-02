package Peer_Related;
import java.util.ArrayList;
import Logging.logrecord;
import Peer_Related.*;


import java.io.*;
import java.util.*;
import java.util.logging.Level;


public class PeerProcess {

	static String common_info =  "Common.cfg";
	static String peer_info= "PeerInfo.cfg";
	static Properties common_cfg = new Properties();
	final static ArrayList<PeerData> allPeers = new ArrayList<PeerData>();


	public static void main(String args[]){

//		int peerId = Integer.valueOf(args[0].trim());
		int peerId=1003;
		String line = null;

		try {
			BufferedReader buffer = new BufferedReader(new FileReader(common_info));

			while((line=buffer.readLine())!=null) {
				String[] temp = line.split(" ");
				common_cfg.setProperty(temp[0], temp[1]);
			}
			buffer.close();
		}
		catch (Exception e) {
			System.err.println(e);;
		}

		try {
			BufferedReader buffer = new BufferedReader(new FileReader(peer_info));
			while((line = buffer.readLine())!=null)
			{
				String temp[] = line.split(" ");
				allPeers.add(new PeerData(Integer.valueOf(temp[0].trim()),temp[1],Integer.valueOf(temp[2].trim()),Integer.valueOf(temp[3].trim())));
			}
			buffer.close();
		}
		catch(Exception e) {
			System.err.println(e);;
		}

		logrecord.getLogRecord().setLoggerForPeer(peerId);

		PeerData peer = PeerData.getPeerByPeerId(peerId,allPeers);

		try {
			CreatePeers peer_setup = new CreatePeers(common_cfg, allPeers, peer);
			peer_setup.startingThreadsandMethods();
			Thread t = new Thread(peer_setup);
			t.start();
			peer_setup.connectToOtherPeers();
		}
		catch (Exception e)
		{
			System.err.println(e);
		}

	}
}
