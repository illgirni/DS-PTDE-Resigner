package illgirni.ds.ptde.pc.resigner;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SaveSlot {
    
    private final int index;
    
    private final int offset;
    
    private final String characterName;
    
    public SaveSlot(final int index, final int offset, final byte[] saveFile) {
        this.index = index;
        this.offset = offset;
        this.characterName = readStringUtf16(saveFile, offset + 256);
        
        //System.out.println(index + ". " + characterName);
        //System.out.println("Offset: " + offset);
        //System.out.println("Offset (Checksum): " + getSlotChecksumOffset());
        //System.out.println("Offset (Slot Data): " + getSlotDataOffset());
        //System.out.println("Length (Slot Data): " + getSlotDataLength());
        //System.out.println("Offset (Slot Content): " + getSlotContentOffset());
        //System.out.println("Length (Slot Content): " + getSlotContentLength());
        //System.out.println("Offset (Content Checksum): " + getSlotContentChecksumOffset());
    }
    
    public String getContentChecksumAsString(final byte[] saveFile) {
        final StringBuilder checksum = new StringBuilder();
        
        for (int i = 0; i < 16; i++) {
            final String checksumValue = Integer.toHexString(Byte.toUnsignedInt(saveFile[getSlotContentChecksumOffset() + i]));
            
            if (checksumValue.length() == 1) {
                checksum.append('0');
            } 
            
            checksum.append(checksumValue.toUpperCase());
        }
        
        return checksum.toString();
    }
    
    public String getChecksumAsString(final byte[] saveFile) {
        final StringBuilder checksum = new StringBuilder();
        
        for (int i = 0; i < 16; i++) {
            final String checksumValue = Integer.toHexString(Byte.toUnsignedInt(saveFile[getSlotChecksumOffset() + i]));
            
            if (checksumValue.length() == 1) {
                checksum.append('0');
            } 
            
            checksum.append(checksumValue.toUpperCase());
        }
        
        return checksum.toString();
    }
    
    public void resign(final byte[] saveFile) throws NoSuchAlgorithmException {
        final MessageDigest md5Generator = MessageDigest.getInstance("MD5");
        
        final byte[] slotContent = Arrays.copyOfRange(saveFile, getSlotContentOffset(), getSlotContentOffset() + getSlotContentLength());
        final byte[] contentChecksum = md5Generator.digest(slotContent);
        
//        System.out.println("Content checksum changes: ");
        
        for (int offsetChecksum = 0; offsetChecksum < contentChecksum.length; offsetChecksum++) {
//            System.out.println((getSlotContentChecksumOffset() + offsetChecksum) + ": " 
//                                    + saveFile[getSlotContentChecksumOffset() + offsetChecksum] 
//                                    + " >>> " 
//                                    +  contentChecksum[offsetChecksum]);
            
            saveFile[getSlotContentChecksumOffset() + offsetChecksum] = contentChecksum[offsetChecksum];
        }
        
        final byte[] slotData = Arrays.copyOfRange(saveFile, getSlotDataOffset(), getSlotDataOffset() + getSlotDataLength());
        final byte[] slotChecksum = md5Generator.digest(slotData);
        
//        System.out.println("Slot checksum changes: ");
        
        for (int offsetChecksum = 0; offsetChecksum < slotChecksum.length; offsetChecksum++) {
//            System.out.println((getSlotChecksumOffset() + offsetChecksum) + ": " 
//                                    + saveFile[getSlotChecksumOffset() + offsetChecksum] 
//                                    + " >>> " 
//                                    + slotChecksum[offsetChecksum]);
            
            saveFile[getSlotChecksumOffset() + offsetChecksum] = slotChecksum[offsetChecksum];
        }
    }
    
    private int getSlotContentOffset() {
        return offset + 20;
    }
    
    private int getSlotContentLength() {
        return 393200;
    }
    
    private int getSlotContentChecksumOffset() {
        return offset + 20 + getSlotContentLength(); 
    }
    
    private int getSlotDataOffset() {
        return offset + 16;
    }
    
    private int getSlotDataLength() {
        return 393220;
    }
    
    private int getSlotChecksumOffset() {
        return offset;
    }
    
    public String getCharacterName() {
        return characterName;
    }
    
    public int getIndex() {
        return index;
    }
    
    
    private static String readStringUtf16(final byte[] byteData, final int offset) {
        int stringEnd = offset;
        int stringLength = 0;
        
        while (stringEnd + 1 < byteData.length && !(byteData[stringEnd] == 0 && byteData[stringEnd + 1] == 0)) {
            stringEnd += 2;
            stringLength += 1;
        }
        
        return readStringUtf16(byteData, offset, stringLength);
        
    }
    
    //characters are encoded with UTF-16LE
    private static String readStringUtf16(final byte[] byteData, final int offset, final int stringLength) {
        if (stringLength < 0) {
            return readStringUtf16(byteData, offset);
        } else if (stringLength == 0) {
            return "";
            
        } else {
            try {
                final CharsetDecoder decoder = Charset.forName("UTF-16LE").newDecoder();
                final CharBuffer stringCharacters = decoder.decode(ByteBuffer.wrap(byteData, offset, stringLength * 2));
                
                return stringCharacters.toString();
                
            } catch (CharacterCodingException e) {
                throw new RuntimeException(e);
            }
            
        }
    }
}
