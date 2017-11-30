package Message_Files;
import java.util.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Messages {
	 public int msglength;
	    public byte msgtype;
	    public byte[] payload;
	    static Byte [] val = {0,1,2,3,4,5,6,7};
	    static HashMap<String, Byte> hmap = new HashMap<String, Byte>();
	    final static String[] str={"Choke", "Unchoke","Interested","Unintersted","Have","Bitfield","Request","Piece"};
	    static {
            hmap.put("Choke", val[0]);
	        hmap.put("Unchoke", val[1]);
	        hmap.put("Interested", val[2]);
	        hmap.put("Uninterested", val[3]);
	        hmap.put("Have", val[4]);
	        hmap.put("Bitfield", val[5]);
	        hmap.put("Request", val[6]);
	        hmap.put("Piece", val[7]);
	    
	    }
	    
	    
	    
	    
	    public String getMessageType()
	    
	    {
	    
	        return getMessageByByte(this.msgtype);
	    
	    }
	    
	      
		  Messages(String type, byte[] payload)
	    	
	     {
	    	
	    	        if(payload==null)
	    		    
	    	        {
	    	
	    	           msglength = 1;
	    	
	    	        }
	    	
	    	        else if(payload.length==0)
	    	
	    	        {
	    	
	    	            msglength = 1;
	    	
	    	        }
	    	
	    	        else {
	    	
	    	            msglength = payload.length + 1;
	    	
	    	        }
	    	
	    	        msgtype = hmap.get(type);
	    	
	    	        this.payload = payload;
	    	
	    	
	    	    }
		  
		  
		  
		  Messages(String type) 
				{
	    	
	    	        this.msgtype = hmap.get(type);
	                this.msglength=1;
	                this.payload = null;;
	    	
	            }
	    	
	     
	    	
	    	    Byte getTypeOfMessage(String type)
	    	
	    	    {
	    	
	    	        return hmap.get(type);
	    	
	    	    }
	    	
	    	
public static String getMessageByByte(byte b)
		    	
	    	    {
	    	    	
	    	    	String res=str[b];
	    	    	return res;
	    	    	
	    	    	
	    	    		
	    	    	
	    	    }	
	    	
	    	    public static Messages getMessage (int length, String type)  {
	    	

	    	
	    	             if(type=="Choke")
	    	             {
	    	
	    	                return new Message_Related().getChoke();
	    	             }
	    	             
	    	             
	    	             else if(type=="Unchoke")
	    	             {
	    	
	    	            	 return new Message_Related().getUnchoke();
                         
	    	             }
	    	
	    	             else if(type=="Interested")
	    	             {
	    	            	 
	    	
	    	                return new Message_Related().getInterested();
	    	                
	    	             }   
	    	
	    	
	    	             else if(type=="Uninterested")
	    	             { 	 
	    	
	    	                return new Message_Related().getUninterested();
	    	                
	    	             }   
	    	
	    	             else if(type=="Have")
	    	             {
	    	
	    	                return new Message_Related().getHave (new byte[length]);
	    	                
	    	             }  
	    	
	    	
	    	             else if(type=="Bitfield")
	    	             {
	    	
	    	                if(length > 0)
	    	
	    	                    return new Message_Related().getBitfield (new byte[length]);
	    	
	    	                else
	    	
	    	                    return new Message_Related().getBitfield(new byte[0]);
	    	
	    	            }
	    	
	    	
	    	             else if(type=="Request")
	    	            	 
	    	             { 	 
	    	
	    	                return new Message_Related().getRequest (new byte[length]);
	    	                
	    	             }   
	  
	    	
	    	             else if(type=="Piece")
	    	             {	 
	    
	    	                return new Piece(new byte[length]);
	    	                
	    	             }   
	    	
	    	
	    	             else
	    	             {
	    	
	    	                return  new Message_Related().getInterested();   
	    	                
	    	             }   
	    	        
	    	
	    	    }
	    	
	    	    public void read (DataInputStream isr) throws IOException {
	    	
	    	        if ((payload != null) && (payload.length) > 0) {
	    	
	    	            isr.readFully(payload, 0, payload.length);
	    	
	    	        }
	    	
	    	    }
	    	
	    	
	    	    public void write (DataOutputStream osr) throws IOException {
	    	
	    	        osr.writeInt (msglength);
	   
	    	        osr.writeByte (msgtype);
	    	
	    	        if ((payload != null)) {
	    
	    	            osr.write (payload, 0, payload.length);
	    	
	    	        }
	    	
	    	    }
	    	
	    	    public  int getPieceIndex() {
	    	
	    	        return ByteBuffer.wrap(Arrays.copyOfRange(payload, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
	    	
	    	    }
	    	    
				public static byte[] getPieceIndexBytes (int pieceIdx) {
	    	
	    	        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(pieceIdx).array();
	    	
	    	    }
	    	
	    	    
	    	
	    	
	    	
}
	    



