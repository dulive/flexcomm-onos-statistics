package org.inesctec.flexcomm.statistics.api;

import org.onosproject.net.AbstractAnnotated;
import org.onosproject.net.Annotations;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public final class DefaultPortStatistics extends AbstractAnnotated implements PortStatistics {

  private final DeviceId deviceId;
  private final PortNumber portNumber;
  private final double currentConsumption;
  private final double powerDrawn;

  private DefaultPortStatistics(DeviceId deviceId, PortNumber portNumber, double currentConsumption, double powerDrawn,
      Annotations annotations) {
    super(annotations);
    this.deviceId = deviceId;
    this.portNumber = portNumber;
    this.currentConsumption = currentConsumption;
    this.powerDrawn = powerDrawn;
  }

  private DefaultPortStatistics() {
    this.deviceId = null;
    this.portNumber = null;
    this.currentConsumption = 0;
    this.powerDrawn = 0;
  }

  public static PortStatistics.Builder builder() {
    return new Builder();
  }

  @Override
  public PortNumber portNumber() {
    return this.portNumber;
  }

  @Override
  public double currentConsumption() {
    return this.currentConsumption;
  }

  @Override
  public double powerDrawn() {
    return this.powerDrawn;
  }

  @Override
  public boolean isZero() {
    return currentConsumption() == 0 && powerDrawn() == 0;
  }

  @Override
  public String toString() {
    return "device: " + deviceId + ", " +
        "port: " + this.portNumber + ", " +
        "currentConsumption: " + this.currentConsumption + ", " +
        "powerDrawn: " + this.powerDrawn + ", " +
        "annotations: " + annotations();
  }

  public static final class Builder implements PortStatistics.Builder {

    DeviceId deviceId = null;
    PortNumber portNumber = null;
    double currentConsumption = 0;
    double powerDrawn = 0;
    Annotations annotations;

    private Builder() {

    }

    public PortStatistics.Builder setDeviceId(DeviceId deviceId) {
      this.deviceId = deviceId;

      return this;
    }

    @Override
    public PortStatistics.Builder setPortNumber(PortNumber portNumber) {
      this.portNumber = portNumber;

      return this;
    }

    @Override
    public PortStatistics.Builder setCurrentConsumption(double currentConsumption) {
      this.currentConsumption = currentConsumption;

      return this;
    }

    @Override
    public PortStatistics.Builder setPowerDrawn(double powerDrawn) {
      this.powerDrawn = powerDrawn;

      return this;
    }

    @Override
    public PortStatistics.Builder setAnnotations(Annotations anns) {
      this.annotations = anns;

      return this;
    }

    @Override
    public DefaultPortStatistics build() {
      return new DefaultPortStatistics(deviceId, portNumber, currentConsumption, powerDrawn, annotations);
    }

  }

}
