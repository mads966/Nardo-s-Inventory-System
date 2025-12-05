package com.nardos.inventory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {

    private static final String URL = "jdbc:mysql://localhost:3306/nardos_inventory?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = ""; 

    public static Connection getConnection() throws SQLException {
        try {
           
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return DriverManager.getConnection(URL, USER, PASS);
    }
}
