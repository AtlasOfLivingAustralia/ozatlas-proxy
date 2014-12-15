package au.org.ala.ozatlasproxy

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class ApiKey implements Serializable {

    String apiKey
    String name
    String description
    String dataResourceId

    static constraints = {
        apiKey unique: true, nullable: false
        dataResourceId nullable: false
    }
}
