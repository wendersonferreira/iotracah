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

package com.caricah.iotracah.datastore.ignitecache;

import com.caricah.iotracah.core.modules.Datastore;
import com.caricah.iotracah.core.worker.state.messages.PublishMessage;
import com.caricah.iotracah.core.worker.state.messages.WillMessage;
import com.caricah.iotracah.core.worker.state.models.Client;
import com.caricah.iotracah.core.worker.state.models.Subscription;
import com.caricah.iotracah.datastore.ignitecache.internal.impl.*;
import com.caricah.iotracah.exceptions.UnRetriableException;
import com.caricah.iotracah.security.realm.state.IOTAccount;
import com.caricah.iotracah.security.realm.state.IOTRole;
import org.apache.commons.configuration.Configuration;
import org.apache.ignite.IgniteAtomicSequence;
import rx.Observable;
import rx.Subscriber;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author <a href="mailto:bwire@caricah.com"> Peter Bwire </a>
 * @version 1.0 8/15/15
 */
public class IgniteDatastore extends Datastore{

    private IgniteAtomicSequence clientIdSequence;

    private final ClientHandler clientHandler = new ClientHandler();

    private final WillHandler willHandler = new WillHandler();

    private final SubscriptionHandler subscriptionHandler = new SubscriptionHandler();

    private final MessageHandler messageHandler = new MessageHandler();

    private final AccountHandler accountHandler = new AccountHandler();

    private final RoleHandler roleHandler = new RoleHandler();

    public IgniteAtomicSequence getClientIdSequence() {
        return clientIdSequence;
    }

    public void setClientIdSequence(IgniteAtomicSequence clientIdSequence) {
        this.clientIdSequence = clientIdSequence;
    }

    /**
     * <code>configure</code> allows the base system to configure itself by getting
     * all the settings it requires and storing them internally. The plugin is only expected to
     * pick the settings it has registered on the configuration file for its particular use.
     *
     * @param configuration
     * @throws UnRetriableException
     */
    @Override
    public void configure(Configuration configuration) throws UnRetriableException {

        clientHandler.configure(configuration);

        subscriptionHandler.configure(configuration);

        messageHandler.configure(configuration);

        willHandler.configure(configuration);

        accountHandler.configure(configuration);

        roleHandler.configure(configuration);
    }

    /**
     * <code>initiate</code> starts the operations of this system handler.
     * All excecution code for the plugins is expected to begin at this point.
     *
     * @throws UnRetriableException
     */
    @Override
    public void initiate() throws UnRetriableException {

        long currentTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        String nameOfSequenceForClientId = "iotracah-sequence-client-id";
        IgniteAtomicSequence seq = getIgnite().atomicSequence(nameOfSequenceForClientId, currentTime, true);
        setClientIdSequence(seq);

        clientHandler.initiate(Client.class, getIgnite());
        subscriptionHandler.initiate(Subscription.class, getIgnite());
        messageHandler.initiate(PublishMessage.class, getIgnite());
        willHandler.initiate(WillMessage.class, getIgnite());
        accountHandler.initiate(IOTAccount.class, getIgnite());
        roleHandler.initiate(IOTRole.class, getIgnite());

    }

    /**
     * <code>terminate</code> halts excecution of this plugin.
     * This provides a clean way to exit /stop operations of this particular plugin.
     */
    @Override
    public void terminate() {

    }

    @Override
    public Observable<Client> getClient(String partition, String clientIdentifier) {

        String query = "partition = ? and clientId = ?";
        Object[] params = {partition, clientIdentifier};

       return clientHandler.getByQuery(Client.class, query, params );
    }

    @Override
    public void saveClient(Client client) {
        clientHandler.save(client);
    }

    @Override
    public void removeClient(Client client) {
        clientHandler.remove(client);
    }

    @Override
    public Observable<WillMessage> getWill(String partition, String clientIdentifier) {

        String query = "partition = ? and clientId = ?";
        Object[] params = {partition, clientIdentifier};

        return willHandler.getByQuery(WillMessage.class, query, params);
    }

