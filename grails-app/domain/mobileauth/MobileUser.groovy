package mobileauth

class MobileUser {

    String userName
    static hasMany = [authkeys: AuthKey]

    static constraints = {
    }
}
