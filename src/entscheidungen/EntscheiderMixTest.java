package entscheidungen;

import static org.junit.Assert.assertArrayEquals;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class EntscheiderMIXTest {

	/**
	 * Testet, ob aus einer Liste von Vertretern das richtige Kind genommen wird. In
	 * den Listen stehen (Priorität, Bewertung) von netten (n) und blöden (b)
	 * Kindern. Die Bewertung ist nur die in der ersten Komponente.
	 */
	public void welchesKindTest(TreeMap<Integer, Integer> n, TreeMap<Integer, Integer> b, int bewertung1Soll) {

		// Aus o.g. Testdaten Knoten bauen
		EntscheiderMIX entscheider = new EntscheiderMIX(new Bewerter());
		TreeMap<Integer, Knoten> netteKinder = new TreeMap<>();
		while (!n.isEmpty()) {
			Map.Entry<Integer, Integer> pb = n.pollLastEntry();
			Knoten kind = new Knoten(null, 0, null);
			int[] bewertung = entscheider.bewerter.schlechtesterWert;
			bewertung[0] = pb.getValue();
			kind.bewertung = bewertung;
			netteKinder.put(pb.getKey(), kind);
		}

		TreeMap<Integer, Knoten> bloedeKinder = new TreeMap<>();
		while (!b.isEmpty()) {
			Map.Entry<Integer, Integer> pb = b.pollLastEntry();
			Knoten kind = new Knoten(null, 1, null);
			int[] bewertung = entscheider.bewerter.schlechtesterWert;
			bewertung[0] = pb.getValue();
			kind.bewertung = bewertung;
			bloedeKinder.put(pb.getKey(), kind);
		}

		// Ausgegebene Bewertung prüfen
		Knoten kind = entscheider.welchesKind(netteKinder, bloedeKinder);
		int[] bewertungIst = kind.bewertung;
		int[] bewertungSoll = entscheider.bewerter.schlechtesterWert;
		bewertungSoll[0] = bewertung1Soll;

		assertArrayEquals(bewertungSoll, bewertungIst);

	}

	/**
	 * Testet, ob aus einer Liste von Vertretern das richtige Kind genommen wird.
	 */
	@Test
	public void welchesKindTest() {

		// (Priorität, Bewertung) von netten (n) und blöden (b) Kindern, und erwartete
		// Bewertung (nur erste Komponente)
		TreeMap<Integer, Integer> n = new TreeMap<>();
		TreeMap<Integer, Integer> b = new TreeMap<>();

		n.put(4, -3);
		n.put(2, 6);
		n.put(1, -1);

		b.put(6, 100);
		b.put(5, 8);
		b.put(3, 7);
		b.put(0, 5);

		welchesKindTest(n, b, 6);

		n.clear();
		b.clear();

		n.put(7, 2);
		n.put(4, 7);

		b.put(6, 13);
		b.put(5, 5);
		b.put(3, 4);

		welchesKindTest(n, b, 5);

		n.clear();
		b.clear();

		n.put(7, 2);
		n.put(4, 7);
		n.put(2, 0);

		b.put(6, 13);
		b.put(5, 5);
		b.put(3, 4);
		b.put(0, 12);

		welchesKindTest(n, b, 5);

		n.clear();
		b.clear();

		n.put(6, 5);
		n.put(3, 13);
		n.put(2, 18);

		b.put(5, 8);
		b.put(4, 17);
		b.put(1, 2);
		b.put(0, 7);

		welchesKindTest(n, b, 8);

		n.clear();
		b.clear();

		n.put(6, 5);
		n.put(3, 6);
		n.put(2, 6);

		b.put(5, 8);
		b.put(4, 17);
		b.put(1, 2);
		b.put(0, 7);

		welchesKindTest(n, b, 6);

		n.clear();
		b.clear();

		n.put(6, 5);
		n.put(3, 4);
		n.put(2, 4);

		b.put(5, 8);
		b.put(4, 17);
		b.put(1, 2);
		b.put(0, 7);

		welchesKindTest(n, b, 5);

		n.clear();
		b.clear();

		n.put(3, 4);
		n.put(2, 4);

		b.put(5, 8);
		b.put(4, 17);
		b.put(1, 2);
		b.put(0, 7);

		welchesKindTest(n, b, 4);

	}
}