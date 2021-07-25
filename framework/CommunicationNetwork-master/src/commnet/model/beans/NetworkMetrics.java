package commnet.model.beans;

public class NetworkMetrics {

	private Integer IdDB;
	private Integer networkIdDB;

	private Integer covFileOverChunk;
	private Integer covMSOverFile;

	private Integer covChunkBasedOverPrecise;
	private Integer covChunkBasedOverComprehensive;
	private Integer covChunkBasedOverArtifact;
	private Integer covPreciseOverChunkBased;
	private Integer covComprehensiveOverChunkBased;
	private Integer covArtifactOverChunkBased;

	private Integer covFileBasedOverPrecise;
	private Integer covFileBasedOverComprehensive;
	private Integer covFileBasedOverArtifact;
	private Integer covPreciseOverFileBased;
	private Integer covComprehensiveOverFileBased;
	private Integer covArtifactOverFileBased;

	private Integer covMSBasedOverPrecise;
	private Integer covMSBasedOverComprehensive;
	private Integer covMSBasedOverArtifact;
	private Integer covPreciseOverMSBased;
	private Integer covComprehensiveOverMSBased;
	private Integer covArtifactOverMSBased;

	private Integer numberComprehensiveEdges;
	private Integer numberPreciseEdges;
	private Integer numberArtifactEdges;

	private Integer numberChunkEdges;
	private Integer numberFileEdges;
	private Integer numberMSEdges;

	public NetworkMetrics(Integer networkidDB) {
		this(null, networkidDB, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, null, null, null, null, null, null, null, null, null);
	}

	public NetworkMetrics(Integer idDB, Integer networkidDB) {
		this(idDB, networkidDB, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
				null, null, null, null, null, null, null, null, null, null, null, null);
	}

	public NetworkMetrics(Integer idDB, Integer networkidDB, Integer cfch, Integer cmsf, Integer cchc, Integer cchp,
			Integer ccha, Integer cfc, Integer cfp, Integer cfa, Integer cmsc, Integer cmsp, Integer cmsa, Integer ccch,
			Integer cpch, Integer cach, Integer ccf, Integer cpf, Integer caf, Integer ccms, Integer cpms, Integer cams,
			Integer numComEd, Integer numPreEd, Integer numArtEd, Integer numChEd, Integer numFiEd, Integer numMsEd) {
		setIdDB(idDB);
		setNetworkIdDB(networkidDB);
		setCovFileOverChunk(cfch);
		setCovMSOverFile(cmsf);
		setCovChunkBasedOverComprehensive(cchc);
		setCovChunkBasedOverPrecise(cchp);
		setCovChunkBasedOverArtifact(ccha);
		setCovFileBasedOverComprehensive(cfc);
		setCovFileBasedOverPrecise(cfp);
		setCovFileBasedOverArtifact(cfa);
		setCovMSBasedOverComprehensive(cmsc);
		setCovMSBasedOverPrecise(cmsp);
		setCovMSBasedOverArtifact(cmsa);
		setCovComprehensiveOverChunkBased(ccch);
		setCovPreciseOverChunkBased(cpch);
		setCovArtifactOverChunkBased(cach);
		setCovComprehensiveOverFileBased(ccf);
		setCovPreciseOverFileBased(cpf);
		setCovArtifactOverFileBased(caf);
		setCovComprehensiveOverMSBased(ccms);
		setCovPreciseOverMSBased(cpms);
		setCovArtifactOverMSBased(cams);
		setNumberComprehensiveEdges(numComEd);
		setNumberPreciseEdges(numPreEd);
		setNumberArtifactEdges(numArtEd);
		setNumberChunkEdges(numChEd);
		setNumberFileEdges(numFiEd);
		setNumberMSEdges(numMsEd);
	}

	public Integer getIdDB() {
		return IdDB;
	}

	public void setIdDB(Integer idDB) {
		IdDB = idDB;
	}

	public Integer getNetworkIdDB() {
		return networkIdDB;
	}

	public void setNetworkIdDB(Integer networkIdDB) {
		this.networkIdDB = networkIdDB;
	}

	public Integer getCovFileOverChunk() {
		return covFileOverChunk;
	}

	public void setCovFileOverChunk(Integer covFileOverChunk) {
		this.covFileOverChunk = covFileOverChunk;
	}

	public Integer getCovMSOverFile() {
		return covMSOverFile;
	}

	public void setCovMSOverFile(Integer covMSOverFile) {
		this.covMSOverFile = covMSOverFile;
	}

	public Integer getCovChunkBasedOverPrecise() {
		return covChunkBasedOverPrecise;
	}

	public void setCovChunkBasedOverPrecise(Integer covChunkBasedOverPrecise) {
		this.covChunkBasedOverPrecise = covChunkBasedOverPrecise;
	}

	public Integer getCovChunkBasedOverComprehensive() {
		return covChunkBasedOverComprehensive;
	}

	public void setCovChunkBasedOverComprehensive(Integer covChunkBasedOverComprehensive) {
		this.covChunkBasedOverComprehensive = covChunkBasedOverComprehensive;
	}

	public Integer getCovChunkBasedOverArtifact() {
		return covChunkBasedOverArtifact;
	}

	public void setCovChunkBasedOverArtifact(Integer covChunkBasedOverArtifact) {
		this.covChunkBasedOverArtifact = covChunkBasedOverArtifact;
	}

	public Integer getCovPreciseOverChunkBased() {
		return covPreciseOverChunkBased;
	}

