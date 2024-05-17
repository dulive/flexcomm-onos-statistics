package org.inesctec.flexcomm.ofexp.impl;

import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;

import org.inesctec.flexcomm.ofexp.api.DefaultGlobalStatistics;
import org.inesctec.flexcomm.ofexp.api.FlexcommStatisticsProvider;
import org.inesctec.flexcomm.ofexp.api.FlexcommStatisticsProviderRegistry;
import org.inesctec.flexcomm.ofexp.api.FlexcommStatisticsProviderService;
import org.inesctec.flexcomm.ofexp.api.GlobalStatistics;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.openflow.controller.OpenFlowController;
import org.onosproject.openflow.controller.OpenFlowEventListener;
import org.onosproject.openflow.controller.OpenFlowSwitch;
import org.onosproject.openflow.controller.OpenFlowSwitchListener;
import org.onosproject.openflow.controller.RoleState;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.projectfloodlight.openflow.protocol.OFExperimenterStatsReply;
import org.projectfloodlight.openflow.protocol.OFFlexcommGlobalEnergyReply;
import org.projectfloodlight.openflow.protocol.OFFlexcommStatsReply;
import org.projectfloodlight.openflow.protocol.OFFlexcommSubtype;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPortStatus;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsType;
import org.slf4j.Logger;

import com.google.common.collect.Maps;

import static org.slf4j.LoggerFactory.getLogger;
import static org.inesctec.flexcomm.ofexp.impl.OsgiPropertyConstants.POLL_FREQ;
import static org.inesctec.flexcomm.ofexp.impl.OsgiPropertyConstants.POLL_FREQ_DEFAULT;
import static org.onlab.util.Tools.get;
import static com.google.common.base.Strings.isNullOrEmpty;

@Component(immediate = true, property = {
    POLL_FREQ + ":Integer=" + POLL_FREQ_DEFAULT,
})
public class OpenFlowFlexcomStatisticsProvider extends AbstractProvider implements FlexcommStatisticsProvider {

  public static final long FLEXCOMM_EXPERIMENTER = 0xf82aL;

  private final Logger log = getLogger(getClass());

  @Reference(cardinality = ReferenceCardinality.MANDATORY)
  protected ComponentConfigService cfgService;

  @Reference(cardinality = ReferenceCardinality.MANDATORY)
  protected OpenFlowController openFlowController;

  @Reference(cardinality = ReferenceCardinality.MANDATORY)
  protected FlexcommStatisticsProviderRegistry providerRegistry;

  private FlexcommStatisticsProviderService providerService;

  private final InternalFlexcommProvider listener = new InternalFlexcommProvider();

  private int flexcommStatsPollFrequency = POLL_FREQ_DEFAULT;

  private final Timer timer = new Timer("onos-openflow-flexcomm-collector");

  private Map<Dpid, FlexcommStatisticsCollector> collectors = Maps.newConcurrentMap();

  public OpenFlowFlexcomStatisticsProvider() {
    super(new ProviderId("of", "org.inesctec.provider.flexcomm"));
  }

  @Activate
  public void activate(ComponentContext context) {
    cfgService.registerProperties(getClass());

    providerService = providerRegistry.register(this);

    openFlowController.addListener(listener);
    openFlowController.addEventListener(listener);

    modified(context);

    log.info("Started");
  }

  @Deactivate
  public void deactivate(ComponentContext context) {
    cfgService.unregisterProperties(getClass(), false);
    listener.disable();

    openFlowController.removeEventListener(listener);
    openFlowController.removeListener(listener);

    providerRegistry.unregister(this);

    collectors.values().forEach(FlexcommStatisticsCollector::stop);
    collectors.clear();

    providerService = null;

    log.info("Stopped");
  }

