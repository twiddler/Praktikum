import java.util.*;

class Entscheider {

	Knoten wurzel;
	Bewerter bewerter;

	Entscheider(Bewerter bewerter) {
		this.bewerter = bewerter;
	}

	int[] min_value(Knoten knoten, int tiefe, int prioritaet, int[] alpha, int[] beta) {
		int[] result = bewerter.besterWert();

		for (Karte karte : knoten.zustand.roboter[0].karten) {
			if (karte.prioritaet < prioritaet) {
				Knoten kind = knoten.kindMitKarte(0, karte);
				kind.bewertung = idk_value(kind, tiefe - 1, alpha, beta);

				if (bewerter.istSchlechter(kind.bewertung, result)) {
					knoten.nachfolger = kind;
					result = kind.bewertung;
				}

				if (bewerter.istSchlechter(kind.bewertung, alpha)) {
					return result;
				}

				if (bewerter.istSchlechter(kind.bewertung, beta)) {
					beta = kind.bewertung;
				}
			}
		}

		return result;
	}

	int[] max_value(Knoten knoten, int tiefe, int prioritaet, int[] alpha, int[] beta) {
		int[] result = bewerter.schlechtesterWert();

		for (Karte karte : knoten.zustand.roboter[1].karten) {
			if (karte.prioritaet < prioritaet) {
				Knoten kind = knoten.kindMitKarte(1, karte);
				kind.bewertung = idk_value(kind, tiefe - 1, alpha, beta);

				if (bewerter.istBesser(kind.bewertung, result)) {
					knoten.nachfolger = kind;
					result = kind.bewertung;
				}

				if (bewerter.istBesser(kind.bewertung, beta)) {
					return result;
				}

				if (bewerter.istBesser(kind.bewertung, alpha)) {
					alpha = kind.bewertung;
				}
			}
		}

		return result;
	}

