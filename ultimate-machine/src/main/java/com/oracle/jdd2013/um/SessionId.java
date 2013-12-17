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
package com.oracle.jdd2013.um;

import com.oracle.jdd2013.utils.JddUtils;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.StoredProcedureQuery;
import javax.transaction.Transactional;

/**
 *
 * @author martinmares
 */
@SessionScoped
@Named
public class SessionId implements Serializable {
    
    private static char[] CHRS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 
        'J', 'K', 'L', 'M', 'N', 'P', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    
    @PersistenceContext
    private EntityManager em;
    
    private int id = -1;
    private final String suffix;

    public SessionId() {
        suffix= generateSuffix();
    }
    
    public SessionId(String completeId) {
        if (completeId == null) {
            completeId = "";
        }
        int ind = completeId.indexOf('.');
        if (ind > -1) {
            int a = -1;
            try {
                a = Integer.parseInt(completeId.substring(0, ind));
            } catch (NumberFormatException ex) {
            }
            id = a;
            suffix = completeId.substring(ind + 1);
        } else {
            int a = -1;
            String b = null;
            try {
                a = Integer.parseInt(completeId);
            } catch (NumberFormatException ex) {
                b = completeId;
            }
            id = a;
            suffix = b;
        }
    }

    public SessionId(int id, String suffix) {
        this.id = id;
        this.suffix = suffix;
    }

    public int getId() {
        return id;
    }

    public String getSuffix() {
        return suffix;
    }

    @Override
    public String toString() {
        return id + "." + suffix;
    }
    
    private String generateSuffix() {
        StringBuilder result = new StringBuilder(3);
        for (int i = 0; i < 3; i++) {
            result.append(CHRS[JddUtils.random(0, CHRS.length)]);
        }
        return result.toString();
    }
    
    @PostConstruct
    @Transactional
    private void generateId() {
        if (id < 0) {
            StoredProcedureQuery query = em.createNamedStoredProcedureQuery("generateId");
            query.setParameter("seq_name", "session");
            query.execute();
            id = ((Long) query.getOutputParameterValue("nextid")).intValue();
        }
    }
    
}
