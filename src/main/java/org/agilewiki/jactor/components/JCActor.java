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
package org.agilewiki.jactor.components;

import org.agilewiki.jactor.JAIterator;
import org.agilewiki.jactor.JANull;
import org.agilewiki.jactor.Mailbox;
import org.agilewiki.jactor.ResponseProcessor;
import org.agilewiki.jactor.bind.JBActor;
import org.agilewiki.jactor.bind.MethodBinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * <p>
 * Application logic is added to a JCActor by including components rather than by subclassing.
 * </p>
 * <p>
 * JCActors are fully interoperable with JLPCActors and JBActors.
 * </p>
 */
final public class JCActor extends JBActor {
    /**
     * The type of actor.
     */
    private String actorType;

    /**
     * Create a JCActor.
     *
     * @param mailbox A mailbox which may be shared with other actors.
     */
    public JCActor(final Mailbox mailbox) {
        super(mailbox);

        bind(Include.class.getName(), new MethodBinding() {
            @Override
            protected void processRequest(Object request, ResponseProcessor rp) throws Exception {
                processInclude(request, rp);
            }
        });
    }

    /**
     * Process an include.
     *
     * @param request The include request.
     * @param rp      The response processor.
     * @throws Exception Any uncaught exceptions from calls to the component open methods.
     */
    private void processInclude(Object request, ResponseProcessor rp) throws Exception {
        Include include = (Include) request;
        Class clazz = include.getClazz();
        final String className = clazz.getName();
        ConcurrentSkipListMap<String, Object> data = getData();
        if (data.containsKey(className)) {
            rp.process(null);
            return;
        }
        Object o = clazz.newInstance();
        data.put(className, o);
        if (!(o instanceof Component)) {
            rp.process(null);
            return;
        }
        final Component c = (Component) o;
        ArrayList<Include> includes = c.includes();
        if (includes == null) {
            c.open(internals, rp);
            return;
        }
        final Iterator<Include> it = includes.iterator();
        (new JAIterator() {
            @Override
            protected void process(final ResponseProcessor rp1) throws Exception {
                if (it.hasNext()) {
                    processInclude(it.next(), rp1);
                } else {
                    c.open(internals, new ResponseProcessor() {
                        @Override
                        public void process(Object response) throws Exception {
                            rp1.process(new JANull());
                        }
                    });
                }
            }
        }).iterate(rp);
    }

    /**
     * Call close on all the components, ignoring any exceptions that are thrown.
     * The order in which close is called on the components is not defined.
     */
    public void close() {
        Iterator<Object> it = getData().values().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof Component) {
                Component c = (Component) o;
                try {
                    c.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Returns the actor type.
     *
     * @return The actor type, or null.
     */
    public String getActorType() {
        return actorType;
    }

    /**
     * Assigns the actorType.
     * Once assigned, it can not be changed.
     *
     * @param actorType The actor type.
     */
    public void setActorType(String actorType) {
        if (this.actorType != null)
            throw new UnsupportedOperationException("The actorType can not be changed");
        this.actorType = actorType;
    }
}
