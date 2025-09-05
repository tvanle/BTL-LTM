#!/usr/bin/env python3
"""
WordBrain2 Multiplayer Game - Python Client
TCP Socket connection to Java server on port 5555
"""

import sys
import json
import socket
import threading
from datetime import datetime
from PyQt5.QtWidgets import *
from PyQt5.QtCore import *
from PyQt5.QtGui import *

class TCPConnection(QObject):
    """Handles TCP Socket connection to game server"""
    
    # Signals for thread-safe GUI updates
    message_received = pyqtSignal(dict)
    connection_lost = pyqtSignal()
    connection_established = pyqtSignal()
    
    def __init__(self):
        super().__init__()
        self.socket = None
        self.connected = False
        self.host = "localhost"
        self.port = 5555
        self.receive_thread = None
        
    def connect_to_server(self):
        """Establish TCP connection to game server"""
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.connect((self.host, self.port))
            self.connected = True
            self.connection_established.emit()
            
            # Start receiving thread
            self.receive_thread = threading.Thread(target=self.receive_messages)
            self.receive_thread.daemon = True
            self.receive_thread.start()
            
            return True
        except Exception as e:
            print(f"Connection failed: {e}")
            return False
    
    def send_message(self, message_dict):
        """Send JSON message to server"""
        if self.connected and self.socket:
            try:
                # Add messageType field for compatibility
                if 'type' in message_dict:
                    message_dict['messageType'] = message_dict['type']
                
                json_str = json.dumps(message_dict)
                # Use length prefix for message framing
                message_bytes = json_str.encode('utf-8')
                self.socket.send(len(message_bytes).to_bytes(4, 'big'))
                self.socket.send(message_bytes)
                print(f"Sent: {json_str}")
            except Exception as e:
                print(f"Send error: {e}")
                self.disconnect()
    
    def receive_messages(self):
        """Receive messages from server (runs in separate thread)"""
        while self.connected:
            try:
                # Read length prefix
                length_bytes = self.socket.recv(4)
                if not length_bytes:
                    break
                
                message_length = int.from_bytes(length_bytes, 'big')
                
                # Read message
                message_bytes = b''
                while len(message_bytes) < message_length:
                    chunk = self.socket.recv(min(4096, message_length - len(message_bytes)))
                    if not chunk:
                        break
                    message_bytes += chunk
                
                if message_bytes:
                    message = json.loads(message_bytes.decode('utf-8'))
                    print(f"Received: {message}")
                    self.message_received.emit(message)
                    
            except json.JSONDecodeError as e:
                print(f"JSON decode error: {e}")
            except Exception as e:
                print(f"Receive error: {e}")
                break
        
        self.connected = False
        self.connection_lost.emit()
    
    def disconnect(self):
        """Close TCP connection"""
        self.connected = False
        if self.socket:
            try:
                self.socket.close()
            except:
                pass
            self.socket = None


