import java.io.*;
 
/**
 * This program demonstrates a simple TCP/IP socket client.
 *
 * @author www.codejava.net
 */

import java.net.*;
import java.util.Scanner;

public class TimeClient {
    enum State{
        START,
        LOGIN,
        REGISTER,
        LOGOUT,
        Exit,
        GAME_SELECTION,
        PLAYING,
    }
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java TimeClient <hostname> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {

            State state = State.START;
            while(state != State.Exit){
                switch (state) {
                    case START:
                        System.out.println("Welcome to the game select an option (1-3):");
                        System.out.println("1. Login");
                        System.out.println("2. Register");
                        System.out.println("3. Exit");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));   
                        String option = reader.readLine();
                        switch (option) {
                            case "1":
                                state = State.LOGIN;
                                break;
                            case "2":
                                state = State.REGISTER;
                                break;
                            case "3":
                                state = State.Exit;
                                break;
                            default:
                                break;
                        }
                        break;
                    case LOGIN:
                        System.out.println("----------------------");
                        System.out.println("Enter your username: ");
                        System.out.println("----------------------");
                        reader = new BufferedReader(new InputStreamReader(System.in));   
                        String username = reader.readLine();
                        System.out.println("----------------------");
                        System.out.println("Enter your password: ");
                        System.out.println("----------------------");
                        String password = reader.readLine();
                        OutputStream output = socket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);
                        writer.println("LOGIN");
                        writer.println(username);
                        writer.println(password); 
                        InputStream input = socket.getInputStream();
                        BufferedReader server_reader = new BufferedReader(new InputStreamReader(input));
                        String response = server_reader.readLine();
                        if(response.equals("SUCCESS")){
                            System.out.println("----------------------");
                            System.out.println("Login \033[32msuccess \033[39m");
                            System.out.println("----------------------");
                            state = State.GAME_SELECTION;
                        }else{
                            System.out.println("----------------------");
                            System.out.println("Login \033[31mfailed\033[39m password or username is incorrect");
                            System.out.println("Please try again");
                            System.out.println("----------------------");
                        }

                        break;
                    case REGISTER:
                        System.out.println("Enter your username: ");
                        reader = new BufferedReader(new InputStreamReader(System.in));   
                        username = reader.readLine();
                        System.out.println("Enter your password: ");
                        password = reader.readLine();
                        output = socket.getOutputStream();
                        writer = new PrintWriter(output, true);
                        writer.println("REGISTER");
                        writer.println(username);
                        writer.println(password); 
                        input = socket.getInputStream();
                        server_reader = new BufferedReader(new InputStreamReader(input));
                        response = server_reader.readLine();
                        if(response.equals("SUCCESS")){
                            System.out.println("Registration successful");
                            state = State.LOGIN;
                        }else{
                            System.out.println("Registration failed username already exists");
                            System.out.println("Please try again");
                        }
                        break;    

                    case GAME_SELECTION:
                        System.out.println("Select a gamemode (1-3):");
                        System.out.println("1. Casual");
                        System.out.println("2. Ranked");
                        System.out.println("3. Exit");
                        reader = new BufferedReader(new InputStreamReader(System.in));
                        option = reader.readLine();

                        output = socket.getOutputStream();
                        writer = new PrintWriter(output, true);
                        writer.println("GAME_SELECTION");
                        switch (option) {
                            case "1":
                                System.out.println("----------------------");
                                System.out.println("Casual mode selected");
                                System.out.println("Waiting for opponents");
                                System.out.println("----------------------");
                                writer.println("CASUAL");
                                while(!reader.ready()){
                                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                }

                                response = reader.readLine();
                                System.out.println("response");
                                System.out.println(response);
                                if(response.equals("GAME_FOUND")){
                                    System.out.println("----------------------");
                                    System.out.println("Found a game!");
                                    System.out.println("----------------------");
                                    state = State.PLAYING;
                                }
                                break;
                            case "2":
                                System.out.println("----------------------");
                                System.out.println("Ranked mode selected");
                                System.out.println("Waiting for opponents");
                                System.out.println("----------------------");
                                writer.println("RANKED");
                                while(!reader.ready()){
                                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                }
                                if(reader.readLine().equals("GAME_FOUND")){
                                    System.out.println("----------------------");
                                    System.out.println("Found a game!");
                                    System.out.println("----------------------");
                                    state = State.PLAYING;
                                }
                                
                                break;
                            case "3":
                                state = State.Exit;
                                break;
                            default:
                                break;
                        }
                        break;
                    case PLAYING:
                        System.out.println("----------------------");
                        System.out.println("Playing game");
                        System.out.println("----------------------");
                        output = socket.getOutputStream();
                        writer = new PrintWriter(output, true);
                        input = socket.getInputStream();
                        reader = new BufferedReader(new InputStreamReader(input));
                        Scanner scanner = new Scanner(System.in);
                        while (true) {
                            String line = reader.readLine();
                            if (line.equals("Your turn to guess a letter!")) {
                                System.out.println("----------------------");
                                System.out.println("Your turn to guess!\nGuess a letter or word:");
                                System.out.println("----------------------");
                                String guess = scanner.next();
                                writer.println(guess);
                            } else {
                                System.out.println(line);
                            }
                        }  
                
                    default:
                        break;
                }
            }

            
            /* 
            for (int i = 1; i < 100; i++) {
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                System.out.println(time);
                
                Thread.sleep(1000);
            } */
 
 

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
