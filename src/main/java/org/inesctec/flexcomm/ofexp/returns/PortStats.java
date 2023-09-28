package org.inesctec.flexcomm.ofexp.returns;

import org.onosproject.net.PortNumber;

/**
 * PortStats
 */
public class PortStats {

  public PortNumber portNumber;
  public double currentConsumption;
  public double powerDrawn;

  public PortStats(PortNumber portNumber, double currentConsumption, double powerDrawn) {
    this.portNumber = portNumber;
    this.currentConsumption = currentConsumption;
    this.powerDrawn = powerDrawn;
  }

  public PortNumber getPortNumber() {
    return portNumber;
  }

  public void setPortNumber(PortNumber portNumber) {
    this.portNumber = portNumber;
  }

  public double getCurrentConsumption() {
    return currentConsumption;
  }

  public void setCurrentConsumption(double currentConsumption) {
    this.currentConsumption = currentConsumption;
  }

  public double getPowerDrawn() {
    return powerDrawn;
  }

  public void setPowerDrawn(double powerDrawn) {
    this.powerDrawn = powerDrawn;
  }

}
