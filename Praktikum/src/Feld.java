class Feld implements Cloneable {

	int[] nachbarn;
	Kante[] kanten;
	Zusatz zusatz;
	private int drehung;
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

	void drehen(int drehung, Roboter[] roboter) {
		this.drehung = (drehung + this.drehung) % 6;
		for (Roboter r : roboter) {
			if (r.stehtAufPosition(this.position)) {
				r.drehen(drehung);
			}
		}
	}

	void ausfuehren(Spielzustand zustand) {
	}

	void kantenAusfuehren(Spielzustand zustand) {
		for (int i = 0; i < this.kanten.length; ++i) {
			this.kanten[i].ausfuehren(zustand.feldAufPosition(this.position), i, zustand);
		}
	}

	void feldzusatzAusfuehren(Spielzustand zustand) {
		this.zusatz.ausfuehren(this.position, zustand);
	}

	Kante kanteInRichtung(int richtung) {
		return this.kanten[(this.drehung + richtung) % 6];
	}

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

	boolean verlassen(Roboter roboter, int richtung) {
		return this.kanteInRichtung(richtung).austreten(roboter);
	}

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