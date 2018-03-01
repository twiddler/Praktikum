package entscheidungen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spiellogik.Karte;
import spiellogik.Spielzustand;

public class Bieter {

	public static Map<Integer, Integer> gebote(final Spielzustand zustand, final List<Karte> bietoptionen) {
		final Map<Integer, Integer> result = new HashMap<>();
		for (final Karte karte : bietoptionen) {
			result.put(karte.prioritaet, 0);
		}
		return result;
	}

}