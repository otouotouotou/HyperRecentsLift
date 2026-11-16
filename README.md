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
