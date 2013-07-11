import org.junit.Test
import se.su.it.svc.util.GeneralUtils
import spock.lang.Specification
import spock.lang.Unroll

class GeneralUtilsSpec extends Specification {
  @Test
  @Unroll
  void "pnrToSsn: When given pnr: \'#pnr\' we expect '\'#expected\'"() {
    expect:
    GeneralUtils.pnrToSsn(pnr)

    where:
    pnr             | expected
    '***********'   | '***********'   // 11 chars, nothing happens.
    '++**********'  | '*********'     // 12 chars, first 2 chars should be cut.
    '++***********' | '++***********' // 13 chars, nothing happens.
  }

}
