package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.Serializable;

public class Message implements Serializable {
    
    String key;
    String message;
    String type;
    
    
    /**
     * @param key
     * @param message
     */
    public Message(String key, String message,String type) {

        this.key = key;
        this.message = message;
        this.type = type;
    }
   
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    
    

}
