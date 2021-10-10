package fr.overrride.scs.encryption

import org.yaml.snakeyaml.Yaml

import java.io.{InputStream, OutputStream}
import java.util

/**
 * Represents a user's organisation
 * */
case class UserOrganization(name: String,
                            organizationalUnit: String,
                            organization: String,
                            city: String,
                            state: String,
                            countryCode: String) {

    /**
     * Writes the UserOrganization fields in the output stream
     * */
    def writeData(out: OutputStream): Unit = {
        def write(str: String): Unit = {
            out.write((str + "\n").getBytes())
        }

        write(name)
        write(organizationalUnit)
        write(organization)
        write(city)
        write(state)
        write(countryCode)
        write("y")
        out.flush()
    }

}

object UserOrganization {

    private final val Yml = new Yaml()

    /**
     * Creates a [[UserOrganization]] object from yaml
     * */
    def fromYaml(inputStream: InputStream): UserOrganization = {
        val map = Yml.load[util.HashMap[String, String]](inputStream)
        import map.get
        val name               = get("name")
        val organizationalUnit = get("organizationalUnit")
        val organization       = get("organization")
        val city               = get("city")
        val state              = get("state")
        val countryCode        = get("countryCode")
        UserOrganization(name, organizationalUnit, organization, city, state, countryCode)
    }
}