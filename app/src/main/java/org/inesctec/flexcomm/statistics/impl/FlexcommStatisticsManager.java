package org.inesctec.flexcomm.statistics.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.inesctec.flexcomm.statistics.impl.OsgiPropertyConstants.FM_PURGE_ON_DISCONNECTION;
import static org.inesctec.flexcomm.statistics.impl.OsgiPropertyConstants.FM_PURGE_ON_DISCONNECTION_DEFAULT;
import static org.onlab.util.Tools.groupedThreads;
import static org.onosproject.security.AppGuard.checkPermission;
import static org.onosproject.security.AppPermission.Type.DEVICE_READ;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.inesctec.flexcomm.statistics.api.FlexcommStatisticsEvent;
import org.inesctec.flexcomm.statistics.api.FlexcommStatisticsListener;
import org.inesctec.flexcomm.statistics.api.FlexcommStatisticsProvider;
import org.inesctec.flexcomm.statistics.api.FlexcommStatisticsProviderRegistry;
import org.inesctec.flexcomm.statistics.api.FlexcommStatisticsProviderService;
import org.inesctec.flexcomm.statistics.api.FlexcommStatisticsService;
import org.inesctec.flexcomm.statistics.api.FlexcommStatisticsStore;
import org.inesctec.flexcomm.statistics.api.FlexcommStatisticsStoreDelegate;
import org.inesctec.flexcomm.statistics.api.GlobalStatistics;
import org.inesctec.flexcomm.statistics.api.PortStatistics;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.config.basics.BasicDeviceConfig;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.provider.AbstractListenerProviderRegistry;
import org.onosproject.net.provider.AbstractProviderService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;

