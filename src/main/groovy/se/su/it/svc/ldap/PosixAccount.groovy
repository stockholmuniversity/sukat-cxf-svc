package se.su.it.svc.ldap

interface PosixAccount {
  public Set<String> getObjectClass()
  public void setLoginShell(String loginContext)
  public void setHomeDirectory(String loginContext)
  public void setGidNumber(String loginContext)
  public void setUidNumber(String loginContext)
}
