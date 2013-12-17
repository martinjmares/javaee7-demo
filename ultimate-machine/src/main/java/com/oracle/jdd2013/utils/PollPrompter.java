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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author martinmares
 */
public class PollPrompter {
    
    private static class WeightItem {
        
        public String value;
        public int weight = 1;
        public long created;

        public WeightItem(String value) {
            this.value = value;
            this.created = System.currentTimeMillis();
        }
        
        public void innerRewrite(WeightItem other) {
            this.value = other.value;
            this.weight = other.weight;
            this.created = other.created;
        }
        
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append(value).append(" - ");
            result.append(weight);
            result.append(" @").append(created);
            return result.toString();
        }
        
    }
    
    private final int promotedMaxSize;
    private final List<WeightItem> promoted;
    private final WeightItem[] yang;
    private int yangCursor = -1;
    private final String[] standards;
    private volatile List<String> fastGues = Collections.EMPTY_LIST;

    public PollPrompter(int promotedSize, int yangSize, String[] standards) {
        this.promotedMaxSize = promotedSize;
        this.promoted = new ArrayList<WeightItem>();
        this.yang = new WeightItem[yangSize];
        if (standards == null) {
            this.standards = new String[0];
        } else {
            for (int i = 0; i < standards.length; i++) {
                standards[i] = Prompter.normalize(standards[i]);
            }
            this.standards = standards;
        }
    }

    List<String> getFastGues() {
        return fastGues;
    }
    
    public void add(String value) {
        value = Prompter.normalize(value);
        if (value == null) {
            return;
        }
        for (String str : standards) {
            if (value.equals(str)) {
                return;
            }
        }
        realAdd(value);
    }

    String[] getStandards() {
        return standards;
    }
    
    private synchronized void realAdd(String value) {
        for (WeightItem wi : yang) {
            if (wi != null && wi.value.equals(value)) {
                wi.weight++;
                return;
            }
        }
        WeightItem candidate = null;
        for (WeightItem wi : promoted) {
            if (wi.value.equals(value)) {
                wi.weight++;
                return;
            }
            if ((candidate == null) ||
                (wi.weight < candidate.weight) || 
                (wi.weight == candidate.weight && wi.created < candidate.created)) {
                candidate = wi;
            }
        }
        yangCursor++;
        if (yangCursor >= yang.length) {
            yangCursor = 0;
        }
        WeightItem onCursor = yang[yangCursor];
        if (onCursor != null) {
            if (promoted.size() < promotedMaxSize) {
                promoted.add(onCursor);
            } else {
                if (candidate.weight <= onCursor.weight) {
                    candidate.innerRewrite(onCursor);
                }
            }
        }
        yang[yangCursor] = new WeightItem(value);
        List<String> l = new ArrayList<String>(promoted.size() + yang.length);
        for (WeightItem wi : yang) {
            if (wi != null) {
                l.add(wi.value);
            }
        }
        for (WeightItem wi : promoted) {
            l.add(wi.value);
        }
        this.fastGues = l;
    }
    
    public synchronized String toString() {
        StringBuilder result = new StringBuilder();
        result.append("  PROMOTED (").append(promoted.size()).append('/').append(promotedMaxSize).append("):\n");
        for (WeightItem wi : promoted) {
            result.append("      ").append(wi).append('\n');
        }
        result.append("  YANG (?/").append(yang.length).append("):\n");
        for (WeightItem wi : yang) {
            if (wi != null) {
                result.append("      ").append(wi).append('\n');
            }
        }
        return result.toString();
    }
    
}
