package main;

import alert.AlertManager;
import report.*;
import sale.*;
import login.*;
import product.*;
import stock.LowStockAlert;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static java.awt.print.Printable.NO_SUCH_PAGE;

import java.awt.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import java.awt.Graphics2D;

public class MainDashboard extends JFrame {
    // Core components
    private User currentUser;
    private Connection connection;
    private UserManager userManager;

    // Services
    private SalesProcessor salesProcessor;
    private ReportGenerator reportGenerator;
    private ProductService productService;
    
    // GUI Components
    private JLabel welcomeLabel;
    private JLabel statusLabel;
    private JLabel timeLabel;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    
    // Menu Components
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu salesMenu;
    private JMenu inventoryMenu;
    private JMenu reportMenu;
    private JMenu toolsMenu;
    private JMenu helpMenu;
    
    // Menu Items
    private JMenuItem changePasswordItem;
    private JMenuItem logoutItem;
    private JMenuItem exitItem;
    
    // Dashboard Panels
    private DashboardHomePanel homePanel;
    private SalesPanel salesPanel;
    private InventoryManagementPanel inventoryPanel;
    private ReportGeneratorPanel reportPanel;
    private SettingsPanel settingsPanel;
    
    // Quick Action Buttons
    private JButton quickSaleButton;
    private JButton manageProductsButton;
    private JButton viewReportsButton;
    private JButton checkAlertsButton;
    private JButton dailySummaryButton;
    private JButton backupButton;
    
    // Statistics Labels
    private JLabel totalProductsLabel;
    private JLabel lowStockLabel;
    private JLabel todaySalesLabel;
    private JLabel todayRevenueLabel;
    private JLabel activeAlertsLabel;
    
    // Status Bar Components
    private JLabel userRoleLabel;
    private JLabel loginTimeLabel;
    private JLabel databaseStatusLabel;
    
    // Timer for auto-refresh
    private Timer dashboardTimer;
    private Timer sessionTimer;
    private String sessionId;
    
