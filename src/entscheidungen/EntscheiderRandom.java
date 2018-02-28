package entscheidungen;

import spiellogik.Karte;
import spiellogik.Parameter;
import spiellogik.Spielzustand;

public class EntscheiderRandom extends Entscheider {

	public EntscheiderRandom(Bewerter bewerter) {
		super(bewerter);
	}

	@Override
	public Karte[] entscheiden(Spielzustand zustand) {
		int freieSlots = Math.min(zustand.roboter[0].gesundheit - 1, Parameter.ZUEGE_PRO_RUNDE);
		Karte[] result = new Karte[freieSlots];
		for(int i = 0; i < result.length; i++){
			result[i] = zustand.roboter[0].karten.get(i);
		}
		return result;
	}

	@Override
	public boolean powerdown(Spielzustand zustand) {
		return false;
	}

}
