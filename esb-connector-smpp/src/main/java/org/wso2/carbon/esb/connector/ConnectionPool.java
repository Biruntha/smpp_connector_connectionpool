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
import org.apache.synapse.MessageContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manage a pool of connections for a single SystemId + SMSC combination to avoid sharing a single
 * connection during parallel invocations.
 *
 * @since 1.0.2
 */
public class ConnectionPool {
    private static final Log log = LogFactory.getLog(ConnectionPool.class);

    /**
     * Maximum number of connections allowed in a single pool that is single destination (SMSC).
     */
    private final int maxSize;
    /**
     * Will maintain already created and available connections upto the max limit.
     */
    public List<ConnectionContext> freePublishers = new ArrayList<ConnectionContext>();
    /**
     * Will maintain no of connections currently in use.
     */
    private int count = 0;
    /**
     * The message context that is processed by a handler in the handle method.
     */
    private MessageContext messageContext;
    /**
     * IP address of the SMSC.
     */
    private String host;
    /**
     * Identifies the ESME system requesting to bind as a receiver with the SMSC.
     */
    private String systemId;

    /**
     * Initialize the Connection pool with max Pool size.
     */
    public ConnectionPool(String host, String systemId, int maxPoolSize,
                          MessageContext messageContext) {
        this.host = host;
        this.systemId = systemId;
        this.maxSize = maxPoolSize;
        this.messageContext = messageContext;
    }

    /**
     * Get the Publisher.
     */
    public ConnectionContext getPublisher() throws Exception {
        printDebugLog("Requesting publisher.");
        for (Iterator<ConnectionContext> iterator = freePublishers.iterator();
             iterator.hasNext(); ) {
            if (iterator.next().getSession() == null) {
                iterator.remove();
            } else {
                break;
            }
        }
        if (!freePublishers.isEmpty()) {
            ConnectionContext publisher = freePublishers.remove(0);
            count++;
            printDebugLog("Returning an existing free publisher : " + publisher);

            return publisher;
        } else if (canHaveMorePublishers()) {
            ConnectionContext publisher = new ConnectionContext(host, systemId, messageContext);
            printDebugLog("Created and returning a whole new publisher for destination with hash : "
                    + publisher);
            count++;

            return publisher;
        } else {
            log.warn("The Publisher pool is fully utilized." + " destination : " + host + ":"
                    + systemId + ", free publishers : " + freePublishers.size() + ", busy publishers : "
                    + count);
        }
        return null;
    }

    /**
     * Add publisher that is already used into the freePublishers
     * or destroy that publisher.
     */
    public void releasePublisher(ConnectionContext publisher) {
        printDebugLog("Releasing Publisher : " + publisher);
        if (publisher.getSession() != null) {
            if (canHaveMorePublishers()) {
                freePublishers.add(publisher);
                printDebugLog("Added publisher back to free pool.");
            } else {
                printDebugLog("Destroying publisher because we have reached maximum size of publisher pool.");
                publisher.close();
            }
        }
        count--;
    }

    /**
     * Check the total no of busyPublishers + freePublishers has reached the Maximum
     */
    public boolean canHaveMorePublishers() {
        return count + freePublishers.size() < maxSize;
    }

    private void printDebugLog(String message) {
        if (log.isDebugEnabled()) {
            log.debug("Message : " + message + " destination : " + host + ":" + systemId + ", free publishers : " +
                    freePublishers.size() + ", busy publishers : " + count);
        }
    }
}