import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*; 

public class Server{
    
    private HashMap<String, String> dns_list;
    private HashMap<String, Integer> login_socket;
    private HashMap<String, Integer> send_socket;
    private HashMap<String,Integer> pop3_recv=new HashMap<>();
    private String domain_name;
    private Socket socket=null;
    private ServerSocket serversocket=null;
    private static int PORT;
    
    private HashMap<String, ArrayList<Message> > inbox;

    
    Server(String dname){
        
        domain_name=dname;
        dns_list=new HashMap<>();
        login_socket=new HashMap<>();
        send_socket=new HashMap<>();
        inbox=new HashMap<>();
        
        dns_list.put("gmail.com", "localhost");
        dns_list.put("sinju.com", "localhost");
        login_socket.put("gmail.com", 5000);
        login_socket.put("sinju.com", 6000);
        send_socket.put("gmail.com", 2500);
        send_socket.put("sinju.com", 2600);
        pop3_recv.put("gmail.com", 3995);
        pop3_recv.put("sinju.com", 3996);
        LoginServer login=new LoginServer(login_socket.get(dname), this);
        Thread login_thread=new Thread(login);
        login_thread.start();
        Pop3Server pserver=new Pop3Server(domain_name, pop3_recv.get(dname), this);
        Thread recv_server=new Thread(pserver);
        recv_server.start();
        try{
	    serversocket=new ServerSocket(send_socket.get(dname));
	    System.out.println("Stmp server "+domain_name+" started");
            while(true){

	        socket=serversocket.accept();
	        System.out.println("Client accepted");
	        ProcessRequest pRequest= new ProcessRequest(domain_name, socket, this);
	        Thread t = new Thread(pRequest);
	        t.start();	
	    }
	}    
	catch(Exception e){
	    System.out.println("thread creation error: "+e);
	        
	}         
    }
    
    public void add_new_user(String id){
        
        if(!inbox.containsKey(id))
            inbox.put(id, new ArrayList<>());
       
    }
    
    public boolean ret_client_present(String id){
    
        return inbox.containsKey(id);
    }
    
    public ArrayList<Message> ret_msg(String id){
        
        return inbox.get(id);
    }
    
    public void del_msg(String id, int i){
        
        ArrayList<Message> list=inbox.get(id);
        list.remove(i);
        inbox.put(id, list);
    }
    
    public String ret_dns_res(String name){
        
        return dns_list.get(name);
    }
    
    public int ret_send_sock(String name){
        
        return send_socket.get(name);
    }
    synchronized public int putmsg(String client, Message message){
        
        System.out.println("Putting in inbox");
        System.out.println(client);
        if(inbox.containsKey(client)){
            
            ArrayList<Message> temp=inbox.get(client);
            temp.add(message);
            inbox.put(client, temp);
          
            return 1;
        }
        return 0;
    }
    
    
    
    public static void main(String args[]){
        
        if(args.length!=1){
            System.out.println("USAGE domain_name");
        }
        Server server=new Server(args[0]);
    }
}

class Pop3Server implements Runnable{
    
    private Server server;
    private int port;
    String dname;
    Pop3Server(String dname, int port, Server ob){
        
        this.dname=dname;
        this.port=port;
        server=ob;
    }
    public void run(){
        
        try{
            ServerSocket ssocket=new ServerSocket(port);
            while(true){

                Socket socket=ssocket.accept();
                ProcessReceive preceive=new ProcessReceive(dname, socket, server);
                Thread t=new Thread(preceive);
                t.start();
            }
        }
        catch(IOException e){
            
            System.out.println("in pop3 "+e);
        }
    }
}

class LoginServer implements Runnable{
    
    private Server server;
    private int port;
    private String id;
    LoginServer(int port, Server ob){
        
        this.port=port;
        server=ob;
    }
    public void run(){
        
        try{
            ServerSocket ssocket=new ServerSocket(port);
            while(true){

                Socket socket=ssocket.accept();
                DataInputStream inp=new DataInputStream(socket.getInputStream());
                DataOutputStream oup=new DataOutputStream(socket.getOutputStream());
                id=inp.readUTF();
                if(id.charAt(0)=='0')
                    server.add_new_user(id.substring(1));
                inp.close();
                socket.close();
            }
        }
        catch(IOException e){
            
            System.out.println("in login: "+e);
        }
    }
    
}

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

class ProcessReceive implements Runnable{
    
