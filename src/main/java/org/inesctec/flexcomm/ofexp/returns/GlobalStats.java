package org.inesctec.flexcomm.ofexp.returns;

/**
 * GlobalStats
 */
public class GlobalStats {

    public double consumption;
    public double load;

    public GlobalStats(double consumption, double load) {
        this.consumption = consumption;
        this.load = load;
    }

    public double getConsumption() {
        return consumption;
    }

    public void setConsumption(double consumption) {
        this.consumption = consumption;
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }
}
