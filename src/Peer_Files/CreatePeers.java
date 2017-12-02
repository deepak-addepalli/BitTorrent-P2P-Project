package Peer_Related;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import Logging.logrecord;
import Peer_Related.*;


/*
 * This class helps peer to connect with other peers, to choke or unchoke other peers. It helps the Peer Manager to implement choking,
 * unchoking, informing the peer manager if it about the parts peer received
 */

public class CreatePeers implements Runnable {
    static FileManager fileManager;
    static PeerManager peerManager;
    static PeerData peer;
    Vector<Handler> connectionsList = new Vector<>();
    static ArrayList<PeerData> peers = new ArrayList<PeerData>();
    public  AtomicBoolean isFileCompleted = new AtomicBoolean(false);
    public AtomicBoolean areNeighboursCompleted = new AtomicBoolean(false);
    public AtomicBoolean EndPeerProcess = new AtomicBoolean(false);
    static Properties common_config = new Properties();

    CreatePeers(Properties common_cfg, ArrayList<PeerData> peers, PeerData peer) throws Exception {
        this.peers = peers;
        this.common_config = common_cfg;
        this.peer = peer;

        fileManager = new FileManager(this.peer,common_cfg,this);

        ArrayList<PeerData> peersExceptLocal = new ArrayList<>(removePeer(peers, peer.peerId));
        peerManager = new PeerManager(peersExceptLocal, this);
    }
    void startingThreadsandMethods()
    {
        Thread t = new Thread(peerManager);
        t.setName("PeerManager Thread");
        t.start();
    }
    public void run() {
        try
        {
            ServerSocket serversok = new ServerSocket(peer.listeningPort);
            while(!EndPeerProcess.get()){
                    Socket s = serversok.accept();
                    Handler conn = new Handler(s,peer.peerId,-1,fileManager,peerManager);
                    if (!connectionsList.contains(conn)) {
                        connectionsList.add(conn);
                        new Thread(conn).start();
                       try {
                          //  wait(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println(peer.peerId);
        }
            }
            public static void closeSockets(Vector<Handler> connectionsList)
            {
                for(Handler h : connectionsList)
                {
                    try {
                        h.sok.close();
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                }

            }

            ArrayList<PeerData> removePeer(ArrayList<PeerData> peers, int peerId)
    {
            	ArrayList<PeerData> newPeers = new ArrayList<PeerData>();

        for(PeerData p : peers)
        {
            if (peerId == p.peerId) { continue;}
            else { 
            	newPeers.add(p);
            }
        }
        return newPeers;
    }

    public void connectToOtherPeers(){
        Queue<PeerData> _listOfPeers = getConnectList(peers,peer.peerId);
        while(_listOfPeers!=null && !_listOfPeers.isEmpty())
        {
            Socket sok = null;
            PeerData r = _listOfPeers.poll();
            try {
                sok = new Socket(r.hostName, r.listeningPort);
                Handler conn = new Handler(sok,peer.peerId,r.peerId,fileManager,peerManager);
                if (!connectionsList.contains(conn)) {
                    connectionsList.add(conn);
                    new Thread(conn).start();
                   try {
                       // wait(10);
                    } catch (Exception e) {
                        System.err.println(e);
                    }
                }


                }
            catch(Exception e) {
                System.err.println(e);
            }

            }

        }


    Queue<PeerData> getConnectList(ArrayList<PeerData> peers, int peerId)
    {
        Queue<PeerData> newPeers = new LinkedList<PeerData>();

        for(PeerData p : peers)
        {
            if (peerId == p.peerId) {break;}
            else { newPeers.add(p);}
        }
        return newPeers;
    }
    public static PeerData getPeer()
    {
        return peer;
    }

    public synchronized void unchokePeers(Collection<Integer> peerIDsToUnchoke)
    {
        if(peerIDsToUnchoke!=null && !peerIDsToUnchoke.isEmpty())
        {
            for (int currentPeer : peerIDsToUnchoke)
            {
                if(!connectionsList.isEmpty())
                {
                    for (Handler temp : connectionsList)
                    {
                        if(temp.remotePeerId.get() == currentPeer)
                            temp.pushInQueue(new Messages("Unchoke"));

                    }
                }
            }
        }
    }
    public synchronized void notInterestedPeers(ArrayList<Integer> peerIDsToUnchoke)
    {
        if(peerIDsToUnchoke!=null && !peerIDsToUnchoke.isEmpty())
        {
            for (int currentPeer : peerIDsToUnchoke)
            {
                if(!connectionsList.isEmpty())
                {
                    for (Handler temp : connectionsList)
                    {
                        if(temp.remotePeerId.get() == currentPeer)
                            temp.pushInQueue(new Messages("Uninterested"));

                    }
                }
            }
        }
    }

    public synchronized void chokePeers(Collection<Integer> peerIDsToChoke)
    {
        if(peerIDsToChoke!=null && !peerIDsToChoke.isEmpty())
        {
            for (int currentPeer : peerIDsToChoke)
            {
                if(!connectionsList.isEmpty())
                {
                    for (Handler temp : connectionsList)
                    {
                        if(temp.remotePeerId.get() == currentPeer)
                            temp.pushInQueue(new Messages("Choke"));

                    }
                }
            }
        }
    }

    public void fileHasCompleted() {
        peerManager.setPeerFileCompleted();
        isFileCompleted.set(true);
        if (isFileCompleted.get() && areNeighboursCompleted.get()) {
            logrecord.getLogRecord().fileComplete();
            EndPeerProcess.set(true);
            logrecord.getLogRecord().closeLogger();


//           System.exit(0);
        }
    }
    public void neighboursHaveCompleted() {
        areNeighboursCompleted.set(true);
        if (isFileCompleted.get() && areNeighboursCompleted.get()) {
            EndPeerProcess.set(true);
            logrecord.getLogRecord().closeLogger();

//          System.exit(0);
        }
    }
    public synchronized void gotPart(int partindex){
        for (Handler conn : connectionsList) {
            conn.pushInQueue(new Messages("Have",partindex));
            if (!peerManager.stillInterested(conn.getRemotePeerId(), fileManager.partsPeerContains()))
            {

                conn.pushInQueue(new Messages("Uninterested"));
            }
        }
    }
}