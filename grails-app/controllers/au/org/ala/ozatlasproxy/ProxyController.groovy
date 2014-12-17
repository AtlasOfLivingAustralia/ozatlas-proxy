package au.org.ala.ozatlasproxy
import grails.converters.JSON
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
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

class ProxyController {

  public static final def API_KEY_PARAM = "apiKey"
  public static final def DATA_RESOURCE_ID_PARAM = "dataResourceUid"

  def grailsApplication

  def groupService

  def submitRecord(){
    log.info("Request received. Proxying to fielddata")
    def parameterMap = request.getParameterMap()

    if (parameterMap[API_KEY_PARAM]) {
        def drId = getDataResourceId(parameterMap[API_KEY_PARAM])
        if (drId) {
            parameterMap << [(DATA_RESOURCE_ID_PARAM): drId]
        }
    }

    //do the http POST
    HttpClient http = new DefaultHttpClient()
    HttpPost post = new HttpPost(grailsApplication.config.submitRecordUrl)
    def nameValuePairs = new ArrayList<NameValuePair>();
    parameterMap.each {k, v ->
        if (v) {
            log.debug "Params: ${k}:${v[0]}"
            nameValuePairs.add(new BasicNameValuePair(k, v[0]))
        }
    }
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

    def queryResponse = http.execute(post)
    log.info("Query response: " + queryResponse.getStatusLine().getStatusCode())
    response.setStatus(queryResponse.getStatusLine().getStatusCode())
    render (contentType: "application/json", text: [success: queryResponse.getStatusLine().getStatusCode() == 200] as JSON)
  }

  def submitRecordMultipart(){

    log.info("Multipart request received. Proxying to fielddata")
    HttpPost post = new HttpPost(grailsApplication.config.submitMultipartRecordUrl);

    def parameterMap = request.getParameterMap()

    if (parameterMap[API_KEY_PARAM]) {
        def drId = getDataResourceId(parameterMap[API_KEY_PARAM])
        if (drId) {
            parameterMap << [(DATA_RESOURCE_ID_PARAM): drId]
        }
    }

    def nameValuePairs = new ArrayList<NameValuePair>();
    parameterMap.each {k, v ->
        if (v) {
            log.debug "Params: ${k}:${v[0]}"
            nameValuePairs.add(new BasicNameValuePair(k, v[0]))
        }
    }

    if(request instanceof MultipartHttpServletRequest){
        MultipartEntity entity = new MultipartEntity()
        nameValuePairs.each {it ->
            entity.addPart(it.name, new StringBody(it.value))
        }
        Map<String, MultipartFile> fileMap = request.getFileMap()
        if (fileMap.containsKey("attribute_file_1")) {

            MultipartFile multipartFile = fileMap.get("attribute_file_1")
            ByteArrayBody body = new ByteArrayBody(multipartFile.getBytes(), multipartFile.getOriginalFilename())
            entity.addPart("attribute_file_1", body);
            post.setEntity(entity);
        }
    }
    else {
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    }

    HttpClient client = new DefaultHttpClient();
    HttpResponse httpResponse = null;
    try {
        httpResponse = client.execute(post);
        response.setStatus(httpResponse.getStatusLine().getStatusCode())
        render (contentType: "application/json", text: [success: httpResponse.getStatusLine().getStatusCode() == 200] as JSON)
    } catch (Exception e) {
        log.error(e.getMessage(), e)
        response.setStatus(500)
        render (contentType: "application/json", text: [success: false] as JSON)
    }
  }

