package dto

enum class Filetype(val symbol : String) {
    
    FILE("f"), DIRECTORY("d")
}
data class FoundDocument(val fileName : String, val filePath : String,val fileType : Filetype, val score : Float) {
}
