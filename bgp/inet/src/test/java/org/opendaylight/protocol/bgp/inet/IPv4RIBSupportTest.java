/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.protocol.bgp.inet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.protocol.bgp.rib.spi.AbstractRIBSupportTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.bgp.rib.rib.loc.rib.tables.routes.Ipv4RoutesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.bgp.rib.rib.loc.rib.tables.routes.Ipv4RoutesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.ipv4.prefixes.DestinationIpv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.ipv4.prefixes.destination.ipv4.Ipv4Prefixes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.ipv4.prefixes.destination.ipv4.Ipv4PrefixesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.ipv4.routes.Ipv4Routes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.ipv4.routes.Ipv4RoutesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.ipv4.routes.ipv4.routes.Ipv4Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.ipv4.routes.ipv4.routes.Ipv4RouteBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.ipv4.routes.ipv4.routes.Ipv4RouteKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.update.attributes.mp.reach.nlri.advertized.routes.destination.type.DestinationIpv4Case;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.update.attributes.mp.reach.nlri.advertized.routes.destination.type.DestinationIpv4CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.PathId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.Update;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.Attributes1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.Attributes2;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev180329.rib.tables.Routes;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidateNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidates;

public final class IPv4RIBSupportTest extends AbstractRIBSupportTest<Ipv4RoutesCase, Ipv4Routes, Ipv4Route,
        Ipv4RouteKey> {
    private IPv4RIBSupport ribSupport;
    private static final PathId PATH_ID = new PathId(1L);
    private static final Ipv4Prefix PREFIX = new Ipv4Prefix("1.2.3.4/32");
    private static final Ipv4RouteKey ROUTE_KEY = new Ipv4RouteKey(PATH_ID, PREFIX.getValue());
    private static final Ipv4Prefixes IPV4_PREFIXES = new Ipv4PrefixesBuilder().setPathId(PATH_ID)
            .setPrefix(PREFIX).build();
    private static final DestinationIpv4Case REACH_NLRI = new DestinationIpv4CaseBuilder()
            .setDestinationIpv4(new DestinationIpv4Builder()
                    .setIpv4Prefixes(Lists.newArrayList(IPV4_PREFIXES)).build()).build();

    private static final org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.update
            .attributes.mp.unreach.nlri.withdrawn.routes.destination.type.DestinationIpv4Case UNREACH_NLRI =
            new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.update
                    .attributes.mp.unreach.nlri.withdrawn.routes.destination.type.DestinationIpv4CaseBuilder()
                    .setDestinationIpv4(new DestinationIpv4Builder()
                            .setIpv4Prefixes(Collections.singletonList(IPV4_PREFIXES)).build()).build();

    private static final Ipv4Route ROUTE = new Ipv4RouteBuilder()
            .setRouteKey(PREFIX.getValue())
            .setAttributes(ATTRIBUTES)
            .setPathId(PATH_ID)
            .setPrefix(PREFIX).build();
    private static final Ipv4Routes ROUTES = new Ipv4RoutesBuilder()
            .setIpv4Route(Collections.singletonList(ROUTE)).build();
    private static final Ipv4Routes EMPTY_ROUTES = new Ipv4RoutesBuilder()
            .setIpv4Route(Collections.emptyList()).build();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.ribSupport = IPv4RIBSupport.getInstance(this.mappingService);
        setUpTestCustomizer(this.ribSupport);
    }

    @Test
    public void testDeleteRoutes() {
        this.ribSupport.deleteRoutes(this.tx, getTablePath(), createNlriWithDrawnRoute(UNREACH_NLRI));
        final InstanceIdentifier<Ipv4Route> instanceIdentifier = this.deletedRoutes.get(0);
        assertEquals(ROUTE_KEY, instanceIdentifier.firstKeyOf(Ipv4Route.class));
    }

    @Test
    public void testPutRoutes() {
        this.ribSupport.putRoutes(this.tx, getTablePath(), createNlriAdvertiseRoute(REACH_NLRI), createAttributes());
        final Ipv4Route route = (Ipv4Route) this.insertedRoutes.get(0).getValue();
        assertEquals(ROUTE, route);
    }


    @Test
    public void testEmptyRoute() {
        final Routes empty = new Ipv4RoutesCaseBuilder().setIpv4Routes(EMPTY_ROUTES).build();
        assertEquals(createEmptyTable(empty), this.ribSupport.emptyTable());
    }

    @Test
    public void testBuildMpUnreachNlriUpdate() {
        final Update update = this.ribSupport.buildUpdate(Collections.emptyList(), createRoutes(ROUTES), ATTRIBUTES);
        assertEquals(UNREACH_NLRI, update.getAttributes().augmentation(Attributes2.class).getMpUnreachNlri()
                .getWithdrawnRoutes().getDestinationType());
        assertNull(update.getAttributes().augmentation(Attributes1.class));
    }

    @Test
    public void testBuildMpReachNlriUpdate() {
        final Update update = this.ribSupport.buildUpdate(createRoutes(ROUTES), Collections.emptyList(), ATTRIBUTES);
        assertEquals(REACH_NLRI, update.getAttributes().augmentation(Attributes1.class).getMpReachNlri()
                .getAdvertizedRoutes().getDestinationType());
        assertNull(update.getAttributes().augmentation(Attributes2.class));
    }

    @Test
    public void testCacheableNlriObjects() {
        assertEquals(ImmutableSet.of(org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp
                .message.rev180329.Ipv4PrefixAndPathId.class), this.ribSupport.cacheableNlriObjects());
    }

    @Test
    public void testCacheableAttributeObjects() {
        assertEquals(ImmutableSet.of(), this.ribSupport.cacheableAttributeObjects());
    }

    @Test
    public void testRouteIdAddPath() {
        final Ipv4RouteKey expected = new Ipv4RouteKey(new PathId(1L), PREFIX.getValue());
        assertEquals(expected, this.ribSupport.createRouteListKey(expected.getPathId().getValue(), PREFIX.getValue()));
    }

    @Test
    public void testRoutePath() {
        final NodeIdentifierWithPredicates prefixNii = createRouteNIWP(ROUTES);
        assertEquals(getRoutePath().node(prefixNii), this.ribSupport.routePath(getTablePath()
                .node(Routes.QNAME), prefixNii));
    }

    @Test
    public void testRouteAttributesIdentifier() {
        assertEquals(new NodeIdentifier(QName.create(Ipv4Routes.QNAME, Attributes.QNAME.getLocalName().intern())),
                this.ribSupport.routeAttributesIdentifier());
    }

    @Test
    public void testRoutesCaseClass() {
        assertEquals(Ipv4RoutesCase.class, this.ribSupport.routesCaseClass());
    }

    @Test
    public void testRoutesContainerClass() {
        assertEquals(Ipv4Routes.class, this.ribSupport.routesContainerClass());
    }

    @Test
    public void testRoutesListClass() {
        assertEquals(Ipv4Route.class, this.ribSupport.routesListClass());
    }

    @Test
    public void testChangedRoutes() {
        final Routes emptyCase = new Ipv4RoutesCaseBuilder().build();
        DataTreeCandidateNode tree = DataTreeCandidates.fromNormalizedNode(getRoutePath(),
                createRoutes(emptyCase)).getRootNode();
        assertTrue(this.ribSupport.changedRoutes(tree).isEmpty());

        final Routes emptyRoutes = new Ipv4RoutesCaseBuilder().setIpv4Routes(new Ipv4RoutesBuilder().build()).build();
        tree = DataTreeCandidates.fromNormalizedNode(getRoutePath(), createRoutes(emptyRoutes)).getRootNode();
        assertTrue(this.ribSupport.changedRoutes(tree).isEmpty());

        final Routes routes = new Ipv4RoutesCaseBuilder().setIpv4Routes(ROUTES).build();
        tree = DataTreeCandidates.fromNormalizedNode(getRoutePath(), createRoutes(routes)).getRootNode();
        final Collection<DataTreeCandidateNode> result = this.ribSupport.changedRoutes(tree);
        Assert.assertFalse(result.isEmpty());
    }
}
