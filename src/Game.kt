import java.awt.Color
import java.util.*

const val COOPERATION_PAYOFF = +1.0
const val SUCKER_FINE = -1.5
const val TEMPTATION_PAYOFF = +1.5
const val DEFAULT_PAYOFF = 0.0

enum class Decision {
    COOPERATE, DEFECT
}

fun payoff(my: Decision, other: Decision): Double {
    return when (my) {
        Decision.COOPERATE -> {
            when (other) {
                Decision.COOPERATE -> COOPERATION_PAYOFF
                Decision.DEFECT -> SUCKER_FINE
            }
        }

        Decision.DEFECT -> {
            when (other) {
                Decision.COOPERATE -> TEMPTATION_PAYOFF
                Decision.DEFECT -> DEFAULT_PAYOFF
            }
        }
    }
}

interface GameEnvironment {
    val random: Random
}

abstract class Strategy {
    abstract val presentation: String
    abstract val presentationColor: Color

    abstract fun makeDecision(otherId: Long, gamesHistory: GamesHistory, env: GameEnvironment): Decision

    override fun toString() = presentation
}