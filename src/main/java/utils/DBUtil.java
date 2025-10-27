package utils;

import java.sql.*;

public class DBUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/studybooking";
    private static final String USER = "root";
    private static final String PASS = "yourpassword";
    static { try { Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) { e.printStackTrace();}
    }
    public static Connection getConnection() throws SQLException { return DriverManager.getConnection(URL, USER, PASS);
    }
}