/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.esb.connector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to act as a single reference point for all connectionPool Objects.
 *
 * @since 1.0.2
 */
public class ConnectionPoolManager {
    private static Log log = LogFactory.getLog(ConnectionPoolManager.class);
    /**
     * Single ConnectionPoolManager instance kept.
     */
    private static ConnectionPoolManager connectionPoolManager = null;
    /**
     * Holds all the Connection Pool objects for SystemId + SMSC combination.
     */
    private Map<String, ConnectionPool> connectionPool = new ConcurrentHashMap<String, ConnectionPool>();

    /**
     * create an object of ConnectionPoolManager.
     */
    public static ConnectionPoolManager getInstance() {
        if (log.isDebugEnabled()) {
            log.debug("Initializing Connection Pool Manager");
        }
        if (connectionPoolManager == null) {
            connectionPoolManager = new ConnectionPoolManager();
        }
        return connectionPoolManager;
    }

    /**
     * @param key   Identity for a single SystemId + SMSC combination.
     * @param value Connection Pool object for a single SMSC + SystemId combination.
     */
    public void put(String key, ConnectionPool value) {
        connectionPool.put(key, value);
    }

    /**
     * @param key Identity for a single SMSC + SystemId combination.
     * @return Connection Pool object for a single SMSC + SystemId combination.
     */
    public ConnectionPool get(String key) {
        return connectionPool.get(key);
    }
}