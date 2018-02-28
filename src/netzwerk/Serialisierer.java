package netzwerk;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import spiellogik.Karte;

public final class Serialisierer {

	public static JSONObject programm(Karte[] karten) {
		int[] prioritaeten = new int[karten.length];
		for (int i = 0; i < karten.length; ++i) {
			prioritaeten[i] = karten[i].prioritaet;
		}
		
		JSONObject result = new JSONObject();
		result.put("programm", prioritaeten);
		return result;
	}
	
	public static JSONObject gebote(Map<Integer, Integer> gebote) {
		
		JSONObject result = new JSONObject();
		JSONArray geboteJSON = new JSONArray();
		for (Map.Entry<Integer, Integer> gebot : gebote.entrySet()) {
			JSONObject gebotJSON = new JSONObject();
			gebotJSON.put("karte", gebot.getKey());
			gebotJSON.put("gebot", gebot.getValue());
			geboteJSON.put(gebotJSON);
		}
		result.put("gebote", geboteJSON);		
		return result;		
		
	}

	public static JSONObject powerdown(boolean powerdown) {
		
		JSONObject result = new JSONObject();
		result.put("powerDown", powerdown);
		return result;
		
	}
	
}
