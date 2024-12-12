import dto.Filetype
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.Duration
import service.SearchService
import java.awt.Desktop
import java.io.File
import java.nio.file.Paths
import javax.imageio.ImageIO

private const val namePathSeparator = "->"
private const val rows = 25

class SearchBoxApp : Application() {
    override fun start(primaryStage: Stage) {
        val debounceDelay: Long = 300 // 300 milliseconds

        val pathsToIndex = listOf(
            "/Users/renato",
//            "/Applications"
        
        )
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
        searchBox.style = "-fx-font-size: 18px; -fx-pref-width: 800px;"
        // ContextMenu for suggestions
        val contextMenu = ContextMenu()
        contextMenu.style = "-fx-font-size: 14px; -fx-pref-width: 800px"
       

        // PauseTransition for debouncing
        val debounce = javafx.animation.PauseTransition(Duration.millis(debounceDelay.toDouble()))

        // Add a listener for keystrokes
        searchBox.textProperty().addListener { _, _, newValue ->
            debounce.setOnFinished {
                if (newValue.length < 3) {
                    contextMenu.hide()
                    return@setOnFinished
                }

                val searchTerms = newValue.split(" ").toList()
                val searchResult = searchService.searchIndex(searchTerms, rows)

                contextMenu.items.clear()
                searchResult.foundDocuments.forEach { result ->
                    
                    val menuItemContent =  "${result.fileName} $namePathSeparator ${result.filePath}"
                    val menuItem = MenuItem(menuItemContent)
                    
                    if(result.fileType == Filetype.DIRECTORY)
                            menuItem.style = "-fx-background-color: lightblue; -fx-padding: 5px;"
                         
                    menuItem.setOnAction {
                        // Keep the ContextMenu open after interaction
                        val fileOpened = openFile(menuItemContent)
                        if (fileOpened) {
                            Platform.runLater {
                                val screenBounds = searchBox.localToScreen(searchBox.boundsInLocal)
                                contextMenu.show(searchBox, screenBounds.minX, screenBounds.maxY)
                            }
                        }
                    }
                    contextMenu.items.add(menuItem)
                }

                if (searchResult.foundDocuments.isNotEmpty()) {
                    val screenBounds = searchBox.localToScreen(searchBox.boundsInLocal)
                    contextMenu.show(searchBox, screenBounds.minX, screenBounds.maxY)
                } else {
                    contextMenu.hide()
                }
            }
            debounce.playFromStart()
        }

        // Hide the ContextMenu if the user clicks outside the TextField
        searchBox.focusedProperty().addListener { _, _, isFocused ->
            if (!isFocused) {
                contextMenu.hide()
            }
        }

        // Layout and scene setup
        val root = VBox(10.0, searchBox)
        root.style = "-fx-padding: 1px; -fx-alignment: center;"
        
        val scene = Scene(root, 800.0, 40.0)
        primaryStage.scene = scene
        primaryStage.title = "Search Box"
//        primaryStage.initStyle(StageStyle.UNDECORATED)
        
//        val icon = Image(javaClass.getResourceAsStream("/spotlight-icon.png"))
//        stage.getIcons().add(new Image(<yourclassname>.class.getResourceAsStream("icon.png")));
        
//        primaryStage.getIcons().add(new Image(<yourclassname>.class.getResourceAsStream("icon.png")));
//        val imagePath = Paths.get(javaClass.classLoader.getResource("spotlight-icon.png").toURI())
//        val read = ImageIO.read(imagePath.toFile())

//        val icon = Image(javaClass.getResourceAsStream("/spotlight-icon.png"))
        val icon = Image(javaClass.classLoader.getResourceAsStream("spotlight-icon.png"))
  
          // Set the icon for the primary stage
          primaryStage.icons.add(icon)
        primaryStage.icons.add(icon)
        primaryStage.show()
    }

    private fun openFile(selectedItem: String): Boolean {
        // Extract the file path from the menu item
        val filePath = selectedItem.split(namePathSeparator).last().trim()
        val file = File(filePath)

        return if (file.exists() && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file) // Open the file with the associated application
                true
            } catch (ex: Exception) {
                println("Error opening file: ${ex.message}")
                false
            }
        } else {
            println("File does not exist or desktop operations are not supported.")
            false
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
