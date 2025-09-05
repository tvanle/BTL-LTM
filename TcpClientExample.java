import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Sample TCP Client for WordBrain2 Game
 * Connect to the TCP server on port 5555 and send messages
 */
public class TcpClientExample {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;
    
    public static void main(String[] args) {
        try {
            // Connect to server
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("Connected to WordBrain2 server on " + SERVER_HOST + ":" + SERVER_PORT);
            
            // Set up streams
            DataInputStream din = new DataInputStream(socket.getInputStream());
            DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
            
            Scanner sc = new Scanner(System.in);
            
            // Start a thread to listen for server messages
            Thread listenerThread = new Thread(() -> {
                try {
                    while (!socket.isClosed()) {
                        String serverMessage = din.readUTF();
                        System.out.println("Server: " + serverMessage);
                    }
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        System.err.println("Error reading from server: " + e.getMessage());
                    }
                }
            });
            listenerThread.start();
            
            // Show menu and handle user input
            showMenu();
            
            String input;
            while (true) {
                System.out.print("Enter your choice (or 'exit' to quit): ");
                input = sc.nextLine();
                
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                
                String jsonMessage = null;
                
                switch (input) {
                    case "1":
                        // Create room
                        jsonMessage = createRoomMessage();
                        break;
                    case "2":
                        // Join room
                        System.out.print("Enter room code: ");
                        String roomCode = sc.nextLine();
                        System.out.print("Enter your name: ");
                        String playerName = sc.nextLine();
                        jsonMessage = joinRoomMessage(roomCode, playerName);
                        break;
                    case "3":
                        // Player ready
                        jsonMessage = playerReadyMessage();
                        break;
                    case "4":
                        // Start game
                        jsonMessage = startGameMessage();
                        break;
                    case "5":
                        // Submit word
                        System.out.print("Enter word: ");
                        String word = sc.nextLine();
                        System.out.print("Enter path (comma-separated, e.g., 0,1,2): ");
                        String path = sc.nextLine();
                        jsonMessage = submitWordMessage(word, path);
                        break;
                    case "menu":
                        showMenu();
                        continue;
                    default:
                        System.out.println("Invalid option. Type 'menu' to see options again.");
                        continue;
                }
                
                if (jsonMessage != null) {
                    dout.writeUTF(jsonMessage);
                    dout.flush();
                    System.out.println("Sent: " + jsonMessage);
                }
            }
            
            // Clean up
            socket.close();
            System.out.println("Disconnected from server.");
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private static void showMenu() {
        System.out.println("\n=== WordBrain2 TCP Client ===");
        System.out.println("1. Create Room");
        System.out.println("2. Join Room");
        System.out.println("3. Player Ready");
        System.out.println("4. Start Game");
        System.out.println("5. Submit Word");
        System.out.println("Type 'menu' to show this menu again");
        System.out.println("Type 'exit' to quit");
        System.out.println("===============================");
    }
    
    private static String createRoomMessage() {
        return "{"
                + "\"type\":\"CREATE_ROOM\","
                + "\"messageType\":\"CREATE_ROOM\","
                + "\"data\":{"
                    + "\"hostName\":\"Player1\","
                    + "\"topic\":\"animals\","
                    + "\"levelCount\":5,"
                    + "\"levelDuration\":30,"
                    + "\"maxPlayers\":4,"
                    + "\"enabledBoosters\":[\"DOUBLE_UP\",\"FREEZE\"]"
                + "}"
                + "}";
    }
    
    private static String joinRoomMessage(String roomCode, String playerName) {
        return "{"
                + "\"type\":\"JOIN_ROOM\","
                + "\"messageType\":\"JOIN_ROOM\","
                + "\"data\":{"
                    + "\"roomCode\":\"" + roomCode + "\","
                    + "\"playerName\":\"" + playerName + "\""
                + "}"
                + "}";
    }
    
    private static String playerReadyMessage() {
        return "{"
                + "\"type\":\"PLAYER_READY\","
                + "\"messageType\":\"PLAYER_READY\","
                + "\"data\":{"
                    + "\"ready\":true"
                + "}"
                + "}";
    }
    
    private static String startGameMessage() {
        return "{"
                + "\"type\":\"START_GAME\","
                + "\"messageType\":\"START_GAME\","
                + "\"data\":{}"
                + "}";
    }
    
    private static String submitWordMessage(String word, String pathStr) {
        String[] pathParts = pathStr.split(",");
        StringBuilder pathArray = new StringBuilder("[");
        for (int i = 0; i < pathParts.length; i++) {
            if (i > 0) pathArray.append(",");
            pathArray.append(pathParts[i].trim());
        }
        pathArray.append("]");
        
        return "{"
                + "\"type\":\"SUBMIT_WORD\","
                + "\"messageType\":\"SUBMIT_WORD\","
                + "\"data\":{"
                    + "\"word\":\"" + word + "\","
                    + "\"path\":" + pathArray.toString()
                + "}"
                + "}";
    }
}