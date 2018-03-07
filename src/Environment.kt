
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.text.DecimalFormat
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

const val COOPERATION_PAYOFF = +1.0
const val SUCKER_FINE = -1.5
const val TEMPTATION_PAYOFF = +1.5
const val DEFAULT_PAYOFF = 0.0

const val LIFE_COST = 0.2 // per cycle
const val INITIAL_ENERGY = 5.0
const val BREEDING_COST = 20.0
const val MIN_ENERGY_TO_BREED = 40.0
const val MIN_ENERGY_TO_LIVE = 0.0

const val INITIAL_AREA_SIZE = 5.0
const val MAX_DISTANCE_TO_MEET = 5.0
const val MAX_MOVE_DISTANCE = 1.0

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

data class Location(val x: Double, val y: Double) {
    fun distanceTo(other: Location): Double {
        val dx = x - other.x
        val dy = y - other.y
        return Math.sqrt(dx * dx + dy * dy)
    }
}

class Individual(
        val id: Long,
        val strategy: Strategy,
        var energy: Double,
        var location: Location,
        val gamesHistory: GamesHistory = GamesHistory(id)
)

class Environment(initialPopulation: Collection<Pair<Strategy, Int>>) {
    private val individuals: MutableCollection<Individual>
    private val grid = Grid<Individual>(MAX_DISTANCE_TO_MEET * 2, { location })

    private var nextId = 0L
    private val random = Random()

    var cycleNumber = 0
        private set

    // statistics
    var games = 0
        private set
    var deaths = 0
        private set
    var births = 0
        private set
    var oneCycleTime = 0
        private set

    init {
        individuals = mutableSetOf()
        for ((strategy, count) in initialPopulation) {
            assert(count > 0)
            var clones = count - 1
            var strategyToUse = strategy

            while (true) {
                val location = Location(random.nextDouble() * INITIAL_AREA_SIZE, random.nextDouble() * INITIAL_AREA_SIZE)
                addIndividual(Individual(newId(), strategyToUse, INITIAL_ENERGY, location))

                if (clones-- == 0) break
                strategyToUse = strategy.clone()
            }
        }
    }

    private fun newId() = nextId++

    private fun addIndividual(individual: Individual) {
        individuals.add(individual)
        grid.addItem(individual)
    }

    private fun removeIndividual(individual: Individual) {
        val removed = individuals.remove(individual)
        assert(removed)
        grid.removeItem(individual)
    }

    private fun moveIndividual(individual: Individual, location: Location) {
        val oldLocation = individual.location
        individual.location = location
        grid.itemMoved(individual, oldLocation)
    }

    val population: Int
        get() = individuals.size

    fun statistics(): String {
        val grouped = individuals.groupBy { it.strategy.presentation }
        val presentationsSorted = grouped.keys.sortedByDescending { grouped[it]!!.size }
        return buildString {
            append("Cycles: $cycleNumber\n")
            append("Last cycle time: $oneCycleTime ms\n")
            append("Games played: $games\n")
            append("Died: $deaths\n")
            append("Born: $births\n")
            append("Total population: $population\n")
            val format = DecimalFormat("#.00")
            for (presentation in presentationsSorted) {
                val group = grouped[presentation]!!
                val percent = format.format(group.size / population.toDouble() * 100)
                val averageEnergy = format.format(group.sumByDouble { it.energy } / group.size)
                append("    \"$presentation\": $percent% (average energy: $averageEnergy)\n")
            }
        }
    }

    private val PADDING = 10

