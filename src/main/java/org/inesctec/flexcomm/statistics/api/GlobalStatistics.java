package org.inesctec.flexcomm.statistics.api;

import static org.onosproject.net.DefaultAnnotations.EMPTY;

import org.onosproject.net.Annotated;
import org.onosproject.net.Annotations;
import org.onosproject.net.DeviceId;

public interface GlobalStatistics extends Annotated {

  DeviceId deviceId();

  double currentConsumption();

  double powerDrawn();

  @Override
  default Annotations annotations() {
    return EMPTY;
  }

  interface Builder {

    Builder setDeviceId(DeviceId deviceId);

    Builder setCurrentConsumption(double currentConsumption);

    Builder setPowerDrawn(double powerDrawn);

    Builder setAnnotations(Annotations anns);

    GlobalStatistics build();
  }
}
