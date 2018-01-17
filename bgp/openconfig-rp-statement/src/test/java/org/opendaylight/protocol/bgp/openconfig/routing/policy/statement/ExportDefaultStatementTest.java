/*
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.openconfig.routing.policy.statement;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.opendaylight.protocol.bgp.openconfig.routing.policy.spi.registry.RouteAttributeContainer.routeAttributeContainerFalse;
import static org.opendaylight.protocol.bgp.openconfig.routing.policy.statement.ExportAttributeTestUtil.CLUSTER;
import static org.opendaylight.protocol.bgp.openconfig.routing.policy.statement.ExportAttributeTestUtil.IPV4;
import static org.opendaylight.protocol.bgp.openconfig.routing.policy.statement.ExportAttributeTestUtil.LOCAL_AS;
import static org.opendaylight.protocol.bgp.openconfig.routing.policy.statement.ExportAttributeTestUtil.createClusterInput;
import static org.opendaylight.protocol.bgp.openconfig.routing.policy.statement.ExportAttributeTestUtil.createInputWithOriginator;
import static org.opendaylight.protocol.bgp.openconfig.routing.policy.statement.ExportAttributeTestUtil.createPathInput;
import static org.opendaylight.protocol.bgp.openconfig.routing.policy.statement.ExportAttributeTestUtil.createPathInputWithAs;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.protocol.bgp.openconfig.routing.policy.impl.PolicyRIBBaseParametersImpl;
import org.opendaylight.protocol.bgp.openconfig.routing.policy.spi.registry.RouteAttributeContainer;
import org.opendaylight.protocol.bgp.rib.spi.policy.BGPRouteEntryExportParameters;
import org.opendaylight.yang.gen.v1.http.openconfig.net.yang.routing.policy.rev151009.routing.policy.top.routing.policy.policy.definitions.policy.definition.statements.Statement;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev171207.ipv4.routes.ipv4.routes.Ipv4Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev171207.path.attributes.Attributes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev171207.PeerRole;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

public class ExportDefaultStatementTest extends AbstractStatementRegistryConsumerTest {
    private static final YangInstanceIdentifier.NodeIdentifierWithPredicates ROUTE_ID_PA
            = new YangInstanceIdentifier.NodeIdentifierWithPredicates(Ipv4Route.QNAME,
            ImmutableMap.of(QName.create(Ipv4Route.QNAME, "prefix").intern(), "1.2.3.4/32"));
    @Mock
    private BGPRouteEntryExportParameters exportParameters;
    private List<Statement> defaultExportStatements;
    private PolicyRIBBaseParametersImpl baseAttributes;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.defaultExportStatements = loadStatement("default-odl-export-policy");
        doReturn(ROUTE_ID_PA).when(this.exportParameters).getRouteId();
        this.baseAttributes = new PolicyRIBBaseParametersImpl(LOCAL_AS, IPV4, CLUSTER);
    }

    /**
     * To eBGP.
     */
    @Test
    public void testToEbgp() {
        final Statement statement = getStatementAndSetToRole("to-external", PeerRole.Ebgp);

        final Attributes expectedOutput = createPathInputWithAs();
        final RouteAttributeContainer attributeContainer = routeAttributeContainerFalse(createPathInput(null));

        assertApplyExportStatement(statement, PeerRole.Ebgp, attributeContainer, expectedOutput);
        assertApplyExportStatement(statement, PeerRole.Ibgp, attributeContainer, expectedOutput);
        assertApplyExportStatement(statement, PeerRole.Internal, attributeContainer, expectedOutput);
        assertApplyExportStatement(statement, PeerRole.RrClient, attributeContainer, expectedOutput);
    }


    /**
     * From iBGP To iBGP.
     */
    @Test
    public void testFromInternalToInternal() {
        final Statement statement = getStatementAndSetToRole("from-internal-to-internal", PeerRole.Ibgp);
        final RouteAttributeContainer attributeContainer = routeAttributeContainerFalse(createClusterInput());
        assertApplyExportStatement(statement, PeerRole.Ibgp, attributeContainer, null);
    }

    /**
     * From iBGP To iBGP.
     */
    @Test
    public void testFromExternalToInternal() {
        final Statement statement = getStatementAndSetToRole("from-external-to-internal", PeerRole.Ibgp);
        final RouteAttributeContainer attributeContainer = routeAttributeContainerFalse(createClusterInput());
        assertApplyExportStatement(statement, PeerRole.Ebgp, attributeContainer, attributeContainer.getAttributes());
    }

    /**
     * From Internal To iBGP.
     */
    @Test
    public void testFromOdlInternalToInternal() {
        final Statement statement = getStatementAndSetToRole("from-odl-internal-to-internal-or-rr-client",
                PeerRole.Ibgp);
        final RouteAttributeContainer attributeContainer = routeAttributeContainerFalse(createClusterInput());
        assertApplyExportStatement(statement, PeerRole.Internal, attributeContainer, createClusterInput());
    }

    /**
     * From RR-Client To iBGP.
     */
    @Test
    public void testFromRRclientToInternal() {
        final Statement statement = getStatementAndSetToRole("from-rr-client-to-internal", PeerRole.Ibgp);
        final RouteAttributeContainer attributeContainer = routeAttributeContainerFalse(createClusterInput());
        assertApplyExportStatement(statement, PeerRole.RrClient, attributeContainer, createInputWithOriginator());
    }

    /**
     * Any role To Internal.
     */
    @Test
    public void testOdlInternal() {
        final Statement statement = getStatementAndSetToRole("to-odl-internal", PeerRole.Internal);
        final RouteAttributeContainer attributeContainer = routeAttributeContainerFalse(createClusterInput());

        assertApplyExportStatement(statement, PeerRole.Ebgp, attributeContainer, null);
        assertApplyExportStatement(statement, PeerRole.Ibgp, attributeContainer, null);
        assertApplyExportStatement(statement, PeerRole.Internal, attributeContainer, null);
        assertApplyExportStatement(statement, PeerRole.RrClient, attributeContainer, null);
    }

    /**
     * To RrClient.
     */
    @Test
    public void testFromExternalToRRClient() {
        final Statement statement = getStatementAndSetToRole("from-external-to-route-reflector", PeerRole.RrClient);
        final RouteAttributeContainer attributeContainer = routeAttributeContainerFalse(createClusterInput());
        assertApplyExportStatement(statement, PeerRole.Ebgp, attributeContainer, attributeContainer.getAttributes());
    }

    /**
     * To RrClient.
     */
    @Test
    public void testFromInternalOrRRClientToRRClient() {
        final Statement statement = getStatementAndSetToRole("from-internal-or-rr-client-to-route-reflector",
                PeerRole.RrClient);
        final Attributes expectedOutput = createInputWithOriginator();
        final RouteAttributeContainer attributeContainer = routeAttributeContainerFalse(createClusterInput());

        assertApplyExportStatement(statement, PeerRole.Ibgp, attributeContainer, expectedOutput);
        assertApplyExportStatement(statement, PeerRole.RrClient, attributeContainer, expectedOutput);
    }

    /**
     * To RrClient.
     */
    @Test
    public void testFromOdlInternalRRClient() {
        final Statement statement = getStatementAndSetToRole("from-odl-internal-to-internal-or-rr-client",
                PeerRole.RrClient);
        final RouteAttributeContainer attributeContainer = routeAttributeContainerFalse(createClusterInput());
        assertApplyExportStatement(statement, PeerRole.Internal, attributeContainer, createClusterInput());
    }

    private Statement getStatementAndSetToRole(final String statementName, final PeerRole toPeerRole) {
        doReturn(toPeerRole).when(this.exportParameters).getToPeerRole();
        return this.defaultExportStatements.stream()
                .filter(st -> st.getName().equals(statementName)).findFirst().get();
    }

    private void assertApplyExportStatement(
            final Statement statement, final PeerRole fromPeerRole,
            final RouteAttributeContainer attInput,
            final Attributes attExpected) {

        doReturn(fromPeerRole).when(this.exportParameters).getFromPeerRole();

        RouteAttributeContainer result = this.statementRegistry.applyExportStatement(
                this.baseAttributes,
                this.exportParameters,
                attInput,
                statement);
        assertEquals(attExpected, result.getAttributes());
    }
}