    public MainDashboard(Connection connection, User user, UserManager userManager) {
        this.connection = connection;
        this.currentUser = user;
        this.userManager = userManager;
        
        // Create session
        this.sessionId = SessionManager.getInstance().createSession(user);
        
        // Initialize services
        initializeServices();
        
        // Setup GUI
        initComponents();
        layoutComponents();
        setupListeners();
        setupKeyboardShortcuts();
        
        // Set window properties
        setTitle("Nardo's Inventory System - Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));
        
        // Center on screen
        centerOnScreen();
        
        // Start dashboard updates
        startDashboardUpdates();
        
        // Start session validation
        startSessionValidation();
        
        // Load initial data
        loadDashboardData();
    }
    
    private void initializeServices() {
        try {
            this.salesProcessor = new SalesProcessor(connection);
            this.reportGenerator = new ReportGenerator(connection);
            this.productService = new ProductService(connection);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error initializing services: " + e.getMessage(),
                "Initialization Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        // Welcome label
        welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!",
                                 SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(33, 150, 243));
        
        // Status label
        statusLabel = new JLabel("Ready", SwingConstants.LEFT);
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Time label
        timeLabel = new JLabel();
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        updateTimeLabel();
        
        // Main content panel with CardLayout
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        
        // Initialize dashboard panels
        homePanel = new DashboardHomePanel();
        salesPanel = new SalesPanel(connection, currentUser.getUserId(), currentUser.getUsername());
        inventoryPanel = new InventoryManagementPanel(currentUser, connection);
        reportPanel = new ReportGeneratorPanel(connection, currentUser.getUserId(), currentUser.getUsername());
        settingsPanel = new SettingsPanel(connection, currentUser);
        
        // Add panels to card layout
        mainContentPanel.add(homePanel, "HOME");
        mainContentPanel.add(salesPanel, "SALES");
        mainContentPanel.add(inventoryPanel, "INVENTORY");
        mainContentPanel.add(reportPanel, "REPORTS");
        mainContentPanel.add(settingsPanel, "SETTINGS");
        
        // Create menu bar
        createMenuBar();
        
        // Create quick action buttons
        createQuickActionButtons();
        
        // Create statistics panel
        createStatisticsPanel();
        
        // Create status bar
        createStatusBar();
    }
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // File Menu
        fileMenu = new JMenu("File");
        
        JMenuItem newSaleItem = new JMenuItem("New Sale");
        newSaleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        
        JMenuItem printItem = new JMenuItem("Print");
        printItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        
        JMenuItem backupItem = new JMenuItem("Backup Database");
        
        changePasswordItem = new JMenuItem("Change Password");
        logoutItem = new JMenuItem("Logout");
        logoutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        
        fileMenu.add(newSaleItem);
        fileMenu.add(printItem);
        fileMenu.addSeparator();
        fileMenu.add(backupItem);
        fileMenu.addSeparator();
        fileMenu.add(changePasswordItem);
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Sales Menu
        salesMenu = new JMenu("Sales");
        
        JMenuItem processSaleItem = new JMenuItem("Process Sale");
        processSaleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        
        JMenuItem quickSaleItem = new JMenuItem("Quick Sale (3 Clicks)");
        quickSaleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        
        JMenuItem salesHistoryItem = new JMenuItem("Sales History");
        
        JMenuItem dailyReportItem = new JMenuItem("Daily Sales Report");
        
        salesMenu.add(processSaleItem);
        salesMenu.add(quickSaleItem);
        salesMenu.addSeparator();
        salesMenu.add(salesHistoryItem);
        salesMenu.add(dailyReportItem);
        
        // Inventory Menu
        inventoryMenu = new JMenu("Inventory");
        
        JMenuItem manageProductsItem = new JMenuItem("Manage Products");
        manageProductsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        
        JMenuItem lowStockItem = new JMenuItem("Low Stock Items");
        
        JMenuItem addStockItem = new JMenuItem("Add New Stock");
        
        JMenuItem suppliersItem = new JMenuItem("Manage Suppliers");
        
        inventoryMenu.add(manageProductsItem);
        inventoryMenu.add(lowStockItem);
        inventoryMenu.add(addStockItem);
        inventoryMenu.addSeparator();
        inventoryMenu.add(suppliersItem);
        
        // Reports Menu
        reportMenu = new JMenu("Reports");
        
        JMenuItem inventoryReportItem = new JMenuItem("Inventory Summary");
        
        JMenuItem salesReportItem = new JMenuItem("Sales Report");
        
        JMenuItem transactionReportItem = new JMenuItem("Transaction History");
        
        JMenuItem customReportItem = new JMenuItem("Custom Report");
        
        reportMenu.add(inventoryReportItem);
        reportMenu.add(salesReportItem);
        reportMenu.add(transactionReportItem);
        reportMenu.addSeparator();
        reportMenu.add(customReportItem);
        
        // Tools Menu
        toolsMenu = new JMenu("Tools");
        
        JMenuItem calculatorItem = new JMenuItem("Calculator");
        
        JMenuItem backupToolItem = new JMenuItem("System Backup");
        
        JMenuItem restoreItem = new JMenuItem("Restore Backup");
        
        toolsMenu.add(calculatorItem);
        toolsMenu.addSeparator();
        toolsMenu.add(backupToolItem);
        toolsMenu.add(restoreItem);
        
        // Help Menu
        helpMenu = new JMenu("Help");
        
        JMenuItem helpContentsItem = new JMenuItem("Help Contents");
        helpContentsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        
        JMenuItem aboutItem = new JMenuItem("About");
        
        helpMenu.add(helpContentsItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(salesMenu);
        menuBar.add(inventoryMenu);
        menuBar.add(reportMenu);
        menuBar.add(toolsMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createQuickActionButtons() {
        quickSaleButton = createStyledButton("Quick Sale", "⚡", new Color(76, 175, 80));
        manageProductsButton = createStyledButton("Manage Products", "📦", new Color(33, 150, 243));
        viewReportsButton = createStyledButton("View Reports", "📊", new Color(255, 152, 0));
        checkAlertsButton = createStyledButton("Check Alerts", "🔔", new Color(244, 67, 54));
        dailySummaryButton = createStyledButton("Daily Summary", "📈", new Color(156, 39, 176));
        backupButton = createStyledButton("Backup", "💾", new Color(0, 150, 136));
    }
    
    private JButton createStyledButton(String text, String icon, Color color) {
        JButton button = new JButton("<html><center><span style='font-size:24px'>" + 
                                   icon + "</span><br>" + text + "</center></html>");
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void createStatisticsPanel() {
        totalProductsLabel = createStatCard("Total Products", "0", new Color(33, 150, 243));
        lowStockLabel = createStatCard("Low Stock", "0", new Color(255, 152, 0));
        todaySalesLabel = createStatCard("Today's Sales", "0", new Color(76, 175, 80));
        todayRevenueLabel = createStatCard("Today's Revenue", "$0.00", new Color(156, 39, 176));
        activeAlertsLabel = createStatCard("Active Alerts", "0", new Color(244, 67, 54));
    }
    
    private JLabel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(Color.DARK_GRAY);
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        JLabel wrapper = new JLabel();
        wrapper.setLayout(new BorderLayout());
        wrapper.add(card, BorderLayout.CENTER);
        
        return wrapper;
    }
    
    private void createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        
        userRoleLabel = new JLabel("Role: " + currentUser.getRole());
        loginTimeLabel = new JLabel("Logged in: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        
        if (connection != null) {
            databaseStatusLabel = new JLabel("Database: Connected", SwingConstants.CENTER);
            databaseStatusLabel.setForeground(new Color(76, 175, 80));
        } else {
            databaseStatusLabel = new JLabel("Database: Mock (Testing Mode)", SwingConstants.CENTER);
            databaseStatusLabel.setForeground(new Color(255, 152, 0));
        }
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        leftPanel.add(userRoleLabel);
        leftPanel.add(new JSeparator(SwingConstants.VERTICAL));
        leftPanel.add(loginTimeLabel);
        
        statusBar.add(leftPanel, BorderLayout.WEST);
        statusBar.add(databaseStatusLabel, BorderLayout.CENTER);
        statusBar.add(new JLabel("Nardo's Inventory v1.0"), BorderLayout.EAST);
        
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel: Welcome and time
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(timeLabel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center: Main content with sidebar
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplit.setDividerLocation(250);
        centerSplit.setResizeWeight(0.2);
        
        // Left sidebar: Quick actions and statistics
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 10));
        
        // Quick actions panel
        JPanel quickActionsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        quickActionsPanel.setBorder(new TitledBorder("Quick Actions"));
        quickActionsPanel.setBackground(Color.WHITE);
        
        quickActionsPanel.add(quickSaleButton);
        quickActionsPanel.add(manageProductsButton);
        quickActionsPanel.add(viewReportsButton);
        quickActionsPanel.add(checkAlertsButton);
        quickActionsPanel.add(dailySummaryButton);
        quickActionsPanel.add(backupButton);
        
        sidebar.add(quickActionsPanel);
        sidebar.add(Box.createVerticalStrut(15));
        
        // Statistics panel
        JPanel statsWrapper = new JPanel(new BorderLayout());
        statsWrapper.setBorder(new TitledBorder("Live Statistics"));
        
        JPanel statsPanel = new JPanel(new GridLayout(5, 1, 5, 5));
        statsPanel.add(totalProductsLabel);
        statsPanel.add(lowStockLabel);
        statsPanel.add(todaySalesLabel);
        statsPanel.add(todayRevenueLabel);
        statsPanel.add(activeAlertsLabel);
        
        statsWrapper.add(statsPanel, BorderLayout.CENTER);
        sidebar.add(statsWrapper);
        
        // Add sidebar to split pane
        centerSplit.setLeftComponent(sidebar);
        centerSplit.setRightComponent(mainContentPanel);
        
        add(centerSplit, BorderLayout.CENTER);
        
        // Status bar at bottom
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshDashboard());
        statusPanel.add(refreshButton, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private void setupListeners() {
        setupMenuActions();
        
        // Quick action button actions
        quickSaleButton.addActionListener(e -> switchToPanel("SALES"));
        manageProductsButton.addActionListener(e -> switchToPanel("INVENTORY"));
        viewReportsButton.addActionListener(e -> switchToPanel("REPORTS"));
        checkAlertsButton.addActionListener(e -> checkLowStockAlerts());
        dailySummaryButton.addActionListener(e -> showDailySummary());
        backupButton.addActionListener(e -> performBackup());
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
    }
    
    private void setupMenuActions() {
        // File menu actions
        fileMenu.getItem(0).addActionListener(e -> switchToPanel("SALES"));
        fileMenu.getItem(1).addActionListener(e -> printCurrentView());
        fileMenu.getItem(3).addActionListener(e -> performBackup());
        
        changePasswordItem.addActionListener(e -> openChangePassword());
        logoutItem.addActionListener(e -> logout());
        exitItem.addActionListener(e -> confirmExit());
        
        // Sales menu actions
        salesMenu.getItem(0).addActionListener(e -> switchToPanel("SALES"));
        salesMenu.getItem(1).addActionListener(e -> {
            switchToPanel("SALES");
            salesPanel.processQuickSale();
        });
        salesMenu.getItem(3).addActionListener(e -> openSalesHistory());
        salesMenu.getItem(4).addActionListener(e -> generateDailySalesReport());
        
        // Inventory menu actions
        inventoryMenu.getItem(0).addActionListener(e -> switchToPanel("INVENTORY"));
        inventoryMenu.getItem(1).addActionListener(e -> checkLowStockAlerts());
        inventoryMenu.getItem(2).addActionListener(e -> {
            switchToPanel("INVENTORY");
            inventoryPanel.addNewStock();
        });
        inventoryMenu.getItem(4).addActionListener(e -> {
            switchToPanel("INVENTORY");
            inventoryPanel.manageSuppliers(connection);
        });
        
        // Reports menu actions
        reportMenu.getItem(0).addActionListener(e -> {
            switchToPanel("REPORTS");
            try {
                reportPanel.generateInventoryReport(userManager.getCurrentUser().getUserId());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        reportMenu.getItem(1).addActionListener(e -> {
            switchToPanel("REPORTS");
            try {
                reportPanel.generateSalesReport(userManager.getCurrentUser().getUserId());
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        reportMenu.getItem(2).addActionListener(e -> {
            switchToPanel("REPORTS");
            reportPanel.generateTransactionReport();
        });
        reportMenu.getItem(4).addActionListener(e -> {
            switchToPanel("REPORTS");
            reportPanel.openCustomReport();
        });
        
        // Tools menu actions
        toolsMenu.getItem(0).addActionListener(e -> openCalculator());
        toolsMenu.getItem(2).addActionListener(e -> performBackup());
        toolsMenu.getItem(3).addActionListener(e -> restoreBackup());
        
        // Help menu actions
        helpMenu.getItem(0).addActionListener(e -> showHelp());
        helpMenu.getItem(2).addActionListener(e -> showAboutDialog());
    }
    
    private void openChangePassword() {
        if (userManager != null && currentUser != null) {
            ChangePasswordVerificationGUI changePasswordGUI = 
                new ChangePasswordVerificationGUI(currentUser, userManager);
            changePasswordGUI.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                "Cannot change password: User session not available",
                "Session Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (userManager != null) {
                userManager.logout();
            }
            
            SwingUtilities.invokeLater(() -> {
                LoginPage loginPage = new LoginPage(connection);
                loginPage.setVisible(true);
            });
            
            dispose();
        }
    }
    
    private void setupKeyboardShortcuts() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();
        
        inputMap.put(KeyStroke.getKeyStroke("F1"), "showHelp");
        actionMap.put("showHelp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHelp();
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke("F2"), "quickSale");
        actionMap.put("quickSale", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchToPanel("SALES");
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke("F5"), "refresh");
        actionMap.put("refresh", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshDashboard();
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke("control S"), "openSales");
        actionMap.put("openSales", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchToPanel("SALES");
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke("control I"), "openInventory");
        actionMap.put("openInventory", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchToPanel("INVENTORY");
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke("control R"), "openReports");
        actionMap.put("openReports", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchToPanel("REPORTS");
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke("control H"), "goHome");
        actionMap.put("goHome", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchToPanel("HOME");
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        actionMap.put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchToPanel("HOME");
            }
        });
        
        inputMap.put(KeyStroke.getKeyStroke("control L"), "logout");
        actionMap.put("logout", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
    }
    
    private void startDashboardUpdates() {
        Timer timeTimer = new Timer(true);
        timeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> updateTimeLabel());
            }
        }, 0, 1000);
        
        dashboardTimer = new Timer(true);
        dashboardTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (isVisible()) {
                        loadDashboardData();
                    }
                });
            }
        }, 30000, 30000);
    }
    
    /**
     * Start session validation timer
     * SRS 1.1.4: Session Timeout After Inactivity
     */
    private void startSessionValidation() {
        sessionTimer = new Timer(true);
        sessionTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    SessionManager.UserSession session = 
                        SessionManager.getInstance().getSession(sessionId);
                    
                    if (session == null || !session.isValid()) {
                        // Session expired
                        JOptionPane.showMessageDialog(MainDashboard.this,
                            "Your session has expired due to inactivity.\n" +
                            "Please login again.",
                            "Session Expired", JOptionPane.WARNING_MESSAGE);
                        logout();
                    } else {
                        long remainingTime = session.getRemainingTime();
                        // Show warning when 5 minutes remaining
                        if (remainingTime < 5 * 60 * 1000 && remainingTime > 4 * 60 * 1000) {
                            JOptionPane.showMessageDialog(MainDashboard.this,
                                "Your session will expire in 5 minutes due to inactivity.\n" +
                                "Please save your work.",
                                "Session Expiring Soon", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });
            }
        }, 60000, 60000); // Check every minute
    }
    
    private void updateTimeLabel() {
        String time = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy - hh:mm:ss a"));
        timeLabel.setText(time);
    }

    public void switchToPanel(String panelName) {
        cardLayout.show(mainContentPanel, panelName);
        updateStatus("Switched to " + panelName.toLowerCase() + " view");
        
        // Refresh panel data when switched to
        if ("SALES".equals(panelName) && salesPanel != null) {
            salesPanel.refreshDashboard();
        } else if ("INVENTORY".equals(panelName) && inventoryPanel != null) {
            inventoryPanel.refreshData();
        } else if ("REPORTS".equals(panelName) && reportPanel != null) {
            reportPanel.refreshData();
        }
    }
    
    private void openSalesHistory() {
        try {
            SalesHistoryForm historyForm = new SalesHistoryForm(salesProcessor);
            
            JFrame historyFrame = new JFrame("Sales History");
            historyFrame.setContentPane(historyForm);
            historyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            historyFrame.setSize(900, 600);
            historyFrame.setLocationRelativeTo(this);
            historyFrame.setVisible(true);
            
            updateStatus("Opened sales history");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error opening sales history: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void performBackup() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFilename = "backup_" + timestamp + ".sql";
            
            // In a real implementation, this would execute a database backup command
            System.out.println("Performing database backup to: " + backupFilename);
            
            JOptionPane.showMessageDialog(this,
                "Backup initiated successfully.\n" +
                "File: " + backupFilename + "\n" +
                "Backup completed at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
            
            updateStatus("Backup performed");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error performing backup: " + e.getMessage(),
                "Backup Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateDailySalesReport() {
        try {
            SaleDAO.SalesStatistics stats = salesProcessor.getTodayStatistics();
            LocalDate today = LocalDate.now();
            
            String report = "DAILY SALES REPORT\n";
            report += "==================\n\n";
            report += "Date: " + today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n";
            report += "Total Sales: " + stats.getTotalSales() + "\n";
            report += "Total Revenue: $" + String.format("%.2f", stats.getTotalRevenue()) + "\n";
            report += "Average Sale: $" + String.format("%.2f", stats.getAverageSale()) + "\n";
            report += "Total Items Sold: " + stats.getTotalItems() + "\n\n";
            report += "Generated by: " + currentUser.getUsername() + "\n";
            report += "Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            
            JTextArea reportArea = new JTextArea(report);
            reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            JOptionPane.showMessageDialog(this,
                new JScrollPane(reportArea),
                "Daily Sales Report",
                JOptionPane.INFORMATION_MESSAGE);
            
            updateStatus("Generated daily sales report");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error generating sales report: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void openCalculator() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Runtime.getRuntime().exec("calc.exe");
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                Runtime.getRuntime().exec("open /System/Applications/Calculator.app");
            } else {
                Runtime.getRuntime().exec("gnome-calculator");
            }
            updateStatus("Opened calculator");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Could not open calculator: " + e.getMessage(),
                "Calculator Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void restoreBackup() {
        JOptionPane.showMessageDialog(this,
            "Restore Backup Feature\n\n" +
            "This feature would allow restoring from a previous backup file.",
            "Restore Backup", JOptionPane.INFORMATION_MESSAGE);
        
        updateStatus("Opened restore backup dialog");
    }
    
    private void showHelp() {
        JTextArea helpText = new JTextArea(
            "NARDO'S INVENTORY SYSTEM - HELP\n" +
            "===============================\n\n" +
            "SALES PROCESSING:\n" +
            "1. Use the Sales panel for quick 3-click sales\n" +
            "2. Select products, enter quantity, and process\n" +
            "3. View sales history for past transactions\n\n" +
            "INVENTORY MANAGEMENT:\n" +
            "1. Add, edit, and remove products\n" +
            "2. Set minimum stock levels for alerts\n" +
            "3. Track stock movements and adjustments\n\n" +
            "REPORT GENERATION:\n" +
            "1. Generate inventory reports\n" +
            "2. Create sales reports by date range\n" +
            "3. View transaction history\n\n" +
            "KEYBOARD SHORTCUTS:\n" +
            "• F1: This help screen\n" +
            "• F2: Switch to Sales panel\n" +
            "• F5: Refresh dashboard\n" +
            "• Ctrl+S: Sales\n" +
            "• Ctrl+I: Inventory\n" +
            "• Ctrl+R: Reports\n" +
            "• Ctrl+L: Logout\n" +
            "• Ctrl+H: Home\n" +
            "• Esc: Return to home\n"
        );
        
        helpText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        helpText.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(helpText);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane,
            "Help", JOptionPane.INFORMATION_MESSAGE);
        
        updateStatus("Help contents displayed");
    }
    
    private void showAboutDialog() {
        String aboutText = 
            "<html><center>" +
            "<h1>Nardo's Inventory System</h1>" +
            "<h3>Version 1.0</h3>" +
            "<hr>" +
            "<p><b>Complete Inventory Management Solution</b></p>" +
            "<p>University of the West Indies - Mona Campus</p>" +
            "<hr>" +
            "<p><b>Features:</b></p>" +
            "<p>• Sales Processing with inventory deduction</p>" +
            "<p>• Inventory Management with low stock alerts</p>" +
            "<p>• Comprehensive Reporting System</p>" +
            "<p>• User Authentication and Security</p>" +
            "<hr>" +
            "<p>COMP2140 - Introduction to Software Engineering</p>" +
            "<p>Group: Mon_3-5_G02</p>" +
            "</center></html>";
        
        JLabel aboutLabel = new JLabel(aboutText);
        aboutLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JOptionPane.showMessageDialog(this, aboutLabel,
            "About", JOptionPane.INFORMATION_MESSAGE);
        
        updateStatus("About dialog displayed");
    }

    private void printCurrentView() {
        String panelName = "Dashboard";
        Component currentPanel = getCurrentVisiblePanel();

        if (currentPanel instanceof DashboardHomePanel) {
            panelName = "Dashboard Home";
        } else if (currentPanel instanceof SalesPanel) {
            panelName = "Sales Panel";
        } else if (currentPanel instanceof InventoryManagementPanel) {
            panelName = "Inventory Management";
        } else if (currentPanel instanceof ReportGeneratorPanel) {
            panelName = "Reports";
        } else if (currentPanel instanceof SettingsPanel) {
            panelName = "Settings";
        }

        // Show print dialog instead of placeholder message
        showPrintDialog(currentPanel, panelName);

        updateStatus("Print command issued for " + panelName);
    }

    private Component getCurrentVisiblePanel() {
        for (Component comp : mainContentPanel.getComponents()) {
            if (comp.isVisible()) {
                return comp;
            }
        }
        return null;
    }

    private void showPrintDialog(Component componentToPrint, String panelName) {
        if (componentToPrint == null) {
            JOptionPane.showMessageDialog(this,
                    "No panel available to print.",
                    "Print Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PrintDialog printDialog = new PrintDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                componentToPrint,
                panelName
        );
        printDialog.setVisible(true);
    }

    /**
     * Custom Printable for printing GUI components
     */
    private class ComponentPrintable implements Printable {
        private Component component;
        private String title;

        public ComponentPrintable(Component component, String title) {
            this.component = component;
            this.title = title;
        }

        @Override
        public int print(Graphics g, PageFormat pf, int pageIndex) {
            if (pageIndex > 0) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());

            // Print header
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("Nardo's Inventory System - " + title, 50, 20);
            g2d.drawString("Printed: " +
                            java.time.LocalDateTime.now().format(
                                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    50, 35);
            g2d.drawString("Printed by: " + currentUser.getUsername(), 50, 50);

            // Draw line separator
            g2d.drawLine(50, 60, (int)pf.getImageableWidth() - 50, 60);

            // Translate for component
            g2d.translate(0, 80);

            // Scale component to fit page width
            double scaleX = (pf.getImageableWidth() - 100) / component.getWidth();
            double scaleY = (pf.getImageableHeight() - 150) / component.getHeight();
            double scale = Math.min(scaleX, scaleY);

            if (scale < 1.0) {
                g2d.scale(scale, scale);
            }

            // Print the component
            component.printAll(g2d);

            return PAGE_EXISTS;
        }
    }

    private void refreshDashboard() {
        loadDashboardData();
        updateStatus("Dashboard refreshed at " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
    
    private void confirmExit() {
        int option = JOptionPane.showConfirmDialog(this,
            "Would you like to view the daily summary before exiting?\n\n",
            "Exit Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            showDailySummary();
            System.exit(0);
        } else if (option == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message + " [" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "]");
    }
    
    private void centerOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        setLocation((screenSize.width - frameSize.width) / 2,
                   (screenSize.height - frameSize.height) / 2);
    }

    // Inner class for dashboard home panel
    private class DashboardHomePanel extends JPanel {
        public DashboardHomePanel() {
            setLayout(new BorderLayout());
            setBackground(new Color(245, 245, 245));

            // Create welcome content
            JPanel welcomePanel = new JPanel(new BorderLayout());
            welcomePanel.setBackground(new Color(33, 150, 243));
            welcomePanel.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));

            JLabel titleLabel = new JLabel("<html><center>" +
                    "<h1 style='color:white; font-size:32px;'>Nardo's Inventory System</h1>" +
                    "<p style='color:white; font-size:18px; margin-top:10px;'>Complete Inventory Management Solution</p>" +
                    "</center></html>", SwingConstants.CENTER);

            welcomePanel.add(titleLabel, BorderLayout.CENTER);

            // Create features panel with centered layout
            JPanel featuresPanel = new JPanel(new GridBagLayout());
            featuresPanel.setBackground(Color.WHITE);

            // Create a grid panel for the feature cards
            JPanel cardsGrid = new JPanel(new GridLayout(2, 3, 30, 30));
            cardsGrid.setBackground(Color.WHITE);
            cardsGrid.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

            cardsGrid.add(createFeatureCard("💰", "Sales Processing",
                    "Process sales with automatic inventory deduction"));
            cardsGrid.add(createFeatureCard("📊", "Report Generation",
                    "Generate comprehensive inventory and sales reports"));
            cardsGrid.add(createFeatureCard("📈", "Daily Summary",
                    "View daily activity summary"));
            cardsGrid.add(createFeatureCard("⚡", "Quick Sales",
                    "3-click sales processing for fast transactions"));
            cardsGrid.add(createFeatureCard("🔔", "Low Stock Alerts",
                    "Automatic stock level notifications"));
            cardsGrid.add(createFeatureCard("📋", "Sales History",
                    "View and analyze past sales transactions"));

            // Center the cards grid within the features panel
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            featuresPanel.add(cardsGrid, gbc);

            add(welcomePanel, BorderLayout.NORTH);
            add(featuresPanel, BorderLayout.CENTER);
        }

        private JPanel createFeatureCard(String icon, String title, String description) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(25, 20, 25, 20)
            ));
            card.setMinimumSize(new Dimension(220, 250));
            card.setPreferredSize(new Dimension(240, 280));
            card.setMaximumSize(new Dimension(260, 300));

            // Icon panel with proper sizing
            JPanel iconPanel = new JPanel(new BorderLayout());
            iconPanel.setBackground(Color.WHITE);
            iconPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
            iconPanel.setPreferredSize(new Dimension(100, 100));

            JLabel iconLabel = new JLabel(icon);
            // Use a font that supports emojis and set reasonable size
            try {
                iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56)); // Reduced from 64/72
            } catch (Exception e) {
                // Fallback if Segoe UI Emoji is not available
                iconLabel.setFont(new Font("Arial Unicode MS", Font.PLAIN, 56));
            }
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setVerticalAlignment(SwingConstants.CENTER);

            // Add some padding around the emoji
            iconLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // Ensure icon label has enough space
            iconLabel.setMinimumSize(new Dimension(80, 80));
            iconLabel.setPreferredSize(new Dimension(90, 90));

            // Add icon label to center of icon panel
            iconPanel.add(iconLabel, BorderLayout.CENTER);

            // Title label
            JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Slightly smaller title
            titleLabel.setForeground(new Color(33, 150, 243));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

            // Description panel
            JPanel descPanel = new JPanel(new BorderLayout());
            descPanel.setBackground(Color.WHITE);
            descPanel.setMaximumSize(new Dimension(220, 80));

            JTextArea descArea = new JTextArea(description);
            descArea.setFont(new Font("Arial", Font.PLAIN, 12));
            descArea.setLineWrap(true);
            descArea.setWrapStyleWord(true);
            descArea.setEditable(false);
            descArea.setBackground(Color.WHITE);
            descArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            // Center the text
            descArea.setAlignmentX(Component.CENTER_ALIGNMENT);

            JScrollPane descScroll = new JScrollPane(descArea);
            descScroll.setBorder(null);
            descScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            descScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            descPanel.add(descScroll, BorderLayout.CENTER);

            // Add components with proper spacing
            card.add(iconPanel);
            card.add(Box.createRigidArea(new Dimension(0, 5)));
            card.add(titleLabel);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(descPanel);

            return card;
        }
    }

    private void loadDashboardData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private int totalProducts = 0;
            private int lowStockItems = 0;
            private int todaySalesCount = 0;
            private double todayRevenue = 0;
            private int activeAlerts = 0;

            @Override
            protected Void doInBackground() {
                try {
                    // Get product statistics
                    totalProducts = productService.getTotalProductCount();
                    lowStockItems = productService.getLowStockCount();

                    // Get today's sales statistics
                    try {
                        SaleDAO.SalesStatistics stats = salesProcessor.getTodayStatistics();
                        todaySalesCount = stats.getTotalSales();
                        todayRevenue = stats.getTotalRevenue();
                    } catch (Exception e) {
                        todaySalesCount = 0;
                        todayRevenue = 0.0;
                        System.err.println("Error getting sales statistics: " + e.getMessage());
                    }

                    // Get active alerts
                    AlertManager alertManager = new AlertManager();
                    activeAlerts = alertManager.getActiveAlertCount();

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainDashboard.this,
                            "Error loading dashboard data: " + e.getMessage(),
                            "Database Error", JOptionPane.ERROR_MESSAGE);

                    totalProducts = 0;
                    lowStockItems = 0;
                    todaySalesCount = 0;
                    todayRevenue = 0;
                    activeAlerts = 0;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Update statistics labels
                    updateStatLabel(totalProductsLabel, String.valueOf(totalProducts));
                    updateStatLabel(lowStockLabel, String.valueOf(lowStockItems));
                    updateStatLabel(todaySalesLabel, String.valueOf(todaySalesCount));
                    updateStatLabel(todayRevenueLabel, String.format("$%.2f", todayRevenue));
                    updateStatLabel(activeAlertsLabel, String.valueOf(activeAlerts));

                    updateStatus("Dashboard data loaded at " +
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                } catch (Exception e) {
                    updateStatus("Error updating dashboard display: " + e.getMessage());
                }
            }

            private void updateStatLabel(JLabel statLabel, String value) {
                Component[] components = statLabel.getComponents();
                if (components.length > 0 && components[0] instanceof JPanel) {
                    JPanel card = (JPanel) components[0];
                    Component[] cardComps = card.getComponents();
                    if (cardComps.length > 1 && cardComps[1] instanceof JLabel) {
                        ((JLabel) cardComps[1]).setText(value);
                    }
                }
            }
        };

        worker.execute();
    }

    private java.util.List<Product> getLowStockProducts() {
        try {
            return productService.getLowStockProducts();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to retrieve low stock products: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return new ArrayList<>();
        }
    }

    private void checkLowStockAlerts() {
        try {
            // Create alert manager
            AlertManager alertManager = new AlertManager();

            // Check current stock levels
            alertManager.checkStockLevels();

            // Get active alerts
            java.util.List<LowStockAlert> alerts = alertManager.getActiveAlerts();

            if (alerts.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No low stock alerts at this time.",
                        "Low Stock Alerts", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            StringBuilder alertMessage = new StringBuilder();
            alertMessage.append("LOW STOCK ALERTS\n");
            alertMessage.append("================\n\n");

            ProductDAO productDAO = new ProductDAO();
            for (stock.LowStockAlert alert : alerts) {
                product.Product product = productDAO.getProductById(alert.getProductId());
                if (product != null) {
                    alertMessage.append(String.format("%s: %d in stock (Min: %d)\n",
                            product.getName(), alert.getCurrentQuantity(), alert.getMinStockLevel()));
                }
            }

            alertMessage.append("\nPlease restock these items soon.");

            JTextArea alertArea = new JTextArea(alertMessage.toString());
            alertArea.setEditable(false);
            alertArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(alertArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Low Stock Alerts", JOptionPane.WARNING_MESSAGE);

            updateStatus("Displayed " + alerts.size() + " low stock alerts");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error checking low stock alerts: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDailySummary() {
        try {
            String summary = reportGenerator.generateExitSummary();

            JTextArea summaryArea = new JTextArea(summary);
            summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            summaryArea.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(summaryArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Daily Summary", JOptionPane.INFORMATION_MESSAGE);

            updateStatus("Displayed daily summary");
        } catch (Exception e) {
            // Show error when database is not connected
            String errorSummary = "DATABASE CONNECTION ERROR\n" +
                    "==========================\n\n" +
                    "Unable to connect to the database.\n\n" +
                    "Error: " + e.getMessage() + "\n\n" +
                    "Please check your database connection and try again.";

            JTextArea summaryArea = new JTextArea(errorSummary);
            summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JOptionPane.showMessageDialog(this, summaryArea,
                    "Daily Summary - Database Error", JOptionPane.ERROR_MESSAGE);

            updateStatus("Failed to generate daily summary - Database error");
        }
    }

    /**
     * Print Dialog with print options
     */
    private class PrintDialog extends JDialog {
        private Component component;
        private String panelName;
        private JCheckBox printHeaderCheck;
        private JRadioButton portraitRadio;
        private JRadioButton landscapeRadio;
        private JButton printButton;
        private JButton previewButton;
        private JButton cancelButton;

        public PrintDialog(Frame parent, Component component, String panelName) {
            super(parent, "Print Options - " + panelName, true);
            this.component = component;
            this.panelName = panelName;

            initializeComponents();
            layoutComponents();
            setupListeners();

            setSize(400, 300);
            setLocationRelativeTo(parent);
        }

        private void initializeComponents() {
            printHeaderCheck = new JCheckBox("Include header with timestamp", true);

            portraitRadio = new JRadioButton("Portrait", true);
            landscapeRadio = new JRadioButton("Landscape");
            ButtonGroup orientationGroup = new ButtonGroup();
            orientationGroup.add(portraitRadio);
            orientationGroup.add(landscapeRadio);

            printButton = new JButton("Print");
            previewButton = new JButton("Print Preview");
            cancelButton = new JButton("Cancel");
        }

        private void layoutComponents() {
            setLayout(new BorderLayout(10, 10));

            // Options panel
            JPanel optionsPanel = new JPanel(new GridBagLayout());
            optionsPanel.setBorder(BorderFactory.createTitledBorder("Print Options"));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0; gbc.gridy = 0;
            optionsPanel.add(printHeaderCheck, gbc);

            gbc.gridy = 1;
            optionsPanel.add(new JLabel("Orientation:"), gbc);

            gbc.gridy = 2;
            JPanel orientationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            orientationPanel.add(portraitRadio);
            orientationPanel.add(landscapeRadio);
            optionsPanel.add(orientationPanel, gbc);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            buttonPanel.add(previewButton);
            buttonPanel.add(printButton);
            buttonPanel.add(cancelButton);

            add(optionsPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private void setupListeners() {
            printButton.addActionListener(e -> performPrint());
            previewButton.addActionListener(e -> showPrintPreview());
            cancelButton.addActionListener(e -> dispose());
        }

        private void performPrint() {
            try {
                PrinterJob job = PrinterJob.getPrinterJob();

                // Set job name
                job.setJobName("Nardo's Inventory - " + panelName);

                // Create printable
                Printable printable = new ComponentPrintable(component, panelName);

                // Get page format
                PageFormat pageFormat = job.defaultPage();

                // Set orientation
                if (landscapeRadio.isSelected()) {
                    pageFormat.setOrientation(PageFormat.LANDSCAPE);
                } else {
                    pageFormat.setOrientation(PageFormat.PORTRAIT);
                }

                job.setPrintable(printable, pageFormat);

                // Show print dialog
                if (job.printDialog()) {
                    job.print();
                    JOptionPane.showMessageDialog(PrintDialog.this,
                            "Print job sent to printer successfully.",
                            "Print Complete", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(PrintDialog.this,
                        "Print error: " + e.getMessage(),
                        "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showPrintPreview() {
            PrintPreviewDialog preview = new PrintPreviewDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    component,
                    panelName
            );
            preview.setVisible(true);
        }
    }

    /**
     * Print Preview Dialog
     */
    private class PrintPreviewDialog extends JDialog {
        private Component component;
        private String panelName;

        public PrintPreviewDialog(Frame parent, Component component, String panelName) {
            super(parent, "Print Preview - " + panelName, true);
            this.component = component;
            this.panelName = panelName;

            initComponents();
            setSize(800, 600);
            setLocationRelativeTo(parent);
        }

        private void initComponents() {
            JPanel mainPanel = new JPanel(new BorderLayout());

            // Preview panel
            JPanel previewPanel = new PreviewPanel();
            JScrollPane scrollPane = new JScrollPane(previewPanel);
            scrollPane.setBorder(BorderFactory.createTitledBorder("Preview"));

            // Buttons
            JButton printButton = new JButton("Print");
            JButton closeButton = new JButton("Close");

            printButton.addActionListener(e -> {
                // Reuse print functionality
                PrintDialog printDialog = new PrintDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        component,
                        panelName
                );
                printDialog.setVisible(true);
            });

            closeButton.addActionListener(e -> dispose());

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(printButton);
            buttonPanel.add(closeButton);

            mainPanel.add(scrollPane, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(mainPanel);
        }

        private class PreviewPanel extends JPanel {
            private static final double SCALE = 0.5;

            public PreviewPanel() {
                setPreferredSize(new Dimension(
                        (int)(component.getWidth() * SCALE) + 100,
                        (int)(component.getHeight() * SCALE) + 150
                ));
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Draw paper background
                g2d.setColor(Color.WHITE);
                g2d.fillRect(50, 50, getWidth() - 100, getHeight() - 100);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(50, 50, getWidth() - 100, getHeight() - 100);

                // Draw header
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("Nardo's Inventory System - " + panelName, 60, 70);
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                g2d.drawString("Printed: " +
                                java.time.LocalDateTime.now().format(
                                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        60, 85);
                g2d.drawString("Printed by: " + currentUser.getUsername(), 60, 100);

                // Draw line separator
                g2d.drawLine(60, 110, getWidth() - 60, 110);

                // Draw component preview
                g2d.translate(60, 130);
                g2d.scale(SCALE, SCALE);
                component.printAll(g2d);
            }
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Try to establish real database connection
                Connection connection = null;
                try {
                    // Use DBManager or direct connection
                    connection = database.DBManager.getConnection();
                    System.out.println("Database connection established successfully");
                } catch (Exception e) {
                    System.out.println("Using mock connection: " + e.getMessage());
                    // Continue with null connection for testing
                }

                // Create mock user for testing
                User testUser = new User(1, "owner1", "password",
                        UserRole.OWNER, "owner@nardos.com", true);

                UserManager userManager = new UserManager(connection);

                // Create and show dashboard
                MainDashboard dashboard = new MainDashboard(connection, testUser, userManager);
                dashboard.setVisible(true);

                System.out.println("Nardo's Inventory System Dashboard started");

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error starting dashboard: " + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}