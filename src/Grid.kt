class Grid<TItem>(step: Double, private val location: TItem.() -> Location) {
    private var map = mutableMapOf<GridXY, MutableSet<TItem>>()

    var step: Double = step
        set(value) {
            field = value
            val allItems = allItems()
            map.clear()
            allItems.forEach { addItem(it) }
        }

    fun allItems(): Set<TItem> = map.values.flatMapTo(hashSetOf()) { it }

    fun addItem(item: TItem) {
        val gridXY = item.location().toGrid()
        map.getOrPut(gridXY) { hashSetOf() }.add(item)
    }

    fun removeItem(item: TItem) {
        val gridXY = item.location().toGrid()
        val removed = map[gridXY]!!.remove(item)
        assert(removed)
    }

    fun itemMoved(item: TItem, oldLocation: Location) {
        val oldGridXY = oldLocation.toGrid()
        val newGridXY = item.location().toGrid()
        if (newGridXY != oldGridXY) {
            val removed = map[oldGridXY]!!.remove(item)
            assert(removed)
            map.getOrPut(newGridXY) { hashSetOf() }.add(item)
        }
    }

    fun itemsWithinDistance(location: Location, distance: Double): Set<TItem> {
        val minX = location.x - distance
        val maxX = location.x + distance
        val minY = location.y - distance
        val maxY = location.y + distance
        val (minGridX, minGridY) = Location(minX, minY).toGrid()
        val (maxGridX, maxGridY) = Location(maxX, maxY).toGrid()

        val result = hashSetOf<TItem>()
        for (gridX in minGridX..maxGridX) {
            for (gridY in minGridY..maxGridY) {
                //TODO: optimization - check that grid element is in distance at all (corners of rectangle may be not)
                map[GridXY(gridX, gridY)]?.filterTo(result) {
                    it.location().distanceTo(location) <= distance
                }
            }
        }
        return result
    }

    private fun Location.toGrid() = GridXY((x / step).toInt(), (y / step).toInt())
/*
    private fun GridXY.minX() = x * step
    private fun GridXY.minY() = y * step
    private fun GridXY.maxX() = (x + 1) * step
    private fun GridXY.maxY() = (y + 1) * step
*/

    private data class GridXY(val x: Int, val y: Int)
}