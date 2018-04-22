package entscheidungen;

import java.util.ArrayList;
import java.util.List;

import spiellogik.Karte;
import spiellogik.Parameter;
import spiellogik.Spielzustand;

public class EntscheiderMDFFMN extends Entscheider {

	public EntscheiderMDFFMN(Bewerter bewerter) {
		super(bewerter);
	}

	@Override
	public Karte[] entscheiden(final Spielzustand zustand) {
		long start = System.nanoTime();
		long ende = start + 58L * 1000000000L;
		List<Karte> bestePermutation = new ArrayList<>();
		int[] besteBewertung = this.bewerter.schlechtesterWert;

		final int freieSlots = Math.min(zustand.roboter[0].gesundheit - 1, Parameter.ZUEGE_PRO_RUNDE);
		final List<ArrayList<Karte>> permutationen = permutationenIterativ(freieSlots, zustand.roboter[0].karten);

		for (final List<Karte> permutation : permutationen) {
			if (System.nanoTime() >= ende)
				break;
			Spielzustand zustandMitPermutation = zustand;
			for (int i = 0; i < Parameter.ZUEGE_PRO_RUNDE; ++i) {
				// Die dummy-Karte muss gespielt werden, da karteSpielen() nach
				// jeder 2ten Karte
				// den Zug beendet (Aktionsfelder, ...)
				zustandMitPermutation = zustand
						.karteSpielen(0,
								i < freieSlots ? permutation.get(i) : zustand.roboter[0].spielbareKarten(i).get(0))
						.karteSpielen(1, Karte.dummy);
			}

			final int[] bewertung = this.bewerter.bewerten(zustandMitPermutation);
			if (this.bewerter.istBesser(bewertung, besteBewertung)) {
				besteBewertung = bewertung;
				bestePermutation = permutation;
			}
		}

		final Karte[] result = new Karte[bestePermutation.size()];
		for (int i = 0; i < bestePermutation.size(); i++) {
			result[i] = bestePermutation.get(i);
		}
		return result;
	}

	public List<ArrayList<Karte>> permutationen(int tiefe, List<Karte> karten) {
		List<ArrayList<Karte>> result = new ArrayList<>();
		if (tiefe == 0) {
		} else if (tiefe == 1) {
			for (final Karte karte : karten) {
				ArrayList<Karte> permutation = new ArrayList<Karte>();
				permutation.add(karte);
				result.add(permutation);
			}
		} else {
			List<ArrayList<Karte>> naechsteEbene = permutationen(tiefe - 1, karten);
			for (ArrayList<Karte> permutation : naechsteEbene) {
				for (Karte karte : karten) {
					if (!permutation.contains(karte)) {
						ArrayList<Karte> erweitertePermutation = (ArrayList<Karte>) permutation.clone();
						erweitertePermutation.add(karte);
						result.add(erweitertePermutation);
					}
				}
			}
		}
		return result;
	}

	public List<ArrayList<Karte>> permutationenIterativ(int laenge, List<Karte> karten) {

		List<ArrayList<Karte>> result = new ArrayList<>();

		for (final Karte karte : karten) {
			ArrayList<Karte> permutation = new ArrayList<>();
			permutation.add(karte);
			result.add(permutation);
		}

		for (int i = 1; i < laenge; ++i) {
			List<ArrayList<Karte>> erweitertePermutationen = new ArrayList<>();

			for (ArrayList<Karte> permutation : result) {
				for (final Karte karte : karten) {
					if (!permutation.contains(karte)) {
						ArrayList<Karte> erweitertePermutation = (ArrayList<Karte>) permutation.clone();
						erweitertePermutation.add(karte);
						erweitertePermutationen.add(erweitertePermutation);
					}
				}
			}

			result = erweitertePermutationen;
		}
		return result;
	}

	@Override
	public boolean powerdown(Spielzustand zustand) {
		return zustand.roboter[0].gesundheit < Parameter.MAX_GESUNDHEIT * 0.4;
	}

}
