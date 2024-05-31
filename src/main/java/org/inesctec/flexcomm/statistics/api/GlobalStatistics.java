package org.inesctec.flexcomm.statistics.api;

import static org.onosproject.net.DefaultAnnotations.EMPTY;

import org.onosproject.net.Annotated;
import org.onosproject.net.Annotations;

public interface GlobalStatistics extends Annotated {

  double currentConsumption();

  double powerDrawn();

  @Override
  default Annotations annotations() {
    return EMPTY;
  }

  boolean isZero();

  interface Builder {

    Builder setCurrentConsumption(double currentConsumption);

    Builder setPowerDrawn(double powerDrawn);

    Builder setAnnotations(Annotations anns);

    GlobalStatistics build();
  }
}
