/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.bgp.rib.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.opendaylight.protocol.util.CheckUtil.readDataOperational;
import static org.opendaylight.protocol.util.CheckUtil.waitFutureSuccess;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceRegistration;
import org.opendaylight.protocol.bgp.inet.RIBActivator;
import org.opendaylight.protocol.bgp.openconfig.spi.BGPTableTypeRegistryConsumer;
import org.opendaylight.protocol.bgp.parser.BGPError;
import org.opendaylight.protocol.bgp.parser.BgpTableTypeImpl;
import org.opendaylight.protocol.bgp.parser.impl.BGPActivator;
import org.opendaylight.protocol.bgp.parser.spi.BGPExtensionProviderContext;
import org.opendaylight.protocol.bgp.parser.spi.pojo.SimpleBGPExtensionProviderContext;
import org.opendaylight.protocol.bgp.rib.impl.config.BgpPeer;
import org.opendaylight.protocol.bgp.rib.impl.spi.BGPPeerRegistry;
import org.opendaylight.protocol.bgp.rib.impl.spi.BGPSessionPreferences;
import org.opendaylight.protocol.bgp.rib.spi.RIBExtensionProviderContext;
import org.opendaylight.protocol.bgp.rib.spi.SimpleRIBExtensionProviderContext;
import org.opendaylight.protocol.util.InetSocketAddressUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.AsNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.bgp.rib.rib.loc.rib.tables.routes.Ipv4RoutesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.ipv4.prefixes.DestinationIpv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.ipv4.prefixes.destination.ipv4.Ipv4PrefixesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.ipv4.routes.Ipv4Routes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.ipv4.routes.ipv4.routes.Ipv4Route;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.inet.rev180329.update.attributes.mp.reach.nlri.advertized.routes.destination.type.DestinationIpv4CaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.NotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.Open;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.OpenBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.PathId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.Update;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.UpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.open.message.BgpParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.AttributesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.attributes.AsPathBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.attributes.ClusterIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.attributes.LocalPrefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.attributes.MultiExitDiscBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.attributes.OriginBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.attributes.OriginatorIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.path.attributes.attributes.as.path.SegmentsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.message.rev180329.update.message.WithdrawnRoutesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.Attributes1;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.Attributes1Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.BgpTableType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.update.attributes.MpReachNlriBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.multiprotocol.rev180329.update.attributes.mp.reach.nlri.AdvertizedRoutesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev180329.BgpRib;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev180329.PeerRole;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.rib.rev180329.rib.TablesKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.BgpId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.BgpOrigin;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.ClusterIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.Ipv4AddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.Ipv6AddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.UnicastSubsequentAddressFamily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.next.hop.c.next.hop.Ipv4NextHopCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.bgp.types.rev180329.next.hop.c.next.hop.ipv4.next.hop._case.Ipv4NextHopBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.Notification;

