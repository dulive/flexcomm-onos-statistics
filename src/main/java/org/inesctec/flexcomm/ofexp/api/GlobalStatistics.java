package org.inesctec.flexcomm.ofexp.api;

import org.onosproject.net.Annotations;
import org.onosproject.net.DeviceId;

import static org.onosproject.net.DefaultAnnotations.EMPTY;

public interface GlobalStatistics extends FlexcommStatistics {

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
