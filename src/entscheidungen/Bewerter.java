package entscheidungen;

import spiellogik.Parameter;
import spiellogik.Roboter;
import spiellogik.Spielzustand;

public class Bewerter {

	/**
	 * Länge der Bewertungstupel. Nötig für die Generierung des schlechtesten
	 * und besten Wertes.
	 **/
	final int anzahlBewertungen = 8;

	int[] bewerten(Spielzustand zustand) {
		Roboter wir = zustand.roboter[0];
		Roboter gegner = zustand.roboter[1];

		boolean wirGewinnen = wir.naechsteFlagge == Parameter.ANZAHL_FLAGGEN;
		boolean gegnerGewinntNicht = gegner.naechsteFlagge < Parameter.ANZAHL_FLAGGEN;
		boolean wirLebenNoch = wir.leben > 0;
		boolean gegnerLebtNichtMehr = gegner.leben == 0;
		int unsereNaechsteFlagge = wir.naechsteFlagge;
		int seineNaechsteFlagge = -gegner.naechsteFlagge;
		int unserAbstandZurNaechstenFlagge = zustand.abstandZurNaechstenFlagge(0);
		int gegnerAbstandZurNaechstenFlagge = zustand.abstandZurNaechstenFlagge(1);

		return new int[] { wirGewinnen ? 1 : 0, gegnerGewinntNicht ? 1 : 0, wirLebenNoch ? 1 : 0,
				gegnerLebtNichtMehr ? 1 : 0, unsereNaechsteFlagge, seineNaechsteFlagge, unserAbstandZurNaechstenFlagge,
				gegnerAbstandZurNaechstenFlagge };
	}

	boolean istBesser(int[] a, int[] b) {
		for (int i = 0; i < a.length; ++i) {
			if (a[i] > b[i])
				return true;
		}
		return false;
	}

	boolean istSchlechter(int[] a, int[] b) {
		for (int i = 0; i < a.length; ++i) {
			if (a[i] < b[i])
				return true;
		}
		return false;
	}

	int[] besseres(int[] a, int[] b) {
		return istBesser(a, b) ? a : b;
	}

	int[] schlechteres(int[] a, int[] b) {
		return istBesser(b, a) ? a : b;
	}

	int[] besterWert() {
		int[] result = new int[anzahlBewertungen];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.MAX_VALUE;
		}
		return result;
	}

	int[] schlechtesterWert() {
		int[] result = new int[anzahlBewertungen];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.MIN_VALUE;
		}
		return result;
	}

}