    fun asImage(width: Int, height: Int): Image {
        assert(width > PADDING * 2)
        assert(height > PADDING * 2)

        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()

        g.paint = Color.WHITE
        g.fillRect(0, 0, width, height)

        val initialMinX = 0.0
        val initialMaxX = INITIAL_AREA_SIZE
        val initialMinY = 0.0
        val initialMaxY = INITIAL_AREA_SIZE

        val xs = individuals.map { it.location.x } + listOf(initialMinX, initialMaxX)
        val ys = individuals.map { it.location.y } + listOf(initialMinY, initialMaxY)
        val minX = xs.min()!!
        val maxX = xs.max()!!
        val minY = ys.min()!!
        val maxY = ys.max()!!

        val scale1 = (maxX - minX) / (width - PADDING * 2)
        val scale2 = (maxY - minY) / (height - PADDING * 2)
        val scale = max(scale1, scale2)

        fun Double.toImageX(): Int = ((this - minX) / scale + PADDING).roundToInt()
        fun Double.toImageY(): Int = ((this - minY) / scale + PADDING).roundToInt()
        fun Double.toImageDimension(): Int = (this / scale).roundToInt()

        g.paint = Color.DARK_GRAY
        g.stroke = BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0f, floatArrayOf(0.9f), 0f)
        g.drawRect(initialMinX.toImageX(), initialMinY.toImageY(),
                (initialMaxX - initialMinX).toImageDimension(), (initialMaxY - initialMinY).toImageDimension())

        for (individual in individuals) {
            g.paint = individual.strategy.presentationColor
            g.fillOval(individual.location.x.toImageX() - 3, individual.location.y.toImageY() - 3, 6, 6)
        }

        g.dispose()
        return image
    }

    fun runOneCycle() {
        val startTime = System.currentTimeMillis()

        for (individual in individuals.toList()) {
            if (individual !in individuals) continue // has died already

            // step 1: find partner and play the game
            val potentialPartners = grid.itemsWithinDistance(individual.location, MAX_DISTANCE_TO_MEET)
                    .filter { it != individual }
            if (potentialPartners.isNotEmpty()) {
                val partner = potentialPartners[random.nextInt(potentialPartners.size)]
                playGame(individual, partner)
                games++
            }

            // step 2: breed
            if (individual.energy >= MIN_ENERGY_TO_BREED) {
                breed(individual)
            }

            // step 3: move
            move(individual)

            // step 4: spend energy and die if not enough
            individual.energy -= LIFE_COST
            dieIfWeak(individual)

        }

        cycleNumber++
        oneCycleTime = (System.currentTimeMillis() - startTime).toInt()
    }

    private fun playGame(individual1: Individual, individual2: Individual) {
        val decision1 = individual1.strategy.makeDecision(individual2.id, individual1.gamesHistory)
        val decision2 = individual2.strategy.makeDecision(individual1.id, individual2.gamesHistory)

        val payoff1 = payoff(decision1, decision2)
        val payoff2 = payoff(decision2, decision1)

        individual1.gamesHistory.addGame(individual2.id, cycleNumber, decision1, decision2)
        individual2.gamesHistory.addGame(individual1.id, cycleNumber, decision2, decision1)

        individual1.energy += payoff1
        individual2.energy += payoff2

        dieIfWeak(individual1)
        dieIfWeak(individual2)
    }

    private fun dieIfWeak(individual: Individual) {
        if (individual.energy < MIN_ENERGY_TO_LIVE) {
            removeIndividual(individual)
            deaths++
        }
    }

    private fun move(individual: Individual) {
        val moveDistance = random.nextDouble() * MAX_MOVE_DISTANCE
        val moveDirection = random.nextDouble() * Math.PI * 2
        val moveX = moveDistance * Math.cos(moveDirection)
        val moveY = moveDistance * Math.sin(moveDirection)
        moveIndividual(individual, Location(individual.location.x + moveX, individual.location.y + moveY))
    }

    private fun breed(individual: Individual) {
        val strategyClone = individual.strategy.clone()
        assert(strategyClone.javaClass == individual.strategy.javaClass)
        addIndividual(Individual(newId(), strategyClone, INITIAL_ENERGY, individual.location))
        individual.energy -= BREEDING_COST
        births++
    }
}