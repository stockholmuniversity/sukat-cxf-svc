package se.su.it.svc

import se.su.it.svc.commons.SvcOneTimeCodeVO

public interface OneTimeCodeService
{
    SvcOneTimeCodeVO getConfirmed(String nin, Integer days)
    SvcOneTimeCodeVO getUnconfirmed(Integer days)
}

