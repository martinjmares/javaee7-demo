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
package com.oracle.jdd2013.um.basic;

import com.oracle.jdd2013.wsocket.client.UMPointsChangedEvent;
import com.oracle.jdd2013.wsocket.client.cmd.CmdLineParams;
import com.oracle.jdd2013.wsocket.client.UltimateMachineClient;
import java.io.File;
import java.io.IOException;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jboss.weld.environment.se.events.ContainerInitialized;

/**
 *
 * @author martinmares
 */
@Singleton
public class FileBaseUltimateMachine {

    @Inject
    private UltimateMachineClient client;

    private volatile int treshold = -1;
    private volatile File file;
    private volatile boolean on = false;

    @Inject
    public FileBaseUltimateMachine(CmdLineParams params) {
        String sth = params.getFirst("umtreshold");
        if (sth == null) {
            System.out.println("FileBaseUltimateMachine: No \"umtreshold\" parameter. Use default = 5");
            this.treshold = 5;
        } else {
            try {
                this.treshold = Integer.parseInt(sth);
            } catch (Exception ex) {
                System.out.println("FileBaseUltimateMachine: Can not parse \"umtreshold\" parameter. Use default = 5");
                this.treshold = 5;
            }
        }
        String filename = params.getFirst("umfile");
        if (filename != null) {
            file = new File(filename);
        }
    }

    public synchronized void pointsChanged(@Observes UMPointsChangedEvent event) {
        if (file == null || treshold < 0) {
            return;
        }
        if (event.getPoints() >= treshold) {
            synchronized (this) {
                if (on) {
                    file.delete();
                }
            }
        }
    }

    public void execute(@Observes ContainerInitialized initEvent) {
        if (file == null) {
            System.out.println("FileBaseUltimateMachine: No \"umfile\" parameter. Can not operate");
            return;
        }
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(600L);
                    } catch (InterruptedException ex) {
                    }
                    boolean actual = file.exists();
                    if (actual != on) {
                        synchronized (FileBaseUltimateMachine.this) {
                            System.out.println("FileBaseUltimateMachine: SWITCHED " + (actual ? "ON" : "OFF"));
                            try {
                                client.sendNewUMState(actual);
                            } catch (IOException ex) {
                                System.out.println("FileBaseUltimateMachine: Can not call remote admin system" + ex);
                                ex.printStackTrace(System.out);
                            }
                            on = actual;
                        }
                    }
                }
            }
        });
        thread.start();
    }

}
