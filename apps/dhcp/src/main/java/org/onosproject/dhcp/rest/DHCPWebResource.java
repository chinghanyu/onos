/*
 * Copyright 2015 Open Networking Laboratory
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
package org.onosproject.dhcp.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.MacAddress;
import org.onosproject.dhcp.DHCPService;
import org.onosproject.rest.AbstractWebResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Manage DHCP address assignments.
 */
@Path("dhcp")
public class DHCPWebResource extends AbstractWebResource {

    final DHCPService service = get(DHCPService.class);

    /**
     * Get DHCP server configuration data.
     * Shows lease, renewal and rebinding times in seconds.
     *
     * @return 200 OK
     */
    @GET
    @Path("config")
    public Response getConfigs() {
        DHCPService service = get(DHCPService.class);
        ObjectNode node = mapper().createObjectNode()
                .put("leaseTime", service.getLeaseTime())
                .put("renewalTime", service.getRenewalTime())
                .put("rebindingTime", service.getRebindingTime());
        return ok(node.toString()).build();
    }

    /**
     * Get all MAC/IP mappings.
     * Shows all MAC/IP mappings held by the DHCP server.
     *
     * @return 200 OK
     */
    @GET
    @Path("mappings")
    public Response listMappings() {
        ObjectNode root = mapper().createObjectNode();

        final Map<MacAddress, Ip4Address> intents = service.listMapping();
        ArrayNode arrayNode = root.putArray("mappings");
        intents.entrySet().forEach(i -> arrayNode.add(mapper().createObjectNode()
                .put("mac", i.getKey().toString())
                .put("ip", i.getValue().toString())));

        return ok(root.toString()).build();
    }



    /**
     * Get all available IPs.
     * Shows all the IPs in the free pool of the DHCP Server.
     *
     * @return 200 OK
     */
    @GET
    @Path("available")
    public Response listAvailableIPs() {
        final Iterable<Ip4Address> availableIPList = service.getAvailableIPs();

        final ObjectNode root = mapper().createObjectNode();
        ArrayNode arrayNode = root.putArray("availableIP");
        availableIPList.forEach(i -> arrayNode.add(i.toString()));
        return ok(root.toString()).build();
    }

    /**
     * Post a new static MAC/IP binding.
     * Registers a static binding to the DHCP server, and displays the current set of bindings.
     *
     * @return 200 OK
     */
    @POST
    @Path("mappings")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setMapping(InputStream stream) {
        ObjectNode root = mapper().createObjectNode();

        try {
            ObjectNode jsonTree = (ObjectNode) mapper().readTree(stream);
            JsonNode macID = jsonTree.get("mac");
            JsonNode ip = jsonTree.get("ip");
            if (macID != null && ip != null) {

                if (!service.setStaticMapping(MacAddress.valueOf(macID.asText()),
                        Ip4Address.valueOf(ip.asText()))) {
                    throw new IllegalArgumentException("Static Mapping Failed. The IP maybe unavailable.");
                }
            }

            final Map<MacAddress, Ip4Address> intents = service.listMapping();
            ArrayNode arrayNode = root.putArray("mappings");
            intents.entrySet().forEach(i -> arrayNode.add(mapper().createObjectNode()
                    .put("mac", i.getKey().toString())
                    .put("ip", i.getValue().toString())));
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        return ok(root.toString()).build();
    }

    /**
     * Delete a static MAC/IP binding.
     * Removes a static binding from the DHCP Server, and displays the current set of bindings.
     *
     * @return 200 OK
     */
    @DELETE
    @Path("mappings/{macID}")
    public Response deleteMapping(@PathParam("macID") String macID) {

        ObjectNode root = mapper().createObjectNode();

        if (!service.removeStaticMapping(MacAddress.valueOf(macID))) {
            throw new IllegalArgumentException("Static Mapping Removal Failed.");
        }
        final Map<MacAddress, Ip4Address> intents = service.listMapping();
        ArrayNode arrayNode = root.putArray("mappings");
        intents.entrySet().forEach(i -> arrayNode.add(mapper().createObjectNode()
                .put("mac", i.getKey().toString())
                .put("ip", i.getValue().toString())));

        return ok(root.toString()).build();
    }
}
