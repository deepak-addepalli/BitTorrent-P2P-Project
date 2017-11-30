package Peer_Files;
import Message_Files.*;
import static Peer_Files.PeerSetup.fileManager;

import java.io.IOException;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestTimeOut extends TimerTask {
    Message_Related.Request msg;
    FileManager f1 = null;
    ObjectWriter out = null;
    AtomicInteger remotePeerId = new AtomicInteger(-1);
    Messages msg1 ;

    public RequestTimeOut(Message_Related.Request message, FileManager f1, ObjectWriter out, Messages message1, AtomicInteger remotePeerId)
    {
        super();
        this.msg = message;
        this.f1 = f1;
        this.out = out;
        this.msg1 = message1;


    }


    public void run()
    {
        if (fileManager.hasPart(msg.getPieceIndex()))
        {
        	return;
        }
        else {

            try {
                out.writeObject(msg1);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
