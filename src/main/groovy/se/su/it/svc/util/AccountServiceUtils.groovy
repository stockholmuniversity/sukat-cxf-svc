package se.su.it.svc.util

class AccountServiceUtils {
  public static String domainToDN(String domain) {
    String retString = ""
    def domainSplit = domain.split("\\.")
    domainSplit.eachWithIndex {String name, int index ->
      if(index > 0) {retString += ",dc=${name}"}
      else {retString += "dc=${name}"}
    }
    return retString
  }
}
