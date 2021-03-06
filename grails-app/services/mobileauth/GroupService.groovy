package mobileauth

import groovy.json.JsonSlurper
import org.springframework.beans.factory.InitializingBean

class GroupService implements InitializingBean {

  def serviceMethod() {}

  def groupMap = [:]
  def scientificNameToCommonName = [:]
  def commonNameToScientificName = [:]
  def commonNameToGroup = [:]

  public void afterPropertiesSet() throws Exception {
      speciesGroups.each { group ->
        log.debug("Groups initialising: " + group.speciesGroup)
        def facetName = group.taxonRank ? group.taxonRank : group.facetName
        Group newGroup = new Group([groupName:group.speciesGroup, facetName:facetName])
        group.taxa.each { taxon ->
          log.debug(taxon.name + " => " + taxon.common)
          groupMap[taxon.name.trim().toLowerCase()] = group.speciesGroup.trim()
          scientificNameToCommonName[taxon.name.trim().toLowerCase()] = taxon.common.trim()
          commonNameToScientificName[taxon.common.trim()] = taxon.name.trim()
          commonNameToGroup.put(taxon.common.trim(), newGroup)
        }

      }
  }

  def getTaxonToSpeciesGroup(String taxonName) {
    groupMap.get(taxonName.toLowerCase())
  }

  def getGroupForCommonName(String commonName){
      commonNameToGroup.get(commonName.trim())
  }

  def getCommonName(String taxonName){
    scientificNameToCommonName.get(taxonName.toLowerCase())
  }

