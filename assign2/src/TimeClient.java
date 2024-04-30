import java.io.*;
import java.net.*;
import java.util.Scanner;

public class TimeClient {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java TimeClient <hostname> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            Scanner scanner = new Scanner(System.in);

            while (true) {
                String line = reader.readLine();
                if (line.equals("Your turn to guess a letter!")) {
                    System.out.print("Your turn to guess!\nGuess a letter or word:");
                    String guess = scanner.next();
                    writer.println(guess);
                } else {
                    System.out.println(line);
                }
            }

            /*
            for (int i = 0; i < 5; i++) {
                System.out.print("Enter an int: ");
                int n = scanner.nextInt();
                writer.println(n);

                String localSum = reader.readLine();
                System.out.println("Local sum: " + localSum);
            }

            String globalSum = reader.readLine();
            System.out.println("Global sum: " + globalSum);
            */
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
