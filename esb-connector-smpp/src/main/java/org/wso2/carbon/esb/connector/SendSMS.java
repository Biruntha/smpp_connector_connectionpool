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
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;

/**
 * Send SMS message to SMSC.
 *
 * @since 1.0.2
 */
public class SendSMS extends AbstractConnector {
    private static final Log log = LogFactory.getLog(SendSMSConnector.class);

    /**
     * @param messageContext It is the representation for a message within the ESB message flow.
     */
    @Override
    public void connect(MessageContext messageContext) {
        /*
          Get the Connection Context.
         */
        ConnectionContext connectionContext = (ConnectionContext) messageContext.getProperty(
                SMPPConstants.CONNECTION_CONTEXT);
        /*
          Is set to true if the Connection Pool is already initialized.
         */
        Boolean isPoolInitialized = (Boolean) messageContext.getProperty(SMPPConstants.IS_POOL_INITIALIZED);
        try {
            if (connectionContext.getSession() != null) {
                connectionContext.publishMessage(messageContext);
            }
        } catch (Exception e) {
            log.error("Error while publishing message ", e);
        } finally {
            if (!isPoolInitialized) {
                connectionContext.close();
            } else {
                /*
                  Get the Connection Pool.
                 */
                ConnectionPool connectionPool = (ConnectionPool) messageContext.getProperty(
                        SMPPConstants.CONNECTION_POOL);
                connectionPool.releasePublisher(connectionContext);
            }
        }
    }
}
