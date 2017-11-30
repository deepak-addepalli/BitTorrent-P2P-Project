/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Peer_Files;

import Logging.logrecord;
import Message_Files.*;

import static Message_Files.Messages.getMessageByByte;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MsgHandler {

    private FileManager _fileManager;
    private boolean _isChoked;
    private LogRecord _logger;
    private PeerManager _peerManager;
    private AtomicInteger _remotePeerID;

    public MsgHandler(AtomicInteger remotePeerId, FileManager fmObj, PeerManager pmObj)
    {
        _remotePeerID = remotePeerId;
        _isChoked = true;
        _fileManager = fmObj;
        _peerManager = pmObj;
    }

    public Messages handleHandshake(HandShake msg)
    {
        BitSet b = _fileManager.partsPeerHas();
        if(!b.isEmpty())
            return(new Message_Related().getBitfield(b.toByteArray()));

        return null;
    }

    public synchronized Messages genericMessageHandle( Messages msg)
    {
        if(null != msg)
        {
            String msgType = msg.getMessageType();
            switch(msgType)
            {
                case "Choke":
                {
                    _isChoked = true;
                    logrecord.getLogRecord().choked(_remotePeerID.get());
                    return null;
                }

                case "Unchoke":
                {
                    _isChoked = false;

                    logrecord.getLogRecord().unchoked(_remotePeerID.get());
                    System.out.println("receiving opt unchoke");
                    return requestForAPiece();
                }

                case "Interested":
                {
                    //Set the remote peer as interested.
                    _peerManager.setIsInterested(_remotePeerID.get());
                    logrecord.getLogRecord().receivedInterested(_remotePeerID.get());
                    //System.out.println("Interested");
                    return null;
                }

                case "Uninterested":
                {
                    //set not interested
                    _peerManager.setIsNotInterested(_remotePeerID.get());
                    logrecord.getLogRecord().receivedNotInterested(_remotePeerID.get());
                    return null;
                }

                case "Have":
                {
                    Message_Related.Have h = (Message_Related.Have)msg;
                    //Get the piece index from payload
                    final int index = ByteBuffer.wrap(Arrays.copyOfRange(h.payload, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt(); //converting an integer to 4 bytes array
                    _peerManager.updateHave(index, _remotePeerID.get());
                    logrecord.getLogRecord().receivedHave(_remotePeerID.get(),index);

                    //If the received piece is of interest then create a new interested message and return it back or else send not interested
                    if(!_fileManager.partsPeerHas().get(index)) {
                        return new Message_Related.Interested();
                    }
                    else {
                        return new Message_Related.Uninterested();
                    }
                }

                case "Bitfield":
                {
                    //Here we recieve a bitfield from some other peer. We need to update _receivedParts bitset of the peer with this information
                    Message_Related.Bitfield bitfield = (Message_Related.Bitfield)msg;
                    BitSet bitset = BitSet.valueOf(bitfield.payload);
                    _peerManager.updateBitField(_remotePeerID.get(), bitset);

                    bitset.andNot(_fileManager.partsPeerHas());


                    if(!bitset.isEmpty())
                        return new Message_Related.Interested();
                    else
                        return new Message_Related.Uninterested();
                }

                case "Request":
                {

                    logrecord.getLogRecord().peerLogger.log(Level.INFO, " for for piece ");
                /* A remote peer has sent this peer a request message. We will do the following
                    1. Get the index of the part the peer has requested for.
                    2. Using peerId, determine whether we are allowed to exchange data with this peer. This will happen if the peer is either a preferred
                        neighbor or an optimistically unchoked neighbor.
                    3. If yes, then we will generate a new piece message and send the data to the peer
                */ // System.out.println("request above");
                    Message_Related.Request r = (Message_Related.Request)msg;
                    int pieceRequestedFor = ByteBuffer.wrap(Arrays.copyOfRange(r.payload, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
                    //System.out.println( " Piece requested for " + pieceRequestedFor + " : "+_remotePeerID);
                    if(pieceRequestedFor!=-1 && _fileManager.partsPeerHas().get(pieceRequestedFor) && _peerManager.canTransferToPeer(_remotePeerID.get()))
                    {
                        byte[] temp = _fileManager.getPiece(pieceRequestedFor, _fileManager.partFilesPath);

                        if(null != temp){
                           // System.out.println("before a piece is made by peer1" +_fileManager.partsPeerHas().toString());
                            return new Piece(temp);
                    }
                    return null;

                }}

                case "Piece":
                {

                    if(msg.getMessageType().equals("Request"))
                    {
                       return null;
                    }
                    System.out.println(msgType);
                    //System.out.println("Piece");




              //
                   Piece piece = (Piece) msg;
               // }

                    //System.out.println("0" +_fileManager.partsPeerHas().toString());
                    int sentPieceIndex = ByteBuffer.wrap(Arrays.copyOfRange(piece.payload, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
                  ;
                    logrecord.getLogRecord().pieceDownloaded(_remotePeerID.get(), piece.getPieceIndex(), _fileManager.partsPeerHas().cardinality());

                    _fileManager.addPiece(sentPieceIndex, piece.getPieceContent());//Something that needs to be seen further
                    _peerManager.receivedPart(_remotePeerID.get(), piece.getPieceContent().length);

                    //System.out.println("2nd" + _fileManager.partsPeerHas().toString());

                    return requestForAPiece();
                }
            }
        }
        return null;
    }

    private Messages requestForAPiece()
    {
      //  System.out.println("comes in request for piece");
        if(!_isChoked)
        {
            logrecord.getLogRecord().peerLogger.log(Level.INFO, "Asking for piece beore ");
            //System.out.println("before index to request" +_fileManager.partsPeerHas().toString());
            int indexOfPieceToRequest = _fileManager.partsToRequest(_peerManager.getReceivedParts(_remotePeerID.get()));
            //System.out.println("before asking for a piece after unchoke is made by peer1" +_fileManager.partsPeerHas().toString());
            System.out.println("peer2 asking for  index " + indexOfPieceToRequest);
            if(indexOfPieceToRequest >= 0){
                return new Message_Related.Request(indexOfPieceToRequest);}
                else{
                    return new Message_Related.Uninterested();//this converts the pieceIndex to bytes
            }
        }

        return null;
    }

}

