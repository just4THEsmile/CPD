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
    static ArrayList<MyPlayer> queue_casual = new ArrayList<>();
    static ArrayList<MyPlayer> queue_ranked = new ArrayList<>();
    static ArrayList<Game> games = new ArrayList<>();
    static DatabaseController db;
    static final int num_players = 2;

    public static void main(String[] args) {
        System.out.println("Server is running");
        db = new DatabaseController();
        db.createQueue(0); // create casual queue
        db.createQueue(1); // create ranked queue
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

                if (queue_casual.size() >= num_players) { // Start a new game after 2 players have connected
                    // get the first num_players from the queue
                    ArrayList <MyPlayer> temp = new ArrayList<MyPlayer>();
                    for (int i = 0; i < num_players; i++) {
                        temp.add(queue_casual.get(0));
                        queue_casual.remove(0);
                    }

                    new Thread(new GameHandler(temp)).start();
                }

                // ranked game timeout
                if (queue_ranked.size() > 0) {
                    waiting_time_ranked += 20;
                }

                if (queue_ranked.size() >= num_players) { // Start a new game after num_players players have connected
                    // Try each combination of num_players players that are closest in rank
                    for (int i = 0; i < queue_ranked.size() - num_players + 1; i++) {
                        MyPlayer pair_1 = queue_ranked.get(i); // highest ranked player in group
                        MyPlayer pair_2 = queue_ranked.get(i + num_players - 1); // lowest ranked player in group

                        if (Math.abs((Integer) pair_1.getValue() - (Integer) pair_2.getValue()) < waiting_time_ranked) { // If the players are close enough in rank
                            ArrayList <MyPlayer> temp = new ArrayList<MyPlayer>();

                            for (int j = 0; j < num_players; j++) {
                                temp.add(queue_ranked.get(i));
                                queue_ranked.remove(i);
                            }

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

                                lock.lock();
                                player_id = db.login(username, password);
                                lock.unlock();

                                writer.println(player_id);

                                if (player_id == -1) {
                                    System.out.println("Login failed");
                                    writer.println("FAILED");
                                } else {
                                    System.out.println("----------------------");
                                    System.out.println("\033[32mLogin success \033[30m");
                                    System.out.println("----------------------");

                                    lock.lock();
                                    username = db.getUsername(player_id);
                                    int game_id = db.get_game_from_user(player_id);
                                    lock.unlock();

                                    if (game_id != -1) {
                                        writer.println("RECONNECTED");
                                        for (Game game : games) {
                                            if (game.getGameID()==game_id) {
                                                game.ReconnectPlayer(new MyPlayer(socket, player_id, username, db.getScore(player_id)));
                                                return;
                                            }
                                        }
                                    } else {
                                        writer.println("SUCCESS");
                                    }
                                }
                                break;
                            case "REGISTER":
                                username = reader.readLine();
                                password = reader.readLine();
                                System.out.println("username: " + username);
                                System.out.println("password: " + password);

                                lock.lock();
                                player_id = db.register(username, password);
                                lock.unlock();

                                if (player_id == -1) {
                                    System.out.println("Register failed");
                                    writer.println("FAILED");
                                } else {
                                    System.out.println("Register success");
                                    writer.println("SUCCESS");
                                    lock.lock();
                                    username = db.getUsername(player_id);
                                    lock.unlock();
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
                                    System.out.println(player_id);
                                    lock.lock();
                                    int score = db.getScore(player_id);
                                    MyPlayer player = new MyPlayer(socket, player_id, db.getUsername(player_id), score);
                                    lock.unlock();
                                    queue_casual.add(player);
                                    System.out.println("queue"+queue_casual.size());
                                    System.out.println("queue"+queue_ranked.size());
                                    return;

                                } else if(gameType.equals("RANKED")){
                                    System.out.println("Ranked");
                                    lock.lock();
                                    int score = db.getScore(player_id);
                                    MyPlayer player = new MyPlayer(socket, player_id, db.getUsername(player_id), score);
                                    lock.unlock();
                                    queue_ranked.add(player);

                                    queue_ranked.sort((a, b) -> (Integer) a.getValue() - (Integer) b.getValue());

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
        static List<MyPlayer> userSockets;

        public GameHandler(List<MyPlayer> userSockets) {
            TimeServer.GameHandler.userSockets = userSockets;
        }

        public void run() {
            // Start the game
            System.out.println("Starting game with " + userSockets.size() + " players");
            System.out.println("Socket" + userSockets.get(0));
            System.out.println("Socket" + userSockets.get(1));
            // Notify the clients that the game has started
            PrintWriter writer;

            List<String> player_ids = new ArrayList<>();
            for (int i = 0; i < userSockets.size(); i++) {
                player_ids.add(userSockets.get(i).getPlayerID().toString());
            }
            try {
                for (int i = 0; i < userSockets.size(); i++) {
                    Socket socket = userSockets.get(i).getKey();
                    writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.println("GAME_FOUND");
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            lock.lock();
            int game_id = db.createGame(player_ids);
            lock.unlock();
            if (game_id == -1) {
                System.out.println("Game creation failed");
            } else {
                System.out.println("Game creation success");
            }
            
            Game game = new Game(userSockets.size(), userSockets,game_id);
            games.add(game);
            game.start();
            games.remove(game);
            lock.lock();
            db.deleteGame(game_id);
            lock.unlock();
            System.out.println("removing game");

            // Print user scores
            for (int i = 0; i < userSockets.size(); i++) {
                try {
                    MyPlayer player = userSockets.get(i);
                    writer = new PrintWriter(player.getKey().getOutputStream(), true);
                    InputStream input = player.getKey().getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    int player_score = game.getScore(i);

                    writer.println("Game over! Your score: " + player_score);
                    writer.println("GAME_OVER");

                    String player_id_string = reader.readLine();
                    int player_id = Integer.parseInt(player_id_string);

                    lock.lock();
                    db.updateScore(player_id, db.getScore(player_id) + player_score);
                    lock.unlock();

                    new Thread(new PlayerHandler(player.getKey(), player_id)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
