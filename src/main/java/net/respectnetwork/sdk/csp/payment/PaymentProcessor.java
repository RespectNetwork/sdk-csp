package net.respectnetwork.sdk.csp.payment;

import java.math.BigDecimal;
import java.util.Currency;

public interface PaymentProcessor {




    /**
     * Process Payment
     * 
     * @param cardNumber
     * @param cvv
     * @param expMonth
     * @param expYear
     * @param amount
     * @param currency
     * @return
     * @throws PaymentProcessingException
     */
    public PaymentStatusCode processPayment(String cardNumber, String cvv, String expMonth, String expYear, BigDecimal amount, Currency currency)
            throws PaymentProcessingException;
    
 
}