	int[] idk_value(Knoten knoten, int tiefe, int[] alpha, int[] beta) {
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
				kind.bewertung = min_value(kind, tiefe, karte.prioritaet, alpha, beta);
				netteKinder.put(karte.prioritaet, kind);
			}
		}

		// Gegnerische Zuege
		TreeMap<Integer, Knoten> bloedeKinder = new TreeMap<>();
		for (Karte karte : knoten.zustand.roboter[1].karten) {
			if (karte.prioritaet < min_prioritaet[0]) {
				Knoten kind = knoten.kindMitKarte(1, karte);
				kind.bewertung = max_value(kind, tiefe, karte.prioritaet, alpha, beta);
				bloedeKinder.put(karte.prioritaet, kind);
			}
		}

		return welchesKind(netteKinder, bloedeKinder).bewertung;
	}

	/**
	 * Aus zwei Listen von netten und bl�den Kindern, jeweils aufsteigend sortiert
	 * nach Priorit�t, bestimmt diese Funktion das Kind was sich durchsetzt.
	 */
	Knoten welchesKind(TreeMap<Integer, Knoten> netteKinder, TreeMap<Integer, Knoten> bloedeKinder) {

		// Ist das schnellste Kind nett oder bl�d?
		boolean vertrittUns = bewerter.istBesser(netteKinder.lastEntry().getValue().bewertung,
				bloedeKinder.lastEntry().getValue().bewertung);

		// Entferne �berfl�ssige Kinder aus den Listen
		ArrayList<Knoten> vertreterListe;
		if (vertrittUns) {
			vertreterListe = vertreterWaehlen(netteKinder, bloedeKinder, vertrittUns);
		} else {
			vertreterListe = vertreterWaehlen(bloedeKinder, netteKinder, vertrittUns);
		}

		// Suche jetzt das Kind raus
		final boolean ungeradeAnzahl = vertreterListe.size() % 2 == 1;
		vertrittUns = ungeradeAnzahl ? vertrittUns : !vertrittUns;
		Knoten result = vertreterListe.remove(vertreterListe.size() - 1);
		for (int i = 0; i < vertreterListe.size(); ++i) {

			Knoten vergleich = vertreterListe.remove(vertreterListe.size() - 1);
			if (i % 2 == 0 ^ vertrittUns ? bewerter.istBesser(vergleich.bewertung, result.bewertung)
					: bewerter.istSchlechter(vergleich.bewertung, result.bewertung)) {
				result = vergleich;
			}

		}

		return result;

	}

	/**
	 * W�hlt aus einer nach aufsteigender Priorit�t sortierten Liste von netten und
	 * bl�den Kindern die vertretenden Kinder aus. Bspw. wird aus einer nach
	 * aufsteigender Priorit�t sortierte Liste [nett1, nett2, nett3, bl�d1, bl�d2,
	 * nett4] die Liste [nett2, bl�d1, nett4], wobei die Bewertung bl�d1 < bl�d2,
	 * nett2 > nett1 und nett2 > nett3.
	 * 
	 * Die �bergebenen Listen werden von dieser Funktion ver�ndert!
	 * 
	 * @param m
	 *            Aus den letzten Elementen dieser Liste, beschr�nkt durch das
	 *            letzte Element aus c, wird das Element ausgew�hlt, welches die
	 *            beste/schlechte Wertung hat.
	 * @param limiter
	 *            Das letzte Element dieser Liste limitiert die zu betrachtenden
	 *            Werte aus m.
	 */
	ArrayList<Knoten> vertreterWaehlen(TreeMap<Integer, Knoten> entsender, TreeMap<Integer, Knoten> limiter,
			boolean vertrittUns) {

		// Priorit�t zum Trennen bestimmen, bzw. abbrechen
		Integer prioritaet = Integer.MIN_VALUE;
		try {
			prioritaet = limiter.lastKey();
		} catch (NoSuchElementException e) {
			if (entsender.isEmpty()) {
				return new ArrayList<Knoten>();
			}
		}

		// Vertreter der Knoten oberhalb der Priorit�t bestimmen
		Knoten vertreter = null;
		SortedMap<Integer, Knoten> kandidaten = entsender.tailMap(prioritaet);
		while (!kandidaten.isEmpty()) {
			Knoten kandidat = entsender.pollLastEntry().getValue();
			if (vertreter == null || (vertrittUns ? this.bewerter.istBesser(kandidat.bewertung, vertreter.bewertung)
					: this.bewerter.istSchlechter(kandidat.bewertung, vertreter.bewertung))) {
				vertreter = kandidat;
			}
		}

		// Rekursive Ergebnisse zusammensetzen
		ArrayList<Knoten> result = new ArrayList<Knoten>();
		result.add(vertreter);
		result.addAll(vertreterWaehlen(limiter, entsender, !vertrittUns));
		return result;

	}

	/**
	 * Durchl�uft von der Wurzel aus die Nachfolger im Spielbaum und gibt die Karten
	 * aus, die auf diesem Weg gespielt wurden.
	 */
	Karte[] zuSpielendeKarten() {
		Karte[] result = new Karte[Parameter.ZUEGE_PRO_RUNDE];
		Knoten n = this.wurzel;
		for (int i = 0; i < result.length; i++) {
			n = n.nachfolger;
			result[i] = n.karte;
		}
		return result;
	}

	/**
	 * Bestimmt von der Wurzel aus die Nachfolger, also die Spielzust�nde, die bei
	 * rationalem Verhalten beider Spieler durchlaufen werden.
	 */
	void zuegeAnalysieren() {
		this.idk_value(this.wurzel, Parameter.ZUEGE_PRO_RUNDE, this.bewerter.schlechtesterWert(),
				this.bewerter.besterWert());
	}

	/**
	 * Gegeben einen Spielzustand, gibt uns diese Funktion die Karten, die wir
	 * spielen sollen.
	 */
	Karte[] entscheiden(Spielzustand zustand) {
		this.wurzel = new Knoten(zustand);
		this.zuegeAnalysieren();
		return zuSpielendeKarten();
	}
}