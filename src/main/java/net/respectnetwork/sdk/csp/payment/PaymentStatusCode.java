package net.respectnetwork.sdk.csp.payment;

public enum PaymentStatusCode {
    
        SUCCESS (100, "The Payment succeeded."),
        FAILURE (200, "The Payment Failed. No Reason Given");
     
        /** Status Code Number */
        private int code;
        
        /** Status Code Description */
        private String description;
        
        /**
         * @return the code
         */
        public int getCode() {
            return code;
        }

        /**
         * @param code the code to set
         */
        public void setCode(int code) {
            this.code = code;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @param description the description to set
         */
        public void setDescription(String description) {
            this.description = description;
        }

        private PaymentStatusCode(int code, String description) {
            this.code = code;
            this.description = description;
        }
}
