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

    @Inject
    public HomeController(Database db) {
        this.db = db;
    }

    public Result updateLocation(Integer userID, String location) {
        Connection connection = null;
        Statement stmt = null;


        try {
            connection = db.getConnection();
            stmt = connection.createStatement();
            stmt.executeUpdate("UPDATE users SET location=\"" + location
                    + "\" WHERE id=" + Integer.toString(userID) + ";");
            connection.close();


        } catch (SQLException e) {
            System.out.println(e);
            return internalServerError();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) { /* ignored */}
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) { /* ignored */}
            }
        }

        return ok();

    }



    public Result deleteFriend(Integer userID, String friendsPhone) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        Integer friendsID = -1;

        try {
            connection = db.getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT id FROM users WHERE phone=" + friendsPhone + ";");

            while (rs.next())
                friendsID = rs.getInt("id");


            //friend not found
            if (friendsID == -1)
                return forbidden();

            stmt.executeUpdate("DELETE FROM friends WHERE id=" + Integer.toString(friendsID) +
                    " AND friend_id=" + Integer.toString(userID) + ";");

            stmt.executeUpdate("DELETE FROM friends WHERE id=" + Integer.toString(userID) +
                    " AND friend_id=" + Integer.toString(friendsID) + ";");

        } catch (SQLException e) {
            System.out.println(e);
            return internalServerError();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
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
        }
        return ok();
    }

    public Result acceptRequest(Integer userID, String friendsPhone) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        Integer friendsID = -1;

        try {
            connection = db.getConnection();
            stmt = connection.createStatement();

            //first get friends ID from their phone number
            rs = stmt.executeQuery("SELECT id FROM users WHERE phone=" + friendsPhone + ";");
            while (rs.next()) {
                friendsID = rs.getInt("id");
            }
            if (friendsID == -1)
                return forbidden();

            //remove request from db
            stmt.executeUpdate("DELETE FROM requests WHERE id=" + Integer.toString(friendsID) +
                    " AND friend_id=" + Integer.toString(userID) + ";");
            //add friendship both ways into db
            stmt.executeUpdate("INSERT INTO friends VALUES (" + Integer.toString(userID) +
                    "," + Integer.toString(friendsID) + ");");
            stmt.executeUpdate("INSERT INTO friends VALUES (" + Integer.toString(friendsID) +
                    "," + Integer.toString(userID) + ");");
        } catch (SQLException e) {
            System.out.println(e);
            return internalServerError();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
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
        }
        return ok();
    }

    public Result rejectRequest(Integer userID, String friendsPhone) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        Integer friendsID = -1;

        try {
            connection = db.getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT id FROM users WHERE phone=" + friendsPhone + ";");

            while (rs.next())
                friendsID = rs.getInt("id");

            if (friendsID == -1)
                return forbidden();

            stmt.executeUpdate("DELETE FROM requests WHERE id=" + Integer.toString(friendsID) +
                    " AND friend_id=" + Integer.toString(userID) + ";");

        } catch (SQLException e) {
            System.out.println(e);
            return internalServerError();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
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
        }
        return ok();
    }

    public Result getAllFriendRequests(Integer userID) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayNode result = Json.newArray();

        try {
            connection = db.getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM users WHERE id IN "
                    + "(SELECT id FROM requests WHERE friend_id=" + userID + " );");

            while (rs.next()) {
                ObjectNode user = Json.newObject();
                user.put("phone", rs.getInt("phone"));
                user.put("location", rs.getString("location"));
                result.add(user);
            }

            return ok(result);

        } catch (SQLException e) {
            System.out.println(e);
            return internalServerError();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
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
        }
    }

    public Result getAllFriends(Integer userID) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayNode result = Json.newArray();

        try {
            connection = db.getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT * FROM users WHERE id IN "
                    + "(SELECT friend_id FROM friends WHERE id=" + userID + " );");

            while (rs.next()) {
                ObjectNode user = Json.newObject();
                user.put("phone", rs.getInt("phone"));
                user.put("location", rs.getString("location"));
                result.add(user);
            }

            return ok(result);

        } catch (SQLException e) {
            System.out.println(e);
            return internalServerError();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
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
        }
    }


    public Result sendFriendRequest(String phone, Integer userID) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        Integer friendsID = -1;

        try {
            connection = db.getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT id FROM users WHERE phone=" + phone + ";");

            while (rs.next())
                friendsID = rs.getInt("id");

            if (friendsID == -1)
                return forbidden();

            stmt.executeUpdate("INSERT INTO requests VALUES (" + Integer.toString(userID) + ", " + Integer.toString(friendsID) + ")");

        } catch (SQLException e) {
            System.out.println(e);
            return internalServerError();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
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
        }
        return ok();
    }


    public Result attemptLogin(String phone) {
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        Integer count = 0;
        ArrayNode result = Json.newArray();


        try {
            connection = db.getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM users WHERE phone=" + phone + ";");

            while (rs.next())
                count = rs.getInt("count");

            if (count == 1) {
                ObjectNode user = Json.newObject();
                rs = stmt.executeQuery("SELECT id FROM users WHERE phone=" + phone + ";");
                user.put("id", rs.getInt("id"));
                result.add(user);
                return ok(result);
            }

            return forbidden();

        } catch (SQLException e) {
            System.out.println(e);
            return internalServerError();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
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
        }
    }

    public Result newUser(String phone) {
        Connection connection = null;
        Integer count = 0;
        ArrayNode result = Json.newArray();
        Statement stmt = null;
        ResultSet rs = null;

        try {
            connection = db.getConnection();
            stmt = connection.createStatement();
            rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM users WHERE phone=" + phone + ";");

            while (rs.next())
                if (rs.getInt("count") != 0)
                    return forbidden("Phone already registered");

            rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM users;");

            while (rs.next())
                count = rs.getInt("count");

            stmt.executeUpdate("INSERT INTO users VALUES (" + Integer.toString(count + 1) + "," + phone + ",NULL,NULL);");
            connection.close();


        } catch (SQLException e) {
            System.out.println(e);
            return internalServerError();
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /* ignored */}
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) { /* ignored */}
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) { /* ignored */}
            }
        }

        ObjectNode user = Json.newObject();
        user.put("id", count);
        result.add(user);
        return ok(result);

    }

}
