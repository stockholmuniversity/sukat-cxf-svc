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
