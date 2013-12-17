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
package com.oracle.jdd2013.um.classic;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App {
    
    private static final Pin PIN_SWITCH = RaspiPin.GPIO_01;
    private static final PinState ON = PinState.LOW;
    
    private final GpioPinDigitalInput piSwitch;
    private final Serial arduino;

    public App(String port) {
        final GpioController gpio = GpioFactory.getInstance();
        arduino = SerialFactory.createInstance();
        arduino.addListener(new SerialDataListener() {
            public void dataReceived(SerialDataEvent event) {
                System.out.println("Arduino: " + event.getData());
            }
        });
        arduino.open(port, 9600);
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException ex) {
        }
        write('h');
        piSwitch = gpio.provisionDigitalInputPin(PIN_SWITCH, "SWITCH");
        piSwitch.addListener(new GpioPinListenerDigital() {
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                stateChanged(event.getState() == ON);
            }
        });
        System.out.println("INITIALIZED - Cancel by break");
    }
    
    private void write(char ch) {
        System.out.println("-> " + ch);
        arduino.write(ch);
    }
    
    private void stateChanged(boolean on) {
        System.out.println("SWITCH: " + (on ? "ON" : "OFF"));
        if (on) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException ex) {}
            write('c');
        }
    }
    
    public static void main( String[] args ) {
        System.out.println("START");
        App app = new App(args[0]);
        while (true) {
            try {
                Thread.sleep(50000L);
            } catch (InterruptedException ex) {}
        }
    }
    
}