  def image = {
      def out = response.getOutputStream()
      try {
        def url = ("http://bie.ala.org.au/ws/species/image/"+ params.imageType + "/" + params.guid).toURL()
        url.getBytes()
        out.write(url.getBytes())
        out.flush()
      } catch (Exception e){
//        out.write("http://bie.ala.org.au/images/noImage85.jpg".toURL().getBytes())
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
    def url = ("http://biocache.ala.org.au/ws/explore/groups.json?fq=geospatial_kosher%3Atrue&facets=species_group&lat=" + params.lat+ "&lon="+params.lon + "&radius="+params.radius).toURL()
    response.setContentType("application/json")
    render url.getText()
  }

  def search = {
    def url = ("http://bie.ala.org.au/search.json?fq="+params.fq+"&pageSize="+params.pageSize+"&q="+params.q).toURL()
    response.setContentType("application/json")
    render url.getText()
  }

  def exploreTaxonGroup = {
    def url = ("http://biocache.ala.org.au/ws/occurrence/search"+params.group+"?fq=geospatial_kosher%3Atrue&facets=species_group&lat=" + params.lat+ "&lon="+params.lon + "&radius="+params.radius + "&start="+ params.start + "&pageSize="+params.pageSize + "&common="+params.common).toURL()
    response.setContentType("application/json")
    render url.getText()
  }

  def exploreGroup = {
    def url = ("http://biocache.ala.org.au/ws/explore/group/"+params.group+"?fq=geospatial_kosher%3Atrue&facets=species_group&lat=" + params.lat+ "&lon="+params.lon + "&radius="+params.radius + "&start="+ params.start + "&pageSize="+params.pageSize + "&common="+params.common).toURL()
    response.setContentType("application/json")
    render url.getText()
  }

  def exploreGroupWithGallery = {
    def url = ("http://biocache.ala.org.au/ws/explore/group/"+params.group+"?fq=geospatial_kosher%3Atrue&facets=species_group&lat=" + params.lat+ "&lon="+params.lon + "&radius="+params.radius + "&start="+ params.start + "&pageSize="+params.pageSize + "&common="+params.common).toURL()

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
  }

  def occurrenceSearch = {
    def url = ("http://biocache.ala.org.au/ws/occurrences/search?q="+params.q+"&fq=geospatial_kosher%3Atrue&facets="+params.facets+"&lat=" + params.lat+ "&lon="+params.lon + "&radius="+params.radius + "&start="+ params.start + "&pageSize="+params.pageSize + '&fsort=index&flimit=300').toURL()
    response.setContentType("application/json")
    render url.getText()
  }

  def latestImages = {
    def pageSize = params.pageSize
    def start = params.start
    if (!pageSize) pageSize = 50
    if (!start) start = 0
    def url = ("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&fq=multimedia:Image&facet=off&&sort=first_loaded_date&dir=desc&pageSize="+pageSize+"&start="+start).toURL()
    response.setContentType("application/json")
    render url.getText()
  }

  def exploreSubgroup = {
    def fullUrl = "http://biocache.ala.org.au/ws/occurrences/search?q=*:*&lat=" + params.lat+ "&lon=" + params.lon + "&radius=" + params.radius + "&pageSize=0&facets=species_guid&flimit=2000&fq=species_subgroup:" + URLEncoder.encode(params.subgroup, "UTF-8")
    log.debug(fullUrl)
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
    results.searchDTOList.each { it.put('recordCount',countLookup.get(it.guid)) }

    //use the bulk lookup to convert to names and text
    response.setContentType("application/json")
    render(contentType: "text/json") { results.searchDTOList }
  }

  /**
   * Biocache query and organisation by higher level groups
   */
  def searchByMultiRanks = {
    def url = ("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&fq=geospatial_kosher%3Atrue&facets=species_subgroup&lat=" + params.lat+ "&lon=" + params.lon + "&radius=" + params.radius + "&pageSize=0&flimit=1000").toURL()
    response.setContentType("application/json")
    def slurper = new JsonSlurper()
    def result = slurper.parseText(url.getText())
    def speciesGroups = []
    if(result.facetResults){
        speciesGroups = result.facetResults[0].fieldResult
    }

    def fullResults = [:]

    speciesGroups.each { sg ->
        def group = groupService.getGroupForCommonName(sg.label.trim())

        def storedGroupTaxa = fullResults.get(group)
        if (storedGroupTaxa == null){
            storedGroupTaxa = [sg]
            if(group != null)
                fullResults.put(group, storedGroupTaxa)
        } else {
           storedGroupTaxa.add(sg)
        }
    }

    render(contentType: "text/json") {
      speciesGroups = array {
            fullResults.each { group, taxa ->
              speciesGroup (
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