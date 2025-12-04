package login;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Service - SRS Requirement 1.1: User Authentication
 * 
 * Implements business logic for user authentication including:
 * - Credential validation against encrypted passwords
 * - Failed attempt tracking and account lockout
 * - Password strength enforcement
 * - Role-based access control
 * 
 * Security Features (SRS 1.1.1 - 1.1.4):
 * - Tracks failed login attempts per account
 * - Temporary account lockout after 3 failed attempts (15 minutes)
 * - Password change validation (requires old password)
 * - User creation restricted to OWNER role only
 * - All passwords encrypted with SHA-256 + salt (PasswordEncryption)
 * 
 * Failure Tracking:
 * - Consecutive failed attempts tracked per username
 * - Attempts reset on successful login
 * - Attempts cleared after account lockout expires
 * - Lockout time: 15 minutes (SRS 1.1.4 requirement)
 * 
 * Usage Flow:
 * 1. LoginPage collects username/password
 * 2. AuthenticationService.authenticate() validates credentials
 * 3. If failed: increment attempt counter, possibly lock account
 * 4. If successful: clear failed attempts, return User object
 */
public class AuthenticationService {
    private final UserManager userManager;
    private final Map<String, Integer> failedAttempts;
    private final Map<String, Long> lockedAccounts;
    
    private static final int MAXIMUM_FAILED_ATTEMPTS = 3;
    private static final long ACCOUNT_LOCKOUT_DURATION_MS = 15 * 60 * 1000;
    
    public AuthenticationService(UserManager userManager) {
        this.userManager = userManager;
        this.failedAttempts = new HashMap<>();
        this.lockedAccounts = new HashMap<>();
    }
    
    /**
     * Authenticates a user with username and password.
     * SRS 1.1.1: Validates credentials against database records.
     * SRS 1.1.4: Implements account lockout after 3 failed attempts.
     * 
     * @param username User's login name
     * @param password User's password (will be encrypted for comparison)
     * @return User object if authentication succeeds, null otherwise
     */
    public User authenticate(String username, String password) {
        if (isAccountLocked(username)) {
            return null;
        }
        
        User user = userManager.authenticate(username, password);
        
        if (user != null) {
            failedAttempts.remove(username);
            return user;
        } else {
            recordFailedAttempt(username);
            return null;
        }
    }
    
    /**
     * Records a failed authentication attempt and locks account if threshold exceeded.
     * SRS 1.1.4: Lock account after 3 failed attempts.
     */
    private void recordFailedAttempt(String username) {
        int attempts = failedAttempts.getOrDefault(username, 0) + 1;
        failedAttempts.put(username, attempts);
        
        if (attempts >= MAXIMUM_FAILED_ATTEMPTS) {
            lockedAccounts.put(username, System.currentTimeMillis());
            JOptionPane.showMessageDialog(null,
                "Account locked due to too many failed attempts.\n" +
                "Please try again in 15 minutes or contact administrator.",
                "Account Locked",
                JOptionPane.ERROR_MESSAGE);
        } else {
            int remaining = MAXIMUM_FAILED_ATTEMPTS - attempts;
            JOptionPane.showMessageDialog(null,
                "Invalid credentials. " + remaining + " attempts remaining.",
                "Authentication Failed",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Checks if an account is currently locked due to failed attempts.
     * SRS 1.1.4: Lockout expires after 15 minutes.
     */
    private boolean isAccountLocked(String username) {
        Long lockTime = lockedAccounts.get(username);
        if (lockTime != null) {
            long elapsed = System.currentTimeMillis() - lockTime;
            if (elapsed < ACCOUNT_LOCKOUT_DURATION_MS) {
                long remainingMinutes = (ACCOUNT_LOCKOUT_DURATION_MS - elapsed) / (60 * 1000);
                JOptionPane.showMessageDialog(null,
                    "Account is locked. Please try again in " + 
                    remainingMinutes + " minutes.",
                    "Account Locked",
                    JOptionPane.WARNING_MESSAGE);
                return true;
            } else {
                lockedAccounts.remove(username);
                failedAttempts.remove(username);
            }
        }
        return false;
    }
    
    /**
     * Resets failed attempt count for an account (admin function).
     */
    public void resetFailedAttempts(String username) {
        failedAttempts.remove(username);
        lockedAccounts.remove(username);
    }
    
    /**
     * Changes user password after validating current password.
     * SRS 1.1: Password security requirement.
     * 
     * @param username User's login name
     * @param oldPassword Current password for validation
     * @param newPassword New password (must meet strength requirements)
     * @return true if password changed successfully
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = userManager.authenticate(username, oldPassword);
        if (user != null) {
            return userManager.changePassword(oldPassword, newPassword);
        }
        return false;
    }
    
    /**
     * Creates a new user account (OWNER-only operation).
     * SRS 1.1: Role-based access control.
     * 
     * @param username New user's login name
     * @param password New user's password
     * @param role User role (OWNER or STAFF)
     * @param email User's email address
     * @return true if user created successfully
     */
    public boolean createUser(String username, String password, UserRole role, String email) {
        if (!userManager.hasPermission(UserRole.OWNER)) {
            JOptionPane.showMessageDialog(null,
                "Only OWNER users can create new accounts.",
                "Permission Denied",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return userManager.createUser(username, password, role, email);
    }
}