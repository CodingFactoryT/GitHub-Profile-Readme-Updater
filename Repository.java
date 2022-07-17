
public class Repository implements Comparable<Repository>{
	private String name;
	private String created_at;
	private int repoNumber = 0;
	
	public int getRepoNumber() {
		return repoNumber;
	}
	public void setRepoNumber(int repoNumber) {
		this.repoNumber = repoNumber;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUpdated_at() {
		return created_at;
	}
	public void setUpdated_at(String updated_at) {
		this.created_at = updated_at;
	}
	public int compareTo(Repository o) {
		return this.created_at.compareTo(o.created_at);
	}
	public void formatCreationDate() {
		this.created_at = created_at.substring(8,10) + "." + created_at.substring(5,7) + "." + created_at.substring(0,4);
	}
	public String formatTableRow() {
		final int maxRepoNameLenght = 100;
		final int maxRepoNumberLenght = 15;
		return "|" + created_at + "|"
			   + this.name + " ".repeat(maxRepoNameLenght - name.length()) + "|`"
			   + this.repoNumber + "`" + " ".repeat(maxRepoNumberLenght - String.valueOf(this.repoNumber).length()) + "|\n";
	}
}
