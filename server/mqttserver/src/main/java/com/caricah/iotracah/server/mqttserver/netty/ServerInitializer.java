/*
 *
 * Copyright (c) 2015 Caricah <info@caricah.com>.
 *
 * Caricah licenses this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 *  of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 *  OF ANY  KIND, either express or implied.  See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 *
 *
 *
 */

package com.caricah.iotracah.server.mqttserver.netty;

import com.caricah.iotracah.core.modules.Server;
import com.caricah.iotracah.server.mqttserver.MqttServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.ssl.SslHandler;

/**
 * @author <a href="mailto:bwire@caricah.com"> Peter Bwire </a>
 * @version 1.0 5/27/15
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {


    private final int connectionTimeout;
    private final SSLHandler sslHandler;
    private final ServerImpl serverImpl;

    public ServerInitializer(ServerImpl serverImpl, int connectionTimeout, SSLHandler sslHandler) {
        this.serverImpl = serverImpl;
        this.sslHandler = sslHandler;
        this.connectionTimeout = connectionTimeout;
    }

    public ServerInitializer(ServerImpl serverImpl, int connectionTimeout) {
        this.serverImpl = serverImpl;
        this.sslHandler = null;
        this.connectionTimeout = connectionTimeout;
    }

    public ServerImpl getServerImpl() {
        return serverImpl;
    }

    public SSLHandler getSslHandler() {
        return sslHandler;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * This method will be called once the {@link SocketChannel} was registered. After the method returns this instance
     * will be removed from the {@link ChannelPipeline} of the {@link SocketChannel}.
     *
     * @param ch the {@link SocketChannel} which was registered.
     * @throws Exception is thrown if an error occurs. In that case the {@link SocketChannel} will be closed.
     */
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline pipeline = ch.pipeline();

        if(null != getSslHandler()) {
            // Add SSL handler first to encrypt and decrypt everything.
            // In this application ssl is only used for transport encryption
            // Identification is not yet part of the deal.

            pipeline.addLast("ssl", new SslHandler(getSslHandler().getSSLEngine()));
        }

        pipeline.addLast("decoder", new MqttDecoder());
        pipeline.addLast("encoder", new MqttEncoder());

        ServerHandler serverHandler = new ServerHandler(getServerImpl());
        // we finally have the chance to add some business logic.
        pipeline.addLast( serverHandler);

    }
}
