package au.org.ala.ozatlasproxy

import grails.converters.JSON
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.lang.time.DateUtils
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

import java.text.SimpleDateFormat

/**
 * A proxy for methods provided by other services in the Atlas.
 *
 * This proxy provides a mechanism for maintaining backwards compatibility
 * with legacy mobile applications deployed on iOS or Android.
 */
class ProxyController {

    public static final def PROJECT_ID_PARAM = "projectId"

    def grailsApplication

    def groupService, authService

    static dateFormats = ["yyyy-MM-dd", "yyyy/MM/dd", "dd MMM yyyy", "dd-MM-yyyy", "dd/MM/yyyy", "dd/MM/yy"].toArray(new String[0])

    /**
     * Handle the mapping from old -> darwin core terms.
     *
     * @param userDetails
     * @param params
     * @return
     */
    private def mapLegacyRecordParam(userDetails, params) {
        params.each { log.debug("Received params: ${it}") }
        def dateString = params.date ?: params.eventDate
        def time = params.time
        def taxonId = params.taxonID ?: params.taxonConceptID
        def taxonName = params.survey_species_search ?: params.scientificName
        def number = params.number
        def accuracyInMeters = params.accuracyInMeters ?: params.coordinateUncertaintyInMeters
        def coordinatePrecision = params.coordinatePrecision
        def imageLicence = params.imageLicence

        // Convert date to desired format
        def dateToUse = {
            try {
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = DateUtils.parseDate(dateString, dateFormats);
                dateFormatter.format(date)
            } catch (IllegalArgumentException ex) {
                log.debug("no date supplied: " + dateString)
                null
            } catch (Exception ex) {
                log.debug("invalid date format: " + dateString)
                null
            }
        }.call()

        log.debug("Retrieved user ID: " + userDetails.userId + ", for user name: ${params.userName}")
        //save the files
        def recordParams = [
                userId                       : userDetails.userId,
                eventDate                    : dateToUse,
                eventTime                    : time,
                taxonConceptID               : taxonId,
                scientificName               : taxonName,
                family                       : params.family,
                kingdom                      : params.kingdom,
                decimalLongitude             : params.longitude,
                decimalLatitude              : params.latitude,
                individualCount              : number,
                coordinateUncertaintyInMeters: accuracyInMeters,
                coordinatePrecision          : coordinatePrecision,
                imageLicence                 : imageLicence,
                commonName                   : params.commonName,
                locality                     : params.locationName,
                device                       : params.deviceName,
                devicePlatform               : params.devicePlatform,
                occurrenceRemarks            : params.notes,
                submissionMethod             : "mobile"
        ]
        recordParams
    }

    /**
     * Proxy a submit record request to the ecodata data record webservice.
     * @return
     */
    def submitRecord() {

        log.info("Request received. Proxying to ${grailsApplication.config.submitRecordUrl}")

        def userDetails = null

        //get the user Id....
        if (params.userName) {
            userDetails = authService.getUserForEmailAddress(params.userName.toLowerCase(), false)
        } else {
            response.sendError(400, "userName must be supplied")
            return
        }

        //check the user is recognised
        if (!userDetails) {
            response.sendError(400, "userName not recognised. Is the user registered ?")
            return
        }

        //create the record
        def recordParams = mapLegacyRecordParam(userDetails, params)
        if (params.apiKey) {
            recordParams.put(PROJECT_ID_PARAM, params.apiKey)
        }

        //do the http POST
        HttpClient http = new DefaultHttpClient()
        HttpPost post = new HttpPost(grailsApplication.config.submitRecordUrl)

        MultipartEntity multipartEntity = new MultipartEntity()
        multipartEntity.addPart("record", new StringBody((recordParams as JSON).toString()))

        //check for multipart request files
        if (request instanceof MultipartHttpServletRequest) {
            Map<String, MultipartFile> fileMap = request.getFileMap()
            fileMap.each { name, multipartFile ->
                ByteArrayBody body = new ByteArrayBody(multipartFile.getBytes(), multipartFile.getOriginalFilename())
                multipartEntity.addPart(name, body);
            }
        }

        //check for imageBase64
        if(params.imageBase64 && params.imageFileName){
            multipartEntity.addPart("imageBase64", new StringBody(params.imageBase64))
            multipartEntity.addPart("imageFileName", new StringBody(params.imageFileName))
        }

        //set the request payload
        post.setEntity(multipartEntity)

        //add the API key header
        post.setHeader("Authorization", grailsApplication.config.ecodata.apiKey)

        //handle response from webservice
        def queryResponse = http.execute(post)
        log.info("Query response: " + queryResponse.getStatusLine().getStatusCode())

        //set response back to client
        response.setStatus(queryResponse.getStatusLine().getStatusCode())
        render(contentType: "application/json", text: [success: queryResponse.getStatusLine().getStatusCode() == 200] as JSON)
    }