public abstract class AbstractAddPathTest extends DefaultRibPoliciesMockTest {
    private static final int RETRY_TIMER = 10;
    static final String RIB_ID = "127.0.0.1";
    static final BgpId BGP_ID = new BgpId(RIB_ID);
    static final Ipv4Address PEER1 = new Ipv4Address("127.0.0.2");
    static final Ipv4Address PEER2 = new Ipv4Address("127.0.0.3");
    static final Ipv4Address PEER3 = new Ipv4Address("127.0.0.4");
    static final Ipv4Address PEER4 = new Ipv4Address("127.0.0.5");
    static final Ipv4Address PEER5 = new Ipv4Address("127.0.0.6");
    static final Ipv4Address PEER6 = new Ipv4Address("127.0.0.7");
    static final AsNumber AS_NUMBER = new AsNumber(AS);
    static final int PORT = InetSocketAddressUtil.getRandomPort();
    static final Ipv4Prefix PREFIX1 = new Ipv4Prefix("1.1.1.1/32");
    private static final ClusterIdentifier CLUSTER_ID = new ClusterIdentifier(RIB_ID);
    static final int HOLDTIMER = 2180;
    private static final Ipv4Address NH1 = new Ipv4Address("2.2.2.2");
    static final Update UPD_100 = createSimpleUpdate(PREFIX1, new PathId(1L), CLUSTER_ID, 100);
    static final Update UPD_50 = createSimpleUpdate(PREFIX1, new PathId(2L), CLUSTER_ID, 50);
    static final Update UPD_200 = createSimpleUpdate(PREFIX1, new PathId(3L), CLUSTER_ID, 200);
    static final Update UPD_20 = createSimpleUpdate(PREFIX1, new PathId(1L), CLUSTER_ID, 20);
    static final Update UPD_NA_100 = createSimpleUpdate(PREFIX1, null, CLUSTER_ID, 100);
    static final Update UPD_NA_100_EBGP = createSimpleUpdateEbgp(PREFIX1);
    static final Update UPD_NA_200 = createSimpleUpdate(PREFIX1, null, CLUSTER_ID, 200);
    static final Update UPD_NA_200_EBGP = createSimpleUpdateEbgp(PREFIX1);
    static final TablesKey TABLES_KEY = new TablesKey(Ipv4AddressFamily.class, UnicastSubsequentAddressFamily.class);
    static final List<BgpTableType> TABLES_TYPE = ImmutableList.of(new BgpTableTypeImpl(TABLES_KEY.getAfi(),
        TABLES_KEY.getSafi()));
    static final Set<TablesKey> AFI_SAFIS_ADVERTIZED = Collections.singleton(TABLES_KEY);
    private BGPExtensionProviderContext context;
    static final InstanceIdentifier<BgpRib> BGP_IID = InstanceIdentifier.create(BgpRib.class);
    static final int GRACEFUL_RESTART_TIME = 5;
    @Mock
    protected ClusterSingletonServiceProvider clusterSingletonServiceProvider;
    BGPDispatcherImpl serverDispatcher;
    RIBExtensionProviderContext ribExtension;
    private RIBActivator ribActivator;
    private BGPActivator bgpActivator;
    private NioEventLoopGroup worker;
    private NioEventLoopGroup boss;
    private org.opendaylight.protocol.bgp.inet.BGPActivator inetActivator;
    protected StrictBGPPeerRegistry serverRegistry;
    protected CodecsRegistryImpl codecsRegistry;

    private List<BGPDispatcherImpl> clientDispatchers;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.ribActivator = new RIBActivator();
        this.ribExtension = new SimpleRIBExtensionProviderContext();

        this.ribActivator.startRIBExtensionProvider(this.ribExtension, this.mappingService);

        this.bgpActivator = new BGPActivator();
        this.inetActivator = new org.opendaylight.protocol.bgp.inet.BGPActivator();
        this.context = new SimpleBGPExtensionProviderContext();
        this.bgpActivator.start(this.context);
        this.inetActivator.start(this.context);
        if (!Epoll.isAvailable()) {
            this.worker = new NioEventLoopGroup();
            this.boss = new NioEventLoopGroup();
        }
        this.serverRegistry = new StrictBGPPeerRegistry();
        this.serverDispatcher = new BGPDispatcherImpl(this.context.getMessageRegistry(), this.boss, this.worker,
            this.serverRegistry);
        doReturn(Mockito.mock(ClusterSingletonServiceRegistration.class)).when(this.clusterSingletonServiceProvider)
            .registerClusterSingletonService(any(ClusterSingletonService.class));

