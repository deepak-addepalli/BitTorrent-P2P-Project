package Peer_Files;
import Logging.logrecord;

import java.util.*;

public class OptimisticUnchoke implements Runnable {

    ArrayList<PeerInfo> unchokable = new ArrayList<PeerInfo>();
    HashSet<Integer> unchokablePeerIds = new HashSet<Integer>();
    PeerSetup _peerSetupObj;

    //Constructor
    public OptimisticUnchoke(PeerSetup obj)
    {
        _peerSetupObj = obj;
    }

    synchronized void setUnchokable(ArrayList<PeerInfo> unchokablePeers)
    {
        unchokable.clear();                                                   //changes
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
            //Simple way of selecing unchokable neighbor
            Collections.shuffle(unchokable);
            int peerId = unchokable.get(0).getPeerId();

            logrecord.getLogRecord().changeOfOptimisticallyUnchokedNeighbors(peerId);
           // logrecord.getLogRecord().changeOfOptimisticallyUnchokedNeighbors(peerId);
            //sends unchoking message
            if(_peerSetupObj.connectionsList!=null){
            Iterator<Handler> it = _peerSetupObj.connectionsList.iterator();


            while(it.hasNext())
            {
                //System.out.println("to send unchoke in unoptimistic unchoke thread");
                Handler newHandler = (Handler)it.next();
                Collection<Integer> peersToUnchoke = new Vector<Integer>();
                if(newHandler.remotePeerId.get() == peerId)
                {
                    //create a new collection and add this connection Handler to it. Then send it to peerSetUp to unchoking the peer
                    peersToUnchoke.add(peerId);
                }
                _peerSetupObj.unchokePeers(peersToUnchoke);
            }
        }
    }
}}
