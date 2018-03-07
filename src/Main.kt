
import java.awt.*
import javax.swing.*
import javax.swing.border.EtchedBorder

fun main(args: Array<String>) {
    val strategies = listOf(
            NaiveStrategy to 1,
            CynicStrategy to 3,
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

/*
    println("Initial population: ${environment.population}")

    while (environment.games < 10000) {
        environment.runOneRound()
        println("Round #${environment.rounds}, games played: ${environment.games}, population: ${environment.population}")
        if (environment.population == 0) return
    }

    println()
    print(environment.statistics())

    val image = environment.asImage(500, 500)
    ImageIO.write(image, "PNG", File("result.png"));
*/
}

class LiveView(private val environment: Environment) : JPanel(BorderLayout()) {
    private val imageView = ImageView().apply {
        border = EtchedBorder(EtchedBorder.RAISED, Color.GRAY, Color.BLACK)
    }

    private val statisticsView = JTextArea().apply {
        isEditable = false
        columns = 40
        lineWrap = true
        background = Color.LIGHT_GRAY
        border = EtchedBorder(EtchedBorder.LOWERED, Color.GRAY, Color.BLACK)
    }

    private val lifeThread = Thread({ run() }, "Evolution Life Thread")
    private var shouldStop = false

    init {
        add(imageView, BorderLayout.CENTER)
        add(statisticsView, BorderLayout.EAST)
        update()
    }

    fun start() {
        lifeThread.start()
    }

    fun stop() {
        shouldStop = true
    }

    private fun run() {
        while(!shouldStop && environment.population > 0) {
            environment.runOneRound()
            update()
        }
    }

    private fun update() {
        if (imageView.width > 0 && imageView.height > 0) {
            imageView.image = environment.asImage(imageView.width, imageView.height)
        }
        statisticsView.text = environment.statistics()
    }

    //TODO: what about resize?
    private class ImageView : JComponent() {
        var image: Image? = null
            set(value) {
                field = value
                repaint()
            }

        override fun paintComponent(g: Graphics) {
            image?.let { g.drawImage(it, 0, 0, this) }
        }

    }
}
