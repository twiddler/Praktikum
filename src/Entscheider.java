import java.util.*;

class Entscheider {

	Knoten wurzel;
	Bewerter bewerter;

	Entscheider(Spielzustand zustand) {
		this.wurzel = new Knoten(zustand);
	}

	int[] min_value(Knoten knoten, int tiefe, int prioritaet) {
		int[] result = bewerter.besterWert();

		for (Karte karte : knoten.zustand.roboter[0].karten) {
			if (karte.prioritaet < prioritaet) {
				Knoten kind = knoten.kindMitKarte(0, karte);
				kind.bewertung = idk_value(kind, tiefe - 1);
				if (bewerter.istSchlechter(kind.bewertung, result)) {
					knoten.nachfolger = kind;
					result = kind.bewertung;
				}
			}
		}

		return result;
	}

	int[] max_value(Knoten knoten, int tiefe, int prioritaet) {
		int[] result = bewerter.schlechtesterWert();

		for (Karte karte : knoten.zustand.roboter[1].karten) {
			if (karte.prioritaet < prioritaet) {
				Knoten kind = knoten.kindMitKarte(1, karte);
				kind.bewertung = idk_value(kind, tiefe - 1);
				if (bewerter.istBesser(kind.bewertung, result)) {
					knoten.nachfolger = kind;
					result = kind.bewertung;
				}
			}
		}

		return result;
	}

	int[] idk_value(Knoten knoten, int tiefe) {
		if (tiefe == 0) {
			return bewerter.bewerten(knoten.zustand);
		}

		int[] min_prioritaet = new int[2];
		for (int i = 0; i < min_prioritaet.length; ++i) {
			for (Karte karte : knoten.zustand.roboter[i].karten) {
				if (karte.prioritaet < min_prioritaet[i]) {
					min_prioritaet[i] = karte.prioritaet;
				}
			}
		}

		// Unsere Zuege
		TreeMap<Integer, Knoten> netteKinder = new TreeMap<>();
		for (Karte karte : knoten.zustand.roboter[0].karten) {
			if (karte.prioritaet < min_prioritaet[1]) {
				Knoten kind = knoten.kindMitKarte(0, karte);
				kind.bewertung = min_value(kind, tiefe, karte.prioritaet);
				netteKinder.put(karte.prioritaet, kind);
			}
		}

		// Gegnerische Zuege
		TreeMap<Integer, Knoten> bloedeKinder = new TreeMap<>();
		for (Karte karte : knoten.zustand.roboter[1].karten) {
			if (karte.prioritaet < min_prioritaet[0]) {
				Knoten kind = knoten.kindMitKarte(1, karte);
				kind.bewertung = max_value(kind, tiefe, karte.prioritaet);
				bloedeKinder.put(karte.prioritaet, kind);
			}
		}

		return welchesKind(netteKinder, bloedeKinder).bewertung;
	}

	/**
	 * Aus zwei Listen von netten und blöden Kindern, jeweils aufsteigend sortiert
	 * nach Priorität, bestimmt diese Funktion das Kind was sich durchsetzt.
	 */
	Knoten welchesKind(TreeMap<Integer, Knoten> netteKinder, TreeMap<Integer, Knoten> bloedeKinder) {

		// Ist das schnellste Kind nett oder blöd?
		boolean vertrittUns = bewerter.istBesser(netteKinder.lastEntry().getValue().bewertung,
				bloedeKinder.lastEntry().getValue().bewertung);

		// Entferne überflüssige Kinder aus den Listen
		ArrayList<Knoten> vertreterListe;
		if (vertrittUns) {
			vertreterListe = vertreterWaehlen(netteKinder, bloedeKinder, vertrittUns);
		} else {
			vertreterListe = vertreterWaehlen(bloedeKinder, netteKinder, !vertrittUns);
		}

		// Die Vertreter sind jetzt von der Priorität aufsteigend sortiert
		Knoten result = vertreterListe.remove(0);
		for (int i = 0; i < vertreterListe.size() - 1; ++i) {

			Knoten vergleich = vertreterListe.get(i);
			if (i % 2 == 0 ^ vertrittUns ? bewerter.istBesser(vergleich.bewertung, result.bewertung)
					: bewerter.istSchlechter(vergleich.bewertung, result.bewertung)) {
				result = vergleich;
			}

		}

		return result;

	}

	/**
	 * Wählt aus einer nach aufsteigender Priorität sortierten Liste von netten und
	 * blöden Kindern die vertretenden Kinder aus, und dreht sie um. Bspw. wird aus
	 * einer nach aufsteigender Priorität sortierte Liste [nett1, nett2, nett3,
	 * blöd1, blöd2, nett4] eine nach absteigender Priorität sortierte Liste [nett4,
	 * blöd1, nett2], wobei die Bewertung blöd1 < blöd2, nett2 > nett1 und nett2 >
	 * nett3.
	 * 
	 * Die übergebenen Listen werden von dieser Funktion verändert!
	 * 
	 * @param m
	 *            Aus den letzten Elementen dieser Liste, beschränkt durch das
	 *            letzte Element aus c, wird das Element ausgewählt, welches die
	 *            beste/schlechte Wertung hat.
	 * @param limiter
	 *            Das letzte Element dieser Liste limitiert die zu betrachtenden
	 *            Werte aus m.
	 */
	ArrayList<Knoten> vertreterWaehlen(TreeMap<Integer, Knoten> entsender, TreeMap<Integer, Knoten> limiter,
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
		result.addAll(vertreterWaehlen(limiter, entsender, !vertrittUns));
		result.add(vertreter);
		return result;

	}

	Karte[] zuSpielendeKarten() {
		Karte[] result = new Karte[Parameter.ZUEGE_PRO_RUNDE];
		Knoten n = this.wurzel;
		for (int i = 0; i < result.length; i++) {
			n = n.nachfolger;
			result[i] = n.karte;
		}
		return result;
	}

	Karte[] entscheiden(Spielzustand zustand) {
		this.idk_value(this.wurzel, Parameter.ZUEGE_PRO_RUNDE);
		return zuspielendeKarten();
	}
}