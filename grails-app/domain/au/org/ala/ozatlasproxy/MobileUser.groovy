package au.org.ala.ozatlasproxy

class MobileUser {

    String userName
    static hasMany = [authkeys: AuthKey]

    static constraints = {
    }
}
