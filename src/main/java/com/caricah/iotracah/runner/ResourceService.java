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

package com.caricah.iotracah.runner;

import com.caricah.iotracah.configurator.ConfigHandler;
import com.caricah.iotracah.system.BaseSystemHandler;
import org.apache.commons.configuration.Configuration;

import java.util.ServiceLoader;

/**
 * @author <a href="mailto:bwire@caricah.com"> Peter Bwire </a>
 * @version 1.0 8/8/15
 */
public abstract class ResourceService {


    private Configuration configuration;

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public ServiceLoader<ConfigHandler> getConfigurationSetLoader(){

        return ServiceLoader.load(ConfigHandler.class);
    }

   public ServiceLoader<BaseSystemHandler> getSystemBaseSetLoader(){

        return ServiceLoader.load(BaseSystemHandler.class);
    }



}