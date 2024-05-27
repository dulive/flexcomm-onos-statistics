package org.inesctec.flexcomm.statistics.impl;

import static org.inesctec.flexcomm.statistics.api.FlexcommStatisticsEvent.Type.GLOBAL_STATS_UPDATED;
import static org.inesctec.flexcomm.statistics.api.FlexcommStatisticsEvent.Type.PORT_STATS_UPDATED;
import static org.onosproject.store.service.EventuallyConsistentMapEvent.Type.PUT;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.inesctec.flexcomm.statistics.api.DefaultGlobalStatistics;
import org.inesctec.flexcomm.statistics.api.DefaultPortStatistics;
import org.inesctec.flexcomm.statistics.api.FlexcommStatisticsEvent;
import org.inesctec.flexcomm.statistics.api.FlexcommStatisticsStore;
import org.inesctec.flexcomm.statistics.api.FlexcommStatisticsStoreDelegate;
import org.inesctec.flexcomm.statistics.api.GlobalStatistics;
import org.inesctec.flexcomm.statistics.api.PortStatistics;
import org.onlab.util.KryoNamespace;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

@Component(immediate = true, service = FlexcommStatisticsStore.class)
public class DistributedFlexcommStatisticsStore
    extends AbstractStore<FlexcommStatisticsEvent, FlexcommStatisticsStoreDelegate>
    implements FlexcommStatisticsStore {

  private final Logger log = getLogger(getClass());

  @Reference(cardinality = ReferenceCardinality.MANDATORY)
  protected StorageService storageService;

  private EventuallyConsistentMap<DeviceId, GlobalStatistics> deviceGlobalStats;
  private EventuallyConsistentMap<DeviceId, GlobalStatistics> deviceGlobalDeltaStats;
  private final EventuallyConsistentMapListener<DeviceId, GlobalStatistics> globalStatsListener = new InternalGlobalStatsListener();

  private EventuallyConsistentMap<DeviceId, Map<PortNumber, PortStatistics>> devicePortStats;
  private EventuallyConsistentMap<DeviceId, Map<PortNumber, PortStatistics>> devicePortDeltaStats;
  private final EventuallyConsistentMapListener<DeviceId, Map<PortNumber, PortStatistics>> portStatsListener = new InternalPortStatsListener();

  protected static final KryoNamespace.Builder SERIALIZER_BUILDER = KryoNamespace.newBuilder()
      .register(KryoNamespaces.API)
      .register(GlobalStatistics.class)
      .register(PortStatistics.class);

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

    devicePortStats = storageService.<DeviceId, Map<PortNumber, PortStatistics>>eventuallyConsistentMapBuilder()
        .withName("onos-flexcomm-port-stats")
        .withSerializer(SERIALIZER_BUILDER)
        .withAntiEntropyPeriod(5, TimeUnit.SECONDS)
        .withTimestampProvider((k, v) -> new WallClockTimestamp())
        .withTombstonesDisabled()
        .build();

    devicePortDeltaStats = storageService.<DeviceId, Map<PortNumber, PortStatistics>>eventuallyConsistentMapBuilder()
        .withName("onos-flexcomm-port-stats-delta")
        .withSerializer(SERIALIZER_BUILDER)
        .withAntiEntropyPeriod(5, TimeUnit.SECONDS)
        .withTimestampProvider((k, v) -> new WallClockTimestamp())
        .withTombstonesDisabled()
        .build();

    deviceGlobalStats.addListener(globalStatsListener);
    devicePortStats.addListener(portStatsListener);
    log.info("Started");
  }

  @Deactivate
  public void deactivate() {
    deviceGlobalStats.removeListener(globalStatsListener);
    devicePortStats.removeListener(portStatsListener);
    deviceGlobalStats.destroy();
    deviceGlobalDeltaStats.destroy();
    devicePortStats.destroy();
    devicePortDeltaStats.destroy();
    log.info("Stopped");
  }

  @Override
  public FlexcommStatisticsEvent updateGlobalStatistics(DeviceId deviceId,
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
  public FlexcommStatisticsEvent updatePortStatistics(DeviceId deviceId, Collection<PortStatistics> portStatistics) {
    Map<PortNumber, PortStatistics> prvStatsMap = devicePortStats.get(deviceId);
    Map<PortNumber, PortStatistics> newStatsMap = Maps.newHashMap();
    Map<PortNumber, PortStatistics> deltaStatsMap = Maps.newHashMap();

    if (prvStatsMap != null) {
      for (PortStatistics newStats : portStatistics) {
        PortNumber port = newStats.portNumber();
        PortStatistics prvStats = prvStatsMap.get(port);
        PortStatistics.Builder builder = DefaultPortStatistics.builder();
        PortStatistics deltaStats = builder.build();
        if (prvStats != null) {
          deltaStats = calcPortDeltaStats(deviceId, prvStats, newStats);
        }
        deltaStatsMap.put(port, deltaStats);
        newStatsMap.put(port, newStats);
      }

    } else {
      for (PortStatistics newStats : portStatistics) {
        PortNumber port = newStats.portNumber();
        newStatsMap.put(port, newStats);
      }
    }

    devicePortDeltaStats.put(deviceId, deltaStatsMap);
    devicePortStats.put(deviceId, newStatsMap);

    return null;
  }

  private PortStatistics calcPortDeltaStats(DeviceId deviceId, PortStatistics prvStats,
      PortStatistics newStats) {
    PortStatistics.Builder builder = DefaultPortStatistics.builder();
    PortStatistics deltaStats = builder.setDeviceId(deviceId)
        .setPortNumber(newStats.portNumber())
        .setCurrentConsumption(newStats.currentConsumption() - prvStats.currentConsumption())
        .setPowerDrawn(newStats.powerDrawn() - prvStats.powerDrawn())
        .build();
    return deltaStats;
  }

  @Override
  public List<GlobalStatistics> getGlobalStatistics() {
    return ImmutableList.copyOf(deviceGlobalStats.values());
  }

  @Override
  public GlobalStatistics getGlobalStatistics(DeviceId deviceId) {
    return deviceGlobalStats.get(deviceId);
  }

  @Override
  public List<GlobalStatistics> getGlobalDeltaStatistics() {
    return ImmutableList.copyOf(deviceGlobalDeltaStats.values());
  }

  @Override
  public GlobalStatistics getGlobalDeltaStatistics(DeviceId deviceId) {
    return deviceGlobalDeltaStats.get(deviceId);
  }

  @Override
  public List<PortStatistics> getPortStatistics() {
    ImmutableList.Builder<PortStatistics> builder = ImmutableList.builder();
    devicePortStats.values().forEach(portStats -> builder.addAll(portStats.values()));
    return builder.build();
  }

  @Override
  public List<PortStatistics> getPortDeltaStatistics() {
    ImmutableList.Builder<PortStatistics> builder = ImmutableList.builder();
    devicePortDeltaStats.values().forEach(portStats -> builder.addAll(portStats.values()));
    return builder.build();
  }

  @Override
  public List<PortStatistics> getPortStatistics(DeviceId deviceId) {
    Map<PortNumber, PortStatistics> portStats = devicePortStats.get(deviceId);
    if (portStats == null) {
      return Collections.emptyList();
    }
    return ImmutableList.copyOf(portStats.values());
  }

  @Override
  public List<PortStatistics> getPortDeltaStatistics(DeviceId deviceId) {
    Map<PortNumber, PortStatistics> portStats = devicePortDeltaStats.get(deviceId);
    if (portStats == null) {
      return Collections.emptyList();
    }
    return ImmutableList.copyOf(portStats.values());
  }

  @Override
  public PortStatistics getStatisticsForPort(DeviceId deviceId, PortNumber portNumber) {
    Map<PortNumber, PortStatistics> portStatsMap = devicePortStats.get(deviceId);
    if (portStatsMap == null) {
      return null;
    }
    PortStatistics portStats = portStatsMap.get(portNumber);
    return portStats;
  }

  @Override
  public PortStatistics getDeltaStatisticsForPort(DeviceId deviceId, PortNumber portNumber) {
    Map<PortNumber, PortStatistics> portStatsMap = devicePortDeltaStats.get(deviceId);
    if (portStatsMap == null) {
      return null;
    }
    PortStatistics portStats = portStatsMap.get(portNumber);
    return portStats;
  }

  private class InternalGlobalStatsListener implements EventuallyConsistentMapListener<DeviceId, GlobalStatistics> {
    @Override
    public void event(EventuallyConsistentMapEvent<DeviceId, GlobalStatistics> event) {
      if (event.type() == PUT) {
        DeviceId deviceId = event.key();
        notifyDelegate(new FlexcommStatisticsEvent(GLOBAL_STATS_UPDATED, deviceId));
      }
    }
  }

  private class InternalPortStatsListener
      implements EventuallyConsistentMapListener<DeviceId, Map<PortNumber, PortStatistics>> {

    @Override
    public void event(EventuallyConsistentMapEvent<DeviceId, Map<PortNumber, PortStatistics>> event) {
      if (event.type() == PUT) {
        DeviceId deviceId = event.key();
        notifyDelegate(new FlexcommStatisticsEvent(PORT_STATS_UPDATED, deviceId));
      }
    }
  }

}
