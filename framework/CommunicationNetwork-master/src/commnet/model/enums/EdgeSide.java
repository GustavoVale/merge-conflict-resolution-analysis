package commnet.model.enums;

public enum EdgeSide {
	LEFT("L"), RIGHT("R"), LEFTRIGHT("LR"), BOTH("B");

	String description;

	EdgeSide(String desc) {
		this.description = desc;
	}

	@Override
	public String toString() {
		return description;
	}

	/**
	 * Get @param text and check which EdgeSide it is
	 * 
	 * @param text
	 * @return the EdgeSide if it corresponds to one of them or null otherwise
	 */
	public static EdgeSide fromString(String text) {
		for (EdgeSide b : EdgeSide.values()) {
			if (b.description.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}
}
