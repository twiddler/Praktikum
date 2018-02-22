package netzwerk;

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

}
