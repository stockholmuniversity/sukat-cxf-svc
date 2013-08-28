/*
 * Copyright (c) 2013, IT Services, Stockholm University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Stockholm University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package se.su.it.svc.manager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This class handles the application context and session related things for this web service application
 */
public class ApplicationContextProvider implements ApplicationContextAware {
  private static ApplicationContext ctx = null;

  /**
   * Returns a ApplicationContext.
   *
   *
   * @return an ApplicationContext object.
   */
  public static ApplicationContext getApplicationContext() {
    return ctx;
  }
  /**
   * Bind hibernate session to current thread.
   * You need to invoke this method before any gorm related work begins on the current thread.
   *
   *
   * @return void.
   */
  public static void bindTxSession() {
    SessionFactory sessionFactory = (SessionFactory) ctx.getBean("sessionFactory");
    Session session = SessionFactoryUtils.getSession(sessionFactory, true);
    TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
  }
  /**
   * Unbind hibernate session to current thread.
   * You need to invoke this after you are finished with any gorm related work on the current thread.
   *
   *
   * @return void.
   */
  public static void unbindTxSession() {
    SessionFactory sessionFactory = (SessionFactory) ctx.getBean("sessionFactory");
    TransactionSynchronizationManager.unbindResource(sessionFactory);
  }
  /**
   * Set the applicationcontext.
   *
   *
   * @return void.
   */
  public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    // Assign the ApplicationContext into a static method
    this.ctx = ctx;
  }
}
