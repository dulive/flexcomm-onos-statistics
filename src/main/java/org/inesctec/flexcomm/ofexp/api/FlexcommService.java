package org.inesctec.flexcomm.ofexp.api;

import java.util.Collection;
// import java.util.List;

// import org.inesctec.provider.flexcomm.returns.PortStats;
import org.onosproject.net.DeviceId;
// import org.onosproject.net.PortNumber;

public interface FlexcommService {

  public Collection<GlobalStatistics> getGlobalStatistics();

  public GlobalStatistics getGlobalStatistics(DeviceId deviceId);

  // public Collection<PortStats> getPortStatistics();
  //
  // public List<PortStats> getPortStatistics(DeviceId deviceId);
  //
  // public PortStats getStatisticsForPort(DeviceId deviceId, PortNumber
  // portNumber);
}
