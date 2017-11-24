import socket
import sys


sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM) #creating an INET, STREAMing socket

host = 'localhost'
# host = '192.168.0.160'
port = 2000
server_address = (host, port)

#print >>sys.stderr, 'Connecting to server (%s Port %s)' % server_address
sock.connect(server_address)

try:
    
   
	msg = str(input("Enter command: "))    
	while msg!='q': #quit from the client , close the socket
		
		sock.sendall(msg.encode() )
		result = sock.recv(2000)
		print( result.decode() )
		msg = str(input("Enter command: "))
		
   
finally:
    #print >>sys.stderr, 'closing socket'
    sock.close()
