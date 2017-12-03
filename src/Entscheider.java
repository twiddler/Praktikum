
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
					// TODO karte löschen
					result = bewerter.besseres(result,
							alphabeta(new Knoten(neuerZustand), tiefe - 1, alpha, beta, 2, Integer.MAX_VALUE));
					alpha = bewerter.besseres(alpha, result);
					if (bewerter.istBesser(alpha, beta)) {

						break;
					}

				}
			}
			return result;

		} else if (spieler == 1) {
			result = bewerter.besterWert();
			for (Karte karte : knoten.zustand.roboter[1].karten) {
				if (karte.prioritaet < prioritaet) {
					Spielzustand neuerZustand = knoten.zustand.karteSpielen(knoten.zustand.roboter[1], karte);
					// TODO karte löschen
					result = bewerter.schlechteres(result,
							alphabeta(new Knoten(neuerZustand), tiefe - 1, alpha, beta, 2, Integer.MAX_VALUE));
					alpha = bewerter.schlechteres(alpha, result);
					if (bewerter.istBesser(alpha, beta)) {

						break;
					}
				}
			}
			return result;

		} else {
			result = ??;
			for (Karte karte : knoten.zustand.roboter[1].karten) {
				if (karte.prioritaet < prioritaet) {
					Spielzustand neuerZustand = knoten.zustand.karteSpielen(knoten.zustand.roboter[1], karte);
					// TODO karte löschen
					result = bewerter.schlechteres(result,
							alphabeta(new Knoten(neuerZustand), tiefe - 1, alpha, beta, 2, Integer.MAX_VALUE));
					alpha = bewerter.schlechteres(alpha, result);
					if (bewerter.istBesser(alpha, beta)) {

						break;
					}
				}
			}
			for (Karte karte : knoten.zustand.roboter[0].karten) {
				if (karte.prioritaet < prioritaet) {
					Spielzustand neuerZustand = knoten.zustand.karteSpielen(knoten.zustand.roboter[0], karte);
					// TODO karte löschen
					result = bewerter.schlechteres(result,
							alphabeta(new Knoten(neuerZustand), tiefe - 1, alpha, beta, 2, Integer.MAX_VALUE));
					alpha = bewerter.schlechteres(alpha, result);
					if (bewerter.istBesser(alpha, beta)) {

						break;
					}
				}
			}
			return result;
		}
	}

}
