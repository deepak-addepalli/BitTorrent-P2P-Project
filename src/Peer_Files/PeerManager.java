package Peer_Related;
import Logging.logrecord;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PeerManager implements Runnable {

    LinkedList<PeerInfo> preferredNeighbours;
    OptimisticUnchoke unchoker = null;
    LinkedList<PeerInfo> peersExceptLocal = new LinkedList<PeerInfo>();
    PeerSetup obj = null;
    AtomicBoolean peerFileCompleted ;


    PeerManager(LinkedList<PeerInfo> peersExceptLocal, PeerSetup obj)
    {
        this.peersExceptLocal = peersExceptLocal;
        unchoker = new OptimisticUnchoke(obj);
        this.obj = obj;
        peerFileCompleted = PeerSetup.peer.hasFile?new AtomicBoolean(true):new AtomicBoolean(false);

    }



    @Override
    public void run() {
        
        Thread t = new Thread(unchoker);         
      
        t.start();

        
        while(true)
        {
        try {
            Thread.sleep(Integer.parseInt(PeerProcess.common_cfg.getProperty("UnchokingInterval"))*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LinkedList<PeerInfo> interestedPeers = getInterestedPeers(peersExceptLocal);
        HashSet<Integer> interestedPeersIds = new HashSet<Integer>();
        interestedPeersIds = PeerInfo.getPeerIds(interestedPeers);

        LinkedList<PeerInfo> preferredPeers  = getPreferredPeers(interestedPeers,peerFileCompleted.get());
        HashSet<Integer> preferredPeersIds = new HashSet<Integer>();
        preferredPeersIds = PeerInfo.getPeerIds(preferredPeers);
            if(preferredPeersIds!=null && preferredPeersIds.size()>0){
                logrecord.getLogRecord().changeOfPrefereedNeighbors(preferredPeersIds);
            }

            this.preferredNeighbours = preferredPeers;
          

        LinkedList<PeerInfo> peersToChoke = getPeersToChoke(preferredPeers);
        HashSet<Integer> peersToChokeIds= new HashSet<Integer>();
        peersToChokeIds = PeerInfo.getPeerIds(peersToChoke);
           
            obj.chokePeers(peersToChokeIds);               
            obj.unchokePeers(preferredPeersIds);

        LinkedList<PeerInfo> optimisticallyUcPeers = getOptimisticallyUcPeers(preferredPeers,interestedPeers);
        HashSet<Integer> optimisticallyUnchokePeerIds = new HashSet<Integer>();
            optimisticallyUnchokePeerIds = PeerInfo.getPeerIds(optimisticallyUcPeers);




           if(optimisticallyUcPeers!=null){
        unchoker.setUnchokable(optimisticallyUcPeers);}

    
    }}



    public void setPeerFileCompleted()
    {
        peerFileCompleted.set(true);
    }


    synchronized void receivedPart(int peerId, int size) {
        PeerInfo peer  = searchForPeer(peerId);
        if (peer != null) {
            peer._bytesDownloadedFrom.addAndGet(size);
        }
    }

  

    synchronized LinkedList<PeerInfo> getOptimisticallyUcPeers(LinkedList<PeerInfo> preferredPeers, LinkedList<PeerInfo> interestedPeers)
    {
        if(interestedPeers.size()>2){
            LinkedList<PeerInfo> peers = new LinkedList<PeerInfo>(interestedPeers);
            int i=0;
            while(i<preferredPeers.size()){
            	peers.remove(preferredPeers.get(i));
            	i++;
            }
            
            return peers;}
        else
            return null;
    }


    synchronized LinkedList<PeerInfo> getPreferredPeers(LinkedList<PeerInfo> interestedPeers,boolean peerFileCompleted)
    {
        LinkedList<PeerInfo> preferredPeers = new LinkedList<PeerInfo>();
        if( interestedPeers!=null && !interestedPeers.isEmpty()) {
            if (peerFileCompleted) {
                Collections.shuffle(interestedPeers);

            } else {
                Collections.sort(interestedPeers, new Comparator<PeerInfo>() {
                    @Override
                    public int compare(PeerInfo o1, PeerInfo o2) {
                        if (o1._bytesDownloadedFrom.get() > o2._bytesDownloadedFrom.get()) {
                            return 1;
                        } else {
                            return -1;
                        }

                    }
                });
            }
            
            if(interestedPeers.size()>=2){
            	preferredPeers.add(interestedPeers.get(0));
            	preferredPeers.add(interestedPeers.get(1));
            	
            }
            else{
            	int i=0;
            	while(i<interestedPeers.size()){
            		preferredPeers.add(interestedPeers.get(i));
            		i++;
            		
            	}
            }
            
         /*   int i = 1;
            for (PeerInfo p : interestedPeers) {
                preferredPeers.add(p);
                if (i == 2 || i == interestedPeers.size()) {
                    break;
                }
                i++;
            }*/
        }
        return preferredPeers;

    }





    synchronized LinkedList<PeerInfo> getPeersToChoke(LinkedList<PeerInfo> preferredPeers)
    {
        LinkedList<PeerInfo> peersToChoke = new LinkedList<PeerInfo>(peersExceptLocal);
        int i=0;
        while(i<preferredPeers.size()){
        	peersToChoke.remove(preferredPeers.get(i));
        	i++;
        }
        return peersToChoke;
    }




   
    public synchronized void setIsInterested(int idOfRemotePeer)
    {
       
        PeerInfo interestedPeer = searchForPeer(idOfRemotePeer);

        if(interestedPeer!=null)
            interestedPeer._interested.set(true);
          
    }

    public synchronized void setIsNotInterested(int idOfRemotePeer)
    {
        PeerInfo notInterestedPeer = searchForPeer(idOfRemotePeer);
        if(null != notInterestedPeer)
           notInterestedPeer. _interested.set (false);
           
    }

  
    public PeerInfo searchForPeer(int peerID)
    {
        if(peersExceptLocal!=null &&!peersExceptLocal.isEmpty())
        {
//            for (PeerInfo temp : peersExceptLocal)
            int i=0; 	
            while(i<peersExceptLocal.size())	
            {
                if(peerID == peersExceptLocal.get(i).peerId){
                    return peersExceptLocal.get(i);
                }
                else{
                	i++;
                }
            }
        }
        return null;
    }

   
    public synchronized void updateHave(int pieceIndex, int remotePeerID)
    {
        PeerInfo peer = searchForPeer(remotePeerID);
        if(null != peer)
            peer._receivedParts.set(pieceIndex);
        haveNeighboursCompleted();
    }

    synchronized boolean stillInterested(int peerId, BitSet bitset) {
        PeerInfo peer  = searchForPeer(peerId);
        if (peer != null) {
            BitSet pBitset = (BitSet) peer._receivedParts.clone();
            pBitset.andNot(bitset);
            return ! pBitset.isEmpty();
        }
        return false;
    }
  

    private synchronized void haveNeighboursCompleted() {
//        for(PeerInfo peer : peersExceptLocal )
        int i=0;
        while(i<peersExceptLocal.size())
        {
            if (peersExceptLocal.get(i)._receivedParts.cardinality() < FileManager._bitsetSize) {
                return;
            }
            i++;
        }
        obj.NeighboursHaveCompleted();
    }
    
    public synchronized void updateBitField(int remotePeerID, BitSet bitfield)
    {
        PeerInfo peer =  searchForPeer(remotePeerID);
        if(peer!= null)
            peer._receivedParts = bitfield;
        haveNeighboursCompleted();
    }

    public synchronized boolean canTransferToPeer(int remotePeerID)
    {
        
    	PeerInfo peer = searchForPeer(remotePeerID);
        System.out.println(preferredNeighbours + " " + unchoker.unchokable.size() + " " + remotePeerID + " " + PeerInfo.getPeerByPeerId(remotePeerID,peersExceptLocal)._interested );
        if(preferredNeighbours!=null && unchoker.unchokable!=null){
        	System.out.println();
        	return (preferredNeighbours.contains(peer) || unchoker.unchokable.contains(peer));
        }
        else{
        	return false;
        }
    }

  
    public synchronized BitSet getReceivedParts(int remotePeerID)
    {
        PeerInfo peer  = searchForPeer(remotePeerID);
        if (peer != null)
            return (BitSet) peer._receivedParts.clone();                
        return new BitSet();  
        
    }

    synchronized LinkedList<PeerInfo> getInterestedPeers(LinkedList<PeerInfo> peers)
    {
        LinkedList<PeerInfo> interestedPeers= new LinkedList<PeerInfo>();
//        for(PeerInfo p : peers)
        	int i=0;
        while(i<peers.size())
        {
            if(peers.get(i)._interested.get()) { 
            	interestedPeers.add(peers.get(i));
            	}
         i++;
        }
        return interestedPeers;
    }
}