package com.bookstore.service;

import org.springframework.stereotype.Service;

/**
 * Payment rules in one place. Cash on delivery is real. UPI and Card are a DEMO: they are marked paid at once and no
 * card or UPI details are collected. To take real money, call Razorpay or Stripe inside statusFor() and only mark the
 * order paid after the gateway confirms the payment.
 */
@Service
public class PaymentService {
    public String label(String code) {
        return switch (code == null ? "" : code) {
            case "UPI" -> "UPI (demo)";
            case "CARD" -> "Card (demo)";
            default -> "Cash on delivery";
        };
    }

    public String statusFor(String code) {
        return "UPI".equals(code) || "CARD".equals(code) ? "Paid (demo)" : "Pay on delivery";
    }
}
