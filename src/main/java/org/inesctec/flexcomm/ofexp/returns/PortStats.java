package org.inesctec.flexcomm.ofexp.returns;

import org.onosproject.net.PortNumber;

/**
 * PortStats
 */
public class PortStats {

    public PortNumber portNumber;
    public double consumption;

    public PortStats(PortNumber portNumber, double consumption) {
        this.portNumber = portNumber;
        this.consumption = consumption;
    }

    public PortNumber getPortNumber() {
        return portNumber;
    }

    public void setPortNumber(PortNumber portNumber) {
        this.portNumber = portNumber;
    }

    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }
}
