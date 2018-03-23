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
		int freieSlots = Math.min(zustand.roboter[0].gesundheit - 1, Parameter.ZUEGE_PRO_RUNDE);
		List<ArrayList<Karte>> per = allPermutationsof(freieSlots, zustand.roboter[0].karten);
		List<Karte> bestOne = new ArrayList<>();
		int[] bestBewertung = bewerter.schlechtesterWert;
		for (List<Karte> aktuell : per) {
			Spielzustand aktuellerZustand = zustand;
			for (int i = 0; i < Parameter.ZUEGE_PRO_RUNDE; i++) {
				if (i < freieSlots) {
					aktuellerZustand = zustand.karteSpielen(0, aktuell.get(i));
				} else {
					aktuellerZustand = zustand.karteSpielen(0, zustand.roboter[0].gesperrteKarten.get(i - freieSlots));
				}
			}
			int[] aktuelleBewertung = bewerter.bewerten(aktuellerZustand);
			if (bewerter.istBesser(aktuelleBewertung, bestBewertung)) {
				bestBewertung = aktuelleBewertung;
				bestOne = aktuell;
			}

		}
		Karte[] result = new Karte[bestOne.size()];
		for (int i = 0; i < bestOne.size(); i++) {
			result[i] = bestOne.get(i);
		}
		return result;
	}

	public List<ArrayList<Karte>> allPermutationsof(int tiefe, List<Karte> karten) {
		List<ArrayList<Karte>> result = new ArrayList<>();
		if (tiefe == 0) {
			return result;
		}
		if (tiefe == 1) {
			for (int i = 0; i < karten.size(); i++) {
				ArrayList<Karte> newList = new ArrayList<Karte>();
				newList.add(karten.get(i));
				result.add(newList);
			}
		} else {
			List<ArrayList<Karte>> nextLayer = allPermutationsof(tiefe - 1, karten);
			for (ArrayList<Karte> list : nextLayer) {
				for (Karte k : karten) {
					ArrayList<Karte> newList = (ArrayList<Karte>) list.clone();
					if (!newList.contains(k)) {
						newList.add(k);
						result.add(newList);
					}

				}
			}
		}
		return result;
	}

	@Override
	public boolean powerdown(Spielzustand zustand) {
		return false;
	}

}
