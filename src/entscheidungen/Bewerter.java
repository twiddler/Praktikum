package entscheidungen;

import spiellogik.Parameter;
import spiellogik.Roboter;
import spiellogik.Spielzustand;

public class Bewerter {

	/**
	 * Länge der Bewertungstupel. Nötig für die Generierung des schlechtesten und
	 * besten Wertes.
	 **/
	final int anzahlBewertungen = 8;

	int[] bewerten(final Spielzustand zustand) {
		final Roboter wir = zustand.roboter[0];
		final Roboter gegner = zustand.roboter[1];

		final boolean wirGewinnen = wir.naechsteFlagge == Parameter.ANZAHL_FLAGGEN;
		final boolean gegnerGewinntNicht = gegner.naechsteFlagge < Parameter.ANZAHL_FLAGGEN;
		final boolean wirLebenNoch = wir.leben > 0;
		final boolean gegnerLebtNichtMehr = gegner.leben == 0;
		final int unsereNaechsteFlagge = wir.naechsteFlagge;
		final int gegnerNaechsteFlagge = -gegner.naechsteFlagge;
		final int unsereGesundheit = wir.gesundheit;
		final int gegnerGesundheit = -gegner.gesundheit;
		// int unserAbstandZurNaechstenFlagge = zustand.abstandZurNaechstenFlagge(0);
		// int gegnerAbstandZurNaechstenFlagge = zustand.abstandZurNaechstenFlagge(1);

		return new int[] { wirGewinnen ? 1 : 0, gegnerGewinntNicht ? 1 : 0, wirLebenNoch ? 1 : 0,
				gegnerLebtNichtMehr ? 1 : 0, unsereNaechsteFlagge, gegnerNaechsteFlagge, unsereGesundheit,
				gegnerGesundheit };
		// unserAbstandZurNaechstenFlagge, gegnerAbstandZurNaechstenFlagge };
	}

	boolean istBesser(final int[] a, final int[] b) {
		for (int i = 0; i < a.length; ++i) {
			if (a[i] > b[i])
				return true;
		}
		return false;
	}

	boolean istSchlechter(final int[] a, final int[] b) {
		for (int i = 0; i < a.length; ++i) {
			if (a[i] < b[i])
				return true;
		}
		return false;
	}

	int[] besseres(final int[] a, final int[] b) {
		return istBesser(a, b) ? a : b;
	}

	int[] schlechteres(final int[] a, final int[] b) {
		return istBesser(b, a) ? a : b;
	}

	int[] besterWert() {
		final int[] result = new int[anzahlBewertungen];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.MAX_VALUE;
		}
		return result;
	}

	int[] schlechtesterWert() {
		final int[] result = new int[anzahlBewertungen];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.MIN_VALUE;
		}
		return result;
	}

}