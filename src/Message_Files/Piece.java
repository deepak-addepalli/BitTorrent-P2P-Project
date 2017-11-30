package Message_Files;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Piece extends Messages {

   public Piece (byte[] payload) {
        super ("Piece", payload);
    }
    public int getPieceIndex() {
        return ByteBuffer.wrap(Arrays.copyOfRange(payload, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
    }


    public Piece (int pieceIdx, byte[] content) {
        super ("Piece", join (pieceIdx, content));
    }

    public byte[] getPieceContent() {
        if ((payload == null) || (payload.length <= 4)) {
            return null;
        }
        return Arrays.copyOfRange(payload, 4, payload.length);
    }

    private static byte[] join (int pieceIdx, byte[] second) {
        byte[] concat = new byte[4 + (second == null ? 0 : second.length)];
        System.arraycopy(getPieceIndexBytes (pieceIdx), 0, concat, 0, 4);
        System.arraycopy(second, 0, concat, 4, second.length);
        return concat;
    }






    }

