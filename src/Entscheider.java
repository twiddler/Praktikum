import java.util.*;

class Entscheider {

	Knoten wurzel;
	Bewerter bewerter;

	Entscheider(Spielzustand zustand) {
		this.wurzel = new Knoten(zustand);
	}

	int[] min_value(Knoten knoten, int prioritaet) {
		int[] result = bewerter.besterWert();

		for (Karte karte : knoten.zustand.roboter[0].karten) {
			if (karte.prioritaet < prioritaet) {
				Knoten kind = knoten.kindMitKarte(0, karte);
				kind.bewertung = idk_value(kind);
				if (bewerter.istSchlechter(kind.bewertung, result)) {
					knoten.nachfolger = kind;
					result = kind.bewertung;
				}
			}
		}

		return result;
	}

	int[] max_value(Knoten knoten, int prioritaet) {
		int[] result = bewerter.schlechtesterWert();

		for (Karte karte : knoten.zustand.roboter[1].karten) {
			if (karte.prioritaet < prioritaet) {
				Knoten kind = knoten.kindMitKarte(1, karte);
				kind.bewertung = idk_value(kind);
				if (bewerter.istBesser(kind.bewertung, result)) {
					knoten.nachfolger = kind;
					result = kind.bewertung;
				}
			}
		}

		return result;
	}

	int[] idk_value(Knoten knoten) {
		/*
		 * TODO: Abbruchbedingung
		 */

		int[] result = new int[] {};

		// Unsere Zuege
		TreeMap<Integer, Knoten> netteKinder = new TreeMap<>();
		for (Karte karte : knoten.zustand.roboter[0].karten) {
			Knoten kind = knoten.kindMitKarte(0, karte);
			kind.bewertung = min_value(kind, karte.prioritaet);
			netteKinder.put(karte.prioritaet, kind);
		}

		// Gegnerische Zuege
		TreeMap<Integer, Knoten> bloedeKinder = new TreeMap<>();
		for (Karte karte : knoten.zustand.roboter[1].karten) {
			Knoten kind = knoten.kindMitKarte(1, karte);
			kind.bewertung = max_value(kind, karte.prioritaet);
			bloedeKinder.put(karte.prioritaet, kind);
		}

		// Einfarbige Abschnitte zusammenfassen

		return result;
	}

	/**
	 * In einer Liste von netten und blöden Kinder, die neben netten und blöden
	 * Kindern stehen, gibt das hier eine Liste mit den nettesten und allerblödsten
	 * Kindern zurück.
	 */
	Knoten vertreterWaehlen(TreeMap<Integer, Knoten> netteKinder, TreeMap<Integer, Knoten> bloedeKinder) {
		
		boolean vertrittUns = bewerter.istBesser(netteKinder.lastEntry().getValue().bewertung,
				bloedeKinder.lastEntry().getValue().bewertung);
		
		ArrayList<Knoten> vertreterListe;
		if (vertrittUns) {
			vertreterListe = vertreterWaehlenHelfer(netteKinder, bloedeKinder, vertrittUns);
		} else {
			vertreterListe = vertreterWaehlenHelfer(bloedeKinder, netteKinder, !vertrittUns);
		}

		Knoten result = vertreterListe.remove(0);
		int[] min = result.bewertung;
		int[] max = min;
		while (!vertreterListe.isEmpty()) {
			vertrittUns = !vertrittUns;
			Knoten vertreter = vertreterListe.remove(0);

			if (!vertrittUns) {
				
			} else {
				if (bewerter.istBesser(vertreter.bewertung, min)) {
					break;
				}
				min = bewerter.schlechteres(min, vertreter.bewertung);
			}
			result = vertreter;
		}
		return result;
		
	}

	/**
	 * @param m
	 *            Aus den letzten Elementen dieser Liste, beschränkt durch das
	 *            letzte Element aus c, wird das Element ausgewählt, welches die
	 *            beste/schlechte Wertung hat.
	 * @param limiter
	 *            Das letzte Element dieser Liste limitiert die zu betrachtenden
	 *            Werte aus m.
	 */
	ArrayList<Knoten> vertreterWaehlenHelfer(TreeMap<Integer, Knoten> entsender, TreeMap<Integer, Knoten> limiter,
			boolean vertrittUns) {

		// Priorität zum Trennen bestimmen, bzw. abbrechen
		Integer prioritaet = Integer.MIN_VALUE;
		try {
			prioritaet = limiter.lastKey();
		} catch (NoSuchElementException e) {
			if (entsender.isEmpty()) {
				return new ArrayList<Knoten>();
			}
		}

		// Vertreter der Knoten oberhalb der Priorität bestimmen
		Knoten vertreter = null;
		SortedMap<Integer, Knoten> kandidaten = entsender.tailMap(prioritaet);
		while (!kandidaten.isEmpty()) {
			Knoten kandidat = entsender.pollLastEntry().getValue();
			if (kandidat == null || (vertrittUns ? bewerter.istBesser(kandidat.bewertung, vertreter.bewertung)
					: bewerter.istSchlechter(kandidat.bewertung, vertreter.bewertung))) {
				vertreter = kandidat;
			}
		}

		// Rekursive Ergebnisse zusammensetzen
		ArrayList<Knoten> result = new ArrayList<Knoten>();
		result.add(vertreter);
		result.addAll(vertreterWaehlenHelfer(limiter, entsender, !vertrittUns));
		return result;

	}

	Karte[] zuspielendeKarten() {
		Karte[] result = new Karte[Parameter.ZUEGE_PRO_RUNDE];
		Knoten n = this.wurzel;
		for (int i = 0; i < result.length; i++) {
			n = n.nachfolger;
			result[i] = n.karte;
		}
		return result;
	}

	Karte[] entscheiden(Spielzustand zustand) {
		int[] __ = this.idk_value(this.wurzel);
		return zuspielendeKarten();
	}
}