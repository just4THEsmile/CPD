public class MyPair<Socket, Integer> {
    private Socket key;
    private Integer value;

    public MyPair(Socket key, Integer value) {
        this.key = key;
        this.value = value;
    }

    // Getter methods for key and value
    public Socket getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }
}