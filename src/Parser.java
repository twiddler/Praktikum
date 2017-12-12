import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Parser {

	static final int spielerID = 0;

	public Spielzustand parseSpielzustand(JSONObject jsonSpielzustand) throws JSONException {
		JSONArray Felder = jsonSpielzustand.getJSONArray("spielbrett");
		Feld[] retFelder = new Feld[Felder.length()];
		List<List<Integer>> posSonderfeld = new ArrayList<>(5);
		List<List<Integer>> posFeldzusatz = new ArrayList<>(2);
		List<Integer> posLaser = new ArrayList<Integer>();
		int[][] nachbarListe = nachbarListe();

		Flagge[] flaggen = new Flagge[4];

		Kante wand = new Wand();
		Kante schlucht = new Schlucht();
		Kante einbahn_rein = new Einbahn_rein();
		Kante einbahn_raus = new Einbahn_raus();
		Kante laser = new Laser();
		Kante normal = new Kante();

		for (int i = 0; i < Felder.length(); i++) {
			JSONObject pFeld = Felder.getJSONObject(i);

			JSONObject pTyp = pFeld.getJSONObject("typ");
			JSONObject zusaetze = pFeld.getJSONObject("zusaetze");

			JSONObject zusaetzeMitte = zusaetze.getJSONObject("mitte");
			JSONArray pKanten = zusaetze.getJSONArray("kanten");

			Kante[] kanten = new Kante[6];
			for (int j = 0; j < pKanten.length(); j++) {
				String kante = (String) pKanten.get(i);
				switch (kante) {
				case "mauer":
					kanten[j] = wand;
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

			Zusatz zusatz;
			String zusatzName = zusaetzeMitte.getString("zusatztyp");
			switch (zusatzName) {
			case "zahltag":
				zusatz = new Zahltag(100);
				posFeldzusatz.get(1).add(i);
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
				posFeldzusatz.get(2).add(i);
				break;
			default:
				zusatz = null;
				break;
			}

			int flaggenNr = zusaetzeMitte.getInt("flagge");
			if (flaggenNr != 0) {
				flaggen[flaggenNr - 1] = new Flagge(i, flaggenNr - 1);
			}

			String typ = pTyp.getString("feldtyp");
			switch (typ) {
			case "dreh":
				retFelder[i] = new Drehfeld(nachbarListe[i], kanten, zusatz, 0, i, pTyp.getInt("richtung"));
				posSonderfeld.get(4).add(i);
				break;
			case "loch":
				retFelder[i] = new Loch(nachbarListe[i], kanten, zusatz, 0, i);
				break;
			case "laufband":
				retFelder[i] = new Laufband(nachbarListe[i], kanten, zusatz, 0, i, pTyp.getInt("richtung"));
				posSonderfeld.get(3).add(i);
				break;
			case "aixpress":
				retFelder[i] = new Laufband(nachbarListe[i], kanten, zusatz, 0, i, pTyp.getInt("richtung"));
				posSonderfeld.get(1).add(i);
				posSonderfeld.get(2).add(i);
				break;
			case "reparatur":
				retFelder[i] = new Reparaturfeld(nachbarListe[i], kanten, zusatz, 0, i, 1); // Gesundheit??
				posSonderfeld.get(5).add(i);
				break;
			default:
				retFelder[i] = new Feld(nachbarListe[i], kanten, zusatz, 0, i);
				break;
			}
		}

		int[][] retSonderfeld = befuelleArray2(posSonderfeld);
		int[][] retFeldzusatz = befuelleArray2(posFeldzusatz);
		int[] retLaser = befuelleArray(posLaser);

		// int geld = jsonSpielzustand.getInt("startgeld");
		JSONArray spieler = jsonSpielzustand.getJSONArray("spieler");
		JSONArray roboter = jsonSpielzustand.getJSONArray("roboter");

		Roboter[] roboterArray = new Roboter[2];

		for (int i = 0; i < 2; i++) {
			JSONObject robo = roboter.getJSONObject(i);
			JSONObject pos = robo.getJSONObject("position");
			JSONObject sp = roboter.getJSONObject(i);
			JSONArray spKarten = sp.getJSONArray("karten");
			ArrayList<Karte> karten = new ArrayList<Karte>();
			for (int j = 0; j < spKarten.length(); j++) {
				JSONObject pKarte = spKarten.getJSONObject(j);
				karten.add(new Karte(pKarte.getInt("prioritaet"), pKarte.getInt("rotation"), pKarte.getInt("schritte"),
						pKarte.getInt("felddrehung")));
			}
			Roboter roboterFertig = new Roboter(pos.getInt("feldindex"), pos.getInt("richtung"), sp.getInt("leben"),
					robo.getInt("gesundheit"), 0, sp.getInt("nextFlag") - 1, robo.getBoolean("virtuell"), karten); // geld
																													// fehlt
			if (spieler.getJSONObject(0).getInt("id") == spielerID) {
				roboterArray[0] = roboterFertig;
			} else {
				roboterArray[1] = roboterFertig;
			}
		}

		Spielzustand ret = new Spielzustand(roboterArray, retFelder, 0, flaggen);
		Spielzustand.positionenMitSonderfeld = retSonderfeld;
		Spielzustand.positionenMitFeldzusatz = retFeldzusatz;
		Spielzustand.positionenMitLasern = retLaser;
		return ret;
	}

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

	int[] befuelleArray(List<Integer> input) {
		int ret[] = new int[input.size()];
		for (int i = 0; i < input.size(); i++) {
			ret[i] = input.get(i);
		}
		return ret;
	}

	int[][] befuelleArray2(List<List<Integer>> input) {
		int ret[][] = new int[input.size()][];
		for (int i = 0; i < input.size(); i++) {
			ret[i] = new int[input.get(i).size()];
			for (int j = 0; j < input.get(i).size(); j++) {
				ret[i][j] = input.get(i).get(j);
			}
		}
		return ret;
	}
}
