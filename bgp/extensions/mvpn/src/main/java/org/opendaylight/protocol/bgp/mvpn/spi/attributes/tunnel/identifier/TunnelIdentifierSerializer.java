/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.mvpn.spi.attributes.tunnel.identifier;

import io.netty.buffer.ByteBuf;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pmsi.tunnel.rev180329.pmsi.tunnel.pmsi.tunnel.TunnelIdentifier;

public interface TunnelIdentifierSerializer<T extends TunnelIdentifier> {
    /**
     * Serialize tunnel identifier.
     *
     * @param tunnelIdentifier Tunnel Identifier body
     * @param buffer           Encoded Tunnel Identifier in ByteBuf
     * @return Tunnel identifier Type
     */
    int serialize(@NonNull T tunnelIdentifier, @NonNull ByteBuf buffer);

    Class<? extends TunnelIdentifier> getClazz();
}
