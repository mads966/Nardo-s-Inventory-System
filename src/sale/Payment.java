package sale;

import java.time.LocalDateTime;

public class Payment {
    private int paymentId;
    private int saleId;
    private String paymentMethod;
    private double amount;
    private double tenderedAmount;
    private double changeAmount;
    private String cardLastFour;
    private String transactionId;
    private LocalDateTime paymentTime;
    private String status;
    private String notes;
    
    // Constructors
    public Payment() {
        this.paymentTime = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    public Payment(int saleId, String paymentMethod, double amount) {
        this();
        this.saleId = saleId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.tenderedAmount = amount;
        this.changeAmount = 0;
    }
    
    // Getters and Setters
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    
    public int getSaleId() { return saleId; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public double getTenderedAmount() { return tenderedAmount; }
    public void setTenderedAmount(double tenderedAmount) { 
        this.tenderedAmount = tenderedAmount;
        calculateChange();
    }
    
    public double getChangeAmount() { return changeAmount; }
    
    public String getCardLastFour() { return cardLastFour; }
    public void setCardLastFour(String cardLastFour) { this.cardLastFour = cardLastFour; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Business methods
    private void calculateChange() {
        if (tenderedAmount >= amount) {
            changeAmount = tenderedAmount - amount;
        } else {
            changeAmount = 0;
        }
    }
    
    public boolean isCashPayment() {
        return "CASH".equalsIgnoreCase(paymentMethod);
    }
    
    public boolean isCardPayment() {
        return "CARD".equalsIgnoreCase(paymentMethod) || 
               "CREDIT".equalsIgnoreCase(paymentMethod) || 
               "DEBIT".equalsIgnoreCase(paymentMethod);
    }
    
    public boolean isComplete() {
        return "COMPLETED".equalsIgnoreCase(status) || 
               "APPROVED".equalsIgnoreCase(status);
    }
    
    public void processCashPayment(double tendered) {
        this.paymentMethod = "CASH";
        this.tenderedAmount = tendered;
        calculateChange();
        this.status = "COMPLETED";
    }
    
    public void processCardPayment(String cardLastFour, String transactionId) {
        this.paymentMethod = "CARD";
        this.cardLastFour = cardLastFour;
        this.transactionId = transactionId;
        this.tenderedAmount = this.amount;
        this.changeAmount = 0;
        this.status = "APPROVED";
    }
    
    public void processMobilePayment(String transactionId) {
        this.paymentMethod = "MOBILE";
        this.transactionId = transactionId;
        this.tenderedAmount = this.amount;
        this.changeAmount = 0;
        this.status = "APPROVED";
    }
    
    @Override
    public String toString() {
        return String.format("Payment: %s - $%.2f - %s", 
            paymentMethod, amount, status);
    }
}