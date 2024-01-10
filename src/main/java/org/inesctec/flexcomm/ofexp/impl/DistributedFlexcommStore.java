package org.inesctec.flexcomm.ofexp.impl;

import static org.inesctec.flexcomm.ofexp.api.FlexcommEvent.Type.GLOBAL_STATS_UPDATED;
import static org.onosproject.store.service.EventuallyConsistentMapEvent.Type.PUT;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.inesctec.flexcomm.ofexp.api.DefaultGlobalStatistics;
import org.inesctec.flexcomm.ofexp.api.FlexcommEvent;
import org.inesctec.flexcomm.ofexp.api.FlexcommStore;
import org.inesctec.flexcomm.ofexp.api.FlexcommStoreDelegate;
import org.inesctec.flexcomm.ofexp.api.GlobalStatistics;
import org.onlab.util.KryoNamespace;
import org.onosproject.net.DeviceId;
import org.onosproject.store.AbstractStore;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.EventuallyConsistentMap;
import org.onosproject.store.service.EventuallyConsistentMapEvent;
import org.onosproject.store.service.EventuallyConsistentMapListener;
import org.onosproject.store.service.StorageService;
import org.onosproject.store.service.WallClockTimestamp;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableSet;

@Component(immediate = true, service = FlexcommStore.class)
public class DistributedFlexcommStore extends AbstractStore<FlexcommEvent, FlexcommStoreDelegate>
    implements FlexcommStore {

  private final Logger log = getLogger(getClass());

  @Reference(cardinality = ReferenceCardinality.MANDATORY)
  protected StorageService storageService;

  private EventuallyConsistentMap<DeviceId, GlobalStatistics> deviceGlobalStats;
  private EventuallyConsistentMap<DeviceId, GlobalStatistics> deviceGlobalDeltaStats;
  private final EventuallyConsistentMapListener<DeviceId, GlobalStatistics> globalStatsListener = new InternalGlobalStatsListener();

  protected static final KryoNamespace.Builder SERIALIZER_BUILDER = KryoNamespace.newBuilder()
      .register(KryoNamespaces.API)
      .register(GlobalStatistics.class);

  @Activate
  public void activate() {
    deviceGlobalStats = storageService.<DeviceId, GlobalStatistics>eventuallyConsistentMapBuilder()
        .withName("onos-flexcomm-global-stats")
        .withSerializer(SERIALIZER_BUILDER)
        .withAntiEntropyPeriod(5, TimeUnit.SECONDS)
        .withTimestampProvider((k, v) -> new WallClockTimestamp())
        .withTombstonesDisabled()
        .build();

    deviceGlobalDeltaStats = storageService.<DeviceId, GlobalStatistics>eventuallyConsistentMapBuilder()
        .withName("onos-flexcomm-global-stats-delta")
        .withSerializer(SERIALIZER_BUILDER)
        .withAntiEntropyPeriod(5, TimeUnit.SECONDS)
        .withTimestampProvider((k, v) -> new WallClockTimestamp())
        .withTombstonesDisabled()
        .build();

    deviceGlobalStats.addListener(globalStatsListener);
    log.info("Started");
  }

  @Deactivate
  public void deactivate() {
    deviceGlobalStats.removeListener(globalStatsListener);
    deviceGlobalStats.destroy();
    deviceGlobalDeltaStats.destroy();
    log.info("Stopped");
  }

  @Override
  public FlexcommEvent updateGlobalStatistics(DeviceId deviceId,
      GlobalStatistics globalStatistics) {

    GlobalStatistics prvStats = deviceGlobalStats.get(deviceId);
    GlobalStatistics.Builder builder = DefaultGlobalStatistics.builder();
    GlobalStatistics deltaStats = builder.build();
    if (prvStats != null) {
      deltaStats = calcGlobalDeltaStats(deviceId, prvStats, globalStatistics);
    }

    deviceGlobalDeltaStats.put(deviceId, deltaStats);
    deviceGlobalStats.put(deviceId, globalStatistics);

    return null;
  }

  private GlobalStatistics calcGlobalDeltaStats(DeviceId deviceId, GlobalStatistics prvStats,
      GlobalStatistics newStats) {

    GlobalStatistics.Builder builder = DefaultGlobalStatistics.builder();
    GlobalStatistics deltaStats = builder.setDeviceId(deviceId)
        .setCurrentConsumption(newStats.currentConsumption() - prvStats.currentConsumption())
        .setPowerDrawn(newStats.powerDrawn() - prvStats.powerDrawn()).build();
    return deltaStats;

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

  private class InternalGlobalStatsListener implements EventuallyConsistentMapListener<DeviceId, GlobalStatistics> {
    @Override
    public void event(EventuallyConsistentMapEvent<DeviceId, GlobalStatistics> event) {
      if (event.type() == PUT) {
        GlobalStatistics globalStatistics = event.value();
        notifyDelegate(new FlexcommEvent(GLOBAL_STATS_UPDATED, globalStatistics));
      }
    }
  }

}
