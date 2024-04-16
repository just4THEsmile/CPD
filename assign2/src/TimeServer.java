import java.io.*;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

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

            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket)).start();
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            int localSum = 0;

            try (
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
            ) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals("done")) {
                        break; // Exit the loop if "done" signal received
                    }
                    int num = Integer.parseInt(line);
                    localSum += num;
                    writer.println(localSum);
                }

                updateGlobalSum(localSum);
                writer.println(globalSum);

            } catch (IOException ex) {
                System.out.println("I/O error: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
        }

        private void updateGlobalSum(int num) {
            lock.lock();
            try {
                globalSum += num;
            } finally {
                lock.unlock();
            }
        }
    }
}
