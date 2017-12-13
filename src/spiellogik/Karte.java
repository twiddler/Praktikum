package spiellogik;

public final class Karte implements Cloneable {

	public int prioritaet;
	int drehung_roboter;
	int schritte;
	int drehung_feld;

	public Karte(final int prioritaet, final int drehung_roboter, final int schritte, final int drehung_feld) {
		this.prioritaet = prioritaet;
		this.drehung_roboter = drehung_roboter;
		this.schritte = schritte;
		this.drehung_feld = drehung_feld;
	}

	@Override
	public Karte clone() {
		Karte result = null;
		try {
			result = (Karte) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		return result;
	}

}