    private DataInputStream inp=null;
    private DataOutputStream oup=null;
    private Socket socket=null;
    private Server server=null;
    private String domain_name;
    ProcessReceive(String dname, Socket s, Server ob){
        
        domain_name=dname;
        socket=s;
        server=ob;
    }
    public void run(){
        
        System.out.println("pop3 client connected");
        try{
            inp=new DataInputStream(socket.getInputStream());
            oup=new DataOutputStream(socket.getOutputStream());
	}
	catch(Exception e){
            System.out.println(e);
            return;
	}
        try{
            String line="", id;
            String tokens[];
            line="+OK pop3 server ready";
            oup.writeUTF(line);
            line=inp.readUTF();
            System.out.println(line);
            tokens=line.split(" ");
            if(tokens.length!=2 && !tokens[0].equals("APOP")){
                
                oup.writeUTF("-ER Syntax error");
                return;
            }
            id=tokens[1];
            if(!server.ret_client_present(id)){
                
                oup.writeUTF("-ER client not present");
                return;
            }
            oup.writeUTF("+OK client present");
            ArrayList<Message> msg_list=server.ret_msg(id);
            int unread;
            unread=msg_list.size();
            
            line=inp.readUTF();
            System.out.println(line);
            if(!line.equals("STAT")){
                
                oup.writeUTF("-ER Command not recognized");
                return;
            }
            oup.writeUTF("+OK "+unread);
            
            line=inp.readUTF();
            while(!line.equals("QUIT")){
                
                System.out.println(line);
                tokens=line.split(" ");
                if(tokens.length!=2 && !tokens[0].equals("RETR")){
                    oup.writeUTF("-ER Command not recognized");
                    return;                    
                }
                int num;
                num=Integer.parseInt(tokens[1])-1;
                if(num<0 || num>unread){
                    
                    oup.writeUTF("-ER message number invalid");
                    return;
                }
                oup.writeUTF("+OK");
                oup.writeUTF(msg_list.get(0).getMessage());
                                
                line=inp.readUTF();
                System.out.println(line);
                tokens=line.split(" ");
                if(tokens.length!=2 && !tokens[0].equals("DELE")){
                    oup.writeUTF("-ER Command not recognized");
                    return;                    
                }
                num=Integer.parseInt(tokens[1])-1;
                if(num<0 || num>unread){
                    
                    oup.writeUTF("-ER message number invalid");
                    return;
                }
                server.del_msg(id, 0);
                oup.writeUTF("+OK");
                
                line=inp.readUTF();
            }
            inp.close();
            oup.close();
            socket.close();
            return;
            
        }
        catch(Exception e){
            
            System.out.println("in pop3: "+e);
            e.printStackTrace();
        }
    }
}

class ProcessRequest implements Runnable{


    private DataInputStream inp=null;
    private DataOutputStream oup=null;
    private Socket socket=null;
    private Server server=null;
    private String domain_name;
    private String client_domain;
    private Message message;
    private char end_data_char='.';

