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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.*;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.extra.SessionState;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.SessionStateListener;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;
import org.json.JSONException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

/**
 * This class maintains all the SMPP sessions and connections to a single SMSC.
 *
 * @since 1.0.2
 */
public class ConnectionContext {

    private static final Log log = LogFactory.getLog(ConnectionContext.class);
    /**
     * SMPP Session used to communicate with SMSC.
     */
    private SMPPSession session;
    /**
     * port to access the SMSC.
     */
    private int port;
    /**
     * The password may be used by the SMSC to authenticate the ESME requesting to bind.
     */
    private String password;
    /**
     * Used to check whether SMSC is connected or not.
     */
    private int enquireLinkTimer;
    /**
     * Time elapsed between smpp request and the corresponding response.
     */
    private int transactionTimer;
    /**
     * Identifies the type of ESME system requesting to bind as a transmitter with the SMSC.
     */
    private String systemType;
    /**
     * Indicates Type of Number of the ESME address.
     */
    private String addressTON;
    /**
     * Numbering Plan Indicator for ESME address.
     */
    private String addressNPI;
    /**
     * Indicates SMS application service.
     */
    private String serviceType;
    /**
     * Type of number for source address.
     */
    private String sourceAddressTon;
    /**
     * Numbering plan indicator for source address.
     */
    private String sourceAddressNpi;
    /**
     * Source address of the short message.
     */
    private String sourceAddress;
    /**
     * Type of number for destination.
     */
    private String distinationAddressTon;
    /**
     * Numbering plan indicator for destination.
     */
    private String distinationAddressNpi;
    /**
     * Destination address of the short message.
     */
    private String distinationAddress;
    /**
     * Used to define message mode and message type.
     */
    private int esmClass;
    /**
     * Protocol identifier.
     */
    private int protocolId;
    /**
     * Sets the priority of the message.
     */
    private int priorityFlag;
    /**
     * Delivery of the message.
     */
    private TimeFormatter timeFormatter;
    /**
     * Validity period of message.
     */
    private String validityPeriod;
    /**
     * Type of the SMSC delivery receipt.
     */
    private String smscDeliveryReceipt;
    /**
     * Flag indicating if submitted message should replace an existing message.
     */
    private int replaceifpresentflag;
    /**
     * Alphabet used in the data encoding of the message.
     */
    private String alphabet;
    /**
     * Defines the encoding scheme of the SMS message.
     */
    private GeneralDataCoding dataCoding;
    /**
     * Indicates short message to send from a predefined list of messages stored on SMSC.
     */
    private int submitdefaultmsgid;
    /**
     * Content of the SMS.
     */
    private String message;

    /**
     * Initialize the ConnectionContext for a specific destination planning to use a pre-defined SMPP connection.
     */
    public ConnectionContext(String host, String systemId, MessageContext messageContext) throws Exception {
        port = Integer.parseInt(messageContext.getProperty(SMPPConstants.PORT).toString());
        password = messageContext.getProperty(SMPPConstants.PASSWORD).toString();
        enquireLinkTimer = Integer.parseInt(messageContext.getProperty(
                SMPPConstants.ENQUIRE_LINK_TIMER).toString());
        transactionTimer = Integer.parseInt(messageContext.getProperty(
                SMPPConstants.TRANSACTION_TIMER).toString());
        systemType = messageContext.getProperty(SMPPConstants.SYSTEM_TYPE).toString();
        addressTON = messageContext.getProperty(SMPPConstants.ADDRESS_TON).toString();
        addressNPI = messageContext.getProperty(SMPPConstants.ADDRESS_NPI).toString();
        try {
            BindParameter bindParameter = new BindParameter(BindType.BIND_TX,
                    systemId, password, systemType,
                    TypeOfNumber.valueOf(addressTON),
                    NumberingPlanIndicator.valueOf(addressNPI), null);
            session = new SMPPSession(host, port, bindParameter);
            session.setEnquireLinkTimer(enquireLinkTimer);
            session.setTransactionTimer(transactionTimer);
            session.addSessionStateListener(new SessionStateListenerImpl());
            if (log.isDebugEnabled()) {
                log.debug("Conected and bind to " + host);
            }
        } catch (IOException e) {
            throw new IOException("Error while configuring: " + e.getMessage(), e);
        }
    }

