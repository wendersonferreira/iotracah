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

package com.caricah.iotracah.core.handlers;

import com.caricah.iotracah.bootstrap.data.models.messages.IotMessageKey;
import com.caricah.iotracah.bootstrap.security.realm.state.IOTClient;
import com.caricah.iotracah.core.security.AuthorityRole;
import com.caricah.iotracah.core.worker.exceptions.ShutdownException;
import com.caricah.iotracah.core.worker.state.Constant;
import com.caricah.iotracah.bootstrap.data.messages.AcknowledgeMessage;
import com.caricah.iotracah.bootstrap.data.messages.PublishMessage;
import com.caricah.iotracah.bootstrap.data.messages.PublishReceivedMessage;
import com.caricah.iotracah.bootstrap.exceptions.RetriableException;
import com.caricah.iotracah.bootstrap.exceptions.UnRetriableException;
import io.netty.handler.codec.mqtt.MqttQoS;
import rx.Observable;

import java.util.Map;

/**
 * @author <a href="mailto:bwire@caricah.com"> Peter Bwire </a>
 */
public class PublishInHandler extends RequestHandler<PublishMessage> {


    /**
     * A PUBLISH Control Packet is sent from a Client to a Server or from Server to a Client
     * to transport an Application Message.
     *
     * @throws RetriableException
     * @throws UnRetriableException
     */
    @Override
    public void handle(PublishMessage publishMessage) throws RetriableException, UnRetriableException {

        log.debug(" handle : client attempting to publish a message.");


        /**
         * During an attempt to publish a message.
         *
         * All Topic Names and Topic Filters MUST be at least one character long [MQTT-4.7.3-1]
         *
         * The wildcard characters can be used in Topic Filters,
         * but MUST NOT be used within a Topic Name [MQTT-4.7.1-1].
         *
         * Topic Names and Topic Filters MUST NOT include the null character (Unicode U+0000) [Unicode] [MQTT-4.7.3-2]
         *
         * Topic Names and Topic Filters are UTF-8 encoded strings, they MUST NOT encode to more than 65535 bytes [MQTT-4.7.3-3]. See Section 1.5.3
         *
         */
        String topic = publishMessage.getTopic();
        if (null == topic ||
                topic.isEmpty() ||
                topic.contains(Constant.MULTI_LEVEL_WILDCARD) ||
                topic.contains(Constant.SINGLE_LEVEL_WILDCARD) ||
                topic.contains(Constant.SYS_PREFIX)
                ) {
            log.info(" handle : Invalid topic " + publishMessage.getTopic());
            throw new ShutdownException(" Invalid topic name");
        }


        /**
         * Before publishing we should get the current session and validate it.
         */
        Observable<IOTClient> permissionObservable = checkPermission(
                publishMessage.getSessionId(), publishMessage.getAuthKey(),
                AuthorityRole.PUBLISH, topic);

        permissionObservable.subscribe(
                (iotSession) -> {

                    try {

                        publishMessage.setPartitionId(iotSession.getPartitionId());
                        publishMessage.setClientId(iotSession.getSessionId());
                        publishMessage.setId(-1);

                        /**
                         * Message processing is based on 4.3 Quality of Service levels and protocol flows
                         */

                        /**
                         *  4.3.1 QoS 0: At most once delivery
                         *  Accepts ownership of the message when it receives the PUBLISH packet.
                         */
                        if (MqttQoS.AT_MOST_ONCE.value() == publishMessage.getQos()) {

                            getMessenger().publish(iotSession.getPartitionId(), publishMessage);
                        }


                        /**
                         * 4.3.2 QoS 1: At least once delivery
                         *
                         * MUST respond with a PUBACK Packet containing the Packet Identifier from the incoming PUBLISH Packet, having accepted ownership of the Application Message
                         * After it has sent a PUBACK Packet the Receiver MUST treat any incoming PUBLISH packet that contains the same Packet Identifier as being a new publication, irrespective of the setting of its DUP flag.
                         */
                        if (MqttQoS.AT_LEAST_ONCE.value() == publishMessage.getQos()) {

                            getMessenger().publish(iotSession.getPartitionId(), publishMessage);

                            //We need to generate a puback message to close this conversation.

                            AcknowledgeMessage acknowledgeMessage = AcknowledgeMessage.from(
                                    publishMessage.getMessageId());
                            acknowledgeMessage.copyTransmissionData(publishMessage);

                            pushToServer(acknowledgeMessage);


                        }


                        /**
                         * 4.3.3 QoS 2: Exactly once delivery
                         *
                         * MUST respond with a PUBREC containing the Packet Identifier from the incoming PUBLISH Packet, having accepted ownership of the Application Message.
                         * Until it has received the corresponding PUBREL packet, the Receiver MUST acknowledge any subsequent PUBLISH packet with the same Packet Identifier by sending a PUBREC.
                         * It MUST NOT cause duplicate messages to be delivered to any onward recipients in this case.
                         *
                         */

                        if (MqttQoS.EXACTLY_ONCE.value() == publishMessage.getQos()) {


                            queueQos2Message(publishMessage);
                        }

                    } catch (UnRetriableException | RetriableException e) {
                        disconnectDueToError(e, publishMessage);
                    }

                }, throwable -> disconnectDueToError(throwable, publishMessage)

        );

    }


    private void queueQos2Message(PublishMessage message) throws UnRetriableException {
        if (MqttQoS.EXACTLY_ONCE.value() == message.getQos()) {
            //This message needs to be retained
            // while handshake is completed before being released.

            message.setIsRelease(false);
            Observable<Map.Entry<Long, IotMessageKey>> messageIdObservable = getDatastore().saveMessage(message);

            messageIdObservable.subscribe(messageIdentity -> {

                //We need to push out a PUBREC

                PublishReceivedMessage publishReceivedMessage = PublishReceivedMessage.from(messageIdentity.getValue().getMessageId());
                publishReceivedMessage.copyTransmissionData(message);
                pushToServer(publishReceivedMessage);

            });


        } else

            throw new UnRetriableException("Only qos 2 messages should be Queued for handshake to occur.");
    }


}
