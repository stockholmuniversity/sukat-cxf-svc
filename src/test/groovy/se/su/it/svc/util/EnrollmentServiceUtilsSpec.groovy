package se.su.it.svc.util

import spock.lang.Specification
import spock.lang.Unroll

class EnrollmentServiceUtilsSpec extends Specification {

  @Unroll
  def "getHomeDirectoryPath should generate homedir='#dir' for uid='#uid'"() {
    expect: EnrollmentServiceUtils.getHomeDirectoryPath(uid) == dir

    where:
    uid   | dir
    null  | null
    ''    | null
    'a'   | null // To short
    'aa'  | EnrollmentServiceUtils.AFS_HOME_DIR_BASE + 'a/a/aa'
    'aaa' | EnrollmentServiceUtils.AFS_HOME_DIR_BASE + 'a/a/aaa'
    'abc' | EnrollmentServiceUtils.AFS_HOME_DIR_BASE + 'a/b/abc'
  }
}
