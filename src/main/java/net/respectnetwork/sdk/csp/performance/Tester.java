package net.respectnetwork.sdk.csp.performance;

import net.respectnetwork.sdk.csp.BasicCSPInformation;

public interface Tester {

        public BasicCSPInformation getCspInformation();
        public void setCspInformation(BasicCSPInformation cspInformation);
        public void execute() throws TestException;
        public void init() throws TestException;

}
