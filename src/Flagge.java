/**
 * 
 * @author xXx Players xXx
 *
 */
final class Flagge implements Cloneable {

	/**
	 * Feld, auf dem der Roboter steht, referenziert durch dessen Position.
	 */
	int position;

	Flagge(final int position) {
		this.position = position;
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
	
	boolean stehtAufPosition(final int position) {
		return this.position == position;
	}

	void beruehren(Roboter roboter) {
		if (roboter.stehtAufPosition(this.position)) {
			roboter.position = this.position;
		}
	}
	
}
