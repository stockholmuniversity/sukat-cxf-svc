package se.su.it.svc.manager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class ApplicationContextProvider implements ApplicationContextAware {
  private static ApplicationContext ctx = null;

  public static ApplicationContext getApplicationContext() {
    return ctx;
  }

  public static void bindTxSession() {
    SessionFactory sessionFactory = (SessionFactory) ctx.getBean("sessionFactory");
    Session session = SessionFactoryUtils.getSession(sessionFactory, true);
    TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
  }

  public static void unbindTxSession() {
    SessionFactory sessionFactory = (SessionFactory) ctx.getBean("sessionFactory");
    TransactionSynchronizationManager.unbindResource(sessionFactory);
  }

  public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    // Assign the ApplicationContext into a static method
    this.ctx = ctx;
  }
}
