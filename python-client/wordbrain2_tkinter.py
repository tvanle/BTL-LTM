#!/usr/bin/env python3
"""
WordBrain2 Client - Tkinter Version (No additional dependencies)
TCP Socket connection to Java server on port 5555
"""

import tkinter as tk
from tkinter import ttk, messagebox
import socket
import threading
import json
from datetime import datetime

class WordBrain2TkinterClient:
    def __init__(self, root):
        self.root = root
        self.root.title("WordBrain2 Multiplayer - Tkinter Client")
        self.root.geometry("900x700")
        
        # TCP connection
        self.socket = None
        self.connected = False
        
        # Game state
        self.player_info = {}
        self.room_info = {}
        self.selected_cells = []
        self.current_word = ""
        
        # Configure styles
        self.setup_styles()
        
        # Create UI
        self.create_ui()
        
    def setup_styles(self):
        """Configure UI styles"""
        self.root.configure(bg='#2c3e50')
        
        # Style configuration
        style = ttk.Style()
        style.theme_use('clam')
        
        # Configure button style
        style.configure('Game.TButton',
                       background='#3498db',
                       foreground='white',
                       borderwidth=1,
                       focuscolor='none',
                       font=('Arial', 12))
        style.map('Game.TButton',
                 background=[('active', '#2980b9')])
        
    def create_ui(self):
        """Create the main UI"""
        # Top frame - Connection
        top_frame = tk.Frame(self.root, bg='#34495e', height=60)
        top_frame.pack(fill='x', padx=5, pady=5)
        
        # Connection status
        self.status_label = tk.Label(top_frame, text="Status: Disconnected", 
                                     fg='#e74c3c', bg='#34495e', font=('Arial', 12))
        self.status_label.pack(side='left', padx=10)
        
        # Connect button
        self.connect_btn = tk.Button(top_frame, text="Connect", 
                                     command=self.connect_to_server,
                                     bg='#27ae60', fg='white', font=('Arial', 12))
        self.connect_btn.pack(side='left', padx=5)
        
        # Room controls
        tk.Label(top_frame, text="Name:", bg='#34495e', fg='white').pack(side='left', padx=5)
        self.name_entry = tk.Entry(top_frame, width=15)
        self.name_entry.insert(0, "Player1")
        self.name_entry.pack(side='left', padx=5)
        
        tk.Label(top_frame, text="Room:", bg='#34495e', fg='white').pack(side='left', padx=5)
        self.room_entry = tk.Entry(top_frame, width=10)
        self.room_entry.pack(side='left', padx=5)
        
        self.create_btn = tk.Button(top_frame, text="Create Room", 
                                    command=self.create_room,
                                    bg='#3498db', fg='white', state='disabled')
        self.create_btn.pack(side='left', padx=5)
        
        self.join_btn = tk.Button(top_frame, text="Join Room", 
                                  command=self.join_room,
                                  bg='#3498db', fg='white', state='disabled')
        self.join_btn.pack(side='left', padx=5)
        
        self.ready_btn = tk.Button(top_frame, text="Ready", 
                                   command=self.toggle_ready,
                                   bg='#f39c12', fg='white', state='disabled')
        self.ready_btn.pack(side='left', padx=5)
        
        self.start_btn = tk.Button(top_frame, text="Start Game", 
                                   command=self.start_game,
                                   bg='#e74c3c', fg='white', state='disabled')
        self.start_btn.pack(side='left', padx=5)
        
        # Main content frame
        content_frame = tk.Frame(self.root, bg='#2c3e50')
        content_frame.pack(fill='both', expand=True, padx=5, pady=5)
        
        # Left panel - Game grid
        left_panel = tk.Frame(content_frame, bg='#2c3e50')
        left_panel.pack(side='left', fill='both', expand=True)
        
        # Grid frame
        self.grid_frame = tk.Frame(left_panel, bg='#34495e')
        self.grid_frame.pack(pady=20)
        
        # Create 5x5 grid
        self.grid_buttons = []
        for i in range(5):
            row = []
            for j in range(5):
                btn = tk.Button(self.grid_frame, text='', 
                               width=6, height=3,
                               font=('Arial', 20, 'bold'),
                               bg='#3498db', fg='white',
                               command=lambda r=i, c=j: self.cell_clicked(r, c))
                btn.grid(row=i, column=j, padx=2, pady=2)
                row.append(btn)
            self.grid_buttons.append(row)
        
        # Word display
        self.word_label = tk.Label(left_panel, text="Current Word: ", 
                                   font=('Arial', 16), bg='#2c3e50', fg='white')
        self.word_label.pack(pady=10)
        
        # Submit button
        self.submit_btn = tk.Button(left_panel, text="Submit Word", 
                                    command=self.submit_word,
                                    bg='#27ae60', fg='white', 
                                    font=('Arial', 14), state='disabled')
        self.submit_btn.pack(pady=5)
        
        # Right panel - Players and scores
        right_panel = tk.Frame(content_frame, bg='#34495e', width=250)
        right_panel.pack(side='right', fill='y', padx=5)
        
        # Players list
        tk.Label(right_panel, text="Players:", bg='#34495e', fg='white',
                font=('Arial', 14, 'bold')).pack(pady=5)
        
        self.players_listbox = tk.Listbox(right_panel, height=8, 
                                          bg='#2c3e50', fg='white',
                                          font=('Arial', 11))
        self.players_listbox.pack(padx=10, pady=5)
        
        # Score
        self.score_label = tk.Label(right_panel, text="Score: 0", 
                                    bg='#34495e', fg='white',
                                    font=('Arial', 16, 'bold'))
        self.score_label.pack(pady=10)
        
        # Message log
        log_frame = tk.Frame(self.root, bg='#34495e', height=120)
        log_frame.pack(fill='x', padx=5, pady=5)
        
        # Scrollbar for log
        scrollbar = tk.Scrollbar(log_frame)
        scrollbar.pack(side='right', fill='y')
        
        self.log_text = tk.Text(log_frame, height=6, bg='#2c3e50', fg='#95a5a6',
                                font=('Courier', 10), yscrollcommand=scrollbar.set)
        self.log_text.pack(fill='both', expand=True)
        scrollbar.config(command=self.log_text.yview)
    
    def connect_to_server(self):
        """Connect to TCP server"""
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.connect(('localhost', 5555))
            self.connected = True
            
            # Update UI
            self.status_label.config(text="Status: Connected", fg='#2ecc71')
            self.connect_btn.config(state='disabled')
            self.create_btn.config(state='normal')
            self.join_btn.config(state='normal')
            
            # Start receive thread
            receive_thread = threading.Thread(target=self.receive_messages)
            receive_thread.daemon = True
            receive_thread.start()
            
            self.log("Connected to server!")
            
        except Exception as e:
            messagebox.showerror("Connection Error", f"Failed to connect: {e}")
            self.log(f"Connection failed: {e}")
    
    def receive_messages(self):
        """Receive messages from server in background thread"""
        buffer = ""
        while self.connected:
            try:
                data = self.socket.recv(4096).decode('utf-8')
                if not data:
                    break
                
                buffer += data
                
                # Try to parse complete JSON messages
                while buffer:
                    try:
                        # Try to find a complete JSON object
                        # Simple approach: try to parse and see if it works
                        message = json.loads(buffer)
                        buffer = ""  # Clear buffer if successful
                        self.handle_message(message)
                    except json.JSONDecodeError:
                        # Not a complete JSON yet, wait for more data
                        break
                    
            except Exception as e:
                self.log(f"Receive error: {e}")
                break
        
        self.connected = False
        self.root.after(0, self.on_disconnected)
    
    def send_message(self, message):
        """Send message to server"""
        if self.connected and self.socket:
            try:
                # Add messageType for compatibility
                if 'type' in message:
                    message['messageType'] = message['type']
                
                json_str = json.dumps(message)
                self.socket.send(json_str.encode('utf-8'))
                print(f"SENT: {json_str}")  # Print to console
                self.log(f"Sent: {message['type']}")
            except Exception as e:
                self.log(f"Send error: {e}")
    
    def handle_message(self, message):
        """Handle message from server (called from receive thread)"""
        # Use after() to update UI from main thread
        self.root.after(0, lambda: self.process_message(message))
    
    def process_message(self, message):
        """Process server message in main thread"""
        msg_type = message.get('type') or message.get('messageType')
        data = message.get('data', {})
        
        print(f"RECEIVED: {json.dumps(message)}")  # Print to console
        self.log(f"Received: {msg_type}")
        
        if msg_type == 'CONNECTION_SUCCESS':
            self.log("Server confirmed connection")
            
        elif msg_type == 'ROOM_CREATED':
            self.room_entry.delete(0, tk.END)
            self.room_entry.insert(0, data.get('roomCode', ''))
            self.ready_btn.config(state='normal')
            self.start_btn.config(state='normal')
            self.log(f"Room created: {data.get('roomCode')}")
            
        elif msg_type == 'ROOM_JOINED':
            self.ready_btn.config(state='normal')
            self.log("Joined room successfully")
            
        elif msg_type == 'PLAYER_JOINED':
            self.log(f"{data.get('playerName')} joined")
            
        elif msg_type == 'ROOM_STATE':
            self.update_players_list(data.get('players', []))
            
        elif msg_type == 'GAME_STARTING':
            self.submit_btn.config(state='normal')
            self.log(f"Game starting in {data.get('countdown')} seconds!")
            
        elif msg_type == 'LEVEL_START':
            self.update_grid(data.get('grid', []))
            self.log("Level started!")
            
        elif msg_type == 'GRID_UPDATE':
            self.update_grid(data.get('grid', []))
            
        elif msg_type == 'WORD_ACCEPTED':
            self.log(f"Word accepted! +{data.get('points')} points")
            self.reset_selection()
            
        elif msg_type == 'WORD_REJECTED':
            self.log(f"Word rejected: {data.get('reason')}")
            self.reset_selection()
            
        elif msg_type == 'LEADERBOARD_UPDATE':
            scores = data.get('scores', [])
            # Update score display
            for score in scores:
                if isinstance(score, dict) and score.get('playerId') == self.player_info.get('id'):
                    self.score_label.config(text=f"Score: {score.get('score', 0)}")
            
        elif msg_type == 'ERROR':
            messagebox.showerror("Error", data.get('error', 'Unknown error'))
    
    def create_room(self):
        """Create new room"""
        name = self.name_entry.get().strip()
        if not name:
            messagebox.showwarning("Warning", "Please enter your name!")
            return
        
        message = {
            "type": "CREATE_ROOM",
            "data": {
                "playerName": name,
                "hostName": name,
                "topic": "animals",
                "levelCount": 5,
                "levelDuration": 30,
                "maxPlayers": 4
            }
        }
        self.send_message(message)
    
    def join_room(self):
        """Join existing room"""
        room_code = self.room_entry.get().strip()
        name = self.name_entry.get().strip()
        
        if not room_code or not name:
            messagebox.showwarning("Warning", "Please enter room code and name!")
            return
        
        message = {
            "type": "JOIN_ROOM",
            "data": {
                "roomCode": room_code,
                "playerName": name
            }
        }
        self.send_message(message)
    
    def toggle_ready(self):
        """Toggle ready status"""
        message = {
            "type": "PLAYER_READY",
            "data": {"ready": True}
        }
        self.send_message(message)
    
    def start_game(self):
        """Start game (host only)"""
        message = {
            "type": "START_GAME",
            "data": {}
        }
        self.send_message(message)
    
    def cell_clicked(self, row, col):
        """Handle grid cell click"""
        # Check if cell has letter
        if not self.grid_buttons[row][col]['text']:
            return
        
        # Check if adjacent to last selected
        if self.selected_cells:
            last_row, last_col = self.selected_cells[-1]
            if abs(row - last_row) > 1 or abs(col - last_col) > 1:
                self.reset_selection()
        
        # Add to selection if not already selected
        if (row, col) not in self.selected_cells:
            self.selected_cells.append((row, col))
            self.current_word += self.grid_buttons[row][col]['text']
            self.grid_buttons[row][col].config(bg='#e74c3c')
            self.word_label.config(text=f"Current Word: {self.current_word}")
    
    def submit_word(self):
        """Submit current word"""
        if self.current_word and len(self.selected_cells) > 1:
            path = [r * 5 + c for r, c in self.selected_cells]
            message = {
                "type": "SUBMIT_WORD",
                "data": {
                    "word": self.current_word,
                    "path": path
                }
            }
            self.send_message(message)
    
    def reset_selection(self):
        """Clear current selection"""
        for row, col in self.selected_cells:
            self.grid_buttons[row][col].config(bg='#3498db')
        self.selected_cells = []
        self.current_word = ""
        self.word_label.config(text="Current Word: ")
    
    def update_grid(self, grid_data):
        """Update grid with new letters"""
        if not grid_data:
            return
        
        for i in range(5):
            for j in range(5):
                if i < len(grid_data) and j < len(grid_data[i]):
                    letter = grid_data[i][j]
                    self.grid_buttons[i][j]['text'] = letter if letter else ''
    
    def update_players_list(self, players):
        """Update players list"""
        self.players_listbox.delete(0, tk.END)
        for player in players:
            if isinstance(player, dict):
                name = player.get('name', 'Unknown')
                ready = "✓" if player.get('ready') else "✗"
                self.players_listbox.insert(tk.END, f"{ready} {name}")
    
    def log(self, message):
        """Add message to log"""
        timestamp = datetime.now().strftime("%H:%M:%S")
        log_msg = f"[{timestamp}] {message}"
        print(log_msg)  # Also print to console
        self.log_text.insert(tk.END, f"{log_msg}\n")
        self.log_text.see(tk.END)
    
    def on_disconnected(self):
        """Handle disconnection"""
        self.status_label.config(text="Status: Disconnected", fg='#e74c3c')
        self.connect_btn.config(state='normal')
        self.create_btn.config(state='disabled')
        self.join_btn.config(state='disabled')
        self.ready_btn.config(state='disabled')
        self.start_btn.config(state='disabled')
        self.submit_btn.config(state='disabled')
        self.log("Disconnected from server")


def main():
    root = tk.Tk()
    app = WordBrain2TkinterClient(root)
    root.mainloop()


if __name__ == '__main__':
    main()