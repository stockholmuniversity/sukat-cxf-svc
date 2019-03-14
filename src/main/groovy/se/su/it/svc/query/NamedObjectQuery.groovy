package se.su.it.svc.query

import groovy.util.logging.Slf4j
import se.su.it.svc.ldap.NamedObject

@Slf4j
public class NamedObjectQuery
{
    /**
     * Find named object by mailLocalAddress
     *
     * @param directory RW or RO LDAP-directory
     * @param email email address
     *
     * @return NamedObject object.
     */
    static NamedObject findByMailLocalAddress(String directory, String email)
    {
        def no = NamedObject.find(directory: directory) {
            eq('mailLocalAddress', email)
        }

        return no
    }
}
