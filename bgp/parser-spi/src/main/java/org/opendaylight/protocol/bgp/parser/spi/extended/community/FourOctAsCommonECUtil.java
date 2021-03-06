/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.parser.spi.extended.community;

import io.netty.buffer.ByteBuf;
import org.opendaylight.protocol.util.ByteBufUtils;
import org.opendaylight.protocol.util.ByteBufWriteUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.AsNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.as._4.spec.common.As4SpecificCommon;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.as._4.spec.common.As4SpecificCommonBuilder;

public final class FourOctAsCommonECUtil {
    private FourOctAsCommonECUtil() {
        throw new UnsupportedOperationException();
    }

    public static As4SpecificCommon parseCommon(final ByteBuf body) {
        return new As4SpecificCommonBuilder().setAsNumber(new AsNumber(ByteBufUtils.readUint32(body)))
            .setLocalAdministrator(ByteBufUtils.readUint16(body)).build();
    }

    public static void serializeCommon(final As4SpecificCommon extComm, final ByteBuf body) {
        body.writeInt(extComm.getAsNumber().getValue().intValue());
        ByteBufWriteUtil.writeUnsignedShort(extComm.getLocalAdministrator(), body);
    }
}
