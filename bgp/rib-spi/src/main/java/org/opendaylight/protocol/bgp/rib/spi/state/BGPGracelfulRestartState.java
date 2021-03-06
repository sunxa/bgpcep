/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.rib.spi.state;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.bgp.operational.rev151009.BgpAfiSafiGracefulRestartState.Mode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev180329.rib.TablesKey;

/**
 * BGP Operational Graceful Restart State.
 */
@NonNullByDefault
public interface BGPGracelfulRestartState {
    /**
     * is Graceful Restart Supported advertized to neighbor.
     *
     * @param tablesKey tables Key
     * @return true if Afi Safi was advertized to neighbor
     */
    boolean isGracefulRestartAdvertized(TablesKey tablesKey);

    /**
     * is Graceful Restart Supported advertized by neighbor.
     *
     * @param tablesKey tables Key
     * @return true if Afi Safi was advertized by neighbor
     */
    boolean isGracefulRestartReceived(TablesKey tablesKey);

    /**
     * This flag indicates whether the local neighbor is currently restarting.
     *
     * @return local restarting state
     */
    boolean isLocalRestarting();

    /**
     * The period of time (advertised by the peer) that the peer expects a restart of a
     * BGP session to take.
     *
     * @return time
     */
    int getPeerRestartTime();

    /**
     * This flag indicates whether the remote neighbor is currently in the process of
     * restarting, and hence received routes are currently stale.
     *
     * @return peer is restarting
     */
    boolean isPeerRestarting();

    /**
     * Returns operational mode of graceful restart. Result depends on advertising
     * and receiving graceful restart capability to/from peer.
     *
     * @return graceful restart operational mode
     */
    Mode getMode();
}
