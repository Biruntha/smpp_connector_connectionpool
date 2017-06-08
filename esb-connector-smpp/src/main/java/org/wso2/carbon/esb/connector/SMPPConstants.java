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

/**
 * Contains all constants used in SMPP connector implementation.
 *
 * @since 1.0.2
 */
public class SMPPConstants {
    // SMPP Config constants.
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String SYSTEM_ID = "systemId";
    public static final String PASSWORD = "password";
    public static final String SYSTEM_TYPE = "systemType";
    public static final String ADDRESS_TON = "addressTon";
    public static final String ADDRESS_NPI = "addressNpi";
    public static final String ENQUIRE_LINK_TIMER = "enquireLinkTimer";
    public static final String TRANSACTION_TIMER = "transactionTimer";
    public static final String SERVICE_TYPE = "serviceType";
    public static final String SOURCE_ADDRESS_TON = "sourceAddressTon";
    public static final String SOURCE_ADDRESS_NPI = "sourceAddressNpi";
    public static final String SOURCE_ADDRESS = "sourceAddress";
    public static final String DISTINATION_ADDRESS_TON = "distinationAddressTon";
    public static final String DISTINATION_ADDRESS_NPI = "distinationAddressNpi";
    public static final String DISTINATION_ADDRESS = "distinationAddress";
    public static final String ALPHABET = "alphabet";
    public static final String SMS_MESSAGE = "message";
    public static final String SMSC_DELIVERY_RECEIPT = "smscDeliveryReceipt";
    public static final String MESSAGE_CLASS = "messageClass";
    public static final String IS_COMPRESSED = "isCompressed";
    public static final String ESM_CLASS = "esmclass";
    public static final String PROTOCOL_ID = "protocolid";
    public static final String PRIORITY_FLAG = "priorityflag";
    public static final String REPLACE_IF_PRESENT_FLAG = "replaceIfPresentFlag";
    public static final String SUBMIT_DEFAULT_MESSAGE_ID = "submitDefaultMsgId";
    public static final String VALIDITY_PERIOD = "validityPeriod";
    public static final String NULL = "null";
    public static final String CONNECTION_POOL_SIZE = "connectionPoolSize";
    public static final String DEFAULT = "DEFAULT";
    public static final String ALPHA_DEFAULT = "ALPHA_DEFAULT";
    public static final String CLASS1 = "CLASS1";
    public static final String ZERO = "0";
    public static final String FALSE = "false";
    public static final String ENQUIRELINK_TIMER_DEFAULT = "50000";
    public static final String TRANSACTION_TIMER_DEFAULT = "500";
    public static final String CONNECTION_CONTEXT = "connectionContext";
    public static final String CONNECTION_POOL = "connectionPool";
    public static final String IS_POOL_INITIALIZED = "isPoolInitialized";
    public static final String SMPPCON = "http://org.wso2.esbconnectors.SMPPConnector";
    public static final String NAMESPACE = "ns";
    public static final String RESULT = "result";
    public static final String MESSAGE_ID = "messageId";
}
