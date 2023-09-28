package org.inesctec.flexcomm.ofexp.returns;

/**
 * GlobalStats
 */
public class GlobalStats {

  public double currentConsumption;
  public double powerDrawn;

  public GlobalStats() {
  }

  public GlobalStats(double currentConsumption, double powerDrawn) {
    this.currentConsumption = currentConsumption;
    this.powerDrawn = powerDrawn;
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
