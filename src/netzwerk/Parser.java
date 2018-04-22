package netzwerk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import spiellogik.Drehfeld;
import spiellogik.EinbahnRaus;
import spiellogik.EinbahnRein;
import spiellogik.Feld;
import spiellogik.Flagge;
import spiellogik.Kante;
import spiellogik.Karte;
import spiellogik.Laser;
import spiellogik.Laufband;
import spiellogik.Loch;
import spiellogik.Mauer;
import spiellogik.Parameter;
import spiellogik.Presse;
import spiellogik.Reparaturfeld;
import spiellogik.Roboter;
import spiellogik.Schlucht;
import spiellogik.Spielzustand;
import spiellogik.Zahltag;
import spiellogik.Zusatz;

/**
 * Erzeugt aus vom Server empfangenen JSONObjects die Spiellogikobjekte.
 * 
 * @author xXx Players xXx
 * 
 */
public class Parser {

	static final int spielerID = 0;

	/**
	 * Das Kartendeck als Kartenobjekte. Wird einmal abgespeichert und danach zum
	 * Nachschlagen für gesperrte Slots benutzt.
	 */
	static List<Karte> deck;

	static List<Karte> kartenParsen(final JSONArray karten) {
		final List<Karte> result = new ArrayList<>();

		for (int i = 0; i < karten.length(); ++i) {
			final JSONObject karte = karten.getJSONObject(i);
			final int prioritaet = karte.getInt("prioritaet");
			final int drehung_roboter = karte.getInt("rotation");
			final int schritte = karte.getInt("schritte");
			final int drehung_feld = karte.getInt("felddrehung");
			result.add(new Karte(prioritaet, drehung_roboter, schritte, drehung_feld));
		}

		return result;

	}

	/**
	 * Parst die Daten zu den Robotern. Diese überladene Funktion ist für die erste
	 * Runde, in der es noch keine gesperrten Karten und nur das Startgeld gibt.
	 */
	static Roboter[] roboterParsen(final int startgeld, final JSONArray spielerJSON, final JSONArray roboterJSON,
			final int unsereID) {
		final Roboter[] result = new Roboter[2];

		for (int i = 0; i < roboterJSON.length(); ++i) {
			// Informationen aus dem Roboterarray
			final JSONObject roboterObject = roboterJSON.getJSONObject(i);
			final int roboterID = roboterObject.getInt("spielerID") == unsereID ? 0 : 1;
			final JSONObject positionRichtung = roboterObject.getJSONObject("position");
			final int position = positionRichtung.getInt("feldindex");
			final int blickrichtung = positionRichtung.getInt("richtung");
			final int gesundheit = roboterObject.getInt("gesundheit");
			Parameter.MAX_GESUNDHEIT = gesundheit;
			final boolean virtuell = roboterObject.getBoolean("virtuell");

			// Informationen aus dem Spielerarray
			final JSONObject spielerObject = spielerJSON.getJSONObject(i);
			final int leben = spielerObject.getInt("leben");
			final int naechsteFlagge = spielerObject.getInt("nextFlag") - 1;
			final List<Karte> karten = kartenParsen(spielerObject.getJSONArray("karten"));

			result[roboterID] = new Roboter(position, blickrichtung, leben, gesundheit, startgeld, naechsteFlagge,
					virtuell, karten, new ArrayList<>());
		}

		// Falls ein Roboter ausgeschieden ist, den anderen mit einem
		// Platzhalter füllen
		if (roboterJSON.length() == 1) {
			for (int i = 0; i < result.length; ++i) {
				if (result[i] == null) {
					result[i] = new Roboter(0, 0, 0, 0, 0, 0, true, new ArrayList<>(), new ArrayList<>());
				}
			}
		}

		return result;
	}

	/**
	 * Parst die Daten zu den Robotern. Diese überladene Funktion ist für alle
	 * Runden bis auf die erste, weil hier gesperrte Karten und Kontostände
	 * berechnet werden müssen.
	 */
	static Roboter[] roboterParsen(final JSONArray spielerJSON, final JSONArray roboterJSON, final int unsereID,
			final JSONArray programmeJSON, final Spielzustand zustand) {
		final Roboter[] result = roboterParsen(0, spielerJSON, roboterJSON, unsereID);

		// Kontostand übernehmen
		for (int i = 0; i < zustand.roboter.length; ++i) {
			result[i].geld = zustand.roboter[i].geld;
		}

		for (int i = 0; i < roboterJSON.length(); ++i) {
			// Wessen gesperrte Karten wollen wir berechnen?
			final JSONObject programmVonSpieler = programmeJSON.getJSONObject(i);
			final int roboterID = programmVonSpieler.getInt("spielerID") == unsereID ? 0 : 1;

			final int freieSlots = Math.max(0, Math.min(Parameter.ZUEGE_PRO_RUNDE, result[roboterID].gesundheit - 1));
			final ArrayList<Karte> gesperrteKarten = new ArrayList<>();
			final JSONArray programm = programmVonSpieler.getJSONArray("programm");
			for (int slot = freieSlots; slot < Parameter.ZUEGE_PRO_RUNDE; ++slot) {
				gesperrteKarten.add(karteMitPrioritaet(programm.getInt(slot)));
			}
			result[roboterID].gesperrteKarten = gesperrteKarten;
		}

		return result;
	}

