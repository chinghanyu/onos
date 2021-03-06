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
package org.onosproject.incubator.net.domain;

import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;

/**
 * Provides connectivity through a domain.
 */
public class TunnelPrimitive extends IntentPrimitive {

    private final ConnectPoint one;
    private final ConnectPoint two;

    public TunnelPrimitive(ApplicationId appId, ConnectPoint one, ConnectPoint two) {
        super(appId);
        this.one = one;
        this.two = two;
    }

    /**
     * The getter for the first connection point associated with a tunnel.
     *
     * @return the first connection point
     */
    public ConnectPoint one() {
        return one;
    }

    /**
     * The getter for the second connection point associated with a tunnel.
     * @return the second connection point
     */
    public ConnectPoint two() {
        return two;
    }
}
