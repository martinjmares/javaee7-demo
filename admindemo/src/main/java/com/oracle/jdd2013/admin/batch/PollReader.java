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
package com.oracle.jdd2013.admin.batch;

import com.oracle.jdd2013.admin.PollVote;
import com.oracle.jdd2013.admin.rs.PollListReader;
import com.oracle.jdd2013.admin.rs.PollVoteReader;
import java.io.Serializable;
import javax.batch.api.chunk.ItemReader;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author martinmares
 */
@Named()
public class PollReader implements ItemReader {
    
    private int count;
    private int pointer = 1;
    
    private static Client client = ClientBuilder
            .newClient()
            .register(PollVoteReader.class)
            .register(PollListReader.class);
    
    private int getCount() {
        return client.target("http://localhost:8080/um/admin/poll/count")
                .request(MediaType.TEXT_PLAIN)
                .get(Integer.class);
    }
    
    private PollVote getOne(int id) {
        return client.target("http://localhost:8080/um/admin/poll")
                .path(String.valueOf(id))
                .request(MediaType.APPLICATION_JSON)
                .get(PollVote.class);
    }
    
    @Override
    public void open(Serializable checkpoint) throws Exception {
        this.count = getCount();
        if (checkpoint != null) {
            pointer = (Integer) checkpoint;
        }
    }

    @Override
    public Object readItem() throws Exception {
        if (pointer <= count) {
            return getOne(pointer++);
        } else {
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        return;
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return new Integer(pointer);
    }
    
}
