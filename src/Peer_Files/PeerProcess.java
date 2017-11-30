package Peer_Files;
import java.util.ArrayList;
import Logging.logrecord;
import Peer_Files.*;


import java.io.*;
import java.util.*;
import java.util.logging.Level;


public class PeerProcess {

    static String common_info =  "Common.cfg";
    static String peer_info= "PeerInfo.cfg";
    static Properties common_cfg = new Properties();
    final static ArrayList<PeerInfo> allPeers = new ArrayList<PeerInfo>();


   public static void main(String args[]){

       // int peerId = Integer.valueOf(args[0].trim());
       int peerId = 1002;
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
            System.err.println(e);
        }

     try {
            BufferedReader buffer = new BufferedReader(new FileReader(peer_info));
            while((line = buffer.readLine())!=null)
          {
              String temp[] = line.split(" ");
        	  allPeers.add(new PeerInfo(temp[0],temp[1],temp[2],temp[3]));
          }
             buffer.close();
           }
        catch(Exception e) {
                   System.err.println(e);
                  }

              logrecord.getLogRecord().setLoggerForPeer(peerId);

              PeerInfo peer = PeerInfo.getPeerByPeerId(peerId,allPeers);

    try {
            PeerSetup peer_setup = new PeerSetup(common_cfg, allPeers, peer);
            peer_setup.startingThreadsandMethods();
            Thread t = new Thread(peer_setup);
            t.start();
            peer_setup.connectToOtherPeers();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

       }
   }