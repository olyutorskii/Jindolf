# Jindolf #

-----------------------------------------------------------------------


## Jindolfとは ? ##

* Jindolfは、CGIゲーム「[人狼BBS][BBS]」の専用クライアント開発プロジェクトです。

* Jindolf is a chat game browser application for 人狼BBS.
人狼BBS and Jindolf players belonged to the Japanese-speaking community.
Therefore, Jindolf documents and comments are heavily written in Japanese.

* Jindolfは2023年10月頃まで [OSDN][OSDN](旧称 SourceForge.jp)
でホスティングされていました。
OSDNの可用性に関する問題が長期化しているため、GitHubへと移転してきました。

* ※ 人狼BBSを主催するninjin氏は、Jindolfの製作に一切関与していません。
Jindolfに関する問い合わせををninjin氏へ投げかけないように！約束だよ！


## ビルド方法 ##

* Jindolfはビルドに際して [Maven 3.3.9+](https://maven.apache.org/)
と JDK 1.8+ を要求します。

* Jindolfはビルドに際してJinParser、Jovsonz、QuetexJ などのライブラリを必要とします。
開発時はMaven等を用いてこれらのライブラリを用意してください。

* Mavenを使わずとも `src/main/java/` 配下のソースツリーをコンパイルすることで
ライブラリを構成することが可能です。


## ライセンス ##

* JinParser独自のソフトウェア資産には [The MIT License][MIT] が適用されます.


## プロジェクト創設者 ##

* 2009年に [Olyutorskii](https://github.com/olyutorskii) によってプロジェクトが発足しました。


## 実行環境 ##

* JindolfはJDK8に相当するJava実行環境で利用できるように作られています。
* JindolfはGUIを通じて操作するプログラムのため、その実行においては
ビットマップディスプレイとポインティングデバイスとキーボードへの接続を
必要とします。
* Jindolfが人狼BBSサーバとHTTP通信を行う場合、TCP/IPネットワーク環境を
必要とします。
* 人狼BBSは符号化された日本語で遊ばれるため、Jindolfの実行には日本語環境が
必要です。


[BBS]: http://ninjinix.com/
[OSDN]: https://ja.osdn.net/projects/jindolf/scm/git/Jindolf/
[MIT]: https://opensource.org/licenses/MIT


--- EOF ---
