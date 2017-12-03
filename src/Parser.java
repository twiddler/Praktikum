import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Parser {
	
	public Spielzustand parseSpielzustand(JSONObject jsonSpielzustand) throws JSONException {
		JSONArray Felder = jsonSpielzustand.getJSONArray("spielbrett");
		Feld[] retFelder = new Feld[Felder.length()];
		List<List<Integer>> posSonderfeld = new ArrayList<>(5);
		List<List<Integer>> posFeldzusatz = new ArrayList<>(2);
		List<Integer> posLaser = new ArrayList<Integer>();
		int[][] nachbarListe = nachbarListe();
		
		Kante wand = new Wand();
		Kante schlucht = new Schlucht();
		Kante einbahn_rein = new Einbahn_rein();
		Kante einbahn_raus = new Einbahn_raus();
		Kante laser = new Laser();
		Kante normal = new Kante();
		
		for (int i = 0; i < Felder.length(); i++) {
			JSONObject pFeld = Felder.getJSONObject(i);
			JSONArray pKanten = pFeld.getJSONArray("kanten");
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
			String zusatzName = pFeld.getString("zusatztyp");
			switch (zusatzName) {
			case "zahltag":
				zusatz = new Zahltag(100);
				posFeldzusatz.get(1).add(i);
				break;
			case "presse":
				zusatz = new Presse(null);//??
				posFeldzusatz.get(2).add(i);
				break;
			default:
				zusatz = null;
				break;
			}

			String typ = pFeld.getString("feldtyp");
			switch (typ) {
			case "dreh":
				retFelder[i] = new Drehfeld(nachbarListe[i], kanten, zusatz, 0, i, pFeld.getInt("richtung")); 
				posSonderfeld.get(4).add(i);
				break;
			case "loch":
				retFelder[i] = new Loch(nachbarListe[i], kanten, zusatz, 0, i);
				break;
			case "laufband":
				retFelder[i] = new Laufband(nachbarListe[i], kanten, zusatz, 0, i, pFeld.getInt("richtung"));
				posSonderfeld.get(3).add(i);
				break;
			case "aixpress":
				retFelder[i] = new Laufband(nachbarListe[i], kanten, zusatz, 0, i, pFeld.getInt("richtung"));
				posSonderfeld.get(1).add(i);
				posSonderfeld.get(2).add(i);
				break;
			case "reparatur":
				retFelder[i] = new Reparaturfeld(nachbarListe[i], kanten, zusatz, 0, i, 1); //Gesundheit??
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
		Spielzustand ret = new Spielzustand(null, retFelder, 0, null);
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
	
	int[] befuelleArray(List<Integer> input){
		int ret[] = new int[input.size()];
		for(int i = 0; i < input.size(); i++){
			ret[i] = input.get(i);
		}
		return ret;
	}
	
	int[][] befuelleArray2(List<List<Integer>> input){
		int ret[][] = new int[input.size()][];
		for(int i = 0; i < input.size(); i++){
			ret[i] = new int[input.get(i).size()];
			for(int j = 0; j < input.get(i).size(); j++){
				ret[i][j] = input.get(i).get(j);
			}
		}
		return ret;
	}
 }
