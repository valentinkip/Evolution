
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

enum class Decision {
    COOPERATE, DEFECT
}

const val COOPERATION_PAYOFF = +1.0
const val SUCKER_FINE = -1.0
const val TEMPTATION_PAYOFF = +1.5
const val DEFAULT_PAYOFF = 0.0

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

    abstract fun makeDecision(otherId: Long): Decision

    open fun remember(otherId: Long, otherDecision: Decision) {
    }

    abstract fun clone(): Strategy

    override fun toString() = presentation
}

data class Location(val x: Double, val y: Double) {
    fun distanceTo(other: Location): Double {
        val dx = x - other.x
        val dy = y - other.y
        return Math.sqrt(dx * dx + dy * dy)
    }
}

class Individual(val id: Long, val strategy: Strategy, var strength: Double, var location: Location)

class Environment(initialPopulation: Collection<Pair<Strategy, Int>>) {
    companion object {
        val INITIAL_STRENGTH = 10.0
        val BREEDING_COST = 5.0
        val MIN_STRENGTH_TO_BREED = 30.0
        val MIN_STRENGTH_TO_LIVE = 0.0

        val INITIAL_AREA_SIZE = 5.0
        val MAX_DISTANCE_TO_MEET = 5.0
        val MAX_MOVE_DISTANCE = 1.0
    }

    private val individuals: MutableCollection<Individual>
    private var nextId = 0L
    private val random = Random()

    // statistics
    var rounds = 0
        private set
    var games = 0
        private set
    var deaths = 0
        private set
    var births = 0
        private set

    init {
        individuals = mutableSetOf()
        for ((strategy, count) in initialPopulation) {
            assert(count > 0)
            var clones = count - 1
            var strategyToUse = strategy

            while (true) {
                val location = Location(random.nextDouble() * INITIAL_AREA_SIZE, random.nextDouble() * INITIAL_AREA_SIZE)
                individuals.add(Individual(newId(), strategyToUse, INITIAL_STRENGTH, location))

                if (clones-- == 0) break
                strategyToUse = strategy.clone()
            }
        }
    }

    private fun newId() = nextId++

    val population: Int
        get() = individuals.size

    fun statistics(): String {
        val grouped = individuals.groupBy { it.strategy.presentation }
        val presentationsSorted = grouped.keys.sortedByDescending { grouped[it]!!.size }
        return buildString {
            append("Rounds: $rounds\n")
            append("Games played: $games\n")
            append("Died: $deaths\n")
            append("Born: $births\n")
            append("Total population: $population\n")
            for (presentation in presentationsSorted) {
                val all = grouped[presentation]!!
                val averageStrength = all.sumByDouble { it.strength } / all.size
                append("    \"$presentation\": ${all.size} (average strength: $averageStrength)\n")
            }
        }
    }

    private val PADDING = 10

    fun asImage(width: Int, height: Int): RenderedImage {
        assert(width > PADDING * 2)
        assert(height > PADDING * 2)

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()

        g.paint = Color.WHITE
        g.fillRect(0, 0, width, height)

        if (population > 0) {
            val xs = individuals.map { it.location.x }
            val ys = individuals.map { it.location.y }
            val minX = xs.min()!!
            val maxX = xs.max()!!
            val minY = xs.min()!!
            val maxY = xs.max()!!

            val scale1 = (maxX - minX) / (width - PADDING * 2)
            val scale2 = (maxY - minY) / (height - PADDING * 2)
            val scale = max(scale1, scale2)
            if (scale > 0) { // TODO:otherwise?
                for (individual in individuals) {
                    val x = ((individual.location.x - minX) / scale + PADDING).roundToInt()
                    val y = ((individual.location.y - minY) / scale + PADDING).roundToInt()
                    g.paint = individual.strategy.presentationColor
                    g.fillOval(x - 3, y - 3, 6, 6)
                }
            }
        }

        g.dispose()
        return image
    }

    fun playOneRound() {
        for (individual in individuals.toList()) {
            if (individual !in individuals) continue // has died already

            // step 1: find partner and play the game
            val potentialPartners = individuals.filter {
                it != individual && it.location.distanceTo(individual.location) <= MAX_DISTANCE_TO_MEET
            }
            if (potentialPartners.isNotEmpty()) {
                val partner = potentialPartners[random.nextInt(potentialPartners.size)]
                playGame(individual, partner)
                games++
            }

            // step 2: breed
            if (individual.strength >= MIN_STRENGTH_TO_BREED) {
                breed(individual)
            }

            // step 3: move
            move(individual)
        }
        rounds++
    }

    private fun playGame(individual1: Individual, individual2: Individual) {
        val decision1 = individual1.strategy.makeDecision(individual2.id)
        val decision2 = individual2.strategy.makeDecision(individual1.id)

        val payoff1 = payoff(decision1, decision2)
        val payoff2 = payoff(decision2, decision1)

        individual1.strategy.remember(individual2.id, decision2)
        individual2.strategy.remember(individual1.id, decision1)

        individual1.strength += payoff1
        individual2.strength += payoff2

        dieIfWeak(individual1)
        dieIfWeak(individual2)
    }

    private fun dieIfWeak(individual: Individual) {
        if (individual.strength < MIN_STRENGTH_TO_LIVE) {
            val removed = individuals.remove(individual)
            assert(removed)
            deaths++
        }
    }

    private fun move(individual: Individual) {
        val moveDistance = random.nextDouble() * MAX_MOVE_DISTANCE
        val moveDirection = random.nextDouble() * Math.PI * 2
        val moveX = moveDistance * Math.cos(moveDirection)
        val moveY = moveDistance * Math.sin(moveDirection)
        individual.location = Location(individual.location.x + moveX, individual.location.y + moveY)
    }

    private fun breed(individual: Individual) {
        val strategyClone = individual.strategy.clone()
        assert(strategyClone.javaClass == individual.strategy.javaClass)
        individuals.add(Individual(newId(), strategyClone, INITIAL_STRENGTH, individual.location))
        individual.strength -= BREEDING_COST
        births++
    }
}