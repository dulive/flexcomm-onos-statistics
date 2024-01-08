package org.inesctec.flexcomm.ofexp.impl;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

import org.inesctec.flexcomm.ofexp.api.DefaultGlobalStatistics;
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

//@Component(immediate = true, service = FlexcommStore.class)
public class SimpleFlexcommStore extends AbstractStore<FlexcommEvent, FlexcommStoreDelegate> implements FlexcommStore {

  private final Logger log = getLogger(getClass());

  private final ConcurrentMap<DeviceId, GlobalStatistics> deviceGlobalStats = Maps.newConcurrentMap();
  private final ConcurrentMap<DeviceId, GlobalStatistics> deviceGlobalDeltaStats = Maps.newConcurrentMap();

  @Activate
  public void activate() {
    log.info("Started");
  }

  @Deactivate
  public void deactivate() {
    deviceGlobalStats.clear();
    deviceGlobalDeltaStats.clear();
    log.info("Stopped");
  }

  @Override
  public FlexcommEvent updateGlobalStatistics(ProviderId providerId, DeviceId deviceId,
      GlobalStatistics globalStatistics) {
    GlobalStatistics prvStats = deviceGlobalStats.get(deviceId);
    GlobalStatistics.Builder builder = DefaultGlobalStatistics.builder();
    GlobalStatistics deltaStats = builder.build();
    if (prvStats != null) {
      deltaStats = calcGlobalDeltaStats(deviceId, prvStats, globalStatistics);
    }

    deviceGlobalDeltaStats.put(deviceId, deltaStats);
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

  @Override
  public Collection<GlobalStatistics> getGlobalDeltaStatistics() {
    return ImmutableSet.copyOf(deviceGlobalDeltaStats.values());
  }

  @Override
  public GlobalStatistics getGlobalDeltaStatistics(DeviceId deviceId) {
    return deviceGlobalDeltaStats.get(deviceId);
  }

  private GlobalStatistics calcGlobalDeltaStats(DeviceId deviceId, GlobalStatistics prvStats,
      GlobalStatistics newStats) {

    GlobalStatistics.Builder builder = DefaultGlobalStatistics.builder();
    GlobalStatistics deltaStats = builder.setDeviceId(deviceId)
        .setCurrentConsumption(newStats.currentConsumption() - prvStats.currentConsumption())
        .setPowerDrawn(newStats.powerDrawn() - prvStats.powerDrawn()).build();
    return deltaStats;

  }
}
