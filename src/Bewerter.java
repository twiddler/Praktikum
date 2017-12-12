class Bewerter {

	final int anzahlBewertungen = 7;

	int[] bewerten(Spielzustand zustand) {
		Roboter wir = zustand.roboter[0];
		Roboter gegner = zustand.roboter[1];

		return new int[] { wir.naechsteFlagge == Parameter.ANZAHL_FLAGGEN ? 1 : 0,
				gegner.naechsteFlagge < Parameter.ANZAHL_FLAGGEN ? 1 : 0, wir.leben > 0 || wir.gesundheit > 0 ? 1 : 0,
				gegner.leben == 0 && gegner.gesundheit == 0 ? 1 : 0, wir.naechsteFlagge, gegner.naechsteFlagge,
				wir.leben > 0 ? 1 : 0 };
	}

	boolean istBesser(int[] a, int[] b) {
		for (int i = 0; i < a.length; ++i) {
			if (a[i] > b[i])
				return true;
		}
		return false;
	}

	boolean istSchlechter(int[] a, int[] b) {
		for (int i = 0; i < a.length; ++i) {
			if (a[i] < b[i])
				return true;
		}
		return false;
	}

	int[] besseres(int[] a, int[] b) {
		return istBesser(a, b) ? a : b;
	}

	int[] schlechteres(int[] a, int[] b) {
		return istBesser(b, a) ? a : b;
	}

	int[] schlechtesterWert() {
		int[] result = new int[anzahlBewertungen];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.MIN_VALUE;
		}
		return result;
	}

	int[] besterWert() {
		int[] result = new int[anzahlBewertungen];
		for (int i = 0; i < result.length; i++) {
			result[i] = Integer.MAX_VALUE;
		}
		return result;
	}

}