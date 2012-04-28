package mobileauth

class AuthKey {

    MobileUser mobileUser
    String key
    
    static constraints = {}
	
	static mapping = {
	    key column: "auth_key"
	}
}
