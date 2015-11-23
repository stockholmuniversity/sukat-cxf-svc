package se.su.it.svc

import groovy.util.logging.Slf4j

import javax.jws.WebParam
import javax.jws.WebService

import org.gcontracts.annotations.Requires

import se.su.it.svc.commons.SvcOneTimeCodeVO

import se.su.it.svc.server.annotations.AuthzRole

import se.su.it.svc.util.GeneralUtils

@WebService @Slf4j
@AuthzRole(role = "sukat-otc-admin")
public class OneTimeCodeServiceImpl implements OneTimeCodeService
{
    /**
     * Creates a confirmed One Time Code.
     *
     * @param nin 12 character national identification number
     *
     * @return Object with uid, password and expire
     */
    @Requires({
        nin && nin.length() == 12
    })
    SvcOneTimeCodeVO getConfirmed(
            @WebParam(name = 'nin') String nin
        )
    {
        def res = GeneralUtils.execHelper("getConfirmedOTC", nin)

        SvcOneTimeCodeVO otcvo  = new SvcOneTimeCodeVO()
        otcvo.uid = res.uid
        otcvo.password = res.password
        otcvo.expire = res.expire

        return otcvo
    }
}

