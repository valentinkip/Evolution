// full history of games of particular individual
class GamesHistory(val individualId: Long) {
    class Entry(
            val partnerId: Long,
            val cycleNumber: Int,
            val myDecision: Decision,
            val hisDecision: Decision
    )

    private val entries = ArrayList<Entry>()
    private val entriesByPartnerId = HashMap<Long, MutableList<Entry>>()

    fun addGame(
            partnerId: Long,
            cycleNumber: Int,
            myDecision: Decision,
            hisDecision: Decision
    ) {
        val entry = Entry(partnerId, cycleNumber, myDecision, hisDecision)
        entries.add(entry)
        entriesByPartnerId.getOrPut(partnerId) { ArrayList() }.add(entry)
    }

    fun allGames(): List<Entry> = entries
    fun gamesWithPartner(partnerId: Long): List<Entry> = entriesByPartnerId[partnerId] ?: emptyList()
}