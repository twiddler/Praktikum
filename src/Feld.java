
/**
 * Ein Spielfeld. Hat Unterklassen Drehfeld, Laufband, Reparaturfeld, Zahltag,
 * Presse, Loch.
 * 
 * @author xXx Players xXx
 *
 */
class Feld implements Cloneable {

	/**
	 * Indizes der angrenzenden Felder, in Reihenfolge
	 */
	int[] nachbarn;

	/**
	 * Kanten des Feldes, in Reihenfolge
	 */
	Kante[] kanten;

	Zusatz zusatz;
	private int drehung;

	/**
	 * Index dieses Feldes
	 */
	int position;

	Feld(int[] nachbarn, Kante[] kanten, Zusatz zusatz, int drehung, int position) {
		this.nachbarn = nachbarn;
		this.kanten = kanten;
		this.zusatz = zusatz;
		this.drehung = drehung;
		this.position = position;
	}

	@Override
	public Feld clone() {
		Feld result = null;
		try {
			result = (Feld) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Dreht das Feld und Roboter darauf.
	 */
	void drehen(int drehung, Roboter[] roboter) {
		this.drehung = (drehung + this.drehung) % 6;
		for (Roboter r : roboter) {
			if (r.stehtAufPosition(this.position)) {
				r.drehen(drehung);
			}
		}
	}

	/**
	 * Führt die Fähigkeit am Zugende aus, bspw. sollen Zahltagfelder Geld
	 * auszahlen.
	 */
	void ausfuehren(Spielzustand zustand) {
	}

	/**
	 * Führt die Fähigkeit der Kanten aus. Also letztendlich nur die Laser.
	 */
	void kantenAusfuehren(Spielzustand zustand) {
		for (int i = 0; i < this.kanten.length; ++i) {
			this.kanten[i].ausfuehren(zustand.feldAufPosition(this.position), i, zustand);
		}
	}

	/**
	 * Führt den Feldzusatz (Zahltag oder Presse) aus.
	 */
	void feldzusatzAusfuehren(Spielzustand zustand) {
		this.zusatz.ausfuehren(this.position, zustand);
	}

	/**
	 * Gibt die Kante in gegebener Richtung als Objekt zurück.
	 */
	Kante kanteInRichtung(int richtung) {
		return this.kanten[(this.drehung + richtung) % 6];
	}

	/**
	 * Versucht die entsprechende Kante zu überschreiten. Falls erfolgreich, wird
	 * der Roboter auf das Feld hier gesetzt, und die Fähigkeit beim Betreten des
	 * Feldes ausgeführt. Bspw. soll ein Loch den betretenden Roboter umbringen.
	 */
	void betreten(Roboter roboter) {
		// Richtung aus der der Roboter kommt bestimmen
		int richtung = -1;
		for (int i = 0; i < this.nachbarn.length; ++i) {
			if (roboter.stehtAufPosition(this.nachbarn[i])) {
				richtung = i;
				break;
			}
		}

		if (richtung == -1) {
			System.err.println("Fehler beim Bestimmen der Richtung");
		}

		if (this.kanteInRichtung(richtung).eintreten(roboter)) {
			roboter.position = this.position;
		}
	}

	/**
	 * Versucht die Kante in dieser Richtung zu übertreten, und gibt den Erfolg
	 * zurück.
	 */
	boolean verlassen(Roboter roboter, int richtung) {
		return this.kanteInRichtung(richtung).austreten(roboter);
	}

	/**
	 * Lässt Roboter auf dem aktuellen Feld vom Laser getroffen werden bzw. schickt
	 * den Laser ins nächste Feld falls keine Kante in dieser Richtung ihn aufhält.
	 */
	void durchlasern(int richtung, Spielzustand zustand) {
		for (Roboter r : zustand.roboter) {
			if (r.stehtAufPosition(this.position)) {
				r.gesundheitVerringern();
				return;
			}
		}

		Feld nachbar = zustand.feldAufPosition(this.nachbarn[richtung]);
		if (this.kanteInRichtung(richtung).rauslaserbar()
				&& nachbar.kanteInRichtung((richtung + 3) % 6).reinlaserbar()) {
			nachbar.durchlasern(richtung, zustand);
		}
	}

}

class Drehfeld extends Feld {

	private int drehungUm;

	Drehfeld(int[] nachbarn, Kante[] kanten, Zusatz zusatz, int drehung, int position, int drehungUm) {
		super(nachbarn, kanten, zusatz, drehung, position);
		this.drehungUm = drehungUm;
	}

	@Override
	void ausfuehren(Spielzustand zustand) {
		this.drehen(this.drehungUm, zustand.roboter);
	}

	@Override
	public Drehfeld clone() {
		Drehfeld result = (Drehfeld) super.clone();
		result.drehungUm = this.drehungUm;
		return result;
	}

}

class Laufband extends Feld {

	private int richtung;

	Laufband(int[] nachbarn, Kante[] kanten, Zusatz zusatz, int drehung, int position, int richtung) {
		super(nachbarn, kanten, zusatz, drehung, position);
		this.richtung = richtung;
	}

	@Override
	void ausfuehren(Spielzustand zustand) {
		for (Roboter r : zustand.roboter) {
			if (r.stehtAufPosition(this.position)) {
				if (this.verlassen(r, richtung)) {
					zustand.feldAufPosition(this.nachbarn[richtung]).betreten(r);
				}
			}
		}
	}

	@Override
	public Laufband clone() {
		Laufband result = (Laufband) super.clone();
		result.richtung = this.richtung;
		return result;
	}

}

class Reparaturfeld extends Feld {

	private int gesundheit;

	Reparaturfeld(int[] nachbarn, Kante[] kanten, Zusatz zusatz, int drehung, int position, int gesundheit) {
		super(nachbarn, kanten, zusatz, drehung, position);
		this.gesundheit = gesundheit;
	}

	@Override
	void ausfuehren(Spielzustand zustand) {
		for (Roboter r : zustand.roboter) {
			if (r.stehtAufPosition(this.position)) {
				r.reparieren(this.gesundheit);
			}
		}
	}

	@Override
	public Reparaturfeld clone() {
		Reparaturfeld result = (Reparaturfeld) super.clone();
		result.gesundheit = this.gesundheit;
		return result;
	}

}

class Loch extends Feld {

	Loch(int[] nachbarn, Kante[] kanten, Zusatz zusatz, int drehung, int position) {
		super(nachbarn, kanten, zusatz, drehung, position);
	}

	@Override
	void betreten(Roboter roboter) {
		super.betreten(roboter);
		roboter.zerstoeren();
	}

}