class GameGrid(QWidget):
    """Game grid widget with clickable cells"""
    
    cell_clicked = pyqtSignal(int, int)
    word_selected = pyqtSignal(str, list)
    
    def __init__(self, size=5):
        super().__init__()
        self.size = size
        self.cells = []
        self.selected_path = []
        self.current_word = ""
        self.grid_data = [[''] for _ in range(size) for _ in range(size)]
        self.init_ui()
        
    def init_ui(self):
        """Initialize grid UI"""
        layout = QGridLayout()
        layout.setSpacing(5)
        
        for i in range(self.size):
            row = []
            for j in range(self.size):
                btn = QPushButton('')
                btn.setFixedSize(80, 80)
                btn.setFont(QFont('Arial', 24, QFont.Bold))
                btn.setStyleSheet("""
                    QPushButton {
                        background-color: #3498db;
                        color: white;
                        border: 2px solid #2c3e50;
                        border-radius: 10px;
                    }
                    QPushButton:hover {
                        background-color: #2980b9;
                    }
                    QPushButton:pressed {
                        background-color: #21618c;
                    }
                """)
                
                # Connect click handler
                btn.clicked.connect(lambda checked, r=i, c=j: self.on_cell_click(r, c))
                btn.installEventFilter(self)
                
                layout.addWidget(btn, i, j)
                row.append(btn)
            self.cells.append(row)
        
        self.setLayout(layout)
    
    def on_cell_click(self, row, col):
        """Handle cell click"""
        if not self.cells[row][col].text():
            return
        
        # Check if cell is adjacent to last selected
        if self.selected_path:
            last_row, last_col = self.selected_path[-1]
            if abs(row - last_row) > 1 or abs(col - last_col) > 1:
                # Not adjacent, reset selection
                self.reset_selection()
        
        # Add to path if not already selected
        if (row, col) not in self.selected_path:
            self.selected_path.append((row, col))
            self.current_word += self.cells[row][col].text()
            self.highlight_cell(row, col, True)
            self.cell_clicked.emit(row, col)
    
    def highlight_cell(self, row, col, selected):
        """Highlight or unhighlight a cell"""
        if selected:
            self.cells[row][col].setStyleSheet("""
                QPushButton {
                    background-color: #e74c3c;
                    color: white;
                    border: 3px solid #c0392b;
                    border-radius: 10px;
                }
            """)
        else:
            self.cells[row][col].setStyleSheet("""
                QPushButton {
                    background-color: #3498db;
                    color: white;
                    border: 2px solid #2c3e50;
                    border-radius: 10px;
                }
            """)
    
    def reset_selection(self):
        """Clear current selection"""
        for row, col in self.selected_path:
            self.highlight_cell(row, col, False)
        self.selected_path = []
        self.current_word = ""
    
    def submit_word(self):
        """Submit the currently selected word"""
        if self.current_word and len(self.selected_path) > 1:
            path = [(r * self.size + c) for r, c in self.selected_path]
            self.word_selected.emit(self.current_word, path)
            self.reset_selection()
    
    def update_grid(self, grid_data):
        """Update grid with new letters"""
        if not grid_data:
            return
        
        for i in range(self.size):
            for j in range(self.size):
                if i < len(grid_data) and j < len(grid_data[i]):
                    letter = grid_data[i][j]
                    self.cells[i][j].setText(letter if letter else '')
                    self.cells[i][j].setEnabled(bool(letter))
    
    def clear_grid(self):
        """Clear all cells"""
        for i in range(self.size):
            for j in range(self.size):
                self.cells[i][j].setText('')
                self.cells[i][j].setEnabled(False)


