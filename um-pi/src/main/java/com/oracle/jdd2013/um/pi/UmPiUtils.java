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

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 *
 * @author martinmares
 */
public class UmPiUtils {

    public static Pin getPin(int pin) throws Exception {
        Pin p;
        switch (pin) {
            case 0:
                p = RaspiPin.GPIO_00;
                break;
            case 1:
                p = RaspiPin.GPIO_01;
                break;
            case 2:
                p = RaspiPin.GPIO_02;
                break;
            case 3:
                p = RaspiPin.GPIO_03;
                break;
            case 4:
                p = RaspiPin.GPIO_04;
                break;
            case 5:
                p = RaspiPin.GPIO_05;
                break;
            case 6:
                p = RaspiPin.GPIO_06;
                break;
            case 7:
                p = RaspiPin.GPIO_07;
                break;
            case 8:
                p = RaspiPin.GPIO_08;
                break;
            case 9:
                p = RaspiPin.GPIO_09;
                break;
            case 10:
                p = RaspiPin.GPIO_10;
                break;
            case 11:
                p = RaspiPin.GPIO_11;
                break;
            case 12:
                p = RaspiPin.GPIO_12;
                break;
            case 13:
                p = RaspiPin.GPIO_13;
                break;
            case 14:
                p = RaspiPin.GPIO_14;
                break;
            case 15:
                p = RaspiPin.GPIO_15;
                break;
            case 16:
                p = RaspiPin.GPIO_16;
                break;
            case 17:
                p = RaspiPin.GPIO_17;
                break;
            case 18:
                p = RaspiPin.GPIO_18;
                break;
            case 19:
                p = RaspiPin.GPIO_19;
                break;
            case 20:
                p = RaspiPin.GPIO_20;
                break;
            default:
                throw new Exception("Pin ID " + pin + " is out of supported interval [0 .. 20].");
        }
        return p;
    }

}
