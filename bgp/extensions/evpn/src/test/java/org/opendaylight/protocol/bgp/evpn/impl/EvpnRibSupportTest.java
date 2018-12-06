/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.evpn.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.opendaylight.protocol.bgp.evpn.impl.EvpnTestUtil.RD;
import static org.opendaylight.protocol.bgp.evpn.impl.nlri.EthADRParserTest.ETHERNET_AD_ROUTE_CASE;
import static org.opendaylight.protocol.bgp.evpn.impl.nlri.EthADRParserTest.ETHERNET_AD_ROUTE_CASE_KEY;
import static org.opendaylight.protocol.bgp.parser.spi.PathIdUtil.NON_PATH_ID;

import com.google.common.collect.ImmutableSet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.protocol.bgp.evpn.impl.nlri.EvpnNlriParser;
import org.opendaylight.protocol.bgp.parser.spi.BGPExtensionProviderContext;
import org.opendaylight.protocol.bgp.parser.spi.pojo.SimpleBGPExtensionProviderContext;
import org.opendaylight.protocol.bgp.rib.spi.AbstractRIBSupportTest;
import org.opendaylight.protocol.util.ByteArray;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.bgp.rib.rib.loc.rib.tables.routes.EvpnRoutesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.bgp.rib.rib.loc.rib.tables.routes.EvpnRoutesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.evpn.destination.EvpnDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.evpn.destination.EvpnDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.evpn.routes.EvpnRoutes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.evpn.routes.EvpnRoutesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.evpn.routes.evpn.routes.EvpnRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.evpn.routes.evpn.routes.EvpnRouteBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.evpn.routes.evpn.routes.EvpnRouteKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.update.attributes.mp.reach.nlri.advertized.routes.destination.type.DestinationEvpnCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.update.attributes.mp.reach.nlri.advertized.routes.destination.type.DestinationEvpnCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.update.attributes.mp.reach.nlri.advertized.routes.destination.type.destination.evpn._case.DestinationEvpnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.PathId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.Update;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.Attributes1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.Attributes2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev180329.rib.tables.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev180329.rib.tables.Routes;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;

