/*
 * Copyright 2011 Bill La Forge
 *
 * This file is part of AgileWiki and is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (LGPL) as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 * or navigate to the following url http://www.gnu.org/licenses/lgpl-2.1.txt
 *
 * Note however that only Scala, Java and JavaScript files are being covered by LGPL.
 * All other files are covered by the Common Public License (CPL).
 * A copy of this license is also included and can be
 * found as well at http://www.opensource.org/licenses/cpl1.0.txt
 */
package org.agilewiki.jactor.apc;

import org.agilewiki.jactor.bufferedEvents.BufferedEventsQueue;
import org.agilewiki.jactor.concurrent.ThreadManager;
import org.agilewiki.jactor.events.ActiveEventProcessor;

import java.util.ArrayList;

abstract public class JAPCActor implements APCActor {

    private xAPCMailbox mailboxX;

    /**
     * Handles callbacks from the inbox.
     */
    private ActiveEventProcessor<APCRequest> eventProcessor = new ActiveEventProcessor<APCRequest>() {
        @Override
        public void haveEvents() {
            mailboxX.dispatchEvents();
        }

        @Override
        public void processEvent(APCRequest request) {
            JAPCActor.this.processRequest(request);
        }
    };

    private APCRequestSource apcRequestSource = new APCRequestSource() {
        @Override
        public void responseFrom(BufferedEventsQueue<APCMessage> eventQueue, APCResponse apcResponse) {
            eventQueue.send(JAPCActor.this, apcResponse);
        }
    };

    /**
     * Create a JAEventActor
     *
     * @param threadManager Provides a thread for processing dispatched events.
     */
    public JAPCActor(ThreadManager threadManager) {
        this(new JAPCMailboxX(threadManager));
    }

    /**
     * Create a JAEventActor
     * Use this constructor when providing an implementation of BufferedEventsQueue
     * other than JABufferedEventsQueue.
     *
     * @param mailboxX The actor's mailboxX.
     */
    public JAPCActor(xAPCMailbox mailboxX) {
        this.mailboxX = mailboxX;
        mailboxX.setEventProcessor(eventProcessor);
        this.mailboxX = mailboxX;
    }

    /**
     * Set the initial capacity for buffered outgoing events.
     *
     * @param initialBufferCapacity The initial capacity for buffered outgoing events.
     */
    @Override
    final public void setInitialBufferCapacity(int initialBufferCapacity) {
        mailboxX.setInitialBufferCapacity(initialBufferCapacity);
    }

    /**
     * The putBufferedEvents method adds events to be processed.
     *
     * @param bufferedEvents The events to be processed.
     */
    @Override
    final public void putBufferedEvents(ArrayList<APCMessage> bufferedEvents) {
        mailboxX.putBufferedEvents(bufferedEvents);
    }

    final protected void send(APCActor actor, Object data, ResponseDestination responseDestination) {
        APCRequest apcRequest = new APCRequest(apcRequestSource, data, responseDestination);
        mailboxX.send(actor, apcRequest);
    }

    final protected void iterate(final APCFunction apcFunction,
                                 final ResponseDestination responseDestination) {
        ResponseDestination rd = new ResponseDestination() {
            @Override
            public void process(Object result) {
                if (result == null)
                    apcFunction.process(this);
                else responseDestination.process(result);
            }
        };
        apcFunction.process(rd);
    }

    abstract protected void processRequest(APCRequest request);
}