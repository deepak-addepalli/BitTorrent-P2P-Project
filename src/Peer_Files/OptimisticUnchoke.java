package Peer_Related;
import Logging.logrecord;

import java.util.*;

public class OptimisticUnchoke implements Runnable {

    LinkedList<PeerInfo> unchokable = new LinkedList<PeerInfo>();
    HashSet<Integer> unchokablePeerIds = new HashSet<Integer>();
    PeerSetup _peerSetupObj;

    public OptimisticUnchoke(PeerSetup obj)
    {
        _peerSetupObj = obj;
    }

    synchronized void setUnchokable(LinkedList<PeerInfo> unchokablePeers)
    {
        unchokable.clear();                                                  
        unchokable = unchokablePeers;
        unchokablePeerIds = PeerInfo.getPeerIds(unchokablePeers);
    }
    public void run()
    {
        try {
            Thread.sleep(Integer.parseInt(PeerProcess.common_cfg.getProperty("OptimisticUnchokingInterval"))*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!unchokable.isEmpty() && unchokable.size()>0)
        {
           
//            Collections.shuffle(unchokable);
//            int peerId = unchokable.getFirst().peerId;
            int index=new Random().nextInt(unchokable.size());
            int peerId=unchokable.get(index).peerId;

            logrecord.getLogRecord().changeOfOptimisticallyUnchokedNeighbors(peerId);
           
            
            if(_peerSetupObj.connectionsList!=null){
            Iterator<Handler> it = _peerSetupObj.connectionsList.iterator();


            while(it.hasNext())
            {
               
                Handler newHandler = (Handler)it.next();
                Collection<Integer> peersToUnchoke = new Vector<Integer>();
                if(newHandler.remotePeerId.get() == peerId)
                {
                    
                    peersToUnchoke.add(peerId);
                }
                _peerSetupObj.unchokePeers(peersToUnchoke);
            }
        }
    }
}}
