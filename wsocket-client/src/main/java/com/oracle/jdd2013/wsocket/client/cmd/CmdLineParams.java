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
package com.oracle.jdd2013.wsocket.client.cmd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jboss.weld.environment.se.bindings.Parameters;

/**
 * Super simple command line parser
 *
 * @author martinmares
 */
@Singleton
public class CmdLineParams {

    public static final String OPERAND = "OPERAND_11111";

    private final Map<String, List<String>> data = new HashMap<>();

    @Inject
    public CmdLineParams(@Parameters String[] args) {
        if (args == null) {
            return;
        }
        Collection<String> actual = null;
        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (actual != null) {
                    actual.add("true");
                }
                if (arg.startsWith("--")) {
                    arg = arg.substring(2);
                } else {
                    arg = arg.substring(1);
                }
                actual = getOrCreate(arg);
            } else {
                if (actual == null) {
                    actual = getOrCreate(OPERAND);
                }
                actual.add(arg);
                actual = null;
            }
        }
        if (actual != null) {
            actual.add("true");
        }
    }

    private List<String> getOrCreate(String key) {
        List<String> result = data.get(key);
        if (result == null) {
            result = new ArrayList<>();
            data.put(key, result);
        }
        return result;
    }
    
    public List<String> get(String key) {
        List<String> result = data.get(key);
        if (result == null || result.isEmpty()) {
            return null;
        } else {
            return Collections.unmodifiableList(result);
        }
    }
    
    public String getFirst(String key) {
        List<String> result = get(key);
        if (result != null) {
            return result.get(0);
        }
        return null;
    }
    
    public int getFirstInt(String key, int defaultValue, boolean printUserMessage) {
        try {
            return Integer.parseInt(getFirst(key));
        } catch (NumberFormatException ex) {
            if (printUserMessage) {
                System.out.println("Command line parameter --" + key + " was not specified. Using default: " + defaultValue);
            }
            return defaultValue;
        }
    }
    
    public boolean exists(String key) {
        return get(key) != null;
    }
    
//    @Produces
//    @CommandLineParameter("")
//    public Object produce(InjectionContext context) {
//        CommandLineParameter annotation = context.getAnnotatedType().getAnnotation(CommandLineParameter.class);
//        String value = annotation.value();
//        String sres = null;
//        if (value == null || value.length() == 0) {
//            sres = getFirst(OPERAND);
//        } else {
//            sres = getFirst(value);
//        }
//        context.getInjectionTarget().
//    }

}
