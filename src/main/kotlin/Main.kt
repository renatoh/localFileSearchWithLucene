import service.SearchService


fun main(args: Array<String>) {
    
    
    
    SearchBoxApp.main(args)
    
/*    val pathsToIndex = listOf("/Users/renato/Documents")
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
    val searchResult2 = searchService.searchIndex(listOf("java"), 10)
    
    println("Total Results :: " + (searchResult.numFound))
    println("Total Results :: " + (searchResult2.numFound))
    
    searchResult.foundDocuments.forEach { 
        println("Document Name : " + it.fileName
                    + " -->" + it.filePath
                    + "  :: Score : " + it.score
        )}*/
    
    
    //shut down app:
//    searcher.indexReader.close()
    
    }
