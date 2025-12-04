/**
 * Main Application and UI Layer - SRS Component: 2.2 User Interface
 * 
 * This package contains the main application entry point and primary user
 * interface components for the inventory management dashboard.
 * 
 * SRS Requirement 1.0 Overall System:
 * - Provide intuitive GUI with clear navigation
 * - Display real-time inventory and sales information
 * - Role-based access control (different views for OWNER vs STAFF)
 * - Responsive design and quick operations (< 2 seconds per action)
 * - Settings for system configuration
 * 
 * Architecture: Main Application and Dashboard
 * - MainDashboard: Primary application window and entry point
 * - InventoryManagementPanel: Tab for inventory operations
 * - SettingsPanel: Configuration and system preferences
 * - ImagePanel: Reusable component for image rendering
 * 
 * Application Flow:
 * 1. User launches application
 * 2. LoginPage appears for authentication
 * 3. Upon successful login, MainDashboard opens
 * 4. Dashboard shows role-appropriate menu and panels
 * 5. User navigates between tabs: Inventory, Sales, Reports
 * 6. Session remains active for 30 minutes (configurable)
 * 7. Auto-logout on inactivity triggers Exit Summary
 * 
 * Main Dashboard Components:
 * - Menu Bar: File, Edit, View, Reports, Help
 * - Tab Panel: Inventory, Sales, Reports, Settings
 * - Status Bar: Current user, session info, alerts
 * - Sidebar: Quick access to main functions
 * - Content Area: Active panel for current operation
 * 
 * Role-Based Visibility:
 * OWNER Role can access:
 * - Full product management (add, edit, deactivate)
 * - All reports and analytics
 * - System settings and configuration
 * - User account management
 * - Low stock management
 * 
 * STAFF Role can access:
 * - View products and inventory
 * - Process sales
 * - View own transaction history
 * - Limited reports
 * - Session information
 * 
 * Key Features:
 * - Real-time alert display for low stock
 * - Quick sales entry for fast checkout
 * - Inventory search and filtering
 * - Session timeout warning (5-minute notice)
 * - Graceful logout with summary
 * 
 * UI Design Principles:
 * - Clear, intuitive navigation
 * - Consistent button placement and labeling
 * - Proper error and success messaging
 * - Responsive to user actions (visual feedback)
 * - Non-blocking operations (background processing)
 * - Accessibility considerations
 * 
 * Performance Requirements:
 * - Dashboard load time: < 3 seconds
 * - Panel switch: < 1 second
 * - Search/filter: < 2 seconds
 * - All operations: < 5 seconds
 */
package main;
