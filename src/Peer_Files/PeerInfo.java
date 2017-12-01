package Peer_Related;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;

public class PeerInfo {
    public final int peerId;
    public final String hostName;
    public final int listeningPort;
    public final boolean hasFile;
    public AtomicInteger _bytesDownloadedFrom;   
    public BitSet _receivedParts;                 
    public AtomicBoolean _interested ;      

    public PeerInfo(String pID, String hName, String lPort, String hFile)
    {
        peerId = Integer.parseInt(pID);
        hostName = hName;
        listeningPort = Integer.parseInt(lPort);
        hasFile = hFile.equals("1")?true:false;
        _bytesDownloadedFrom = new AtomicInteger (0);
        _receivedParts = new BitSet();
        _interested = new AtomicBoolean (false);

    }


    static HashSet<Integer> getPeerIds(LinkedList<PeerInfo> peers)
    {
        HashSet<Integer> peerIds = new HashSet<Integer>();
     if(peers!=null && !peers.isEmpty()){
        int i=0;
        while(i<peers.size())
        {
            peerIds.add(peers.get(i).peerId);
            i++;
        }
    }
        return peerIds;}


    public static PeerInfo getPeerByPeerId(int peerId, LinkedList<PeerInfo> list)
    {
       int i=0;
       while(i<list.size()) 
       {
           if((list.get(i).peerId)==peerId)
           {
        	   return list.get(i);
           }
           else
        	   i++;
       }
       return null;
    }
    
  /*  public int getPeerId() {
        return peerId;
    }*/

   /* public int getPort() {
        return listeningPort;
    }*/

    public String getPeerAddress() {
        return hostName;
    }

  /*  public boolean hasFile() {
        return hasFile;
    }


    //to Understand
    public boolean isInterested() {
        return _interested.get();
    }

    public void setInterested() {
        _interested.set (true);
    }

    public void setNotIterested() {
        _interested.set (false);
    }
*/
}
