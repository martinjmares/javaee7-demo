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
package com.oracle.jdd2013.wsocket.client;

import com.oracle.jdd2013.wsocket.client.cmd.CmdLineParams;
import java.io.IOException;
import java.net.URI;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 *
 * @author martinmares
 */
@Singleton
@ClientEndpoint
public class UltimateMachineClient {
    
    private final URI uri;
    private Session session;
    
    @Inject
    private Event<UMPointsChangedEvent> pointsEvent;

    @Inject
    UltimateMachineClient(CmdLineParams params) throws IOException, DeploymentException {
        String suri = params.getFirst("umuri");
        if (suri == null || suri.length() == 0) {
            throw new IllegalStateException("Parameter --uri must be specified on the command line");
        }
        uri = URI.create(suri);
        System.out.println("UM Client URI: " + uri);
        reconnect();
    }

    private void reconnect() throws IOException, DeploymentException {
        WebSocketContainer cont = ContainerProvider.getWebSocketContainer();
        this.session = cont.connectToServer(this, uri);
    }
    
    @OnMessage
    public void onMessage(String str) {
        if (str.startsWith("POINTS:")) {
            try {
                pointsEvent.fire(new UMPointsChangedEvent(Integer.parseInt(str.substring(7).trim())));
            } catch (NumberFormatException ex) {
                System.out.println("Can not parse points: " + str.substring(7));
            }
        } else {
            System.out.println("Unknown web socket message: " + str);
        }
    }
    
    @OnClose
    public void onClose() {
        this.session = null;
        System.out.println("Connection was closed. Try to reconnect.");
        try {
            reconnect();
        } catch (IOException | DeploymentException ex) {
            System.out.println("Can not reconnect to " + uri);
        }
    }
    
    public void sendNewUMState(boolean opened) throws IOException {
        if (session != null) {
            session.getBasicRemote().sendText(opened ? "UM: ON" : "UM: OFF");
        }
    }
    
}