    def image = {
        def out = response.getOutputStream()
        try {
            def url = ("http://bie.ala.org.au/ws/species/image/" + params.imageType + "/" + params.guid).toURL()
            url.getBytes()
            out.write(url.getBytes())
            out.flush()
        } catch (Exception e) {
            response.sendRedirect(grailsApplication.config.grails.serverURL + "/images/noImage85.jpg")
        } finally {
            out.close()
        }
    }

    def geocode = {
        def url = ("http://maps.googleapis.com/maps/api/geocode/json?sensor=true&latlng=" + params.latlng).toURL()
        response.setContentType("application/json")
        render url.getText()
    }

    def placeSearch = {
        def url = ("http://maps.googleapis.com/maps/api/geocode/json?sensor=true&address=" + params.address.encodeAsURL()).toURL()
        response.setContentType("application/json")
        render url.getText()
    }

    def exploreGroups = {
        def url = ("http://biocache.ala.org.au/ws/explore/groups.json?fq=geospatial_kosher%3Atrue&facets=species_group&lat=" + params.lat + "&lon=" + params.lon + "&radius=" + params.radius).toURL()
        response.setContentType("application/json")
        render url.getText()
    }

    def search = {
        def url = ("http://bie.ala.org.au/ws/search.json?fq=${params.fq}&pageSize=${params.pageSize}&q=${params.q}").toURL()
        response.setContentType("application/json")
        render url.getText()
    }

    def species = {
        def url = ("http://bie.ala.org.au/ws/species/${params.id}").toURL()
        response.setContentType("application/json")
        render url.getText()
    }

    def exploreTaxonGroup = {
        def url = ("http://biocache.ala.org.au/ws/occurrence/search" + params.group + "?fq=geospatial_kosher%3Atrue&facets=species_group&lat=" + params.lat + "&lon=" + params.lon + "&radius=" + params.radius + "&start=" + params.start + "&pageSize=" + params.pageSize + "&common=" + params.common).toURL()
        response.setContentType("application/json")
        render url.getText()
    }

    def exploreGroup = {
        def url = ("http://biocache.ala.org.au/ws/explore/group/" + params.group + "?fq=geospatial_kosher%3Atrue&facets=species_group&lat=" + params.lat + "&lon=" + params.lon + "&radius=" + params.radius + "&start=" + params.start + "&pageSize=" + params.pageSize + "&common=" + params.common).toURL()
        response.setContentType("application/json")
        render url.getText()
    }

    def exploreGroupWithGallery = {
        def url = ("http://biocache.ala.org.au/ws/explore/group/" + params.group + "?fq=geospatial_kosher%3Atrue&facets=species_group&lat=" + params.lat + "&lon=" + params.lon + "&radius=" + params.radius + "&start=" + params.start + "&pageSize=" + params.pageSize + "&common=" + params.common).toURL()

        try {
            def slurper = new JsonSlurper()
            def result = slurper.parseText(url.getText())
            def guids = result.collect { x -> x.guid }

            // do a HTTP POST to http://bie.ala.org.au/species/bulklookup with a JSON Array body
            def jsonOutput = new JsonOutput()
            def jsonBody = jsonOutput.toJson(guids)

            //do the http POST
            HttpClient http = new DefaultHttpClient()
            HttpPost post = new HttpPost("http://bie.ala.org.au/species/guids/bulklookup.json")
            post.setEntity(new StringEntity(jsonBody))
            def queryResponse = http.execute(post)
            def responseAsString = null
            queryResponse.getEntity().getContent().withReader { rdr -> responseAsString = rdr.readLines().join() }

            //send to client
            response.setContentType("application/json")
            render responseAsString
        } catch (Exception e) {
            log.error("Bad request. URL:" + url.toString())
        }
    }

