package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;

import play.db.Database;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * This controller contains an action to handle HTTP requests
 */
public class HomeController extends Controller {

    private final Database db;

    //we won't need more than one at a time
    //by making them private variables we can release
    // the resources much easier
    private Connection connection;
    private Statement stmt;
    private ResultSet rs;

    @Inject
    public HomeController(Database db) {
        this.db = db;
    }

    private void updateDatabase(String query) throws SQLException {

        try {
            connection = db.getConnection();
            stmt = connection.createStatement();
            stmt.executeUpdate(query);

        } catch (SQLException e) {
            System.out.println(e);
            throw e;

        }
    }

    private ResultSet getFromDatabase(String query) throws SQLException {

        try {
            connection = db.getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery(query);

            return rs;

        } catch (SQLException e) {
            System.out.println(e);
            throw e;
        }
    }

    private void freeResources() {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }

    private Integer getFriendID(String friendsPhone){

        int friendsID = -1;
        try{
            rs = getFromDatabase("SELECT id FROM users WHERE phone=\"" + friendsPhone + "\";");
            while (rs.next())
                friendsID = rs.getInt("id");

        } catch (SQLException e){
            System.out.println(e);
        } finally {
            freeResources();
            return friendsID;
        }

    }

    //All functions using the database should be wrapped in a try/finally block
    //to ensure the resources will be released at the end

    public Result updateLocation(Integer userID, String location) {

        try {
            updateDatabase("UPDATE users SET location=\"" + location
                    + "\" WHERE id=" + Integer.toString(userID) + ";");
            return ok();
        } catch (SQLException e) {
            return internalServerError();
        } finally {
            freeResources();
        }

    }


    public Result deleteFriend(Integer userID, String friendsPhone) {

        try {
            Integer friendsID = getFriendID(friendsPhone);

//        friend not found
            if (friendsID == -1)
                return forbidden();

            //multiple queries in one call can only be done with updateDatabase
            updateDatabase("DELETE FROM friends WHERE id=" + Integer.toString(friendsID)
                    + " AND friend_id=" + Integer.toString(userID) + ";"
                    + " DELETE FROM friends WHERE id=" + Integer.toString(userID)
                    + " AND friend_id=" + Integer.toString(friendsID) + ";");
            return ok();
        } catch (SQLException e) {
            return internalServerError();
        } finally {
            freeResources();
        }

    }

    public Result acceptRequest(Integer userID, String friendsPhone) {

        try {
            Integer friendsID = getFriendID(friendsPhone);

            if (friendsID == -1)
                return forbidden();

            //remove request from db and both friendships to it
            String query = "DELETE FROM requests WHERE id=" + Integer.toString(friendsID)
                    + " AND friend_id=" + Integer.toString(userID) + ";";

            query += " INSERT INTO friends VALUES (" + Integer.toString(userID)
                    + "," + Integer.toString(friendsID) + ");";

            query += " INSERT INTO friends VALUES (" + Integer.toString(friendsID)
                    + "," + Integer.toString(userID) + ");";

            updateDatabase(query);
            return ok();
        } catch (SQLException e) {
            return internalServerError();
        } finally {
            freeResources();
        }


    }

    public Result rejectRequest(Integer userID, String friendsPhone) {

        try {
            int friendsID = getFriendID(friendsPhone);

            if (friendsID == -1)
                return forbidden();

            updateDatabase("DELETE FROM requests WHERE id=" + Integer.toString(friendsID)
                    + " AND friend_id=" + Integer.toString(userID) + ";");
            return ok();
        } catch (SQLException e) {
            return internalServerError();
        } finally {
            freeResources();
        }

    }

    public Result getAllFriendRequests(Integer userID) {
        ArrayNode result = Json.newArray();

        try {
            rs = getFromDatabase("SELECT * FROM users WHERE id IN "
                    + "(SELECT id FROM requests WHERE friend_id=" + userID + " );");

            while (rs.next()) {
                ObjectNode user = Json.newObject();
                user.put("phone", rs.getInt("phone"));
                user.put("location", rs.getString("location"));
                result.add(user);
            }
            return ok(result);

        } catch (SQLException e) {
            return internalServerError();
        } finally {
            freeResources();

        }

    }

    public Result getAllFriends(Integer userID) {

        try {

            ArrayNode result = Json.newArray();
            rs = getFromDatabase("SELECT * FROM users WHERE id IN "
                    + "(SELECT friend_id FROM friends WHERE id=" + userID + " );");

            while (rs.next()) {
                ObjectNode user = Json.newObject();
                user.put("phone", rs.getInt("phone"));
                user.put("location", rs.getString("location"));
                result.add(user);
            }
            return ok(result);

        } catch (SQLException e) {
            return internalServerError();
        } finally {
            freeResources();
        }

    }


    public Result sendFriendRequest(String phone, Integer userID) {

        try {
            Integer friendsID = getFriendID(phone);

            if (friendsID == -1)
                return forbidden("Friend not found");

            updateDatabase("INSERT INTO requests VALUES (" + Integer.toString(userID) + ", "
                    + Integer.toString(friendsID) + ")");
            return ok();
        } catch (SQLException e) {
            return internalServerError();
        } finally {
            freeResources();
        }

    }


    public Result attemptLogin(String phone) {

        try {
            Integer count = 1;
            ArrayNode result = Json.newArray();
            rs = getFromDatabase("SELECT COUNT(*) AS count FROM users WHERE phone=\"" + phone + "\";");

            while (rs.next())
                count = rs.getInt("count");

            if (count == 1) {
                ObjectNode user = Json.newObject();
                rs = getFromDatabase("SELECT id FROM users WHERE phone=\"" + phone + "\";");
                user.put("id", rs.getInt("id"));
                result.add(user);
                return ok(result);
            } else
                return forbidden("User not found");
        } catch (SQLException e) {
            System.out.println(e);
            return internalServerError();
        } finally {
            freeResources();
        }


    }

    public Result newUser(String phone) {

        Integer max = 0;
        ArrayNode result = Json.newArray();

        try {
            rs = getFromDatabase("SELECT COUNT(*) AS count FROM users WHERE phone=\"" + phone + "\";");
            while (rs.next())
                if (rs.getInt("count") != 0)
                    return forbidden("Phone already registered");

            rs = getFromDatabase("SELECT MAX(id) AS max FROM users;");
            while (rs.next())
                max = rs.getInt("max");

            updateDatabase("INSERT INTO users VALUES (" + Integer.toString(max + 1) + ","
                    + phone + ",NULL,NULL);");
            ObjectNode user = Json.newObject();
            user.put("id", max);
            result.add(user);
            return ok(result);

        } catch (SQLException e) {
            return internalServerError("Could not insert user into db");
        } finally {
            freeResources();
        }


    }

}

