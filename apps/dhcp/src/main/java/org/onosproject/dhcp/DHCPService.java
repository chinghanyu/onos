/*
 * Copyright 2014 Open Networking Laboratory
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
package org.onosproject.dhcp;

import org.onlab.packet.Ip4Address;
import org.onlab.packet.MacAddress;

import java.util.Map;

/**
 * DHCP Service Interface.
 */
public interface DHCPService {

    /**
     * Returns a collection of all the MacAddress to IPAddress mapping.
     *
     * @return collection of mappings.
     */
    Map<MacAddress, Ip4Address> listMapping();

    /**
     * Returns the default lease time granted by the DHCP Server.
     *
     * @return lease time
     */
    int getLeaseTime();

    /**
     * Returns the default renewal time granted by the DHCP Server.
     *
     * @return renewal time
     */
    int getRenewalTime();

    /**
     * Returns the default rebinding time granted by the DHCP Server.
     *
     * @return rebinding time
     */
    int getRebindingTime();

    /**
     * Registers a static IP mapping with the DHCP Server.
     *
     * @param macID     macID of the client
     * @param ipAddress IP Address requested for the client
     * @return true if the mapping was successfully registered, false otherwise
     */
    boolean setStaticMapping(MacAddress macID, Ip4Address ipAddress);

    /**
     * Removes a static IP mapping with the DHCP Server.
     *
     * @param macID macID of the client
     * @return true if the mapping was successfully removed, false otherwise
     */
    boolean removeStaticMapping(MacAddress macID);

    /**
     * Returns the list of all the available IPs with the server.
     *
     * @return list of available IPs
     */
    Iterable<Ip4Address> getAvailableIPs();

}
