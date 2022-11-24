package org.inesctec.flexcomm.ofexp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.inesctec.flexcomm.ofexp.returns.GlobalStats;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.openflow.controller.Dpid;
import org.onosproject.openflow.controller.OpenFlowController;
import org.onosproject.openflow.controller.OpenFlowMessageListener;
import org.onosproject.openflow.controller.OpenFlowSwitch;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.projectfloodlight.openflow.protocol.OFExperimenter;
import org.projectfloodlight.openflow.protocol.OFFlexcommGlobalStatsReply;
import org.projectfloodlight.openflow.protocol.OFFlexcommGlobalStatsRequest;
import org.projectfloodlight.openflow.protocol.OFFlexcommMsgType;
import org.projectfloodlight.openflow.protocol.OFFlexcommStatsHeader;
import org.projectfloodlight.openflow.protocol.OFFlexcommStatsReply;
import org.projectfloodlight.openflow.protocol.OFFlexcommStatsType;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FlexCommOpenFlow
 */
@Component(immediate = true)
public class FlexCommOpenFlowImpl implements FlexCommOpenFlow{

    private final static String APP_NAME = "org.inesctec.flexcomm.ofexp";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AtomicLong xidAtomic = new AtomicLong(1);

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected OpenFlowController openFlowController;

    protected ApplicationId appId;

    private OpenFlowMessageListener messageListener;
    private BlockingHashMap<GlobalStats> globalReps;
    //private BlockingHashMap<PortStats> globalReps = new BlockingHashMap<>();
    
    private ExecutorService executorService;

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(APP_NAME);

        executorService = Executors.newFixedThreadPool(5);

        messageListener = new InternalOpenFlowMessageListener();
        globalReps = new BlockingHashMap<>();

        openFlowController.addMessageListener(messageListener);
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        openFlowController.removeMessageListener(messageListener);
        log.info("Stopped");
    }

    @Override
    public Future<GlobalStats> getGlobalStats(Dpid dpid) {
        OpenFlowSwitch openFlowSwitch = openFlowController.getSwitch(dpid);

        if (openFlowSwitch == null) {
            log.error("switch {} is null", dpid);
            return null;
        }

        if (!openFlowSwitch.isConnected()) {
            log.error("switch {} is not connected", dpid);
            return null;
        }

        long xid = xidAtomic.getAndIncrement();
        OFFlexcommGlobalStatsRequest req = openFlowSwitch.factory()
                                                         .buildFlexcommGlobalStatsRequest()
                                                         .setXid(xid)
                                                         .build();
        openFlowSwitch.sendMsg(req);

        return CompletableFuture.supplyAsync(() -> this.globalReps.get(xid), executorService);
    }

    //public Future<PortStats> getPortStats(Dpid dpid, PortNumber port);

    private class InternalOpenFlowMessageListener implements OpenFlowMessageListener {

        @Override
        public void handleIncomingMessage(Dpid dpid, OFMessage msg) {
            OpenFlowSwitch sw = openFlowController.getSwitch(dpid);
            if (sw == null) {
                //log something
                return;
            }

            if (msg.getType() == OFType.EXPERIMENTER &&
                ((OFExperimenter)msg).getExperimenter() == 0xf82aL) {
                OFFlexcommStatsHeader header = (OFFlexcommStatsHeader)msg;
                if (header.getSubtype() == OFFlexcommMsgType.REPLY.ordinal()) {
                    OFFlexcommStatsReply rep = (OFFlexcommStatsReply)header;
                    if (rep.getStatType() == OFFlexcommStatsType.ST_GLOBAL.ordinal()) {
                        OFFlexcommGlobalStatsReply rep_global = (OFFlexcommGlobalStatsReply)rep;

                        GlobalStats ret = 
                            new GlobalStats(Double.longBitsToDouble(rep_global.getConsumption().getValue()),
                                            Double.longBitsToDouble(rep_global.getLoad().getValue()));

                        globalReps.put(rep_global.getXid(), ret);
                    } else {
                        log.error("Got invalid FlexComm stat type from {}: {}", dpid, rep);
                    }
                } else {
                    log.error("Got invalid FlexComm type from {}: {}", dpid, header);
                }
            }
        }

        @Override
        public void handleOutgoingMessage(Dpid dpid, List<OFMessage> msgs) {
            // do nothing
            return;
        }
    }

    private class BlockingHashMap<V> {

        private Map<Long, V> map;

        public BlockingHashMap() {
            this.map = new HashMap<>();
        }

        public V get(Long xid) {
            V value;
            synchronized (this.map) {
                while (!this.map.containsKey(xid)){
                    try {
                        this.map.wait();
                    } catch (Exception e) {
                        log.error("Got Exception {} while waiting for get", e);
                    }
                }
                value = this.map.get(xid);
                this.map.remove(xid);
            }
            return value;
        }

        public void put(Long xid, V value) {
            synchronized (this.map) {
                this.map.put(xid, value);
                this.map.notifyAll();
            }
        }
    }
}
