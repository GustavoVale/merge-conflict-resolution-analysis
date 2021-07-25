package commnet.model.beans;

import commnet.model.enums.EdgeSide;

public class DeveloperEdge {

	private Integer idDB;
	private Integer networkIdDB;
	private DeveloperNode devA;
	private DeveloperNode devB;
	private Integer weight = new Integer(0);
	private Integer edgeType;
	private EdgeSide edgeSide;

	public DeveloperEdge() {
		this(null, null, null, null, null, null, null);
	}

	public DeveloperEdge(Integer id) {
		this(id, null, null, null, null, null, null);
	}

	public DeveloperEdge(DeveloperNode devA, DeveloperNode devB, Integer edgeType, EdgeSide edgeSide,
			Integer weightEdge) {
		this(null, null, devA, devB, edgeType, edgeSide, weightEdge);
	}

	public DeveloperEdge(Integer idDB, Integer networkIdDB, DeveloperNode devA, DeveloperNode devB, Integer edgeType,
			EdgeSide edgeSide, Integer weight) {
		setIdDB(idDB);
		setNetworkIdDB(networkIdDB);
		setDevA(devA);
		setDevB(devB);
		setEdgeType(edgeType);
		setEdgeSide(edgeSide);
		setWeight(weight);
	}

	public Integer getIdDB() {
		return this.idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public Integer getNetworkID() {
		return this.networkIdDB;
	}

	public void setNetworkIdDB(Integer networkID) {
		this.networkIdDB = networkID;
	}

	public DeveloperNode getDevA() {
		return devA;
	}

	public void setDevA(DeveloperNode devA) {
		this.devA = devA;
	}

	public DeveloperNode getDevB() {
		return devB;
	}

	public void setDevB(DeveloperNode devB) {
		this.devB = devB;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public int getWeight() {
		return this.weight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((devA == null) ? 0 : devA.hashCode());
		result = prime * result + ((devB == null) ? 0 : devB.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeveloperEdge other = (DeveloperEdge) obj;
		if (devA == null) {
			if (other.devA != null)
				return false;
		}
		if (devB == null) {
			if (other.devB != null)
				return false;
		}
		if (!getEdgeSide().equals(other.getEdgeSide())) {
			return false;
		}
		if (!getEdgeType().equals(other.getEdgeType())) {
			return false;
		}

		// testing bidirectionality
		if ((devB.equals(other.devB) && devA.equals(other.devA))
				|| (devB.equals(other.devA) && devA.equals(other.devB))) {
			return true;
		}
		if ((devB.equals(other.devB) && !devA.equals(other.devA))
				|| (!devB.equals(other.devB) && devA.equals(other.devA))
				|| (!devB.equals(other.devB) && !devA.equals(other.devA))) {
			return false;
		}

		return true;
	}

	public boolean devsEqualIDs(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeveloperEdge other = (DeveloperEdge) obj;
		if (devA.getIdDB() == null) {
			if (other.devA.getIdDB() != null)
				return false;
		}
		if (devB.getIdDB() == null) {
			if (other.devB.getIdDB() != null)
				return false;
		}
		// testing bidirectionality
		if ((devB.equalIds(other.devB) && devA.equalIds(other.devA))
				|| (devB.equalIds(other.devA) && devA.equalIds(other.devB))) {
			return true;
		}
		if ((devB.equalIds(other.devB) && !devA.equalIds(other.devA))
				|| (!devB.equalIds(other.devB) && devA.equalIds(other.devA))
				|| (!devB.equalIds(other.devB) && !devA.equalIds(other.devA))) {
			return false;
		}

		return true;
	}

	public Integer getEdgeType() {
		return this.edgeType;
	}

	public void setEdgeType(Integer edgeType) {
		this.edgeType = edgeType;
	}

	public EdgeSide getEdgeSide() {
		return this.edgeSide;
	}

	public void setEdgeSide(EdgeSide edgeSide) {
		this.edgeSide = edgeSide;
	}

	public void incrementWeight() {
		this.weight++;

	}

}
