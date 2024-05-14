import java.io.*;
import java.net.*;

import java.util.concurrent.locks.ReentrantLock;

import javax.xml.crypto.Data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

 
/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */
import java.util.List;
import java.util.ArrayList;

public class TimeServer extends Thread {
    private static ReentrantLock lock = new ReentrantLock();
    static ArrayList<Socket> queue_casual = new ArrayList<>();
    static ArrayList<MyPair<Socket,Integer>> queue_ranked = new ArrayList<>();
    static DatabaseController db;

    public static void main(String[] args) {
        System.out.println("Server is running");
        db = new DatabaseController();
        //------------------------------------------------------- DATABASE CONNECTION
        if (args.length < 1) {
            System.out.println("Usage: java TimeServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            new Thread(new QueueHandler()).start();

            Socket socket;
            while (true) { // Receive new players
                socket = serverSocket.accept();
                new Thread(new PlayerHandler(socket)).start();

                /*System.out.println("queue_caual.size() " + queue_casual.size());
                System.out.println("queue_ranked.size() " + queue_ranked.size());*/
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static class QueueHandler implements Runnable {
        int waiting_time_ranked = 0;

        public void run(){
            while(true) {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (queue_casual.size() >= 2) { // Start a new game after 2 players have connected
                    // get the first 2 values from the queue
                    ArrayList <Socket> temp = new ArrayList<Socket>();
                    temp.add(queue_casual.get(0));
                    temp.add(queue_casual.get(1));
                    queue_casual.remove(0);
                    queue_casual.remove(0);
                    new Thread(new GameHandler(temp)).start();
                }

                // ranked game timeout
                if (queue_ranked.size() > 0) {
                    waiting_time_ranked += 20;
                }

                if (queue_ranked.size() >= 2) { // Start a new game after 2 players have connected
                    // Try each combination of two players that are closest
                    for (int i = 0; i < queue_ranked.size() - 1; i++) {
                        MyPair pair_1 = queue_ranked.get(i);
                        MyPair pair_2 = queue_ranked.get(i + 1);

                        if(Math.abs((Integer) pair_1.getValue() - (Integer) pair_2.getValue()) < waiting_time_ranked){
                            ArrayList <Socket> temp = new ArrayList<Socket>();
                            temp.add((Socket)pair_1.getKey());
                            temp.add((Socket)pair_2.getKey());
                            queue_ranked.remove(pair_1);
                            queue_ranked.remove(pair_2);

                            if (queue_ranked.isEmpty()) {
                                waiting_time_ranked = 0;
                            }

                            new Thread(new GameHandler(temp)).start();
                        }
                    }
                }
            }
        }
    }

    private static class PlayerHandler implements Runnable {
        private Socket socket;
        private int player_id;
        private boolean isConnected = false;

        public PlayerHandler(Socket socket) {
            this.socket = socket;
            this.player_id = -1;
        }

        public PlayerHandler(Socket socket, int player_id) {
            this.socket = socket;
            this.player_id = player_id;
            this.isConnected = true;
        }

        public void run() {
            try {
                System.out.println("New client connected: "+ socket.getPort());
                while (socket != null) {
                    InputStream input = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    OutputStream output = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);

                    String line;
                    if (isConnected) { // Skip login if the player is already connected
                        line = "GAME_SELECTION";
                    } else {
                        line = reader.readLine();
                    }

                    if (line != null) {
                        switch (line) {
                            case "LOGIN":
                                String username = reader.readLine();
                                String password = reader.readLine();
                                System.out.println("username: " + username);
                                System.out.println("password: " + password);
                                player_id = db.login(username, password);
                                writer.println(player_id);

                                if (player_id == -1) {
                                    System.out.println("Login failed");
                                    writer.println("FAILED");
                                } else {
                                    System.out.println("----------------------");
                                    System.out.println("\033[32mLogin success \033[30m");
                                    System.out.println("----------------------");
                                    writer.println("SUCCESS");
                                }
                                break;
                            case "REGISTER":
                                username = reader.readLine();
                                password = reader.readLine();
                                System.out.println("username: " + username);
                                System.out.println("password: " + password);
                                player_id = db.register(username, password);

                                if (player_id == -1) {
                                    System.out.println("Register failed");
                                    writer.println("FAILED");
                                } else {
                                    System.out.println("Register success");
                                    writer.println("SUCCESS");
                                }
                                break;
                            case "LOGOUT":
                                System.out.println("Logout");
                                socket.close();
                                socket = null;
                                break;
                            case "Exit":
                                System.out.println("Exit");
                                socket.close();
                                socket = null;
                                break;
                            case "GAME_SELECTION":
                                System.out.println("Game selection");
                                String gameType = reader.readLine();
                                System.out.println("Game: " + gameType);

                                if (gameType.equals("CASUAL")) {
                                    System.out.println("Casual");
                                    queue_casual.add(socket);
                                    System.out.println("queue"+queue_casual.size());
                                    System.out.println("queue"+queue_ranked.size());
                                    return;

                                } else if(gameType.equals("RANKED")){
                                    System.out.println("Ranked");
                                    int score = db.getScore(player_id);

                                    MyPair<Socket, Integer> player = new MyPair<>(socket, score);
                                    queue_ranked.add(player);
                                    System.out.println("queue" + queue_casual.size());
                                    System.out.println("queue" + queue_ranked.size());
                                    return;
                                } else {
                                    System.out.println("Invalid game type");
                                }
                                break;
                            case "PLAYING":
                                System.out.println("Playing");
                                break;
                            default:
                                break;
                        }

                    } else {
                        socket.close();
                        socket = null;
                    }
                }
            } catch (IOException ex) {
                System.out.println("Server exception: " + ex.getMessage());
                ex.printStackTrace();
            }

            System.out.println("queue"+queue_casual.size());
            System.out.println("queue"+queue_ranked.size());
            System.err.println("Thread running");
        }
    }

    private static class GameHandler implements Runnable {
        List<Socket> userSockets;

        public GameHandler(List<Socket> userSockets) {
            this.userSockets = new ArrayList<>(userSockets);
        }

        public void run() {
            // Start the game
            System.out.println("Starting game with " + userSockets.size() + " players");
            System.out.println("Socket" + userSockets.get(0));
            System.out.println("Socket" + userSockets.get(1));
            // Notify the clients that the game has started
            PrintWriter writer;

            try {
                for (int i = 0; i < userSockets.size(); i++) {
                    Socket socket = userSockets.get(i);
                    writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.println("GAME_FOUND");
                }
            } catch(IOException e) {
                e.printStackTrace();
            }

            Game game = new Game(userSockets.size(), userSockets);
            game.start();

            // Print user scores
            for (int i = 0; i < userSockets.size(); i++) {
                try {
                    Socket socket = userSockets.get(i);
                    writer = new PrintWriter(socket.getOutputStream(), true);
                    InputStream input = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    int player_score = game.getScore(i);

                    writer.println("Game over! Your score: " + player_score);
                    writer.println("GAME_OVER");

                    String player_id_string = reader.readLine();
                    int player_id = Integer.parseInt(player_id_string);

                    lock.lock();
                    db.updateScore(player_id, db.getScore(player_id) + player_score);
                    lock.unlock();

                    new Thread(new PlayerHandler(socket, player_id)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