	static Feld[] felderParsen(final JSONArray felder) {

		final Feld[] result = new Feld[felder.length()];
		final List<List<Integer>> posSonderfeld = new ArrayList<>();
		for (int i = 0; i < 5; ++i) {
			posSonderfeld.add(new ArrayList<>());
		}
		final List<List<Integer>> posFeldzusatz = new ArrayList<>();
		for (int i = 0; i < 2; ++i) {
			posFeldzusatz.add(new ArrayList<>());
		}
		final List<Integer> posLaser = new ArrayList<Integer>();
		final int[][] nachbarListe = nachbarListe();

		// Wir brauchen nur ein Objekt pro Kantentyp
		final Kante mauer = new Mauer();
		final Kante schlucht = new Schlucht();
		final Kante einbahn_rein = new EinbahnRein();
		final Kante einbahn_raus = new EinbahnRaus();
		final Kante laser = new Laser();
		final Kante normal = new Kante();

		for (int i = 0; i < felder.length(); i++) {
			final JSONObject pFeld = felder.getJSONObject(i);
			final JSONObject pTyp = pFeld.getJSONObject("typ");
			final JSONObject zusaetze = pFeld.getJSONObject("zusaetze");
			final JSONObject zusaetzeMitte = zusaetze.getJSONObject("mitte");
			final JSONArray pKanten = zusaetze.getJSONArray("kanten");

			final Kante[] kanten = new Kante[6];
			for (int j = 0; j < pKanten.length(); j++) {
				switch (pKanten.getString(j)) {
				case "mauer":
					kanten[j] = mauer;
					break;
				case "schlucht":
					kanten[j] = schlucht;
					break;
				case "einbahnInnen":
					kanten[j] = einbahn_rein;
					break;
				case "einbahnAussen":
					kanten[j] = einbahn_raus;
					break;
				case "lazor":
					kanten[j] = laser;
					posLaser.add(j);
					break;
				default:
					kanten[j] = normal;
					break;
				}
			}

			Zusatz zusatz = null;
			switch (zusaetzeMitte.getString("zusatztyp")) {
			case "zahltag":
				zusatz = new Zahltag();
				posFeldzusatz.get(0).add(i);
				break;
			case "presse":
				final JSONArray aktivIn = zusaetzeMitte.getJSONArray("aktiv");
				final boolean[] aktiv = new boolean[5];
				for (int j = 0; j < aktiv.length; j++) {
					if (aktivIn.toString().contains(Integer.toString(j))) {
						aktiv[j] = true;
					} else {
						aktiv[j] = false;
					}
				}
				zusatz = new Presse(aktiv);
				posFeldzusatz.get(1).add(i);
				break;
			}

			switch (pTyp.getString("feldtyp")) {
			case "dreh":
				result[i] = new Drehfeld(nachbarListe[i], kanten, zusatz, i, pTyp.getInt("richtung"));
				posSonderfeld.get(3).add(i);
				break;
			case "loch":
				result[i] = new Loch(nachbarListe[i], kanten, zusatz, i);
				break;
			case "laufband":
				result[i] = new Laufband(nachbarListe[i], kanten, zusatz, i, pTyp.getInt("richtung"));
				posSonderfeld.get(2).add(i);
				break;
			case "aixpress":
				result[i] = new Laufband(nachbarListe[i], kanten, zusatz, i, pTyp.getInt("richtung"));
				posSonderfeld.get(0).add(i);
				posSonderfeld.get(1).add(i);
				break;
			case "reparatur":
				result[i] = new Reparaturfeld(nachbarListe[i], kanten, zusatz, i);
				posSonderfeld.get(4).add(i);
				break;
			default:
				result[i] = new Feld(nachbarListe[i], kanten, zusatz, i);
				break;
			}
		}

		final int[][] retSonderfeld = befuelleArray2(posSonderfeld);
		final int[][] retFeldzusatz = befuelleArray2(posFeldzusatz);
		final int[] retLaser = befuelleArray(posLaser);

		Spielzustand.positionenMitSonderfeld = retSonderfeld;
		Spielzustand.positionenMitFeldzusatz = retFeldzusatz;
		Spielzustand.positionenMitLasern = retLaser;

		return result;

	}

