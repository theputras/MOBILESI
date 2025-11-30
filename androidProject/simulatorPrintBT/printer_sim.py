import bluetooth

server_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)

port = 1
server_socket.bind(("", port))
server_socket.listen(1)

# Mendapatkan info device
uuid = "00001101-0000-1000-8000-00805F9B34FB" # UUID standar Serial Port (SPP)

# Advertise service supaya HP bisa "melihat" service ini
bluetooth.advertise_service(server_socket, "Thermal Printer Sim",
                            service_id=uuid,
                            service_classes=[uuid, bluetooth.SERIAL_PORT_CLASS],
                            profiles=[bluetooth.SERIAL_PORT_PROFILE])

print(f"Waiting for connection on RFCOMM channel {port}...")

try:
    client_socket, client_info = server_socket.accept()
    print(f"Accepted connection from {client_info}")

    while True:
        data = client_socket.recv(1024)
        if not data:
            break
        
        # Decode bytes ke string (karena printer kirimnya bytes)
        try:
            text = data.decode('utf-8')
            print(f"PRINTING: {text}")
            
            # --- DISINI LOGIKA KIRIM KE WEBSITE ---
            # Lu bisa pake requests atau websocket client buat lempar
            # text ini ke Node.js atau Flask server lu.
            
        except UnicodeDecodeError:
            print(f"PRINTING (RAW BYTES): {data}")

except OSError:
    pass

print("Disconnected.")
client_socket.close()
server_socket.close()