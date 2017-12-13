package entscheidungen;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import spiellogik.Karte;
import spiellogik.Parameter;
import spiellogik.Spielzustand;

class Entscheider {

	Knoten wurzel;
	Bewerter bewerter;

	Entscheider(Bewerter bewerter) {
		this.bewerter = bewerter;
	}

	int[] min_value(Knoten knoten, int tiefe, int prioritaet, int[] alpha, int[] beta) {
		int[] result = bewerter.besterWert();

		for (Karte karte : knoten.zustand.roboter[1].karten) {
			if (karte.prioritaet < prioritaet) {
				Knoten kind = knoten.kindMitKarte(1, karte);
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

		for (Karte karte : knoten.zustand.roboter[0].karten) {
			if (karte.prioritaet < prioritaet) {
				Knoten kind = knoten.kindMitKarte(0, karte);
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
			if (karte.prioritaet > min_prioritaet[1]) {
				Knoten kind = knoten.kindMitKarte(0, karte);
				kind.bewertung = min_value(kind, tiefe, karte.prioritaet, alpha, beta);
				netteKinder.put(karte.prioritaet, kind);
			}
		}

		// Gegnerische Zuege
		TreeMap<Integer, Knoten> bloedeKinder = new TreeMap<>();
		for (Karte karte : knoten.zustand.roboter[1].karten) {
			if (karte.prioritaet > min_prioritaet[0]) {
				Knoten kind = knoten.kindMitKarte(1, karte);
				kind.bewertung = max_value(kind, tiefe, karte.prioritaet, alpha, beta);
				bloedeKinder.put(karte.prioritaet, kind);
			}
		}
		knoten.nachfolger = welchesKind(netteKinder, bloedeKinder);
		return knoten.nachfolger.bewertung;
	}

	/**
	 * Aus zwei Listen von netten und blöden Kindern, jeweils aufsteigend sortiert
	 * nach Priorität, bestimmt diese Funktion das Kind was sich durchsetzt.
	 */
	Knoten welchesKind(TreeMap<Integer, Knoten> netteKinder, TreeMap<Integer, Knoten> bloedeKinder) {

		ArrayList<Knoten> vertreterListe = vertreterWaehlen(netteKinder, bloedeKinder);

		Knoten result = vertreterListe.remove(vertreterListe.size() - 1);
		while (!vertreterListe.isEmpty()) {
			Knoten vergleich = vertreterListe.remove(vertreterListe.size() - 1);
			if (vergleich.spieler == 0 ? bewerter.istBesser(vergleich.bewertung, result.bewertung)
					: bewerter.istSchlechter(vergleich.bewertung, result.bewertung)) {
				result = vergleich;
			}
		}

		return result;

	}

	/**
	 * Wählt aus zwei nach aufsteigender Priorität sortierten Listen von netten und
	 * blöden Kindern die vertretenden Kinder aus. Ist bspw. die nach aufsteigender
	 * Priorität sortierte, vereinigte Liste [nett1, nett2, nett3, blöd1, blöd2,
	 * nett4] mit den Bewertungen nett2 > nett1, nett2 > nett3, blöd1 < blöd2, dann
	 * ist das Ergebnis dieser Funktion [nett2, blöd1, nett4].
	 * 
	 * Die übergebenen Listen werden von dieser Funktion verändert!
	 */
	ArrayList<Knoten> vertreterWaehlen(TreeMap<Integer, Knoten> netteKinder, TreeMap<Integer, Knoten> bloedeKinder) {

		// Ist das schnellste Kind nett oder blöd?
		boolean vertrittUns = netteKinder.lastEntry().getKey() > bloedeKinder.lastEntry().getKey();

		if (vertrittUns) {
			return vertreterWaehlen(netteKinder, bloedeKinder, vertrittUns);
		} else {
			return vertreterWaehlen(bloedeKinder, netteKinder, vertrittUns);
		}

	}

	/**
	 * Sucht anhand des Elements mit höchster Priorität in limiter den bis dahin
	 * zusammenhängenden Bereich in entsender. Je nachdem welche der beiden Listen
	 * die netten oder blöden Kinder sind, wird eine andere Bewertungsfunktion
	 * benutzt. Die Funktion ruft sich dann mit getauschten Parametern selbst auf,
	 * bis die Liste leer ist.
	 * 
	 * @param entsender
	 *            Aus den letzten Elementen dieser Liste, beschränkt durch das
	 *            letzte Element aus limiter, wird das Element ausgewählt, welches
	 *            die beste/schlechte Wertung hat.
	 * @param limiter
	 *            Das letzte Element dieser Liste limitiert die zu betrachtenden
	 *            Werte aus entsender.
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
	 * Durchläuft von der Wurzel aus die Nachfolger im Spielbaum und gibt die Karten
	 * aus, die auf diesem Weg gespielt wurden.
	 */
	Karte[] zuSpielendeKarten() {
		Karte[] result = new Karte[2 * Parameter.ZUEGE_PRO_RUNDE];
		Knoten n = this.wurzel;
		for (int i = 0; i < result.length; i++) {
			n = n.nachfolger;
			if (n.spieler == 0) {
				result[i] = n.karte;
			}
		}
		return result;
	}

	/**
	 * Bestimmt von der Wurzel aus die Nachfolger, also die Spielzustände, die bei
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