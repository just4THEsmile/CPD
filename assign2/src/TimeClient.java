import java.net.*;
import java.io.*;
 
/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */
public class TimeClient {
 
    public static void main(String[] args) {
        if (args.length < 2) return;
 
        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
        System.out.println("Connected to the server...");
        try (Socket socket = new Socket(hostname, port)) {
            for (int i = 1; i < 100; i++) {
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                System.out.println("Received value: " + i*2);
            
                writer.println(i*2);
                

    
                String time = reader.readLine();

                System.out.println(time);
                
                Thread.sleep(1000);
            }
 
 
        } catch (UnknownHostException ex) {
 
            System.out.println("Server not found: " + ex.getMessage());
 
        } catch (IOException ex) {
 
            System.out.println("I/O error: " + ex.getMessage());
        }catch (InterruptedException ex) {
 
            System.out.println("Interrupted error: " + ex.getMessage());
        }
    }
}