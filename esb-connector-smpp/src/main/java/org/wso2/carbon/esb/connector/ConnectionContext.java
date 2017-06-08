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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
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
class ConnectionContext {

    private static final Log log = LogFactory.getLog(ConnectionContext.class);
    /**
     * SMPP Session used to communicate with SMSC.
     */
    private SMPPSession session;

    /**
     * Creating SMPP connection.
     */
    ConnectionContext(String host, String systemId, MessageContext messageContext) throws IOException {
        /*
          port to access the SMSC.
         */
        int port = Integer.parseInt(messageContext.getProperty(SMPPConstants.PORT).toString());
        /*
          The password may be used by the SMSC to authenticate the ESME requesting to bind.
         */
        String password = messageContext.getProperty(SMPPConstants.PASSWORD).toString();
        /*
          Used to check whether SMSC is connected or not.
         */
        int enquireLinkTimer = Integer.parseInt(messageContext.getProperty(SMPPConstants.ENQUIRE_LINK_TIMER).toString());
        /*
          Time elapsed between smpp request and the corresponding response.
         */
        int transactionTimer = Integer.parseInt(messageContext.getProperty(SMPPConstants.TRANSACTION_TIMER).toString());
        /*
          Identifies the type of ESME system requesting to bind as a transmitter with the SMSC.
         */
        String systemType = messageContext.getProperty(SMPPConstants.SYSTEM_TYPE).toString();
        /*
          Indicates Type of Number of the ESME address.
         */
        String addressTON = messageContext.getProperty(SMPPConstants.ADDRESS_TON).toString();
        /*
          Numbering Plan Indicator for ESME address.
         */
        String addressNPI = messageContext.getProperty(SMPPConstants.ADDRESS_NPI).toString();
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
            throw new IOException("Error while configuring: ", e);
        }
    }

    /**
     * Method exposed to publish a message using this SMPP session.
     */
    void publishMessage(MessageContext messageContext) throws PDUException, ResponseTimeoutException,
            InvalidResponseException, NegativeResponseException, IOException, XMLStreamException, JSONException {
        if (log.isDebugEnabled()) {
            log.debug("started to load parameters for publishing SMPP message");
        }
        /*
          Indicates SMS application service.
         */
        String serviceType = messageContext.getProperty(SMPPConstants.SERVICE_TYPE).toString();
        /*
          Type of number for source address.
         */
        String sourceAddressTon = messageContext.getProperty(SMPPConstants.SOURCE_ADDRESS_TON).toString();
        /*
          Numbering plan indicator for source address.
         */
        String sourceAddressNpi = messageContext.getProperty(SMPPConstants.SOURCE_ADDRESS_NPI).toString();
        /*
          Source address of the short message.
         */
        String sourceAddress = messageContext.getProperty(SMPPConstants.SOURCE_ADDRESS).toString();
        /*
          Type of number for destination.
         */
        String distinationAddressTon = messageContext.getProperty(SMPPConstants.DISTINATION_ADDRESS_TON).toString();
        /*
          Numbering plan indicator for destination.
         */
        String distinationAddressNpi = messageContext.getProperty(SMPPConstants.DISTINATION_ADDRESS_NPI).toString();
        /*
          Destination address of the short message.
         */
        String distinationAddress = messageContext.getProperty(SMPPConstants.DISTINATION_ADDRESS).toString();
        /*
          Used to define message mode and message type.
         */
        int esmClass = Integer.parseInt(messageContext.getProperty(SMPPConstants.ESM_CLASS).toString());
        /*
          Protocol identifier.
         */
        int protocolId = Integer.parseInt(messageContext.getProperty(SMPPConstants.PROTOCOL_ID).toString());
        /*
          Sets the priority of the message.
         */
        int priorityFlag = Integer.parseInt(messageContext.getProperty(SMPPConstants.PRIORITY_FLAG).toString());
        /*
          Delivery of the message.
         */
        TimeFormatter timeFormatter = new AbsoluteTimeFormatter();
        /*
          Validity period of message.
         */
        String validityPeriod = messageContext.getProperty(SMPPConstants.VALIDITY_PERIOD).toString();
        /*
          Type of the SMSC delivery receipt.
         */
        String smscDeliveryReceipt = messageContext.getProperty(SMPPConstants.SMSC_DELIVERY_RECEIPT).toString();
        /*
          Flag indicating if submitted message should replace an existing message.
         */
        int replaceifpresentflag = Integer.parseInt(messageContext.getProperty(
                SMPPConstants.REPLACE_IF_PRESENT_FLAG).toString());
        /*
          Alphabet used in the data encoding of the message.
         */
        String alphabet = messageContext.getProperty(SMPPConstants.ALPHABET).toString();
        String messageClass = messageContext.getProperty(SMPPConstants.MESSAGE_CLASS).toString();
        Boolean iscompressed = Boolean.parseBoolean(messageContext.getProperty(SMPPConstants.IS_COMPRESSED).toString());
        /*
          Defines the encoding scheme of the SMS message.
         */
        GeneralDataCoding dataCoding = new GeneralDataCoding(Alphabet.valueOf(alphabet),
                MessageClass.valueOf(messageClass), iscompressed);
        /*
          Indicates short message to send from a predefined list of messages stored on SMSC.
         */
        int submitdefaultmsgid = Integer.parseInt(messageContext.getProperty(
                SMPPConstants.SUBMIT_DEFAULT_MESSAGE_ID).toString());
        /*
          Content of the SMS.
         */
        String message = messageContext.getProperty(SMPPConstants.SMS_MESSAGE).toString();
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
            generateResult(messageContext, messageId);

            if (log.isDebugEnabled()) {
                log.debug("Message submitted, message_id is " + messageId);
            }
        } catch (PDUException e) {
            // Invalid PDU parameter.
            session = null;
            throw new PDUException("Invalid PDU parameter ", e);
        } catch (ResponseTimeoutException e) {
            // Response timeout.
            session = null;
            throw new ResponseTimeoutException("Response timeout ", e);
        } catch (InvalidResponseException e) {
            // Invalid responselid respose.
            session = null;
            throw new InvalidResponseException("Invalid response ", e);
        } catch (NegativeResponseException e) {
            // Receiving negative response (non-zero command_status).
            session = null;
            throw new IOException("Receive negative response ", e);
        } catch (IOException e) {
            session = null;
            throw new IOException("IO error occur", e);
        }
    }

    /**
     * Generate the result(messageId) to display after sending SMS.
     *
     * @param messageContext The message context that is used in generate result mediation flow.
     * @param output         Result to display after sending message.
     */
    private static void generateResult(MessageContext messageContext, String output) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = factory.createOMNamespace(SMPPConstants.SMPPCON, SMPPConstants.NAMESPACE);
        OMElement result = factory.createOMElement(SMPPConstants.RESULT, ns);
        OMElement messageElement = factory.createOMElement(SMPPConstants.MESSAGE_ID, ns);
        messageElement.setText(output);
        result.addChild(messageElement);
        preparePayload(messageContext, result);
    }

    /**
     * Prepare payload is used to delete the element in existing body and add the new element.
     *
     * @param messageContext The message context that is used to prepare payload message flow.
     * @param element        The OMElement that needs to be added in the body.
     */
    private static void preparePayload(MessageContext messageContext, OMElement element) {
        SOAPBody soapBody = messageContext.getEnvelope().getBody();
        for (Iterator itr = soapBody.getChildElements(); itr.hasNext(); ) {
            OMElement child = (OMElement) itr.next();
            child.detach();
        }
        soapBody.addChild(element);
    }

    /**
     * Get session Object.
     *
     * @return SMPPSession SMPP session object.
     */
    SMPPSession getSession() {
        return session;
    }

    /**
     * Method to properly shutdown the SMPP sessions and connections in the proper order.
     */
    void close() {
        if (session != null && session.getSessionState().isBound()) {
            session.unbindAndClose();
            if (log.isDebugEnabled()) {
                log.debug("Session is unbinded");
            }
        }
    }

    /**
     * This class will receive the notification from {@link SMPPSession} for the state changes.
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
