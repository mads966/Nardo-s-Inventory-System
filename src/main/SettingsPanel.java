// SettingsPanel.java
package main;

import login.User;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class SettingsPanel extends JPanel {
    public SettingsPanel(Connection connection, User user) {
        setLayout(new BorderLayout());
        add(new JLabel("<html><center><h1>Settings</h1>" +
                      "<p>System configuration and user management</p></center></html>", 
                      SwingConstants.CENTER), BorderLayout.CENTER);
    }
}