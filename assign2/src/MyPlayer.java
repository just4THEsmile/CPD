import java.net.*;

public class MyPlayer {
    private Socket socket;
    private Integer player_id;
    private String username;
    private Integer score;

    public MyPlayer(Socket socket, Integer player_id ,String username, Integer score) {
        this.socket = socket;
        this.username = username;
        this.player_id = player_id;
        this.score = score ;
    }

    // Getter methods for key and value
    public Socket getKey() {
        return socket;
    }

    public Integer getValue() {
        return score;
    }
    public Integer getPlayerID() {
        return player_id;
    }
    public String getUsername() {
        return username;
    }
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}