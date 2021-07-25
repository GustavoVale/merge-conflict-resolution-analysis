package commnet.model.enums;

public enum ChangeType {

	FORMATTING("F"), ACCESS_MODIFIER("AM"), OTHER("OT");

	String description;

	ChangeType(String desc) {
		this.description = desc;
	}

	@Override
	public String toString() {
		return description;
	}

	/**
	 * Get @param text and check which ChangeType it is
	 * 
	 * @param text
	 * @return the ChangeType if it corresponds to one of them or null otherwise
	 */
	public static ChangeType fromString(String text) {
		for (ChangeType b : ChangeType.values()) {
			if (b.description.equalsIgnoreCase(text)) {
				return b;
			}
		}
		return null;
	}

}