    def occurrenceSearch = {
        def url = ("http://biocache.ala.org.au/ws/occurrences/search?q=" + params.q + "&fq=geospatial_kosher%3Atrue&facets=" + params.facets + "&lat=" + params.lat + "&lon=" + params.lon + "&radius=" + params.radius + "&start=" + params.start + "&pageSize=" + params.pageSize + '&fsort=index&flimit=300').toURL()
        response.setContentType("application/json")
        render url.getText()
    }

    def latestImages = {
        def pageSize = params.pageSize
        def start = params.start
        if (!pageSize) pageSize = 50
        if (!start) start = 0
        def url = ("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&fq=multimedia:Image&facet=off&&sort=first_loaded_date&dir=desc&pageSize=" + pageSize + "&start=" + start).toURL()
        response.setContentType("application/json")
        render url.getText()
    }

    def exploreSubgroup = {
        def fullUrl = "http://biocache.ala.org.au/ws/occurrences/search?q=*:*&lat=" + params.lat + "&lon=" + params.lon + "&radius=" + params.radius + "&pageSize=0&facets=species_guid&flimit=2000&fq=species_subgroup:" + URLEncoder.encode(params.subgroup, "UTF-8")
        try {
            def url = (fullUrl).toURL()
            def slurper = new JsonSlurper()
            def result = slurper.parseText(url.getText())

            def speciesGroups = result.facetResults[0].fieldResult
            def names = []
            def countLookup = [:]
            speciesGroups.each {
                names << it.label
                countLookup.put(it.label, it.count)
            }

            //do the http POST
            def jsonOutput = new JsonOutput()
            def jsonBody = jsonOutput.toJson(names)
            HttpClient http = new DefaultHttpClient()
            HttpPost post = new HttpPost("http://bie.ala.org.au/species/guids/bulklookup.json")
            post.setEntity(new StringEntity(jsonBody))
            def queryResponse = http.execute(post)
            def responseAsString = null
            queryResponse.getEntity().getContent().withReader { rdr -> responseAsString = rdr.readLines().join() }

            def results = slurper.parseText(responseAsString)
            results.searchDTOList.each { it.put('recordCount', countLookup.get(it.guid)) }

            //use the bulk lookup to convert to names and text
            response.setContentType("application/json")
            render(contentType: "text/json") { results.searchDTOList }
        } catch (Exception e) {
            log.error("Bad request. URL:" + fullUrl)
        }
    }

    /**
     * Biocache query and organisation by higher level groups
     */
    def searchByMultiRanks = {
        def url = ("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&fq=geospatial_kosher%3Atrue&facets=species_subgroup&lat=" + params.lat + "&lon=" + params.lon + "&radius=" + params.radius + "&pageSize=0&flimit=1000").toURL()
        response.setContentType("application/json")
        def slurper = new JsonSlurper()
        def result = slurper.parseText(url.getText())


        def speciesGroups = []
        if (result.facetResults) {
            speciesGroups = result.facetResults[0].fieldResult
        }

        def fullResults = [:]

        speciesGroups.each { sg ->
            def group = groupService.getGroupForCommonName(sg.label.trim())

            def storedGroupTaxa = fullResults.get(group)
            if (storedGroupTaxa == null) {
                storedGroupTaxa = [sg]
                if (group != null)
                    fullResults.put(group, storedGroupTaxa)
            } else {
                storedGroupTaxa.add(sg)
            }
        }

        render(contentType: "text/json") {
            speciesGroups = array {
                fullResults.each { group, taxa ->
                    speciesGroup(
                            groupName: group.groupName,
                            facetName: group.facetName,
                            groups: array {
                                for (taxon in taxa) {
                                    def sciName = groupService.commonNameToScientificName.get(taxon.label.trim())
                                    speciesGroup(
                                            commonName: taxon.label,
                                            scientificName: sciName,
                                            recordCount: taxon.count,
                                            imageUrl: "https://m.ala.org.au/multigroupImages/" + sciName?.toLowerCase() + ".jpg",
                                    )
                                }
                            }
                    )
                }
            }
        }
    }

    private def getDataResourceId(apiKey) {
        def key = ApiKey.findByApiKey(apiKey)

        def drId = null
        if (key) {
            drId = key.getDataResourceId()
            log.info "Data Resource UID ${drId} found for API key ${apiKey}"
        } else {
            log.warn "Unrecognised API Key ${apiKey}"
        }

        drId
    }
}