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
import com.caricah.iotracah.bootstrap.data.messages.ConnectAcknowledgeMessage;
import com.caricah.iotracah.bootstrap.data.messages.base.IOTMessage;
import com.caricah.iotracah.bootstrap.exceptions.UnRetriableException;
import com.caricah.iotracah.server.netty.SSLHandler;
import com.caricah.iotracah.server.netty.ServerImpl;
import com.caricah.iotracah.server.netty.ServerInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.mqtt.MqttConnectReturnCode;
import io.netty.handler.codec.mqtt.MqttMessage;
import org.apache.commons.configuration.Configuration;

import java.util.Objects;

/**
 * @author <a href="mailto:bwire@caricah.com"> Peter Bwire </a>
 * @version 1.0 9/28/15
 */
public class MqttServerImpl extends ServerImpl<MqttMessage> {


    public static final String CONFIGURATION_SERVER_MQTT_TCP_PORT = "system.internal.server.mqtt.tcp.port";
    public static final int CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_TCP_PORT = 1883;

    public static final String CONFIGURATION_SERVER_MQTT_SSL_PORT = "system.internal.server.mqtt.tcp.port";
    public static final int CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_SSL_PORT = 8883;

    public static final String CONFIGURATION_SERVER_MQTT_SSL_IS_ENABLED = "system.internal.server.mqtt.ssl.is.enabled";
    public static final boolean CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_SSL_IS_ENABLED = true;

    public static final String CONFIGURATION_SERVER_MQTT_CONNECTION_TIMEOUT = "system.internal.server.mqtt.connection.timeout";
    public static final int CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_CONNECTION_TIMEOUT = 10;


    public MqttServerImpl(Server<MqttMessage> internalServer) {
        super(internalServer);
    }

    /**
     * @param configuration Object carrying all configurable properties from file.
     * @throws UnRetriableException
     * @link configure method supplies the configuration object carrying all the
     * properties parsed from the external properties file.
     */

    public void configure(Configuration configuration) throws UnRetriableException {
        log.info(" configure : setting up our configurations.");

        int tcpPort = configuration.getInt(CONFIGURATION_SERVER_MQTT_TCP_PORT, CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_TCP_PORT);
        setTcpPort(tcpPort);

        int sslPort = configuration.getInt(CONFIGURATION_SERVER_MQTT_SSL_PORT, CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_SSL_PORT);
        setSslPort(sslPort);

        boolean sslEnabled = configuration.getBoolean(CONFIGURATION_SERVER_MQTT_SSL_IS_ENABLED, CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_SSL_IS_ENABLED);
        setSslEnabled(sslEnabled);

        if (isSslEnabled()) {

            setSslHandler(new SSLHandler(configuration));

        }

        int connectionTimeout = configuration.getInt(CONFIGURATION_SERVER_MQTT_CONNECTION_TIMEOUT, CONFIGURATION_VALUE_DEFAULT_SERVER_MQTT_CONNECTION_TIMEOUT);
        setConnectionTimeout(connectionTimeout);

    }


    @Override
    protected ServerInitializer<MqttMessage> getServerInitializer(ServerImpl<MqttMessage> serverImpl, int connectionTimeout) {
        return new MqttServerInitializer(serverImpl, connectionTimeout);
    }

    @Override
    protected ServerInitializer<MqttMessage> getServerInitializer(ServerImpl<MqttMessage> serverImpl, int connectionTimeout, SSLHandler sslHandler) {
        return new MqttServerInitializer(serverImpl, connectionTimeout, sslHandler);
    }

    @Override
    public void postProcess(IOTMessage ioTMessage) {

        switch (ioTMessage.getMessageType()) {
            case ConnectAcknowledgeMessage.MESSAGE_TYPE:

                ConnectAcknowledgeMessage conMessage = (ConnectAcknowledgeMessage) ioTMessage;


                /**
                 * Use the connection acknowledgement message to store session id for persistance.
                 */

                Channel channel = getChannel( ioTMessage.getConnectionId());
                if (Objects.nonNull( channel)) {

                    if (MqttConnectReturnCode.CONNECTION_ACCEPTED.equals(conMessage.getReturnCode())) {

                        channel.attr(ServerImpl.REQUEST_SESSION_ID).set(ioTMessage.getSessionId());
                    }else{
                        closeClient(ioTMessage.getConnectionId());
                    }
                }

                break;
            default:
                super.postProcess(ioTMessage);
        }


    }


}
