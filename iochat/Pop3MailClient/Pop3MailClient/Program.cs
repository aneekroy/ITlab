// POP3 Email Client Test harness (Main)
// =====================================
//
// copyright by Peter Huber, Singapore, 2006
// this code is provided as is, bugs are probable, free for any use, no responsiblity accepted :-)
//
// based on POP3 Client as a C# Class, by Bill Dean, http://www.codeproject.com/csharp/Pop3client.asp 
// based on Retrieve Mail From a POP3 Server Using C#, by Agus Kurniawan, http://www.codeproject.com/csharp/popapp.asp 
// based on Post Office Protocol - Version 3, http://www.ietf.org/rfc/rfc1939.txt

using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Mail;
using System.Text;

namespace Pop3 {
  class Program {
    static void Main(string[] args) {
      Console.WriteLine("POP3 Mail Client Demo");
      Console.WriteLine("=====================");
      Console.WriteLine();
      try {
        //prepare pop client
        // TODO: Replace username and password with your own credentials.
        Pop3.Pop3MailClient DemoClient = new Pop3.Pop3MailClient("pop.gmail.com", 995, true, "Username@gmail.com", "password");
        DemoClient.IsAutoReconnect = true;

        //remove the following line if no tracing is needed
        DemoClient.Trace += new Pop3.TraceHandler(Console.WriteLine);
        DemoClient.ReadTimeout = 60000; //give pop server 60 seconds to answer

        //establish connection
        DemoClient.Connect();

        //get mailbox statistics
        int NumberOfMails, MailboxSize;
        DemoClient.GetMailboxStats(out NumberOfMails, out MailboxSize);

        //get a list of mails
        List<int> EmailIds;
        DemoClient.GetEmailIdList(out EmailIds);

        //get a list of unique mail ids
        List<Pop3.EmailUid> EmailUids;
        DemoClient.GetUniqueEmailIdList(out EmailUids);

        //get email size
        DemoClient.GetEmailSize(1);

        //get email
        string Email;
        DemoClient.GetRawEmail(1, out Email);
        
        //delete email
        DemoClient.DeleteEmail(1);

        //get a list of mails
        List<int> EmailIds2;
        DemoClient.GetEmailIdList(out EmailIds2);

        //undelete all emails
        DemoClient.UndeleteAllEmails();

        //ping server
        DemoClient.NOOP();

        //test some error conditions
        DemoClient.GetRawEmail(1000000, out Email);
        DemoClient.DeleteEmail(1000000);
        

        //close connection
        DemoClient.Disconnect();

      } catch (Exception ex) {
        Console.WriteLine();
        Console.WriteLine("Run Time Error Occured:");
        Console.WriteLine(ex.Message);
        Console.WriteLine(ex.StackTrace);
      }

        Console.WriteLine();
        Console.WriteLine("======== Press Enter to end program");
        Console.ReadLine();
    }
  }
}
