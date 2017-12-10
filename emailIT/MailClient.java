/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mailclient;

/**
 *
 * @author souvik
 */
public class MailClient {

    /**
     * @param args the command line arguments
     */
    boolean flag=false;
    String ip, id;
    int port;
    public static void main(String[] args) {
        
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
         try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
           //</editor-fold>
           
        MailClient mclient=new MailClient();
        Login mlogin=new Login(mclient);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
               mlogin.setVisible(true);
            }
        });
        mclient.start_connection();
        
    }
    public void get_data(String id, String ip, int port){
        this.ip=ip;
        this.id=id;
        this.port=port;
        flag=true;
        
    }
    public void start_connection(){
        
        while(flag==false){
            
            try{
                Thread.sleep(100);
            }
            catch(InterruptedException e){
                System.out.print(e);
            }
        }
       
        Client client=new Client(id, ip, port);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                client.setVisible(true);
            }
        });
    }
}
