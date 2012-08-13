package se.su.it.svc.ldap

import gldapo.schema.annotation.GldapoNamingAttribute
import gldapo.schema.annotation.GldapoSchemaFilter

class SuRole implements Serializable
{
   static final long serialVersionUID = -687991492884005038L;
	@GldapoSchemaFilter("(objectClass=suRole)")

  @GldapoNamingAttribute
  String cn
  Set<String> roleOccupant
  String mydn

  static constraints =
  {
    cn(nullable:false)
    roleOccupant(nullable:true)
  }

    String getOu() {
        def matcher = mydn =~ "ou=([^,]*),"

        if(matcher.getCount())
        {
            String ret = matcher[0][1]
            return ret
        }

        return null
    }



    public String toString() {


        return "${ou}: ${cn}"
    }

    public boolean equals(Object obj) {
        if(obj instanceof SuRole) {
            return this.mydn.equals(obj.mydn)
        } else {
            log.warn("suRole.equals() SHOULD NOT BE REACHABLE")
            return false;
        }
    }
}
