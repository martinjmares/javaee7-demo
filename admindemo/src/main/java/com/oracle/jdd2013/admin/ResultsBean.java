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
package com.oracle.jdd2013.admin;

import com.oracle.jdd2013.admin.rs.PollListReader;
import com.oracle.jdd2013.admin.rs.PollVoteReader;
import com.oracle.jdd2013.utils.JddUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author martinmares
 */
@Named
@SessionScoped
public class ResultsBean implements Serializable {

    public static class Statistics implements Serializable {
        
        private int count = 1;
        private final int poll;
        private final String name;
        private final Collection<String> alternatives = new ArrayList<String>();

        public Statistics(int poll, String name) {
            this.name = name;
            this.poll = poll;
        }

        public int getCount() {
            return count;
        }

        public int getPoll() {
            return poll;
        }

        public String getName() {
            return name;
        }
        
        public void inc() {
            count++;
        }
        
        public boolean contains(String str) {
            if (str == null || str.length() == 0) {
                return false;
            }
            if (str.equalsIgnoreCase(name)) {
                return true;
            }
            for (String alt : alternatives) {
                if (str.equalsIgnoreCase(alt)) {
                    return true;
                }
            }
            return false;
        }
        
        public boolean containsExact(String str) {
            if (str == null || str.length() == 0) {
                return false;
            }
            if (str.equalsIgnoreCase(name)) {
                return true;
            }
            for (String alt : alternatives) {
                if (str.equalsIgnoreCase(alt)) {
                    return true;
                }
            }
            return false;
        }
        
        public void addAlternative(String str) {
            alternatives.add(str);
        }
        
        public String getAlternativesStr() {
            StringBuilder result = new StringBuilder();
            for (String alt : alternatives) {
                if (result.length() > 0) {
                    result.append(", ");
                }
                result.append(alt);
                if (result.length() > 100) {
                    return result.toString() + "...";
                }
            }
            return result.toString();
        }
        
    }
    
    public enum Status {
        IDLE, STARTING, PROCESSING
    }
    
    @Resource
    ManagedExecutorService executor;
    
    private Collection<Statistics> stats = new ArrayList<Statistics>();
    private volatile Status status = Status.IDLE;
    
    private void recompute() {
        status = Status.PROCESSING;
        synchronized (stats) {
            stats.clear();
        }
        List<PollVote> votes = selectAll();
        for (PollVote vote : votes) {
            String normAnswer = JddUtils.normalizeString(vote.getAnswer());
            Statistics s = null;
            for (Statistics stat : stats) {
                if (JddUtils.almostEquals(normAnswer, stat.getName())) {
                    s = stat;
                    break;
                }
            }
            if (s == null) {
                s = new Statistics(vote.getPoll(), vote.getAnswer());
                synchronized (stats) {
                    stats.add(s);
                }
            } else {
                s.inc();
                if (!s.containsExact(vote.getAnswer())) {
                    s.addAlternative(vote.getAnswer());
                }
            }
        }
        status = Status.IDLE;
    }
    
    public String startRecomputing() {
        status = Status.STARTING;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                recompute();
            }
        });
        return "resultlist";
    }

    public Status getStatus() {
        return status;
    }
    
    public Collection<Statistics> getStatistics() {
        Collection<Statistics> result = new TreeSet<>(new Comparator<Statistics>() {
                @Override
                public int compare(Statistics o1, Statistics o2) {
                    int result = o1.poll - o2.poll;
                    if (result == 0) {
                        return o2.count - o1.count;
                    }
                    return result;
                }
            });
        synchronized (stats) {
            result.addAll(stats);
        }
        return result;
    }
    
    
    
    
    private List selectAll() {
        return client.target("http://um.martin-jacek-mares.cloudbees.net/um/admin/poll")
                .request(MediaType.APPLICATION_JSON)
                .get(List.class);
    }
    
    private static Client client = ClientBuilder
            .newClient()
            .register(PollVoteReader.class)
            .register(PollListReader.class);
    
}
