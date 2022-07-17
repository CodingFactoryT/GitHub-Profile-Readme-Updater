import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.Base64;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Main {
	public static final String USER = "CodingFactoryT";
	static final String AUTH_TOKEN = ""; //insert the Authorisation token here

	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException{
		String fileContent = getProfileReadmeFileContent();
		fileContent = fileContent.substring(0, fileContent.indexOf("|Date      |Repository "));
		fileContent += getTableContent();
		
		writeProfileReadmeContentToWebsite(fileContent);
	}
	
	public static String getProfileReadmeFileContent() throws URISyntaxException, IOException, InterruptedException {
		HttpRequest getProfileReadmeFileContentRequest = HttpRequest.newBuilder()
				.uri(new URI("https://raw.githubusercontent.com/" + USER + "/" + USER + "/master/README.md"))
				.GET()
				.build();
		
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpResponse<String> getProfileReadmeFileContentResponse = httpClient.send(getProfileReadmeFileContentRequest, BodyHandlers.ofString());
		return getProfileReadmeFileContentResponse.body();
	}
	
	public static String getAllRepos() throws URISyntaxException, IOException, InterruptedException {
		HttpRequest getAllReposRequest = HttpRequest.newBuilder()
				.uri(new URI("https://api.github.com/users/" + USER + "/repos"))
				.GET()
				.build();
		
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpResponse<String> getAllReposResponse = httpClient.send(getAllReposRequest, BodyHandlers.ofString());
		return getAllReposResponse.body();
	}
	
	public static String getTableContent() throws JsonSyntaxException, URISyntaxException, IOException, InterruptedException {
		String tableContent = "|Date      |Repository Name" + " ".repeat(85) + "|Repository Number|\n"
				 			+ "|----------|" + "-".repeat(100) + "|:---------------:|\n";

		Gson gson = new Gson();
		Repository[] repositories = gson.fromJson(getAllRepos(), Repository[].class);
		
		Arrays.sort(repositories);
		
		for(int i = 0; i < repositories.length; i++) {
			repositories[i].formatCreationDate();	
			repositories[i].setRepoNumber(i+1);
			tableContent += repositories[i].formatTableRow();
		}
		return tableContent + "Test";
	}
	
	public static void writeProfileReadmeContentToWebsite(String fileContent) throws URISyntaxException, IOException, InterruptedException {
		PutRequest putRequest = new PutRequest();
		putRequest.setMessage("Updated table of repository creation dates");
		putRequest.setContent(Base64.getEncoder().encodeToString(fileContent.getBytes()));
		putRequest.setSha(getFileSha());
		
		Gson gson = new Gson();
		String json = gson.toJson(putRequest);
		HttpRequest writeProfileReadmeContentToWebsiteRequest = HttpRequest.newBuilder()
				.uri(new URI("https://api.github.com/repos/" + USER  + "/"  + USER + "/contents/README.md"))
				.PUT(BodyPublishers.ofString(json))
				.header("Authorization", "token " + AUTH_TOKEN)
				.build();
		
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpResponse<String> writeProfileReadmeContentToWebsiteResponse = httpClient.send(writeProfileReadmeContentToWebsiteRequest, BodyHandlers.ofString());
		System.out.println(writeProfileReadmeContentToWebsiteResponse.body());
	}
	
	public static String getFileSha() throws IOException, InterruptedException, URISyntaxException {
		HttpRequest getFileShaRequest = HttpRequest.newBuilder()
				.uri(new URI("https://api.github.com/repos/" + USER  + "/"  + USER + "/contents/README.md"))
				.GET()
				.build();
		
		HttpClient httpClient = HttpClient.newHttpClient();
		HttpResponse<String> getFileShaResponse = httpClient.send(getFileShaRequest, BodyHandlers.ofString());
		
		Gson gson = new Gson();
		return gson.fromJson(getFileShaResponse.body(), PutRequest.class).sha;
	}

}
