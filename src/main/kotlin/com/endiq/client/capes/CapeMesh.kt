package com.endiq.client.capes

/**
 * The single mesh this client draws: Minecraft's 10x16 cape panel, hanging from the
 * body with the standard cape UV layout. Immutable, built once, no per-frame maths.
 */
object CapeMesh {
    data class Vertex(val x: Float, val y: Float, val z: Float, val u: Float, val v: Float,
                      val nx: Float, val ny: Float, val nz: Float, val color: Int = -1)

    val vertices: List<Vertex> by lazy { panel(-5f, 0f, 2.1f, 10f, 16f, 1f) }

    private fun uv(a: Int, b: Int, c: Int, d: Int) = floatArrayOf(a / 64f, b / 32f, c / 64f, d / 32f)

    private fun panel(x: Float, y: Float, z: Float, w: Float, h: Float, d: Float): List<Vertex> {
        val result = mutableListOf<Vertex>()
        fun face(points: List<Triple<Float, Float, Float>>, nx: Float, ny: Float, nz: Float, region: FloatArray) {
            val corners = arrayOf(region[0] to region[1], region[0] to region[3], region[2] to region[3], region[2] to region[1])
            points.forEachIndexed { index, point ->
                result += Vertex(point.first, point.second, point.third,
                    corners[index].first, corners[index].second, nx, ny, nz)
            }
        }
        face(listOf(Triple(x, y, z), Triple(x, y + h, z), Triple(x + w, y + h, z), Triple(x + w, y, z)), 0f, 0f, -1f, uv(1, 1, 11, 17))
        face(listOf(Triple(x + w, y, z + d), Triple(x + w, y + h, z + d), Triple(x, y + h, z + d), Triple(x, y, z + d)), 0f, 0f, 1f, uv(12, 1, 22, 17))
        face(listOf(Triple(x, y, z + d), Triple(x, y + h, z + d), Triple(x, y + h, z), Triple(x, y, z)), -1f, 0f, 0f, uv(0, 1, 1, 17))
        face(listOf(Triple(x + w, y, z), Triple(x + w, y + h, z), Triple(x + w, y + h, z + d), Triple(x + w, y, z + d)), 1f, 0f, 0f, uv(11, 1, 12, 17))
        face(listOf(Triple(x, y, z + d), Triple(x, y, z), Triple(x + w, y, z), Triple(x + w, y, z + d)), 0f, -1f, 0f, uv(1, 0, 11, 1))
        face(listOf(Triple(x, y + h, z), Triple(x, y + h, z + d), Triple(x + w, y + h, z + d), Triple(x + w, y + h, z)), 0f, 1f, 0f, uv(11, 0, 21, 1))
        return result
    }
}
