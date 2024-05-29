package org.inesctec.flexcomm.statistics.api;

import java.util.Collection;
import java.util.List;

import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.store.Store;

public interface FlexcommStatisticsStore extends Store<FlexcommStatisticsEvent, FlexcommStatisticsStoreDelegate> {

  FlexcommStatisticsEvent updateGlobalStatistics(DeviceId deviceId, GlobalStatistics globalStatistics);

  FlexcommStatisticsEvent updatePortStatistics(DeviceId deviceId, Collection<PortStatistics> portStatistics);

  GlobalStatistics getGlobalStatistics(DeviceId deviceId);

  GlobalStatistics getGlobalDeltaStatistics(DeviceId deviceId);

  List<PortStatistics> getPortStatistics(DeviceId deviceId);

  List<PortStatistics> getPortDeltaStatistics(DeviceId deviceId);

  PortStatistics getStatisticsForPort(DeviceId deviceId, PortNumber portNumber);

  PortStatistics getDeltaStatisticsForPort(DeviceId deviceId, PortNumber portNumber);

  default void purgeStatistics(DeviceId deviceId) {
  }
}
