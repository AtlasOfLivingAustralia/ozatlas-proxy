grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        mavenLocal()
        mavenRepo ("http://nexus.ala.org.au/content/groups/public/")
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.16'
        build 'mysql:mysql-connector-java:5.1.16'
        runtime 'mysql:mysql-connector-java:5.1.16'
        build 'postgresql:postgresql:9.0-801.jdbc4'
        runtime 'postgresql:postgresql:9.0-801.jdbc4'
        build 'org.apache.httpcomponents:httpcore:4.1.2'
        build 'org.apache.httpcomponents:httpclient:4.1.2'
        runtime 'org.apache.httpcomponents:httpcore:4.1.2'
        runtime 'org.apache.httpcomponents:httpclient:4.1.2'
        runtime 'org.apache.httpcomponents:httpmime:4.1.2'
        build 'org.apache.commons:commons-lang3:3.1'
        runtime 'org.apache.commons:commons-lang3:3.1'
    }

    plugins {
        runtime ":cors:1.1.6"
        runtime ":hibernate:3.6.10.19"
        runtime ":ala-auth:1.3.1"
        runtime ":ala-bootstrap2:2.2"
        runtime ":release:3.0.1"
        build ':tomcat:7.0.54'
    }
}