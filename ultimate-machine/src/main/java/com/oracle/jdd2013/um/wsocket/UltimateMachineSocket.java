/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.oracle.jdd2013.um.wsocket;

import com.oracle.jdd2013.um.PointsChangeEvent;
import com.oracle.jdd2013.um.UltimateMachine;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author martinmares
 */
@ApplicationScoped
@ServerEndpoint("/socket")
public class UltimateMachineSocket {
    
    private static Logger logger = Logger.getLogger(UltimateMachineSocket.class.getName());
    
    @Inject
    UltimateMachine ultimateMachine;
    
    private transient Map<String, Session> sessions = new HashMap<String, Session>();

    @OnOpen
    public void open(Session session, EndpointConfig conf) {
        sessions.put(session.getId(), session);
    }
    
    @OnClose
    public void close(Session session, CloseReason reason) { 
        sessions.remove(session.getId());
    }
    
    @OnMessage
    public void message (Session session, String msg) {
        if ("UM: ON".equals(msg)) {
            ultimateMachine.setOpened(true);
        } else if ("UM: OFF".equals(msg)) {
            ultimateMachine.setOpened(false);
        } else {
            logger.log(Level.WARNING, "Unknown message: {0}", msg);
        }
    }
    
    public void pointsChanged(@Observes PointsChangeEvent event) {
        String points = "POINTS: " + String.valueOf(event.getValue());
        for (Session session : sessions.values()) {
            session.getAsyncRemote().sendText(points);
        }
    }
    
}
