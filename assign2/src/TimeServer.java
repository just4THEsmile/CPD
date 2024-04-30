import java.io.*;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;
import java.util.ArrayList;


public class TimeServer {
    private static int globalSum = 0;
    private static ReentrantLock lock = new ReentrantLock();


    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java TimeServer <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            List<Socket> userSockets = new ArrayList<>();

            while (true) { // Wait for 2 players to connect
                Socket socket = serverSocket.accept();
                userSockets.add(socket);
                if (userSockets.size() == 2) { // Start a new game after 2 players have connected
                    new Thread(new ClientHandler(userSockets)).start();
                    userSockets.clear();
                }
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        List<Socket> userSockets;

        public ClientHandler(List<Socket> userSockets) {
            this.userSockets = new ArrayList<>(userSockets);
        }

        public void run() {
            // Start the game
            Game game = new Game(userSockets.size(), userSockets);
            game.start();

            // Print user scores
            for (int i = 0; i < userSockets.size(); i++) {
                try {
                    Socket socket = userSockets.get(i);
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    writer.println("Game over! Your score: " + game.getScore(i));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
