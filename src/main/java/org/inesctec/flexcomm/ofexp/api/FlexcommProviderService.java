package org.inesctec.flexcomm.ofexp.api;

// import java.util.Collection;

import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.ProviderService;

public interface FlexcommProviderService extends ProviderService<FlexcommProvider> {

  void updateGlobalStatistics(DeviceId deviceId, GlobalStatistics globalStatistics);
  // void updatePortStatistics(DeviceId devicedId, Collection<PortStatistics>
  // portStatistics);

}
