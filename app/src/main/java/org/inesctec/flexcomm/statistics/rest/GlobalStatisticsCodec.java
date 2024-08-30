package org.inesctec.flexcomm.statistics.rest;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inesctec.flexcomm.statistics.api.GlobalStatistics;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.impl.AnnotatedCodec;

import com.fasterxml.jackson.databind.node.ObjectNode;

public final class GlobalStatisticsCodec extends AnnotatedCodec<GlobalStatistics> {

  @Override
  public ObjectNode encode(GlobalStatistics entry, CodecContext context) {
    checkNotNull(entry, "Global Statistics cannot be null");

    final ObjectNode result = context.mapper().createObjectNode()
        .put("currentConsumption", entry.currentConsumption())
        .put("powerDrawn", entry.powerDrawn());

    return annotate(result, entry, context);
  }

}
