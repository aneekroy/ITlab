import smtplib
import time
import imaplib
import email
from Tkinter import *
import tkMessageBox
import tkFont
        
class MailBox:

    def __init__(self, master):
        self.master = master
        master.title("Email-Client")
        master.configure(background = "papaya whip")
        self.subject_label = Label(master, text="Sub:", font=("Comic Sans MS", 10), bg = "cornflower blue")
        self.inbox_label = Label(master, text="Inbox", font=("Comic Sans MS", 10), bg = "cornflower blue")
        self.to_label = Label(master, text="To:", font=("Comic Sans MS", 10), bg = "cornflower blue")
        self.user_label = Label(master, text="Mail Id:", font=("Comic Sans MS", 10), bg = "cornflower blue")
        self.pswd_label = Label(master, text="Password:", font=("Comic Sans MS", 10), bg = "cornflower blue")

        self.to_entry = Entry(master, font=("Comic Sans MS", 10))
        self.sub_entry = Entry(master, font=("Comic Sans MS", 10))
        self.user_entry = Entry(master, font=("Comic Sans MS", 10))
        self.pswd_entry = Entry(master, show = "*")
        self.mail_text = Text(master, highlightbackground="black", font=("Comic Sans MS", 10), bg="SkyBlue1")
        self.inbox_text = Text(master, highlightbackground="black", font=("Comic Sans MS", 10), bg="SkyBlue1")
        
        self.user_input = self.user_entry.get()
        self.pswd_input = self.pswd_entry.get()
        self.to_input = self.to_entry.get()
        self.subject_input = self.sub_entry.get()
        self.mail_input = self.mail_text.get("1.0",'end-1c')

        #custom_font = tkFont.Font(family=("Comic Sans MS", 10))
        self.view_button = Button(master, text="Show mails", command=self.read_email_from_gmail, highlightbackground="black", bg = "orange", font = ("Comic Sans MS", 10))
        self.refresh_button = Button(master, text="Refresh", command=self.refresh, highlightbackground="black", bg = "orange", font = ("Comic Sans MS", 10))
        self.send_button = Button(master, text="Send", command=self.send_email_to_gmail, highlightbackground="black", bg = "orange", font = ("Comic Sans MS", 10))

        # LAYOUT

        self.user_label.grid(row=0, column=0, sticky=E)
        self.pswd_label.grid(row=1, column=0, sticky=E)
        self.to_label.grid(row=2, column=1, sticky=E)
        self.subject_label.grid(row=3, column=1, sticky=E)
        self.inbox_label.grid(row=3, column=0, sticky=E)

        self.to_entry.grid(row=2, column=2, columnspan=1, sticky=W+E)
        self.sub_entry.grid(row=3, column=2, columnspan=1, sticky=W+E)
        self.user_entry.grid(row=0, column=1, columnspan=2, sticky=W+E)
        self.pswd_entry.grid(row=1, column=1, columnspan=2, sticky=W+E)

        self.mail_text.grid(row=4, column=2, columnspan=1, sticky=W+E)
        self.inbox_text.grid(row=4, column=0, columnspan=2, sticky=W+E)
        

        self.view_button.grid(row=5, column=0, sticky=W+E)
        self.refresh_button.grid(row=5, column=1, sticky=W+E)
        self.send_button.grid(row=5, column=2, sticky=W+E)

        


    def send_email_to_gmail(self):
        
        if not self.user_entry.get():
            tkMessageBox.showerror("Error","Gmail Id field can't be empty")
        if not self.pswd_entry.get():
            tkMessageBox.showerror("Error","Password field can't be empty")
        if not self.to_entry.get():
            tkMessageBox.showerror("Error","Recipient field can't be empty")
                    
        self.user_input = self.user_entry.get()
        self.pswd_input = self.pswd_entry.get()
        self.to_input = self.to_entry.get()
        self.subject_input = self.sub_entry.get()
        self.mail_input = self.mail_text.get("1.0",'end-1c')
        
        gmail_user = self.user_input
        gmail_password = self.pswd_input
        sent_from = gmail_user  
        
        to = self.to_input.split()
        #to.append(self.to_input)
        subject = self.subject_input
        body = self.mail_input
        
        email_text = """
        From: %s  
        To: %s  
        Subject: %s
        %s
        """ % (sent_from, ", ".join(to), subject, body)
        try:  
            server = smtplib.SMTP_SSL('smtp.gmail.com', 465)
            server.ehlo()
            server.login(gmail_user, gmail_password)
            server.sendmail(sent_from, to, email_text)
            server.close()

            print ("Email sent successfully")
            tkMessageBox.showinfo("Notification","Your message has been sent")
        except:  
            tkMessageBox.showinfo("Notification","Oops! something went wrong...")
            print ("Error")
                
            
        self.user_entry.delete(0, 'end')
        self.pswd_entry.delete(0, 'end')
        self.to_entry.delete(0, 'end')
        self.sub_entry.delete(0, 'end')    
        self.mail_text.delete('1.0', END)    
        

    def read_email_from_gmail(self):
    
        if not self.user_entry.get():
            tkMessageBox.showerror("Error","Gmail Id field can't be empty")
        if not self.pswd_entry.get():
            tkMessageBox.showerror("Error","Password field can't be empty")
            
        self.user_input = self.user_entry.get()
        self.pswd_input = self.pswd_entry.get()
        gmail_user = self.user_input
        gmail_password = self.pswd_input
        
        try:
            mail = imaplib.IMAP4_SSL('imap.gmail.com', 993)
            mail.login(gmail_user,gmail_password)
            mail.select('inbox')

            type, data = mail.search(None, 'ALL')
            mail_ids = data[0]

            id_list = mail_ids.split()   
            first_email_id = int(id_list[0])
            latest_email_id = int(id_list[-1])
            mail_count = 5 
            inbox_content = ''


            for i in range(latest_email_id,first_email_id, -1):
                typ, data = mail.fetch(i, '(RFC822)' )   
                try:
                    for response_part in data:
                        if mail_count < 1:
                            break;
                        if isinstance(response_part, tuple):
                            msg = email.message_from_string(response_part[1])
                            email_subject = msg['subject']
                            email_from = msg['from']
                            print 'From : ' + email_from + '\n'
                            print 'Subject : ' + email_subject + '\n'
                            inbox_content += 'From : ' + email_from + '\n'
                            inbox_content += 'Subject : ' + email_subject + '\n'
                            mail_count -= 1
                    if mail_count < 1:
                        break;
                except:
                    pass           
                    
                    
            self.inbox_text.insert('1.0', inbox_content)
            inbox_content = ''   
            tkMessageBox.showinfo("Notification","5 latest messages displayed")                      

        except Exception, e:
            print str(e)
                
            

    def refresh(self):
        self.user_entry.delete(0, 'end')
        self.pswd_entry.delete(0, 'end')
        self.to_entry.delete(0, 'end')
        self.sub_entry.delete(0, 'end')    
        self.mail_text.delete('1.0', END)
        self.inbox_text.delete('1.0', END)
        
        
        
root = Tk()
my_gui = MailBox(root)
root.mainloop()        
        
        
        
        
            
