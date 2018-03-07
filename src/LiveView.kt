import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.border.EtchedBorder

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