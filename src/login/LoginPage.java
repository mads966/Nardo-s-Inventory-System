package login;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import main.MainDashboard;

public class LoginPage extends JFrame {
    private static String savedPassword = "admin123"; // Default password
    private UserManager userManager;
    private AuthenticationService authenticationService;
    private static Connection connection;
    
    public LoginPage(Connection connection) {
        this.connection = connection;
        this.userManager = new UserManager(connection);
        this.authenticationService = new AuthenticationService(userManager);
        
        initComponents();
        setTitle("Nardo's Inventory System - Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }
    
    private void initComponents() {
        // Use ImagePanel for background if image exists
        ImagePanel backgroundPanel;
        try {
            backgroundPanel = new ImagePanel("login_background.jpg");
        } catch (Exception e) {
            backgroundPanel = new ImagePanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Create gradient background
                    Graphics2D g2d = (Graphics2D) g;
                    Color color1 = new Color(33, 150, 243);
                    Color color2 = new Color(76, 175, 80);
                    GradientPaint gradient = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };
        }
        
        backgroundPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel titleLabel = new JLabel("Nardo's Inventory System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        backgroundPanel.add(titleLabel, gbc);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Login to Continue", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.WHITE);
        gbc.gridy = 1;
        backgroundPanel.add(subtitleLabel, gbc);
        
        // Username Label
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        backgroundPanel.add(userLabel, gbc);
        
        // Username Field
        JTextField userField = new JTextField(15);
        userField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        backgroundPanel.add(userField, gbc);
        
        // Password Label
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        passLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridy = 3;
        gbc.gridx = 0;
        backgroundPanel.add(passLabel, gbc);
        
        // Password Field
        JPasswordField passField = new JPasswordField(15);
        passField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridx = 1;
        backgroundPanel.add(passField, gbc);
        
        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(new Color(76, 175, 80));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridwidth = 2;
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        backgroundPanel.add(loginButton, gbc);
        
        // Forgot Password Button
        JButton forgotButton = new JButton("Forgot Password?");
        forgotButton.setFont(new Font("Arial", Font.PLAIN, 11));
        forgotButton.setForeground(new Color(255, 255, 255, 200));
        forgotButton.setContentAreaFilled(false);
        forgotButton.setBorderPainted(false);
        forgotButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 5;
        backgroundPanel.add(forgotButton, gbc);
        
        // Action Listeners
        loginButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter both username and password", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Use AuthenticationService for login with account lockout protection
            // SRS 1.1.1: Credential validation
            // SRS 1.1.4: Account lockout after 3 failed attempts
            User authenticatedUser = authenticationService.authenticate(username, password);
            
            if (authenticatedUser != null) {
                JOptionPane.showMessageDialog(this, 
                    "Login successful! Welcome " + authenticatedUser.getUsername(),
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Open MainDashboard with UserManager
                SwingUtilities.invokeLater(() -> {
                    MainDashboard dashboard = new MainDashboard(connection, authenticatedUser, userManager);
                    dashboard.setVisible(true);
                });
                
                dispose(); // Close login window
            } else {
                // AuthenticationService handles error messages and account lockout
                passField.setText("");
            }
        });
        
        // Enter key to login
        passField.addActionListener(e -> loginButton.doClick());
        
        forgotButton.addActionListener(e -> {
            String username = userField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter your username first",
                    "Username Required",
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            int option = JOptionPane.showConfirmDialog(this,
                "Would you like to reset your password?",
                "Password Reset",
                JOptionPane.YES_NO_OPTION);
                
            if (option == JOptionPane.YES_OPTION) {
                // For test mode, show password reset options
                if (connection == null) {
                    String[] options = {"Test Owner (owner1/validPwd)", "Test Staff (staff1/staffPwd)"};
                    String choice = (String) JOptionPane.showInputDialog(this,
                        "Test Mode Credentials:\n\n" +
                        "• Owner: username=owner1, password=validPwd\n" +
                        "• Staff: username=staff1, password=staffPwd\n\n" +
                        "Select credentials to use:",
                        "Test Credentials",
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]);
                        
                    if (choice != null) {
                        if (choice.contains("Owner")) {
                            userField.setText("owner1");
                            passField.setText("validPwd");
                        } else {
                            userField.setText("staff1");
                            passField.setText("staffPwd");
                        }
                    }
                } else {
                    // In production, open password reset dialog
                    new SetPasswordGUI().setVisible(true);
                }
            }
        });
        
        // Add hover effect to login button
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(56, 142, 60));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(76, 175, 80));
            }
        });
        
        setContentPane(backgroundPanel);
    }
    
    // Static method for backward compatibility with existing code
    public static void savePassword(String newPassword) {
        savedPassword = newPassword;
    }
    
    // Static method for backward compatibility
    public static String getSavedPassword() {
        return savedPassword;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Connection connection = null;

            try {
                // Try to get connection from DBManager
                connection = database.DBManager.getConnection();
                
                if (connection == null) {
                    System.out.println("Running in TEST MODE - Database connection unavailable");
                    JOptionPane.showMessageDialog(null,
                        "Database connection failed.\n\n" +
                        "Running in TEST MODE with mock data.\n\n" +
                        "Test Credentials:\n" +
                        "• Owner: username=owner1, password=validPwd\n" +
                        "• Staff: username=staff1, password=staffPwd",
                        "Database Connection Failed",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
                System.out.println("Running in TEST MODE - Database connection unavailable");
                JOptionPane.showMessageDialog(null,
                    "Database connection failed.\n\n" +
                    "Running in TEST MODE with mock data.\n\n" +
                    "Test Credentials:\n" +
                    "• Owner: username=owner1, password=validPwd\n" +
                    "• Staff: username=staff1, password=staffPwd",
                    "Database Connection Failed",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            LoginPage loginPage = new LoginPage(connection);
            loginPage.setVisible(true);
        });
    }
}