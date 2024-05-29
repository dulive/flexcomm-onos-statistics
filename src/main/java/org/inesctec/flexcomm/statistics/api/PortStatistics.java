package org.inesctec.flexcomm.statistics.api;

import static org.onosproject.net.DefaultAnnotations.EMPTY;

import org.onosproject.net.Annotated;
import org.onosproject.net.Annotations;
import org.onosproject.net.PortNumber;

public interface PortStatistics extends Annotated {

  PortNumber portNumber();

  double currentConsumption();

  double powerDrawn();

  @Override
  default Annotations annotations() {
    return EMPTY;
  }

  interface Builder {

    Builder setPortNumber(PortNumber portNumber);

    Builder setCurrentConsumption(double currentConsumption);

    Builder setPowerDrawn(double powerDrawn);

    Builder setAnnotations(Annotations anns);

    PortStatistics build();
  }
}