  @Modified
  public void modified(ComponentContext context) {
    Dictionary<?, ?> properties = context != null ? context.getProperties() : new Properties();
    int newFlexcommStatsPollFrequency;
    try {
      String s = get(properties, POLL_FREQ);
      newFlexcommStatsPollFrequency = isNullOrEmpty(s) ? flexcommStatsPollFrequency : Integer.parseInt(s.trim());

    } catch (NumberFormatException | ClassCastException e) {
      newFlexcommStatsPollFrequency = flexcommStatsPollFrequency;
    }

    if (newFlexcommStatsPollFrequency != flexcommStatsPollFrequency) {
      flexcommStatsPollFrequency = newFlexcommStatsPollFrequency;
      collectors.values().forEach(fsc -> fsc.adjustPollInterval(flexcommStatsPollFrequency));
    }

    log.info("Settings: flexcommStatsPollFrequency={}", flexcommStatsPollFrequency);
  }

  private class InternalFlexcommProvider implements OpenFlowSwitchListener, OpenFlowEventListener {

    private boolean isDisable = false;

    private void stopCollectorIfNeeded(FlexcommStatisticsCollector collector) {
      if (collector != null) {
        collector.stop();
      }
    }

    @Override
    public void portChanged(Dpid dpid, OFPortStatus status) {
      return;
    }

    @Override
    public void receivedRoleReply(Dpid dpid, RoleState requested, RoleState response) {
      return;
    }

    @Override
    public void switchAdded(Dpid dpid) {
      if (providerService == null) {
        return;
      }

      OpenFlowSwitch sw = openFlowController.getSwitch(dpid);
      if (sw == null) {
        log.error("Switch {} is not found", dpid);
        return;
      }

      // TODO: forma de verificar se switch suporta flexcomm stats?
      FlexcommStatisticsCollector fsc = new FlexcommStatisticsCollector(timer, sw, flexcommStatsPollFrequency);
      stopCollectorIfNeeded(collectors.put(dpid, fsc));
      fsc.start();

      if (openFlowController.getSwitch(dpid) == null) {
        switchRemoved(dpid);
      }
    }

    @Override
    public void switchChanged(Dpid dpid) {
      return;
    }

    @Override
    public void switchRemoved(Dpid dpid) {
      stopCollectorIfNeeded(collectors.remove(dpid));
    }

    @Override
    public void handleMessage(Dpid dpid, OFMessage msg) {
      if (isDisable) {
        return;
      }

      try {
        switch (msg.getType()) {
          case STATS_REPLY:
            if (((OFStatsReply) msg).getStatsType() == OFStatsType.EXPERIMENTER &&
                ((OFExperimenterStatsReply) msg).getExperimenter() == FLEXCOMM_EXPERIMENTER) {
              OFFlexcommStatsReply flexcommStatsReply = (OFFlexcommStatsReply) msg;

              if (flexcommStatsReply.getSubtype() == OFFlexcommSubtype.GLOBAL_ENERGY.ordinal()) {
                OFFlexcommGlobalEnergyReply flexcommGlobalEnergyReply = (OFFlexcommGlobalEnergyReply) msg;
                double currentConsumption = Double
                    .longBitsToDouble(flexcommGlobalEnergyReply.getCurrentConsumption().getValue());
                double powerDrawn = Double
                    .longBitsToDouble(flexcommGlobalEnergyReply.getPowerDrawn().getValue());
                DeviceId deviceId = DeviceId.deviceId(Dpid.uri(dpid));
                GlobalStatistics.Builder builder = DefaultGlobalStatistics.builder();
                GlobalStatistics stats = builder.setDeviceId(deviceId)
                    .setCurrentConsumption(currentConsumption)
                    .setPowerDrawn(powerDrawn)
                    .build();
                providerService.updateGlobalStatistics(deviceId, stats);

              } else if (flexcommStatsReply.getSubtype() == OFFlexcommSubtype.PORT_ENERGY.ordinal()) {
                // TODO: handle port energy reply
              }
            }
            break;
          default:
            break;
        }
      } catch (IllegalStateException e) {

      }
    }

    private void disable() {
      isDisable = true;
    }

  }
}
