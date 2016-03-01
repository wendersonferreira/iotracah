/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.caricah.iotracah.bootstrap.data.models.client;

import com.caricah.iotracah.bootstrap.security.realm.state.IOTClient;
import org.apache.ignite.cache.*;
import org.apache.ignite.cache.store.*;
import org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStoreFactory;
import org.apache.ignite.configuration.*;
import org.apache.ignite.lang.*;

import javax.cache.configuration.*;
import java.sql.*;
import java.util.*;

/**
 * CacheConfig definition.
 *
 * Code generated by Apache Ignite Schema Import utility: 02/24/2016.
 */
public class CacheConfig {
    /**
    * Configure cache.
    *  @param name Cache name.
    * @param storeFactory Cache store factory.
     */
    public static  CacheConfiguration<IotClientKey, IOTClient> cache(String name, CacheJdbcPojoStoreFactory<IotClientKey, IOTClient> storeFactory) {

        CacheConfiguration<IotClientKey, IOTClient> ccfg = new CacheConfiguration<>(name);

        if(Objects.nonNull(storeFactory)) {

            ccfg.setCacheStoreFactory(storeFactory);
        ccfg.setReadThrough(true);
        ccfg.setWriteThrough(true);
        ccfg.setWriteBehindEnabled(true);
    }
        // Configure cache types. 
        Collection<CacheTypeMetadata> meta = new ArrayList<>();

        // iot_client.
        CacheTypeMetadata type = new CacheTypeMetadata();

        meta.add(type);

        type.setDatabaseSchema("public");
        type.setDatabaseTable("iot_client");
        type.setKeyType(IotClientKey.class.getName());
        type.setValueType(IOTClient.class.getName());

        // Key fields for iot_client.
        Collection<CacheTypeFieldMetadata> keys = new ArrayList<>();
        keys.add(new CacheTypeFieldMetadata("session_id", Types.VARCHAR, "sessionId", String.class));
        type.setKeyFields(keys);

        // Value fields for iot_client.
        Collection<CacheTypeFieldMetadata> vals = new ArrayList<>();
        vals.add(new CacheTypeFieldMetadata("date_created", Types.TIMESTAMP, "dateCreated", java.sql.Timestamp.class));
        vals.add(new CacheTypeFieldMetadata("date_modified", Types.TIMESTAMP, "dateModified", java.sql.Timestamp.class));
        vals.add(new CacheTypeFieldMetadata("is_active", Types.BIT, "isActive", boolean.class));
        vals.add(new CacheTypeFieldMetadata("session_id", Types.VARCHAR, "sessionId", String.class));
        vals.add(new CacheTypeFieldMetadata("username", Types.VARCHAR, "username", String.class));
        vals.add(new CacheTypeFieldMetadata("client_identification", Types.VARCHAR, "clientIdentification", String.class));
        vals.add(new CacheTypeFieldMetadata("is_clean_session", Types.BIT, "isCleanSession", boolean.class));
        vals.add(new CacheTypeFieldMetadata("connection_id", Types.VARCHAR, "connectionId", String.class));
        vals.add(new CacheTypeFieldMetadata("connected_cluster", Types.VARCHAR, "connectedCluster", String.class));
        vals.add(new CacheTypeFieldMetadata("connected_node", Types.VARCHAR, "connectedNode", String.class));
        vals.add(new CacheTypeFieldMetadata("protocol", Types.VARCHAR, "protocol", String.class));
        vals.add(new CacheTypeFieldMetadata("protocol_data", Types.VARCHAR, "protocolData", String.class));
        vals.add(new CacheTypeFieldMetadata("auth_key", Types.VARCHAR, "authKey", String.class));
        vals.add(new CacheTypeFieldMetadata("timeout", Types.BIGINT, "timeout", long.class));
        vals.add(new CacheTypeFieldMetadata("start_timestamp", Types.TIMESTAMP, "startTimestamp", java.sql.Timestamp.class));
        vals.add(new CacheTypeFieldMetadata("stop_timestamp", Types.TIMESTAMP, "stopTimestamp", java.sql.Timestamp.class));
        vals.add(new CacheTypeFieldMetadata("last_access_time", Types.TIMESTAMP, "lastAccessTime", java.sql.Timestamp.class));
        vals.add(new CacheTypeFieldMetadata("expiry_timestamp", Types.TIMESTAMP, "expiryTimestamp", java.sql.Timestamp.class));
        vals.add(new CacheTypeFieldMetadata("is_expired", Types.BIT, "isExpired", boolean.class));
        vals.add(new CacheTypeFieldMetadata("host", Types.VARCHAR, "host", String.class));
        vals.add(new CacheTypeFieldMetadata("attributes", Types.VARCHAR, "attributes", String.class));
        vals.add(new CacheTypeFieldMetadata("partition_id", Types.VARCHAR, "partitionId", String.class));
        type.setValueFields(vals);

        // Query fields for iot_client.
        Map<String, Class<?>> qryFlds = new LinkedHashMap<>();

        qryFlds.put("dateCreated", java.sql.Timestamp.class);
        qryFlds.put("dateModified", java.sql.Timestamp.class);
        qryFlds.put("isActive", boolean.class);
        qryFlds.put("sessionId", String.class);
        qryFlds.put("username", String.class);
        qryFlds.put("clientIdentification", String.class);
        qryFlds.put("isCleanSession", boolean.class);
        qryFlds.put("connectionId", String.class);
        qryFlds.put("connectedCluster", String.class);
        qryFlds.put("connectedNode", String.class);
        qryFlds.put("protocol", String.class);
        qryFlds.put("protocolData", String.class);
        qryFlds.put("authKey", String.class);
        qryFlds.put("timeout", long.class);
        qryFlds.put("startTimestamp", java.sql.Timestamp.class);
        qryFlds.put("stopTimestamp", java.sql.Timestamp.class);
        qryFlds.put("lastAccessTime", java.sql.Timestamp.class);
        qryFlds.put("expiryTimestamp", java.sql.Timestamp.class);
        qryFlds.put("isExpired", boolean.class);
        qryFlds.put("host", String.class);
        qryFlds.put("attributes", String.class);
        qryFlds.put("partitionId", String.class);

        type.setQueryFields(qryFlds);

        // Ascending fields for iot_client.
        Map<String, Class<?>> ascFlds = new LinkedHashMap<>();

        ascFlds.put("sessionId", String.class);
        ascFlds.put("partitionId", String.class);

        type.setAscendingFields(ascFlds);

        // Groups for iot_client.
        Map<String, LinkedHashMap<String, IgniteBiTuple<Class<?>, Boolean>>> grps = new LinkedHashMap<>();

        LinkedHashMap<String, IgniteBiTuple<Class<?>, Boolean>> grpItems = new LinkedHashMap<>();

        grpItems.put("partitionId", new IgniteBiTuple<Class<?>, Boolean>(String.class, false));
        grpItems.put("clientIdentification", new IgniteBiTuple<Class<?>, Boolean>(String.class, false));

        grps.put("iot_client_partition_id_7898cde240153aa5_uniq", grpItems);

        type.setGroups(grps);

        ccfg.setTypeMetadata(meta);

        return ccfg;
    }
}