	public void setCovPreciseOverChunkBased(Integer covPreciseOverChunkBased) {
		this.covPreciseOverChunkBased = covPreciseOverChunkBased;
	}

	public Integer getCovComprehensiveOverChunkBased() {
		return covComprehensiveOverChunkBased;
	}

	public void setCovComprehensiveOverChunkBased(Integer covComprehensiveOverChunkBased) {
		this.covComprehensiveOverChunkBased = covComprehensiveOverChunkBased;
	}

	public Integer getCovArtifactOverChunkBased() {
		return covArtifactOverChunkBased;
	}

	public void setCovArtifactOverChunkBased(Integer covArtifactOverChunkBased) {
		this.covArtifactOverChunkBased = covArtifactOverChunkBased;
	}

	public Integer getCovFileBasedOverPrecise() {
		return covFileBasedOverPrecise;
	}

	public void setCovFileBasedOverPrecise(Integer covFileBasedOverPrecise) {
		this.covFileBasedOverPrecise = covFileBasedOverPrecise;
	}

	public Integer getCovFileBasedOverComprehensive() {
		return covFileBasedOverComprehensive;
	}

	public void setCovFileBasedOverComprehensive(Integer covFileBasedOverComprehensive) {
		this.covFileBasedOverComprehensive = covFileBasedOverComprehensive;
	}

	public Integer getCovFileBasedOverArtifact() {
		return covFileBasedOverArtifact;
	}

	public void setCovFileBasedOverArtifact(Integer covFileBasedOverArtifact) {
		this.covFileBasedOverArtifact = covFileBasedOverArtifact;
	}

	public Integer getCovPreciseOverFileBased() {
		return covPreciseOverFileBased;
	}

	public void setCovPreciseOverFileBased(Integer covPreciseOverFileBased) {
		this.covPreciseOverFileBased = covPreciseOverFileBased;
	}

	public Integer getCovComprehensiveOverFileBased() {
		return covComprehensiveOverFileBased;
	}

	public void setCovComprehensiveOverFileBased(Integer covComprehensiveOverFileBased) {
		this.covComprehensiveOverFileBased = covComprehensiveOverFileBased;
	}

	public Integer getCovArtifactOverFileBased() {
		return covArtifactOverFileBased;
	}

	public void setCovArtifactOverFileBased(Integer covArtifactOverFileBased) {
		this.covArtifactOverFileBased = covArtifactOverFileBased;
	}

	public Integer getCovMSBasedOverPrecise() {
		return covMSBasedOverPrecise;
	}

	public void setCovMSBasedOverPrecise(Integer covMSBasedOverPrecise) {
		this.covMSBasedOverPrecise = covMSBasedOverPrecise;
	}

	public Integer getCovMSBasedOverComprehensive() {
		return covMSBasedOverComprehensive;
	}

	public void setCovMSBasedOverComprehensive(Integer covMSBasedOverComprehensive) {
		this.covMSBasedOverComprehensive = covMSBasedOverComprehensive;
	}

	public Integer getCovMSBasedOverArtifact() {
		return covMSBasedOverArtifact;
	}

	public void setCovMSBasedOverArtifact(Integer covMSBasedOverArtifact) {
		this.covMSBasedOverArtifact = covMSBasedOverArtifact;
	}

	public Integer getCovPreciseOverMSBased() {
		return covPreciseOverMSBased;
	}

	public void setCovPreciseOverMSBased(Integer covPreciseOverMSBased) {
		this.covPreciseOverMSBased = covPreciseOverMSBased;
	}

	public Integer getCovComprehensiveOverMSBased() {
		return covComprehensiveOverMSBased;
	}

	public void setCovComprehensiveOverMSBased(Integer covComprehensiveOverMSBased) {
		this.covComprehensiveOverMSBased = covComprehensiveOverMSBased;
	}

	public Integer getCovArtifactOverMSBased() {
		return covArtifactOverMSBased;
	}

	public void setCovArtifactOverMSBased(Integer covArtifactOverMSBased) {
		this.covArtifactOverMSBased = covArtifactOverMSBased;
	}

	public Integer getNumberComprehensiveEdges() {
		return numberComprehensiveEdges;
	}

	public void setNumberComprehensiveEdges(Integer numberComprehensiveEdges) {
		this.numberComprehensiveEdges = numberComprehensiveEdges;
	}

	public Integer getNumberPreciseEdges() {
		return numberPreciseEdges;
	}

	public void setNumberPreciseEdges(Integer numberPreciseEdges) {
		this.numberPreciseEdges = numberPreciseEdges;
	}

	public Integer getNumberArtifactEdges() {
		return numberArtifactEdges;
	}

	public void setNumberArtifactEdges(Integer numberArtifactEdges) {
		this.numberArtifactEdges = numberArtifactEdges;
	}

	public Integer getNumberChunkEdges() {
		return numberChunkEdges;
	}

	public void setNumberChunkEdges(Integer numberChunkEdges) {
		this.numberChunkEdges = numberChunkEdges;
	}

	public Integer getNumberFileEdges() {
		return numberFileEdges;
	}

	public void setNumberFileEdges(Integer numberFileEdges) {
		this.numberFileEdges = numberFileEdges;
	}

	public Integer getNumberMSEdges() {
		return numberMSEdges;
	}

	public void setNumberMSEdges(Integer numberMSEdges) {
		this.numberMSEdges = numberMSEdges;
	}

}