    /**
     * Method exposed to publish a message using this SMPP session.
     */
    public void publishMessage(MessageContext messageContext) throws PDUException,
            ResponseTimeoutException, InvalidResponseException, NegativeResponseException, IOException, XMLStreamException, JSONException {
        if (log.isDebugEnabled()) {
            log.debug("started to load params for publishing message");
        }
        serviceType = messageContext.getProperty(SMPPConstants.SERVICE_TYPE).toString();
        sourceAddressTon = messageContext.getProperty(
                SMPPConstants.SOURCE_ADDRESS_TON).toString();
        sourceAddressNpi = messageContext.getProperty(
                SMPPConstants.SOURCE_ADDRESS_NPI).toString();
        sourceAddress = messageContext.getProperty(SMPPConstants.SOURCE_ADDRESS).toString();
        distinationAddressTon = messageContext.getProperty(
                SMPPConstants.DISTINATION_ADDRESS_TON).toString();
        distinationAddressNpi = messageContext.getProperty(
                SMPPConstants.DISTINATION_ADDRESS_NPI).toString();
        distinationAddress = messageContext.getProperty(
                SMPPConstants.DISTINATION_ADDRESS).toString();
        esmClass = Integer.parseInt(messageContext.getProperty(
                SMPPConstants.ESM_CLASS).toString());
        protocolId = Integer.parseInt(messageContext.getProperty(
                SMPPConstants.PROTOCOL_ID).toString());
        priorityFlag = Integer.parseInt(messageContext.getProperty(
                SMPPConstants.PRIORITY_FLAG).toString());
        timeFormatter = new AbsoluteTimeFormatter();
        validityPeriod = messageContext.getProperty(SMPPConstants.VALIDITY_PERIOD).toString();
        smscDeliveryReceipt = messageContext.getProperty(
                SMPPConstants.SMSC_DELIVERY_RECEIPT).toString();
        replaceifpresentflag = Integer.parseInt(messageContext.getProperty(
                SMPPConstants.REPLACE_IF_PRESENT_FLAG).toString());
        alphabet = messageContext.getProperty(SMPPConstants.ALPHABET).toString();
        String messageClass = messageContext.getProperty(SMPPConstants.MESSAGE_CLASS).toString();
        Boolean iscompressed = Boolean.parseBoolean(messageContext.getProperty(
                SMPPConstants.IS_COMPRESSED).toString());
        dataCoding = new GeneralDataCoding(Alphabet.valueOf(alphabet),
                MessageClass.valueOf(messageClass), iscompressed);
        submitdefaultmsgid = Integer.parseInt(messageContext.getProperty(
                SMPPConstants.SUBMIT_DEFAULT_MESSAGE_ID).toString());
        message = messageContext.getProperty(SMPPConstants.SMS_MESSAGE).toString();
        try {
            //Send the SMS message.
            String messageId = session.submitShortMessage(
                    serviceType,
                    TypeOfNumber.valueOf(sourceAddressTon),
                    NumberingPlanIndicator.valueOf(sourceAddressNpi),
                    sourceAddress,
                    TypeOfNumber.valueOf(distinationAddressTon),
                    NumberingPlanIndicator.valueOf(distinationAddressNpi),
                    distinationAddress,
                    new ESMClass(esmClass),
                    (byte) protocolId, (byte) priorityFlag,
                    timeFormatter.format(new Date()),
                    validityPeriod,
                    new RegisteredDelivery(SMSCDeliveryReceipt.valueOf(smscDeliveryReceipt)),
                    (byte) replaceifpresentflag,
                    dataCoding, (byte) submitdefaultmsgid,
                    message.getBytes());
            String response = SMPPConstants.START_TAG + messageId + SMPPConstants.END_TAG;
            OMElement element;
            element = transformMessages(response);
            preparePayload(messageContext, element);

            if (log.isDebugEnabled()) {
                log.debug("Message submitted, message_id is " + messageId);
            }
        } catch (PDUException e) {
            // Invalid PDU parameter.
            session = null;
            throw new PDUException("Invalid PDU parameter" + e.getMessage(), e);
        } catch (ResponseTimeoutException e) {
            // Response timeout.
            session = null;
            throw new ResponseTimeoutException("Response timeout" + e.getMessage(), e);
        } catch (InvalidResponseException e) {
            // Invalid responselid respose.
            session = null;
            throw new InvalidResponseException("Invalid response" + e.getMessage(), e);
        } catch (NegativeResponseException e) {
            // Receiving negative response (non-zero command_status).
            session = null;
            throw new IOException("Receive negative response" + e.getMessage(), e);
        } catch (IOException e) {
            session = null;
            throw new IOException("IO error occur" + e.getMessage(), e);
        } catch (XMLStreamException e) {
            throw new XMLStreamException("Error while preparing the payload " + e.getMessage(), e);
        } catch (JSONException e) {
            throw new JSONException("Error while preparing the payload ");
        }
    }

    /**
     * Prepare payload.
     *
     * @param messageContext The message context that is processed by a handler in the handle method.
     * @param element        OMElement.
     */
    private void preparePayload(MessageContext messageContext, OMElement element) {
        SOAPBody soapBody = messageContext.getEnvelope().getBody();
        for (Iterator itr = soapBody.getChildElements(); itr.hasNext(); ) {
            OMElement child = (OMElement) itr.next();
            child.detach();
        }
        for (Iterator itr = element.getChildElements(); itr.hasNext(); ) {
            OMElement child = (OMElement) itr.next();
            soapBody.addChild(child);
        }
    }

    /**
     * Create a OMElement.
     *
     * @param output output.
     * @return return resultElement.
     * @throws XMLStreamException
     * @throws IOException
     * @throws org.codehaus.jettison.json.JSONException
     */
    private OMElement transformMessages(String output) throws XMLStreamException, IOException,
            JSONException {
        OMElement resultElement;
        resultElement = AXIOMUtil.stringToOM(output);
        return resultElement;
    }

    /**
     * Get session Object.
     */
    public SMPPSession getSession() {
        return session;
    }

    /**
     * Method to properly shutdown the SMPP sessions and connections in the proper order.
     */
    public void close() {
        if (session != null && session.getSessionState().isBound()) {
            session.unbindAndClose();
            if (log.isDebugEnabled()) {
                log.debug("Session is unbinded");
            }
        }
    }

    /**
     * This class will receive the notification from {@link SMPPSession} for the
     * state changes.
     */
    private class SessionStateListenerImpl implements SessionStateListener {
        @Override
        public void onStateChange(SessionState sessionState, SessionState sessionState1, Object o) {
            if (sessionState.equals(SessionState.CLOSED)) {
                session = null;
                if (log.isDebugEnabled()) {
                    log.debug("Session closed");
                }
            }
        }
    }
}