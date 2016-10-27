package parser;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple Java program to connect to MySQL database running on localhost and
 * running SELECT and INSERT query to retrieve and add data.
 *
 * @author Javin Paul
 */
public class MySQLConnect {

    // JDBC URL, username and password of MySQL server
    private static String url;
    private static String user;
    private static String password;

    // JDBC variables for opening and managing connection
    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;

    public void setUrl(String url) {
        MySQLConnect.url = url;
    }

    public void setUser(String user) {
        MySQLConnect.user = user;
    }

    public void setPassword(String password) {
        MySQLConnect.password = password;
    }

    public MySQLConnect() throws SQLException {
    
    }

    public ResultSet Query(String query) throws SQLException {
        con = DriverManager.getConnection(url, user, password);
        stmt = con.createStatement();
        // executing SELECT query
        rs = stmt.executeQuery(query);
        return rs;
    }

    public static void closeConnection() {

        //close connection ,stmt and resultset here
        try {
            con.close();
        } catch (SQLException se) {
            /*can't do anything */ }
        try {
            stmt.close();
        } catch (SQLException se) {
            /*can't do anything */ }
        try {
            rs.close();
        } catch (SQLException se) {
            /*can't do anything */ }
    }

}
