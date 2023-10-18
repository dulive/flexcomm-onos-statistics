package org.inesctec.flexcomm.ofexp.api;

import java.util.Collection;

import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.store.Store;

public interface FlexcommStore extends Store<FlexcommEvent, FlexcommStoreDelegate> {

  FlexcommEvent updateGlobalStatistics(ProviderId providerId, DeviceId deviceId, GlobalStatistics globalStatistics);
  // FlexcommEvent updatePortStatistics(ProviderId providerId, DeviceId deviceId,
  // Collection<PortStatistics> portStatistics);

  Collection<GlobalStatistics> getGlobalStatistics();

  GlobalStatistics getGlobalStatistics(DeviceId deviceId);

  // public Collection<PortStats> getPortStatistics();
  //
  // public List<PortStats> getPortStatistics(DeviceId deviceId);
  //
  // public PortStats getStatisticsForPort(DeviceId deviceId, PortNumber
  // portNumber);

  default void purgeStatistics(DeviceId deviceId) {
  }

}
