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
package org.onosproject.ovsdb.controller.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.onlab.packet.IpAddress;
import org.onosproject.ovsdb.controller.OvsdbConstant;
import org.onosproject.ovsdb.controller.OvsdbNodeId;
import org.onosproject.ovsdb.controller.driver.DefaultOvsdbClient;
import org.onosproject.ovsdb.controller.driver.OvsdbAgent;
import org.onosproject.ovsdb.controller.driver.OvsdbProviderService;
import org.onosproject.ovsdb.rfc.jsonrpc.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main controller class. Handles all setup and network listeners -
 * Distributed ovsdbClient.
 */
public class Controller {
    protected static final Logger log = LoggerFactory
            .getLogger(Controller.class);

    private int ovsdbPort = OvsdbConstant.OVSDBPORT;

    private OvsdbAgent agent;
    private Callback monitorCallback;

    private final ExecutorService executorService = Executors
            .newFixedThreadPool(10);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Class<? extends ServerChannel> serverChannelClass;

    /**
     * Initialization.
     */
    private void initEventLoopGroup() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        serverChannelClass = NioServerSocketChannel.class;
    }

    /**
     * Accepts incoming connections.
     */
    private void startAcceptingConnections() throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();

        b.group(bossGroup, workerGroup).channel(serverChannelClass)
                .childHandler(new OnosCommunicationChannelInitializer());
        b.option(ChannelOption.SO_BACKLOG, 128);
        b.option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 32 * 1024);
        b.option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, 8 * 1024);
        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.childOption(ChannelOption.SO_KEEPALIVE, true);
        b.bind(ovsdbPort).sync();
    }

    /**
     * Tells controller that we're ready to accept ovsdb node loop.
     */
    public void run() throws InterruptedException {
        initEventLoopGroup();
        startAcceptingConnections();
    }

    /**
     * Adds channel pipiline to handle a new connected node.
     */
    private class OnosCommunicationChannelInitializer
            extends ChannelInitializer<SocketChannel> {
        protected void initChannel(SocketChannel channel) throws Exception {
            log.info("New channel created");
            channel.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));
            channel.pipeline().addLast(new MessageDecoder());
            handleNewNodeConnection(channel);

        }
    }

    /**
     * Handles the new connection of a node.
     *
     * @param channel the channel to use.
     */
    private void handleNewNodeConnection(final Channel channel) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                log.info("Handle new node connection");

                IpAddress ipAddress = IpAddress
                        .valueOf(((InetSocketAddress) channel.remoteAddress())
                                .getAddress().getHostAddress());
                long port = ((InetSocketAddress) channel.remoteAddress())
                        .getPort();

                log.info("Get connection from ip address {} : {}",
                         ipAddress.toString(), port);

                OvsdbNodeId nodeId = new OvsdbNodeId(ipAddress, port);
                OvsdbProviderService ovsdbProviderService = getNodeInstance(nodeId,
                                                                            agent,
                                                                            monitorCallback,
                                                                            channel);
                ovsdbProviderService.setConnection(true);
                OvsdbJsonRpcHandler ovsdbJsonRpcHandler = new OvsdbJsonRpcHandler(
                                                                                  nodeId);
                ovsdbJsonRpcHandler
                        .setOvsdbProviderService(ovsdbProviderService);
                channel.pipeline().addLast(ovsdbJsonRpcHandler);

                ovsdbProviderService.nodeAdded();
                ChannelFuture closeFuture = channel.closeFuture();
                closeFuture
                        .addListener(new ChannelConnectionListener(
                                                                   ovsdbProviderService));
            }
        });
    }

    /**
     * Gets an ovsdb client instance.
     *
     * @param nodeId data ovsdb node id
     * @param agent OvsdbAgent
     * @param monitorCallback Callback
     * @param channel Channel
     * @return OvsdbProviderService instance
     */
    protected OvsdbProviderService getNodeInstance(OvsdbNodeId nodeId,
                                                   OvsdbAgent agent,
                                                   Callback monitorCallback,
                                                   Channel channel) {
        OvsdbProviderService ovsdbProviderService = new DefaultOvsdbClient(
                                                                           nodeId);
        ovsdbProviderService.setAgent(agent);
        ovsdbProviderService.setCallback(monitorCallback);
        ovsdbProviderService.setChannel(channel);
        return ovsdbProviderService;
    }

    /**
     * Starts controller.
     *
     * @param agent OvsdbAgent
     * @param monitorCallback Callback
     */
    public void start(OvsdbAgent agent, Callback monitorCallback) {
        this.agent = agent;
        this.monitorCallback = monitorCallback;
        try {
            this.run();
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting to start");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Stops controller.
     *
     */
    public void stop() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
