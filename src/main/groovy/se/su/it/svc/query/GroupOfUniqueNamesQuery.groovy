package se.su.it.svc.query

import groovy.util.logging.Slf4j
import se.su.it.svc.ldap.GroupOfUniqueNames

@Slf4j
public class GroupOfUniqueNamesQuery
{
    /**
     * Find group by mailLocalAddress
     *
     * @param directory RW or RO LDAP-directory
     * @param email email address
     *
     * @return GroupOfUniqueNames object.
     */
    static GroupOfUniqueNames findByMailLocalAddress(String directory, String email)
    {
        def group = GroupOfUniqueNames.find(directory: directory) {
            eq('mailLocalAddress', email)
        }

        return group
    }
}
