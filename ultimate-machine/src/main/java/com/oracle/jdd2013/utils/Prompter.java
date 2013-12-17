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
package com.oracle.jdd2013.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author martinmares
 */
@ApplicationScoped
public class Prompter {
    
    private final int SIZE_PROMOTED = 4;
    private final int SIZE_YANG = 3;
    private final String[][] FIXED_ANSWERS = new String[][] {
        {"kill bill", "nespoutany django", "hanbny pancharti", "grindhouse", "auto zabijak", "sin city", "jackie brown", "ctyri pokoje", "pulp fiction", "gauneri", "od soumraku do usvitu"},
        {"java", "clojure", "fantom", "groovy", "jruby", "jython", "scala"},
        {"glassfish", "jboss", "jonas", "geronimo", "tomcat", "websphere", "weblogic", "wildfly"}
    };
    
    private final Map<Integer, PollPrompter> prompters = new HashMap<Integer, PollPrompter>();
    
    private PollPrompter initPollPrompter(Poll poll) {
        if (FIXED_ANSWERS.length > poll.getId()) {
            return new PollPrompter(SIZE_PROMOTED, SIZE_YANG, FIXED_ANSWERS[poll.getId()]);
        } else {
            return new PollPrompter(SIZE_PROMOTED, SIZE_YANG, null);
        }
    }
    
    private PollPrompter getPollPrompter(Poll poll) {
        PollPrompter result = prompters.get(poll.getId());
        if (result == null) {
            synchronized(this) {
                result = prompters.get(poll.getId());
                if (result == null) {
                    result = initPollPrompter(poll);
                    prompters.put(poll.getId(), result);
                }
            }
        }
        return result;
    }
    
    static String normalize(String str) {
        if (str == null) {
            return null;
        }
        str = str.trim();
        if (str.length() == 0) {
            return null;
        }
        str = JddUtils.normalize(str.toLowerCase());
        return str;
    }
    
    public String prompt(Poll poll, String baseValue) {
        baseValue = normalize(baseValue);
        if (baseValue == null) {
            return null;
        }
        //Process
        String result = null;
        int resultDistance = Integer.MAX_VALUE;
        PollPrompter pp = getPollPrompter(poll);
        String[] standards = pp.getStandards();
        for (String ps : standards) {
            int dist = JddUtils.editDistance(baseValue, ps);
            if (dist == 0) {
                return null;
            } else if (dist < resultDistance) {
                resultDistance = dist;
                result = ps;
            }
        }
        List<String> posib = pp.getFastGues();
        for (String ps : posib) {
            int dist = JddUtils.editDistance(baseValue, ps);
            if (dist == 0) {
                return null;
            } else if (dist < resultDistance) {
                resultDistance = dist;
                result = ps;
            }
        }
        //Return result;
        if (resultDistance <= (baseValue.length() / 2)) {
            return result;
        } else {
            return null;
        }
    }
    
    public void add(Poll poll, String value) {
        value = normalize(value);
        if (value == null) {
            return;
        }
        PollPrompter pp = getPollPrompter(poll);
        pp.add(value);
    }
    
}
