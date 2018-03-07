
import java.awt.*
import javax.swing.*

fun main(args: Array<String>) {
    val strategies = listOf(
            NaiveStrategy to 1,
            CynicStrategy to 5,
            RandomStrategy(0.5) to 1,
            RandomStrategy(0.25) to 1,
            RandomStrategy(0.75) to 1,
            TitForTatStrategy() to 5,
            NaiveProberStrategy(0.1) to 1,
            NaiveProberStrategy(0.2) to 1,
            NaiveProberStrategy(0.3) to 1
    )
    val environment = Environment(strategies)

    val view = LiveView(environment)

    JFrame("Evolution").apply {
        layout = BorderLayout()
        add(view, BorderLayout.CENTER)
        size = Dimension(1024, 768)
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        isVisible = true
    }

    view.start()
}

