package org.inesctec.flexcomm.ofexp.api;

import java.util.Collection;
// import java.util.List;

import org.onosproject.event.ListenerService;
// import org.inesctec.provider.flexcomm.returns.PortStats;
import org.onosproject.net.DeviceId;
// import org.onosproject.net.PortNumber;

public interface FlexcommStatisticsService
    extends ListenerService<FlexcommStatisticsEvent, FlexcommStatisticsListener> {

  public Collection<GlobalStatistics> getGlobalStatistics();

  public Collection<GlobalStatistics> getGlobalDeltaStatistics();

  public GlobalStatistics getGlobalStatistics(DeviceId deviceId);

  public GlobalStatistics getGlobalDeltaStatistics(DeviceId deviceId);

  // public Collection<PortStats> getPortStatistics();
  // public Collection<PortStats> getPortDeltaStatistics();
  //
  // public List<PortStats> getPortStatistics(DeviceId deviceId);
  // public List<PortStats> getPortDeltaStatistics(DeviceId deviceId);
  //
  // public PortStats getStatisticsForPort(DeviceId deviceId, PortNumber
  // portNumber);
  // public PortStats getDeltaStatisticsForPort(DeviceId deviceId, PortNumber
  // portNumber);
}
