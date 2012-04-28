dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
	dialect = org.hibernate.dialect.MySQLDialect	    
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:mem:devDb;MVCC=TRUE"
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE"
        }
    }
    production {
        dataSource {
            dbCreate = "create"
            driverClassName = "com.mysql.jdbc.Driver"
            url = "jdbc:mysql://ala-biedb1.vm.csiro.au/mobileauth"
		    username = "mobileauth"
		    password = "RHitoHYAbfmJBCT0zywo143tH"   
		    dialect = org.hibernate.dialect.MySQLDialect		    
            pooled = true
            properties {
               maxActive = -1
               minEvictableIdleTimeMillis=1800000
               timeBetweenEvictionRunsMillis=1800000
               numTestsPerEvictionRun=3
               testOnBorrow=true
               testWhileIdle=true
               testOnReturn=true
               validationQuery="SELECT 1"
            }
        }
    }
}
