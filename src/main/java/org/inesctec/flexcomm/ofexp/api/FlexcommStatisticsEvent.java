package org.inesctec.flexcomm.ofexp.api;

import org.onosproject.event.AbstractEvent;

public class FlexcommStatisticsEvent extends AbstractEvent<FlexcommStatisticsEvent.Type, FlexcommStatistics> {

  public enum Type {
    GLOBAL_STATS_UPDATED,
    PORT_STATS_UPDATED,
  }

  public FlexcommStatisticsEvent(Type type, FlexcommStatistics stats) {
    super(type, stats);
  }

  public FlexcommStatisticsEvent(Type type, FlexcommStatistics stats, long time) {
    super(type, stats, time);
  }
}
