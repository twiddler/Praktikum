package entscheidungen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import spiellogik.Karte;
import spiellogik.Spielzustand;

public class Bieter {

	public static Map<Integer, Integer> gebote(Spielzustand zustand, ArrayList<Karte> bietoptionen) {

		Map<Integer, Integer> result = new HashMap<>();

		for (Karte karte : bietoptionen) {
			result.put(karte.prioritaet, 0);
		}

		return result;

	}

}
