//package com.av.avmessenger;
//
//public class msgModelclass {
//    String message;
//    String senderid;
//    long timeStamp;
//
//    public msgModelclass() {
//    }
//
//    public msgModelclass(String message, String senderid, long timeStamp) {
//        this.message = message;
//        this.senderid = senderid;
//        this.timeStamp = timeStamp;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//
//    public String getSenderid() {
//        return senderid;
//    }
//
//    public void setSenderid(String senderid) {
//        this.senderid = senderid;
//    }
//
//    public long getTimeStamp() {
//        return timeStamp;
//    }
//
//    public void setTimeStamp(long timeStamp) {
//        this.timeStamp = timeStamp;
//    }
//}


package com.av.avmessenger;
import java.util.ArrayList;
import java.util.List;

public class msgModelclass {
    private String message;

    private String senderid;
    private long timestamp;
    //private List<Integer> sessionKey;
    //private List<Integer> sessionIV;
    private String cipherText;

  //  private byte[] u; // Added field for u
//    private byte[] v;

    private List<Integer> u;  // Change this from byte[] to List<Integer>
    private List<Integer> v;

    // Default constructor required for calls to DataSnapshot.getValue(msgModelclass.class)
    public msgModelclass() {}

    public msgModelclass(String cipherText, String senderid, long timestamp,List<Integer> u, List<Integer> v,String message) {
        this.message = message;
        this.senderid = senderid;
        this.timestamp = timestamp;
        //this.sessionKey = sessionKey;
        //this.sessionIV = sessionIV;
        this.u = u;
        this.v = v;
        this.cipherText=cipherText;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCipherText() { return cipherText; }  // Make sure getter is named `getCipherText`
    public void setCipherText(String cipherText) { this.cipherText = cipherText; }

    public String getSenderid() { return senderid; }
    public void setSenderid(String senderid) { this.senderid = senderid; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    //public List<Integer> getSessionKey() { return sessionKey; }
   // public void setSessionKey(List<Integer> sessionKey) { this.sessionKey = sessionKey; }

    //public List<Integer> getSessionIV() { return sessionIV; }
    //public void setSessionIV(List<Integer> sessionIV) { this.sessionIV = sessionIV; }

    // Getter and Setter methods
    public List<Integer> getU() { return u; }
    public List<Integer> getV() { return v; }



    // Convert byte array to List<Integer>
    public static List<Integer> byteArrayToList(byte[] bytes) {
        List<Integer> intList = new ArrayList<>();
        for (byte b : bytes) {
            intList.add((int) b); // Cast byte to Integer
        }
        return intList;
    }

    // Convert List<Integer> back to byte array
    public static byte[] listToByteArray(List<Integer> intList) {
        byte[] bytes = new byte[intList.size()];
        for (int i = 0; i < intList.size(); i++) {
            bytes[i] = intList.get(i).byteValue(); // Cast Integer to byte
        }
        return bytes;
    }
}



