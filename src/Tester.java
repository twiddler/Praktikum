import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Map;
import java.util.TreeMap;

public class Tester {

	/**
	 * Testet, ob aus einer Liste von Vertretern das richtige Kind genommen wird.
	 */
	@Test
	public void welchesKindTest() {

		// (Priorität, Bewertung) von netten (n) und blöden (b) Kindern, und erwartete
		// Bewertung (nur erste Komponente)
		TreeMap<Integer, Integer> n = new TreeMap<>();
		n.put(4, -3);
		n.put(2, 6);
		n.put(1, -1);

		TreeMap<Integer, Integer> b = new TreeMap<>();
		b.put(6, 100);
		b.put(5, 8);
		b.put(3, 7);
		b.put(0, 5);

		int bewertung1Soll = 6;

		// Aus o.g. Testdaten Knoten bauen
		Entscheider entscheider = new Entscheider(null, new Bewerter());
		TreeMap<Integer, Knoten> netteKinder = new TreeMap<>();
		while (!n.isEmpty()) {
			Map.Entry<Integer, Integer> pb = n.pollLastEntry();
			Knoten kind = new Knoten(null);
			int[] bewertung = entscheider.bewerter.schlechtesterWert();
			bewertung[0] = pb.getValue();
			kind.bewertung = bewertung;
			netteKinder.put(pb.getKey(), kind);
		}

		TreeMap<Integer, Knoten> bloedeKinder = new TreeMap<>();
		while (!b.isEmpty()) {
			Map.Entry<Integer, Integer> pb = b.pollLastEntry();
			Knoten kind = new Knoten(null);
			int[] bewertung = entscheider.bewerter.schlechtesterWert();
			bewertung[0] = pb.getValue();
			kind.bewertung = bewertung;
			bloedeKinder.put(pb.getKey(), kind);
		}

		// Ausgegebene Bewertung prüfen
		Knoten kind = entscheider.welchesKind(netteKinder, bloedeKinder);
		int[] bewertungIst = kind.bewertung;
		int[] bewertungSoll = entscheider.bewerter.schlechtesterWert();
		bewertungSoll[0] = bewertung1Soll;

		assertArrayEquals(bewertungSoll, bewertungIst, "Falsche Bewertung");

	}
}