	/**
	 * Gibt ein Array mit allen Flaggen zurück.
	 */
	static Flagge[] flaggenParsen(JSONArray felder) {

		final SortedMap<Integer, Flagge> flaggen = new TreeMap<>();
		for (int i = 0; i < felder.length(); ++i) {
			final int flaggenNr = felder.getJSONObject(i).getJSONObject("zusaetze").getInt("flagge");
			if (flaggenNr != 0) {
				flaggen.put(flaggenNr, new Flagge(i, flaggenNr - 1));
			}
		}

		final Flagge[] result = new Flagge[flaggen.size()];
		for (Map.Entry<Integer, Flagge> entry : flaggen.entrySet()) {
			result[entry.getKey() - 1] = entry.getValue();
		}

		return result;

	}

	/**
	 * Sucht aus dem Kartendeck die Karte mit dieser Priorität.
	 */
	static Karte karteMitPrioritaet(int prioritaet) {
		for (final Karte karte : deck) {
			if (karte.prioritaet == prioritaet) {
				return karte;
			}
		}

		assert false : "Karte mit Prioritaet " + prioritaet + " nicht im Deck gefunden.";
		return null;
	}

	public static Spielzustand ersteRunde(JSONObject json, int unsereID) {

		final Roboter[] roboter = roboterParsen(json.getInt("startgeld"), json.getJSONArray("spieler"),
				json.getJSONArray("roboter"), unsereID);
		Parameter.ANZAHL_SPIELFELDRINGE = (int) (1d / 2
				+ Math.sqrt(1d / 4 + (json.getJSONArray("spielbrett").length() - 1) / 3));
		final Feld[] felder = felderParsen(json.getJSONArray("spielbrett"));
		final Flagge[] flaggen = flaggenParsen(json.getJSONArray("spielbrett"));

		// Fürs spätere Nachschlagen das Deck abspeichern
		deck = kartenParsen(json.getJSONArray("kartendeck"));

		// Handkarten ins Deck
		for (final Roboter r : roboter) {
			deck.addAll(r.karten);
		}

		return new Spielzustand(roboter, felder, flaggen);

	}

	public static Spielzustand nteRunde(final JSONObject json, final int unsereID, final Spielzustand zustand) {

		final Roboter[] roboter = roboterParsen(json.getJSONArray("spieler"), json.getJSONArray("roboter"), unsereID,
				json.getJSONArray("programme"), zustand);
		final Feld[] felder = felderParsen(json.getJSONArray("spielbrett"));
		final Flagge[] flaggen = flaggenParsen(json.getJSONArray("spielbrett"));

		return new Spielzustand(roboter, felder, flaggen);

	}

	static void setzeNachbarn(final int index1, final int index2, final int rotation, final int[][] felderNachbarn) {
		felderNachbarn[index1][rotation] = index2;
		felderNachbarn[index2][(rotation + 3) % 6] = index1;
	}

	static int[][] nachbarListe() {
		int felder = 3 * Parameter.ANZAHL_SPIELFELDRINGE * (Parameter.ANZAHL_SPIELFELDRINGE - 1) + 1;
		final int[][] ret = new int[felder][6];
		for (int i = 0; i < 6; i++) {
			setzeNachbarn(0, i + 1, i, ret);
		}
		for (int i = 0; i < Parameter.ANZAHL_SPIELFELDRINGE - 1; i++) {
			int pS = 0;
			int pT = 6 * (i + 2) - 1;
			int sInd = getIndexNr(i, pS);
			int tInd = getIndexNr(i + 1, pT);
			for (int j = 0; j < 6; j++) {
				for (int m = 0; m < 3; m++) {
					setzeNachbarn(sInd, tInd, (j + 5 + m) % 6, ret);
					pT++;
					tInd = getIndexNr(i + 1, pT);
				}
				setzeNachbarn(sInd, getIndexNr(i, pS + 1), (j + 2) % 6, ret);
				for (int k = 0; k < i; k++) {
					pS++;
					sInd = getIndexNr(i, pS);
					setzeNachbarn(sInd, getIndexNr(i, pS + 1), (j + 2) % 6, ret);
					setzeNachbarn(sInd, getIndexNr(i + 1, pT - 1), j, ret);
					setzeNachbarn(sInd, tInd, (j + 1) % 6, ret);
					pT++;
					tInd = getIndexNr(i + 1, pT);
				}
				pS++;
				sInd = getIndexNr(i, pS);
				pT--;
				tInd = getIndexNr(i + 1, pT);
			}
		}
		return ret;
	}

