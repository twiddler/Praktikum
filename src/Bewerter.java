class Bewerter {

	static int[] bewerten(Spielzustand zustand) {
		Roboter wir = zustand.roboter[0];
		Roboter gegner = zustand.roboter[1];

		return new int[] { wir.letzteFlagge == Parameter.ANZAHL_FLAGGEN ? 1 : 0,
				gegner.letzteFlagge != Parameter.ANZAHL_FLAGGEN ? 1 : 0, wir.leben > 0 || wir.gesundheit > 0 ? 1 : 0,
				gegner.leben == 0 && gegner.gesundheit == 0 ? 1 : 0, wir.letzteFlagge, gegner.letzteFlagge,
				wir.leben > 0 ? 1 : 0 };
	}

	static boolean besserAls(int[] a, int[] b) {
		for (int i = 0; i < a.length; ++i) {
			if (a[i] > b[i])
				return true;
		}
		return false;
	}

}