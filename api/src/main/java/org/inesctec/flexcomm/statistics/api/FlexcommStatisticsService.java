package org.inesctec.flexcomm.statistics.api;

import java.util.List;

import org.onosproject.event.ListenerService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

public interface FlexcommStatisticsService
    extends ListenerService<FlexcommStatisticsEvent, FlexcommStatisticsListener> {

  public GlobalStatistics getGlobalStatistics(DeviceId deviceId);

  public GlobalStatistics getGlobalDeltaStatistics(DeviceId deviceId);

  public List<PortStatistics> getPortStatistics(DeviceId deviceId);

  public List<PortStatistics> getPortDeltaStatistics(DeviceId deviceId);

  public PortStatistics getStatisticsForPort(DeviceId deviceId, PortNumber portNumber);

  public PortStatistics getDeltaStatisticsForPort(DeviceId deviceId, PortNumber portNumber);
}
