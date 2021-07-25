package commnet.model.beans;

public class DeveloperNode {

	private Integer idDB;
	private String name;
	private String email;

	public DeveloperNode() {
		this(null, null, null);
	}

	public DeveloperNode(String anEmail) {
		this(null, null, anEmail);
	}

	public DeveloperNode(Integer idDB, String anEmail) {
		this(idDB, null, anEmail);
	}

	public DeveloperNode(Integer id, String aName, String anEmail) {
		setIdDB(id);
		setName(aName);
		setEmail(anEmail);
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((idDB == null) ? 0 : idDB.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		DeveloperNode other = (DeveloperNode) obj;
		if (other.name == null) {
			return false;
		}
		if (name.equals(other.name)) {
			return true;
		}
		if (email == null && other.email == null) {
			return false;
		}
		if (email == "" && other.email == "") {
			return false;
		} else if (email != other.email)
			return false;

		return true;
	}
	
	public boolean equalIds(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeveloperNode other = (DeveloperNode) obj;
		if (other.idDB == null) {
			return false;
		}
		if (!idDB.equals(other.idDB)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return this.name + "(#" + this.idDB + "): " + this.email;
	}

}
