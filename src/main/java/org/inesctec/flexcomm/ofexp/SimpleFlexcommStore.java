package org.inesctec.flexcomm.ofexp;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import org.inesctec.flexcomm.ofexp.api.FlexcommEvent;
import org.inesctec.flexcomm.ofexp.api.FlexcommStore;
import org.inesctec.flexcomm.ofexp.api.FlexcommStoreDelegate;
import org.inesctec.flexcomm.ofexp.api.GlobalStatistics;
import org.inesctec.flexcomm.ofexp.api.FlexcommEvent.Type;
import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.store.AbstractStore;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true, service = FlexcommStore.class)
public class SimpleFlexcommStore extends AbstractStore<FlexcommEvent, FlexcommStoreDelegate> implements FlexcommStore {

  private final Logger log = getLogger(getClass());

  private final ConcurrentMap<DeviceId, GlobalStatistics> deviceGlobalStats = Maps.newConcurrentMap();

  @Activate
  public void activate() {
    log.info("Started");
  }

  @Deactivate
  public void deactivate() {
    deviceGlobalStats.clear();
    log.info("Stopped");
  }

  @Override
  public FlexcommEvent updateGlobalStatistics(ProviderId providerId, DeviceId deviceId,
      GlobalStatistics globalStatistics) {
    deviceGlobalStats.put(deviceId, globalStatistics);
    return new FlexcommEvent(Type.GLOBAL_STATS_UPDATED, globalStatistics);
  }

  @Override
  public Collection<GlobalStatistics> getGlobalStatistics() {
    return ImmutableSet.copyOf(deviceGlobalStats.values());
  }

  @Override
  public GlobalStatistics getGlobalStatistics(DeviceId deviceId) {
    return deviceGlobalStats.get(deviceId);
  }

}
