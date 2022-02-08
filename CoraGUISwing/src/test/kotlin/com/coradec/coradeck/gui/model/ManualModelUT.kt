/*
 * Copyright ⓒ 2018 − 2021 by Coradec LLC.  All rights reserved.
 */

package com.coradec.coradeck.gui.model

import com.coradec.coradeck.bus.model.BusApplication
import com.coradec.coradeck.bus.model.impl.BasicBusApplication
import com.coradec.coradeck.bus.module.CoraBusImpl
import com.coradec.coradeck.com.module.CoraComImpl
import com.coradec.coradeck.conf.module.CoraConfImpl
import com.coradec.coradeck.ctrl.module.CoraControlImpl
import com.coradec.coradeck.dir.module.CoraDirImpl
import com.coradec.coradeck.gui.ctrl.impl.ApplicationLayout
import com.coradec.coradeck.gui.ctrl.impl.ApplicationLayout.ApplicationSections.CONTENT_PLANE
import com.coradec.coradeck.gui.ctrl.impl.ApplicationLayout.ApplicationSections.CONTROL_PLANE
import com.coradec.coradeck.gui.model.bus.ButtonImpl
import com.coradec.coradeck.gui.model.bus.FrameImpl
import com.coradec.coradeck.gui.model.bus.LabelImpl
import com.coradec.coradeck.module.model.CoraModules
import com.coradec.coradeck.text.module.CoraTextImpl
import com.coradec.coradeck.type.module.impl.CoraTypeImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ManualModelUT {
    @Test
    fun testManualModelConstruction() {
        // given:
        val gui = GUIModel("Manual")
        val labelName = "Label"
        val buttonName = "Button"
        val frame = FrameImpl().apply { gui["Main"] = this }
        val label = LabelImpl()
        val button = ButtonImpl()
        // when:
        val layout = ApplicationLayout()
        frame.layout = layout
        layout[CONTENT_PLANE].add(label)
        layout[CONTROL_PLANE].add(button)
        Thread.sleep(20)
        frame.standby()
        // then:
        assertThat(gui.members).containsValue(frame)
//        assertThat(layout[CONTENT_PLANE].contains(labelName)).isTrue()
//        assertThat(layout[CONTROL_PLANE].contains(buttonName)).isTrue()
        assertThat(frame.contains(labelName).value).isTrue()
        assertThat(frame.contains(buttonName).value).isTrue()
        assertThat(frame.visible).isFalse()
        assertThat(label.visible).isTrue()
        assertThat(button.visible).isTrue()
        assertThat(app.lookup("Main").value).isEqualTo(frame.memberView)
    }

    @Test
    fun testManualModelIllumination() {
        // given:
        val gui = GUIModel("Manual")
        val labelName = "Label"
        val buttonName = "Button"
        val frame = FrameImpl().apply { gui["Main"] = this }
        val label = LabelImpl()
        val button = ButtonImpl()
        val layout = ApplicationLayout()
        frame.layout = layout
        layout[CONTENT_PLANE].add(label)
        layout[CONTROL_PLANE].add(button)
        Thread.sleep(20)
        frame.standby()
        // when:
        frame.visible = true
        Thread.sleep(100)
        // then:
        assertThat(frame.visible).isTrue()
        assertThat(label.visible).isTrue()
        assertThat(button.visible).isTrue()
    }

    companion object {
        private lateinit var app: BusApplication

        @BeforeAll
        @JvmStatic
        fun setup() {
            CoraModules.register(
                CoraConfImpl::class,
                CoraTextImpl::class,
                CoraTypeImpl::class,
                CoraComImpl::class,
                CoraControlImpl::class,
                CoraDirImpl::class,
                CoraBusImpl::class
            )
            app = object: BasicBusApplication("test", emptyList()) {
                override fun run() {
                    Thread.sleep(10000)
                }
            }
        }
    }
}
