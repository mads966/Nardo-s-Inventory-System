package login;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class SetPasswordGUI extends JFrame {
    private Connection connection;
    private UserManager userManager;

    public SetPasswordGUI() {
        this(null, null);
    }

    public SetPasswordGUI(Connection connection, UserManager userManager) {
        this.connection = connection;
        this.userManager = userManager;
        
        initComponents();
        setTitle("Password Alteration");
        setSize(350, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel passwordLabel = new JLabel("New Password:");
        gbc.gridy = 0;
        panel.add(passwordLabel, gbc);
        
        JPasswordField passwordField = new JPasswordField();
        gbc.gridy = 1;
        panel.add(passwordField, gbc);

        JButton saveButton = new JButton("Save Password");
        gbc.gridy = 2;
        panel.add(saveButton, gbc);

        saveButton.addActionListener(e -> {
            String newPass = new String(passwordField.getPassword());

            if (newPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a password.");
                return;
            }

            // If UserManager is available, use it for password change
            if (userManager != null) {
                // Note: For resetting password without old password, 
                // we need additional logic (like email verification)
                JOptionPane.showMessageDialog(this, 
                    "Please use the Change Password feature from within your account.",
                    "Use Account Settings",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
                return;
            }
            
            // Fallback to static method for backward compatibility
            LoginPage.savePassword(newPass);
            JOptionPane.showMessageDialog(this, "Password saved. Please log in.");
            new LoginPage(connection).setVisible(true);
            dispose();
        });

        add(panel);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}