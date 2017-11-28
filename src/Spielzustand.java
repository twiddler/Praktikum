
/**
 * Speichert den Zustand, in dem sich das Spiel befindet. Dazu z�hlen die
 * Verbindungen der Felder, Zust�nde der Roboter, usw..
 * 
 * @author xXx Players xXx
 *
 */
final class Spielzustand implements Cloneable {

	/**
	 * Die zwei Roboter. roboter[0] sind wir, roboter[1] der Gegner.
	 */
	Roboter[] roboter;

	/**
	 * Alle Spielfelder.
	 */
	private Feld[] felder;

	/**
	 * Der wievielte Zug einer Runde es gerade ist.
	 */
	int zug;

	/**
	 * Indizes der Felder, in Reihenfolge der Ausf�hrung: Aixpresslaufb�nder,
	 * Aixpresslaufb�nder, Laufb�nder, Drehscheiben, Reparaturfelder.
	 * (Aixpresslaufb�nder sind bei uns normale Laufb�nder.) Da sich die Positionen
	 * nicht �ndern, ist dieses Feld Eigenschaft der Klasse, und muss nur einmal
	 * bef�llt werden.
	 */
	static int[][] positionenMitSonderfeld;

	/**
	 * Analog zu positionenMitSonderfeld (Zahltage, Pressen)
	 */
	static int[][] positionenMitFeldzusatz;

	/**
	 * Analog zu positionenMitSonderfeld
	 */
	static int[] positionenMitLasern;

	Spielzustand(final Roboter[] roboter, final Feld[] felder, final int zug) {
		this.roboter = roboter;
		this.felder = felder;
		this.zug = zug;
	}

	@Override
	public Spielzustand clone() {
		Spielzustand result = null;
		try {
			result = (Spielzustand) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		result.roboter = new Roboter[2];
		for (int i = 0; i < roboter.length; ++i) {
			result.roboter[i] = this.roboter[i].clone();
		}

		result.felder = new Feld[this.felder.length];
		for (int i = 0; i < this.felder.length; i++) {
			result.felder[i] = this.felder[i].clone();
		}

		return result;
	}

	/**
	 * Gibt den Spielzustand zur�ck, der beim Spielen einer Karte erreicht wird.
	 * Hier ist noch unklar, wie die Karte einem Roboter zugeordnet wird.
	 */
	Spielzustand karteSpielen() {
		final Spielzustand result = this.clone();

		// TODO

		return result;
	}

	/**
	 * Gibt den Spielzustand zur�ck, der durchs Ausf�hren der Aktionsfelder und
	 * Feldzus�tze erreicht wird.
	 */
	Spielzustand feldaktionenAusfuehren() {
		final Spielzustand result = this.clone();

		// Sonderfelder
		for (final int[] positionen : Spielzustand.positionenMitSonderfeld) {
			for (final int position : positionen) {
				result.feldAufPosition(position).ausfuehren(result);
			}
		}

		// Zahltage
		for (final int position : Spielzustand.positionenMitFeldzusatz[0]) {
			result.feldAufPosition(position).feldzusatzAusfuehren(result);
		}

		// Laser
		for (final int position : Spielzustand.positionenMitLasern) {
			result.feldAufPosition(position).kantenAusfuehren(result);
		}

		// Pressen
		for (final int position : Spielzustand.positionenMitFeldzusatz[1]) {
			result.feldAufPosition(position).feldzusatzAusfuehren(result);
		}

		return result;
	}

	/**
	 * Gibt den Spielzustand nach dem Feuern beider Roboterlaser zur�ck. Dessen
	 * Zugnr. soll eins h�her sein.
	 */
	Spielzustand roboterlaserFeuern() {
		final Spielzustand result = this.clone();
		for (int i = 0; i < result.roboter.length; ++i) {
			result.roboter[i].lasern(this);
		}
		++result.zug;
		return result;
	}

	/**
	 * Gibt das position-te Feld zur�ck.
	 */
	Feld feldAufPosition(final int position) {
		return this.felder[position];
	}
}
