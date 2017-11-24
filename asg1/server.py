import socket
import sys
import os
import subprocess



sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

host = socket.gethostbyname('localhost')
port = 2000
server_address = (host, port)

print  ("Starting up on :%s Port :%s" % server_address, file=sys.stderr)
sock.bind(server_address)

sock.listen(0) #listen for one connection at a time

while True:
    
    print ("Waiting For a Secured Connection", file=sys.stderr)
    c, client_address = sock.accept()

    try:
        print ("Connection From the client: ", client_address, file=sys.stderr)

       
        while True:
            data = c.recv(16) #recieve 16 bytes of data
            # if (data == 'ls -l'.encode()):
            #     #os.system('ls -l')
            #     print ("From the connected user: " + str(subprocess.check_output("ls -l", shell=True))) 
            #     output = subprocess.check_output("ls -l", shell=True)
            # elif (data == 'pwd'.encode()):
            #    	#os.system('pwd')
            #     print ("From the connected user: " + str(subprocess.check_output("pwd", shell=True)))
            #     output = subprocess.check_output("pwd", shell=True)
            # elif (data == 'date'.encode()):
            #    	#os.system('date')
            #     print ("From the connected user: " + str(subprocess.check_output("date", shell=True)))
            #     output = subprocess.check_output("date", shell=True)
            if (data.decode() == '0'):
                #os.system('ls -l')
                print ("From the connected user:" + str(subprocess.check_output("date", shell=True).decode())) 
                output = subprocess.check_output("date", shell=True)
            elif (data.decode() == '1'):
                #os.system('pwd')
                print ("From the connected user:" + str(subprocess.check_output("whoami", shell=True).decode()))
                output = subprocess.check_output("whoami", shell=True)
            elif (data.decode() == '2'):
                #os.system('date')
                print ("From the connected user:" + str(subprocess.check_output("ls", shell=True).decode()))
                output = subprocess.check_output("ls", shell=True)
            elif (data.decode() == '3'):
                #os.system('date')
                print ("From the connected user: " + str(subprocess.check_output("pwd", shell=True).decode()))
                output = subprocess.check_output("pwd", shell=True)
            else:

                output = "Wrong".encode()
           
            c.send(output)
    finally:
        print ("Closing connection and the server", file=sys.stderr)
        c.close()
        
