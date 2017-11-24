import socket
import time
import os
import sys

from uuid import getnode as get_mac

def bind_and_listen(server_socket, port_no):
    host_name = ''

    server_socket.bind((host_name, port_no))


    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(('8.8.8.8', 80))
    deviceIP = s.getsockname()[0]

    print("Successfully bound socket to host: %s, port: %d" % (host_name, port_no))

    while True:
        command, addr = server_socket.recvfrom(4096)
        command = command.decode('ascii')
        print(command)

        # ':'.join(format(s, '02x') for s in bytes.fromhex('00163e2fbab7'))

        deviceMAC = str(hex(get_mac()))[2:]
        deviceMAC = ':'.join(format(s, '02x') for s in bytes.fromhex(deviceMAC))

        if command == deviceIP:
            server_socket.sendto(bytes(deviceMAC.encode('ascii')), addr)


if __name__ == '__main__':
    if len(sys.argv) != 2:
        print("Provide port number!")
        sys.exit(-1)

    port_no = int(sys.argv[1])

    server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

    try:
        bind_and_listen(server_socket, port_no)
    finally:
        server_socket.close()
        print("Successfully closed socket!")
