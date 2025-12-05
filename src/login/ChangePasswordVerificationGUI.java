package login;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordVerificationGUI extends JFrame {
    private User currentUser;
    private UserManager userManager;
    private AuthenticationService authenticationService;
    
    public ChangePasswordVerificationGUI(User currentUser, UserManager userManager) {
        this.currentUser = currentUser;
        this.userManager = userManager;
        this.authenticationService = new AuthenticationService(userManager);
        
        initComponents();
        setTitle("Change Password");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.gridx = 0;
        
        // Old Password
        JLabel oldPasswordLabel = new JLabel("Old Password:");
        gbc.gridy = 0;
        panel.add(oldPasswordLabel, gbc);
        
        JPasswordField oldPasswordField = new JPasswordField(20);
        gbc.gridy = 1;
        panel.add(oldPasswordField, gbc);
        
        // New Password
        JLabel newPasswordLabel = new JLabel("New Password:");
        gbc.gridy = 2;
        panel.add(newPasswordLabel, gbc);
        
        JPasswordField newPasswordField = new JPasswordField(20);
        gbc.gridy = 3;
        panel.add(newPasswordField, gbc);
        
        // Confirm Password
        JLabel confirmPasswordLabel = new JLabel("Confirm New Password:");
        gbc.gridy = 4;
        panel.add(confirmPasswordLabel, gbc);
        
        JPasswordField confirmPasswordField = new JPasswordField(20);
        gbc.gridy = 5;
        panel.add(confirmPasswordField, gbc);
        
        // Save Button
        JButton saveButton = new JButton("Save New Password");
        saveButton.setBackground(new Color(76, 175, 80));
        saveButton.setForeground(Color.WHITE);
        gbc.gridy = 6;
        panel.add(saveButton, gbc);
        
        saveButton.addActionListener(e -> {
            String oldPass = new String(oldPasswordField.getPassword());
            String newPass = new String(newPasswordField.getPassword());
            String confirmPass = new String(confirmPasswordField.getPassword());
            
            // Validate inputs
            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "All fields are required", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, 
                    "New passwords do not match", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newPass.length() < 6) {
                JOptionPane.showMessageDialog(this, 
                    "New password must be at least 6 characters long", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Change password using UserManager
            if (userManager != null && currentUser != null) {
                boolean success = userManager.changePassword(oldPass, newPass);
                
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Password updated successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to update password. Please check your old password.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "User session not available. Please login again.", 
                    "Session Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        add(panel);
        setVisible(true);
    }
}