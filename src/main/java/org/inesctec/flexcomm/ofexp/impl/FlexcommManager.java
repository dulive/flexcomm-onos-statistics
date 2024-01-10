package org.inesctec.flexcomm.ofexp.impl;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.inesctec.flexcomm.ofexp.api.FlexcommEvent;
import org.inesctec.flexcomm.ofexp.api.FlexcommListener;
import org.inesctec.flexcomm.ofexp.api.FlexcommProvider;
import org.inesctec.flexcomm.ofexp.api.FlexcommProviderRegistry;
import org.inesctec.flexcomm.ofexp.api.FlexcommProviderService;
import org.inesctec.flexcomm.ofexp.api.FlexcommService;
import org.inesctec.flexcomm.ofexp.api.FlexcommStore;
import org.inesctec.flexcomm.ofexp.api.FlexcommStoreDelegate;
import org.inesctec.flexcomm.ofexp.api.GlobalStatistics;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.net.DeviceId;
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

import static org.inesctec.flexcomm.ofexp.impl.OsgiPropertyConstants.FM_PURGE_ON_DISCONNECTION;
import static org.inesctec.flexcomm.ofexp.impl.OsgiPropertyConstants.FM_PURGE_ON_DISCONNECTION_DEFAULT;
import static org.slf4j.LoggerFactory.getLogger;
import static org.onosproject.security.AppGuard.checkPermission;
import static org.onosproject.security.AppPermission.Type.DEVICE_READ;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.onlab.util.Tools.groupedThreads;

// TODO: Add port stats
@Component(immediate = true, service = {
    FlexcommService.class,
    FlexcommProviderRegistry.class
}, property = {
    FM_PURGE_ON_DISCONNECTION + ":Boolean=" + FM_PURGE_ON_DISCONNECTION_DEFAULT
})
public class FlexcommManager
    extends AbstractListenerProviderRegistry<FlexcommEvent, FlexcommListener, FlexcommProvider, FlexcommProviderService>
    implements FlexcommService, FlexcommProviderRegistry {

  public static final String DEVICE_ID_NULL = "Device ID cannot be null";

  private final Logger log = getLogger(getClass());

  private final FlexcommStoreDelegate delegate = new InternalFlexcommStoreDelegate();
  private final DeviceListener deviceListener = new InternalDeviceListener();

  private ExecutorService eventExecutor;

  @Reference(cardinality = ReferenceCardinality.MANDATORY)
  protected FlexcommStore store;

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
    eventDispatcher.addSink(FlexcommEvent.class, listenerRegistry);
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
    eventDispatcher.removeSink(FlexcommEvent.class);
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
  public Collection<GlobalStatistics> getGlobalStatistics() {
    checkPermission(DEVICE_READ);
    return store.getGlobalStatistics();
  }

  @Override
  public Collection<GlobalStatistics> getGlobalDeltaStatistics() {
    checkPermission(DEVICE_READ);
    return store.getGlobalDeltaStatistics();
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
  protected FlexcommProviderService createProviderService(FlexcommProvider provider) {
    return new InternalFlexcommProviderService(provider);
  }

  private class InternalFlexcommStoreDelegate implements FlexcommStoreDelegate {

    @Override
    public void notify(FlexcommEvent event) {
      post(event);
    }

  }

  private class InternalFlexcommProviderService extends AbstractProviderService<FlexcommProvider>
      implements FlexcommProviderService {

    InternalFlexcommProviderService(FlexcommProvider provider) {
      super(provider);
    }

    @Override
    public void updateGlobalStatistics(DeviceId deviceId, GlobalStatistics globalStatistics) {
      checkNotNull(deviceId, DEVICE_ID_NULL);
      checkNotNull(globalStatistics, "Global statistics cannot be null");
      checkValidity();

      FlexcommEvent event = store.updateGlobalStatistics(deviceId, globalStatistics);
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
                  "removing stats", deviceId);
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
