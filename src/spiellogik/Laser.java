package spiellogik;

public final class Laser extends Mauer {

	@Override
	void ausfuehren(final Feld feld, final int richtung, final Spielzustand zustand) {
		feld.durchlasern(feld, (richtung + 3) % 6, zustand, true);
	}

}