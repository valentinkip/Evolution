import java.io.File
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    val strategies = listOf(
            NaiveStrategy to 3,
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
    println("Initial population: ${environment.population}")

    while (environment.games < 10000) {
        environment.playOneRound()
        println("Round #${environment.rounds}, games played: ${environment.games}, population: ${environment.population}")
        if (environment.population == 0) return
    }

    println()
    print(environment.statistics())

    val image = environment.asImage(500, 500)
    ImageIO.write(image, "PNG", File("result.png"));
}