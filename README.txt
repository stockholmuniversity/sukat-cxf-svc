This project will produce a runnable war.
It depends on cxf-svc-server-XXXX.jar.
To generate the war, do as follow:

mvn assembly:assembly

The war file will appear in the root directory


Notes:
To start:
sudo java -DDEBUG -Djava.security.krb5.conf=/etc/krb5.conf -Djava.security.auth.login.config=/etc/spnego.conf -Djavax.security.auth.useSubjectCredsOnly=false -Dorg.eclipse.jetty.util.log.DEBUG=true -Dsun.security.spnego.debug=all -Djava.security.krb5.kdc=kerberos.su.se -Djava.security.krb5.realm=SU.SE -jar cxf-svc-skeleton.war

/etc/krb5.conf (You need to have a valid krb5.conf in /etc)

/etc/spnego.conf file (There is an example in the repo at etc/spnego.conf. Modify to reflect your keytab. The spnego.conf needs to be in /etc) :
spnego conf is used used for kerberos authentication when receiving requests (com.sun.security.jgss.accept) and when doing LDAP (GLDAPO) queries (com.sun.security.jgss.initiat).
OBSERVE: BOTH CREDENTIALS NEED TO BE IN THE SAME KEYTAB

com.sun.security.jgss.accept {
	com.sun.security.auth.module.Krb5LoginModule required
	principal="HTTP/jqvarlocal.it.su.se@SU.SE"
	useKeyTab=true
	keyTab="/etc/krb5.keytab-cxf-svc"
	storeKey=true
	debug=true
	doNotPrompt=true;
};

com.sun.security.jgss.initiate {
        com.sun.security.auth.module.Krb5LoginModule required
        principal="cxf-svc-test@SU.SE"
        keyTab="/etc/krb5.keytab-cxf-svc"
        useKeyTab=true
        storeKey=true
        debug=true
        doNotPrompt=true;
};

spnego.properties (Needs to be in the current dir when you start the webapp. You can find the spnego.properties file in the root of the repo, modify it to reflect your keytab.)

SSL Konfiguration:
Java Keystore skall ligga i current directory och ha namnet "cxf-svc-server.keystore"
Bygg alltid java keystore med password "changeit"

GORM:

Gå in i beans-groovy.xml och ändra db connectivity settings


GLDAPO:

Se spnego.conf ovan.