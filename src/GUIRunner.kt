import tornadofx.*
import net.ufoproductions.*
import net.ufoproductions.views.*

class GUIRunner: App(PrimaryView::class) {
    fun main(args: Array<String>) {
        /**
         * GUIRunner Primary launcher for GUI of program.
         * TODO: Write more, and also make it such that JavaFX is actually detected.
         */
        println("Engaging GUIRunner...")
        launch<GUIRunner>(args)
    }
}