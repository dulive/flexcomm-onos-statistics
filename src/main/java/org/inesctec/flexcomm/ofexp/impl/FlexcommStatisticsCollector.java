package org.inesctec.flexcomm.ofexp.impl;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import org.onosproject.openflow.controller.OpenFlowSwitch;
import org.onosproject.openflow.controller.RoleState;
import org.projectfloodlight.openflow.protocol.OFFlexcommGlobalEnergyRequest;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;
import static com.google.common.base.Preconditions.checkNotNull;

public class FlexcommStatisticsCollector {

  private final Logger log = getLogger(getClass());

  private static final long SECONDS = 1000L;

  private OpenFlowSwitch sw;
  private Timer timer;
  private TimerTask task;

  private int refreshInterval;
  private final AtomicLong xidAtomic = new AtomicLong(1);

  public FlexcommStatisticsCollector(Timer timer, OpenFlowSwitch sw, int interval) {
    this.timer = timer;
    this.sw = checkNotNull(sw, "Null switch");
    this.refreshInterval = interval;
  }

  private class InternalTimerTask extends TimerTask {

    @Override
    public void run() {
      sendFlexcommStatisticRequest();
    }
  }

  public synchronized void start() {
    log.info("Starting Flexcomm Stats collection thread for {}", sw.getStringId());
    task = new InternalTimerTask();
    timer.scheduleAtFixedRate(task, 1 * SECONDS, refreshInterval * SECONDS);
  }

  public synchronized void stop() {
    log.info("Stopping Flexcomm Stats collection thread for {}", sw.getStringId());
    task.cancel();
    task = null;
  }

  public synchronized void adjustPollInterval(int pollInterval) {
    this.refreshInterval = pollInterval;
    task.cancel();
    task = new InternalTimerTask();
    timer.scheduleAtFixedRate(task, refreshInterval * SECONDS, refreshInterval * SECONDS);
  }

  private void sendFlexcommStatisticRequest() {
    if (sw.getRole() != RoleState.MASTER) {
      return;
    }

    log.trace("Collecting stats for {}", sw.getStringId());

    // TODO: add flexcomm port stats request
    // try using multipart msg
    Long statsXid = xidAtomic.getAndIncrement();
    OFFlexcommGlobalEnergyRequest statsRequest = sw.factory().buildFlexcommGlobalEnergyRequest().setXid(statsXid)
        .build();
    sw.sendMsg(statsRequest);
  }

}