class WordBrain2Client(QMainWindow):
    """Main game window"""
    
    def __init__(self):
        super().__init__()
        self.tcp_connection = TCPConnection()
        self.game_grid = None
        self.player_info = {}
        self.room_info = {}
        self.init_ui()
        self.connect_signals()
        
    def init_ui(self):
        """Initialize main UI"""
        self.setWindowTitle("WordBrain2 Multiplayer - Python Client")
        self.setGeometry(100, 100, 1000, 800)
        
        # Set dark theme
        self.setStyleSheet("""
            QMainWindow {
                background-color: #2c3e50;
            }
            QLabel {
                color: white;
                font-size: 14px;
            }
            QLineEdit {
                padding: 8px;
                font-size: 14px;
                border: 2px solid #34495e;
                border-radius: 5px;
                background-color: #34495e;
                color: white;
            }
            QPushButton {
                padding: 10px;
                font-size: 14px;
                background-color: #3498db;
                color: white;
                border: none;
                border-radius: 5px;
            }
            QPushButton:hover {
                background-color: #2980b9;
            }
            QListWidget {
                background-color: #34495e;
                color: white;
                border: 2px solid #2c3e50;
                border-radius: 5px;
            }
        """)
        
        # Central widget
        central = QWidget()
        self.setCentralWidget(central)
        main_layout = QVBoxLayout(central)
        
        # Header
        header = QLabel("WordBrain2 Multiplayer")
        header.setAlignment(Qt.AlignCenter)
        header.setStyleSheet("font-size: 28px; font-weight: bold; padding: 20px;")
        main_layout.addWidget(header)
        
        # Connection panel
        conn_panel = QHBoxLayout()
        
        self.status_label = QLabel("Status: Disconnected")
        self.status_label.setStyleSheet("color: #e74c3c;")
        conn_panel.addWidget(self.status_label)
        
        conn_panel.addStretch()
        
        self.connect_btn = QPushButton("Connect to Server")
        self.connect_btn.clicked.connect(self.connect_to_server)
        conn_panel.addWidget(self.connect_btn)
        
        main_layout.addLayout(conn_panel)
        
        # Room panel
        room_panel = QHBoxLayout()
        
        self.room_code_input = QLineEdit()
        self.room_code_input.setPlaceholderText("Room Code")
        self.room_code_input.setMaximumWidth(150)
        room_panel.addWidget(QLabel("Room:"))
        room_panel.addWidget(self.room_code_input)
        
        self.player_name_input = QLineEdit()
        self.player_name_input.setPlaceholderText("Your Name")
        self.player_name_input.setMaximumWidth(200)
        room_panel.addWidget(QLabel("Name:"))
        room_panel.addWidget(self.player_name_input)
        
        self.create_room_btn = QPushButton("Create Room")
        self.create_room_btn.clicked.connect(self.create_room)
        self.create_room_btn.setEnabled(False)
        room_panel.addWidget(self.create_room_btn)
        
        self.join_room_btn = QPushButton("Join Room")
        self.join_room_btn.clicked.connect(self.join_room)
        self.join_room_btn.setEnabled(False)
        room_panel.addWidget(self.join_room_btn)
        
        self.ready_btn = QPushButton("Ready")
        self.ready_btn.clicked.connect(self.toggle_ready)
        self.ready_btn.setEnabled(False)
        room_panel.addWidget(self.ready_btn)
        
        self.start_btn = QPushButton("Start Game")
        self.start_btn.clicked.connect(self.start_game)
        self.start_btn.setEnabled(False)
        room_panel.addWidget(self.start_btn)
        
        room_panel.addStretch()
        main_layout.addLayout(room_panel)
        
        # Game area
        game_layout = QHBoxLayout()
        
        # Left: Game grid
        left_panel = QVBoxLayout()
        
        self.game_grid = GameGrid(5)
        self.game_grid.word_selected.connect(self.submit_word)
        left_panel.addWidget(self.game_grid)
        
        # Word submit button
        self.submit_btn = QPushButton("Submit Word")
        self.submit_btn.clicked.connect(self.game_grid.submit_word)
        self.submit_btn.setEnabled(False)
        left_panel.addWidget(self.submit_btn)
        
        # Current word display
        self.word_label = QLabel("Current Word: ")
        self.word_label.setStyleSheet("font-size: 18px; padding: 10px;")
        left_panel.addWidget(self.word_label)
        
        game_layout.addLayout(left_panel)
        
        # Right: Players and scores
        right_panel = QVBoxLayout()
        
        right_panel.addWidget(QLabel("Players:"))
        self.player_list = QListWidget()
        self.player_list.setMaximumWidth(250)
        right_panel.addWidget(self.player_list)
        
        right_panel.addWidget(QLabel("Leaderboard:"))
        self.leaderboard = QListWidget()
        self.leaderboard.setMaximumWidth(250)
        right_panel.addWidget(self.leaderboard)
        
        # Score display
        self.score_label = QLabel("Your Score: 0")
        self.score_label.setStyleSheet("font-size: 18px; font-weight: bold;")
        right_panel.addWidget(self.score_label)
        
        game_layout.addLayout(right_panel)
        main_layout.addLayout(game_layout)
        
        # Message log
        self.message_log = QTextEdit()
        self.message_log.setReadOnly(True)
        self.message_log.setMaximumHeight(150)
        self.message_log.setStyleSheet("background-color: #34495e; color: #95a5a6;")
        main_layout.addWidget(self.message_log)
    
    def connect_signals(self):
        """Connect TCP signals to handlers"""
        self.tcp_connection.connection_established.connect(self.on_connected)
        self.tcp_connection.connection_lost.connect(self.on_disconnected)
        self.tcp_connection.message_received.connect(self.handle_server_message)
        
        # Update word label when cells are clicked
        self.game_grid.cell_clicked.connect(lambda: 
            self.word_label.setText(f"Current Word: {self.game_grid.current_word}"))
    
    def connect_to_server(self):
        """Connect to game server"""
        if self.tcp_connection.connect_to_server():
            self.log_message("Connecting to server...")
        else:
            self.log_message("Failed to connect to server!")
    
    def on_connected(self):
        """Handle successful connection"""
        self.status_label.setText("Status: Connected")
        self.status_label.setStyleSheet("color: #2ecc71;")
        self.connect_btn.setEnabled(False)
        self.create_room_btn.setEnabled(True)
        self.join_room_btn.setEnabled(True)
        self.log_message("Connected to game server!")
    
    def on_disconnected(self):
        """Handle disconnection"""
        self.status_label.setText("Status: Disconnected")
        self.status_label.setStyleSheet("color: #e74c3c;")
        self.connect_btn.setEnabled(True)
        self.create_room_btn.setEnabled(False)
        self.join_room_btn.setEnabled(False)
        self.ready_btn.setEnabled(False)
        self.start_btn.setEnabled(False)
        self.submit_btn.setEnabled(False)
        self.log_message("Disconnected from server!")
    
    def create_room(self):
        """Create a new room"""
        name = self.player_name_input.text().strip()
        if not name:
            QMessageBox.warning(self, "Warning", "Please enter your name!")
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
        self.tcp_connection.send_message(message)
        self.log_message(f"Creating room as {name}...")
    
    def join_room(self):
        """Join existing room"""
        room_code = self.room_code_input.text().strip()
        name = self.player_name_input.text().strip()
        
        if not room_code or not name:
            QMessageBox.warning(self, "Warning", "Please enter room code and name!")
            return
        
        message = {
            "type": "JOIN_ROOM",
            "data": {
                "roomCode": room_code,
                "playerName": name
            }
        }
        self.tcp_connection.send_message(message)
        self.log_message(f"Joining room {room_code} as {name}...")
    
    def toggle_ready(self):
        """Toggle ready status"""
        message = {
            "type": "PLAYER_READY",
            "data": {
                "ready": True
            }
        }
        self.tcp_connection.send_message(message)
        self.log_message("Ready status toggled")
    
    def start_game(self):
        """Start the game (host only)"""
        message = {
            "type": "START_GAME",
            "data": {}
        }
        self.tcp_connection.send_message(message)
        self.log_message("Starting game...")
    
    def submit_word(self, word, path):
        """Submit a word to server"""
        message = {
            "type": "SUBMIT_WORD",
            "data": {
                "word": word,
                "path": path
            }
        }
        self.tcp_connection.send_message(message)
        self.log_message(f"Submitted word: {word}")
    
    def handle_server_message(self, message):
        """Handle messages from server"""
        msg_type = message.get('type') or message.get('messageType')
        data = message.get('data', {})
        
        if msg_type == 'CONNECTION_SUCCESS':
            self.log_message("Connection confirmed by server")
            
        elif msg_type == 'ROOM_CREATED':
            self.room_info = data
            self.player_info['id'] = data.get('playerId')
            self.room_code_input.setText(data.get('roomCode', ''))
            self.ready_btn.setEnabled(True)
            self.start_btn.setEnabled(True)
            self.log_message(f"Room created: {data.get('roomCode')}")
            
        elif msg_type == 'ROOM_JOINED':
            self.room_info = data
            self.player_info['id'] = data.get('playerId')
            self.ready_btn.setEnabled(True)
            self.log_message(f"Joined room successfully")
            
        elif msg_type == 'PLAYER_JOINED':
            player_name = data.get('playerName', 'Unknown')
            self.log_message(f"{player_name} joined the room")
            self.update_player_list(data.get('players', []))
            
        elif msg_type == 'PLAYER_LEFT':
            player_id = data.get('playerId')
            self.log_message(f"Player {player_id} left the room")
            
        elif msg_type == 'ROOM_STATE':
            self.update_room_state(data)
            
        elif msg_type == 'GAME_STARTING':
            countdown = data.get('countdown', 5)
            self.log_message(f"Game starting in {countdown} seconds!")
            self.submit_btn.setEnabled(True)
            
        elif msg_type == 'LEVEL_START':
            self.log_message("Level started!")
            grid_data = data.get('grid', [])
            self.game_grid.update_grid(grid_data)
            
        elif msg_type == 'GRID_UPDATE':
            grid_data = data.get('grid', [])
            self.game_grid.update_grid(grid_data)
            
        elif msg_type == 'WORD_ACCEPTED':
            points = data.get('points', 0)
            word = data.get('word', '')
            self.log_message(f"Word '{word}' accepted! +{points} points")
            
        elif msg_type == 'WORD_REJECTED':
            reason = data.get('reason', 'Invalid word')
            self.log_message(f"Word rejected: {reason}")
            
        elif msg_type == 'LEADERBOARD_UPDATE':
            self.update_leaderboard(data.get('scores', []))
            
        elif msg_type == 'OPPONENT_SCORED':
            player = data.get('playerId', 'Someone')
            points = data.get('points', 0)
            word = data.get('word', '')
            self.log_message(f"{player} scored {points} with '{word}'")
            
        elif msg_type == 'ERROR':
            error = data.get('error', 'Unknown error')
            self.log_message(f"Error: {error}")
            QMessageBox.critical(self, "Error", error)
    
    def update_room_state(self, state):
        """Update room state display"""
        players = state.get('players', [])
        self.player_list.clear()
        for player in players:
            name = player.get('name', 'Unknown')
            ready = "✓" if player.get('ready') else "✗"
            is_host = " (Host)" if player.get('isHost') else ""
            self.player_list.addItem(f"{ready} {name}{is_host}")
    
    def update_player_list(self, players):
        """Update player list display"""
        self.player_list.clear()
        for player in players:
            if isinstance(player, dict):
                name = player.get('name', 'Unknown')
                self.player_list.addItem(name)
            else:
                self.player_list.addItem(str(player))
    
    def update_leaderboard(self, scores):
        """Update leaderboard display"""
        self.leaderboard.clear()
        for i, score in enumerate(scores, 1):
            if isinstance(score, dict):
                player = score.get('playerName', 'Unknown')
                points = score.get('score', 0)
                self.leaderboard.addItem(f"{i}. {player}: {points}")
            else:
                self.leaderboard.addItem(str(score))
        
        # Update own score if present
        for score in scores:
            if isinstance(score, dict) and score.get('playerId') == self.player_info.get('id'):
                self.score_label.setText(f"Your Score: {score.get('score', 0)}")
    
    def log_message(self, message):
        """Add message to log"""
        timestamp = datetime.now().strftime("%H:%M:%S")
        self.message_log.append(f"[{timestamp}] {message}")
    
    def closeEvent(self, event):
        """Handle window close"""
        self.tcp_connection.disconnect()
        event.accept()


def main():
    app = QApplication(sys.argv)
    client = WordBrain2Client()
    client.show()
    sys.exit(app.exec_())


if __name__ == '__main__':
    main()