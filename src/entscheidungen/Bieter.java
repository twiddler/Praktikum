package entscheidungen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spiellogik.Karte;
import spiellogik.Spielzustand;

public class Bieter {

	public static Map<Integer, Integer> gebote(final Spielzustand zustand, final List<Karte> bietoptionen,
			Entscheider entscheider) {
		zustand.roboter[0].karten.addAll(bietoptionen);
		Karte[] ausgewaehlteKarten = entscheider.entscheiden(zustand);
		List<Karte> zuBieteneKarten = new ArrayList<Karte>();
		for (final Karte karte : bietoptionen) {
			for (Karte ausgewaehlteKarte : ausgewaehlteKarten) {
				if (karte.prioritaet == ausgewaehlteKarte.prioritaet) {
					zuBieteneKarten.add(karte);
				}
			}
		}
		int bietBetrag = (zustand.roboter[0].geld - 50) / zuBieteneKarten.size();
		final Map<Integer, Integer> result = new HashMap<>();
		for (final Karte karte : zuBieteneKarten) {
			result.put(karte.prioritaet, bietBetrag);
		}
		return result;
	}

}