public final class EvpnRibSupportTest extends AbstractRIBSupportTest<EvpnRoutesCase, EvpnRoutes, EvpnRoute,
        EvpnRouteKey> {
    private EvpnRibSupport ribSupport;
    private static final EvpnRoute ROUTE;
    private static final EvpnRouteKey ROUTE_KEY;
    private static final EvpnDestination EVPN_DESTINATION = new EvpnDestinationBuilder()
            .setRouteDistinguisher(RD)
            .setEvpnChoice(ETHERNET_AD_ROUTE_CASE)
            .setPathId(NON_PATH_ID)
            .build();
    private static final DestinationEvpnCase REACH_NLRI = new DestinationEvpnCaseBuilder()
            .setDestinationEvpn(new DestinationEvpnBuilder()
                    .setEvpnDestination(Collections.singletonList(EVPN_DESTINATION)).build()).build();
    private static final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.update
            .attributes.mp.unreach.nlri.withdrawn.routes.destination.type.DestinationEvpnCase UNREACH_NLRI =
            new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn.rev180329.update.attributes
                    .mp.unreach.nlri.withdrawn.routes.destination.type.DestinationEvpnCaseBuilder()
                    .setDestinationEvpn(new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.evpn
                            .rev180329.update.attributes.mp.unreach.nlri.withdrawn.routes.destination.type
                            .destination.evpn._case.DestinationEvpnBuilder()
                            .setEvpnDestination(Collections.singletonList(EVPN_DESTINATION)).build()).build();

    private static final EvpnRoutes EVPN_ROUTES;

    static {
        final BGPActivator act = new BGPActivator();
        final BGPExtensionProviderContext context = new SimpleBGPExtensionProviderContext();
        act.start(context);
        final ByteBuf buffer = Unpooled.buffer();
        EvpnNlriParser.serializeNlri(Collections.singletonList(new EvpnDestinationBuilder()
                .setRouteDistinguisher(RD).setEvpnChoice(ETHERNET_AD_ROUTE_CASE_KEY).build()), buffer);
        ROUTE_KEY = new EvpnRouteKey(new PathId(0L), ByteArray.encodeBase64(buffer));
        ROUTE = new EvpnRouteBuilder().setRouteKey(ROUTE_KEY.getRouteKey())
                .setPathId(ROUTE_KEY.getPathId())
                .setAttributes(ATTRIBUTES).setRouteDistinguisher(RD).setEvpnChoice(ETHERNET_AD_ROUTE_CASE).build();
        EVPN_ROUTES = new EvpnRoutesBuilder().setEvpnRoute(Collections.singletonList(ROUTE)).build();
    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.ribSupport = EvpnRibSupport.getInstance(this.mappingService);
        setUpTestCustomizer(this.ribSupport);
    }

    @Test
    public void testDeleteRoutes() {
        this.ribSupport.deleteRoutes(this.tx, getTablePath(), createNlriWithDrawnRoute(UNREACH_NLRI));
        final InstanceIdentifier<EvpnRoute> instanceIdentifier = this.deletedRoutes.get(0);
        assertEquals(ROUTE_KEY, instanceIdentifier.firstKeyOf(EvpnRoute.class));
    }

    @Test
    public void testPutRoutes() {
        this.ribSupport.putRoutes(this.tx, getTablePath(), createNlriAdvertiseRoute(REACH_NLRI), createAttributes());
        final EvpnRoute route = (EvpnRoute) this.insertedRoutes.get(0).getValue();
        assertEquals(ROUTE, route);
    }


    @Test
    public void testEmptyRoute() {
        assertEquals(createEmptyTable(), this.ribSupport.emptyTable());
    }

    @Test
    public void testBuildMpUnreachNlriUpdate() {
        final Update update = this.ribSupport.buildUpdate(Collections.emptyList(),
                createRoutes(EVPN_ROUTES), ATTRIBUTES);
        assertEquals(UNREACH_NLRI, update.getAttributes().augmentation(Attributes2.class).getMpUnreachNlri()
                .getWithdrawnRoutes().getDestinationType());
        assertNull(update.getAttributes().augmentation(Attributes1.class));
    }

    @Test
    public void testBuildMpReachNlriUpdate() {
        final Update update = this.ribSupport.buildUpdate(
                createRoutes(EVPN_ROUTES), Collections.emptyList(), ATTRIBUTES);
        assertEquals(REACH_NLRI, update.getAttributes().augmentation(Attributes1.class).getMpReachNlri()
                .getAdvertizedRoutes().getDestinationType());
        assertNull(update.getAttributes().augmentation(Attributes2.class));
    }

    @Test
    public void testCacheableNlriObjects() {
        assertEquals(ImmutableSet.of(), this.ribSupport.cacheableNlriObjects());
    }

    @Test
    public void testCacheableAttributeObjects() {
        assertEquals(ImmutableSet.of(), this.ribSupport.cacheableAttributeObjects());
    }

    @Test
    public void testRouteIdAddPath() {
        assertEquals(ROUTE_KEY, this.ribSupport.createRouteListKey(ROUTE_KEY.getPathId(), ROUTE_KEY.getRouteKey()));
    }

    @Test
    public void testRoutePath() {
        final NodeIdentifierWithPredicates prefixNii = createRouteNIWP(EVPN_ROUTES);
        assertEquals(getRoutePath().node(prefixNii),
                this.ribSupport.routePath(getTablePath(), prefixNii));
    }

    @Test
    public void testRouteAttributesIdentifier() {
        assertEquals(new NodeIdentifier(QName.create(EvpnRoutes.QNAME,
                Attributes.QNAME.getLocalName().intern())), this.ribSupport.routeAttributesIdentifier());
    }

    @Test
    public void testRoutesCaseClass() {
        assertEquals(EvpnRoutesCase.class, this.ribSupport.routesCaseClass());
    }

    @Test
    public void testRoutesContainerClass() {
        assertEquals(EvpnRoutes.class, this.ribSupport.routesContainerClass());
    }

    @Test
    public void testRoutesListClass() {
        assertEquals(EvpnRoute.class, this.ribSupport.routesListClass());
    }

    @Test
    public void testChangedRoutes() {
        final Routes emptyCase = new EvpnRoutesCaseBuilder().build();
        DataTreeCandidateNode tree = DataTreeCandidates.fromNormalizedNode(getRoutePath(),
                createRoutes(emptyCase)).getRootNode();
        Assert.assertTrue(this.ribSupport.changedRoutes(tree).isEmpty());

        final Routes emptyRoutes = new EvpnRoutesCaseBuilder().setEvpnRoutes(new EvpnRoutesBuilder().build()).build();
        tree = DataTreeCandidates.fromNormalizedNode(getRoutePath(), createRoutes(emptyRoutes)).getRootNode();
        Assert.assertTrue(this.ribSupport.changedRoutes(tree).isEmpty());

        final Routes routes = new EvpnRoutesCaseBuilder().setEvpnRoutes(EVPN_ROUTES).build();
        tree = DataTreeCandidates.fromNormalizedNode(getRoutePath(), createRoutes(routes)).getRootNode();
        final Collection<DataTreeCandidateNode> result = this.ribSupport.changedRoutes(tree);
        Assert.assertFalse(result.isEmpty());
    }
}
