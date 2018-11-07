This project will produce a runnable war.
It depends on cxf-svc-server-XXXX.jar.
To generate the war, do as follow:

./mvnw assembly:assembly

The war file will appear in the root directory


Notes:
To start:
sudo java -Dconfig.properties=/local/cxf-server/conf/config.properties -Dlog.file=/local/cxf-server/logs/sukat-svc-application.log -jar sukat-cxf-svc-executable.war

To start with DEBUGGING:
sudo java -DDEBUG -Dconfig.properties=/local/cxf-server/conf/config.properties -Dlog.file=/local/cxf-server/logs/sukat-svc-application.log -Dorg.eclipse.jetty.util.log.DEBUG=true -Dsun.security.spnego.debug=all -jar sukat-cxf-svc-executable.war

/etc/krb5.conf (You need to have a valid krb5.conf in /etc)

/etc/spnego.conf file (There is an example in the repo at etc/spnego.conf.in. Modify to reflect your keytab. The spnego.conf needs to be specified in the config.properties file see config.properties.in) :
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

spnego.properties (Needs to be specified in the config.properties file. You can find an example spnego.properties.in file in the root of the repo, modify it to reflect your keytab.)

SSL Konfiguration:
Java Keystore skall ligga i current directory och ha namnet "cxf-svc-server.keystore"
Bygg alltid java keystore med password "changeit"

GORM:

Gå in i beans-groovy.xml och ändra db connectivity settings


GLDAPO:

Se spnego.conf ovan.

Create new release:

Run the create_release script in ci-scripts dir:

Automatically increment minor version (no argument to script needed):
$ ci-scripts/create_release

Manually specify a release version:
$ ci-scripts/create_release 1.2.0

