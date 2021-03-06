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

package com.caricah.iotracah.datastore.ignitecache.internal.impl;

import com.caricah.iotracah.bootstrap.data.messages.PublishMessage;
import com.caricah.iotracah.bootstrap.data.models.messages.CacheConfig;
import com.caricah.iotracah.bootstrap.data.models.messages.IotMessageKey;
import com.caricah.iotracah.bootstrap.exceptions.UnRetriableException;
import com.caricah.iotracah.datastore.ignitecache.internal.AbstractHandler;
import org.apache.commons.configuration.Configuration;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStoreFactory;
import org.apache.ignite.configuration.CacheConfiguration;
import rx.Observable;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:bwire@caricah.com"> Peter Bwire </a>
 * @version 1.0 9/20/15
 */
public class MessageHandler extends AbstractHandler<IotMessageKey, PublishMessage > {

    public static final String CONFIG_IGNITECACHE_MESSAGE_CACHE_NAME = "config.ignitecache.message.cache.name";
    public static final String CONFIG_IGNITECACHE_MESSAGE_CACHE_NAME_VALUE_DEFAULT = "iotracah_message_cache";

    @Override
    public void configure(Configuration configuration) {

        String cacheName = configuration.getString(CONFIG_IGNITECACHE_MESSAGE_CACHE_NAME, CONFIG_IGNITECACHE_MESSAGE_CACHE_NAME_VALUE_DEFAULT);
        setCacheName(cacheName);
    }

    @Override
    protected CacheConfiguration<IotMessageKey, PublishMessage> getCacheConfiguration(boolean persistanceEnabled, DataSource ds) {

        CacheJdbcPojoStoreFactory<IotMessageKey, PublishMessage> factory = null;

        if (persistanceEnabled){
            factory = new CacheJdbcPojoStoreFactory<>();
            factory.setDataSource(ds);
        }


        return CacheConfig.cache(getCacheName(), factory);

    }


    @Override
    public IotMessageKey keyFromModel(PublishMessage model) {

        IotMessageKey messageKey = new IotMessageKey();
        messageKey.setPartitionId(model.getPartitionId());
        messageKey.setClientId(model.getClientId());
        messageKey.setMessageId(model.getMessageId());

        return messageKey;
    }

    @Override
    public void initializeSequence(String nameOfSequence, Ignite ignite) {

        String queryForCount = "SELECT MAX(id) FROM PublishMessage";
        Object[] params = { };

        Long currentMax = getByQueryAsValue(Long.class, queryForCount, params).toBlocking().single();
        if(null == currentMax){
            currentMax = 0l;
        }

        IgniteAtomicSequence idSequence = ignite.atomicSequence(nameOfSequence, currentMax, true);
        setIdSequence(idSequence);
    }

    public Observable<Map.Entry<Long, IotMessageKey>> saveWithIdCheck(PublishMessage publishMessage) {

        return Observable.create(observer -> {
            try {


                if (publishMessage.getId() < 1) {
                    publishMessage.setId(getIdSequence().incrementAndGet());
                }


                if (PublishMessage.ID_TO_FORCE_GENERATION_ON_SAVE == publishMessage.getMessageId()) {

                    int messageId = getPartitionClientMessageId(
                            publishMessage.getSessionId(),
                            publishMessage.getIsInbound(),
                            publishMessage.getId()
                    );

                    //Implement max check.
                    publishMessage.setMessageId(messageId);
                }

                IotMessageKey messageKey = PublishMessage.createMessageKey(publishMessage);

                getDatastoreCache().put(messageKey, publishMessage);

                observer.onNext(new AbstractMap.SimpleEntry<>(publishMessage.getId(), messageKey));
                observer.onCompleted();

            } catch (UnRetriableException e) {
                log.error(" save : issues while saving item ", e);
                observer.onError(e);
            }

        });

    }



    private int getPartitionClientMessageId(String sessionId, boolean isInBound, long id) {

        String queryForCount = "SELECT " +
                "(SELECT MAX(messageId) FROM PublishMessage WHERE clientId = ? AND isInbound = ? AND id < ? ) " +
                "+ (SELECT COUNT(id) FROM PublishMessage WHERE clientId = ? AND isInbound = ? AND id <= ? ) " +
                " AS newId";
        Object[] params = {sessionId, isInBound, id, sessionId, isInBound, id };

        Long currentMax = getByQueryAsValue(Long.class, queryForCount, params).toBlocking().single();
        if (Objects.isNull(currentMax)) {
            return 1;
        }else{

            return currentMax.intValue();
        }
    }

    public void removeById(long oldMessageId) {




    }
}
