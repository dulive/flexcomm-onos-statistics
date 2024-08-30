package org.inesctec.flexcomm.statistics.rest;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inesctec.flexcomm.statistics.api.PortStatistics;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.impl.AnnotatedCodec;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class PortStatisticsCodec extends AnnotatedCodec<PortStatistics> {

  @Override
  public ObjectNode encode(PortStatistics entry, CodecContext context) {
    checkNotNull(entry, "Port Statistics cannot be null");

    final ObjectNode result = context.mapper().createObjectNode()
        .put("port", entry.portNumber().toLong())
        .put("currentConsumption", entry.currentConsumption())
        .put("powerDrawn", entry.powerDrawn());

    return annotate(result, entry, context);
  }

}
