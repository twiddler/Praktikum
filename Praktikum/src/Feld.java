class Feld {

	int[] nachbarn;
	Kante[] kanten;
	private int drehung;
	int position;

	public Feld() {

	}

	public Feld(Feld feld) {
		this.nachbarn = new int[feld.nachbarn.length];
		for (int i = 0; i < feld.nachbarn.length; i++) {
			this.nachbarn[i] = feld.nachbarn[i];
		}

		this.kanten = new Kante[feld.kanten.length];
		for (int i = 0; i < feld.kanten.length; i++) {
			this.kanten[i] = feld.kanten[i];
		}

		this.drehung = feld.drehung;
		this.position = feld.position;
	}

	void drehen(int drehung) {
		this.drehung = (drehung + this.drehung) % 6;
	}

	void ausfuehren(Spielzustand zustand) {}

	void kantenAusfuehren(Spielzustand zustand) {
		for (int i = 0; i < this.kanten.length; i++) {
			this.kanten[i].ausfuehren(zustand.feldAnPosition(this.position), (i + 3) % 6, zustand);
		}
	}

	Kante kanteInRichtung(int richtung) {
		return this.kanten[(this.drehung + richtung) % 6];
	}

	void betreten(Roboter roboter) {
		// Richtung aus der der Roboter kommt bestimmen
		int richtung = -1;
		for (int i = 0; i < this.nachbarn.length; ++i) {
			if (this.nachbarn[i] == roboter.position) {
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
			if (r.position == this.position) {
				r.gesundheitVerringern();
				return;
			}
		}

		Feld nachbar = zustand.feldAnPosition(this.nachbarn[richtung]);
		if (this.kanteInRichtung(richtung).rauslaserbar()
				&& nachbar.kanteInRichtung((richtung + 3) % 6).reinlaserbar()) {
			nachbar.durchlasern(richtung, zustand);
		}
	}

}

class Drehfeld extends Feld {

	private int schritte;

	public Drehfeld(Feld feld) {
		super(feld);
		this.schritte = ((Drehfeld) feld).schritte;
	}

	void ausfuehren(Spielzustand zustand) {
		this.drehen(this.schritte);
	}

}

class Laufband extends Feld {

	private int richtung;

	public Laufband(Feld feld) {
		super(feld);
		this.richtung = ((Laufband) feld).richtung;
	}

	void ausfuehren(Spielzustand zustand) {
		for (Roboter r : zustand.roboter) {
			if (this.verlassen(r, richtung)) {
				zustand.feldAnPosition(this.nachbarn[richtung]).betreten(r);
			}
		}
	}
	
}

class Reparaturfeld extends Feld {
	
	private int gesundheit;

	public Reparaturfeld(Feld feld) {
		super(feld);
		this.gesundheit = ((Reparaturfeld) feld).gesundheit;
	}
	
	void ausfuehren(Spielzustand zustand) {
		for (Roboter r : zustand.roboter) {
			r.reparieren(this.gesundheit);
		}
	}
	
}

class Zahltag extends Feld {
	
	private int geld;

	public Zahltag(Feld feld) {
		super(feld);
		this.geld = ((Zahltag) feld).geld;
	}
	
	void ausfuehren(Spielzustand zustand) {
		for (Roboter r : zustand.roboter) {
			r.erhalteGeld(this.geld);
		}
	}
	
}

class Presse extends Feld {

	private boolean[] aktivInZuegen;
	
	public Presse(Feld feld) {
		super(feld);
		this.aktivInZuegen = ((Presse) feld).aktivInZuegen;
	}
	
	void ausfuehren(Spielzustand zustand) {
		for (boolean aktiv : aktivInZuegen) {
			if (aktiv) {
				for (Roboter r : zustand.roboter) {
					if (r.position == this.position) {
						r.zerstoeren();
					}
				}
				return;
			}
		}
	}
	
}

class Loch extends Feld {
	
	void betreten(Roboter roboter) {
		super.betreten(roboter);
		roboter.zerstoeren();
	}
	
}