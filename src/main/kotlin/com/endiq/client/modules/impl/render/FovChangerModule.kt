package com.endiq.client.modules.impl.render

import com.endiq.client.modules.Module

class FovChangerModule : Module("FOV Changer","Live visual FOV override without invalid vanilla option values",Category.RENDER) {
    val fov=slider("FOV",default=90f,min=30f,max=130f)
}
