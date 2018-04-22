package entscheidungen;

import spiellogik.Roboter;
import spiellogik.Spielzustand;

/**
 * Stellt Bewertungstupel zum Bewerten von Spielzuständen bereit. Hier wird auch
 * eingestellt, wie bewertet wird, z.B. "Haben wir gewonnen?", "Wieviele Flaggen
 * müssen wir noch einsammeln?", usw..
 * 
 * @author xXx Players xXx
 * 
 */
public class Bewerter {

	/**
	 * Länge der Bewertungstupel. Nötig für die Generierung des schlechtesten und
	 * besten Wertes.
	 **/
	final int anzahlBewertungen = 10;
	final int[] besterWert = new int[anzahlBewertungen];
	final int[] schlechtesterWert = new int[anzahlBewertungen];

	public Bewerter() {
		for (int i = 0; i < this.besterWert.length; i++) {
			this.besterWert[i] = Integer.MAX_VALUE;
			this.schlechtesterWert[i] = Integer.MIN_VALUE;
		}
	}

	int[] bewerten(final Spielzustand zustand) {
		final Roboter wir = zustand.roboter[0];
		final Roboter gegner = zustand.roboter[1];

		final boolean wirGewinnen = wir.naechsteFlagge == zustand.flaggen.length;
		final boolean gegnerGewinntNicht = gegner.naechsteFlagge < zustand.flaggen.length;
		final boolean wirLebenNoch = wir.leben > 0;
		final boolean gegnerLebtNichtMehr = gegner.leben == 0;
		final int unsereNaechsteFlagge = wir.naechsteFlagge;
		final int gegnerNaechsteFlagge = -gegner.naechsteFlagge;
		final int unsereGesundheit = wir.gesundheit;
		final int gegnerGesundheit = -gegner.gesundheit;
		final int unserAbstandZurNaechstenFlagge = zustand.abstandZurNaechstenFlagge(0);
		final int gegnerAbstandZurNaechstenFlagge = zustand.abstandZurNaechstenFlagge(1);

		return new int[] { wirGewinnen ? 1 : 0, gegnerGewinntNicht ? 1 : 0, wirLebenNoch ? 1 : 0,
				gegnerLebtNichtMehr ? 1 : 0, unsereNaechsteFlagge, gegnerNaechsteFlagge, unsereGesundheit,
				gegnerGesundheit, unserAbstandZurNaechstenFlagge, gegnerAbstandZurNaechstenFlagge };
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

}