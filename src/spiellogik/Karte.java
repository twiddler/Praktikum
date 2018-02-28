package spiellogik;

public final class Karte implements Cloneable, Comparable<Karte> {

	public int prioritaet;
	int drehung_roboter;
	int schritte;
	int drehung_feld;

	public static Karte dummy = new Karte(Integer.MIN_VALUE, 0, 0, 0);

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
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (!Karte.class.isAssignableFrom(obj.getClass())) {
	        return false;
	    }
	    return this.prioritaet == ((Karte) obj).prioritaet;
	}
	
	@Override
	public int compareTo(Karte karte) {
	    if (karte == null) {
	    	throw new NullPointerException();
	    }
	    return this.prioritaet - karte.prioritaet;
	}

}