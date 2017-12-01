package Peer_Related;
import All_Messages.HandShake;

import java.io.*;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import All_Messages.*;
import Logging.logrecord;
public class Handler implements Runnable {
    Socket sok = null;
    int localpeer = -1;
    int expectedRemotePeer = -1;
    AtomicInteger remotePeerId = new AtomicInteger(-1);
    ObjectDeserialization in = null;
    ObjectSerialization out = null;
    BlockingQueue<Messages> queue = new LinkedBlockingQueue<>();
    FileManager f1 = null;
    PeerManager p1 = null;
    class CheckingMessages implements Runnable {  
        boolean isRemotePeerIdChoked = true;
        public void run() {
            while (true) {
                try {
                    if(queue!=null && !queue.isEmpty()){
                    Messages m = queue.take();

                        if(m==null){continue;}
                    if (remotePeerId.get() != -1) {
                        if (m.getMessageType().equals("Choke") && !isRemotePeerIdChoked) {
                            isRemotePeerIdChoked = true;

                            sendMessage(m);
                        } else if (m.getMessageType().equals("Unchoke") && isRemotePeerIdChoked) {
                            isRemotePeerIdChoked = false;
                            sendMessage(m);
                        }

                        else {
                            sendMessage(m);
                        }

                    }}


                } catch (InterruptedException e) {
                    e.printStackTrace();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public Handler(Socket connection, int localPeer, int expectedRemotePeer, FileManager f1, PeerManager p1) throws IOException {
        this.sok = connection;
        this.localpeer = localPeer;
        out = new ObjectSerialization(sok.getOutputStream());
        this.f1 = f1;
        this.p1 = p1;
        this.expectedRemotePeer = expectedRemotePeer;

    }

    public boolean equals(Handler obj) {
        if (obj.remotePeerId == this.remotePeerId) {
            return true;
        } else {
            return false;
        }
    }

    public void run() {
        try {

            CheckingMessages cm = new CheckingMessages();
            Thread t = new Thread(cm);
            t.setName("Thread to check messages");
            t.start();

            in = new ObjectDeserialization(sok.getInputStream());

            HandShake handshake = new HandShake(localpeer);
            System.out.println("handshake received from ");
            out.writeObject(handshake);
            System.out.println("handshake written");
            HandShake msg = (HandShake) in.readObject();
            remotePeerId.set(msg.getPeerId());

            if (expectedRemotePeer!=-1 && (remotePeerId.get() != expectedRemotePeer)) {
                throw new Exception("Remote peer id " + remotePeerId + " does not match with the expected id: " + expectedRemotePeer);    //to reframe
            }
            logrecord.getLogRecord().MakesConnection(remotePeerId.get());
            MsgHandler msgHandler = new MsgHandler(remotePeerId, f1, p1);
            sendMessage(msgHandler.handleHandshake(msg));

            while (true) {

                try {
                Messages otherMessage = (Messages) in.readObject();

                    sendMessage(msgHandler.genericMessageHandle(otherMessage));
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
         catch (Exception e)
            {
                e.printStackTrace();
        }
    }
    public int getRemotePeerId(){
        return remotePeerId.get();
    }

    public synchronized void pushInQueue(final Messages m) {
        queue.add(m);
    }

    public synchronized void sendMessage(Messages message) throws IOException {

        if (message != null) {
            out.writeObject(message);
        
    }
    }}
