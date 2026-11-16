package com.overnight.hyperrecentslift

import android.util.Log
import android.view.View
import android.view.ViewGroup
import io.github.libxposed.api.XposedInterface.ExceptionMode
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

/**
 * HyperOS ランチャーの最近タスク画面を全体的に上にずらす LSPosed モジュール。
 * Modern Xposed API (libxposed) 102 / Kotlin。設定なし・固定値で動作する。
 *
 * 背景:
 * HyperOS 3 の最近タスク画面は一括終了ボタンが画面最下部にあり、
 * ジェスチャーで最近タスクを開くとき指がボタンに触れて、誤って
 * 全終了してしまうことがある。画面全体を上にずらして回避する。
 *
 * 仕組み:
 *  - 対象は com.miui.home (HyperOS ランチャー) のみ
 *  - 最近タスク画面のルート View (RecentsContainer) の生成直後に
 *    translationY を設定し、全体を上にずらす。一括終了ボタンは
 *    コンテナの内側にあるため一緒に動く
 *  - ランチャー側が表示のたびに translationY を戻すため、
 *    OnPreDrawListener で描画のたびに値を確認して維持する
 *  - 一括終了ボタンまわりだけは、さらに単独で少し上にずらす。
 *    大きく上げるとカードの描画レイヤーの裏に潜ってしまうため、
 *    カードと重ならない小さな値にしている
 *
 * ずらし量は実機調整で決めた固定値 (全体 75dp、ボタン単独 15dp)。
 * 対象クラス名とボタンのビュー構成は HyperCeiler (AGPL-3.0) の
 * 公開ソースの参照と実機のビュー構成ダンプで特定した。
 * コードの流用はない (配置を変える処理は本モジュール独自)。
 * 変更履歴は README を参照。
 */
class MainHook : XposedModule() {

    private var hooksInstalled = false

    override fun onPackageReady(param: PackageReadyParam) {
        if (param.packageName != LAUNCHER_PACKAGE) return
        if (!param.isFirstPackage || hooksInstalled) return
        hooksInstalled = true
        installHooks(param.classLoader)
    }

    private fun installHooks(classLoader: ClassLoader) {
        try {
            val containerClass = Class.forName(RECENTS_CONTAINER, false, classLoader)
            val onFinishInflate = containerClass.getDeclaredMethod("onFinishInflate")
            hook(onFinishInflate)
                .setId("hyperrecentslift.onFinishInflate")
                .setExceptionMode(ExceptionMode.PROTECTIVE)
                .intercept { chain ->
                    val result = chain.proceed()
                    (chain.thisObject as? View)?.let { applyLift(it) }
                    result
                }
            log(Log.INFO, TAG, "hooks installed on $RECENTS_CONTAINER")
        } catch (t: Throwable) {
            log(Log.ERROR, TAG, "failed to install hooks", t)
        }
    }

    /**
     * コンテナ全体と一括終了ボタンまわりにずらしを適用し、維持する。
     * 値が同じときの setter は実質何もしないので、毎フレームの
     * 確認でも負荷は小さい。
     */
    private fun applyLift(container: View) {
        val density = container.resources.displayMetrics.density
        val liftPx = -(LIFT_DP * density)
        val buttonPx = -(BUTTON_LIFT_DP * density)
        container.translationY = liftPx
        log(Log.INFO, TAG, "lift applied: ${LIFT_DP}dp, button ${BUTTON_LIFT_DP}dp")

        var lastCorrectionLogAt = 0L
        var buttonViews: List<View>? = null
        container.viewTreeObserver.addOnPreDrawListener {
            if (container.translationY != liftPx) {
                container.translationY = liftPx
                // ランチャー側にリセットされていた記録 (間引きあり)
                val now = System.currentTimeMillis()
                if (now - lastCorrectionLogAt > CORRECTION_LOG_INTERVAL_MS) {
                    lastCorrectionLogAt = now
                    log(Log.INFO, TAG, "lift re-applied (was reset by launcher)")
                }
            }
            // ボタンのビューは初回表示時に探し、以後は維持だけ行う
            if (buttonViews == null && container.isShown) {
                buttonViews = findButtonViews(container)
                if (buttonViews != null) {
                    log(Log.INFO, TAG, "button views found: ${buttonViews!!.size}")
                }
            }
            buttonViews?.forEach { v ->
                if (v.translationY != buttonPx) {
                    v.translationY = buttonPx
                }
            }
            true // 描画は止めない
        }
    }

    /**
     * 一括終了ボタンまわりのビューを探す。構成は実機ダンプで確認した:
     * RecentsContainer > RecentsDecorations の中に、ボタン本体の
     * CircleAndTickAnimView (id=clearAnimView) と、その土台の
     * LinearLayout (id=memoryAndClearContainer) がある。
     * 両方を同じ量ずらすことで、見た目とタッチ範囲を一緒に動かす。
     */
    private fun findButtonViews(root: View): List<View>? {
        val found = mutableListOf<View>()
        collectByEntryName(root, found, 0)
        val hasButton = found.any { entryName(it) == ID_CLEAR_ANIM_VIEW }
        return if (hasButton) found else null
    }

    private fun collectByEntryName(view: View, out: MutableList<View>, depth: Int) {
        if (depth > MAX_TREE_DEPTH) return
        val name = entryName(view)
        if (name == ID_CLEAR_ANIM_VIEW || name == ID_MEMORY_AND_CLEAR_CONTAINER) {
            out.add(view)
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                collectByEntryName(view.getChildAt(i), out, depth + 1)
            }
        }
    }

    private fun entryName(view: View): String? = try {
        if (view.id != View.NO_ID) {
            view.resources.getResourceEntryName(view.id)
        } else {
            null
        }
    } catch (t: Throwable) {
        null
    }

    companion object {
        private const val TAG = "HyperRecentsLift"
        private const val LAUNCHER_PACKAGE = "com.miui.home"
        private const val RECENTS_CONTAINER = "com.miui.home.recents.views.RecentsContainer"

        /** 全体を上にずらす量 (dp) */
        private const val LIFT_DP = 75f

        /** 一括終了ボタンまわりを単独で上にずらす量 (dp) */
        private const val BUTTON_LIFT_DP = 15f

        /** 一括終了ボタン本体のリソース名 */
        private const val ID_CLEAR_ANIM_VIEW = "clearAnimView"

        /** 一括終了まわりの土台コンテナのリソース名 */
        private const val ID_MEMORY_AND_CLEAR_CONTAINER = "memoryAndClearContainer"

        private const val MAX_TREE_DEPTH = 10
        private const val CORRECTION_LOG_INTERVAL_MS = 5000L
    }
}
