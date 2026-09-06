package com.endiq.client.cosmetics

/** Immutable low-poly meshes in Minecraft model pixels. No fake entities or server packets. */
object CosmeticMeshes {
    enum class Anchor { HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG, ROOT }
    data class Vertex(val x: Float, val y: Float, val z: Float, val u: Float, val v: Float,
                      val nx: Float, val ny: Float, val nz: Float, val color: Int = -1)
    data class Part(val anchor: Anchor, val vertices: List<Vertex>, val motion: String = "", val pivotX: Float = 0f)
    private fun box(x: Float, y: Float, z: Float, w: Float, h: Float, d: Float, cape: Boolean = false, color: Int = -1): List<Vertex> {
        val result = mutableListOf<Vertex>()
        fun face(points: List<Triple<Float, Float, Float>>, nx: Float, ny: Float, nz: Float, uv: FloatArray = floatArrayOf(0f,0f,1f,1f)) {
            val coords = arrayOf(uv[0] to uv[1], uv[0] to uv[3], uv[2] to uv[3], uv[2] to uv[1])
            points.forEachIndexed { i, p -> result += Vertex(p.first,p.second,p.third,coords[i].first,coords[i].second,nx,ny,nz,color) }
        }
        fun uv(a: Int,b: Int,c: Int,e: Int) = floatArrayOf(a/64f,b/32f,c/64f,e/32f)
        val full = floatArrayOf(0f,0f,1f,1f)
        face(listOf(Triple(x,y,z),Triple(x,y+h,z),Triple(x+w,y+h,z),Triple(x+w,y,z)),0f,0f,-1f,if(cape)uv(1,1,11,17) else full)
        face(listOf(Triple(x+w,y,z+d),Triple(x+w,y+h,z+d),Triple(x,y+h,z+d),Triple(x,y,z+d)),0f,0f,1f,if(cape)uv(12,1,22,17) else full)
        face(listOf(Triple(x,y,z+d),Triple(x,y+h,z+d),Triple(x,y+h,z),Triple(x,y,z)),-1f,0f,0f,if(cape)uv(0,1,1,17) else full)
        face(listOf(Triple(x+w,y,z),Triple(x+w,y+h,z),Triple(x+w,y+h,z+d),Triple(x+w,y,z+d)),1f,0f,0f,if(cape)uv(11,1,12,17) else full)
        face(listOf(Triple(x,y,z+d),Triple(x,y,z),Triple(x+w,y,z),Triple(x+w,y,z+d)),0f,-1f,0f,if(cape)uv(1,0,11,1) else full)
        face(listOf(Triple(x,y+h,z),Triple(x,y+h,z+d),Triple(x+w,y+h,z+d),Triple(x+w,y+h,z)),0f,1f,0f,if(cape)uv(11,0,21,1) else full)
        return result
    }
    private fun b(x:Number,y:Number,z:Number,w:Number,h:Number,d:Number,color:Int=-1) = box(x.toFloat(),y.toFloat(),z.toFloat(),w.toFloat(),h.toFloat(),d.toFloat(),color=color)
    val models: Map<CosmeticManager.CosmeticType, List<Part>> by lazy {
        mapOf(
            CosmeticManager.CosmeticType.CAPE to listOf(Part(Anchor.BODY,box(-5f,0f,2.1f,10f,16f,1f,true),"cape")),
            CosmeticManager.CosmeticType.HAT to listOf(Part(Anchor.HEAD,b(-5,-8.8,-5,10,.8,10)+b(-4.3,-12.5,-4.3,8.6,3.8,8.6))),
            CosmeticManager.CosmeticType.MASK to listOf(Part(Anchor.HEAD,b(-4.25,-4.4,-4.65,8.5,3.5,.6)+b(-4.4,-3.8,-4.2,.35,1,8.4)+b(4.05,-3.8,-4.2,.35,1,8.4))),
            CosmeticManager.CosmeticType.WINGS to listOf(-1,1).map { side ->
                val feathers=(0..3).flatMap { n ->
                    val length=13f-n*2f
                    b(if(side<0)-length else 0f, n*3f,2.8f,length,3.8f,.7f)
                }
                Part(Anchor.BODY,feathers,"wing",side*2f)
            },
            CosmeticManager.CosmeticType.SUIT to listOf(
                Part(Anchor.BODY,b(-4.3,.2,-2.35,8.6,11.5,4.7)+b(-3.7,1,2.4,7.4,9.8,2)),
                Part(Anchor.LEFT_ARM,b(-1.2,-1,-2.3,4.4,4.5,4.6)),
                Part(Anchor.RIGHT_ARM,b(-3.2,-1,-2.3,4.4,4.5,4.6)),
                Part(Anchor.LEFT_LEG,b(-2.25,7.5,-2.25,4.5,4.7,4.5)),
                Part(Anchor.RIGHT_LEG,b(-2.25,7.5,-2.25,4.5,4.7,4.5))),
            CosmeticManager.CosmeticType.PET to listOf(Part(Anchor.ROOT,
                b(10,19,0,7,3,8)+b(12,20,-3,3,2.4,3)+
                b(8.8,21,.5,2,1,2)+b(16.2,21,.5,2,1,2)+b(8.8,21,5.5,2,1,2)+b(16.2,21,5.5,2,1,2)+
                b(12.25,20.2,-3.1,.5,.5,.12,0xFF102523.toInt())+b(14.25,20.2,-3.1,.5,.5,.12,0xFF102523.toInt()),"pet"))
        )
    }
}
