import java.awt.Color
import java.util.*

object NaiveStrategy : Strategy() {
    override val presentation get() = "Naive"
    override val presentationColor get() = Color.PINK
    override fun makeDecision(otherId: Long) = Decision.COOPERATE
    override fun clone() = this
}

object CynicStrategy : Strategy() {
    override val presentation get() = "Cynic"
    override val presentationColor get() = Color.BLACK
    override fun makeDecision(otherId: Long) = Decision.DEFECT
    override fun clone() = this
}

class RandomStrategy(private val defectRate: Double) : Strategy() {
    init {
        assert(0.0 < defectRate && defectRate < 1.0)
    }

    private val random = Random()

    override val presentation get() = "Random (defectRate = $defectRate)"
    override val presentationColor = (defectRate * 255).toInt().let { Color(it, it, it) }

    override fun makeDecision(otherId: Long): Decision {
        return if (random.nextBoolean()) Decision.COOPERATE else Decision.DEFECT
    }

    override fun clone() = RandomStrategy(defectRate)
}

open class TitForTatStrategy : Strategy() {
    private val reminscences = mutableMapOf<Long, Decision>()

    override val presentation get() = "Tit for Tat"
    override val presentationColor get() = Color.GREEN

    override fun makeDecision(otherId: Long) = reminscences[otherId] ?: Decision.COOPERATE

    override fun remember(otherId: Long, otherDecision: Decision) {
        reminscences[otherId] = otherDecision
    }

    override fun clone() = TitForTatStrategy()
}

class NaiveProberStrategy(private val probeRate: Double) : TitForTatStrategy() {
    init {
        assert(0.0 < probeRate && probeRate < 1.0)
    }

    private val random = Random()

    override val presentation get() = "Naive Prober (probeRate = $probeRate)"
    override val presentationColor: Color get() = Color.RED

    override fun makeDecision(otherId: Long): Decision {
        return if (random.nextDouble() > probeRate) super.makeDecision(otherId) else Decision.DEFECT
    }

    override fun clone() = NaiveProberStrategy(probeRate)
}
