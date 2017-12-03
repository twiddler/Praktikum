import java.util.ArrayList;

public class Knoten {

	Spielzustand zustand;
	ArrayList<Knoten> nachfolger;
	int[] bewertung;
	Karte karte;

	public Knoten(Spielzustand zustand) {
		this.zustand = zustand;
		nachfolger = new ArrayList<Knoten>();
	}
	
	
}
