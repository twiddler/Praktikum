package entscheidungen;

import spiellogik.Roboter;
import spiellogik.Spielzustand;

public class BewerterEgozentrisch extends Bewerter {

	final int anzahlBewertungen = 6;

	@Override
	int[] bewerten(final Spielzustand zustand) {
		final Roboter wir = zustand.roboter[0];

		final boolean wirGewinnen = wir.naechsteFlagge == zustand.flaggen.length;
		final boolean wirLebenNoch = wir.leben > 0;
		final int unsereNaechsteFlagge = wir.naechsteFlagge;
		final int unserLeben = wir.leben;
		final int unsereGesundheit = wir.gesundheit;
		final int unserAbstandZurNaechstenFlagge = -zustand.abstandZurNaechstenFlagge(0);

		return new int[] { wirGewinnen ? 1 : 0, wirLebenNoch ? 1 : 0, unsereNaechsteFlagge, unserLeben,
				unsereGesundheit, unserAbstandZurNaechstenFlagge };
	}

}
