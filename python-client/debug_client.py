#!/usr/bin/env python3
"""
Debug client - Simple TCP client to test server connection
"""

import socket
import json
import threading
import time

def receive_messages(sock):
    """Receive messages from server"""
    while True:
        try:
            data = sock.recv(4096).decode('utf-8')
            if not data:
                break
            print(f"[SERVER]: {data}")
        except Exception as e:
            print(f"[ERROR] Receive: {e}")
            break

def main():
    print("=== WordBrain2 Debug Client ===")
    print("Connecting to localhost:5555...")
    
    try:
        # Connect
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect(('localhost', 5555))
        print("[OK] Connected!")
        
        # Start receive thread
        thread = threading.Thread(target=receive_messages, args=(sock,))
        thread.daemon = True
        thread.start()
        
        # Wait for initial message
        time.sleep(1)
        
        # Test messages
        print("\n--- Testing CREATE_ROOM ---")
        message = {
            "type": "CREATE_ROOM",
            "messageType": "CREATE_ROOM",
            "data": {
                "playerName": "TestPlayer",
                "hostName": "TestPlayer",
                "topic": "animals",
                "levelCount": 5,
                "levelDuration": 30,
                "maxPlayers": 4
            }
        }
        
        json_str = json.dumps(message)
        print(f"[SEND]: {json_str}")
        sock.send(json_str.encode('utf-8'))
        
        # Wait for response
        time.sleep(2)
        
        # Keep connection alive
        print("\nPress Enter to send test messages, or type 'quit' to exit")
        while True:
            cmd = input("> ")
            if cmd == 'quit':
                break
            elif cmd == 'join':
                room_code = input("Room code: ")
                msg = {
                    "type": "JOIN_ROOM",
                    "messageType": "JOIN_ROOM",
                    "data": {
                        "roomCode": room_code,
                        "playerName": "Player2"
                    }
                }
                sock.send(json.dumps(msg).encode('utf-8'))
                print(f"[SEND]: JOIN_ROOM {room_code}")
            elif cmd == 'ready':
                msg = {
                    "type": "PLAYER_READY",
                    "messageType": "PLAYER_READY",
                    "data": {"ready": True}
                }
                sock.send(json.dumps(msg).encode('utf-8'))
                print("[SEND]: PLAYER_READY")
            elif cmd == 'start':
                msg = {
                    "type": "START_GAME",
                    "messageType": "START_GAME",
                    "data": {}
                }
                sock.send(json.dumps(msg).encode('utf-8'))
                print("[SEND]: START_GAME")
        
        sock.close()
        print("Disconnected.")
        
    except Exception as e:
        print(f"[ERROR]: {e}")

if __name__ == '__main__':
    main()