    ProcessRequest(String dname, Socket s, Server ob){

        domain_name=dname;
	socket=s;
	server=ob;
        message=new Message();
	message.setReceiver_list(new ArrayList<>()); 
    }
    public void send_msg(String msg){
		
        try{
            oup.writeUTF(msg);
	}
	catch(IOException e){
            System.out.println("transmit error"+e);
	}	
    }
    public String recv_msg(){
        
        String line="";
        try{
            line=inp.readUTF();
        }
        catch(IOException e){

            System.out.println("receiving error "+e);
        }
        return line.trim();
    }    
    public void run(){

	System.out.println("listening to clients");
	try{
            inp=new DataInputStream(socket.getInputStream());
            oup=new DataOutputStream(socket.getOutputStream());
	}
	catch(Exception e){
            System.out.println(e);
            return;
	}	
        String line = "";
        String[] tokens;
        send_msg("220 "+domain_name+" "+"SMTP");
        /*HELO*/
        line=recv_msg();
        System.out.println(line);
        tokens=line.split(" ");
        if(tokens.length==0 || !tokens[0].equals("HELO")){
            send_msg("500 Syntaxerror command not recognised");
            return;
        }    
        if(tokens.length!=2){
            send_msg("501 Syntaxerror invalid parameters");
            return;
        }    
        client_domain=tokens[1];
        send_msg("250 "+domain_name+" connected");
        /*MAIL, FROM*/  
        line=recv_msg();
        System.out.println(line);
        tokens=line.split(" ");
        if(tokens.length==0 || !tokens[0].equals("MAIL")){
            send_msg("500 Syntaxerror command not recognised");
            return;
        }    
        System.out.println(tokens[1].substring(5, 10));
        if(tokens.length!=2 || !tokens[1].substring(0, 5).equals("FROM:")){
            send_msg("501 Syntaxerror invalid parameters");
            return;
        }    
        message.setSender(tokens[1].substring(5));
        send_msg("250 OK");
        /*RCPT, TO*/
        line=recv_msg();
        System.out.println(line);
        tokens=line.split(" ");
        if(tokens.length==0 || !tokens[0].equals("RCPT")){
            send_msg("500 Syntaxerror command not recognised");
            return;
        }    
        if(tokens.length!=2 || !tokens[1].substring(0, 3).equals("TO:")){
            send_msg("501 Syntaxerror invalid parameters");
            return;
        }    
        message.add_Reciever(tokens[1].substring(3));
        send_msg("250 OK");
        
        line=recv_msg();
        System.out.println(line);
        tokens=line.split(" ");
        if(tokens.length==0){
            send_msg("500 Syntaxerror command not recognised");
            return;
        }
        while(tokens[0].equals("RCPT")){
           
            if(tokens.length!=2 || !tokens[1].substring(0, 3).equals("TO:")){
                send_msg("501 Syntaxerror invalid parameters");
                return;
            }    
            message.add_Reciever(tokens[1].substring(3));
            send_msg("250 OK");
            line=recv_msg();
            tokens=line.split(" ");
            if(tokens.length==0){
                send_msg("500 Syntaxerror command not recognised");
                return;
            }
        } 
        
        /*DATA*/
        if(tokens.length==0 || !tokens[0].equals("DATA")){
            send_msg("500 Syntaxerror command not recognised");
            return;
        }
        send_msg("354 End data with <CR><LF>"+end_data_char+"<CR><LF>");
        String msg="";
        while(true){
            line=recv_msg();
            System.out.println(line);
            if(line.startsWith(String.valueOf(end_data_char)+end_data_char))
                line=line.substring(1);
            if(line.startsWith(String.valueOf(end_data_char)))
                break;
            msg+=line+"\n";
        }
        message.setMessage(msg);
        send_msg("250 OK: queued for delivery");
        line=recv_msg();
        System.out.println(line);
        tokens=line.split(" ");
        if(tokens.length==0 || !tokens[0].equals("QUIT")){
            send_msg("500 Syntaxerror command not recognised");
            return;
        }
        send_msg("221 End");
        try{
            inp.close();
            oup.close();
            socket.close();
        }    
        catch(Exception e){
            
            System.out.println("Error in closing sockets");
        }
        int i;
        String recv_name;
        ArrayList<String> receivers_list=message.getReceiver_list();
        for(i=0;i<message.getReceiver_list().size();i++){
            
            recv_name=receivers_list.get(i);
            int j=recv_name.indexOf("@");
            if(domain_name.equals(recv_name.substring(j+1))==true){
                
                int success=server.putmsg(recv_name, message);
                if(success==0){
                    System.out.println("no such client");
                                        
                }
            }
            else{
                
                System.out.println("Hopping to next server");
                send_mail(recv_name, message);
            }
        }
    }
    
    public void send_mail(String recv_name, Message message){
        
        try{
           
            int i=recv_name.indexOf("@");
            String client_domain=recv_name.substring(i+1);
            String ip=server.ret_dns_res(client_domain);
            Socket socket=new Socket(ip, server.ret_send_sock(client_domain));
            DataInputStream inp=new DataInputStream(socket.getInputStream());
            DataOutputStream oup=new DataOutputStream(socket.getOutputStream());
            String line=inp.readUTF();
            line="HELO "+domain_name;
            oup.writeUTF(line);
            line=inp.readUTF();
            if(Integer.parseInt(line.substring(0, 3))!=250)             
                return;
            line="MAIL FROM:"+message.getSender();
            oup.writeUTF(line);
            line=inp.readUTF();
            if(Integer.parseInt(line.substring(0, 3))!=250)
                return; 
            line="RCPT TO:"+recv_name;
            oup.writeUTF(line);
            line=inp.readUTF();
            if(Integer.parseInt(line.substring(0, 3))!=250)             
                return;
            line="DATA";
            oup.writeUTF(line);
            line=inp.readUTF();
            if(Integer.parseInt(line.substring(0, 3))!=354)             
                return;
            i=line.indexOf("<CR><LF>");
            char data_end_char=line.charAt(i+8);
            /*sending data*/
            String lines[]=message.getMessage().split("\n");
            for(String t: lines){
                
                if(t.startsWith(String.valueOf(data_end_char)))
                    t=data_end_char+t;
                t=t+'\n';
                oup.writeUTF(t);
            }
            line=String.valueOf(data_end_char)+'\n';
            oup.writeUTF(line);
            line=inp.readUTF();
            if(Integer.parseInt(line.substring(0, 3))!=250)             
                return;
            line="QUIT";
            oup.writeUTF(line);
            line=inp.readUTF();
            if(Integer.parseInt(line.substring(0, 3))!=221)             
                return;
            inp.close();
            oup.close();
            socket.close();
           
       }
       catch(Exception e){
           
           System.out.println(e);
       }
    }
}    