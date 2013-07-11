package se.su.it.svc.util

class GeneralUtils {
  /**
   * Transform any 12 char pnr to 10 char for use in finding by socialSecurityNumber
   * @param pnr
   * @return
   */
  public static String pnrToSsn(String pnr) {
    return (pnr?.length() == 12) ? pnr[2..11] : pnr
  }
}
