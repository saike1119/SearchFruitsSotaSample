//packageのsearchdbsotaは任意のpackage名に変更してください
package jp.vstone.searchdbsota;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import jp.vstone.RobotLib.CPlayWave;
import jp.vstone.RobotLib.CRobotMem;
import jp.vstone.RobotLib.CRobotPose;
import jp.vstone.RobotLib.CRobotUtil;
import jp.vstone.RobotLib.CSotaMotion;
import jp.vstone.sotatalk.SpeechRecog;
import jp.vstone.sotatalk.TextToSpeechSota;

public class SearchDbSota {

	static final String TAG = "SearchDbSota";
	// 実行ファイルを指定
	static final String getText_url = "実行ファイルを指定"; // 例:http://192.168.11.10/SearchFruitsSotaSample/SearchFruits.php

	// VSMDと通信ソケット・メモリアクセス用クラス
	private static CRobotMem mem = new CRobotMem();
	private static CSotaMotion motion = new CSotaMotion(mem);
	// Sota用モーション制御クラス
	private static SpeechRecog recog = new SpeechRecog(motion);
	private static CRobotPose pose;

	public static void main(String[] args) {
		CRobotUtil.Log(TAG, "Start " + TAG);

		// VSMDと通信ソケット・メモリアクセス用クラス
		CRobotMem mem = new CRobotMem();
		// Sota用モーション制御クラス
		CSotaMotion motion = new CSotaMotion(mem);

		if (mem.Connect()) {
			// Sota仕様にVSMDを初期化
			motion.InitRobot_Sota();

			CRobotUtil.Log(TAG, "Rev. " + mem.FirmwareRev.get());

			// サーボモータを現在位置でトルクOnにする
			CRobotUtil.Log(TAG, "Servo On");
			motion.ServoOn();

			// 全ての軸を初期化
			pose = new CRobotPose();
			pose.SetPose(new Byte[] { 1, 2, 3, 4, 5, 6, 7, 8 } // id
					, new Short[] { 0, -900, 0, 900, 0, 0, 0, 0 } // target pos
			);
			// LEDを点灯（左目：赤、右目：赤、口：Max、電源ボタン：赤）
			pose.setLED_Sota(Color.ORANGE, Color.ORANGE, 255, Color.ORANGE);

			motion.play(pose, 100);
			CRobotUtil.wait(100);
		}

		while (true) {
			pose();
			CPlayWave.PlayWave_wait(TextToSpeechSota.getTTSFile("何の果物について知りたいですか？やめる時は終了と言ってください。"));
			CRobotUtil.Log(TAG, "Mic Recording...");
			String fruitName = recog.getResponse(15000, 3);
			CRobotUtil.Log(TAG, fruitName);

			if (fruitName.contains("終了")) {
				CRobotUtil.Log(TAG, "finish...");
				break;
			}
			String speech_text = searchFruit(fruitName);
			CPlayWave.PlayWave_wait(TextToSpeechSota.getTTSFile(speech_text));
		}
	}

	/**
	 * WebサーバにアクセスしてテキストをGETで取得する処理
	 *
	 * @param strGetUrl
	 * @return
	 */
	public static String getStringByCallGET(String strGetUrl) {

		HttpURLConnection con = null;
		StringBuffer result = new StringBuffer();

		try {

			URL url = new URL(strGetUrl);

			con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("GET");
			con.connect(); // URLにGETでリクエストを送信

			// HTTPレスポンスコード
			final int status = con.getResponseCode();
			if (status == HttpURLConnection.HTTP_OK) {
				// 通信に成功した
				// テキストを取得する
				final InputStream in = con.getInputStream();
				String encoding = con.getContentEncoding();
				if (null == encoding) {
					encoding = "UTF-8";
				}
				final InputStreamReader inReader = new InputStreamReader(in, encoding);
				final BufferedReader bufReader = new BufferedReader(inReader);
				String line = null;
				// 1行ずつテキストを読み込む
				while ((line = bufReader.readLine()) != null) {
					result.append(line);
				}
				bufReader.close();
				inReader.close();
				in.close();
			} else {
				// System.out.println(status);
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			if (con != null) {
				// コネクションを切断
				con.disconnect();
			}
		}
		// System.out.println("result=" + result.toString());

		return result.toString();
	}

	/**
	 * DBから値を検索する
	 *
	 * @param fruit
	 * @return
	 */
	public static String searchFruit(String fruit) {
		String param = "?";
		String speechSota = "";
		String error = "error";

		if (fruit != null && fruit.length() > 0) {
			param += "fruitsName=" + encodeWord(fruit);
		}
		speechSota = getStringByCallGET(getText_url + param);
		if (speechSota != null && speechSota.length() > 0) {
			return speechSota;
		}
		return error;
	}

	/**
	 * 受け取ったStringをエンコードする
	 *
	 * @param word
	 * @return
	 */
	public static String encodeWord(String word) {
		if (word != null) {
			try {
				word = URLEncoder.encode(word, "utf-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return word;
	}

	/**
	 * あざといポーズ
	 */
	public static void pose() {
		pose.SetPose(new Byte[] { 1, 2, 3, 4, 5 }, new Short[] { 0, 180, -850, -180, 850 });
		motion.play(pose, 1000);
		CRobotUtil.wait(100);
	}
}
