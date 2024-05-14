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
        GAME_OVER
    }
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java TimeClient <hostname> <port>");
            return;
        }

        String player_id_string = null;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(hostname, port)) {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            PrintWriter writer = new PrintWriter(output, true);
            Scanner scanner = new Scanner(System.in);

            State state = State.START;
            while(state != State.Exit) {
                switch (state) {
                    case START:
                        System.out.println("Welcome to the game select an option (1-3):");
                        System.out.println("1. Login");
                        System.out.println("2. Register");
                        System.out.println("3. Exit");
                        String option = scanner.next();

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
                        String username = scanner.next();
                        System.out.println("----------------------");
                        System.out.println("Enter your password: ");
                        System.out.println("----------------------");
                        String password = scanner.next();
                        writer.println("LOGIN");
                        writer.println(username);
                        writer.println(password);
                        player_id_string = reader.readLine();
                        String response = reader.readLine();

                        if (response.equals("SUCCESS")) {
                            System.out.println("----------------------");
                            System.out.println("Login \033[32msuccess \033[39m");
                            System.out.println("----------------------");
                            state = State.GAME_SELECTION;
                        } else {
                            System.out.println("----------------------");
                            System.out.println("Login \033[31mfailed\033[39m password or username is incorrect");
                            System.out.println("Please try again");
                            System.out.println("----------------------");
                        }
                        break;
                    case REGISTER:
                        System.out.println("Enter your username: ");
                        username = scanner.next();
                        System.out.println("Enter your password: ");
                        password = scanner.next();
                        writer.println("REGISTER");
                        writer.println(username);
                        writer.println(password); 
                        response = reader.readLine();

                        if (response.equals("SUCCESS")) {
                            System.out.println("Registration successful");
                            state = State.START;
                        } else {
                            System.out.println("Registration failed username already exists");
                            System.out.println("Please try again");
                        }
                        break;
                    case GAME_SELECTION:
                        System.out.println("Select a gamemode (1-3):");
                        System.out.println("1. Casual");
                        System.out.println("2. Ranked");
                        System.out.println("3. Exit");
                        option = scanner.next();

                        writer.println("GAME_SELECTION");
                        switch (option) {
                            case "1":
                                System.out.println("----------------------");
                                System.out.println("Casual mode selected");
                                System.out.println("Waiting for opponents");
                                System.out.println("----------------------");
                                writer.println("CASUAL");
                                while(!reader.ready()){
                                    //reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
                                while(!reader.ready()) {
                                    //reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                }

                                response = reader.readLine();
                                if (response.equals("GAME_FOUND")){
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

                        while (true) {
                            String line = reader.readLine();
                            if (line.equals("Your turn to guess a letter!")) {
                                System.out.println("----------------------");
                                System.out.print("Your turn to guess!\nGuess a letter or word:\n");
                                System.out.println("----------------------");
                                String guess = scanner.next();
                                writer.println(guess);
                            } else if (line.equals("GAME_OVER")) {
                                state = State.GAME_OVER;
                                break;
                            }
                            else {
                                System.out.println(line);
                            }
                        }
                    case GAME_OVER:
                        System.out.println("Game over! frfr no cap");
                        writer.println(player_id_string); // send player id to game server for reconnecting
                        state = State.GAME_SELECTION;
                        break;
                    default:
                        break;
                }
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