    @Override
    public void saveWill(WillMessage will) {
        willHandler.save(will);
    }

    @Override
    public void removeWill(WillMessage will) {
        willHandler.remove(will);
    }

    @Override
    public Observable<Subscription> getSubscription(String partition, String partitionQosTopicFilter, Subscription defaultSubscription) {
       return subscriptionHandler.getByKeyWithDefault(partitionQosTopicFilter, defaultSubscription);
    }

    @Override
    public Observable<Subscription> getSubscription(String partition, String partitionQosTopicFilter) {
        return subscriptionHandler.getByKey(partitionQosTopicFilter);
    }

    @Override
    public void saveSubscription(Subscription subscription) {
        subscriptionHandler.save(subscription);
    }

    @Override
    public void removeSubscription(Subscription subscription) {
        subscriptionHandler.remove(subscription);
    }

    @Override
    public Observable<String> distributePublish(String partition, Set<String> topicBreakDown, PublishMessage publishMessage) {

        return Observable.create(observer -> {

                    log.debug(" distributePublish : obtaining subscribers for topic {}", topicBreakDown);

                try {

                    CountDownLatch countDownLatch = new CountDownLatch(topicBreakDown.size());

                    for (String topicFilter : topicBreakDown) {

                        Observable<Subscription> subscriptionObservable = getSubscription(publishMessage.getPartition(), topicFilter);
                        subscriptionObservable.subscribe(

                                new Subscriber<Subscription>() {
                                    @Override
                                    public void onCompleted() {
                                        countDownLatch.countDown();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        countDownLatch.countDown();
                                    }

                                    @Override
                                    public void onNext(Subscription subscription) {

                                        log.debug( " distributePublish onNext : obtained a subscription {} for message {}", subscription, publishMessage);
                                        subscription.getSubscriptions().forEach(observer::onNext);

                                    }
                                }
                        );


                    }
                    //Wait for all subscribers to be dealt with.
                    countDownLatch.await();
                    observer.onCompleted();

                } catch (Exception e) {
                    observer.onError(e);
                }

        });

    }

    @Override
    public Observable<PublishMessage> getActiveMessages(Client client) {

        String query = "partition = ? and clientId = ?";
        Object[] params = {client.getPartition(), client.getClientId()};

        return messageHandler.getByQuery(PublishMessage.class, query, params);
    }

    @Override
    public Observable<PublishMessage> getMessage(String partition, String clientIdentifier, long messageId, boolean isInbound) {

        String query = "partition = ? and clientId = ? and messageId = ? and inBound = ?";
        Object[] params = {partition, clientIdentifier, messageId, isInbound};

        return messageHandler.getByQuery(PublishMessage.class, query, params);
    }

    @Override
    public Observable<Long> saveMessage(PublishMessage publishMessage) {

        messageHandler.save(publishMessage);

        return Observable.create(observer -> {
            // callback with value
            observer.onNext(publishMessage.getMessageId());
            observer.onCompleted();


        });

    }

    @Override
    public void removeMessage(PublishMessage publishMessage) {
        messageHandler.remove(publishMessage);
    }

    @Override
    public String nextClientId() {
        long nextSequence = getClientIdSequence().incrementAndGet();
        return String.format("iotracah-cl-id-%d", nextSequence);
    }


    @Override
    public IOTAccount getIOTAccount(String partition, String username) {

        String cacheKey = IOTAccount.createCacheKey(partition, username);

         return accountHandler.getByKeyWithDefault(cacheKey, null).toBlocking().single();
    }

    @Override
    public void saveIOTAccount(IOTAccount account) {

        accountHandler.save(account);

    }

    @Override
    public IOTRole getIOTRole(String partition, String rolename) {

        String cacheKey = IOTRole.createCacheKey(partition, rolename);

        return roleHandler.getByKeyWithDefault(cacheKey,null).toBlocking().single();
    }

    @Override
    public void saveIOTRole(IOTRole iotRole) {
        roleHandler.save(iotRole);
    }
}