package com.endiq.client.hud

import com.endiq.client.compat.*
import com.endiq.client.modules.ModuleManager
import com.endiq.client.modules.impl.hud.*
import com.endiq.client.modules.impl.pvp.*
import com.endiq.client.modules.impl.hud.ToggleSprintModule
import com.endiq.client.modules.impl.render.*
import com.endiq.client.modules.impl.utility.*
import com.endiq.client.modules.impl.hypixel.*

object HudRenderer {
    private val WHITE  = 0xFFFFFFFF.toInt()
    private val GREEN  = 0xFF3D9970.toInt()
    private val RED    = 0xFFE05252.toInt()
    private val YELLOW = 0xFFFFFF55.toInt()
    private val BLACK  = 0x88000000.toInt()
    private val BLUE   = 0xFF5B9BD5.toInt()

    private inline fun <reified T : com.endiq.client.modules.Module> mod(): T? = ModuleManager.get<T>()

    fun onTick() {
        val client = MinecraftClient.getInstance()
        val player = client.player

        // AutoHide
        mod<AutoHideHudModule>()?.let {
            if (it.enabled) it.tick(player?.velocity?.lengthSquared() ?: 0.0 > 0.01)
        }

        // Block Indicator
        mod<BlockIndicatorModule>()?.let {
            if (it.enabled) {
                val hit = client.crosshairTarget
                it.blockName = if (hit is BlockHitResult)
                    client.world?.getBlockState(hit.blockPos)?.block?.name?.string ?: "" else ""
            }
        }

        // Pack Display
        mod<PackDisplayModule>()?.let {
            if (it.enabled) {
                it.packName = lastResourcePackName()
            }
        }

        // Sprint
        mod<com.endiq.client.modules.impl.movement.SprintModule>()?.let {
            if (it.enabled && player != null && player.forwardSpeed > it.minSpeed.value && !player.isSprinting &&
                (!it.cancelSneak.value || !client.options.sneakKey.isPressed) && player.hungerManager.foodLevel>6)
                player.isSprinting = true
        }

        // Toggle Sprint
        mod<ToggleSprintModule>()?.let {
            if (it.enabled) it.sprintOn = player?.isSprinting ?: false
        }

        mod<TimersModule>()?.tick()

        mod<FpsModule>()?.tick()
        mod<ZoomModule>()?.tick(client.player!=null && client.currentScreen==null && isKeyDown(org.lwjgl.glfw.GLFW.GLFW_KEY_C))

        mod<AutoTextModule>()?.tick()
        mod<WaypointsModule>()?.tick()

        // Net Graph sampling
        mod<NetGraphModule>()?.let { if (it.enabled) it.onTick() }

        // Hit Color flash decay
        mod<HitColorModule>()?.let { it.tick() }
        mod<ComboCounterModule>()?.tick()
    }

