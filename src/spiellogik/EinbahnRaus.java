package spiellogik;

public final class EinbahnRaus extends Kante {

	@Override
	boolean eintreten(final Bewegbar b, final Spielzustand zustand) {
		return false;
	}

	@Override
	boolean reinlaserbar() {
		return false;
	}

}