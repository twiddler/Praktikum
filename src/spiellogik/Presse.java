package spiellogik;

public final class Presse extends Zusatz {

	private final boolean[] aktivInZuegen;

	public Presse(final boolean[] aktivInZuegen) {
		this.aktivInZuegen = aktivInZuegen;
	}

	@Override
	void ausfuehren(final int position, final Spielzustand zustand) {
		if (aktivInZuegen[zustand.wievielterZug()]) {
			for (final Roboter r : zustand.roboter) {
				if (r.stehtAufPosition(position)) {
					r.zerstoeren(zustand);
				}
			}
		}
	}

}