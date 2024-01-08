package org.inesctec.flexcomm.ofexp.api;

import org.onosproject.net.AbstractAnnotated;
import org.onosproject.net.Annotations;
import org.onosproject.net.DeviceId;

public final class DefaultGlobalStatistics extends AbstractAnnotated implements GlobalStatistics {

  private final DeviceId deviceId;
  private final double currentConsumption;
  private final double powerDrawn;

  private DefaultGlobalStatistics(DeviceId deviceId, double currentConsumption, double powerDrawn,
      Annotations annotations) {
    super(annotations);
    this.deviceId = deviceId;
    this.currentConsumption = currentConsumption;
    this.powerDrawn = powerDrawn;
  }

  private DefaultGlobalStatistics() {
    this.deviceId = null;
    this.currentConsumption = 0;
    this.powerDrawn = 0;
  }

  public static GlobalStatistics.Builder builder() {
    return new Builder();
  }

  @Override
  public DeviceId deviceId() {
    return this.deviceId;
  }

  @Override
  public double currentConsumption() {
    return this.currentConsumption;
  }

  @Override
  public double powerDrawn() {
    return this.powerDrawn;
  }

  public static final class Builder implements GlobalStatistics.Builder {

    DeviceId deviceId = null;
    double currentConsumption = 0;
    double powerDrawn = 0;
    Annotations annotations;

    private Builder() {

    }

    @Override
    public GlobalStatistics.Builder setDeviceId(DeviceId deviceId) {
      this.deviceId = deviceId;

      return this;
    }

    @Override
    public GlobalStatistics.Builder setCurrentConsumption(double currentConsumption) {
      this.currentConsumption = currentConsumption;

      return this;
    }

    @Override
    public GlobalStatistics.Builder setPowerDrawn(double powerDrawn) {
      this.powerDrawn = powerDrawn;

      return this;
    }

    @Override
    public GlobalStatistics.Builder setAnnotations(Annotations anns) {
      this.annotations = anns;

      return this;
    }

    @Override
    public DefaultGlobalStatistics build() {
      return new DefaultGlobalStatistics(deviceId, currentConsumption, powerDrawn, annotations);
    }

  }

}
