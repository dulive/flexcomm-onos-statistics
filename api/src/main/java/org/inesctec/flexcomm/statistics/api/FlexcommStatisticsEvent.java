package org.inesctec.flexcomm.statistics.api;

import org.onosproject.event.AbstractEvent;
import org.onosproject.net.DeviceId;

public class FlexcommStatisticsEvent extends AbstractEvent<FlexcommStatisticsEvent.Type, DeviceId> {

  public enum Type {
    GLOBAL_STATS_UPDATED,
    PORT_STATS_UPDATED,
  }

  public FlexcommStatisticsEvent(Type type, DeviceId deviceId) {
    super(type, deviceId);
  }

  public FlexcommStatisticsEvent(Type type, DeviceId deviceId, long time) {
    super(type, deviceId, time);
  }
}
