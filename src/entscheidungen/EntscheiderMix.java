package entscheidungen;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import spiellogik.Karte;
import spiellogik.Parameter;
import spiellogik.Roboter;
import spiellogik.Spielzustand;

/**
 * �hnlich wie Minimax, allerdings nur auf jeder zweiten Ebene, da aufgrund der
 * Priorit�ten von Karten auf den anderen Ebenen beide Spieler betrachtet werden
 * m�ssen. W�rde bspw. Spieler 1 die h�chste Karte spielen, m�ssten danach nur
 * noch Karten von Spieler 2 betrachtet werden.
 * 
 * (Die Laufzeit dieses Algorithmus ist leider zu schlecht. Mit 9 Karten in
 * jeder Hand und 5 Z�gen ben�tigten wir 4,5 min.)
 * 
 * @author xXx Players xXx
 * 
 */
public class EntscheiderMix extends Entscheider {

	Knoten wurzel;

	public EntscheiderMix(Bewerter bewerter) {
		super(bewerter);
	}

	int[] min_value(Knoten knoten, int tiefe, int prioritaet, int[] alpha, int[] beta) {
		int[] result = bewerter.besterWert;

		for (Karte karte : knoten.zustand.roboter[1].spielbareKarten(Parameter.ZUEGE_PRO_RUNDE - tiefe)) {
			if (karte.prioritaet < prioritaet) {
				Knoten kind = knoten.kindMitKarte(1, karte);
				kind.bewertung = mix_value(kind, tiefe - 1, alpha, beta);

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
		int[] result = bewerter.schlechtesterWert;

		for (Karte karte : knoten.zustand.roboter[0].spielbareKarten(Parameter.ZUEGE_PRO_RUNDE - tiefe)) {
			if (karte.prioritaet < prioritaet) {
				Knoten kind = knoten.kindMitKarte(0, karte);
				kind.bewertung = mix_value(kind, tiefe - 1, alpha, beta);

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

	// int iterationen = 0;

	int[] mix_value(Knoten knoten, int tiefe, int[] alpha, int[] beta) {

		// Nur zum Absch�tzen wie lange das Programm noch l�uft
		// ++iterationen;
		// if (tiefe > 3) {
		// String t = "";
		// for (int i = 0; i < 5 - tiefe; ++i) {
		// t += " ";
		// }
		// t += tiefe;
		// System.out.println(iterationen + ". Runde idkvalue, Tiefe: " + t);
		// }

		if (tiefe == 0) {
			return bewerter.bewerten(knoten.zustand);
		}

		// Die jeweils kleinste Priorit�t der Handkarten beider Spieler bestimmen
		int[] min_prioritaet = new int[2];
		for (int i = 0; i < min_prioritaet.length; ++i) {
			min_prioritaet[i] = Integer.MAX_VALUE;
			for (Karte karte : knoten.zustand.roboter[i].spielbareKarten(Parameter.ZUEGE_PRO_RUNDE - tiefe)) {
				if (karte.prioritaet < min_prioritaet[i]) {
					min_prioritaet[i] = karte.prioritaet;
				}
			}
		}

		// F�r beide Roboter m�gliche Z�ge berechnen
		List<TreeMap<Integer, Knoten>> kinder = new ArrayList<>();
		for (int i = 0; i < 2; ++i) {
			TreeMap<Integer, Knoten> k = new TreeMap<>();
			for (Karte karte : knoten.zustand.roboter[i].spielbareKarten(Parameter.ZUEGE_PRO_RUNDE - tiefe)) {
				if (karte.prioritaet > min_prioritaet[(i + 1) % 2]) {
					Knoten kind = knoten.kindMitKarte(i, karte);
					kind.bewertung = i == 0 ? min_value(kind, tiefe, karte.prioritaet, alpha, beta)
							: max_value(kind, tiefe, karte.prioritaet, alpha, beta);
					k.put(karte.prioritaet, kind);
				}
			}
			kinder.add(k);
		}

		knoten.nachfolger = welchesKind(kinder.get(0), kinder.get(1));
		return knoten.nachfolger.bewertung;
	}

	/**
	 * Aus zwei Listen von netten und bl�den Kindern, jeweils aufsteigend sortiert
	 * nach Priorit�t, bestimmt diese Funktion das Kind was sich durchsetzt.
	 */
	Knoten welchesKind(TreeMap<Integer, Knoten> netteKinder, TreeMap<Integer, Knoten> bloedeKinder) {

		List<Knoten> vertreterListe = vertreterWaehlen(netteKinder, bloedeKinder);

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
	 * W�hlt aus zwei nach aufsteigender Priorit�t sortierten Listen von netten und
	 * bl�den Kindern die vertretenden Kinder aus. Ist bspw. die nach aufsteigender
	 * Priorit�t sortierte, vereinigte Liste [nett1, nett2, nett3, bl�d1, bl�d2,
	 * nett4] mit den Bewertungen nett2 > nett1, nett2 > nett3, bl�d1 < bl�d2, dann
	 * ist das Ergebnis dieser Funktion [nett2, bl�d1, nett4].
	 * 
	 * Die �bergebenen Listen werden von dieser Funktion ver�ndert!
	 */
	List<Knoten> vertreterWaehlen(TreeMap<Integer, Knoten> netteKinder, TreeMap<Integer, Knoten> bloedeKinder) {

		// Ist das schnellste Kind nett oder bl�d?
		assert !netteKinder.isEmpty() || !bloedeKinder.isEmpty();
		boolean vertrittUns;
		if (netteKinder.isEmpty()) {
			vertrittUns = false;
		} else if (bloedeKinder.isEmpty()) {
			vertrittUns = true;
		} else {
			vertrittUns = netteKinder.lastEntry().getKey() > bloedeKinder.lastEntry().getKey();
		}

		TreeMap<Integer, Knoten> entsender = vertrittUns ? netteKinder : bloedeKinder;
		TreeMap<Integer, Knoten> limiter = vertrittUns ? bloedeKinder : netteKinder;
		return vertreterWaehlen(entsender, limiter, vertrittUns);

	}

	/**
	 * Sucht anhand des Elements mit h�chster Priorit�t in limiter den bis dahin
	 * zusammenh�ngenden Bereich in entsender. Je nachdem welche der beiden Listen
	 * die netten oder bl�den Kinder sind, wird eine andere Bewertungsfunktion
	 * benutzt. Die Funktion ruft sich dann mit getauschten Parametern selbst auf,
	 * bis die Liste leer ist.
	 * 
	 * @param entsender
	 *            Aus den letzten Elementen dieser Liste, beschr�nkt durch das
	 *            letzte Element aus limiter, wird das Element ausgew�hlt, welches
	 *            die beste/schlechte Wertung hat.
	 * @param limiter
	 *            Das letzte Element dieser Liste limitiert die zu betrachtenden
	 *            Werte aus entsender.
	 */
	List<Knoten> vertreterWaehlen(TreeMap<Integer, Knoten> entsender, TreeMap<Integer, Knoten> limiter,
			boolean vertrittUns) {

		// Priorit�t zum Trennen bestimmen, bzw. abbrechen
		Integer prioritaet = Integer.MIN_VALUE;
		try {
			prioritaet = limiter.lastKey();
		} catch (NoSuchElementException e) {
			if (entsender.isEmpty()) {
				return new ArrayList<>();
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
		List<Knoten> result = new ArrayList<>();
		result.add(vertreter);
		result.addAll(vertreterWaehlen(limiter, entsender, !vertrittUns));
		return result;

	}

	/**
	 * Durchl�uft von der Wurzel aus die Nachfolger im Spielbaum und gibt die Karten
	 * aus, die auf diesem Weg gespielt wurden. Gesperrte Karten kommen nicht ins
	 * Ergebnis.
	 */
	Karte[] zuSpielendeKarten() {

		Knoten n = this.wurzel;
		Roboter roboter = n.zustand.roboter[0];
		int freieSlots = Parameter.ZUEGE_PRO_RUNDE - (roboter.gesperrteKarten.size());

		Karte[] result = new Karte[freieSlots];
		for (int i = 0; i < result.length; ++i) {
			for (int j = 0; j < 2; ++j) {
				n = n.nachfolger;
				if (n.spieler == 0) {
					result[i] = n.karte;
				}
			}
		}

		return result;
	}

	/**
	 * Bestimmt von der Wurzel aus die Nachfolger, also die Spielzust�nde, die bei
	 * rationalem Verhalten beider Spieler durchlaufen werden.
	 */
	void zuegeAnalysieren() {
		this.mix_value(this.wurzel, Parameter.ZUEGE_PRO_RUNDE, this.bewerter.schlechtesterWert,
				this.bewerter.besterWert);
	}

	/**
	 * Gegeben einen Spielzustand, gibt uns diese Funktion die Karten, die wir
	 * spielen sollen.
	 */
	@Override
	public Karte[] entscheiden(Spielzustand zustand) {
		this.wurzel = new Knoten(zustand);
		this.zuegeAnalysieren();
		return zuSpielendeKarten();
	}

	@Override
	public boolean powerdown(Spielzustand zustand) {
		// Hier w�re es cool gewesen den Effekt des Powerdowns auf den n�chsten Zug
		// abzusch�tzen. Mit einer leeren Karte h�tte das implementierbar sein m�ssen,
		// allerdings w�re die Vorhersage was der Gegner tun w�rde sehr spekulativ
		// geworden. Deshalb hier nach l�ngerem Diskutieren eine etwas einfachere Regel.
		// ;)
		return zustand.roboter[0].gesundheit < Parameter.MAX_GESUNDHEIT * 0.4;
	}
}