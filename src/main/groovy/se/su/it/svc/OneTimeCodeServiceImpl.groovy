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
     * @param days how many days should the code be valid
     *
     * @return Object with uid, password and expire
     */
    @Requires({
        nin && nin.length() == 12 && days
    })
    SvcOneTimeCodeVO getConfirmed(
            @WebParam(name = 'nin') String nin,
            @WebParam(name = 'days') Integer days
        )
    {
        def res = GeneralUtils.execHelper("getConfirmedOTC", "${nin} ${days}")

        SvcOneTimeCodeVO otcvo  = new SvcOneTimeCodeVO()
        otcvo.uid = res.uid
        otcvo.password = res.password
        otcvo.expire = res.expire

        return otcvo
    }

    /**
     * Creates an unconfirmed One Time Code.
     *
     * @param days how many days should the code be valid
     *
     * @return Object with uid, password and expire
     */
    @Requires({
        days && days <= 28
    })
    SvcOneTimeCodeVO getUnconfirmed(
            @WebParam(name = 'days') Integer days
        )
    {
        def res = GeneralUtils.execHelper("getUnconfirmedOTC", "${days}")

        SvcOneTimeCodeVO otcvo  = new SvcOneTimeCodeVO()
        otcvo.uid = res.uid
        otcvo.password = res.password
        otcvo.expire = res.expire

        return otcvo
    }
}

