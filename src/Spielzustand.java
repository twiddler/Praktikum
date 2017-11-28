
/**
 * Speichert den Zustand, in dem sich das Spiel befindet. Dazu zählen die
 * Verbindungen der Felder, Zustände der Roboter, usw..
 * 
 * @author xXx Players xXx
 *
 */
class Spielzustand implements Cloneable {

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
	 * Indizes der Felder, in Reihenfolge der Ausführung: Aixpresslaufbänder,
	 * Aixpresslaufbänder, Laufbänder, Drehscheiben, Reparaturfelder.
	 * (Aixpresslaufbänder sind bei uns normale Laufbänder.) Da sich die Positionen
	 * nicht ändern, ist dieses Feld Eigenschaft der Klasse, und muss nur einmal
	 * befüllt werden.
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

	Spielzustand(Roboter[] roboter, Feld[] felder, int zug) {
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
	 * Gibt den Spielzustand zurück, der beim Spielen einer Karte erreicht wird.
	 * Hier ist noch unklar, wie die Karte einem Roboter zugeordnet wird.
	 */
	Spielzustand karteSpielen() {
		Spielzustand result = this.clone();

		// TODO

		return result;
	}

	/**
	 * Gibt den Spielzustand zurück, der durchs Ausführen der Aktionsfelder und
	 * Feldzusätze erreicht wird.
	 */
	Spielzustand feldaktionenAusfuehren() {
		Spielzustand result = this.clone();

		// Sonderfelder
		for (int[] positionen : Spielzustand.positionenMitSonderfeld) {
			for (int position : positionen) {
				result.feldAufPosition(position).ausfuehren(result);
			}
		}

		// Zahltage
		for (int position : Spielzustand.positionenMitFeldzusatz[0]) {
			result.feldAufPosition(position).feldzusatzAusfuehren(result);
		}

		// Laser
		for (int position : Spielzustand.positionenMitLasern) {
			result.feldAufPosition(position).kantenAusfuehren(result);
		}

		// Pressen
		for (int position : Spielzustand.positionenMitFeldzusatz[1]) {
			result.feldAufPosition(position).feldzusatzAusfuehren(result);
		}

		return result;
	}

	/**
	 * Gibt den Spielzustand nach dem Feuern beider Roboterlaser zurück. Dessen
	 * Zugnr. soll eins höher sein.
	 */
	Spielzustand roboterlaserFeuern() {
		Spielzustand result = this.clone();
		for (int i = 0; i < result.roboter.length; ++i) {
			result.roboter[i].lasern(this);
		}
		++result.zug;
		return result;
	}

	/**
	 * Gibt das position-te Feld zurück.
	 */
	Feld feldAufPosition(int position) {
		return this.felder[position];
	}
}