	static int getIndexNr(final int ring, final int stelle) {
		final int s = stelle % ((ring + 1) * 6);
		if (ring < Parameter.ANZAHL_SPIELFELDRINGE - 1) {
			return 3 * ring * (ring + 1) + s + 1;
		} else {
			return letzterRing(s);
		}
	}

	static int letzterRing(final int s) {
		int felder = Parameter.ANZAHL_SPIELFELDRINGE * 6;
		final int ret[] = new int[felder];
		int p = 1;
		int t = (Parameter.ANZAHL_SPIELFELDRINGE - 1) * 4;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < Parameter.ANZAHL_SPIELFELDRINGE; j++) {
				ret[p] = getIndexNr(Parameter.ANZAHL_SPIELFELDRINGE - 2, t);
				p++;
				p %= felder;
				t--;
				if (t < 0) {
					t = (Parameter.ANZAHL_SPIELFELDRINGE - 1) * 6 - 1;
				}
			}
			t += ((2 * (Parameter.ANZAHL_SPIELFELDRINGE - 1)) + 1);
			t %= (6 * (Parameter.ANZAHL_SPIELFELDRINGE - 1));
		}
		return ret[s];
	}

	static int[] befuelleArray(final List<Integer> input) {
		final int ret[] = new int[input.size()];
		for (int i = 0; i < input.size(); i++) {
			ret[i] = input.get(i);
		}
		return ret;
	}

	static int[][] befuelleArray2(final List<List<Integer>> input) {
		final int ret[][] = new int[input.size()][];
		for (int i = 0; i < input.size(); i++) {
			ret[i] = new int[input.get(i).size()];
			for (int j = 0; j < input.get(i).size(); j++) {
				ret[i][j] = input.get(i).get(j);
			}
		}
		return ret;
	}

	public static List<Karte> bietoptionen(final JSONArray bietoptionen) {
		return kartenParsen(bietoptionen);
	}

	/**
	 * Gibt den Gewinnern der Auktionen die Karten und zieht die Gebote vom
	 * Kontostand ab
	 */
	public static Spielzustand auktionsergebnisAuswerten(final JSONArray auktionsergebnisse, final Spielzustand zustand,
			final int unsereID) {

		for (int i = 0; i < auktionsergebnisse.length(); ++i) {
			// Für jede Karte den Höchstbietenden ermitteln
			final JSONObject auktionsergebnis = auktionsergebnisse.getJSONObject(i);
			final JSONArray gebote = auktionsergebnis.getJSONArray("gebote");
			Integer hoechstbietender = null;
			int hoechstesGebot = 0; // 0 ^= kein Gebot
			for (int j = 0; j < gebote.length(); ++j) {
				final JSONObject gebot = gebote.getJSONObject(j);
				final int preis = gebot.getInt("preis");
				final int roboterID = gebot.getInt("spieler") == unsereID ? 0 : 1;

				// Bei Gleichstand kriegt keiner die Karte
				if (preis == hoechstesGebot) {
					hoechstbietender = null;
				} else if (preis > hoechstesGebot) {
					hoechstesGebot = preis;
					hoechstbietender = roboterID;
				}

				zustand.roboter[roboterID].geld -= preis;
			}

			// Ihm die Karte geben
			if (hoechstbietender != null) {
				final int prioritaet = auktionsergebnis.getInt("karte");
				zustand.roboter[hoechstbietender].karten.add(karteMitPrioritaet(prioritaet));
			}
		}

		return zustand;

	}

	public static Spielzustand powerdowns(final JSONArray powerdowns, final int unsereID, final Spielzustand zustand) {

		for (final Roboter roboter : zustand.roboter) {
			roboter.poweredDown = false;
		}

		for (int i = 0; i < powerdowns.length(); ++i) {
			zustand.roboter[powerdowns.getInt(i) == unsereID ? 0 : 1].poweredDown = true;
		}

		return zustand;

	}

	public static Spielzustand handkarten(final JSONArray handkarten, final int unsereID, final Spielzustand zustand) {

		for (int i = 0; i < handkarten.length(); ++i) {
			final JSONObject o = handkarten.getJSONObject(i);
			zustand.roboter[o.getInt("spielerID") == unsereID ? 0 : 1].karten = kartenParsen(o.getJSONArray("karten"));
		}

		return zustand;

	}
}