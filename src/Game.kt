import java.awt.Color

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

abstract class Strategy {
    abstract val presentation: String
    abstract val presentationColor: Color

    abstract fun makeDecision(otherId: Long, gameHistory: GamesHistory): Decision

    abstract fun clone(): Strategy

    override fun toString() = presentation
}