package org.inesctec.flexcomm.statistics.rest;

import static org.slf4j.LoggerFactory.getLogger;

import org.inesctec.flexcomm.statistics.api.GlobalStatistics;
import org.inesctec.flexcomm.statistics.api.PortStatistics;
import org.onosproject.codec.CodecService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;

@Component(immediate = true)
public class FlexcommStatisticsCodecRegistrator {

  private final Logger log = getLogger(getClass());

  @Reference(cardinality = ReferenceCardinality.MANDATORY)
  protected CodecService codecService;

  @Activate
  public void activate() {
    codecService.registerCodec(GlobalStatistics.class, new GlobalStatisticsCodec());
    codecService.registerCodec(PortStatistics.class, new PortStatisticsCodec());

    log.info("Started");
  }

  @Deactivate
  public void deactivate() {
    codecService.unregisterCodec(GlobalStatistics.class);
    codecService.unregisterCodec(PortStatistics.class);

    log.info("Stopped");
  }

}
