import tornadofx.*
import net.ufoproductions.*
import net.ufoproductions.views.*

class GUIRunner: App(PrimaryView::class) {
    fun main(args: Array<String>) {
        println("Engaging GUIRunner...")
        launch<GUIRunner>(args)
    }
}