        this.codecsRegistry = CodecsRegistryImpl.create(this.mappingService.getCodecFactory(),
                this.ribExtension.getClassLoadingStrategy());
        this.clientDispatchers = new ArrayList<>();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        this.serverDispatcher.close();
        if (!Epoll.isAvailable()) {
            this.worker.shutdownGracefully(0, 0, TimeUnit.SECONDS);
            this.boss.shutdownGracefully(0, 0, TimeUnit.SECONDS);
        }
        this.mappingService.close();
        this.ribActivator.close();
        this.inetActivator.close();
        this.bgpActivator.close();
        this.clientDispatchers.forEach(BGPDispatcherImpl::close);
        this.clientDispatchers = null;
        super.tearDown();
    }

    void sendRouteAndCheckIsOnLocRib(final BGPSessionImpl session, final Ipv4Prefix prefix, final long localPreference,
        final int expectedRoutesOnDS) throws Exception {
        waitFutureSuccess(session.writeAndFlush(createSimpleUpdate(prefix, null, null, localPreference)));
        checkLocRib(expectedRoutesOnDS);
    }

    void sendWithdrawalRouteAndCheckIsOnLocRib(final BGPSessionImpl session, final Ipv4Prefix prefix,
        final long localPreference, final int expectedRoutesOnDS) throws Exception {
        waitFutureSuccess(session.writeAndFlush(createSimpleWithdrawalUpdate(prefix, localPreference)));
        checkLocRib(expectedRoutesOnDS);
    }

    void sendNotification(final BGPSessionImpl session) {
        final Notification notMsg = new NotifyBuilder().setErrorCode(BGPError.OPT_PARAM_NOT_SUPPORTED.getCode())
            .setErrorSubcode(BGPError.OPT_PARAM_NOT_SUPPORTED.getSubcode()).setData(new byte[] { 4, 9 }).build();
        waitFutureSuccess(session.writeAndFlush(notMsg));
    }

    void causeBGPError(final BGPSessionImpl session) {
        final Open openObj = new OpenBuilder().setBgpIdentifier(new Ipv4Address("1.1.1.1"))
            .setHoldTimer(50).setMyAsNumber(72).build();
        waitFutureSuccess(session.writeAndFlush(openObj));
    }

    private void checkLocRib(final int expectedRoutesOnDS) throws Exception {
        // FIXME: remove this sleep
        Thread.sleep(100);
        readDataOperational(getDataBroker(), BGP_IID, bgpRib -> {
            final Ipv4RoutesCase routes = (Ipv4RoutesCase) bgpRib.getRib().get(0).getLocRib().getTables().get(0)
                .getRoutes();
            final int size;
            if (routes != null) {
                final Ipv4Routes routesCase = routes.getIpv4Routes();
                if (routesCase != null) {
                    final List<Ipv4Route> routeList = routesCase.getIpv4Route();
                    size = routeList == null ? 0 : routeList.size();
                } else {
                    size = 0;
                }
            } else {
                size = 0;
            }

            Assert.assertEquals(expectedRoutesOnDS, size);
            return bgpRib;
        });
    }

    void checkPeersPresentOnDataStore(final int numberOfPeers) throws Exception {
        readDataOperational(getDataBroker(), BGP_IID, bgpRib -> {
            Assert.assertEquals(numberOfPeers, bgpRib.getRib().get(0).getPeer().size());
            return bgpRib;
        });
    }

    BGPSessionImpl createPeerSession(final Ipv4Address peer, final BgpParameters bgpParameters,
        final SimpleSessionListener sessionListener) throws InterruptedException {
        return createPeerSession(peer, bgpParameters, sessionListener, AS_NUMBER);
    }

    BGPSessionImpl createPeerSession(final Ipv4Address peer, final BgpParameters bgpParameters,
                                     final SimpleSessionListener sessionListener,
                                     final AsNumber remoteAsNumber) throws InterruptedException {
        final StrictBGPPeerRegistry clientRegistry = new StrictBGPPeerRegistry();
        final BGPDispatcherImpl clientDispatcher = new BGPDispatcherImpl(this.context.getMessageRegistry(), this.boss,
                this.worker, clientRegistry);

        clientDispatchers.add(clientDispatcher);
        clientRegistry.addPeer(new IpAddress(new Ipv4Address(RIB_ID)), sessionListener,
                new BGPSessionPreferences(remoteAsNumber, HOLDTIMER, new BgpId(peer),
                        AS_NUMBER, Lists.newArrayList(bgpParameters)));

        return connectPeer(peer, clientDispatcher);
    }

    static BGPPeer configurePeer(final BGPTableTypeRegistryConsumer tableRegistry, final Ipv4Address peerAddress,
            final RIBImpl ribImpl, final BgpParameters bgpParameters, final PeerRole peerRole,
            final BGPPeerRegistry bgpPeerRegistry) {
        return configurePeer(tableRegistry, peerAddress, ribImpl, bgpParameters, peerRole, bgpPeerRegistry,
                AFI_SAFIS_ADVERTIZED, Collections.emptySet());
    }

    static BGPPeer configurePeer(final BGPTableTypeRegistryConsumer tableRegistry,
            final Ipv4Address peerAddress, final RIBImpl ribImpl, final BgpParameters bgpParameters,
            final PeerRole peerRole, final BGPPeerRegistry bgpPeerRegistry, final Set<TablesKey> afiSafiAdvertised,
            final Set<TablesKey> gracefulAfiSafiAdvertised) {
        final BgpPeer bgpPeer = Mockito.mock(BgpPeer.class);
        doReturn(Optional.empty()).when(bgpPeer).getErrorHandling();
        return configurePeer(tableRegistry, peerAddress, ribImpl, bgpParameters, peerRole, bgpPeerRegistry,
                afiSafiAdvertised, gracefulAfiSafiAdvertised, Collections.emptyMap(), bgpPeer);
    }

    static BGPPeer configurePeer(final BGPTableTypeRegistryConsumer tableRegistry, final Ipv4Address peerAddress,
            final RIBImpl ribImpl, final BgpParameters bgpParameters, final PeerRole peerRole,
            final BGPPeerRegistry bgpPeerRegistry, final Set<TablesKey> afiSafiAdvertised,
            final Set<TablesKey> gracefulAfiSafiAdvertised, final Map<TablesKey, Integer> llGracefulTimersAdvertised,
            final BgpPeer peer) {
        final IpAddress ipAddress = new IpAddress(peerAddress);

        final BGPPeer bgpPeer = new BGPPeer(tableRegistry, new IpAddress(peerAddress), null, ribImpl, peerRole,
                null, null, null, afiSafiAdvertised, gracefulAfiSafiAdvertised, llGracefulTimersAdvertised, peer);
        final List<BgpParameters> tlvs = Lists.newArrayList(bgpParameters);
        bgpPeerRegistry.addPeer(ipAddress, bgpPeer,
                new BGPSessionPreferences(AS_NUMBER, HOLDTIMER, new BgpId(RIB_ID), AS_NUMBER, tlvs));
        bgpPeer.instantiateServiceInstance();
        return bgpPeer;
    }

    private static BGPSessionImpl connectPeer(final Ipv4Address localAddress, final BGPDispatcherImpl dispatcherImpl)
            throws InterruptedException {
        final Future<BGPSessionImpl> future = dispatcherImpl
                .createClient(new InetSocketAddress(localAddress.getValue(), PORT),
                        new InetSocketAddress(RIB_ID, PORT), RETRY_TIMER, true);
        Thread.sleep(200);
        waitFutureSuccess(future);
        Thread.sleep(100);
        return future.getNow();
    }

    static BgpParameters createParameter(final boolean addPath) {
        return createParameter(addPath, false, null);
    }

    static BgpParameters createParameter(final boolean addPath,
                                         final boolean addIpv6,
                                         final Map<TablesKey, Boolean> gracefulTables) {
        final TablesKey ipv4Key = new TablesKey(Ipv4AddressFamily.class, UnicastSubsequentAddressFamily.class);
        final List<TablesKey> advertisedTables = Lists.newArrayList(ipv4Key);
        if (addIpv6) {
            final TablesKey ipv6Key = new TablesKey(Ipv6AddressFamily.class, UnicastSubsequentAddressFamily.class);
            advertisedTables.add(ipv6Key);
        }
        final List<TablesKey> addPathTables = new ArrayList();
        if (addPath) {
            addPathTables.add(ipv4Key);
        }
        return PeerUtil.createBgpParameters(advertisedTables, addPathTables, gracefulTables, GRACEFUL_RESTART_TIME);
    }

    private static Update createSimpleUpdate(final Ipv4Prefix prefix, final PathId pathId,
            final ClusterIdentifier clusterId, final long localPreference) {
        final AttributesBuilder attBuilder = new AttributesBuilder();
        attBuilder.setLocalPref(new LocalPrefBuilder().setPref(localPreference).build());
        attBuilder.setOrigin(new OriginBuilder().setValue(BgpOrigin.Igp).build());
        attBuilder.setAsPath(new AsPathBuilder().setSegments(Collections.emptyList()).build());
        attBuilder.setMultiExitDisc(new MultiExitDiscBuilder().setMed(0L).build());
        if (clusterId != null) {
            attBuilder.setClusterId(new ClusterIdBuilder().setCluster(Collections.singletonList(clusterId)).build());
            attBuilder.setOriginatorId(new OriginatorIdBuilder().setOriginator(new Ipv4Address(clusterId)).build());
        }
        addAttributeAugmentation(attBuilder, prefix, pathId);
        return new UpdateBuilder().setAttributes(attBuilder.build()).build();
    }

    private static Update createSimpleUpdateEbgp(final Ipv4Prefix prefix) {
        final AttributesBuilder attBuilder = new AttributesBuilder();
        attBuilder.setOrigin(new OriginBuilder().setValue(BgpOrigin.Igp).build());
        attBuilder.setAsPath(new AsPathBuilder().setSegments(Collections.singletonList(
            new SegmentsBuilder().setAsSequence(Collections.singletonList(AS_NUMBER)).build())).build());
        addAttributeAugmentation(attBuilder, prefix, null);

        return new UpdateBuilder().setAttributes(attBuilder.build()).build();
    }

    private static void addAttributeAugmentation(final AttributesBuilder attBuilder, final Ipv4Prefix prefix,
        final PathId pathId) {
        attBuilder.setUnrecognizedAttributes(Collections.emptyList());
        attBuilder.addAugmentation(Attributes1.class,
            new Attributes1Builder().setMpReachNlri(
                new MpReachNlriBuilder()
                    .setCNextHop(new Ipv4NextHopCaseBuilder().setIpv4NextHop(new Ipv4NextHopBuilder().setGlobal(NH1)
                        .build()).build())
                    .setAfi(Ipv4AddressFamily.class)
                    .setSafi(UnicastSubsequentAddressFamily.class)
                    .setAdvertizedRoutes(new AdvertizedRoutesBuilder().setDestinationType(
                        new DestinationIpv4CaseBuilder().setDestinationIpv4(
                            new DestinationIpv4Builder().setIpv4Prefixes(Collections.singletonList(
                                new Ipv4PrefixesBuilder().setPathId(pathId).setPrefix(new Ipv4Prefix(prefix)).build()))
                                .build())
                            .build()).build())
                    .build()).build());
    }

    private static Update createSimpleWithdrawalUpdate(final Ipv4Prefix prefix, final long localPreference) {
        final AttributesBuilder attBuilder = new AttributesBuilder();
        attBuilder.setLocalPref(new LocalPrefBuilder().setPref(localPreference).build());
        attBuilder.setOrigin(new OriginBuilder().setValue(BgpOrigin.Igp).build());
        attBuilder.setAsPath(new AsPathBuilder().setSegments(Collections.emptyList()).build());
        attBuilder.setUnrecognizedAttributes(Collections.emptyList());
        return new UpdateBuilder()
                .setWithdrawnRoutes(Collections.singletonList(new WithdrawnRoutesBuilder().setPrefix(prefix).build()))
                .build();
    }
}
