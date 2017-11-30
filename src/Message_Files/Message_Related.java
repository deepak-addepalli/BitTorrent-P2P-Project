package Message_Files;


public class Message_Related{

public class Bitfield extends Messages {
	public Bitfield (byte[] bitfield) {
        super ("Bitfield", bitfield);
    }
}
public Bitfield getBitfield(byte[] a) {
	return new Bitfield(a);
}


public class Choke extends Messages {

    public Choke(){
        super ("Choke");

    }
}

public Choke getChoke() {
	return new Choke();
}

public static class Have extends Messages {

	 Have (byte[] pieceIndex) {
		 super ("Have", pieceIndex);
	  }

	  public Have (int pieceIdx) {
	        this (getPieceIndexBytes (pieceIdx));
	  }


}
public Have getHave(byte[] a) {
	return new Have(a);
}


public static class Interested  extends Messages {

    public Interested() {
        super ("Interested");
    }
}
public Interested getInterested() {
 return new Interested();
}


public static class Request extends Messages {
	public Request (byte[] pieceIdx)
    {

        super ("Request", pieceIdx);
    }
    public Request (int pieceIdx) {
        this (getPieceIndexBytes (pieceIdx));
    }
}
public Request getRequest(byte[] a) {
	return new Request(a);
}

public static class Unchoke extends Messages {

    public Unchoke() {
        super ("Unchoke");
    }
}
public Unchoke getUnchoke() {
	return new Unchoke();
}

public static class Uninterested  extends Messages{

    public Uninterested() {
        super ("Uninterested");
    }
}
public Uninterested getUninterested() {
	return new Uninterested();
}



}