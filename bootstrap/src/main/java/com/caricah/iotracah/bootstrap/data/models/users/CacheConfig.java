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

package com.caricah.iotracah.bootstrap.data.models.users;

import com.caricah.iotracah.bootstrap.security.realm.state.IOTAccount;
import org.apache.ignite.cache.*;
import org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStoreFactory;
import org.apache.ignite.configuration.*;
import org.apache.ignite.lang.*;

import java.sql.*;
import java.util.*;

/**
 * CacheConfig definition.
 *
 * Code generated by Apache Ignite Schema Import utility: 02/23/2016.
 */
public class CacheConfig {
    /**
    * Configure cache.
    *  @param name Cache name.
    * @param storeFactory Cache store factory.
     */
    public static CacheConfiguration<IotAccountKey, IOTAccount> cache(String name, CacheJdbcPojoStoreFactory<IotAccountKey, IOTAccount> storeFactory) {

        CacheConfiguration<IotAccountKey, IOTAccount> ccfg = new CacheConfiguration<>(name);

        if(Objects.nonNull(storeFactory)) {
            ccfg.setCacheStoreFactory(storeFactory);
            ccfg.setReadThrough(true);
            ccfg.setWriteThrough(true);
        }

        // Configure cache types. 
        Collection<CacheTypeMetadata> meta = new ArrayList<>();

        // iot_user.
        CacheTypeMetadata type = new CacheTypeMetadata();

        meta.add(type);

        type.setDatabaseSchema("public");
        type.setDatabaseTable("iot_user");
        type.setKeyType(IotAccountKey.class.getName());
        type.setValueType(IOTAccount.class.getName());

        // Key fields for iot_user.
        Collection<CacheTypeFieldMetadata> keys = new ArrayList<>();
        keys.add(new CacheTypeFieldMetadata("username", Types.VARCHAR, "username", String.class));
        keys.add(new CacheTypeFieldMetadata("partition_id", Types.VARCHAR, "partitionId", String.class));
        type.setKeyFields(keys);

        // Value fields for iot_user.
        Collection<CacheTypeFieldMetadata> vals = new ArrayList<>();
        vals.add(new CacheTypeFieldMetadata("id", Types.BIGINT, "id", long.class));
        vals.add(new CacheTypeFieldMetadata("date_created", Types.TIMESTAMP, "dateCreated", java.sql.Timestamp.class));
        vals.add(new CacheTypeFieldMetadata("date_modified", Types.TIMESTAMP, "dateModified", java.sql.Timestamp.class));
        vals.add(new CacheTypeFieldMetadata("is_active", Types.BIT, "isActive", boolean.class));
        vals.add(new CacheTypeFieldMetadata("username", Types.VARCHAR, "username", String.class));
        vals.add(new CacheTypeFieldMetadata("credential", Types.VARCHAR, "credential", String.class));
        vals.add(new CacheTypeFieldMetadata("credential_salt", Types.BINARY, "credentialSalt", Object.class));
        vals.add(new CacheTypeFieldMetadata("rolelist", Types.VARCHAR, "rolelist", String.class));
        vals.add(new CacheTypeFieldMetadata("is_locked", Types.BIT, "isLocked", boolean.class));
        vals.add(new CacheTypeFieldMetadata("is_credential_expired", Types.BIT, "isCredentialExpired", boolean.class));
        vals.add(new CacheTypeFieldMetadata("partition_id", Types.VARCHAR, "partitionId", String.class));
        type.setValueFields(vals);

        // Query fields for iot_user.
        Map<String, Class<?>> qryFlds = new LinkedHashMap<>();

        qryFlds.put("id", long.class);
        qryFlds.put("dateCreated", java.sql.Timestamp.class);
        qryFlds.put("dateModified", java.sql.Timestamp.class);
        qryFlds.put("isActive", boolean.class);
        qryFlds.put("username", String.class);
        qryFlds.put("credential", String.class);
        qryFlds.put("credentialSalt", Object.class);
        qryFlds.put("rolelist", String.class);
        qryFlds.put("isLocked", boolean.class);
        qryFlds.put("isCredentialExpired", boolean.class);
        qryFlds.put("partitionId", String.class);

        type.setQueryFields(qryFlds);

        // Ascending fields for iot_user.
        Map<String, Class<?>> ascFlds = new LinkedHashMap<>();

        ascFlds.put("id", long.class);
        ascFlds.put("partitionId", String.class);

        type.setAscendingFields(ascFlds);

        // Groups for iot_user.
        Map<String, LinkedHashMap<String, IgniteBiTuple<Class<?>, Boolean>>> grps = new LinkedHashMap<>();

        LinkedHashMap<String, IgniteBiTuple<Class<?>, Boolean>> grpItems = new LinkedHashMap<>();

        grpItems.put("partitionId", new IgniteBiTuple<Class<?>, Boolean>(String.class, false));
        grpItems.put("username", new IgniteBiTuple<Class<?>, Boolean>(String.class, false));

        grps.put("iot_user_partition_id_4b5f477ac7644239_uniq", grpItems);

        type.setGroups(grps);

        ccfg.setTypeMetadata(meta);

        return ccfg;
    }
}
