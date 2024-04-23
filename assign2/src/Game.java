import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

enum RoundResult {
    GUESSLETTER,
    GUESSWORD,
    FAILEDGUESS
}
public class Game {
    private List<Socket> userSockets;
    private String secretWord;
    private StringBuilder currentGuess;
    private int currentPlayerIndex;
    private List<int> playerScores;

    public Game(int players, List<Socket> userSockets) {
        this.userSockets = userSockets;
        this.secretWord = "hangman";
        this.currentGuess = new StringBuilder();
        this.currentPlayerIndex = 0;
        this.playerScores = new ArrayList<>();
    }

    public void start() {
        // Code to start the game
        System.out.println("Starting game with " + userSockets.size() + " players");

        // Initialize the currentWordGuesses list with underscores representing each letter of the word
        for (int i = 0; i < secretWord.length(); i++) {
            currentGuess.append("_");
        }

        // Initialize the player scores
        for (int i = 0; i < userSockets.size(); i++) {
            playerScores.add(0);
        }

        // Send the initial game state to all players
        for (Socket socket : userSockets) {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("Starting Hangman game!");
                writer.println("The word has " + secretWord.length() + " letters.");
                writer.println("Current word: " + initialGuess.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Play rounds until the word is guessed
        while (!currentGuess.toString().equals(secretWord)) {
            playRound();
        }

        // Game over
        for (Socket socket : userSockets) {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.println("Game over! The word was: " + secretWord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void playRound() {
        // Code to play a round of the game
        System.out.println("Playing round for player " + currentPlayerIndex);

        // Get the current player's socket
        Socket currentPlayerSocket = userSockets.get(currentPlayerIndex);

        // Receive the guess from the current player
        String guess = receiveGuess(currentPlayerSocket);

        RoundResult result;
        // Update the current word guesses based on the guess
        int numOccurrences;
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
        for (Socket socket : userSockets) {
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                switch (result) {
                    case GUESSLETTER:
                        writer.println("Player " + currentPlayerIndex + " guessed: " + guess);
                        writer.println("Current word: " + currentWordGuesses.get(currentWordGuesses.size() - 1));
                        break;
                    case GUESSWORD:
                        writer.println("Player " + currentPlayerIndex + " guessed the word: " + guess);
                        writer.println("Current word: " + currentWordGuesses.get(currentWordGuesses.size() - 1));
                        break;
                    case FAILEDGUESS:
                        writer.println("Player " + currentPlayerIndex + " guessed: " + guess + " - Incorrect guess!");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Update the player scores
        if (result == RoundResult.GUESSLETTER || result == RoundResult.GUESSWORD) {
            playerScores.set(currentPlayerIndex, playerScores.get(currentPlayerIndex) + numOccurrences);
        }

        // Move to the next player
        currentPlayerIndex = (currentPlayerIndex + 1) % userSockets.size();
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
            if (secretWord.charAt(i) == guess.charAt(0) && updatedGuess.charAt(i) == '_'{
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
            currentGuess = new StringBuilder(secretWord);
            return currentGuess.toString().chars().filter(ch -> ch == '_').count();
        } else {
            return 0;
        }
    }
}
