/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.videoplayer.ui.gesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.isDesktop

/**
 * Remembers and returns the current [GestureFamily] based on the most recent input type.
 *
 * On desktop, this defaults to [GestureFamily.MOUSE] and dynamically switches to [GestureFamily.TOUCH]
 * when a touch input is detected, and back to [GestureFamily.MOUSE] when mouse input is detected.
 *
 * On mobile platforms, this always returns [GestureFamily.TOUCH].
 */
@Composable
fun rememberCurrentGestureFamily(): State<GestureFamily> {
    val platform = LocalPlatform.current
    return remember(platform) {
        if (platform.isDesktop()) {
            mutableStateOf(GestureFamily.MOUSE)
        } else {
            mutableStateOf(GestureFamily.TOUCH)
        }
    }
}

/**
 * Modifier that detects the pointer input type (touch vs mouse) and updates the [GestureFamily] state.
 *
 * Should be applied at a high level in the composable tree to capture input events
 * before child gestures consume them.
 *
 * Only effective on desktop; on mobile this is a no-op.
 */
@Stable
fun Modifier.detectInputType(
    currentFamilyState: State<GestureFamily>,
    platform: Platform,
): Modifier {
    if (!platform.isDesktop()) return this
    val mutableState = currentFamilyState as? androidx.compose.runtime.MutableState ?: return this
    return this.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                if (event.type == PointerEventType.Press || event.type == PointerEventType.Move) {
                    val pointerType = event.changes.firstOrNull()?.type ?: continue
                    val newFamily = when (pointerType) {
                        PointerType.Touch -> GestureFamily.TOUCH
                        PointerType.Mouse -> GestureFamily.MOUSE
                        else -> continue
                    }
                    if (mutableState.value != newFamily) {
                        mutableState.value = newFamily
                    }
                }
            }
        }
    }
}
