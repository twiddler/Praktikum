class Spielzustand implements Cloneable {

	Roboter[] roboter;
	private Feld[] felder;
	int zug;
	static int[][] positionenMitSonderfeld; // [Aixpresslaufbänder, Aixpresslaufbänder, Laufbänder, Drehscheiben,
											// Reparaturfelder]
	static int[][] positionenMitFeldzusatz; // [Zahltage, Pressen]
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

	Spielzustand karteSpielen() {
		Spielzustand result = this.clone();

		// TODO

		return result;
	}

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

	Spielzustand roboterlaserFeuern() {
		Spielzustand result = this.clone();
		for (int i = 0; i < result.roboter.length; ++i) {
			result.roboter[i].lasern(this);
		}
		++result.zug;
		return result;
	}

	Feld feldAufPosition(int position) {
		return this.felder[position];
	}
}
