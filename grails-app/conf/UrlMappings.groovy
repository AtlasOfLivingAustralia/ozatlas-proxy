class UrlMappings {

	static mappings = {

        "/image/$imageType/$guid"(controller: "proxy"){ action = [GET:"image"] }

		"/proxy/search.json"(controller: "proxy"){ action = [GET:"search"] }
		"/proxy/species/$id"(controller: "proxy"){ action = [GET:"species"] }

		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
