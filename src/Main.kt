
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.WindowConstants

fun main(args: Array<String>) {
    val view = LiveView()

    JFrame("Evolution").apply {
        layout = BorderLayout()
        add(view, BorderLayout.CENTER)
        size = Dimension(1024, 768)
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isVisible = true
    }
}

