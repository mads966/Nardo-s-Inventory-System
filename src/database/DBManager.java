
package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Manager - SRS Component: 2.3.2 Database Layer
 * 
 * Manages JDBC connection lifecycle for the inventory system.
 * Implements singleton pattern to maintain a single database connection
 * throughout the application lifetime.
 * 
 * Responsibilities:
 * - Establish MySQL database connection
 * - Verify connection validity
 * - Gracefully close connection on application exit
 * - Handle connection failures with meaningful error messages
 * 
 * Configuration:
 * - Database: nardos_inventory (MySQL)
 * - Host: localhost:3306
 * - Credentials: Configured in DATABASE constants
 * - Driver: MySQL Connector/J (com.mysql.cj.jdbc.Driver)
 * 
 * Usage:
 *   Connection conn = DBManager.getConnection();
 *   // Perform database operations
 *   DBManager.closeConnection();
 * 
 * Thread Safety: Connection is managed statically (not thread-safe for concurrent use)
 * For multi-threaded applications, consider using connection pooling.
 */
public class DBManager {
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/nardos_inventory";
    private static final String DATABASE_USER = "root";
    private static final String DATABASE_PASSWORD = "";
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    private static Connection connection = null;
    
    /**
     * Retrieves the active database connection, creating one if necessary.
     * 
     * @return Active database connection
     * @throws SQLException If connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName(JDBC_DRIVER);
                connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
                System.out.println("Database connection established successfully");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found: " + JDBC_DRIVER, e);
            } catch (SQLException e) {
                throw new SQLException("Failed to establish database connection to " + DATABASE_URL, e);
            }
        }
        return connection;
    }
    
    /**
     * Safely closes the database connection.
     * Called on application shutdown.
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Verifies if the database connection is currently active.
     * 
     * @return true if connection exists and is not closed
     */
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}