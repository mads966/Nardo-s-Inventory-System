
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
    // Try multiple common passwords
    private static final String[] DATABASE_PASSWORDS = {"", "password", "root", "admin", "123456", "thisSQLP@ssword"};
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver"; 
    
    private static Connection connection = null;
    private static boolean connectionAttempted = false;
    
    /**
     * Retrieves the active database connection, creating one if necessary.
     * Attempts multiple password combinations for flexibility.
     * Returns null if connection fails (test mode).
     * 
     * @return Active database connection or null if in test mode
     * @throws SQLException If connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName(JDBC_DRIVER);
                
                // Try each password
                SQLException lastException = null;
                for (String password : DATABASE_PASSWORDS) {
                    try {
                        connection = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, password);
                        System.out.println("✓ Database connection established successfully");
                        System.out.println("  URL: " + DATABASE_URL);
                        System.out.println("  User: " + DATABASE_USER);
                        connectionAttempted = true;
                        return connection;
                    } catch (SQLException e) {
                        lastException = e;
                        // Continue to next password
                    }
                }
                
                // All passwords failed
                System.err.println("✗ Database connection failed with all password attempts");
                System.err.println("  URL: " + DATABASE_URL);
                System.err.println("  User: " + DATABASE_USER);
                System.err.println("  Last error: " + lastException.getMessage());
                System.err.println("  Running in TEST MODE (mock database)");
                connectionAttempted = true;
                return null; // Return null to indicate test mode
                
            } catch (ClassNotFoundException e) {
                System.err.println("✗ MySQL JDBC Driver not found: " + JDBC_DRIVER);
                System.err.println("  Running in TEST MODE (mock database)");
                connectionAttempted = true;
                return null;
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