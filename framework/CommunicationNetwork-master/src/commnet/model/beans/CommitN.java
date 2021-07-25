package commnet.model.beans;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import gitwrapper.repo.Commit;

public class CommitN {

	private Integer idDB;
	private Integer projectIdDB;
	private Integer contIdDB;
	private Integer committerIdDB;
	private String hash;
	private Date commitDate;
	private Timestamp fullCommitDate;

	public CommitN() {

	}

	public CommitN(Integer idDB, Integer projectID, Integer contributorID, Integer committerID, String commitHash,
			OffsetDateTime commitDate) {
		setIdDB(idDB);
		setProjectIdDB(projectID);
		setContIdDB(contributorID);
		setCommitterIdDB(committerID);
		setHash(commitHash);
		convertCommitDate(commitDate);
	}

	public CommitN(Integer idDB) {
		this(idDB, null, null, null, null, null, null);
	}

	public CommitN(Integer idDB, Integer projectID, Integer contributorID, Integer committerID, String commitHash,
			Date commitDate) {
		this(idDB, projectID, contributorID, committerID, commitHash, null, null);
	}

	public CommitN(Integer idDB, Integer projectID, Integer contributorID, Integer committerID, String commitHash,
			Date commitDate, Timestamp fullCommitDate) {
		setIdDB(idDB);
		setProjectIdDB(projectID);
		setContIdDB(contributorID);
		setCommitterIdDB(committerID);
		setHash(commitHash);
		setCommitDate(commitDate);
		setFullCommitDate(fullCommitDate);
	}

	public Integer getIdDB() {
		return idDB;
	}

	public void setIdDB(Integer idDB) {
		this.idDB = idDB;
	}

	public Integer getProjectIdDB() {
		return projectIdDB;
	}

	public void setProjectIdDB(Integer projectIdDB) {
		this.projectIdDB = projectIdDB;
	}

	public Integer getContIdDB() {
		return contIdDB;
	}

	public void setContIdDB(Integer contIdDB) {
		this.contIdDB = contIdDB;
	}

	public Integer getCommitterIdDB() {
		return committerIdDB;
	}

	public void setCommitterIdDB(Integer committerIdDB) {
		this.committerIdDB = committerIdDB;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Date getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(Date commitDate) {
		this.commitDate = commitDate;
	}

	public Timestamp getFullCommitDate() {
		return fullCommitDate;
	}

	public void setFullCommitDate(Timestamp fullCommitDate) {
		this.fullCommitDate = fullCommitDate;
	}

	public void setCommitDate(Commit commit) {
		if (commit != null && commit.getAuthorTime() != null) {
			OffsetDateTime aux = commit.getAuthorTime();
			LocalDateTime dateToConvert = aux.toLocalDateTime();
			java.util.Date convertToDate = Date.from(
					dateToConvert.atOffset(aux.getOffset()).atZoneSameInstant(ZoneId.systemDefault()).toInstant());
			this.commitDate = new Date(convertToDate.getTime());
		}
	}

	public void convertCommitDate(OffsetDateTime date) {
		LocalDateTime dateToConvert = date.toLocalDateTime();
		java.util.Date convertToDate = Date
				.from(dateToConvert.atOffset(date.getOffset()).atZoneSameInstant(ZoneId.systemDefault()).toInstant());
		this.commitDate = new Date(convertToDate.getTime());
	}

	public void updateCommitDate() {
		long timestamp = this.getFullCommitDate().getTime();
		java.sql.Date sqlDate = new java.sql.Date(timestamp);
		setCommitDate(sqlDate);
	}

	public List<String> getTimeFromGMTString() throws RuntimeException {
		@SuppressWarnings("deprecation")
		String dateInGMTString = getCommitDate().toGMTString();
		dateInGMTString = dateInGMTString.replace(" GMT", "");
		String[] parts = dateInGMTString.split(" ");
		String time = parts[3];

		String[] timeParts = time.split(":");
		List<String> newStringList = new ArrayList<String>();
		newStringList.add(timeParts[0]);
		newStringList.add(timeParts[1]);
		newStringList.add(timeParts[2]);

		return newStringList;
	}

}
