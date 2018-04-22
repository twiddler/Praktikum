package spiellogik;

/**
 * Ein Spielfeld. Hat Unterklassen Drehfeld, Laufband, Reparaturfeld, Zahltag,
 * Presse, Loch.
 * 
 * @author xXx Players xXx
 *
 */
public class Feld implements Cloneable {

	/**
	 * Indizes der angrenzenden Felder, in Reihenfolge
	 */
	final int[] nachbarn;

	/**
	 * Kanten des Feldes, in Reihenfolge
	 */
	final Kante[] kanten;

	final Zusatz zusatz;
	private int drehung = 0;

	/**
	 * Index dieses Feldes
	 */
	final int position;

	public Feld(final int[] nachbarn, final Kante[] kanten, final Zusatz zusatz, final int position) {
		this.nachbarn = nachbarn;
		this.kanten = kanten;
		this.zusatz = zusatz;
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
	final void drehen(final int drehung, final Roboter[] roboter) {
		this.drehung = (drehung + this.drehung) % 6;
		for (final Roboter r : roboter) {
			if (r.stehtAufPosition(this.position)) {
				r.drehen(drehung);
			}
		}
	}

	/**
	 * Führt die Fähigkeit am Zugende aus, bspw. sollen Zahltagfelder Geld
	 * auszahlen.
	 */
	void ausfuehren(final Spielzustand zustand) {
	}

	/**
	 * Führt die Fähigkeit der Kanten aus. Also letztendlich nur die Laser.
	 */
	final void kantenAusfuehren(final Spielzustand zustand) {
		for (int i = 0; i < this.kanten.length; ++i) {
			this.kanten[i].ausfuehren(zustand.feldAufPosition(this.position), (i + this.drehung) % 6, zustand);
		}
	}

	/**
	 * Führt den Feldzusatz (Zahltag oder Presse) aus.
	 */
	final void feldzusatzAusfuehren(final Spielzustand zustand) {
		this.zusatz.ausfuehren(this.position, zustand);
	}

	/**
	 * Gibt die Kante in gegebener Richtung als Objekt zurück.
	 */
	final Kante kanteInRichtung(final int richtung) {
		return this.kanten[((this.drehung - richtung) % 6 + 6) % 6];
	}

	/**
	 * Versucht die entsprechende Kante zu überschreiten. Falls erfolgreich,
	 * wird der Roboter auf das Feld hier gesetzt, und die Fähigkeit beim
	 * Betreten des Feldes ausgeführt. Bspw. soll ein Loch den betretenden
	 * Roboter umbringen.
	 * 
	 * Steht auf diesem Feld ein Roboter, wird versucht ihn vom Feld zu
	 * schieben. Wenn das nicht geht, bleibt der eintretende Roboter auf seinem
	 * alten Feld.
	 */
	void betreten(final Bewegbar b, final Spielzustand zustand) {

		// Richtung aus der der Roboter kommt bestimmen
		int richtung = -1;
		for (int i = 0; i < this.nachbarn.length; ++i) {
			if (b.stehtAufPosition(this.nachbarn[i])) {
				richtung = i;
				break;
			}
		}
		assert richtung != -1 : "Fehler beim Bestimmen der Richtung";

		if (this.kanteInRichtung(richtung).eintreten(b, zustand)) {
			boolean betritt = true;

			// Nicht virtuelle Roboter können schieben
			if (b instanceof Roboter && !((Roboter) b).virtuell) {
				for (final Roboter roboter : zustand.roboter) {

					// Nicht virtuelle Roboter können geschoben werden
					if (roboter.stehtAufPosition(this.position) && !roboter.virtuell) {
						betritt = false;
						final int gegenrichtung = (richtung + 3) % 6;
						final Feld nachbar = zustand.feldAufPosition(this.nachbarn[gegenrichtung]);

						// Über eine Schlucht können wir immer schieben, danach
						// müssen es beide Kanten
						// zulassen
						if (this.kanteInRichtung(gegenrichtung) instanceof Schlucht
								|| (this.kanteInRichtung(gegenrichtung).rauslaserbar()
										&& nachbar.kanteInRichtung(richtung).reinlaserbar())) {
							if (this.verlassen(roboter, gegenrichtung, zustand)) {
								nachbar.betreten(roboter, zustand);
							}
							betritt = true;
						}
					}
				}
			}
			if (betritt) {
				b.position = this.position;
			}
		}
	}

	/**
	 * Versucht die Kante in dieser Richtung zu übertreten, und gibt den Erfolg
	 * zurück.
	 */
	final boolean verlassen(final Bewegbar b, final int richtung, final Spielzustand zustand) {
		return this.kanteInRichtung(richtung).austreten(b, zustand);
	}

	/**
	 * Lässt Roboter auf dem aktuellen Feld vom Laser getroffen werden bzw.
	 * schickt den Laser ins nächste Feld falls keine Kante in dieser Richtung
	 * ihn aufhält.
	 */
	final void durchlasern(Feld quelle, final int richtung, final Spielzustand zustand,
			final boolean trifftVirtuelleRoboter) {

		// Wenn ein oder mehrere Roboter getroffen werden, nehmen diese Schaden
		// und der
		// Laser wird gestoppt
		boolean roboterGetroffen = false;
		for (final Roboter r : zustand.roboter) {
			if (r.stehtAufPosition(this.position) && (!r.virtuell || trifftVirtuelleRoboter)) {
				r.schaedigen();
				roboterGetroffen = true;
			}
		}
		if (roboterGetroffen) {
			return;
		}

		final Feld nachbar = zustand.feldAufPosition(this.nachbarn[richtung]);
		if (this.kanteInRichtung(richtung).rauslaserbar() && nachbar.kanteInRichtung((richtung + 3) % 6).reinlaserbar()
				&& nachbar != quelle) {
			nachbar.durchlasern(quelle, richtung, zustand, trifftVirtuelleRoboter);
		}

	}

}