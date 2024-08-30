/*
 * Copyright 2024-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.inesctec.flexcomm.statistics.rest;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.inesctec.flexcomm.statistics.api.FlexcommStatisticsService;
import org.inesctec.flexcomm.statistics.api.GlobalStatistics;
import org.inesctec.flexcomm.statistics.api.PortStatistics;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("")
public class FlexcommStatisticsWebResource extends AbstractWebResource {

  @GET
  @Path("global")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getGlobalStatistics() {
    final FlexcommStatisticsService service = get(FlexcommStatisticsService.class);
    final Iterable<Device> devices = get(DeviceService.class).getDevices();
    final ObjectNode root = mapper().createObjectNode();
    final ArrayNode rootArrayNode = root.putArray("statistics");
    for (final Device device : devices) {
      final ObjectNode deviceStatsNode = mapper().createObjectNode();
      deviceStatsNode.put("device", device.id().toString());
      final ArrayNode statisticsNode = deviceStatsNode.putArray("global");
      final GlobalStatistics globalStatsEntry = service.getGlobalStatistics(device.id());
      if (globalStatsEntry != null) {
        statisticsNode.add(codec(GlobalStatistics.class).encode(globalStatsEntry, this));
      }
      rootArrayNode.add(deviceStatsNode);
    }

    return ok(root).build();
  }

  @GET
  @Path("global/{deviceId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getGlobalStatisticsByDeviceId(@PathParam("deviceId") String deviceId) {
    final FlexcommStatisticsService service = get(FlexcommStatisticsService.class);
    final GlobalStatistics globalStatsEntry = service.getGlobalStatistics(DeviceId.deviceId(deviceId));
    final ObjectNode root = mapper().createObjectNode();
    final ArrayNode rootArrayNode = root.putArray("statistics");
    final ObjectNode deviceStatsNode = mapper().createObjectNode();
    deviceStatsNode.put("device", deviceId);
    final ArrayNode statisticsNode = deviceStatsNode.putArray("global");
    if (globalStatsEntry != null) {
      statisticsNode.add(codec(GlobalStatistics.class).encode(globalStatsEntry, this));
    }
    rootArrayNode.add(deviceStatsNode);

    return ok(root).build();
  }

  @GET
  @Path("delta/global")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getGlobalDeltaStatistics() {
    final FlexcommStatisticsService service = get(FlexcommStatisticsService.class);
    final Iterable<Device> devices = get(DeviceService.class).getDevices();
    final ObjectNode root = mapper().createObjectNode();
    final ArrayNode rootArrayNode = root.putArray("statistics");
    for (final Device device : devices) {
      final ObjectNode deviceStatsNode = mapper().createObjectNode();
      deviceStatsNode.put("device", device.id().toString());
      final ArrayNode statisticsNode = deviceStatsNode.putArray("global");
      final GlobalStatistics globalStatsEntry = service.getGlobalDeltaStatistics(device.id());
      if (globalStatsEntry != null) {
        statisticsNode.add(codec(GlobalStatistics.class).encode(globalStatsEntry, this));
      }
      rootArrayNode.add(deviceStatsNode);
    }

    return ok(root).build();
  }

  @GET
  @Path("delta/global/{deviceId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getGlobalDeltaStatisticsByDeviceId(@PathParam("deviceId") String deviceId) {
    final FlexcommStatisticsService service = get(FlexcommStatisticsService.class);
    final GlobalStatistics globalStatsEntry = service.getGlobalDeltaStatistics(DeviceId.deviceId(deviceId));
    final ObjectNode root = mapper().createObjectNode();
    final ArrayNode rootArrayNode = root.putArray("statistics");
    final ObjectNode deviceStatsNode = mapper().createObjectNode();
    deviceStatsNode.put("device", deviceId);
    final ArrayNode statisticsNode = deviceStatsNode.putArray("global");
    if (globalStatsEntry != null) {
      statisticsNode.add(codec(GlobalStatistics.class).encode(globalStatsEntry, this));
    }
    rootArrayNode.add(deviceStatsNode);

    return ok(root).build();
  }

  @GET
  @Path("ports")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPortStatistics() {
    final FlexcommStatisticsService service = get(FlexcommStatisticsService.class);
    final Iterable<Device> devices = get(DeviceService.class).getDevices();
    final ObjectNode root = mapper().createObjectNode();
    final ArrayNode rootArrayNode = root.putArray("statistics");
    for (final Device device : devices) {
      final ObjectNode deviceStatsNode = mapper().createObjectNode();
      deviceStatsNode.put("device", device.id().toString());
      final ArrayNode statisticsNode = deviceStatsNode.putArray("ports");
      final Iterable<PortStatistics> portStatsEntries = service.getPortStatistics(device.id());
      if (portStatsEntries != null) {
        for (final PortStatistics entry : portStatsEntries) {
          statisticsNode.add(codec(PortStatistics.class).encode(entry, this));
        }
      }
      rootArrayNode.add(deviceStatsNode);
    }

    return ok(root).build();
  }

  @GET
  @Path("ports/{deviceId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPortsStatisticsByDeviceId(@PathParam("deviceId") String deviceId) {
    final FlexcommStatisticsService service = get(FlexcommStatisticsService.class);
    final Iterable<PortStatistics> portStatsEntries = service.getPortStatistics(DeviceId.deviceId(deviceId));
    final ObjectNode root = mapper().createObjectNode();
    final ArrayNode rootArrayNode = root.putArray("statistics");
    final ObjectNode deviceStatsNode = mapper().createObjectNode();
    deviceStatsNode.put("device", deviceId);
    final ArrayNode statisticsNode = deviceStatsNode.putArray("ports");
    if (portStatsEntries != null) {
      for (final PortStatistics entry : portStatsEntries) {
        statisticsNode.add(codec(PortStatistics.class).encode(entry, this));
      }
    }
    rootArrayNode.add(deviceStatsNode);

    return ok(root).build();
  }

  @GET
  @Path("ports/{deviceId}/{port}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPortsStatisticsByDeviceIdAndPort(@PathParam("deviceId") String deviceId,
      @PathParam("port") String port) {
    final FlexcommStatisticsService service = get(FlexcommStatisticsService.class);
    final PortNumber portNumber = PortNumber.portNumber(port);
    final PortStatistics portStatsEntry = service.getStatisticsForPort(DeviceId.deviceId(deviceId), portNumber);
    final ObjectNode root = mapper().createObjectNode();
    final ArrayNode rootArrayNode = root.putArray("statistics");
    final ObjectNode deviceStatsNode = mapper().createObjectNode();
    deviceStatsNode.put("device", deviceId);
    final ArrayNode statisticsNode = deviceStatsNode.putArray("ports");
    if (portStatsEntry != null) {
      statisticsNode.add(codec(PortStatistics.class).encode(portStatsEntry, this));
    }
    rootArrayNode.add(deviceStatsNode);

    return ok(root).build();
  }

  @GET
  @Path("delta/ports")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPortsDeltaStatistics() {
    final FlexcommStatisticsService service = get(FlexcommStatisticsService.class);
    final Iterable<Device> devices = get(DeviceService.class).getDevices();
    final ObjectNode root = mapper().createObjectNode();
    final ArrayNode rootArrayNode = root.putArray("statistics");
    for (final Device device : devices) {
      final ObjectNode deviceStatsNode = mapper().createObjectNode();
      deviceStatsNode.put("device", device.id().toString());
      final ArrayNode statisticsNode = deviceStatsNode.putArray("ports");
      final Iterable<PortStatistics> portStatsEntries = service.getPortDeltaStatistics(device.id());
      if (portStatsEntries != null) {
        for (final PortStatistics entry : portStatsEntries) {
          statisticsNode.add(codec(PortStatistics.class).encode(entry, this));
        }
      }
      rootArrayNode.add(deviceStatsNode);
    }

    return ok(root).build();
  }

  @GET
  @Path("delta/ports/{deviceId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPortsDeltaStatisticsByDeviceId(@PathParam("deviceId") String deviceId) {
    final FlexcommStatisticsService service = get(FlexcommStatisticsService.class);
    final Iterable<PortStatistics> portStatsEntries = service.getPortDeltaStatistics(DeviceId.deviceId(deviceId));
    final ObjectNode root = mapper().createObjectNode();
    final ArrayNode rootArrayNode = root.putArray("statistics");
    final ObjectNode deviceStatsNode = mapper().createObjectNode();
    deviceStatsNode.put("device", deviceId);
    final ArrayNode statisticsNode = deviceStatsNode.putArray("ports");
    if (portStatsEntries != null) {
      for (final PortStatistics entry : portStatsEntries) {
        statisticsNode.add(codec(PortStatistics.class).encode(entry, this));
      }
    }
    rootArrayNode.add(deviceStatsNode);

    return ok(root).build();
  }

  @GET
  @Path("delta/ports/{deviceId}/{port}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPortsDeltaStatisticsByDeviceIdAndPort(@PathParam("deviceId") String deviceId,
      @PathParam("port") String port) {
    final FlexcommStatisticsService service = get(FlexcommStatisticsService.class);
    final PortNumber portNumber = PortNumber.portNumber(port);
    final PortStatistics portStatsEntry = service.getDeltaStatisticsForPort(DeviceId.deviceId(deviceId), portNumber);
    final ObjectNode root = mapper().createObjectNode();
    final ArrayNode rootArrayNode = root.putArray("statistics");
    final ObjectNode deviceStatsNode = mapper().createObjectNode();
    deviceStatsNode.put("device", deviceId);
    final ArrayNode statisticsNode = deviceStatsNode.putArray("ports");
    if (portStatsEntry != null) {
      statisticsNode.add(codec(PortStatistics.class).encode(portStatsEntry, this));
    }
    rootArrayNode.add(deviceStatsNode);

    return ok(root).build();
  }

}
