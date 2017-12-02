package Peer_Related;
import Logging.logrecord;

import java.util.*;

public class OptimisticUnchoke implements Runnable {

    ArrayList<PeerData> unchokable = new ArrayList<PeerData>();
    HashSet<Integer> unchokablePeerIds = new HashSet<Integer>();
    CreatePeers createPeerObj;

    public OptimisticUnchoke(CreatePeers obj)
    {
        createPeerObj = obj;
    }

    synchronized void setUnchokable(ArrayList<PeerData> unchokablePeers)
    {
        unchokable = unchokablePeers;
        unchokablePeerIds = PeerData.getPeerIds(unchokablePeers);
    }
    public void run()
    {
        try {
            Thread.sleep(Integer.parseInt(PeerProcess.common_cfg.getProperty("OptimisticUnchokingInterval"))*1000);
        } catch (InterruptedException e) {
            System.err.println(e);;
        }
        if(!unchokable.isEmpty() && unchokable.size()>0)
        {
            int index=new Random().nextInt(unchokable.size());
            int peerId=unchokable.get(index).peerId;

            logrecord.getLogRecord().changeOfOptimisticallyUnchokedNeighbors(peerId);
            ArrayList<Integer> peersToUnchoke = new ArrayList<Integer>();

            if(createPeerObj.connectionsList!=null){
            	for(Handler h: createPeerObj.connectionsList) {
    				if(h.remotePeerId.get()==peerId) {
    					peersToUnchoke.add(peerId);
    				}
    				createPeerObj.unchokePeers(peersToUnchoke);
    			}

            
        }
    }
}}
