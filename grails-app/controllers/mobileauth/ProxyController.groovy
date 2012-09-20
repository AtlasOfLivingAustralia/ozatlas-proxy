package mobileauth

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity

class ProxyController {

  def groupService

  def image = {
      def url = ("http://bie.ala.org.au/ws/species/image/"+ params.imageType + "/" + params.guid).toURL()
      response.setContentType("image/jpeg")
      def out = response.getOutputStream()
      out.write(url.getBytes())
      out.flush()
      out.close()
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
    def url = ("http://bie.ala.org.au/ws/search.json?fq="+params.fq+"&pageSize="+params.pageSize+"&q="+params.q).toURL()
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

    // do a HTTP POST to http://bie.ala.org.au/ws/species/bulklookup with a JSON Array body
    def jsonOutput = new JsonOutput()
    def jsonBody = jsonOutput.toJson(guids)

    //do the http POST
    HttpClient http = new DefaultHttpClient()
    HttpPost post = new HttpPost("http://bie.ala.org.au/ws/species/guids/bulklookup.json")
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

  def searchByMultiRanks = {

    def jsonSlurper = new JsonSlurper()

    def orders = ("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&fq=geospatial_kosher%3Atrue&facets=order&lat=" + params.lat+ "&lon="+params.lon + "&radius="+params.radius + "&start=0&pageSize=0&flimit=867").toURL()

  //  println("Order URL : " + orders.toString())

    def familiesForAmphibians = ("http://biocache.ala.org.au/ws/occurrences/search?q=class:AMPHIBIA&fq=geospatial_kosher%3Atrue&facets=family&lat=" + params.lat+ "&lon="+params.lon + "&radius="+params.radius + "&start=0&pageSize=0").toURL()

    def classesForMolluscs = ("http://biocache.ala.org.au/ws/occurrences/search?q=phylum:MOLLUSCA&fq=geospatial_kosher%3Atrue&facets=class&lat=" + params.lat+ "&lon="+params.lon + "&radius="+params.radius + "&start=0&pageSize=0").toURL()

    def classesForCrustacea = ("http://biocache.ala.org.au/ws/occurrences/search?q=species_group:Crustaceans&fq=geospatial_kosher%3Atrue&facets=class&lat=" + params.lat+ "&lon="+params.lon + "&radius="+params.radius + "&start=0&pageSize=0").toURL()

    def speciesGroups = ("http://biocache.ala.org.au/ws/occurrences/search?q=kingdom:Plantae%20OR%20kingdom:Fungi&fq=geospatial_kosher%3Atrue&facets=species_group&lat=" + params.lat+ "&lon="+params.lon + "&radius="+params.radius + "&start=0&pageSize=0").toURL()

    println("#####Species Group URL: " + speciesGroups.toString())

    def timeit = { String message, Closure cl ->
      def startTime = System.currentTimeMillis()
      cl()
      def deltaTime = System.currentTimeMillis() - startTime
     // println "$message \ttime: $deltaTime"
    }

    def jsonOrders = null
    def jsonFamilies = null
    def jsonClasses = null
    def jsonSpeciesGroups = null
    def jsonCrustaeans = null

    timeit("get orders"){ jsonOrders = jsonSlurper.parseText(orders.getText())?.facetResults[0]?.fieldResult }
    timeit("get families") { jsonFamilies = jsonSlurper.parseText(familiesForAmphibians.getText())?.facetResults[0]?.fieldResult }
    timeit("get classes") { jsonClasses = jsonSlurper.parseText(classesForMolluscs.getText())?.facetResults[0]?.fieldResult }
    timeit("get speciesgroups") { jsonSpeciesGroups = jsonSlurper.parseText(speciesGroups.getText())?.facetResults[0]?.fieldResult }
    timeit("get crustaceans") { jsonCrustaeans = jsonSlurper.parseText(classesForCrustacea.getText())?.facetResults[0]?.fieldResult }

    //group the orders by species group
    def ordersGrouped = jsonOrders.groupBy { groupService.getTaxonToSpeciesGroup(it.label) }
    def familiesGrouped = jsonFamilies.groupBy { groupService.getTaxonToSpeciesGroup(it.label) }
    def classesGrouped = jsonClasses.groupBy { groupService.getTaxonToSpeciesGroup(it.label) }
    def crustaceanGrouped = jsonCrustaeans.groupBy { groupService.getTaxonToSpeciesGroup(it.label) }
    def groupsGrouped = jsonSpeciesGroups.groupBy { groupService.getTaxonToSpeciesGroup(it.label) }

    //render the JSON
    render(contentType: "text/json") {
      speciesGroups = array {
        ordersGrouped.each() { groupName, groupList ->
          def groupListSorted = groupList.sort { groupService.getCommonName(it.label)}
          if(groupName != null){
            speciesGroup(
              groupName: groupName ? groupName : 'Other',
              facetName: 'order',
              groups: array {
                for (group in groupListSorted) {
                  speciesGroup(
                    commonName: groupService.getCommonName(group.label),
                    scientificName: group.label,
                    recordCount: group.count
                  )
                }
              }
            )
          }
        }
        //families
        familiesGrouped.each() { groupName, groupList ->
          def groupListSorted = groupList.sort { groupService.getCommonName(it.label)}
          if(groupName != null){
            speciesGroup(
              groupName: groupName ? groupName : 'Other',
              facetName: 'family',
              groups: array {
                for (group in groupListSorted) {
                  speciesGroup(
                    commonName: groupService.getCommonName(group.label),
                    scientificName: group.label,
                    recordCount: group.count
                  )
                }
              }
            )
          }
        }
        //classes
        classesGrouped.each() { groupName, groupList ->
          def groupListSorted = groupList.sort { groupService.getCommonName(it.label)}
          if(groupName != null){
            speciesGroup(
              groupName: groupName ? groupName : 'Other',
              facetName: 'class',
              groups: array {
                for (group in groupListSorted) {
                  speciesGroup(
                    commonName: groupService.getCommonName(group.label),
                    scientificName: group.label,
                    recordCount: group.count
                  )
                }
              }
            )
          }
        }
        //classesForCrustacea
        crustaceanGrouped.each() { groupName, groupList ->
          //println("########### rendering: " + groupName)
          def groupListSorted = groupList.sort { groupService.getCommonName(it.label)}
          if(groupName != null){
            speciesGroup(
                    groupName: groupName ? groupName : 'Other',
                    facetName: 'class',
                    groups: array {
                      for (group in groupListSorted) {
                        speciesGroup(
                                commonName: groupService.getCommonName(group.label),
                                scientificName: group.label,
                                recordCount: group.count
                        )
                      }
                    }
            )
          }
        }

        //groups
        groupsGrouped.each() { groupName, groupList ->
          //println("########### rendering: " + groupName)
          def groupListSorted = groupList.sort { groupService.getCommonName(it.label)}
          if(groupName != null){
            speciesGroup(
              groupName: groupName ? groupName : 'Other',
              facetName: 'species_group',
              groups: array {
                for (group in groupListSorted) {
                  speciesGroup(
                    commonName: groupService.getCommonName(group.label),
                    scientificName: group.label,
                    recordCount: group.count
                  )
                }
              }
            )
          }
        }
      };
    }
  }
}