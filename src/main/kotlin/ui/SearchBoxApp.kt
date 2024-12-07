import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.Duration
import service.SearchService
import java.awt.Desktop
import java.io.File

private const val namePathSeparator = "->"

private const val rows = 25

class SearchBoxApp : Application() {
    override fun start(primaryStage: Stage) {


        val debounceDelay: Long = 300 // 300 milliseconds

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

        val searchBox = TextField()
        searchBox.promptText = "Search here..." // Placeholder text
        searchBox.style = "-fx-font-size: 18px; -fx-pref-width: 400px;"

        val resultsListView = ListView<String>()
        resultsListView.style = "-fx-font-size: 16px; -fx-text-fill: gray;"

        // Add a listener for keystrokes (text property listener)
        /*    searchBox.textProperty().addListener { _, _, newValue ->
    
                val searchResult = searchService.searchIndex(listOf(newValue), 10)
    
                outputLabel.text = searchResult.foundDocuments.map { it.fileName + "=>" + it.filePath }.joinToString("\n")
    
            }*/

        // PauseTransition for debouncing
        val debounce = javafx.animation.PauseTransition(Duration.millis(debounceDelay.toDouble()))

        // Add a listener for keystrokes
        searchBox.textProperty().addListener { _, _, newValue ->
            debounce.setOnFinished {
                if (newValue.length < 3) {
                    return@setOnFinished
                }
                val searchTerms = newValue.split(" ").toList()
                val searchResult = searchService.searchIndex(searchTerms, rows)
                val resultItems = searchResult.foundDocuments.map { "${it.fileType}: ${it.fileName} $namePathSeparator ${it.filePath}" }
                resultsListView.items.setAll(resultItems)

                searchResult.foundDocuments.forEach {
                    println(
                        "Document Name : " + it.fileName
                                + " -$namePathSeparator" + it.filePath
                                + "  :: Score : " + it.score
                    )
                }
            }
            debounce.playFromStart()
        }

        // Add a click listener to ListView items

        searchBox.setOnKeyTyped { event ->
            if (event.character == "\r")  {
                resultsListView.requestFocus() // Move focus to the ListView
                if (resultsListView.items.isNotEmpty()) {
                    resultsListView.selectionModel.select(0) // Optionally select the first item
                }
            }
        }
        
        resultsListView.setOnMouseClicked { event ->
            if (event.clickCount == 2)
                openFile( resultsListView)
        }

        resultsListView.setOnKeyTyped { event ->
            if (event.character == "\r")
            {
                openFile( resultsListView)
            }
            else
            {
                searchBox.requestFocus() 
                searchBox.positionCaret(searchBox.text.length)
            }
        }


        // Layout and scene setup
        val root = VBox(10.0, searchBox, resultsListView)
        root.style = "-fx-padding: 20px; -fx-alignment: center;"

        val scene = Scene(root, 1200.0, 400.0)
        primaryStage.scene = scene
        primaryStage.title = "Search Box"
        primaryStage.show()
    }

    private fun openFile(resultsListView: ListView<String>) {
   

        val selectedItem = resultsListView.selectionModel.selectedItem
        if (selectedItem != null) {

        // Extract the file path from the list item
        val filePath = selectedItem.split(namePathSeparator).last().trim()

        val file = File(filePath)
        if (file.exists() && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file) // Open the file with the associated application
            } catch (ex: Exception) {
                println("Error opening file: ${ex.message}")
            }
        } else {
            println("File does not exist or desktop operations are not supported.")
        }
        // Add your action here (e.g., open file, display details, etc.)
    }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {


            val java = SearchBoxApp::class.java
            launch(java)
        }
    }
}
