package netzwerk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
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

public class Parser {

	static final int spielerID = 0;

	/**
	 * Das Kartendeck als Kartenobjekte. Wird einmal abgespeichert und danach
	 * zum Nachschlagen für gesperrte Slots benutzt.
	 */
	static ArrayList<Karte> deck;

	static ArrayList<Karte> kartenParsen(JSONArray karten) {
		ArrayList<Karte> result = new ArrayList<>();

		for (int i = 0; i < karten.length(); ++i) {
			JSONObject karte = karten.getJSONObject(i);
			int prioritaet = karte.getInt("prioritaet");
			int drehung_roboter = karte.getInt("rotation");
			int schritte = karte.getInt("schritte");
			int drehung_feld = karte.getInt("felddrehung");
			result.add(new Karte(prioritaet, drehung_roboter, schritte, drehung_feld));
		}

		return result;

	}

	/**
	 * Parst die Daten zu den Robotern. Diese überladene Funktion ist für die
	 * erste Runde, in der es noch keine gesperrten Karten und nur das Startgeld
	 * gibt.
	 */
	static Roboter[] roboterParsen(int startgeld, JSONArray spielerJSON, JSONArray roboterJSON, int unsereID) {
		Roboter[] result = new Roboter[2];

		for (int i = 0; i < roboterJSON.length(); ++i) {
			// Informationen aus dem Roboterarray
			JSONObject roboterObject = roboterJSON.getJSONObject(i);
			int roboterID = roboterObject.getInt("spielerID") == unsereID ? 0 : 1;
			JSONObject positionRichtung = roboterObject.getJSONObject("position");
			int position = positionRichtung.getInt("feldindex");
			int blickrichtung = positionRichtung.getInt("richtung");
			int gesundheit = roboterObject.getInt("gesundheit");
			boolean virtuell = roboterObject.getBoolean("virtuell");

			// Informationen aus dem Spielerarray
			JSONObject spielerObject = spielerJSON.getJSONObject(i);
			int leben = spielerObject.getInt("leben");
			int naechsteFlagge = spielerObject.getInt("nextFlag");
			ArrayList<Karte> karten = kartenParsen(spielerObject.getJSONArray("karten"));

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
	static Roboter[] roboterParsen(JSONArray spielerJSON, JSONArray roboterJSON, int unsereID, JSONArray programmeJSON,
			Spielzustand zustand) {
		Roboter[] result = roboterParsen(0, spielerJSON, roboterJSON, unsereID);

		// Kontostand übernehmen
		for (int i = 0; i < zustand.roboter.length; ++i) {
			result[i].geld = zustand.roboter[i].geld;
		}

		for (int i = 0; i < roboterJSON.length(); ++i) {
			// Wessen gesperrte Karten wollen wir berechnen?
			JSONObject programmVonSpieler = programmeJSON.getJSONObject(i);
			int roboterID = programmVonSpieler.getInt("spielerID") == unsereID ? 0 : 1;

			int freieSlots = Math.max(0, Math.min(Parameter.ZUEGE_PRO_RUNDE, result[roboterID].gesundheit - 1));
			ArrayList<Karte> gesperrteKarten = new ArrayList<>();
			JSONArray programm = programmVonSpieler.getJSONArray("programm");
			for (int slot = freieSlots; slot < Parameter.ZUEGE_PRO_RUNDE; ++slot) {
				gesperrteKarten.add(karteMitPrioritaet(programm.getInt(slot)));
			}
			result[roboterID].gesperrteKarten = gesperrteKarten;
		}

		return result;
	}

	static Feld[] felderParsen(JSONArray felder) {

		Feld[] result = new Feld[felder.length()];
		List<List<Integer>> posSonderfeld = new ArrayList<>();
		for (int i = 0; i < 5; ++i) {
			posSonderfeld.add(new ArrayList<>());
		}
		List<List<Integer>> posFeldzusatz = new ArrayList<>();
		for (int i = 0; i < 2; ++i) {
			posFeldzusatz.add(new ArrayList<>());
		}
		List<Integer> posLaser = new ArrayList<Integer>();
		int[][] nachbarListe = nachbarListe();

		Kante mauer = new Mauer();
		Kante schlucht = new Schlucht();
		Kante einbahn_rein = new EinbahnRein();
		Kante einbahn_raus = new EinbahnRaus();
		Kante laser = new Laser();
		Kante normal = new Kante();

		for (int i = 0; i < felder.length(); i++) {
			JSONObject pFeld = felder.getJSONObject(i);
			JSONObject pTyp = pFeld.getJSONObject("typ");
			JSONObject zusaetze = pFeld.getJSONObject("zusaetze");
			JSONObject zusaetzeMitte = zusaetze.getJSONObject("mitte");
			JSONArray pKanten = zusaetze.getJSONArray("kanten");

			Kante[] kanten = new Kante[6];
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
				zusatz = new Zahltag(100); // TODO: Wieviel wird ausgezahlt?
				posFeldzusatz.get(0).add(i);
				break;
			case "presse":
				JSONArray aktivIn = zusaetzeMitte.getJSONArray("aktiv");
				boolean[] aktiv = new boolean[5];
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
				result[i] = new Reparaturfeld(nachbarListe[i], kanten, zusatz, i, 1); // TODO:
																						// Wieviel
																						// wird
																						// repariert?
				posSonderfeld.get(4).add(i);
				break;
			default:
				result[i] = new Feld(nachbarListe[i], kanten, zusatz, i);
				break;
			}
		}

		int[][] retSonderfeld = befuelleArray2(posSonderfeld);
		int[][] retFeldzusatz = befuelleArray2(posFeldzusatz);
		int[] retLaser = befuelleArray(posLaser);

		Spielzustand.positionenMitSonderfeld = retSonderfeld;
		Spielzustand.positionenMitFeldzusatz = retFeldzusatz;
		Spielzustand.positionenMitLasern = retLaser;

		return result;

	}

	static Flagge[] flaggenParsen(JSONArray felder) {

		Flagge[] result = new Flagge[Parameter.ANZAHL_FLAGGEN];

		for (int i = 0; i < felder.length(); i++) {
			JSONObject feld = felder.getJSONObject(i);
			int flaggenNr = feld.getJSONObject("zusaetze").getInt("flagge");
			if (flaggenNr != 0) {
				result[flaggenNr - 1] = new Flagge(i, flaggenNr - 1);
			}
		}

		return result;

	}

	/**
	 * Sucht aus dem Kartendeck die Karte mit dieser Priorität.
	 */
	static Karte karteMitPrioritaet(int prioritaet) {
		for (Karte karte : deck) {
			if (karte.prioritaet == prioritaet) {
				return karte;
			}
		}

		System.err.println("Karte mit Prioritaet " + prioritaet + " nicht im Deck gefunden.");
		return null;
	}

	public static Spielzustand ersteRunde(JSONObject json, int unsereID) {

		Roboter[] roboter = roboterParsen(json.getInt("startgeld"), json.getJSONArray("spieler"),
				json.getJSONArray("roboter"), unsereID);
		Feld[] felder = felderParsen(json.getJSONArray("spielbrett"));
		Flagge[] flaggen = flaggenParsen(json.getJSONArray("spielbrett"));

		// Fürs spätere Nachschlagen das Deck abspeichern
		deck = kartenParsen(json.getJSONArray("kartendeck"));

		// Handkarten ins Deck
		for (Roboter r : roboter) {
			deck.addAll(r.karten);
		}

		return new Spielzustand(roboter, felder, 0, flaggen);

	}

	public static Spielzustand nteRunde(JSONObject json, int unsereID, Spielzustand zustand) {

		Roboter[] roboter = roboterParsen(json.getJSONArray("spieler"), json.getJSONArray("roboter"), unsereID,
				json.getJSONArray("programme"), zustand);
		Feld[] felder = felderParsen(json.getJSONArray("spielbrett"));
		Flagge[] flaggen = flaggenParsen(json.getJSONArray("spielbrett"));

		return new Spielzustand(roboter, felder, 0, flaggen);

	}
	//
	// public Spielzustand parseSpielzustand(JSONObject jsonSpielzustand) throws
	// JSONException {
	// JSONArray Felder = jsonSpielzustand.getJSONArray("spielbrett");
	// Feld[] retFelder = new Feld[Felder.length()];
	// List<List<Integer>> posSonderfeld = new ArrayList<>(5);
	// List<List<Integer>> posFeldzusatz = new ArrayList<>(2);
	// List<Integer> posLaser = new ArrayList<Integer>();
	// int[][] nachbarListe = nachbarListe();
	//
	// Flagge[] flaggen = new Flagge[4];
	//
	// Kante mauer = new Mauer();
	// Kante schlucht = new Schlucht();
	// Kante einbahn_rein = new EinbahnRein();
	// Kante einbahn_raus = new EinbahnRaus();
	// Kante laser = new Laser();
	// Kante normal = new Kante();
	//
	// for (int i = 0; i < Felder.length(); i++) {
	// JSONObject pFeld = Felder.getJSONObject(i);
	//
	// JSONObject pTyp = pFeld.getJSONObject("typ");
	// JSONObject zusaetze = pFeld.getJSONObject("zusaetze");
	//
	// JSONObject zusaetzeMitte = zusaetze.getJSONObject("mitte");
	// JSONArray pKanten = zusaetze.getJSONArray("kanten");
	//
	// Kante[] kanten = new Kante[6];
	// for (int j = 0; j < pKanten.length(); j++) {
	// String kante = (String) pKanten.get(j);
	// switch (kante) {
	// case "mauer":
	// kanten[j] = mauer;
	// break;
	// case "schlucht":
	// kanten[j] = schlucht;
	// break;
	// case "einbahnInnen":
	// kanten[j] = einbahn_rein;
	// break;
	// case "einbahnAussen":
	// kanten[j] = einbahn_raus;
	// break;
	// case "lazor":
	// kanten[j] = laser;
	// posLaser.add(j);
	// break;
	// default:
	// kanten[j] = normal;
	// break;
	// }
	// }
	//
	// Zusatz zusatz;
	// String zusatzName = zusaetzeMitte.getString("zusatztyp");
	// switch (zusatzName) {
	// case "zahltag":
	// zusatz = new Zahltag(100);
	// posFeldzusatz.get(0).add(i);
	// break;
	// case "presse":
	// JSONArray aktivIn = zusaetzeMitte.getJSONArray("aktiv");
	// boolean[] aktiv = new boolean[5];
	// for (int j = 0; j < aktiv.length; j++) {
	// if (aktivIn.toString().contains(Integer.toString(j))) {
	// aktiv[j] = true;
	// } else {
	// aktiv[j] = false;
	// }
	// }
	// zusatz = new Presse(aktiv);
	// posFeldzusatz.get(1).add(i);
	// break;
	// default:
	// zusatz = null;
	// break;
	// }
	//
	// int flaggenNr = zusaetzeMitte.getInt("flagge");
	// if (flaggenNr != 0) {
	// flaggen[flaggenNr - 1] = new Flagge(i, flaggenNr - 1);
	// }
	//
	// String typ = pTyp.getString("feldtyp");
	// switch (typ) {
	// case "dreh":
	// retFelder[i] = new Drehfeld(nachbarListe[i], kanten, zusatz, i,
	// pTyp.getInt("richtung"));
	// posSonderfeld.get(3).add(i);
	// break;
	// case "loch":
	// retFelder[i] = new Loch(nachbarListe[i], kanten, zusatz, i);
	// break;
	// case "laufband":
	// retFelder[i] = new Laufband(nachbarListe[i], kanten, zusatz, i,
	// pTyp.getInt("richtung"));
	// posSonderfeld.get(2).add(i);
	// break;
	// case "aixpress":
	// retFelder[i] = new Laufband(nachbarListe[i], kanten, zusatz, i,
	// pTyp.getInt("richtung"));
	// posSonderfeld.get(0).add(i);
	// posSonderfeld.get(1).add(i);
	// break;
	// case "reparatur":
	// retFelder[i] = new Reparaturfeld(nachbarListe[i], kanten, zusatz, i, 1);
	// // Gesundheit??
	// posSonderfeld.get(4).add(i);
	// break;
	// default:
	// retFelder[i] = new Feld(nachbarListe[i], kanten, zusatz, i);
	// break;
	// }
	// }
	//
	// int[][] retSonderfeld = befuelleArray2(posSonderfeld);
	// int[][] retFeldzusatz = befuelleArray2(posFeldzusatz);
	// int[] retLaser = befuelleArray(posLaser);
	//
	// // int geld = jsonSpielzustand.getInt("startgeld");
	// JSONArray spieler = jsonSpielzustand.getJSONArray("spieler");
	// JSONArray roboter = jsonSpielzustand.getJSONArray("roboter");
	//
	// Roboter[] roboterArray = new Roboter[2];
	//
	// for (int i = 0; i < 2; i++) {
	// JSONObject robo = roboter.getJSONObject(i);
	// JSONObject pos = robo.getJSONObject("position");
	// JSONObject sp = roboter.getJSONObject(i);
	// JSONArray spKarten = sp.getJSONArray("karten");
	// ArrayList<Karte> karten = new ArrayList<Karte>();
	// for (int j = 0; j < spKarten.length(); j++) {
	// JSONObject pKarte = spKarten.getJSONObject(j);
	// karten.add(new Karte(pKarte.getInt("prioritaet"),
	// pKarte.getInt("rotation"), pKarte.getInt("schritte"),
	// pKarte.getInt("felddrehung")));
	// }
	// Roboter roboterFertig = new Roboter(pos.getInt("feldindex"),
	// pos.getInt("richtung"), sp.getInt("leben"),
	// robo.getInt("gesundheit"), 0, sp.getInt("nextFlag") - 1,
	// robo.getBoolean("virtuell"), karten); // geld
	// // fehlt
	// if (spieler.getJSONObject(0).getInt("id") == spielerID) {
	// roboterArray[0] = roboterFertig;
	// } else {
	// roboterArray[1] = roboterFertig;
	// }
	// }
	//
	// Spielzustand ret = new Spielzustand(roboterArray, retFelder, 0, flaggen);
	// Spielzustand.positionenMitSonderfeld = retSonderfeld;
	// Spielzustand.positionenMitFeldzusatz = retFeldzusatz;
	// Spielzustand.positionenMitLasern = retLaser;
	// return ret;
	// }

	static void setzeNachbarn(int index1, int index2, int rotation, int[][] felderNachbarn) {
		felderNachbarn[index1][rotation] = index2;
		felderNachbarn[index2][(rotation + 3) % 6] = index1;
	}

	static int[][] nachbarListe() {
		int[][] ret = new int[127][6];
		for (int i = 0; i < 6; i++) {
			setzeNachbarn(0, i + 1, i, ret);
		}
		for (int i = 0; i < 6; i++) {
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

	static int getIndexNr(int ring, int stelle) {
		int s = stelle % ((ring + 1) * 6);
		if (ring != 6) {
			return 3 * ring * (ring + 1) + s + 1;
		} else {
			return letzterRing(s);
		}
	}

	static int letzterRing(int s) {
		int ret[] = new int[42];
		int p = 1;
		int t = 24;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 7; j++) {
				ret[p] = getIndexNr(5, t);
				p++;
				p %= 42;
				t--;
				if (t < 0) {
					t = 35;
				}
			}
			t += 13;
			t %= 36;
		}
		return ret[s];
	}

	static int[] befuelleArray(List<Integer> input) {
		int ret[] = new int[input.size()];
		for (int i = 0; i < input.size(); i++) {
			ret[i] = input.get(i);
		}
		return ret;
	}

	static int[][] befuelleArray2(List<List<Integer>> input) {
		int ret[][] = new int[input.size()][];
		for (int i = 0; i < input.size(); i++) {
			ret[i] = new int[input.get(i).size()];
			for (int j = 0; j < input.get(i).size(); j++) {
				ret[i][j] = input.get(i).get(j);
			}
		}
		return ret;
	}

	public static ArrayList<Karte> bietoptionen(JSONArray bietoptionen) {

		return kartenParsen(bietoptionen);

	}

	/**
	 * Gibt den Gewinnern der Auktionen die Karten und zieht die Gebote vom
	 * Kontostand ab
	 */
	public static Spielzustand auktionsergebnisAuswerten(JSONArray auktionsergebnisse, Spielzustand zustand,
			int unsereID) {

		for (int i = 0; i < auktionsergebnisse.length(); ++i) {
			// Für jede Karte den Höchstbietenden ermitteln
			JSONObject auktionsergebnis = auktionsergebnisse.getJSONObject(i);
			JSONArray gebote = auktionsergebnis.getJSONArray("gebote");
			Integer hoechstbietender = null;
			int hoechstesGebot = 0; // 0 ^= kein Gebot
			for (int j = 0; j < gebote.length(); ++j) {
				JSONObject gebot = gebote.getJSONObject(j);
				int preis = gebot.getInt("preis");
				int roboterID = gebot.getInt("spieler") == unsereID ? 0 : 1;

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
				int prioritaet = auktionsergebnis.getInt("karte");
				zustand.roboter[hoechstbietender].karten.add(karteMitPrioritaet(prioritaet));
			}
		}

		return zustand;

	}

	public static Spielzustand powerdowns(JSONArray powerdowns, int unsereID, Spielzustand zustand) {

		for (Roboter roboter : zustand.roboter) {
			roboter.poweredDown = false;
		}

		for (int i = 0; i < powerdowns.length(); ++i) {
			zustand.roboter[powerdowns.getInt(i) == unsereID ? 0 : 1].poweredDown = true;
		}

		return zustand;

	}

	public static Spielzustand handkarten(JSONArray handkarten, int unsereID, Spielzustand zustand) {

		for (int i = 0; i < handkarten.length(); ++i) {
			JSONObject o = handkarten.getJSONObject(i);
			zustand.roboter[o.getInt("spielerID") == unsereID ? 0 : 1].karten = kartenParsen(o.getJSONArray("karten"));
		}

		return zustand;

	}
}