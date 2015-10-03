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

package com.caricah.iotracah.server.httpserver.netty;

import com.caricah.iotracah.server.netty.ServerHandler;
import com.caricah.iotracah.server.netty.ServerImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.mqtt.MqttMessage;

import java.io.Serializable;

/**
 * @author <a href="mailto:bwire@caricah.com"> Peter Bwire </a>
 * @version 1.0 10/1/15
 */
public class HttpServerHandler extends ServerHandler<FullHttpMessage> {


    public HttpServerHandler(HttpServerImpl serverImpl) {
        super(serverImpl);
    }

    /**
     * Is called for each message of type {@link MqttMessage}.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleChannelInboundHandler}
     *            belongs to
     * @param msg the message to handle
     * @throws Exception is thrown if an error occurred
     */
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpMessage msg) throws Exception {

        log.debug(" messageReceived : received the message {}", msg);

         Serializable connectionId = ctx.channel().attr(ServerImpl.REQUEST_CONNECTION_ID).get();

        getInternalServer().pushToWorker(connectionId, null, null, null, msg);

    }
}