@Component(immediate = true, service = {
    FlexcommStatisticsService.class,
    FlexcommStatisticsProviderRegistry.class
}, property = {
    FM_PURGE_ON_DISCONNECTION + ":Boolean=" + FM_PURGE_ON_DISCONNECTION_DEFAULT
})
public class FlexcommStatisticsManager
    extends
    AbstractListenerProviderRegistry<FlexcommStatisticsEvent, FlexcommStatisticsListener, FlexcommStatisticsProvider, FlexcommStatisticsProviderService>
    implements FlexcommStatisticsService, FlexcommStatisticsProviderRegistry {

  public static final String DEVICE_ID_NULL = "Device ID cannot be null";
  public static final String PORT_NUMBER_NULL = "Port number cannot be null";

  private final Logger log = getLogger(getClass());

  private final FlexcommStatisticsStoreDelegate delegate = new InternalFlexcommStoreDelegate();
  private final DeviceListener deviceListener = new InternalDeviceListener();

  private ExecutorService eventExecutor;

  @Reference(cardinality = ReferenceCardinality.MANDATORY)
  protected FlexcommStatisticsStore store;

  @Reference(cardinality = ReferenceCardinality.MANDATORY)
  protected DeviceService deviceService;

  @Reference(cardinality = ReferenceCardinality.MANDATORY)
  protected ComponentConfigService cfgService;

  @Reference(cardinality = ReferenceCardinality.MANDATORY)
  protected NetworkConfigRegistry netCfgService;

  private boolean purgeOnDisconnection = FM_PURGE_ON_DISCONNECTION_DEFAULT;

  @Activate
  public void activate(ComponentContext context) {
    eventExecutor = Executors.newSingleThreadExecutor(groupedThreads("onos/flexcomm/stats", "event"));
    store.setDelegate(delegate);
    eventDispatcher.addSink(FlexcommStatisticsEvent.class, listenerRegistry);
    deviceService.addListener(deviceListener);
    cfgService.registerProperties(getClass());
    modified(context);

    log.info("Started");
  }

  @Deactivate
  public void deactivate(ComponentContext context) {
    eventExecutor.shutdown();
    deviceService.removeListener(deviceListener);
    cfgService.unregisterProperties(getClass(), false);
    store.unsetDelegate(delegate);
    eventDispatcher.removeSink(FlexcommStatisticsEvent.class);
    log.info("Stopped");
  }

  @Modified
  public void modified(ComponentContext context) {
    Dictionary<?, ?> properties = context != null ? context.getProperties() : new Properties();
    Boolean flag;

    flag = Tools.isPropertyEnabled(properties, FM_PURGE_ON_DISCONNECTION);
    if (flag == null) {
      log.info("PurgeOnDisconnection is not configured, " +
          "using current value of {}", purgeOnDisconnection);
    } else {
      purgeOnDisconnection = flag;
      log.info("Configured. PurgeOnDisconnection is {}",
          purgeOnDisconnection ? "enabled" : "disabled");
    }
  }

  @Override
  public GlobalStatistics getGlobalStatistics(DeviceId deviceId) {
    checkPermission(DEVICE_READ);
    checkNotNull(deviceId, DEVICE_ID_NULL);
    return store.getGlobalStatistics(deviceId);
  }

  @Override
  public GlobalStatistics getGlobalDeltaStatistics(DeviceId deviceId) {
    checkPermission(DEVICE_READ);
    checkNotNull(deviceId, DEVICE_ID_NULL);
    return store.getGlobalDeltaStatistics(deviceId);
  }

  @Override
  public List<PortStatistics> getPortStatistics(DeviceId deviceId) {
    checkPermission(DEVICE_READ);
    checkNotNull(deviceId, DEVICE_ID_NULL);
    return store.getPortStatistics(deviceId);
  }

  @Override
  public List<PortStatistics> getPortDeltaStatistics(DeviceId deviceId) {
    checkPermission(DEVICE_READ);
    checkNotNull(deviceId, DEVICE_ID_NULL);
    return store.getPortDeltaStatistics(deviceId);
  }

  @Override
  public PortStatistics getStatisticsForPort(DeviceId deviceId, PortNumber portNumber) {
    checkPermission(DEVICE_READ);
    checkNotNull(deviceId, DEVICE_ID_NULL);
    checkNotNull(portNumber, PORT_NUMBER_NULL);
    return store.getStatisticsForPort(deviceId, portNumber);
  }

  @Override
  public PortStatistics getDeltaStatisticsForPort(DeviceId deviceId, PortNumber portNumber) {
    checkPermission(DEVICE_READ);
    checkNotNull(deviceId, DEVICE_ID_NULL);
    checkNotNull(portNumber, PORT_NUMBER_NULL);
    return store.getDeltaStatisticsForPort(deviceId, portNumber);
  }

  @Override
  protected FlexcommStatisticsProviderService createProviderService(FlexcommStatisticsProvider provider) {
    return new InternalFlexcommStatisticsProviderService(provider);
  }

  private class InternalFlexcommStoreDelegate implements FlexcommStatisticsStoreDelegate {

    @Override
    public void notify(FlexcommStatisticsEvent event) {
      post(event);
    }

  }

  private class InternalFlexcommStatisticsProviderService extends AbstractProviderService<FlexcommStatisticsProvider>
      implements FlexcommStatisticsProviderService {

    InternalFlexcommStatisticsProviderService(FlexcommStatisticsProvider provider) {
      super(provider);
    }

    @Override
    public void updateGlobalStatistics(DeviceId deviceId, GlobalStatistics globalStatistics) {
      checkNotNull(deviceId, DEVICE_ID_NULL);
      checkNotNull(globalStatistics, "Global statistics cannot be null");
      checkValidity();

      FlexcommStatisticsEvent event = store.updateGlobalStatistics(deviceId, globalStatistics);
      post(event);
    }

    @Override
    public void updatePortStatistics(DeviceId deviceId, Collection<PortStatistics> portStatistics) {
      checkNotNull(deviceId, DEVICE_ID_NULL);
      checkNotNull(portStatistics, "Port statistics list cannot be null");
      checkValidity();

      FlexcommStatisticsEvent event = store.updatePortStatistics(deviceId, portStatistics);
      post(event);
    }

  }

  private class InternalDeviceListener implements DeviceListener {

    @Override
    public void event(DeviceEvent event) {
      eventExecutor.execute(() -> processEventInternal(event));
    }

    private void processEventInternal(DeviceEvent event) {
      switch (event.type()) {
        case DEVICE_REMOVED:
        case DEVICE_SUSPENDED:
          DeviceId deviceId = event.subject().id();
          if (!deviceService.isAvailable(deviceId)) {
            BasicDeviceConfig cfg = netCfgService.getConfig(deviceId, BasicDeviceConfig.class);
            // if purgeOnDisconnection is set for the device or it's a global configuration
            // lets remove the stats.
            boolean purge = cfg != null && cfg.isPurgeOnDisconnectionConfigured() ? cfg.purgeOnDisconnection()
                : purgeOnDisconnection;
            if (purge) {
              log.info("PurgeOnDisconnection is requested for device {}, " +
                  "removing statistics", deviceId);
              store.purgeStatistics(deviceId);
            }
          }
          break;
        default:
          break;

      }
    }
  }
}
