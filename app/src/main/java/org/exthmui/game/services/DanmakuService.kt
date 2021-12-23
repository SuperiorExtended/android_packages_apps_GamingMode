/*
 * Copyright (C) 2020 The exTHmUI Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exthmui.game.services

import android.app.Notification
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

import org.exthmui.game.controller.DanmakuController

class DanmakuService : NotificationListenerService() {
    private val lastNotificationMap = mutableMapOf<String, String>()
    private var useFilter = false
    private var notificationBlacklist: List<String>? = null
    private var danmakuController: DanmakuController? = null

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (danmakuController?.shouldShowDanmaku() != true) return
        val extras = sbn.notification.extras
        if (!isInBlackList(sbn.packageName)) {
            val lastNotification = lastNotificationMap.getOrDefault(sbn.packageName, "")
            var title = extras.getString(Notification.EXTRA_TITLE)
            if (title?.isNotBlank() != true) title = extras.getString(Notification.EXTRA_TITLE_BIG)
            val text = extras.getString(Notification.EXTRA_TEXT)
            var danmakuText = ""
            if (title?.isNotBlank() == true) {
                danmakuText += "[$title]"
            }
            if (text?.isNotBlank() == true) {
                danmakuText += text
            }
            if (danmakuText.isNotBlank() && (!useFilter || compareDanmaku(
                    danmakuText,
                    lastNotification
                ))
            ) {
                danmakuController?.showDanmaku(danmakuText)
            }
            lastNotificationMap[sbn.packageName] = danmakuText
        }
    }

    fun init(context: Context, controller: DanmakuController?) {
        danmakuController = controller
        val blacklist = Settings.System.getString(
            context.contentResolver,
            Settings.System.GAMING_MODE_DANMAKU_APP_BLACKLIST
        )
        if (blacklist?.isNotBlank() == true) {
            notificationBlacklist = blacklist.split(",")
        }
        useFilter = Settings.System.getInt(
            context.contentResolver,
            Settings.System.GAMING_MODE_DANMAKU_DYNAMIC_NOTIFICATION_FILTER, 1
        ) == 1
    }

    private fun isInBlackList(packageName: String): Boolean {
        return notificationBlacklist?.contains(packageName) == true
    }

    private fun compareDanmaku(a: String, b: String): Boolean {
        val tA = a.replace("[\\d]+\\.[\\d]+|[\\d]".toRegex(), "")
        val tB = b.replace("[\\d]+\\.[\\d]+|[\\d]".toRegex(), "")
        val filterThreshold = 3
        return if (tA.isBlank() || tB.isBlank()) {
            levenshtein(a, b) > filterThreshold
        } else {
            levenshtein(tA, tB) > filterThreshold
        }
    }

    companion object {
        // 最小编辑距离
        private fun levenshtein(a: CharSequence, b: CharSequence): Int {
            if (a.isBlank()) {
                return if (b.isBlank()) 0 else b.length
            } else if (b.isBlank()) {
                return a.length
            }
            val lenA = a.length
            val lenB = b.length
            val dp = Array(lenA + 1) { IntArray(lenB + 1) }
            var flag: Int
            for (i in 0..lenA) {
                for (j in 0..lenB) dp[i][j] = lenA + lenB
            }
            for (i in 1..lenA) dp[i][0] = i
            for (j in 1..lenB) dp[0][j] = j
            for (i in 1..lenA) {
                for (j in 1..lenB) {
                    flag = if (a[i - 1] == b[j - 1]) {
                        0
                    } else {
                        1
                    }
                    dp[i][j] = (dp[i - 1][j - 1] + flag).coerceAtMost(
                        (dp[i - 1][j] + 1).coerceAtMost(dp[i][j - 1] + 1)
                    )
                }
            }
            return dp[lenA][lenB]
        }
    }
}