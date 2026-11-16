# HyperRecentsLift

HyperOS ランチャーのタスク画面を、カードごと全体的に上にずらす LSPosed モジュール。
Modern Xposed API (libxposed) 102 / Kotlin。

## 動機

HyperOS 3 のタスク画面では一括終了ボタンが画面最下部にあり、
ジェスチャーでタスクを開くときに指がボタンへ触れて、
意図せず全てのタスクを終了してしまうことがある。
カードも含めて画面全体が下寄りなので、コンテナごと上にずらして回避する。


## バージョン

- v0.8: ソース整理のみ (コメントの清書、未使用のテーマを削除)。
  挙動は v0.7 と同一
- v0.7: 値を固定化 (全体 75dp / ボタン単独 15dp)。設定画面と
- v0.2: ずらしの維持方式を変更 (v0.1 は効いたり効かなかったりだった)。
  ずらし量は固定 80dp のまま
- v0.1: 初版。生成時に 1 回だけずらす方式

## 使い方

1. LSPosed マネージャーで有効化し、スコープで「HyperOS ランチャー
   (com.miui.home)」を選ぶ
2. ランチャーを再起動する (端末の再起動でも可)
3. タスク画面を開いて確認する

## ライセンス・出典

- 本モジュールのコードは独自に書いたもので、ライセンスは Apache-2.0
- 依存ライブラリ: libxposed API (Apache-2.0、compileOnly)
- フック対象クラス名 (`com.miui.home.recents.views.RecentsContainer`) の
  特定にあたり、HyperCeiler (AGPL-3.0、作者 sevtinge ほか、
  https://github.com/ReChronoRain/HyperCeiler) の公開ソースを参照した。
  コードの流用はない

## English

# HyperRecentsLift

An LSPosed module that shifts the HyperOS launcher's task screen, cards and all, upward.
Modern Xposed API (libxposed) 102 / Kotlin.

## Motivation

On HyperOS 3's task screen, the clear-all button is at the very bottom of the screen, and the gesture used to open the task screen can touch the button and close all tasks by accident.
The whole screen sits low, cards included, so this module shifts the entire container up to avoid that.


## Versions

- v0.8: Source cleanup only (tidied comments, removed an unused theme).
  Behavior is identical to v0.7
- v0.7: Values fixed (whole screen 75dp / button 15dp on its own). The settings screen and
- v0.2: Changed how the shift is maintained (v0.1 worked only sometimes).
  The shift amount stayed fixed at 80dp
- v0.1: First version. Shifted only once, when the view was created

## How to use

1. Enable the module in the LSPosed manager and select "HyperOS Launcher
   (com.miui.home)" in its scope
2. Restart the launcher (rebooting the device also works)
3. Open the task screen and check

## License / Credits

- This module's code is original, released under Apache-2.0
- Dependency: libxposed API (Apache-2.0, compileOnly)
- The hook target class name (`com.miui.home.recents.views.RecentsContainer`) was
  identified by referencing the published source of HyperCeiler (AGPL-3.0, by
  sevtinge and others, https://github.com/ReChronoRain/HyperCeiler).
  No code was reused
