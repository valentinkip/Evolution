class LifeProcessController(
        private val updateView: () -> Unit,
        private val onStateChanged: () -> Unit
) {
    var environment: Environment? = null
        private set
    private var thread: MyThread? = null

    enum class State {
        NOT_STARTED, RUNNING, PAUSING, PAUSED, FINISHED
    }

    val state: State
        get() {
            val thread = thread
            return when {
                thread == null -> State.NOT_STARTED
                thread.finished -> if (environment!!.population > 0) State.PAUSED else State.FINISHED
                thread.shouldStop -> State.PAUSING
                else -> State.RUNNING
            }
        }

    fun start(initialPopulation: Collection<Pair<Strategy, Int>>) {
        assert(environment == null)
        assert(thread == null)

        environment = Environment(initialPopulation)

        thread = MyThread()
        thread!!.start()
        onStateChanged()
    }

    fun pause() {
        thread!!.shouldStop = true
        onStateChanged()
    }

    fun resume() {
        assert(thread != null)
        assert(!thread!!.finished)
        thread = MyThread()
        thread!!.start()
        onStateChanged()
    }

    private inner class MyThread : Thread("Life Thread") {
        @Volatile var shouldStop = false
        @Volatile var finished = false

        override fun run() {
            while (!shouldStop && environment!!.population > 0) {
                environment!!.runOneCycle()
                updateView()
            }

            finished = true
            onStateChanged()
        }
    }
}