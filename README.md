AdkTwitter
	•	作成 鹿児島大学　山之上　卓
	•	作成日 2012 11/23

	•	概要
Arduino ボードに接続された光センサ(LilyPad Light Sensor) で光の強度を 計測し、暗くなったら、LED を点灯し、明るくなったら LED を消灯します。 また、LED が点灯・消灯したとき、Android 端末を経由して、twitter にそのことをつぶやきます。

	•	物理的接続概要
	   LED----------+-Arduino Mega ADK ---(ADK)---Android---(Twitter4j)--- Twitter
	   Light Sensor-+
	
	•	Android は、2.3.4 以上
	•	LED, Sensor, Arduino Mega の接続
	•	LED と抵抗1本(600ohm - 1K ohm)を直列接続し、アノード側を Arduino ボードの 8 番端子 (PWM 8) に接続し、カソード側を GND 端子に接続します。
	•	Lilypad センサの + を Arduino ボードの 5V 端子に接続し、
	◦	を GND 端子に接続し、S を ANALOG IN の A0 に接続しますL。
	•	ソフトウェアのインストール
	•	PCで本パッケージ(AdkTwitter)のzipファイル(AdkTwitter.zip)を unzip します。
** Arduino
	•	PCでArduino の開発環境(Arduino-IDE)(1.0以上)を利用できるようにします。
	
	•	AdkTwitter/firmware/arduino_libs 内の AndroidAccessory と USB_Host_Shield の２つのディレクトリ以下を、 arduino-IDEの libraries ディレクトリにコピーします。
	•	arduino-IDE を起動(arduino.exeを実行)し、AdkTwitter/firmware/adkArduino 内の adkArduino.ino を開きます。
	•	Arduino Mega ADK ボードをUSBケーブルでPCに接続します。
	•	arduino-IDE のメニューの [Tools]-[Board] で、Arduino Mega 2560 or Mega ADK を選択し、[Tools]-[Serial Port]で、接続したUSB ケーブルに対応した Serial Port を選択します。
	•	adkArduino.ino のプログラムを Arduino Mega ADK に upload します。
** Android
	•	AdkTwitter 内の org.yamalab.AdkTwiter.apk を android 端末のSDカード 　にコピーし、インストールします。
	•	ASTRO ファイルマネージャなどを使って、SDカードにコピーされた org.yamalab.AdkTwitter.apk をインストールします。AdkTwitter のアイコンが 　アプリケーションのメニューの中に追加されます。
	•	プログラムを修正・変更するときは、Eclipse で AdkTwitter/app を 　Android の既存プロジェクトとしてロードします。
	•	接続
	•	Arduino Mega ADK のUSB の B型 コネクタ(正方形に近い形のコネクタ)を電源 　に接続されたUSBケーブルと接続します。
	•	Arduino Mega ADK のUSB の A型 コネクタ(平べったいコネクタ)と Android 端末 　のMicro コネクタをUSBケーブルで接続します。
	•	ケーブル接続時に、アプリケーション起動を尋ねるダイアログが現れる場合が 　ありますが、それはキャンセルします。
	
	•	起動
	
	•	Arduino のリセットボタンを押します。アプリケーション起動を尋ねる 　ダイアログが現れる場合がありますが、それはキャンセルします。
	•	AdkTwitter をタッチし、起動します。このとき、アプリケーション起動の 確認ダイアログが現れたら、OK をタッチします。
	•	正常に起動したら、画面の上部に In, Out, stop, twitter, tweet, Login のボタンが表れ、In の背後の色が水色になっています。In の画面が下に 　表示されていることを示しています。
	•	twitter ボタンが選択され、そこで下に現れた「Twitte にログインしつぶやく」 　ボタンが表示された場合、これをタッチし、画面の上部の「Login」ボタンを 　タッチし、twitter のログイン画面を表示し、自分のid でtwitter にログイン 　します。
	•	正常に動作している場合、LED のOn/Off が入れ替わるたびに、ログインした 　twitterのアカウントで、LED の On/Off と時間と #twitterTest がつぶやかれ 　ます。また、Android 端末側のIn の画面の一番下の窓に、 　　activity set device d 8 1 か activity set device d 8 0 が表示されます。
	
	•	終了
	
	•	画面の上部の Stop ボタンをタッチします。
	•	Android の設定で、AdkTwitter を強制終了します。

	•	謝辞 このプロジェクトは以下を利用・参考にしています。感謝します。

	•	Arduino
	•	Google Android Open Accessory Development Kit http://developer.android.com/tools/adk/index.html Google APIs Platform 2.3.3, API Level: 10 
	•	twitter
	•	Twitter4j http://twitter4j.org/ja/index.html
	•	Android での Twitter4j を使った OAuth認証とツイートの例 by henteko07 http://d.hatena.ne.jp/henteko07/20110329/1301410470
	•	Eclipse

AdkTwitter

	•	Author Takashi Yamanoue, Kagoshima University
	•	Date 23Nov2012

	•	Abstract

On/Off the LED which is connected to the Arduino board, when the light sensor(Lilypad light sensor) senses that it is dark or bright. When the LED is On/Off, it is tweeted through the Android terminal.

	•	Physical Connection

LED----------+-Arduino---(ADK)---Android---(Twitter4j)--- Twitter Light Sensor-+

	•	Acknowledgement
	
This project uses followings. Thanks to all of them.

Google Android Open Accessory Development Kit http://developer.android.com/tools/adk/index.html Google APIs Platform 2.3.3, API Level: 10
twitter
Twitter4j http://twitter4j.org/ja/index.html
Android での Twitter4j を使った OAuth認証とツイートの例 by henteko07 http://d.hatena.ne.jp/henteko07/20110329/1301410470
Eclipse
