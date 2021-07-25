package commnet.model.enums;

public enum ExtraMode {
	COMMITUPDATER ("CU"),
	EDGEREMOVER ("ER"),
	DEVELOPERREMOVER ("DR"),
	ISSUEREMOVER ("IR"),
	NONUPDATEDCOMMITRETRIEVER ("NUCR"),
	PROJECTMETRICREMOVER ("PMR"),
	NETMETRICREMOVER ("NMR"),
	MSMETRICREMOVER ("MSMR"),
	FILEMETRICREMOVER ("FIMR"),
	CHUNKMETRICREMOVER ("CHMR"),
	COMMITFIELDSUPDATER ("CFU");
	
	String description;

	ExtraMode (String desc){
		this.description = desc;
	}
	
	@Override
	public String toString(){
		return description;
	}
}
