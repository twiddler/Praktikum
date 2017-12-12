/**
 * 
 * @author xXx Players xXx
 *
 */

final class Flagge extends Bewegbar implements Cloneable {

	int nummer;

	Flagge(final int position, final int nummer) {
		this.position = position;
		this.nummer = nummer;
	}

	@Override
	public Flagge clone() {
		Flagge result = null;
		try {
			result = (Flagge) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return result;
	}

	void beruehren(final Roboter roboter, final Spielzustand zustand) {
		if (roboter.stehtAufPosition(this.position) && this.nummer == roboter.naechsteFlagge) {
			if (this.nummer < Parameter.ANZAHL_FLAGGEN - 1 || zustand.istLetzterZug()) {
				++roboter.naechsteFlagge;
			}
		}
	}

}
