import java.awt.Color

object NaiveStrategy : Strategy() {
    override val presentation get() = "Naive"
    override val presentationColor get() = Color.PINK
    override fun makeDecision(otherId: Long, gamesHistory: GamesHistory, env: GameEnvironment) = Decision.COOPERATE
}

object CynicStrategy : Strategy() {
    override val presentation get() = "Cynic"
    override val presentationColor get() = Color.BLACK
    override fun makeDecision(otherId: Long, gamesHistory: GamesHistory, env: GameEnvironment) = Decision.DEFECT
}

class RandomStrategy(private val defectRate: Double) : Strategy() {
    init {
        assert(0.0 < defectRate && defectRate < 1.0)
    }

    override val presentation get() = "Random (defectRate = $defectRate)"
    override val presentationColor = (defectRate * 255).toInt().let { Color(it, it, it) }

    override fun makeDecision(otherId: Long, gamesHistory: GamesHistory, env: GameEnvironment): Decision {
        return if (env.random.nextDouble() > defectRate) Decision.COOPERATE else Decision.DEFECT
    }
}

object TitForTatStrategy : Strategy() {
    override val presentation get() = "Tit for Tat"
    override val presentationColor get() = Color.GREEN

    override fun makeDecision(otherId: Long, gamesHistory: GamesHistory, env: GameEnvironment): Decision {
        val prevGames = gamesHistory.gamesWithPartner(otherId)
        return prevGames.lastOrNull()?.hisDecision ?: Decision.COOPERATE
    }
}

class NaiveProberStrategy(private val probeRate: Double) : Strategy() {
    init {
        assert(0.0 < probeRate && probeRate < 1.0)
    }

    override val presentation get() = "Naive Prober (probeRate = $probeRate)"
    override val presentationColor: Color get() = Color.RED

    override fun makeDecision(otherId: Long, gamesHistory: GamesHistory, env: GameEnvironment): Decision {
        return if (env.random.nextDouble() > probeRate)
            TitForTatStrategy.makeDecision(otherId, gamesHistory, env)
        else
            Decision.DEFECT
    }
}
