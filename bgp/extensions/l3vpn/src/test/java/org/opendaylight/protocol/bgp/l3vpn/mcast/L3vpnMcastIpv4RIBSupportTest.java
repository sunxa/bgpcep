/*
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.l3vpn.mcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.protocol.bgp.rib.spi.AbstractRIBSupportTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.bgp.rib.rib.loc.rib.tables.routes.L3vpnMcastRoutesIpv4Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.bgp.rib.rib.loc.rib.tables.routes.L3vpnMcastRoutesIpv4CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.l3vpn.mcast.destination.L3vpnMcastDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.l3vpn.mcast.destination.L3vpnMcastDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.l3vpn.mcast.routes.L3vpnMcastRoute;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.l3vpn.mcast.routes.L3vpnMcastRouteBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.l3vpn.mcast.routes.L3vpnMcastRouteKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.l3vpn.mcast.routes.ipv4.L3vpnMcastRoutesIpv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.l3vpn.mcast.routes.ipv4.L3vpnMcastRoutesIpv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.update.attributes.mp.reach.nlri.advertized.routes.destination.type.DestinationL3vpnMcastIpv4AdvertizedCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.update.attributes.mp.reach.nlri.advertized.routes.destination.type.DestinationL3vpnMcastIpv4AdvertizedCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.update.attributes.mp.reach.nlri.advertized.routes.destination.type.destination.l3vpn.mcast.ipv4.advertized._case.DestinationIpv4L3vpnMcastBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.update.attributes.mp.unreach.nlri.withdrawn.routes.destination.type.DestinationL3vpnMcastIpv4WithdrawnCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.l3vpn.mcast.rev180417.update.attributes.mp.unreach.nlri.withdrawn.routes.destination.type.DestinationL3vpnMcastIpv4WithdrawnCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.PathId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.Update;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.Attributes1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.Attributes2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev180329.rib.tables.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev180329.rib.tables.Routes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.RdIpv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.RouteDistinguisher;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;

public class L3vpnMcastIpv4RIBSupportTest extends AbstractRIBSupportTest<L3vpnMcastRoutesIpv4Case ,L3vpnMcastRoutesIpv4,
        L3vpnMcastRoute, L3vpnMcastRouteKey> {
    private static final PathId PATH_ID = new PathId(Uint32.ZERO);
    private static final IpPrefix IPV4_PREFIX = new IpPrefix(new Ipv4Prefix("34.1.22.0/24"));
    private static final RouteDistinguisher RD = new RouteDistinguisher(new RdIpv4("1.2.3.4:258"));
    private static final L3vpnMcastRouteKey ROUTE_KEY
            = new L3vpnMcastRouteKey(PATH_ID, "IAABAQIDBAECGCIBFgAAAAA=");
    private static final L3vpnMcastRoute ROUTE = new L3vpnMcastRouteBuilder()
            .setRouteKey(ROUTE_KEY.getRouteKey())
            .setPathId(ROUTE_KEY.getPathId())
            .setAttributes(ATTRIBUTES)
            .setRouteDistinguisher(RD)
            .setPrefix(IPV4_PREFIX)
            .build();
    private static final L3vpnMcastRoutesIpv4 MCAST_L3VPN_ROUTES
            = new L3vpnMcastRoutesIpv4Builder().setL3vpnMcastRoute(Collections.singletonList(ROUTE)).build();

    private static final L3vpnMcastDestination MCAST_L3VPN_DESTINATION = new L3vpnMcastDestinationBuilder()
            .setRouteDistinguisher(RD)
            .setPrefix(IPV4_PREFIX)
            .setPathId(PATH_ID)
            .build();
    private static final DestinationL3vpnMcastIpv4AdvertizedCase REACH_NLRI
            = new DestinationL3vpnMcastIpv4AdvertizedCaseBuilder()
            .setDestinationIpv4L3vpnMcast(new DestinationIpv4L3vpnMcastBuilder()
                    .setL3vpnMcastDestination(Collections.singletonList(MCAST_L3VPN_DESTINATION)).build()).build();
    private static final DestinationL3vpnMcastIpv4WithdrawnCase UNREACH_NLRI
            = new DestinationL3vpnMcastIpv4WithdrawnCaseBuilder()
            .setDestinationIpv4L3vpnMcast(new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp
                    .l3vpn.mcast.rev180417.update.attributes.mp.unreach.nlri.withdrawn.routes.destination.type
                    .destination.l3vpn.mcast.ipv4.withdrawn._case.DestinationIpv4L3vpnMcastBuilder()
                    .setL3vpnMcastDestination(Collections.singletonList(MCAST_L3VPN_DESTINATION)).build()).build();

    private L3VpnMcastIpv4RIBSupport ribSupport;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ribSupport = L3VpnMcastIpv4RIBSupport.getInstance(this.mappingService);
        setUpTestCustomizer(ribSupport);
    }

    @Test
    public void testDeleteRoutes() {
        final ContainerNode withdraw = createNlriWithDrawnRoute(UNREACH_NLRI);
        this.ribSupport.deleteRoutes(this.tx, getTablePath(), withdraw);
        final InstanceIdentifier<L3vpnMcastRoute> instanceIdentifier = this.deletedRoutes.get(0);
        assertEquals(ROUTE_KEY, instanceIdentifier.firstKeyOf(L3vpnMcastRoute.class));
    }

    @Test
    public void testPutRoutes() {
        this.ribSupport.putRoutes(this.tx, getTablePath(), createNlriAdvertiseRoute(REACH_NLRI), createAttributes());
        final L3vpnMcastRoute route = (L3vpnMcastRoute) this.insertedRoutes.get(0).getValue();
        assertEquals(ROUTE, route);
    }


    @Test
    public void testEmptyRoute() {
        Assert.assertEquals(createEmptyTable(), this.ribSupport.emptyTable());
    }

    @Test
    public void testBuildMpUnreachNlriUpdate() {
        final Collection<MapEntryNode> routes = createRoutes(MCAST_L3VPN_ROUTES);
        final Update update = this.ribSupport.buildUpdate(Collections.emptyList(), routes, ATTRIBUTES);
        assertEquals(UNREACH_NLRI, update.getAttributes().augmentation(Attributes2.class).getMpUnreachNlri()
                .getWithdrawnRoutes().getDestinationType());
        assertNull(update.getAttributes().augmentation(Attributes1.class));
    }

    @Test
    public void testBuildMpReachNlriUpdate() {
        final Collection<MapEntryNode> routes = createRoutes(MCAST_L3VPN_ROUTES);
        final Update update = this.ribSupport.buildUpdate(routes, Collections.emptyList(), ATTRIBUTES);
        assertEquals(REACH_NLRI, update.getAttributes().augmentation(Attributes1.class).getMpReachNlri()
                .getAdvertizedRoutes().getDestinationType());
        assertNull(update.getAttributes().augmentation(Attributes2.class));
    }

    @Test
    public void testCacheableNlriObjects() {
        Assert.assertEquals(ImmutableSet.of(L3vpnMcastRoutesIpv4Case.class), this.ribSupport.cacheableNlriObjects());
    }

    @Test
    public void testCacheableAttributeObjects() {
        Assert.assertEquals(ImmutableSet.of(), this.ribSupport.cacheableAttributeObjects());
    }

    @Test
    public void testRouteIdAddPath() {
        Assert.assertEquals(ROUTE_KEY, this.ribSupport.createRouteListKey(ROUTE_KEY.getPathId(),
            ROUTE_KEY.getRouteKey()));
    }

    @Test
    public void testRoutePath() {
        final YangInstanceIdentifier.NodeIdentifierWithPredicates prefixNii = createRouteNIWP(MCAST_L3VPN_ROUTES);
        final YangInstanceIdentifier expected = getRoutePath().node(prefixNii);
        final YangInstanceIdentifier actual = this.ribSupport.routePath(getTablePath(), prefixNii);
        assertEquals(expected, actual);
    }

    @Test
    public void testRouteAttributesIdentifier() {
        Assert.assertEquals(new YangInstanceIdentifier.NodeIdentifier(
                        Attributes.QNAME.withModule(BindingReflections.getQNameModule(L3vpnMcastRoutesIpv4Case.class))),
                this.ribSupport.routeAttributesIdentifier());
    }

    @Test
    public void testRoutesCaseClass() {
        Assert.assertEquals(L3vpnMcastRoutesIpv4Case.class, this.ribSupport.routesCaseClass());
    }

    @Test
    public void testRoutesContainerClass() {
        Assert.assertEquals(L3vpnMcastRoutesIpv4.class, this.ribSupport.routesContainerClass());
    }

    @Test
    public void testRoutesListClass() {
        Assert.assertEquals(L3vpnMcastRoute.class, this.ribSupport.routesListClass());
    }

    @Test
    public void testChangedRoutes() {
        final Routes emptyCase = new L3vpnMcastRoutesIpv4CaseBuilder().build();
        DataTreeCandidateNode tree = DataTreeCandidates.fromNormalizedNode(getRoutePath(),
                createRoutes(emptyCase)).getRootNode();
        assertTrue(this.ribSupport.changedRoutes(tree).isEmpty());

        final Routes emptyRoutes
                = new L3vpnMcastRoutesIpv4CaseBuilder()
                .setL3vpnMcastRoutesIpv4(new L3vpnMcastRoutesIpv4Builder().build()).build();
        tree = DataTreeCandidates.fromNormalizedNode(getRoutePath(), createRoutes(emptyRoutes)).getRootNode();
        assertTrue(this.ribSupport.changedRoutes(tree).isEmpty());

        final Routes routes = new L3vpnMcastRoutesIpv4CaseBuilder().setL3vpnMcastRoutesIpv4(MCAST_L3VPN_ROUTES).build();
        tree = DataTreeCandidates.fromNormalizedNode(getRoutePath(), createRoutes(routes)).getRootNode();
        final Collection<DataTreeCandidateNode> result = this.ribSupport.changedRoutes(tree);
        assertFalse(result.isEmpty());
    }
}
