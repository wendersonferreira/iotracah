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

package com.caricah.iotracah.bootstrap.data.models.subscriptionfilters;

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
 * Code generated by Apache Ignite Schema Import utility: 02/25/2016.
 */
public class CacheConfig {
    /**
    * Configure cache.
    *  @param name Cache name.
    * @param storeFactory Cache store factory.
     */
    public static <K, V> CacheConfiguration<IotSubscriptionFilterKey, IotSubscriptionFilter> cache(String name, CacheJdbcPojoStoreFactory<IotSubscriptionFilterKey, IotSubscriptionFilter> storeFactory) {

        CacheConfiguration<IotSubscriptionFilterKey, IotSubscriptionFilter> ccfg = new CacheConfiguration<>(name);

        if(Objects.nonNull(storeFactory)) {

            ccfg.setCacheStoreFactory(storeFactory);
        ccfg.setReadThrough(true);
        ccfg.setWriteThrough(true);
        ccfg.setWriteBehindEnabled(true);
    }

        // Configure cache types. 
        Collection<CacheTypeMetadata> meta = new ArrayList<>();

        // iot_subscription_filter.
        CacheTypeMetadata type = new CacheTypeMetadata();

        meta.add(type);

        type.setDatabaseSchema("public");
        type.setDatabaseTable("iot_subscription_filter");
        type.setKeyType(IotSubscriptionFilterKey.class.getName());
        type.setValueType(IotSubscriptionFilter.class.getName());

        // Key fields for iot_subscription_filter.
        Collection<CacheTypeFieldMetadata> keys = new ArrayList<>();
        keys.add(new CacheTypeFieldMetadata("name", Types.VARCHAR, "name", String.class));
        keys.add(new CacheTypeFieldMetadata("partition_id", Types.VARCHAR, "partitionId", String.class));
        type.setKeyFields(keys);

        // Value fields for iot_subscription_filter.
        Collection<CacheTypeFieldMetadata> vals = new ArrayList<>();
        vals.add(new CacheTypeFieldMetadata("date_created", Types.TIMESTAMP, "dateCreated", java.sql.Timestamp.class));
        vals.add(new CacheTypeFieldMetadata("date_modified", Types.TIMESTAMP, "dateModified", java.sql.Timestamp.class));
        vals.add(new CacheTypeFieldMetadata("is_active", Types.BIT, "isActive", boolean.class));
        vals.add(new CacheTypeFieldMetadata("id", Types.BIGINT, "id", long.class));
        vals.add(new CacheTypeFieldMetadata("name", Types.VARCHAR, "name", String.class));
        vals.add(new CacheTypeFieldMetadata("is_final", Types.BIT, "isFinal", boolean.class));
        vals.add(new CacheTypeFieldMetadata("partition_id", Types.VARCHAR, "partitionId", String.class));
        vals.add(new CacheTypeFieldMetadata("parent_id", Types.BIGINT, "parentId", Long.class));
        type.setValueFields(vals);

        // Query fields for iot_subscription_filter.
        Map<String, Class<?>> qryFlds = new LinkedHashMap<>();

        qryFlds.put("dateCreated", java.sql.Timestamp.class);
        qryFlds.put("dateModified", java.sql.Timestamp.class);
        qryFlds.put("isActive", boolean.class);
        qryFlds.put("id", long.class);
        qryFlds.put("name", String.class);
        qryFlds.put("isFinal", boolean.class);
        qryFlds.put("partitionId", String.class);
        qryFlds.put("parentId", Long.class);

        type.setQueryFields(qryFlds);

        // Ascending fields for iot_subscription_filter.
        Map<String, Class<?>> ascFlds = new LinkedHashMap<>();

        ascFlds.put("id", long.class);
        ascFlds.put("partitionId", String.class);
        ascFlds.put("parentId", Long.class);

        type.setAscendingFields(ascFlds);

        // Groups for iot_subscription_filter.
        Map<String, LinkedHashMap<String, IgniteBiTuple<Class<?>, Boolean>>> grps = new LinkedHashMap<>();

        LinkedHashMap<String, IgniteBiTuple<Class<?>, Boolean>> grpItems = new LinkedHashMap<>();

        grpItems.put("partitionId", new IgniteBiTuple<Class<?>, Boolean>(String.class, false));
        grpItems.put("name", new IgniteBiTuple<Class<?>, Boolean>(String.class, false));

        grps.put("iot_subscription_filter_partition_id_5207837a4ebbe100_uniq", grpItems);

        type.setGroups(grps);

        ccfg.setTypeMetadata(meta);

        return ccfg;
    }
}
