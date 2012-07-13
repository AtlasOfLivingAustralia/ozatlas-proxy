class UrlMappings {

	static mappings = {

        "/image/$imageType/$guid"(controller: "proxy"){ action = [GET:"image"] }
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