    fun onHudRender(ctx: GuiContext) {
        val client = MinecraftClient.getInstance() ?: return
        val player = client.player
        val tr = client.textRenderer
        val sw = ctx.scaledWindowWidth
        val sh = ctx.scaledWindowHeight

        // Wait for two unobstructed rendered frames before grabbing the game image.
        mod<CameraModule>()?.frame()
        mod<CameraModule>()?.let { camera ->
            if(camera.notification.value && System.currentTimeMillis()-camera.noticeAt<5000 && camera.notice.isNotEmpty()) {
                val value=com.endiq.client.gui.components.TurtleTheme.fit(tr,camera.notice,sw-24)
                ctx.drawTextWithShadow(tr,value,(sw-tr.getWidth(value))/2,sh-80,WHITE)
            }
            val fraction=1f-(System.currentTimeMillis()-camera.flashAt)/(camera.flashDuration.value*1000f)
            if(camera.enabled && camera.flashEffect.value && fraction>0f) {
                val alpha=(camera.flashColor.a*fraction).toInt().coerceIn(0,255)
                ctx.fill(0,0,sw,sh,(alpha shl 24) or (camera.flashColor.toArgb() and 0xFFFFFF))
            }
        }
        mod<WaypointsModule>()?.let { if(it.enabled)HudStyles.text(ctx,it,it.visibleText(),sw-160,60) }
        mod<ZoomModule>()?.let { if(it.showFovNum.value && it.isZooming)ctx.drawTextWithShadow(tr,"Zoom ${"%.1f".format(it.currentFov)}",sw/2-28,sh-58,WHITE) }

        mod<PopupEventsModule>()?.let { NotificationsRenderer.render(ctx,it) }

        // AutoHide check
        mod<AutoHideHudModule>()?.let {
            if (it.enabled && !it.shouldShow()) return
        }

        var ly = 2
        fun left(module:com.endiq.client.modules.Module,t:String) {
            HudStyles.text(ctx,module,t,2,ly);ly+=10
        }

        mod<FpsModule>()?.let            { if (it.enabled) left(it,it.getText()) }
        mod<PingModule>()?.let          { if (it.enabled) left(it,it.getText()) }
        mod<CpsModule>()?.let            { if (it.enabled) left(it,it.getText()) }
        mod<CoordinatesModule>()?.let { if (it.enabled) left(it,it.getText()) }
        mod<ServerAddressModule>()?.let { if (it.enabled) left(it,it.getText()) }
        mod<PackDisplayModule>()?.let { if (it.enabled) left(it,"Pack: ${it.packName}") }
        mod<SpeedHudModule>()?.let { if (it.enabled) left(it,it.getText()) }
        mod<MemoryHudModule>()?.let { if (it.enabled) left(it,it.getText()) }
        mod<ClockHudModule>()?.let { if (it.enabled) left(it,it.getText()) }
        mod<DirectionHudModule>()?.let { if (it.enabled) left(it,it.getText()) }
        mod<ToggleSprintModule>()?.let { if (it.enabled) left(it,it.getText()) }

        val blockInd = mod<BlockIndicatorModule>()
        if (blockInd?.enabled == true && blockInd.blockName.isNotEmpty())
            left(blockInd,"Block: ${blockInd.blockName}")

        val reach = mod<ReachDisplayModule>()
        if (reach?.enabled == true && reach.lastReach > 0) left(reach,reach.getText())

        // Independently sample the target: this must not depend on Block Indicator being enabled.
        mod<BlockOverlayModule>()?.let { overlay ->
            val hit=client.crosshairTarget
            if(overlay.enabled && hit is BlockHitResult) {
                val name=client.world?.getBlockState(hit.blockPos)?.block?.name?.string.orEmpty()
                if(name.isNotEmpty()) {
                    val pos=hit.blockPos;val coords="${pos.x}, ${pos.y}, ${pos.z}"
                    val lines=when(overlay.style.selected) { 1->listOf("$name ($coords)");2->listOf(name,"Position: $coords");else->listOf(name) }
                    val scale=overlay.scale.value;val pad=overlay.bgPadding.value.toInt()
                    val width=lines.maxOf { tr.getWidth(it) };val height=lines.size*10
                    val x=(sw/2f+overlay.posOffsetX.value-width*scale/2).coerceIn(0f,(sw-width*scale).coerceAtLeast(0f))
                    val y=(sh/2f+overlay.posOffsetY.value).coerceIn(0f,(sh-height*scale).coerceAtLeast(0f))
                    ctx.transformed(x,y,scale) {
                        if(overlay.showBg.value)ctx.fill(-pad,-pad,width+pad,height+pad-2,overlay.bgColor.toArgb())
                        lines.forEachIndexed { i,line->ctx.drawText(tr,line,0,i*10,overlay.textColor.toArgb(),overlay.shadow.value) }
                    }
                }
            }
        }

        // Attack cooldown bar; only the implemented bar controls are advertised.
        mod<AttackIndicatorModule>()?.let {
            if(it.enabled && player!=null) {
                val c=player.getAttackCooldownProgress(0f).coerceIn(0f,1f)
                if(!it.onlyInCombat.value || c<1f) {
                    val scale=it.scale.value;val w=it.barWidth.value.toInt();val h=it.barHeight.value.toInt()
                    val x=(sw*it.posX.value/100f-w*scale/2).coerceIn(0f,(sw-w*scale).coerceAtLeast(0f))
                    val y=(sh*it.posY.value/100f).coerceIn(0f,(sh-(h+12)*scale).coerceAtLeast(0f))
                    val color=if(c>=1f)it.readyColor.toArgb() else it.chargeColor.toArgb()
                    ctx.transformed(x,y,scale) {
                        if(it.outline.value)ctx.fill(-1,-1,w+1,h+1,WHITE)
                        val alpha=(it.bgColor.a*it.bgAlpha.value/255f).toInt().coerceIn(0,255)
                        ctx.fill(0,0,w,h,(alpha shl 24) or (it.bgColor.toArgb() and 0xFFFFFF))
                        ctx.fill(0,0,(c*w).toInt(),h,color)
                        if(it.showPercent.value)ctx.drawText(tr,"${(c*100).toInt()}%",0,h+2,color,it.shadow.value)
                    }
                }
            }
        }

        // Armor bar
        mod<ArmorBarModule>()?.let {
            if (it.enabled && player != null && player.armor > 0) {
                val bx = sw/2-40; val by = sh-34
                ctx.fill(bx, by, bx+80, by+3, BLACK)
                ctx.fill(bx, by, bx+player.armor*4, by+3, BLUE)
            }
        }

        // Potion status (top right)
        mod<PotionStatusModule>()?.let {
            if (it.enabled && player != null) {
                var py = 2
                for (e in player.statusEffects) {
                    val s = e.duration/20
                    val lbl = "${effectName(e)} ${s/60}:${"%02d".format(s%60)}"
                    val w = tr.getWidth(lbl)+4
                    ctx.fill(sw-w-2, py, sw-2, py+10, BLACK)
                    ctx.drawTextWithShadow(tr, lbl, sw-w, py+1, WHITE)
                    py += 12
                }
            }
        }

        // UHC overlay
        mod<UhcOverlayModule>()?.let {
            if (it.enabled && player != null) {
                val hp  = "%.1f".format(player.health)
                val sat = "%.1f".format(player.hungerManager.saturationLevel)
                ctx.drawTextWithShadow(tr, "HP: $hp  SAT: $sat", sw/2-40, sh-50, RED)
            }
        }

        // Nearby-player table honors its filters, fields and layout controls.
        mod<PvpInfoModule>()?.let { info ->
            if(info.enabled && player!=null) {
                val candidates=com.endiq.client.modules.impl.render.EntityCache.playersWithin(info.maxDist.value.toDouble())
                    .filter { e->e!=player && (!info.showOnlyEnemy.value || player.scoreboardTeam==null || e.scoreboardTeam!=player.scoreboardTeam) }
                val nearby=when(info.sortBy.selected) {
                    1->candidates.sortedBy { it.health }
                    2->candidates.sortedBy { it.name.string }
                    else->candidates.sortedBy { player.distanceTo(it) }
                }.take(info.maxPlayers.value.toInt())
                if(nearby.isNotEmpty()) {
                    val rows=nearby.map { e->buildList {
                        if(info.showName.value)add(e.name.string)
                        if(info.showHealth.value)add("${"%.0f".format(e.health)}hp")
                        if(info.showDist.value)add("${"%.1f".format(player.distanceTo(e))}m")
                        if(info.showArmor.value)add("Armor ${e.armor}")
                    }.joinToString(" ") }
                    val colors=listOf(info.headerColor.toArgb())+nearby.map { e->
                        if(info.colorHealth.value)when { e.health<8f->RED;e.health<16f->YELLOW;else->GREEN } else info.textColor.toArgb()
                    }
                    HudStyles.text(ctx,info,(listOf("Nearby:")+rows).joinToString("\n"),lineColors=colors)
                }
            }
        }

        // Timer controls now drive a monotonic clock and every visible style option.
        mod<TimersModule>()?.let { timer ->
            if(timer.enabled && timer.clock.started) {
                val label=timer.text();val scale=timer.scale.value
                val x=timer.posX.value/100f*(sw-tr.getWidth(label)*scale-8).coerceAtLeast(0f)+4
                val y=timer.posY.value/100f*(sh-14*scale).coerceAtLeast(0f)+3
                ctx.transformed(x,y,scale) {
                    if(timer.showBg.value)ctx.fill(-4,-3,tr.getWidth(label)+4,11,timer.bgColor.toArgb())
                    ctx.drawText(tr,label,0,0,if(timer.finished)timer.alertColor.toArgb() else timer.textColor.toArgb(),timer.shadow.value)
                }
            }
        }

        // Motion blur overlay
        mod<MotionBlurModule>()?.let {
            if (it.enabled && player != null) {
                val dy = Math.abs(player.yaw - it.lastYaw)
                val dp = Math.abs(player.pitch - it.lastPitch)
                val amount = ((dy+dp) * it.strength.value / 20f).coerceIn(0f, 0.55f)
                if (amount > 0.03f) ctx.fill(0, 0, sw, sh, (amount*180).toInt().coerceIn(8,180) shl 24)
                it.lastYaw = player.yaw; it.lastPitch = player.pitch
            }
        }

        // Keystrokes
        mod<KeystrokesModule>()?.let {
            if (it.enabled) {
                val o = client.options; val bx = 4; val by = sh-52
                drawKey(ctx, "W", bx+13, by,      o.forwardKey.isPressed)
                drawKey(ctx, "A", bx,    by+13, o.leftKey.isPressed)
                drawKey(ctx, "S", bx+13, by+13, o.backKey.isPressed)
                drawKey(ctx, "D", bx+26, by+13, o.rightKey.isPressed)
                drawKey(ctx, "^", bx+13, by+26, o.jumpKey.isPressed)
            }
        }

        // Armor Status
        mod<ArmorStatusModule>()?.let {
            if (it.enabled && player != null) {
                var ax = 2; val ay = sh-68
                for (stack in armorStacks(player)) {
                    if (stack.isEmpty) continue
                    ctx.drawItem(stack, ax, ay)
                    val pct = if (stack.maxDamage > 0) (stack.maxDamage-stack.damage)*100/stack.maxDamage else 100
                    val col = when { pct > 60 -> 0xFF55FF55.toInt(); pct > 30 -> YELLOW; else -> RED }
                    ctx.drawTextWithShadow(tr, "$pct", ax+1, ay+9, col)
                    ax += 18
                }
            }
        }

        // Hit Color flash overlay
        mod<HitColorModule>()?.let {
            if (it.enabled && it.flashTicks > 0) {
                val strength = (it.flashTicks.toFloat() / it.duration.value.coerceAtLeast(1f)) * it.intensity.value
                val base = it.hitColor.toArgb()
                val alpha = ((base ushr 24) * strength).toInt().coerceIn(0, 255)
                ctx.fill(0, 0, sw, sh, (alpha shl 24) or (base and 0x00FFFFFF))
            }
        }

        // Combo Counter
        mod<ComboCounterModule>()?.let {
            if (it.enabled && it.combo > 1 && !it.isExpired()) {
                val cx = (sw * it.posX.value / 100f).toInt()
                val cy = (sh * it.posY.value / 100f).toInt()
                val pop=if(it.lastPopTicks>0 && it.shakeOnHit.value)2 else 0
                val number="${it.combo}x";val label=" COMBO";val width=tr.getWidth(number+label);val scale=it.scale.value
                ctx.transformed((cx-width*scale/2).coerceAtLeast(0f),(cy-pop).toFloat(),scale) {
                    ctx.drawText(tr,number,0,0,it.comboColor.toArgb(),it.shadow.value)
                    ctx.drawText(tr,label,tr.getWidth(number),0,it.textColor.toArgb(),it.shadow.value)
                }
            }
        }

        // Net Graph
        mod<NetGraphModule>()?.let {
            if (it.enabled && it.samples.isNotEmpty()) {
                val gx = (sw * it.posX.value / 100f).toInt()
                val gy = (sh * it.posY.value / 100f).toInt()
                val gw = it.width.value.toInt()
                val gh = it.height.value.toInt()
                ctx.fill(gx, gy, gx + gw, gy + gh, it.bgColor.toArgb())
                val n = it.samples.size
                val barW = (gw.toFloat() / n).coerceAtLeast(1f)
                val maxPing = (it.samples.maxOrNull() ?: 1).coerceAtLeast(1)
                it.samples.forEachIndexed { i, ping ->
                    val bh = ((ping.toFloat() / maxPing) * gh).toInt().coerceIn(1, gh)
                    val bx = gx + (i * barW).toInt()
                    val col = if (ping > it.badAbove.value) it.badColor.toArgb() else it.barColor.toArgb()
                    ctx.fill(bx, gy + gh - bh, (bx + barW).toInt().coerceAtMost(gx + gw), gy + gh, col)
                }
            }
        }

        // Team View
        mod<TeamViewModule>()?.let {
            if (it.enabled && player != null) {
                val myTeam = player.scoreboardTeam
                if (myTeam != null) {
                    val mates = client.world?.entities
                        ?.filterIsInstance<PlayerEntity>()
                        ?.filter { e -> e != player && e.scoreboardTeam == myTeam && player.distanceTo(e) < it.maxDist.value }
                        ?.sortedBy { e -> player.distanceTo(e) }?.take(it.maxPlayers.value.toInt()) ?: emptyList()
                    if (mates.isNotEmpty()) {
                        val rows=mates.map { m->"${m.name.string} ${"%.0f".format(player.distanceTo(m))}m" }
                        HudStyles.text(ctx,it,(listOf("Team:")+rows).joinToString("\n"),
                            lineColors=listOf(it.headerColor.toArgb())+rows.map { _->it.textColor.toArgb() })
                    }
                }
            }
        }
    }

    private fun drawKey(ctx: GuiContext, label: String, x: Int, y: Int, pressed: Boolean) {
        ctx.fill(x, y, x+12, y+12, if (pressed) 0xCC3D9970.toInt() else 0x88000000.toInt())
        ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, label,
            x+2, y+2, if (pressed) 0xFF000000.toInt() else WHITE)
    }
}
