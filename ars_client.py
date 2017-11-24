import sys
from socket import *

arp_cache = {}

if __name__ == '__main__':
    while True:
        choice = input('> ').split(' ')
        if choice[0] == 'getmacaddr':
            ip = choice[1]       
            client_socket = socket(AF_INET, SOCK_DGRAM)
            client_socket.setsockopt(SOL_SOCKET, SO_BROADCAST, 1)
            client_socket.settimeout(2)
            client_socket.sendto(bytes(ip.encode('ascii')), ('<broadcast>', 2000))
            try:
                response, x = client_socket.recvfrom(4096)
                response = str(response)[2:-1]
                client_socket.close()
            except timeout as e:
                print('timeout')
            else:
                print(response, x[0])
                arp_cache[x[0]] = response
        elif choice[0] == 'showarpcache':
            for ip in arp_cache:
                print((ip, arp_cache[ip]))
        else:
            print('Error in parsing input')
            
