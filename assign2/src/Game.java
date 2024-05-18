import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

enum RoundResult {
    GUESSLETTER,
    GUESSWORD,
    FAILEDGUESS,
    INVALIDGUESS
}

public class Game {
    static private List<MyPlayer> players;
    private String secretWord;
    private StringBuilder currentGuess;
    private int currentPlayerIndex;
    private List<Integer> playerScores;
    private int gameID;

    public Game(int players_num, List<MyPlayer> players,int gameID) {
        Game.players = players;
        this.secretWord = "hangman";
        this.currentGuess = new StringBuilder();
        this.currentPlayerIndex = 0;
        this.playerScores = new ArrayList<>();
        this.gameID = gameID;
    }

    public int getGameID() {
        return gameID;
    }

    public void ReconnectPlayer(MyPlayer reconnected_player) {
        // Code to add a player to the game


        for(MyPlayer player : players){
            if(player.getPlayerID() == reconnected_player.getPlayerID()){
                player.setSocket(reconnected_player.getKey());
                return;
            }
        }
        System.out.println("Player reconnected to game " + gameID + ": " + reconnected_player.getUsername());

        

    }

    public void start() {
        // Code to start the game
        System.out.println("Starting game with " + players.size() + " players");

        // Initialize the currentWordGuesses list with underscores representing each letter of the word
        for (int i = 0; i < secretWord.length(); i++) {
            currentGuess.append("_");
        }

        // Initialize the player scores
        for (int i = 0; i < players.size(); i++) {
            playerScores.add(0);
        }

        // Send the initial game state to all players
        for (MyPlayer player : players) {
            try {
                PrintWriter writer = new PrintWriter(player.getKey().getOutputStream(), true);
                writer.println("Starting Hangman game!");
                writer.println("The word has " + secretWord.length() + " letters.");
                writer.println("Current word: " + currentGuess.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Play rounds until the word is guessed
        while (!currentGuess.toString().equals(secretWord)) {
            playRound();
        }

        // Game over
        for (MyPlayer player : players) {
            try {
                PrintWriter writer = new PrintWriter(player.getKey().getOutputStream(), true);
                writer.println("Game over! The word was: " + secretWord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void playRound() {
        // Code to play a round of the game
        System.out.println("Playing round for player " + (currentPlayerIndex + 1));

        // Get the current player's socket
        MyPlayer currentPlayerSocket = players.get(currentPlayerIndex);

        // Receive the guess from the current player
        String guess = receiveGuess(currentPlayerSocket.getKey());

        RoundResult result = RoundResult.INVALIDGUESS;
        // Update the current word guesses based on the guess
        int numOccurrences = -1;
        if (guess.length() == 1) {
            numOccurrences = guessLetter(guess);
            result = numOccurrences > 0 ? RoundResult.GUESSLETTER : RoundResult.FAILEDGUESS;
        } else if (guess.length() == secretWord.length()) {
            numOccurrences = guessWord(guess);
            result = numOccurrences > 0 ? RoundResult.GUESSWORD : RoundResult.FAILEDGUESS;
        } else {
            System.out.println("Invalid guess: " + guess);
        }

        // Send the updated game state to all players
        for (MyPlayer player : players) {
            try {
                PrintWriter writer = new PrintWriter(player.getKey().getOutputStream(), true);
                switch (result) {
                    case GUESSLETTER:
                        writer.println("Player " + (currentPlayerIndex + 1) + " guessed: " + guess);
                        writer.println("Current word: " + currentGuess);
                        break;
                    case GUESSWORD:
                        writer.println("Player " + (currentPlayerIndex + 1) + " guessed the word: " + guess);
                        writer.println("Current word: " + currentGuess);
                        break;
                    case FAILEDGUESS:
                        writer.println("Player " + (currentPlayerIndex + 1) + " guessed: " + guess + " - Incorrect guess!");
                        writer.println("Current word: " + currentGuess);
                        break;
                    case INVALIDGUESS:
                        writer.println("Invalid guess: " + guess);
                        break;
                }
            } catch (IOException e) {
                int playerIndex = players.indexOf(player);
                players.remove(player);
                playerScores.remove(playerIndex);
                e.printStackTrace();
            }
        }

        // Update the player scores
        if (result == RoundResult.GUESSLETTER || result == RoundResult.GUESSWORD) {
            playerScores.set(currentPlayerIndex, playerScores.get(currentPlayerIndex) + numOccurrences);
        }
        // 
        // Move to the next player
        System.out.println("Moving to next player"+currentPlayerIndex + " " + players.size());
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private String receiveGuess(Socket socket) {
        // Code to receive a guess from a player
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("Your turn to guess a letter!");
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "Could not receive guess";
        }
    }

    private int guessLetter(String guess) {
        // Code to update the current word guesses based on a guess
        StringBuilder updatedGuess = new StringBuilder(currentGuess);
        int counter = 0;

        for (int i = 0; i < secretWord.length(); i++) {
            if (secretWord.charAt(i) == guess.charAt(0) && updatedGuess.charAt(i) == '_') {
                updatedGuess.setCharAt(i, guess.charAt(0));
                counter++;
            }
        }
        currentGuess = updatedGuess;
        return counter;
    }

    private int guessWord(String guess) {
        // Code to check if the guess is the secret word
        if (secretWord.equals(guess)) {
            int n = (int) currentGuess.toString().chars().filter(ch -> ch == '_').count();
            currentGuess = new StringBuilder(guess);
            return n;
        } else {
            return 0;
        }
    }

    public int getScore(int playerIndex) {
        return playerScores.get(playerIndex);
    }
}
