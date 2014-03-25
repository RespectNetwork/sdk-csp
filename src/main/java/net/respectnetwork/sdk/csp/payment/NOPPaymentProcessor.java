package net.respectnetwork.sdk.csp.payment;

import java.math.BigDecimal;
import java.util.Currency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NOPPaymentProcessor implements PaymentProcessor {
    
    /** Class Logger */
    private static final Logger logger = LoggerFactory.getLogger(NOPPaymentProcessor.class);
    
    
    
    public PaymentStatusCode processPayment(String cardNumber, String cvv, String expMonth, String expYear, BigDecimal amount, Currency currency)
            throws PaymentProcessingException {
        
        logger.debug("Processing NOP Payment for {} : {} : {} : {} : {} : {} ", cardNumber, cvv, expMonth, expYear, currency.getSymbol(), amount );
        return PaymentStatusCode.SUCCESS;
                      
    }

}
