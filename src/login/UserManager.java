package login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.security.NoSuchAlgorithmException;

import product.*;
import report.*;
import sale.*;

/**
 * SECURITY NOTE: Now using PasswordEncryption.hashPassword() with SHA-256 + salt
 * for secure password storage. All password comparisons use PasswordEncryption.verifyPassword().
 */

/**
 * UserManager - Manages user authentication, role-based access, and session tracking.
 * Supports test-mode (null connection) with in-memory fallback.
 */
public class UserManager {
    private static final Map<String, User> testModeUsers = new HashMap<>();
    private Connection connection;
    private User currentUser;
    private long lastActivityTime;
    private static final long IDLE_TIMEOUT_MILLIS = 15 * 60 * 1000; // 15 minutes
    
    static {
        // Initialize test-mode users with encrypted passwords
        try {
            User owner = new User(1, "owner1", PasswordEncryption.hashPassword("validPwd"), UserRole.OWNER, "owner@nardo.com", true);
            User staff = new User(2, "staff1", PasswordEncryption.hashPassword("staffPwd"), UserRole.STAFF, "staff@nardo.com", true);
            testModeUsers.put("owner1", owner);
            testModeUsers.put("staff1", staff);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error initializing test users: " + e.getMessage());
        }
    }
    
    public UserManager(Connection connection) {
        this.connection = connection;
        this.currentUser = null;
        this.lastActivityTime = System.currentTimeMillis();
    }
    
    /**
     * Authenticate user with username and password.
     * @param username Username
     * @param password Plain text password
     * @return Authenticated User or null if authentication fails
     */
    public User authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            System.out.println("Authentication failed: Empty username or password");
            return null;
        }
        
        try {
            User user = null;
            
            if (connection != null) {
                // Database-backed authentication
                String query = "SELECT * FROM users WHERE username = ? AND is_active = TRUE";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, username);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String storedPassword = rs.getString("password");
                            if (PasswordEncryption.verifyPassword(password, storedPassword)) {
                                user = new User(
                                    rs.getInt("user_id"),
                                    rs.getString("username"),
                                    storedPassword,
                                    UserRole.valueOf(rs.getString("role")),
                                    rs.getString("email"),
                                    true
                                );
                            }
                        }
                    }
                }
            } else {
                // Test-mode: use in-memory users
                user = testModeUsers.get(username);
                if (user != null && PasswordEncryption.verifyPassword(password, user.getPassword())) {
                    // Password matches
                } else {
                    user = null;
                }
            }
            
            if (user != null) {
                this.currentUser = user;
                this.lastActivityTime = System.currentTimeMillis();
                System.out.println("User authenticated: " + username + " (Role: " + user.getRole() + ")");
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        
        System.out.println("Authentication failed for username: " + username);
        return null;
    }
    
    /**
     * Check if current session is valid (not idle beyond timeout).
     * @return true if session is valid
     */
    public boolean isSessionValid() {
        if (currentUser == null) {
            return false;
        }
        
        long elapsedTime = System.currentTimeMillis() - lastActivityTime;
        if (elapsedTime > IDLE_TIMEOUT_MILLIS) {
            System.out.println("Session timeout: User " + currentUser.getUsername() + " idle for " + (elapsedTime / 1000) + " seconds");
            logout();
            return false;
        }
        
        // Update last activity time
        this.lastActivityTime = System.currentTimeMillis();
        return true;
    }
    
    /**
     * Logout current user.
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("User logged out: " + currentUser.getUsername());
            this.currentUser = null;
        }
    }
    
    /**
     * Get current authenticated user.
     * @return Current user or null
     */
    public User getCurrentUser() {
        if (isSessionValid()) {
            return currentUser;
        }
        return null;
    }
    
    /**
     * Check if current user has permission for a specific role.
     * @param requiredRole Role required to perform action
     * @return true if user has the required role or higher
     */
    public boolean hasPermission(UserRole requiredRole) {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }
        
        // OWNER has all permissions
        if (user.getRole() == UserRole.OWNER) {
            return true;
        }
        
        // STAFF can only perform non-admin operations
        if (requiredRole == UserRole.STAFF) {
            return user.getRole() == UserRole.STAFF || user.getRole() == UserRole.OWNER;
        }
        
        return false;
    }
    
    /**
     * Create a new user (OWNER only).
     */
    public boolean createUser(String username, String password, UserRole role, String email) {
        if (!hasPermission(UserRole.OWNER)) {
            System.out.println("Permission denied: Only OWNER can create users");
            return false;
        }
        
        try {
            String encryptedPassword = PasswordEncryption.hashPassword(password);
            if (connection != null) {
                String query = "INSERT INTO users (username, password, role, email, is_active) VALUES (?, ?, ?, ?, TRUE)";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, username);
                    stmt.setString(2, encryptedPassword);
                    stmt.setString(3, role.toString());
                    stmt.setString(4, email);
                    stmt.executeUpdate();
                    System.out.println("User created: " + username + " with role " + role);
                    return true;
                }
            } else {
                // Test-mode: add to in-memory users
                User newUser = new User(testModeUsers.size() + 1, username, encryptedPassword, role, email, true);
                testModeUsers.put(username, newUser);
                System.out.println("Test-mode user created: " + username);
                return true;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.err.println("Error creating user: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Change password for current user.
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }
        
        // Verify old password
        if (!PasswordEncryption.verifyPassword(oldPassword, user.getPassword())) {
            System.out.println("Password change failed: Incorrect old password");
            return false;
        }
        
        try {
            String encryptedPassword = PasswordEncryption.hashPassword(newPassword);
            if (connection != null) {
                String query = "UPDATE users SET password = ? WHERE user_id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.setString(1, encryptedPassword);
                    stmt.setInt(2, user.getUserId());
                    stmt.executeUpdate();
                    user.setPassword(encryptedPassword);
                    System.out.println("Password changed for user: " + user.getUsername());
                    return true;
                }
            } else {
                // Test-mode
                user.setPassword(encryptedPassword);
                testModeUsers.put(user.getUsername(), user);
                System.out.println("Test-mode password changed for user: " + user.getUsername());
                return true;
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.err.println("Error changing password: " + e.getMessage());
        }
        
        return false;
    }
    
    
    /**
     * Get all active users (OWNER only).
     */
    public List<User> getAllUsers() {
        if (!hasPermission(UserRole.OWNER)) {
            System.out.println("Permission denied: Only OWNER can view all users");
            return new ArrayList<>();
        }
        
        List<User> users = new ArrayList<>();
        try {
            if (connection != null) {
                String query = "SELECT * FROM users WHERE is_active = TRUE";
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {
                    while (rs.next()) {
                        users.add(new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            UserRole.valueOf(rs.getString("role")),
                            rs.getString("email"),
                            true
                        ));
                    }
                }
            } else {
                // Test-mode
                users.addAll(testModeUsers.values());
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        
        return users;
    }
}
