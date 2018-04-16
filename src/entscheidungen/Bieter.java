package entscheidungen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spiellogik.Karte;
import spiellogik.Spielzustand;

public class Bieter {

	/**
	 * Führt den Entscheider mit (den Handkarten und zstzl.) den Bietoptionen aus,
	 * und schaut welche Karten der Bietoption benutzt werden. Auf diese wird
	 * geboten.
	 */
	public static Map<Integer, Integer> gebote(final Spielzustand zustand, final List<Karte> bietoptionen,
			Entscheider entscheider) {

		// Wenn wir alleine sind kaufen wir einfach alles
		if (zustand.roboter[1].leben == 0) {
			final Map<Integer, Integer> result = new HashMap<>();
			for (final Karte karte : bietoptionen) {
				result.put(karte.prioritaet, 1);
			}
			return result;
		}

		// Ansonsten schauen wir welche wir benutzen würden wenn wir alle haben
		final Spielzustand zustandAllesGekauft = zustand.clone();
		zustandAllesGekauft.roboter[0].karten.addAll(bietoptionen);
		Karte[] ausgewaehlteKarten = entscheider.entscheiden(zustandAllesGekauft);
		List<Karte> zuBietendeKarten = new ArrayList<Karte>();
		for (final Karte karte : bietoptionen) {
			for (final Karte ausgewaehlteKarte : ausgewaehlteKarten) {
				if (karte.prioritaet == ausgewaehlteKarte.prioritaet) {
					zuBietendeKarten.add(karte);
				}
			}
		}

		// Wir bieten auf alle gleich viel und behalten ein bisschen als Reserve
		final Map<Integer, Integer> result = new HashMap<>();
		if (zuBietendeKarten.size() > 0) {
			int bietBetrag = (zustandAllesGekauft.roboter[0].geld - 50) / zuBietendeKarten.size();
			for (final Karte karte : zuBietendeKarten) {
				result.put(karte.prioritaet, bietBetrag);
			}
		}
		return result;

	}

}