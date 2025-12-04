package login;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationService {
    private UserManager userManager;
    private Map<String, Integer> failedAttempts;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCKOUT_TIME = 15 * 60 * 1000; // 15 minutes
    private Map<String, Long> lockedAccounts;
    
    public AuthenticationService(UserManager userManager) {
        this.userManager = userManager;
        this.failedAttempts = new HashMap<>();
        this.lockedAccounts = new HashMap<>();
    }
    
    public User authenticate(String username, String password) {
        // Check if account is locked
        if (isAccountLocked(username)) {
            return null;
        }
        
        User user = userManager.authenticate(username, password);
        
        if (user != null) {
            // Reset failed attempts on successful login
            failedAttempts.remove(username);
            return user;
        } else {
            // Increment failed attempts
            int attempts = failedAttempts.getOrDefault(username, 0) + 1;
            failedAttempts.put(username, attempts);
            
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                // Lock the account
                lockedAccounts.put(username, System.currentTimeMillis());
                JOptionPane.showMessageDialog(null,
                    "Account locked due to too many failed attempts.\n" +
                    "Please try again in 15 minutes or contact administrator.",
                    "Account Locked",
                    JOptionPane.ERROR_MESSAGE);
            } else {
                int remaining = MAX_FAILED_ATTEMPTS - attempts;
                JOptionPane.showMessageDialog(null,
                    "Invalid credentials. " + remaining + " attempts remaining.",
                    "Authentication Failed",
                    JOptionPane.WARNING_MESSAGE);
            }
            
            return null;
        }
    }
    
    private boolean isAccountLocked(String username) {
        Long lockTime = lockedAccounts.get(username);
        if (lockTime != null) {
            long elapsed = System.currentTimeMillis() - lockTime;
            if (elapsed < LOCKOUT_TIME) {
                long remainingMinutes = (LOCKOUT_TIME - elapsed) / (60 * 1000);
                JOptionPane.showMessageDialog(null,
                    "Account is locked. Please try again in " + 
                    remainingMinutes + " minutes.",
                    "Account Locked",
                    JOptionPane.WARNING_MESSAGE);
                return true;
            } else {
                // Lock expired
                lockedAccounts.remove(username);
                failedAttempts.remove(username);
            }
        }
        return false;
    }
    
    public void resetFailedAttempts(String username) {
        failedAttempts.remove(username);
        lockedAccounts.remove(username);
    }
    
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = userManager.authenticate(username, oldPassword);
        if (user != null) {
            return userManager.changePassword(oldPassword, newPassword);
        }
        return false;
    }
    
    public boolean createUser(String username, String password, UserRole role, String email) {
        // Only owner can create users
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