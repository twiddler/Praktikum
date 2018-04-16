package netzwerk;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import spiellogik.Karte;

/**
 * Nimmt an den Server zu sendende Objekte entgegen und gibt dem Protokoll
 * entsprechende JSONObjects zurück.
 * 
 * @author xXx Players xXx
 * 
 */
public final class Serialisierer {

	public static JSONObject programm(final Karte[] karten) {
		final int[] prioritaeten = new int[karten.length];
		for (int i = 0; i < karten.length; ++i) {
			prioritaeten[i] = karten[i].prioritaet;
		}

		final JSONObject result = new JSONObject();
		result.put("programm", prioritaeten);
		return result;
	}

	public static JSONObject gebote(Map<Integer, Integer> gebote) {

		final JSONObject result = new JSONObject();
		final JSONArray geboteJSON = new JSONArray();
		for (final Map.Entry<Integer, Integer> gebot : gebote.entrySet()) {
			final JSONObject gebotJSON = new JSONObject();
			gebotJSON.put("karte", gebot.getKey());
			gebotJSON.put("gebot", gebot.getValue());
			geboteJSON.put(gebotJSON);
		}
		result.put("gebote", geboteJSON);
		return result;

	}

	public static JSONObject powerdown(boolean powerdown) {

		final JSONObject result = new JSONObject();
		result.put("powerDown", powerdown);
		return result;

	}

}