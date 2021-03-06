/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bmp.impl.app;

import com.google.common.collect.ImmutableMap;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev180329.rib.TablesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.AddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.SubsequentAddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bmp.monitor.rev180329.BmpMonitor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

public final class TablesUtil {

    public static final QName BMP_TABLES_QNAME
            = QName.create(BmpMonitor.QNAME.getModule(), "tables");
    public static final QName BMP_ATTRIBUTES_QNAME =
            QName.create(BmpMonitor.QNAME.getModule(), "attributes");
    public static final QName BMP_ROUTES_QNAME = QName.create(BmpMonitor.QNAME.getModule(), "routes");
    public static final QName BMP_AFI_QNAME = QName.create(BMP_TABLES_QNAME.getModule(), "afi");
    public static final QName BMP_SAFI_QNAME = QName.create(BMP_TABLES_QNAME.getModule(), "safi");
    private static final String AFI = "afi";
    private static final String SAFI = "safi";

    private TablesUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates Yang Instance Identifier path argument from supplied AFI and SAFI.
     *
     * @param afi Class representing AFI
     * @param safi Class representing SAFI
     * @return NodeIdentifierWithPredicates for specified AFI, SAFI combination.
     */
    public static NodeIdentifierWithPredicates toYangTablesKey(final Class<? extends AddressFamily> afi,
            final Class<? extends SubsequentAddressFamily> safi) {
        return NodeIdentifierWithPredicates.of(BMP_TABLES_QNAME, ImmutableMap.of(
            BMP_AFI_QNAME, BindingReflections.findQName(afi),
            BMP_SAFI_QNAME, BindingReflections.findQName(safi)));
    }

    /**
     * Creates Yang Instance Identifier path argument from supplied QNAMES and AFI and SAFI.
     *
     * @param nodeName QName reprenting node
     * @param afi Class representing AFI
     * @param safi Class representing SAFI
     * @return NodeIdentifierWithPredicates for specified AFI, SAFI combination.
     */
    public static NodeIdentifierWithPredicates toYangTablesKey(final QName nodeName,
            final Class<? extends AddressFamily> afi,
            final Class<? extends SubsequentAddressFamily> safi) {
        final QName afiQname = QName.create(nodeName, AFI).intern();
        final QName safiQname = QName.create(nodeName, SAFI).intern();
        return NodeIdentifierWithPredicates.of(nodeName, ImmutableMap.of(
            afiQname, BindingReflections.findQName(afi),
            safiQname, BindingReflections.findQName(safi)));
    }

    /**
     * Creates Yang Instance Identifier path argument from supplied {@link TablesKey}.
     *
     * @param tablesKey Tables key representing table.
     * @return NodeIdentifierWithPredicates of for specified AFI, SAFI combination.
     */
    public static NodeIdentifierWithPredicates toYangTablesKey(final TablesKey tablesKey) {
        return toYangTablesKey(tablesKey.getAfi(), tablesKey.getSafi());
    }
}
