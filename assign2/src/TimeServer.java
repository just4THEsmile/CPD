import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;
 
/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */



public class TimeServer extends Thread{
    static int total = 0;
    int local = 0;
    Socket socket;
    static ReentrantLock lock;

    public TimeServer(Socket socket, ReentrantLock lock) {
        this.socket = socket;
        this.lock = lock;
    }
    public int  get_Total(){
        return total;
    }
    public int get_finish_value(){
        return local;
    }
    public Socket get_Socket(){
        return socket;
    }
    @Override
    public void run(){
        int value = 0;
        try {
            System.out.println("New client connected: "+ socket.getPort());
            while(socket != null){

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                
                String time = reader.readLine();
                if (time != null) {
                    value += Integer.parseInt(time);
                    

                    OutputStream output = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);

                    writer.println(value);
                }else{
                    
                    socket.close();
                    socket = null;
                }
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("total"+total);
        System.out.println("total"+value);
        lock.lock();
        total += value;
        lock.unlock();
        System.out.println("total"+total);
        System.err.println("Thread running");
    }
 
    public static void main(String[] args) {
        if (args.length < 1) return;
        ReentrantLock lock = new ReentrantLock();
        int port = Integer.parseInt(args[0]);
        ArrayList<TimeServer> threads = new ArrayList<TimeServer>();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
 
            System.out.println("Server is listening on port " + port);
            Socket socket = null;
            while (true) {

                while (socket == null) {
                    socket = serverSocket.accept();
                    TimeServer thread= new TimeServer(socket,lock);
                    threads.add(thread);
                    thread.start();
                    socket=null;
                }

                

            }
 
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}