package Peer_Related;
import Logging.logrecord;
import Peer_Related.*;


import java.io.*;
import java.util.*;
import java.util.logging.Level;


public class PeerProcess {

    static String CONFIGFILENAME =  "Common.cfg";      
    static String PEERINFOFILENAME= "PeerInfo.cfg";
    static Properties common_cfg ;
    final static LinkedList<PeerInfo> peers = new LinkedList<PeerInfo>();
    public static void main(String args[]) throws Exception {

        int peerId = 1003;
      
        //int peerId = Integer.parseInt(args[0]);
        Reader commonReader =null;
        Reader peerReader =null;
        common_cfg = readCommonFile(commonReader);
        readPeerFile(peerReader);
        logrecord.getLogRecord().setLoggerForPeer(peerId);
        PeerInfo peer = PeerInfo.getPeerByPeerId(peerId,peers);
        try {
            PeerSetup peerSetup = new PeerSetup(common_cfg, peers, peer);
            peerSetup.startingThreadsandMethods();
            Thread t = new Thread(peerSetup);
            t.setName("Making peer as server thread");
            t.start();
            peerSetup.connectToOtherPeers();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    static Properties readCommonFile(Reader commonReader) {
        Properties cProperty = new Properties() {
            public synchronized void load(Reader commonReader) {

                try {
                    commonReader = new FileReader(CONFIGFILENAME);

                    BufferedReader cReader = new BufferedReader(commonReader);

                    int i=0;

                    for (String line; (line = cReader.readLine()) != null; i++) {
                        String arr[] = line.split(" ");
                        setProperty(arr[0], arr[1]);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally
                {

                    try {
                        commonReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

        };
        try {
            cProperty.load(commonReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cProperty;
    }
    static void readPeerFile(Reader peerReader) {
        try {
            peerReader = new FileReader(PEERINFOFILENAME);
            BufferedReader pReader = new BufferedReader(peerReader);


            int i=0;
            for (String line; (line = pReader.readLine()) != null; i++) {
                String arr[] = line.split(" ");
                peers.add(new PeerInfo(arr[0], arr[1], arr[2], arr[3]));



            }


        } catch (Exception e) {

        }
        finally
        {
            try {
                peerReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    }