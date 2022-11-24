package org.inesctec.flexcomm.ofexp;

import java.util.concurrent.Future;

import org.inesctec.flexcomm.ofexp.returns.GlobalStats;
import org.onosproject.openflow.controller.Dpid;

/**
 * FlexCommOpenFlow
 */
public interface FlexCommOpenFlow {

    public Future<GlobalStats> getGlobalStats(Dpid dpid);
    //public Future<PortStats> getPortStats(Dpid dpid, PortNumber port);
    
}
