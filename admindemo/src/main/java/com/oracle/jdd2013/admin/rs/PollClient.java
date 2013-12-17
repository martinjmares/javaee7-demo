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
package com.oracle.jdd2013.admin.rs;

import com.oracle.jdd2013.admin.Config;
import com.oracle.jdd2013.admin.PollVote;
import com.oracle.jdd2013.utils.SimpleSec;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author martinmares
 */
@Named
@SessionScoped
public class PollClient implements Serializable {
    
    @Inject
    private Config config;
    
    private static Client client = ClientBuilder
            .newClient()
            .register(PollVoteReader.class)
            .register(PollListReader.class);
    
    private WebTarget getTarget() {
        client.getConfiguration();
        return client.target(config.getUrl()).path("poll");
    }
    
    public int getCount() {
        return getTarget()
                .path("count")
                .request(MediaType.TEXT_PLAIN)
                .get(Integer.class);
    }
    
    public List getList() {
        return getTarget()
                .request(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public List getSubList(int pollId) {
        return getTarget()
                .queryParam("pollid", pollId)
                .request(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    public PollVote get(int id) {
        return getTarget()
                .path(String.valueOf(id))
                .request(MediaType.APPLICATION_JSON)
                .get(PollVote.class);
    }
    
    public PollVote getSecure(int id) {
        return getTarget()
                .path(String.valueOf(id))
                .request(MediaType.APPLICATION_JSON)
                .header(SimpleSec.HEADER_NAME, SimpleSec.HEADER_VALUE)
                .get(PollVote.class);
    }
    
}
