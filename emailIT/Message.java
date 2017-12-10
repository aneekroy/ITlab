/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.util.ArrayList;

/**
 *
 * @author souvik
 */
class Message{
    
    private String sender;
    private ArrayList<String> receiver_list;
    private String message;

    public Message() {
        
        sender="";
        receiver_list=new ArrayList<>();
        message="";
    }

    
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public ArrayList<String> getReceiver_list() {
        return receiver_list;
    }

    public void setReceiver_list(ArrayList<String> receiver_list) {
        this.receiver_list = receiver_list;
    }

    public void add_Reciever(String receiver){
        
        receiver_list.add(receiver);
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}