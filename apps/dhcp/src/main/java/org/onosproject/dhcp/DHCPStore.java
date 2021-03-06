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
 * DHCPStore Interface.
 */
public interface DHCPStore {

    /**
     * Appends all the IPs in a given range to the free pool of IPs.
     *
     * @param startIP Start IP for the range
     * @param endIP End IP for the range
     */
    void populateIPPoolfromRange(Ip4Address startIP, Ip4Address endIP);

    /**
     * Returns an IP Address for a Mac ID, in response to a DHCP DISCOVER message.
     *
     * @param macID Mac ID of the client requesting an IP
     * @return IP address assigned to the Mac ID
     */
    Ip4Address suggestIP(MacAddress macID, Ip4Address requestedIP);

    /**
     * Assigns the requested IP to the Mac ID, in response to a DHCP REQUEST message.
     *
     * @param macID Mac Id of the client requesting an IP
     * @param ipAddr IP Address being requested
     * @param leaseTime Lease time offered by the server for this mapping
     * @return returns true if the assignment was successful, false otherwise
     */
    boolean assignIP(MacAddress macID, Ip4Address ipAddr, int leaseTime);

    /**
     * Sets the default time for which suggested IP mappings are valid.
     *
     * @param timeInSeconds default time for IP mappings to be valid
     */
    void setDefaultTimeoutForPurge(int timeInSeconds);

    /**
     * Sets the delay after which the dhcp server will purge expired entries.
     *
     * @param timeInSeconds default time
     */
    void setTimerDelay(int timeInSeconds);

    /**
     * Releases the IP assigned to a Mac ID into the free pool.
     *
     * @param macID the macID for which the mapping needs to be changed
     */
    void releaseIP(MacAddress macID);

    /**
     * Returns a collection of all the MacAddress to IPAddress mapping.
     *
     * @return the collection of the mappings
     */
    Map<MacAddress, Ip4Address> listMapping();

    /**
     * Assigns the requested IP to the MAC ID (if available) for an indefinite period of time.
     *
     * @param macID macID of the client
     * @param ipAddr IP Address requested for the client
     * @return true if the mapping was successfully registered, false otherwise
     */
    boolean assignStaticIP(MacAddress macID, Ip4Address ipAddr);

    /**
     * Removes a static IP mapping associated with the given MAC ID from the DHCP Server.
     *
     * @param macID macID of the client
     * @return true if the mapping was successfully registered, false otherwise
     */
    boolean removeStaticIP(MacAddress macID);

    /**
     * Returns the list of all the available IPs with the server.
     *
     * @return list of available IPs
     */
    Iterable<Ip4Address> getAvailableIPs();

}
