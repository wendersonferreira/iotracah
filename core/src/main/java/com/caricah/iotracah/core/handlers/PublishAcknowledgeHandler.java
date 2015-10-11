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


import com.caricah.iotracah.core.security.AuthorityRole;
import com.caricah.iotracah.core.worker.state.messages.AcknowledgeMessage;
import com.caricah.iotracah.core.worker.state.messages.PublishMessage;
import com.caricah.iotracah.core.worker.state.models.Client;
import com.caricah.iotracah.exceptions.RetriableException;
import com.caricah.iotracah.exceptions.UnRetriableException;
import rx.Observable;

/**
 * @author <a href="mailto:bwire@caricah.com"> Peter Bwire </a>
 */
public class PublishAcknowledgeHandler extends RequestHandler<AcknowledgeMessage> {

    public PublishAcknowledgeHandler(AcknowledgeMessage message) {
        super(message);
    }

    @Override
    public void handle() throws RetriableException, UnRetriableException {

        //Check for connect permissions
        Observable<Client> permissionObservable = checkPermission(getMessage().getSessionId(),
                getMessage().getAuthKey(), AuthorityRole.CONNECT);

        permissionObservable.subscribe(

                (client) -> {

                    //Handle acknowledging of message.

                    Observable<PublishMessage> messageObservable = getDatastore().getMessage(
                client.getPartition(), client.getClientId(),
                getMessage().getMessageId(), false);

        messageObservable.subscribe(getDatastore()::removeMessage);

                }, this::disconnectDueToError);

    }
}
