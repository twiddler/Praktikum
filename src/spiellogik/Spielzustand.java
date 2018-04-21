package spiellogik;

import java.util.Collections;

/**
 * Speichert den Zustand, in dem sich das Spiel befindet. Dazu zählen die
 * Verbindungen der Felder, Zustände der Roboter, usw..
 * 
 * @author xXx Players xXx
 *
 */
public class Spielzustand implements Cloneable {

	/**
	 * Die zwei Roboter. roboter[0] sind wir, roboter[1] der Gegner.
	 */
	public Roboter[] roboter;

	/**
	 * Alle Spielfelder.
	 */
	private Feld[] felder;

	/**
	 * Flaggen in aufsteigender Reihenfolge
	 */
	Flagge[] flaggen;

	/**
	 * Wieviele Karten schon gespielt wurden. Wird benutzt um zu bestimmen der
	 * wievielte Zug in einer Runde es ist, und ob nach einer Karte die gespielt
	 * wird ein Zug (aus 2 gespielten Karten) abgeschlossen ist.
	 */
	int gespielteKarten = 0;

	/**
	 * Indizes der Felder, in Reihenfolge der Ausführung: Aixpresslaufbänder,
	 * Aixpresslaufbänder, Laufbänder, Drehscheiben, Reparaturfelder.
	 * (Aixpresslaufbänder sind bei uns normale Laufbänder.) Da sich die Positionen
	 * nicht ändern, ist dieses Feld Eigenschaft der Klasse, und muss nur einmal
	 * befüllt werden.
	 */
	public static int[][] positionenMitSonderfeld;

	/**
	 * Analog zu positionenMitSonderfeld (Zahltage, Pressen)
	 */
	public static int[][] positionenMitFeldzusatz;

	/**
	 * Analog zu positionenMitSonderfeld
	 */
	public static int[] positionenMitLasern;

	public Spielzustand(final Roboter[] roboter, final Feld[] felder, Flagge[] flaggen) {
		this.roboter = roboter;
		this.felder = felder;
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

		result.roboter = new Roboter[this.roboter.length];
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
	 * Gibt den Spielzustand zurück, der beim Spielen einer Karte erreicht wird.
	 */
	public Spielzustand karteSpielen(final int besitzer, final Karte karte) {
		final Spielzustand result = this.clone();

		final Roboter roboter = result.roboter[besitzer];
		roboter.drehen(karte.drehung_roboter);
		roboter.laufen(karte.schritte, result);
		if (!roboter.zerstoert) {
			result.feldAufPosition(roboter.position).drehen(karte.drehung_feld, result.roboter);
		}
		roboter.karten.remove(karte);

		++result.gespielteKarten;
		if (result.gespielteKarten % 2 == 0) {
			result.zugBeenden();
		}

		return result;
	}

	/**
	 * Gibt den Spielzustand zurück, der durchs Ausführen der Aktionsfelder und
	 * Feldzusätze erreicht wird.
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

	void respawns() {
		for (final Roboter r : this.roboter) {
			if (r.zerstoert) {
				r.respawn(this);
			}
		}

		if (!this.roboter[0].stehtAufPosition(this.roboter[1].position)) {
			for (final Roboter r : this.roboter) {
				r.virtuell = false;
			}
		}
	}

	/**
	 * Führt alle Aktionen aus, die am Ende eines Zuges gemacht werden.
	 * 
	 * Achtung: Klont den Spielzustand nicht, sondern arbeitet auf sich selbst!
	 */
	void zugBeenden() {
		this.feldaktionenAusfuehren();
		this.roboterlaserFeuern();
		this.flaggenBeruehren();
		this.respawns();
	}

	/**
	 * Gibt das position-te Feld zurück.
	 */
	Feld feldAufPosition(final int position) {
		return this.felder[position];
	}

	public int wievielterZug() {
		return this.gespielteKarten / 2;
	}
	
	boolean istLetzterZug() {
		return this.wievielterZug() == Parameter.ZUEGE_PRO_RUNDE - 1;
	}

	/**
	 * Berechnet den Abstand vom übergebenen Roboter zu seiner nächsten Flagge.
	 * Ignoriert Hindernisse.
	 */
	public int abstandZurNaechstenFlagge(final int besitzer) {
		final Roboter roboter = this.roboter[besitzer];
		final Flagge flagge = this.flaggen[roboter.naechsteFlagge];

		if (roboter.position == -1)
			return Integer.MAX_VALUE;

		Feld feld = this.feldAufPosition(roboter.position);

		for (int ring = 1; ring < Parameter.ANZAHL_SPIELFELDRINGE; ++ring) {
			// Auf den nächsten Ring gehen
			feld = this.feldAufPosition(feld.nachbarn[0]);

			// Erstmal nach rechts gehen
			int richtung = 2;
			for (int drehen = 0; drehen < 6; ++drehen) {
				for (int laufen = 0; laufen < ring; ++laufen) {
					if (flagge.stehtAufPosition(feld.position)) {
						return ring;
					}
					feld = this.feldAufPosition(feld.nachbarn[richtung]);
				}
				++richtung;
				richtung %= 6;
			}
		}

		return Integer.MAX_VALUE;
	}

	public void handkartenSortieren() {
		for (final Roboter roboter : this.roboter) {
			Collections.sort(roboter.karten);
		}
	}
}
