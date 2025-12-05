package login;

import java.sql.Connection;
import java.util.regex.Pattern;

/**
 * SecurityManager - SRS Requirement 1.1: Security Management
 * 
 * Centralized security management for the Inventory System.
 * Handles:
 * - Data encryption/decryption
 * - Authorization checks
 * - Security event logging
 * - Input validation
 * - Password management
 */
public class SecurityManager {
    private final UserDAO userDAO;
    private final Connection connection;
    
    // Input validation patterns
    private static final Pattern SQL_INJECTION_PATTERN = 
        Pattern.compile(".*[;'\"\\\\].*|.*(DROP|DELETE|INSERT|UPDATE|SELECT|UNION|EXEC|EXECUTE).*", 
                       Pattern.CASE_INSENSITIVE);
    private static final Pattern XSS_PATTERN = 
        Pattern.compile(".*[<>\"'&].*", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRODUCT_NAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9\\s\\-&().,]+$");
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    public SecurityManager(UserDAO userDAO, Connection connection) {
        this.userDAO = userDAO;
        this.connection = connection;
    }
    
    /**
     * Encrypts sensitive data using password encryption utility
     * SRS 1.1: Data encryption
     * 
     * @param data Data to encrypt
     * @return Encrypted data
     */
    public String encryptData(String data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be null or empty");
        }
        
        try {
            return PasswordEncryption.hashPassword(data);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifies encrypted data against plaintext
     * SRS 1.1: Data verification
     * 
     * @param plaintext Plaintext data
     * @param encrypted Encrypted data to verify against
     * @return true if plaintext matches encrypted data
     */
    public boolean verifyEncryptedData(String plaintext, String encrypted) {
        if (plaintext == null || encrypted == null) {
            return false;
        }
        
        return PasswordEncryption.verifyPassword(plaintext, encrypted);
    }
    
    /**
     * Validates user authorization for a specific action
     * SRS 1.1: Role-based access control
     * 
     * @param user User to check authorization for
     * @param action Action to authorize
     * @param requiredRoles Roles that are allowed to perform this action
     * @return true if user is authorized
     */
    public boolean validateAuthorization(User user, String action, UserRole... requiredRoles) {
        if (user == null || !user.isActive()) {
            return false;
        }
        
        // Check if user's role is in the list of required roles
        for (UserRole role : requiredRoles) {
            if (user.getRole() == role) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if user has OWNER role
     * 
     * @param user User to check
     * @return true if user is OWNER
     */
    public boolean isOwner(User user) {
        return user != null && user.getRole() == UserRole.OWNER;
    }
    
    /**
     * Checks if user has STAFF role
     * 
     * @param user User to check
     * @return true if user is STAFF
     */
    public boolean isStaff(User user) {
        return user != null && user.getRole() == UserRole.STAFF;
    }
    
    /**
     * Logs a security event for audit trail
     * SRS 1.1: Audit logging
     * 
     * @param eventType Type of security event
     * @param user User involved in the event
     * @param details Additional event details
     */
    public void logSecurityEvent(String eventType, User user, String details) {
        if (user == null || userDAO == null) {
            System.err.println("[SECURITY] " + eventType + ": " + details);
            return;
        }
        
        try {
            String eventDetails = eventType + " - " + details;
            userDAO.logAudit(user.getUserId(), eventType, "security_events", 
                           user.getUserId(), null, eventDetails);
            System.out.println("[SECURITY] " + eventType + " by " + user.getUsername() + ": " + details);
        } catch (Exception e) {
            System.err.println("[SECURITY ERROR] Failed to log security event: " + e.getMessage());
        }
    }
    
    /**
     * Validates input to prevent SQL injection
     * SRS 1.1: Input validation
     * 
     * @param input Input string to validate
     * @return true if input is safe
     */
    public boolean validateInputSQLSafety(String input) {
        if (input == null) {
            return true; // null is acceptable
        }
        
        return !SQL_INJECTION_PATTERN.matcher(input).matches();
    }
    
    /**
     * Validates input to prevent XSS attacks
     * 
     * @param input Input string to validate
     * @return true if input is safe
     */
    public boolean validateInputXSSSafety(String input) {
        if (input == null) {
            return true; // null is acceptable
        }
        
        return !XSS_PATTERN.matcher(input).matches();
    }
    
    /**
     * Validates product name format
     * 
     * @param productName Product name to validate
     * @return true if product name is valid
     */
    public boolean validateProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return false;
        }
        
        if (productName.length() > 100) {
            return false;
        }
        
        return PRODUCT_NAME_PATTERN.matcher(productName).matches() &&
               validateInputSQLSafety(productName);
    }
    
    /**
     * Validates email format
     * 
     * @param email Email to validate
     * @return true if email is valid
     */
    public boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        if (email.length() > 100) {
            return false;
        }
        
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates username format
     * 
     * @param username Username to validate
     * @return true if username is valid
     */
    public boolean validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        if (username.length() < 3 || username.length() > 50) {
            return false;
        }
        
        // Username should be alphanumeric with underscores/dots
        return username.matches("^[a-zA-Z0-9._]+$") &&
               validateInputSQLSafety(username);
    }
    
    /**
     * Validates password strength
     * 
     * @param password Password to validate
     * @return true if password meets strength requirements
     */
    public boolean validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        // Minimum 8 characters
        if (password.length() < 8) {
            return false;
        }
        
        // Must contain uppercase
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        
        // Must contain lowercase
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        
        // Must contain digit
        if (!password.matches(".*[0-9].*")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Sanitizes input by removing potentially dangerous characters
     * 
     * @param input Input to sanitize
     * @return Sanitized input
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Remove leading/trailing whitespace
        input = input.trim();
        
        // Remove multiple spaces
        input = input.replaceAll("\\s+", " ");
        
        // Remove control characters
        input = input.replaceAll("[\\p{Cntrl}]", "");
        
        return input;
    }
    
    /**
     * Validates numeric input
     * 
     * @param input Input to validate
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return true if input is a valid number within range
     */
    public boolean validateNumericInput(String input, int min, int max) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        try {
            int value = Integer.parseInt(input.trim());
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validates numeric input for doubles
     * 
     * @param input Input to validate
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return true if input is a valid double within range
     */
    public boolean validateDoubleInput(String input, double min, double max) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        try {
            double value = Double.parseDouble(input.trim());
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Logs user login attempt
     * 
     * @param username Username attempting to login
     * @param success Whether login was successful
     */
    public void logLoginAttempt(String username, boolean success) {
        String eventType = success ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
        String details = "User: " + username;
        System.out.println("[SECURITY] " + eventType + ": " + details);
    }
    
    /**
     * Logs user logout
     * 
     * @param user User logging out
     */
    public void logLogout(User user) {
        if (user != null) {
            logSecurityEvent("LOGOUT", user, "User logged out");
        }
    }
    
    /**
     * Logs password change
     * 
     * @param user User changing password
     */
    public void logPasswordChange(User user) {
        if (user != null) {
            logSecurityEvent("PASSWORD_CHANGE", user, "User changed password");
        }
    }
    
    /**
     * Logs unauthorized access attempt
     * 
     * @param user User attempting unauthorized action
     * @param action Action attempted
     */
    public void logUnauthorizedAttempt(User user, String action) {
        if (user != null) {
            logSecurityEvent("UNAUTHORIZED_ATTEMPT", user, "Attempted: " + action);
        }
    }
}
