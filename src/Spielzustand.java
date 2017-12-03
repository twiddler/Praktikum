
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
	 * Flaggen in aufsteigender Reihenfolge
	 */
	Flagge[] flaggen;

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

	Spielzustand(final Roboter[] roboter, final Feld[] felder, final int zug, Flagge[] flaggen) {
		this.roboter = roboter;
		this.felder = felder;
		this.zug = zug;
		this.flaggen = flaggen;
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

		result.flaggen = new Flagge[this.flaggen.length];
		for (int i = 0; i < this.flaggen.length; i++) {
			result.flaggen[i] = this.flaggen[i].clone();
		}

		return result;
	}

	/**
	 * Gibt den Spielzustand zur�ck, der beim Spielen einer Karte erreicht wird.
	 * Hier ist noch unklar, wie die Karte einem Roboter zugeordnet wird.
	 */
	Spielzustand karteSpielen(final Roboter roboter, final Karte karte) {
		final Spielzustand result = this.clone();

		roboter.drehen(karte.drehung_roboter);
		roboter.laufen(karte.schritte, result);
		result.feldAufPosition(roboter.position).drehen(karte.drehung_feld, result.roboter);

		return result;
	}

	/**
	 * Gibt den Spielzustand zur�ck, der durchs Ausf�hren der Aktionsfelder und
	 * Feldzus�tze erreicht wird.
	 */
	void feldaktionenAusfuehren() {
		// Sonderfelder
		for (final int[] positionen : Spielzustand.positionenMitSonderfeld) {
			for (final int position : positionen) {
				this.feldAufPosition(position).ausfuehren(this);
			}
		}

		// Zahltage
		for (final int position : Spielzustand.positionenMitFeldzusatz[0]) {
			this.feldAufPosition(position).feldzusatzAusfuehren(this);
		}

		// Laser
		for (final int position : Spielzustand.positionenMitLasern) {
			this.feldAufPosition(position).kantenAusfuehren(this);
		}

		// Pressen
		for (final int position : Spielzustand.positionenMitFeldzusatz[1]) {
			this.feldAufPosition(position).feldzusatzAusfuehren(this);
		}
	}

	void roboterlaserFeuern() {
		for (final Roboter r : this.roboter) {
			r.lasern(this);
		}
	}

	void flaggenBeruehren() {
		for (final Flagge flagge : this.flaggen) {
			for (final Roboter r : this.roboter) {
				flagge.beruehren(r, this);
			}
		}
	}

	Spielzustand zugBeenden() {
		Spielzustand result = this.clone();

		result.feldaktionenAusfuehren();
		result.roboterlaserFeuern();
		result.flaggenBeruehren();
		++result.zug;

		return result;
	}

	/**
	 * Gibt das position-te Feld zur�ck.
	 */
	Feld feldAufPosition(final int position) {
		return this.felder[position];
	}

	boolean istLetzterZug() {
		return this.zug == Parameter.ZUEGE_PRO_RUNDE - 1;
	}
}
