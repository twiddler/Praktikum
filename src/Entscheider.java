
public class Entscheider {

	Knoten wurzel;
	Bewerter bewerter;

	public Entscheider(Spielzustand zustand) {
		this.wurzel = new Knoten(zustand);
	}

	int[] alphabeta(Knoten knoten, int tiefe, int[] alpha, int[] beta, int spieler, int prioritaet) {
		int[] result;
		if (tiefe == 0) {
			return this.bewerter.bewerten(knoten.zustand);
			 
		}

		if (spieler == 0) {
			result = bewerter.schlechtesterWert();
			for (Karte karte : knoten.zustand.roboter[0].karten) {
				if (karte.prioritaet < prioritaet) {
					Spielzustand neuerZustand = knoten.zustand.karteSpielen(knoten.zustand.roboter[0], karte);
					Knoten neuerKnoten = new Knoten(neuerZustand);
					knoten.karte = karte;
					knoten.zustand.roboter[0].karten.remove(karte);
					knoten.nachfolger.add(neuerKnoten);
					result = bewerter.besseres(result,
							alphabeta(neuerKnoten, tiefe - 1, alpha, beta, 2, Integer.MAX_VALUE));
					alpha = bewerter.besseres(alpha, result);
					if (bewerter.istBesser(alpha, beta)) {
						break;
					}

				}
			}
			knoten.bewertung = result;
			return result;

		} else if (spieler == 1) {
			result = bewerter.besterWert();
			for (Karte karte : knoten.zustand.roboter[1].karten) {
				if (karte.prioritaet < prioritaet) {
					Spielzustand neuerZustand = knoten.zustand.karteSpielen(knoten.zustand.roboter[1], karte);
					Knoten neuerKnoten = new Knoten(neuerZustand);
					knoten.karte = karte;
					knoten.zustand.roboter[1].karten.remove(karte);
					knoten.nachfolger.add(neuerKnoten);
					result = bewerter.schlechteres(result,
							alphabeta(neuerKnoten, tiefe - 1, alpha, beta, 2, Integer.MAX_VALUE));
					beta = bewerter.schlechteres(alpha, result);
					if (bewerter.istBesser(alpha, beta)) {
						break;
					}
				}
			}
			knoten.bewertung = result;
			return result;

		} else {
			int[] minResult = bewerter.besterWert();
			int gegnerischePriotitaet = -1; // ??
			for (Karte karte : knoten.zustand.roboter[1].karten) {
				Spielzustand neuerZustand = knoten.zustand.karteSpielen(knoten.zustand.roboter[1], karte);
				Knoten neuerKnoten = new Knoten(neuerZustand);
				knoten.karte = karte;
				knoten.zustand.roboter[1].karten.remove(karte);
				knoten.nachfolger.add(neuerKnoten);
				int[] newResult = alphabeta(neuerKnoten, tiefe - 1, alpha, beta, 0, karte.prioritaet);
				if(bewerter.istBesser(newResult, minResult)){ // wenn das neue schlechter ist als das aktuelle ??
					minResult = newResult;
					gegnerischePriotitaet = karte.prioritaet;
				}
				
				beta = bewerter.schlechteres(alpha, minResult);
				if (bewerter.istBesser(alpha, beta)) {
					break;

				}
			
			}
			int[] maxResult = bewerter.schlechtesterWert();
			int eigenePriotitaet = -1; 
			for (Karte karte : knoten.zustand.roboter[0].karten) {
				Spielzustand neuerZustand = knoten.zustand.karteSpielen(knoten.zustand.roboter[0], karte);
				Knoten neuerKnoten = new Knoten(neuerZustand);
				knoten.karte = karte;
				knoten.zustand.roboter[0].karten.remove(karte);
				knoten.nachfolger.add(neuerKnoten);
				int[] newResult = alphabeta(neuerKnoten, tiefe - 1, alpha, beta, 0, karte.prioritaet);
				if(bewerter.istBesser(maxResult, newResult)){ // wenn das neue besser ist als das aktuelle ??
					maxResult = newResult;
					eigenePriotitaet = karte.prioritaet;
				}
				alpha = bewerter.besseres(alpha, maxResult);
				if (bewerter.istBesser(alpha, beta)) {
					break;
				}
			}
			if(gegnerischePriotitaet > eigenePriotitaet){
				knoten.bewertung = minResult;
				return minResult;
			}
			knoten.bewertung = maxResult;
			return maxResult;
		}
	}

	Karte[] zuspielendeKarten(){
		Karte[] karten = new Karte[5];
		Knoten aktuell = wurzel;
		int[] bewertung = wurzel.bewertung;
		for(int i = 0; i < karten.length; i++){
			for(Knoten knoten : aktuell.nachfolger){
				if(bewertung == knoten.bewertung){
					karten[i] = knoten.karte;
					aktuell = knoten;
					break;
				}
			}
		}
		
		
		return karten;
	}
}