  def speciesGroups = (new JsonSlurper()).parseText("""[{
"speciesGroup":"Mammals",
"taxonRank":"order",
"taxa" : [
{"name":"DASYUROMORPHIA","common":"Marsupials, Dasyuroid & Carnivores"},
{"name":"DIPROTODONTIA","common":"Diprotodont Marsupials"},
{"name":"NOTORYCTEMORPHIA","common":"Marsupial Moles"},
{"name":"PERAMELEMORPHIA","common":"Bandicoots, Bilbies"},
{"name":"MONOTREMATA","common":"Monotremes"},
{"name":"ARTIODACTYLA","common":"Cloven-hoofed Ungulates"},
{"name":"CARNIVORA","common":"Carnivores"},
{"name":"CETACEA","common":"Dolphins, Porpoises, Whales"},
{"name":"CHIROPTERA","common":"Bats"},
{"name":"INSECTIVORA","common":"Shrews"},
{"name":"LAGOMORPHA","common":"Hares, Pikas, Rabbits"},
{"name":"PERRISODACTYLA","common":"Horses"},
{"name":"RODENTIA","common":"Rodents"},
{"name":"SIRENIA","common":"Dugongs, Manatees, Sea Cows"}
]
},
{
"speciesGroup":"Birds",
"taxonRank":"order",
"taxa" : [
{"name":"ANSERIFORMES","common":"Ducks, Geese"},
{"name":"APODIFORMES","common":"Hummingbirds, Swifts"},
{"name":"CAPRIMULGIFORMES","common":"Nightjars, Frogmouths"},
{"name":"CHARADRIIFORMES","common":"Coursers, Thick-knees"},
{"name":"CICONIIFORMES","common":"Bitterns, Ibises"},
{"name":"COLUMBIFORMES","common":"Doves"},
{"name":"CORACIIFORMES","common":"Kingfishers"},
{"name":"CUCULIFORMES","common":"Cuckoos"},
{"name":"FALCONIFORMES","common":"Falcons"},
{"name":"GALLIFORMES","common":"Fowls"},
{"name":"GRUIFORMES","common":"Cranes"},
{"name":"PASSERIFORMES","common":"Perching Birds"},
{"name":"PELECANIFORMES","common":"Large waterbirds"},
{"name":"PHOENICOPTERIFORMES","common":"Flamingos"},
{"name":"PODICIPEDIFORMES","common":"Grebes"},
{"name":"PROCELLARIIFORMES","common":"Petrels, Fulmars"},
{"name":"PSITTACIFORMES","common":"Parrots"},
{"name":"SPHENISCIFORMES","common":"Penguins"},
{"name":"STRIGIFORMES","common":"Owls"},
{"name":"STRUTHIONIFORMES","common":"Ostriches"},
{"name":"TURNICIFORMES","common":"Buttonquails"}
]
},
{
"speciesGroup":"Insects",
"taxonRank":"order",
"taxa" : [
{"name":"ARCHAEOGNATHA","common":"Bristletails"},
{"name":"BLATTODEA","common":"Cockroaches"},
{"name":"COLEOPTERA","common":"Beetles"},
{"name":"DERMAPTERA","common":"Earwigs"},
{"name":"DIPTERA","common":"Flies"},
{"name":"EMBIOPTERA","common":"Web-spinners"},
{"name":"EPHEMEROPTERA","common":"Mayflies"},
{"name":"HEMIPTERA","common":"Aphids"},
{"name":"HYMENOPTERA","common":"Ants"},
{"name":"ISOPTERA","common":"Termites"},
{"name":"LEPIDOPTERA","common":"Butterflies, Moths"},
{"name":"MANTODEA","common":"Mantids"},
{"name":"MECOPTERA","common":"Scorpion-flies"},
{"name":"MEGALOPTERA","common":"Alderflies"},
{"name":"NEUROPTERA","common":"Ant-lions"},
{"name":"ODONATA","common":"Dragonflies"},
{"name":"ORTHOPTERA","common":"Grasshoppers"},
{"name":"PHASMIDA","common":"Stick Insects"},
{"name":"PHTHIRAPTERA","common":"Lice"},
{"name":"PLECOPTERA","common":"Stoneflies"},
{"name":"PSOCOPTERA","common":"Booklice"},
{"name":"SIPHONAPTERA","common":"Fleas"},
{"name":"STREPSIPTERA","common":"Strepsipterans"},
{"name":"THYSANOPTERA","common":"Thrips"},
{"name":"TRICHOPTERA","common":"Caddisflies"},
{"name":"ZORAPTERA","common":"Zorapterans"},
{"name":"ZYGENTOMA","common":"Silverfish"}
]
},
{
"speciesGroup":"Amphibians",
"taxonRank":"family",
"taxa" : [
{"name":"BUFONIDAE","common":"Bufonids"},
{"name":"HYLIDAE","common":"Hylids"},
{"name":"MICROHYLIDAE","common":"Microhylid Frogs"},
{"name":"MYOBATRACHIDAE","common":"Australian Frogs"},
{"name":"RANIDAE","common":"Ranid Frogs"}
]
},
{
"speciesGroup":"Reptiles",
"taxonRank":"order",
"taxa" : [
{"name":"CROCODYLIA","common":"Crocodiles"},
{"name":"SQUAMATA","common":"Lizards, Snakes"},
{"name":"TESTUDINES","common":"Tortoises, Turtles"}
]
},
{
"speciesGroup":"Fish",
"taxonRank":"order",
"taxa" : [
{"name":"MYXINIFORMES","common": "Hagfishes"},
{"name":"CARCHARHINIFORMES","common":"Ground Sharks"},
{"name":"HETERODONTIFORMES","common":"Bullhead Sharks"},
{"name":"HEXANCHIFORMES","common":"Cow Sharks"},
{"name":"LAMNIFORMES","common":"Mackerel Sharks"},
{"name":"MYLIOBATIFORMES","common":"Batoids"},
{"name":"ORECTOLOBIFORMES","common":"Carpet Sharks"},
{"name":"PRISTIFORMES","common":"Sawfish"},
{"name":"PRISTIOPHORIFORMES","common":"Saw Sharks"},
{"name":"RAJIFORMES","common":"Softnose Skates"},
{"name":"RHINOBATIFORMES","common":"Guitarfish"},
{"name":"SQUALIFORMES","common":"Dogfish Sharks"},
{"name":"SQUATINIFORMES","common":"Angel Sharks"},
{"name":"TORPEDINIFORMES","common":"Electric Rays"},
{"name":"CHIMAERIFORMES","common":"Chimaeras"},
{"name":"CERATODONTIFORMES","common":"Lungfish"},
{"name":"CLUPEIFORMES","common":"Anchovies"},
{"name":"ALBULIFORMES","common":"Bonefishes"},
{"name":"ANGUILLIFORMES","common":"Eels"},
{"name":"ELOPIFORMES","common":"Tarpons"},
{"name":"NOTACANTHIFORMES","common":"Spiny Eels"},
{"name":"SACCOPHARYNGIFORMES","common":"Sackpharynx Fishes"},
{"name":"ATHERINIFORMES","common":"Rainbow Fishes"},
{"name":"BELONIFORMES","common":"Halfbeeks"},
{"name":"BERYCIFORMES","common":"Ray-finned fishes"},
{"name":"CYPRINODONTIFORMES","common":"Killifishes"},
{"name":"GASTEROSTEIFORMES","common":"Dragonfishes"},
{"name":"MUGILIFORMES","common":"Mullet fish"},
{"name":"PERCIFORMES","common":"Perch-like Fishes"},
{"name":"PLEURONECTIFORMES","common":"Flatfishes"},
{"name":"SCORPAENIFORMES","common":"Scorpion Fishes, Sculpins"},
{"name":"STEPHANOBERYCIFORMES","common":"Deep-sea ray-finned fishes"},
{"name":"SYNBRANCHIFORMES","common":"Swamp Eels"},
{"name":"TETRAODONTIFORMES","common":"Cowfishes"},
{"name":"ZEIFORMES","common":"Boarfishes"},
{"name":"AULOPIFORMES","common":"Marine ray-finned fish"},
{"name":"LAMPRIDIFORMES","common":"Opahs"},
{"name":"CYPRINIFORMES","common":"Minnows"},
{"name":"GONORHYNCHIFORMES","common":"Milkfishes"},
{"name":"SILURIFORMES","common":"Catfishes"},
{"name":"BATRACHOIDIFORMES","common":"Batrachoidiforms"},
{"name":"GADIFORMES","common":"Cods"},
{"name":"LOPHIIFORMES","common":"Anglerfishes"},
{"name":"OPHIDIIFORMES","common":"Ophidiiforms"},
{"name":"POLYMIXIIFORMES","common":"Beardfishes"},
{"name":"ARGENTINIFORMES","common":"Baldfishes,Tubeshoulders"},
{"name":"SALMONIFORMES","common":"Salmons"},
{"name":"MYCTOPHIFORMES","common":"Latern Fishes, Neoscopelids"},
{"name":"ATELEOPODIFORMES","common":"Jellynose Fishes"},
{"name":"STOMIIFORMES","common":"Deep-sea ray-finned fishes"},
{"name":"OSTEOGLOSSIFORMES","common":"Bonytongues"}
]
},
{
"speciesGroup":"Molluscs",
"taxonRank":"class",
"taxa" : [
{"name":"APLACOPHORA","common":"Solenogasters"},
{"name":"BIVALVIA","common":"Mussels, Clams"},
{"name":"CEPHALOPODA","common":"Cuttlefish"},
{"name":"GASTROPODA","common":"Gastropods, Slugs, Snails"},
{"name":"POLYPLACOPHORA","common":"Chitons"},
{"name":"SCAPHOPODA","common":"Tooth Shells"}
]
},
{
"speciesGroup":"Crustaceans",
"taxonRank":"class",
"taxa" : [
{"name":"BRANCHIOPODA","common":"Fairy shrimp, Clam shrimp"},
{"name":"MALACOSTRACA","common":"Crabs, Lobsters"},
{"name":"MAXILLOPODA","common":"Barnacles, Copepods"},
{"name":"OSTRACODA","common":"Seed shrimp"}
]
},
{
"speciesGroup":"Plants",
"facetName":"species_group",
"taxa" : [
{"name":"Monocots", "common" : "Monocots"},
{"name":"Dicots", "common" : "Dicots"},
{"name":"Angiosperms", "common" : "Flowering plants"},
{"name":"FernsAndAllies", "common" : "Ferns and Allies"},
{"name":"Gymnosperms", "common":"Conifers, Cycads"}
]
},
{
"speciesGroup":"Fungi",
"facetName":"phylum",
"taxa" : [
{"name":"Ascomycota", "common" : "Asco's"},
{"name":"Basidiomycota", "common" : "Basidio's"},
{"name":"Chytridiomycota", "common" : "Chytrids"},
{"name":"Zygomycota", "common" : "Zygomycetes"},
{"name":"Glomeromycota", "common":"Glomeromycota"}
]
}
]""")
}
