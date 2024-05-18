import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseController
{
    private static Connection connection;
    private static Statement statement;

    public DatabaseController() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
            this.statement = this.connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            ResultSet rs = statement.executeQuery("select * from person");
            System.out.println("------------");
            System.out.println("----test_db----");
            System.out.println("------------");
            while(rs.next()) {
                // read the result set
                System.out.println("name = " + rs.getString("name"));
                System.out.println("password = " + rs.getString("password"));
                System.out.println("id= " + rs.getInt("id"));
                System.out.println("score = " + rs.getString("score"));
            }
            // remove games
            statement.executeUpdate("drop table if exists game_person");
            statement.executeUpdate("drop table if exists game");
            statement.executeUpdate("create table game (game_id integer primary key)");
            statement.executeUpdate("create table game_person (game_id integer, person_id integer, primary key (game_id, person_id))");

            System.out.println("------------");
            System.out.println("----test_db----");
            System.out.println("------------");
        }
        catch(SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.out.println("bruhh1");
            e.printStackTrace(System.err);
        }

    }

    public static Connection getConnection(){
        return connection;
    }
    public static Statement getStatement(){
        return statement;
    }
    public static void closeConnection(){
        try {
            connection.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static int createGame(List <String> players_id){
        int max_game_id = 0;
        try{
            String sql = "SELECT MAX(game_id) AS max_game_id FROM game";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs= statement.executeQuery();
            if(!rs.next()){
                max_game_id = 0;
            }else{
                max_game_id = rs.getInt("max_game_id");
                max_game_id++;
            }
            String sqlString = "insert into game values(?)";
            statement = connection.prepareStatement(sqlString);
            statement.setInt(1, max_game_id);
            statement.executeUpdate();
            for(String player_id: players_id){


                String sqlString3 = "insert into game_person values(?, ?)";
                PreparedStatement statement3 = connection.prepareStatement(sqlString3);
                statement3.setInt(1, max_game_id);
                statement3.setString(2, player_id);
                statement3.executeUpdate();
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return max_game_id;
    }

    public static void deleteGame(int game_id){
        try{
            String sql = "delete from game where game_id = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, game_id);
            statement.executeUpdate();
            sql = "delete from game_person where game_id = ?;";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, game_id);
            statement.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static void addPlayer(String player, String password) {
        try {
            ResultSet rs = statement.executeQuery("SELECT MAX(id) AS max_person_id FROM person;");
            int person_id = rs.getInt("max_person_id");
            person_id++;
            String sql = "insert into person values(?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, person_id);
            statement.setString(2, player);
            statement.setString(3, password);
            statement.setInt(4, 0);
            statement.executeUpdate();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static int login(String player, String password){
        try{
            String sql = "select id, name, score from person where name = ? and password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, player);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            
            if(!rs.next()){
                return -1;
            }


            return rs.getInt("id");
        }catch(SQLException e){
            e.printStackTrace();
        }
        return -1;
    }

    public static String getUsername(int id){
        try{
            String sql = "select name from person where id = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()){
                return null;
            }
            return rs.getString("name");
        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public static int register(String player, String password){
        try{
            String sql = "select id from person where name = ?;";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, player);
            ResultSet rs = statement.executeQuery();
            if(rs.next()){
                System.out.println("User already exists");
                return -1;
            }
            addPlayer(player, password);
            return login(player, password);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return -1;
    }

    public static String getPassword(String player){
        try{
            ResultSet rs = statement.executeQuery("select password from person where name = "+player+";");
            return rs.getString("password");
        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    public static int get_game_from_user(int player) {
        try{
            String sqlString = "select game_id from game_person where person_id = ?;";
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setInt(1, player);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()){
                System.out.println("not playing any game");
                return -1;
            }
            System.out.println("playing an game");
            return rs.getInt("game_id");
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }




    public static void updateScore(int player, int score){
        try{
            String sqlString = "select score from person where id = ?;";
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setInt(1, player);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()){
                System.out.println("ERROR");
                return;
            }
            String sql = "update person set score = ? where id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, rs.getInt("score")+score);
            statement.setInt(2, player);
            statement.executeUpdate();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static int getScore(int id){
        try{
            String sqlString = "select score from person where id = ?;";
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if(!rs.next()){
                System.out.println("get score Error");

                return -1;
            }
            return rs.getInt("score");
        }catch(SQLException e){
            e.printStackTrace();
            return -1;
        }

    }



    public static void main(String[] args)
    {
        try
        {

            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
            statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists person");
            statement.executeUpdate("drop table if exists game");
            statement.executeUpdate("drop table if exists game_person");

            statement.executeUpdate("create table game (game_id integer primary key)");
            statement.executeUpdate("create table game_person (game_id integer, person_id integer, primary key (game_id, person_id))");
            statement.executeUpdate("create table person (id integer primary key autoincrement, name string, password string, score integer)");

            ResultSet rs = statement.executeQuery("SELECT MAX(id) AS max_person_id FROM person;");

            statement.executeUpdate("insert into person values(0 ,'admin',1234, 0)");
            statement.executeUpdate("insert into person values( 1,'camaramenn',1234, 99999999)");

            rs = statement.executeQuery("select * from person");
            while(rs.next())
            {
                // read the result set
                System.out.println("name = " + rs.getString("name"));
                System.out.println("id = " + rs.getInt("id"));
                System.out.println("score = " + rs.getString("score"));
            }
        }
        catch(SQLException e)
        {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.out.println("bruhh");
            e.printStackTrace(System.err);
        }

    }
}