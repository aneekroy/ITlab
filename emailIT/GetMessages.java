/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author souvik
 */
public class GetMessages {
    
    Client cmain;
    String id, ip;
    HashMap<String,Integer> pop3_recv=new HashMap<>();
    public GetMessages(String id, String ip, Client ob) {
        
        this.id=id;
        this.ip=ip;
        cmain=ob;
        pop3_recv.put("gmail.com", 3995);
        pop3_recv.put("sinju.com", 3996);
       
    }
    
    public void error(){
        
        javax.swing.JOptionPane.showMessageDialog(null, "error", "Message", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
    public void recv_messages(){
        
        try{
            
            int i=id.indexOf("@");
            Socket socket=new Socket(ip, pop3_recv.get(id.substring(i+1)));
            DataInputStream inp=new DataInputStream(socket.getInputStream());
            DataOutputStream oup=new DataOutputStream(socket.getOutputStream());
            
            String line;
            line=inp.readUTF();
            System.out.println("Server: "+line+"\n");
            if(!line.substring(0, 3).equals("+OK")){
                error();
            }
            line="APOP "+id;
            oup.writeUTF(line);
            System.out.println("Client: "+line+"\n");
            line=inp.readUTF();
            System.out.println("Server: "+line+"\n");
            if(!line.substring(0, 3).equals("+OK")){
                error();
            }
            
            line="STAT";
            oup.writeUTF(line);
            System.out.println("Client: "+line+"\n");
            line=inp.readUTF();
            System.out.println("Server: "+line+"\n");
            if(!line.substring(0, 3).equals("+OK")){
                error();
            }
            int unread=Integer.parseInt(line.substring(4));
            javax.swing.JOptionPane.showMessageDialog(null, unread+" messages", "Message", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            
            for(i=1;i<=unread;i++){
                
                line="RETR "+i;
                System.out.println("Client: "+line+"\n");
                oup.writeUTF(line);
                line=inp.readUTF();
                System.out.println("Server: "+line+"\n");
                if(!line.equals("+OK")){
                    error();
                }
                line=inp.readUTF();
                System.out.println("Server: "+line+"\n");
                String lines[]=line.split("\n");
                Message new_msg=new Message();
               
                for(String t: lines){
                    
                    if(t.startsWith("FROM: ")){
                        new_msg.setSender(t.substring(6));
                    }
                    else if(t.startsWith("TO: ")){
                        ArrayList<String> receivers=new ArrayList<>();
                        t=t.substring(4);
                        for(String r: t.split(";"))
                            receivers.add(r);
                        new_msg.setReceiver_list(receivers);
                    }
                }
                new_msg.setMessage(line);
                cmain.save_msg(new_msg);
                line="DELE "+i;
                System.out.println("Client: "+line+"\n");
                oup.writeUTF(line);
                line=inp.readUTF();
                System.out.println("Server: "+line+"\n");
                if(!line.equals("+OK")){
                    error();
                }
            }
            oup.writeUTF("QUIT");
            inp.close();
            oup.close();
            socket.close();
        }
        catch(Exception e){
            
            System.out.println("in pop3: "+e);
        }
    }
}
