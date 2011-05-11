[UTF-8 Japanese]

                               J i n d o l f
                                  README

                                              Copyright(c) 2008 olyutorskii


=== Jindolfとは ===

 Jindolfプロジェクトは、CGIゲーム「人狼BBS」を快適にプレイするための
専用クライアントを製作するために発足したオープンソースプロジェクトです。

※ このアーカイブは、開発者向けにJindolfのソースコードのみをまとめたものです。
　 ただJindolfをプレイしたいだけの人は、別途JARファイルを入手してください。
※ 人狼BBSのURLは [ http://homepage2.nifty.com/ninjinia/ ] まで
※ 人狼BBSを主催するninjin氏は、Jindolfの製作に一切関与していません。
　 Jindolfに関する問い合わせををninjin氏へ投げかけないように！約束だよ！


=== 実行環境 ===

 - JindolfはJava言語(JLS3)で記述されたプログラムです。
 - JindolfはJRE1.5に準拠したJava実行環境で利用できるように作られています。
   原則として、JRE1.5に準拠した実行系であれば、プラットフォームを選びません。
 - JindolfはGUIを通じて操作するプログラムのため、その実行においては
   ビットマップディスプレイとポインティングデバイスとキーボードへの接続を
   必要とします。
 - Jindolfは人狼BBSサーバとHTTP通信を行うため、TCP/IPネットワーク環境を
   必要とします。
 - 人狼BBSは符号化された日本語で遊ばれるため、Jindolfの実行には日本語環境が
   必要です。


=== 依存ライブラリ ===

 - Jindolfはビルドおよび実行に際してJinCoreおよびJinParserライブラリを
   必要とします。開発時はMaven等を用いてこれらのライブラリを用意してください。



=== アーカイブ管理体制 ===

  このアーカイブは、UTF-8による開発環境を前提として構成されています。
  このアーカイブの原本となる開発資産は、
      http://hg.sourceforge.jp/view/jindolf/Jindolf
  を上位に持つMercurialリポジトリで管理されています。


=== 開発プロジェクト運営元 ===

  http://sourceforge.jp/projects/jindolf/devel/ まで。


=== ソフトウェア利用者向けポータルサイト ===

  http://jindolf.sourceforge.jp/ まで。


=== ディレクトリ内訳構成 ===

基本的にはMaven2のmaven-archetype-quickstart構成に準じます。

./README.txt
    あなたが今見てるこれ。

./CHANGELOG.txt
    変更履歴。

./LICENSE.txt
    ライセンスに関して。

./pom.xml
    Maven2用プロジェクト構成定義ファイル。

./build.xml
    Ant用追加タスク。

./src/main/java/
    Javaのソースコード。

./src/main/resources/
    プロパティファイルなどの各種リソース。

./src/test/java/
    JUnit 4.* 用のユニットテストコード。

./src/main/config/checks.xml
    Checkstyle用configファイル。

./src/main/config/pmdrules.xml
    PMD用ルール定義ファイル。

./src/main/assembly/descriptor.xml
    ソースアーカイブ構成定義ファイル。


--- EOF ---
