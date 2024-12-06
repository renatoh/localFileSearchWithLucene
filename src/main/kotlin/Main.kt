import service.SearchService


fun main(args: Array<String>) {

    val pathsToIndex = listOf("/Users/renato/Documents")
    val excludedPaths = listOf(
        "/Users/renato/Pictures",
        "/Users/renato/projects", "/Users/renato/music",
        "/Users/renato/Library",
        "/Users/renato/.m2",
        "/Users/renato/y"
    )

    val searchService = SearchService(pathsToIndex, excludedPaths)

    searchService.indexDirectories()
    
    val searchResult = searchService.searchIndex(listOf("solr"), 10)
    
    println("Total Results :: " + (searchResult.numFound))
    
    searchResult.foundDocuments.forEach { 
        println("Document Name : " + it.fileName
                    + " -->" + it.filePath
                    + "  :: Score : " + it.score
        )}
    }
