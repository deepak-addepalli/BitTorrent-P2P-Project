package Peer_Related;
import Logging.logrecord;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class FileManager
{
    private final int _peerId;
    private String _fileName;
    private final boolean _hasFile;
    private final BitSet _partsPeerHas;
    private final int _partSize;
    public static  int _bitsetSize;
    private int _fileSize;
    public static String _PartsPath;
    public static String _actualFilePath;
    private BitSet _partsRequested;
    public static String _fileCreationPath;

    PeerSetup peersetupobj = null;

    public FileManager(PeerInfo peer, Properties obj,PeerSetup peersetupobj) throws Exception
    {
        _peerId = peer.peerId;
        _hasFile = peer.hasFile;
        _partSize = Integer.parseInt(obj.getProperty("PieceSize"));
         System.out.println(_partSize);
        _fileSize = Integer.parseInt(obj.getProperty("FileSize"));
        _bitsetSize = (int) Math.ceil ((float)_fileSize/(float)_partSize);
        this.peersetupobj = peersetupobj;
        _fileName = obj.getProperty("FileName");
        _fileCreationPath = "./peer_"+_peerId+"/files/" + _fileName;
        _PartsPath = "./peer_"+_peerId+"/files/parts/";


        _partsPeerHas = new BitSet(_bitsetSize);
        _partsRequested = new BitSet(_bitsetSize);

        File _partsDir = new File(_PartsPath);
        _partsDir.mkdirs();
        File file = new File(_fileCreationPath);

        if(_hasFile)
        {
        	for (int i = 0; i < _bitsetSize; i++){
                _partsPeerHas.set(i, true);
        	}
            try
            {
            	splitFile(_partSize, _fileCreationPath, _PartsPath);
            }
            catch(Exception e)
            {
                System.err.println(e);
            }
        }
    }

    public BitSet partsPeerHas()
    {
        return (BitSet)_partsPeerHas.clone();
    }

    public synchronized void addPiece(int i, byte[] p)
    {

        if(!hasPart(i))
        {
            _partsPeerHas.set(i);

            saveToDirectory(i, p);

            peersetupobj.gotPart(i);

            if(isFileCompleted())
            {
            	mergeFile(_partsPeerHas.cardinality());
                peersetupobj.FileHasCompleted();

            }
        }
    }

    public  void saveToDirectory(int i, byte[] p)
    {
        FileOutputStream op = null;
        try
        {
            File f = new File(_PartsPath+"/"+i);
            op = new FileOutputStream(f);
            peersetupobj.gotPart(i);
            op.write(p);
            op.flush();
            op.close();
        }
        catch(IOException e)
        {
            System.err.println(e);
        }
    }

    public synchronized boolean hasPart(int index)
    {
        if(index >=0 && index<_partsPeerHas.size()) {
			return _partsPeerHas.get(index);
        }
        return false;
    }
    public static byte[] getPieceIndexBytes (int pieceIdx) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(pieceIdx).array();
    }

    public byte[] getPiece(int index, String path)
    {
        if(hasPart(index) && path.trim().compareTo("") != 0)
        {
            byte[] b = new byte[4];
            b = getPieceIndexBytes(index);
            try
            {
                File f = new File(path+ "/" + Integer.toString(index));
                FileInputStream in = null;
                try
                {
                    in = new FileInputStream(f);

                    byte[] barray = new byte[(int)f.length()];
                    byte[] barrayfinal = new byte[(int)f.length() + b.length];
                    in.read(barray);
                    for(int i=0;i<4;i++)
                    {
                        barrayfinal[i] =b[i];
                    }

                    for(int i=4;i<barrayfinal.length;i++)
                    {

                        barrayfinal[i]=barray[i-4];
                    }

                    return barrayfinal;
                }
                catch(FileNotFoundException e)
                {
                    System.err.println(e);
                }
            }
            catch(Exception e)
            {
                System.err.println(e);
            }
        }
        return null;
    }

    synchronized int partsToRequest(BitSet partsRemotePeerHas)
    {
        partsRemotePeerHas.andNot(partsPeerHas());
        partsRemotePeerHas.andNot(_partsRequested);

        if(!partsRemotePeerHas.isEmpty())
        {
            List<Integer> listOfSetIndices = new ArrayList();
            for(int i = partsRemotePeerHas.nextSetBit(0); i != -1 ; i = partsRemotePeerHas.nextSetBit(i+1))
                listOfSetIndices.add(i);
            if(listOfSetIndices.size()>0){
            int index = listOfSetIndices.get(new Random().nextInt(listOfSetIndices.size()));
            _partsRequested.set(index);

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                synchronized (_partsRequested) {
                                    _partsRequested.clear(index);
                                }
                            }
                        },3000);
            return index;
            }
        }
        return -1;
    }


    private boolean isFileCompleted()
    {
        for (int i = 0; i < _bitsetSize; i++){
            if (!_partsPeerHas.get(i)){
                return false;
            }
        }
        logrecord.getLogRecord().fileComplete();
        return true;
    }

    private void splitFile(int partSize, String _fileCreationPath, String _PartsPath) throws Exception
    {
        if(partSize <= 0 || _fileName.trim().compareTo("") == 0)
        {
            throw new Exception ("File Name Invalid or Part Size less than 0");
        }
        else
        {
            FileInputStream ip = null;
            FileOutputStream op = null;

            try
            {
                ip = new FileInputStream(_fileCreationPath);
                byte[] chunkOfFile;
                int temp = _fileSize;
                int bytesToRead = _partSize;
                int read = 0;
                int chunkNumber = 0;
                while (temp > 0)
                {
                    if (temp < bytesToRead)
                    	bytesToRead = temp;

                    chunkOfFile = new byte[bytesToRead];
                    read = ip.read(chunkOfFile);
                    temp -= read;

                    op = new FileOutputStream(new File(_PartsPath + "/" + Integer.toString(chunkNumber++)));
                    op.write(chunkOfFile);
                    op.flush();
                    op.close();
                    chunkOfFile = null;
                    op = null;
                }
                ip.close();
            }
            catch(IOException e)
            {
                System.err.println(e);
            }
        }
    }

    private void mergeFile(int numberOfParts)
    {
        System.out.println("Inside merge file" + numberOfParts);
        if(numberOfParts > 0)
        {
            try
            {
                FileOutputStream os  = new FileOutputStream(new File(_fileCreationPath));
                FileInputStream is = null;
                byte[] temp = null;
                for(int i=0; i<numberOfParts; i++)
                {
                    File f = new File(_PartsPath+"/"+i);
                    is = new FileInputStream(f);
                    temp = new byte[(int)f.length()];
                    is.read(temp);
                    os.write(temp);
                    temp = null;
                    is.close();
                    is = null;
                }
                os.close();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
        }
    }
}
