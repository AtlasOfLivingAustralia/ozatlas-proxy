package au.org.ala.ozatlasproxy


class ApiKeyController {
    static scaffold = ApiKey

    def save() {
        params.apiKey = UUID.randomUUID().toString()

        def apiKey = new ApiKey(params)

        apiKey.save(flush: true)

        redirect(action: "show", id: apiKey.id)
    }
}
