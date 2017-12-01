package Peer_Related;

import All_Messages.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import Logging.logrecord;
import Peer_Related.*;

public class PeerSetup implements Runnable {
    static FileManager fileManager;
    static PeerManager peerManager;
    static PeerInfo peer;
    Vector<Handler> connectionsList = new Vector<>();
    static LinkedList<PeerInfo> peers = new LinkedList<PeerInfo>();
    public  AtomicBoolean isFileCompleted = new AtomicBoolean(false);
    public AtomicBoolean areNeighboursCompleted = new AtomicBoolean(false);
    public AtomicBoolean EndPeerProcess = new AtomicBoolean(false);
    static Properties common_cfg = new Properties();
    
    PeerSetup(Properties common_cfg, LinkedList<PeerInfo> peers, PeerInfo peer) throws Exception {
        this.peers = peers;
        this.common_cfg = common_cfg;
        this.peer = peer;

        fileManager = new FileManager(this.peer,common_cfg,this);

        LinkedList<PeerInfo> peersExceptLocal = new LinkedList<>(removePeer(peers, peer.peerId));
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
                    addConnection(conn);
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
                        e.printStackTrace();
                    }
                }

            }
    public void connectToOtherPeers(){
        Queue<PeerInfo> _listOfPeers = getConnectList(peers,peer.peerId);
        while(_listOfPeers!=null && !_listOfPeers.isEmpty())
        {
            Socket sok = null;
            PeerInfo r = _listOfPeers.poll();
            try {
                sok = new Socket(r.hostName, r.listeningPort);
                Handler conn = new Handler(sok,peer.peerId,r.peerId,fileManager,peerManager);
                  addConnection(conn);
               // Thread.sleep(10);


                }
            catch(Exception e) {
                e.printStackTrace();
            }

            }

        }
    public synchronized void addConnection(Handler conn) {
        if (!connectionsList.contains(conn)) {
            connectionsList.add(conn);
            new Thread(conn).start();
           try {
                wait(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    LinkedList<PeerInfo> removePeer(LinkedList<PeerInfo> peers, int peerId)
    {
        LinkedList<PeerInfo> newPeers = new LinkedList<PeerInfo>();

        for(PeerInfo p : peers)
        {
            if (peerId == p.peerId) { continue;}
            else { newPeers.add(p);}
        }
        return newPeers;
    }
    Queue<PeerInfo> getConnectList(LinkedList<PeerInfo> peers, int peerId)
    {
        Queue<PeerInfo> newPeers = new LinkedList<PeerInfo>();

        for(PeerInfo p : peers)
        {
            if (peerId == p.peerId) {break;}
            else { newPeers.add(p);}
        }
        return newPeers;
    }
    public static PeerInfo getPeer()
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
    public synchronized void notInterestedPeers(Collection<Integer> peerIDsToUnchoke)
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

    public void FileHasCompleted() {
        peerManager.setPeerFileCompleted();
        isFileCompleted.set(true);
        if (isFileCompleted.get() && areNeighboursCompleted.get()) {
            logrecord.getLogRecord().fileComplete();
            EndPeerProcess.set(true);
            logrecord.getLogRecord().closeLogger();


//           System.exit(0);
        }
    }
    public void NeighboursHaveCompleted() {
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
            if (!peerManager.stillInterested(conn.getRemotePeerId(), fileManager.partsPeerHas()))
            {

                conn.pushInQueue(new Messages("Uninterested"));
            }
        }
    }
}