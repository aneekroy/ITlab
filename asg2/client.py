import socket
import sys


sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM) #creating an INET, STREAMing socket

host = 'localhost'
port = 2000
server_address = (host, port)

print ("Connecting to %s Port %s" % server_address, file=sys.stderr)
sock.connect(server_address)


while True:
    msg2 = str(input("Are you a manager?(y/n): "))	
    sock.sendall(msg2.encode())
    result2 = sock.recv(1000)
    if(result2.decode()=='y'):
        msg1 = str(input("Please input your password: "))
        sock.sendall(msg1.encode())
        result1 = sock.recv(1000)
        if (result1.decode() == 'Welcome manager'):
            print (result1.decode())
            break
        else:
            print (result1.decode())
            pass
    else:
        print (result2.decode())
        break
    
try:
    
   
	msg = str(input("Enter your command: "))    
	while msg!='q': #quit from the client
		
		sock.sendall(msg.encode())
		result = sock.recv(1000)
		print (str(result.decode()))
                #print (str(result))
		msg = str(input("Enter your command: "))
		
   
finally:
    print ('closing socket', file=sys.stderr)
    sock.close()
