import LifeProcessController.State.*
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.border.EtchedBorder

class LiveView : JPanel(BorderLayout()) {
    private val lifeProcessController = LifeProcessController(
            updateView = { updateViews() },
            onStateChanged = { controlPanel.updateButtonsState() }
    )

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

    private val controlPanel = ControlPanel()

    init {
        add(imageView, BorderLayout.CENTER)
        add(statisticsView, BorderLayout.EAST)
        add(controlPanel, BorderLayout.NORTH)
        updateViews()
    }

    private fun updateViews() {
        val environment = lifeProcessController.environment
        if (imageView.width > 0 && imageView.height > 0) {
            imageView.image = environment?.asImage(imageView.width, imageView.height)
        }
        statisticsView.text = environment?.statistics() ?: ""
    }

    private inner class ControlPanel : JPanel() {
        private val startButton = JButton("START")
        private val pauseResumeButton = JButton("PAUSE")

        init {
            add(startButton)
            add(pauseResumeButton)

            startButton.addActionListener {
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
                lifeProcessController.start(strategies)
            }

            pauseResumeButton.addActionListener {
                if (lifeProcessController.state == PAUSED) {
                    lifeProcessController.resume()
                }
                else {
                    lifeProcessController.pause()
                }
            }

            updateButtonsState()
        }

        fun updateButtonsState() {
            val state = lifeProcessController.state
            startButton.isEnabled = state != RUNNING && state != PAUSING
            pauseResumeButton.isEnabled = state == RUNNING || state == PAUSED
            pauseResumeButton.text = when (state) {
                NOT_STARTED, RUNNING -> "PAUSE"
                PAUSING -> "PAUSING..."
                PAUSED, FINISHED -> "RESUME"
            }
        }
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