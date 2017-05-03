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

import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.Connector;

/**
 * SMPP Connector Configuration.
 *
 * @since 1.0.2
 */
public class SMSConfig extends AbstractConnector implements Connector {
    /**
     * Get the Connection Pool Manager.
     */
    private ConnectionPoolManager connectionPoolManager;
    /**
     * Key identity for SystemId + SMSC Combination.
     */
    private String connectionContextKey;
    /**
     * Get the Connection Pool.
     */
    private ConnectionPool connectionPool;
    /**
     * Get the Connection Context.
     */
    private ConnectionContext connectionContext;
    /**
     * Is set to true if the Connection Pool is already initialized.
     */
    private Boolean isPoolInitialized;

    /**
     * @param messageContext The message context that is processed by a handler in the handle method.
     * @throws ConnectException
     */
    @Override
    public void connect(MessageContext messageContext) throws ConnectException {
        //IP address of the SMSC.
        String host = messageContext.getProperty(SMPPConstants.HOST).toString();
        if (StringUtils.isEmpty(host)) {
            handleException("Bind parameter(host) is not set ", messageContext);
        }
        //Identifies the ESME system requesting to bind as a transmitter with the SMSC.
        String systemId = messageContext.getProperty(SMPPConstants.SYSTEM_ID).toString();
        if (StringUtils.isEmpty(systemId)) {
            handleException("Bind parameter(systemId) is not set for " + host, messageContext);
        }
        //Port to access the SMSC.
        String port = messageContext.getProperty(SMPPConstants.PORT).toString();
        if (StringUtils.isEmpty(port)) {
            handleException("Bind parameter(port) is not set for " + host, messageContext);
        }
        //The password may be used by the SMSC to authenticate the ESME requesting to bind.
        String password = messageContext.getProperty(SMPPConstants.PASSWORD).toString();
        if (StringUtils.isEmpty(password)) {
            handleException("Bind parameter(password) is not set for " + host, messageContext);
        }
        //Identifies the type of ESME system requesting to bind as a transmitter with the SMSC.
        String systemType = (String) getParameter(messageContext, SMPPConstants.SYSTEM_TYPE);
        if (StringUtils.isEmpty(systemType)) {
            messageContext.setProperty(SMPPConstants.SYSTEM_TYPE, SMPPConstants.NULL);
        }
        //Indicates Type of Number of the ESME address.
        String addressTON = (String) getParameter(messageContext, SMPPConstants.ADDRESS_TON);
        if (StringUtils.isEmpty(addressTON)) {
            handleException("Bind parameter (Address TON) is not set for " + host, messageContext);
        }
        //Numbering Plan Indicator for ESME address.
        String addressNPI = (String) getParameter(messageContext, SMPPConstants.ADDRESS_NPI);
        if (StringUtils.isEmpty(addressNPI)) {
            handleException("Bind parameter (Address NPI) is not set for " + host, messageContext);
        }
        //Indicates SMS application service.
        String serviceType = messageContext.getProperty(SMPPConstants.SERVICE_TYPE).toString();
        if (StringUtils.isEmpty(serviceType)) {
            messageContext.setProperty(SMPPConstants.SERVICE_TYPE, SMPPConstants.NULL);
        }
        //Source address of the short message.
        String sourceAddress = messageContext.getProperty(SMPPConstants.SOURCE_ADDRESS).toString();
        //Type of number for source address.
        String sourceAddressTon = messageContext.getProperty(
                SMPPConstants.SOURCE_ADDRESS_TON).toString();
        if (StringUtils.isEmpty(sourceAddressTon)) {
            handleException("Source Address TON is not set for " + sourceAddress, messageContext);
        }
        //Numbering plan indicator for source address.
        String sourceAddressNpi = messageContext.getProperty(
                SMPPConstants.SOURCE_ADDRESS_NPI).toString();
        if (StringUtils.isEmpty(sourceAddressNpi)) {
            handleException("Source Address NPI is not set for " + sourceAddress, messageContext);
        }
        //Destination address of the short message.
        String distinationAddress = messageContext.getProperty(
                SMPPConstants.DISTINATION_ADDRESS).toString();
        //Type of number for destination.
        String distinationAddressTon = messageContext.getProperty(
                SMPPConstants.DISTINATION_ADDRESS_TON).toString();
        if (StringUtils.isEmpty(distinationAddressTon)) {
            handleException("Distination Address TON is not set for " + distinationAddress,
                    messageContext);
        }
        //Numbering plan indicator for destination.
        String distinationAddressNpi = messageContext.getProperty(
                SMPPConstants.DISTINATION_ADDRESS_NPI).toString();
        if (StringUtils.isEmpty(distinationAddressNpi)) {
            handleException("Distination Address NPI is not set for " + distinationAddress,
                    messageContext);
        }
        //Used to define message mode and message type.
        String esmClass = messageContext.getProperty(SMPPConstants.ESM_CLASS).toString();
        if (StringUtils.isEmpty(esmClass)) {
            //set it to default value.
            messageContext.setProperty(SMPPConstants.ESM_CLASS, SMPPConstants.ZERO);
        }
        //Protocol identifier.
        String protocolId = messageContext.getProperty(SMPPConstants.PROTOCOL_ID).toString();
        if (StringUtils.isEmpty(protocolId)) {
            //set it to default value.
            messageContext.setProperty(SMPPConstants.PROTOCOL_ID, SMPPConstants.ZERO);
        }
        //Sets the priority of the message.
        String priorityFlag = messageContext.getProperty(SMPPConstants.PRIORITY_FLAG).toString();
        if (StringUtils.isEmpty(priorityFlag)) {
            //set it to default value
            messageContext.setProperty(SMPPConstants.PRIORITY_FLAG, SMPPConstants.ZERO);
        }
        //Type of the SMSC delivery receipt.
        String smscDeliveryReceipt = messageContext.getProperty(
                SMPPConstants.SMSC_DELIVERY_RECEIPT).toString();
        if (StringUtils.isEmpty(smscDeliveryReceipt)) {
            messageContext.setProperty(SMPPConstants.SMSC_DELIVERY_RECEIPT, SMPPConstants.DEFAULT);
        }
        //Flag indicating if submitted message should replace an existing message.
        String replaceifpresentflag = messageContext.getProperty(
                SMPPConstants.REPLACE_IF_PRESENT_FLAG).toString();
        if (StringUtils.isEmpty(replaceifpresentflag)) {
            //set it to default value.
            messageContext.setProperty(SMPPConstants.REPLACE_IF_PRESENT_FLAG, SMPPConstants.ZERO);
        }
        //Alphabet used in the data encoding of the message.
        String alphabet = messageContext.getProperty(SMPPConstants.ALPHABET).toString();
        if (StringUtils.isEmpty(alphabet)) {
            messageContext.setProperty(SMPPConstants.ALPHABET, SMPPConstants.ALPHA_DEFAULT);
        }
        String messageClass = messageContext.getProperty(SMPPConstants.MESSAGE_CLASS).toString();
        if (StringUtils.isEmpty(messageClass)) {
            messageContext.setProperty(SMPPConstants.MESSAGE_CLASS, SMPPConstants.CLASS1);
        }
        String iscompressed = messageContext.getProperty(SMPPConstants.IS_COMPRESSED).toString();
        if (StringUtils.isEmpty(iscompressed)) {
            //set it to default value.
            messageContext.setProperty(SMPPConstants.IS_COMPRESSED, SMPPConstants.FALSE);
        }
        //Indicates short message to send from a predefined list of messages stored on SMSC.
        String submitdefaultmsgid = messageContext.getProperty(
                SMPPConstants.SUBMIT_DEFAULT_MESSAGE_ID).toString();
        if (StringUtils.isEmpty(submitdefaultmsgid)) {
            //set it to default value.
            messageContext.setProperty(SMPPConstants.SUBMIT_DEFAULT_MESSAGE_ID, SMPPConstants.ZERO);
        }
        String enquireLinktimer = messageContext.getProperty(
                SMPPConstants.ENQUIRE_LINK_TIMER).toString();
        if (StringUtils.isEmpty(enquireLinktimer)) {
            //set it to default value.
            messageContext.setProperty(SMPPConstants.ENQUIRE_LINK_TIMER,
                    SMPPConstants.ENQUIRELINK_TIMER_DEFAULT);
        }
        String transactiontimer = messageContext.getProperty(
                SMPPConstants.TRANSACTION_TIMER).toString();
        if (StringUtils.isEmpty(transactiontimer)) {
            //set it to default value.
            messageContext.setProperty(SMPPConstants.TRANSACTION_TIMER,
                    SMPPConstants.TRANSACTION_TIMER_DEFAULT);
        }
        String connectionPoolSize = messageContext.getProperty(SMPPConstants.CONNECTION_POOL_SIZE).toString();
        if (StringUtils.isEmpty(connectionPoolSize) || StringUtils.equals(connectionPoolSize, "0")) {
            isPoolInitialized = false;
            //Get the Connection Context.
            try {
                connectionContext = new ConnectionContext(host, systemId, messageContext);
            } catch (Exception e) {
                handleException("Error while creating session ", messageContext);
            }
        } else {
            isPoolInitialized = true;
            connectionPoolManager = ConnectionPoolManager.getInstance();
            log.info("Processing message for destination : " + host + " with " + systemId);
            if (log.isDebugEnabled()) {
                log.debug("Processing message for destination : " + host + " with " + systemId);
            }
            connectionContextKey = systemId + ":" + host;
            if (log.isDebugEnabled()) {
                log.debug("PublisherContextKey : " + connectionContextKey + " for " + host
                        + " and " + systemId + "with Connection pool size " + connectionPoolSize);
            }
            connectionPool = connectionPoolManager.get(connectionContextKey);
            if (null == connectionPool) {
                if (log.isDebugEnabled()) {
                    log.debug("SMPP Connection pool is miss for destination : " + host);
                }
                connectionPool = new ConnectionPool(host, systemId, Integer.parseInt(connectionPoolSize), messageContext);
                connectionPoolManager.put(connectionContextKey, connectionPool);
            }
            try {
                connectionContext = connectionPool.getPublisher();
            } catch (Exception e) {
                log.error("Error while getting the publisher from connection pool");
            }
            messageContext.setProperty(SMPPConstants.CONNECTION_POOL, connectionPool);
        }
        messageContext.setProperty(SMPPConstants.IS_POOL_INITIALIZED, isPoolInitialized);
        messageContext.setProperty(SMPPConstants.CONNECTION_CONTEXT, connectionContext);
    }
}