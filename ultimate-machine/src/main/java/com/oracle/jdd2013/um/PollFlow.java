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

import com.oracle.jdd2013.um.entities.PollEntity;
import com.oracle.jdd2013.utils.Poll;
import com.oracle.jdd2013.utils.Prompter;
import java.io.Serializable;
import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.faces.flow.Flow;
import javax.faces.flow.FlowScoped;
import javax.faces.flow.builder.FlowBuilder;
import javax.faces.flow.builder.FlowBuilderParameter;
import javax.faces.flow.builder.FlowDefinition;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

/**
 *
 * @author martinmares
 */
@Named
@FlowScoped(PollFlow.FLOW_NAME)
public class PollFlow implements Serializable {
    
    public static final String FLOW_NAME = "pollflow";
    
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private SessionId sessionId;
    
    @Inject
    private Prompter prompter;
    
    @Inject
    private UltimateMachine ultimateMachine;
    
    private Poll poll = new Poll();
    private String answer = "";
    private String answerPrompt = null;
    
    public String getQuestion() {
        return poll.getQuestion();
    }

    public String getAnswer() {
        return answer;
    }

    public String getAnswerPrompt() {
        return answerPrompt;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public void setAnswer(String answer) {
        if (answer != null) {
            answer = answer.trim();
            if (answer.length() > 50) {
                answer = answer.substring(0, 50);
            } else if (answer.length() == 0) {
                answer = null;
            }
        }
        this.answer = answer;
        this.answerPrompt = prompter.prompt(poll, answer);
    }
    
    private void bulkUpdate() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<PollEntity> q = cb.createCriteriaUpdate(PollEntity.class);
        Root<PollEntity> e = q.from(PollEntity.class);
        q.set(e.get("answer"), answer)
                .where(
                        cb.and(
                                cb.equal(e.get("question"), poll), 
                                cb.equal(e.get("sessionid"), sessionId)));
        em.createQuery(q).executeUpdate();
    }
    
    @Transactional
    public String process(boolean stopMachine) {
        if (answer != null && answer.length() > 0) {
            bulkUpdate();
            em.persist(new PollEntity(sessionId, poll, answer));
            //Update prompter
            prompter.add(poll, answer);
        }
        if (stopMachine) {
            ultimateMachine.incPoints();
        } else {
            ultimateMachine.decPoints();
        }
        return "polldone";
    }
    
    @Produces
    @FlowDefinition
    public static Flow defineFlow(@FlowBuilderParameter FlowBuilder fb) {
        fb.id("", FLOW_NAME); 
        fb.viewNode(FLOW_NAME, "/" + FLOW_NAME + "/poll.xhtml").markAsStartNode();
        fb.returnNode("cancel").fromOutcome("/index");
        fb.returnNode("stopm").fromOutcome("#{pollFlow.process(true)}");
        fb.returnNode("nostopm").fromOutcome("#{pollFlow.process(false)}");
        return fb.getFlow();
        //"#{pollFlow.process(true)}"
    }
    
}
