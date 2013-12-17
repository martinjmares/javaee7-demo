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
package com.oracle.jdd2013.um.pi;

import com.oracle.jdd2013.wsocket.client.UMPointsChangedEvent;
import com.oracle.jdd2013.wsocket.client.UltimateMachineClient;
import com.oracle.jdd2013.wsocket.client.cmd.CmdLineParams;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
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
public class UltimateMachineSwitch {
    
    private enum State {
        ON, OFF, SWITCHING_OFF
    }
    
    private static final PinState ON = PinState.LOW;
    
    @Inject
    private UltimateMachineClient client;
    
    private final GpioPinDigitalInput piSwitch;
    private final Serial arduino;
    private final int trashold;
    private State state;
    private volatile long lastHello = System.currentTimeMillis();
    
    @Inject
    public UltimateMachineSwitch(CmdLineParams params) throws Exception {
        //Switch
        int pin = params.getFirstInt("umswitch", 1, true);
        final GpioController gpio = GpioFactory.getInstance();
        piSwitch = gpio.provisionDigitalInputPin(UmPiUtils.getPin(pin), "SWITCH");
        piSwitch.addListener(new GpioPinListenerDigital() {
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                stateChanged(event.getState() == ON);
            }
        });
        state = piSwitch.getState() == ON ? State.ON : State.OFF;
        trashold = params.getFirstInt("umtrashold", 5, true);
        //Arduino communication
        arduino = SerialFactory.createInstance();
        String ardPort = params.getFirst("umarduino");
        if (ardPort == null) {
            throw new IllegalStateException("Command line parameter --umarduino was not defined");
        }
        arduino.addListener(new SerialDataListener() {
            public void dataReceived(SerialDataEvent event) {
                System.out.println("Arduino: " + event.getData());
                if ("hello".equalsIgnoreCase(event.getData())) {
                    lastHello = System.currentTimeMillis();
                }
            }
        });
        arduino.open(ardPort, 9600);
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException ex) {
        }
    }
    
    private void stateChanged(boolean on) {
        System.out.println("SWITCH: " + (on ? "ON" : "OFF"));
        synchronized (this) {
            this.state = on ? State.ON : State.OFF;
        }
        try {
            client.sendNewUMState(on);
        } catch (IOException ex) {
            System.out.println("ERROR: Can not send new switch state to the server");
        }
    }
    
    private synchronized void sendCommand(char ch) {
        arduino.write(ch);
    }
    
    public void vote(@Observes UMPointsChangedEvent event) {
        System.out.println("POINTS: " + event.getPoints());
        if (event.getPoints() >= trashold) {
            synchronized (this) {
                if (this.state == State.ON) {
                    sendCommand('c');
                    this.state = State.SWITCHING_OFF;
                }
            }
        }
    }
    
    public void execute(@Observes ContainerInitialized initEvent) {
        System.out.println("EXECUTING: " + this.getClass().getName());
        this.lastHello = System.currentTimeMillis();
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException ex) {
                }
                sendCommand('h');
                if ((System.currentTimeMillis() - lastHello) > 41000) {
                    System.out.println("ERROR: Arduino does not react with hello");
                }
            }
        });
        thread.start();
    }
    
}
