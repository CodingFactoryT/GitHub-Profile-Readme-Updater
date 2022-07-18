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
	static final Gson GSON = new Gson(); 
	static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException{
		Request readmeGetRequest = requestProfileReadme();
		String encodedContent = readmeGetRequest.content.replaceAll("\n", "");
		String fileContent = new String(Base64.getDecoder().decode(encodedContent.getBytes()));
		String fileSha = readmeGetRequest.sha;
		fileContent = fileContent.substring(0, fileContent.indexOf("|Date      |Repository Name"));
		fileContent += formatTable(getAllRepos());
		
		uploadContent(fileContent, fileSha);
	}
	
	public static Request requestProfileReadme() throws URISyntaxException, IOException, InterruptedException {
		HttpRequest getFileContentRequest = HttpRequest.newBuilder()
				.uri(new URI("https://api.github.com/repos/" + USER  + "/"  + USER + "/readme"))
				.GET()
				.build();
		
		return GSON.fromJson(sendRequest(getFileContentRequest).body(), Request.class);
	}
	
	public static Repository[] getAllRepos() throws URISyntaxException, IOException, InterruptedException {
		HttpRequest getAllReposRequest = HttpRequest.newBuilder()
				.uri(new URI("https://api.github.com/users/" + USER + "/repos"))
				.GET()
				.build();
		
		return GSON.fromJson(sendRequest(getAllReposRequest).body(), Repository[].class);
	}
	
	public static String formatTable(Repository[] repositories) throws JsonSyntaxException, URISyntaxException, IOException, InterruptedException {
		String tableContent = "|Date      |Repository Name" + " ".repeat(85) + "|Repository Number|\n"
				 			+ "|----------|" + "-".repeat(100) + "|:---------------:|\n";
		
		Arrays.sort(repositories);
		
		for(int i = 0; i < repositories.length; i++) {
			repositories[i].formatCreationDate();	
			repositories[i].setRepoNumber(i+1);
			tableContent += repositories[i].formatTableRow();
		}
		return tableContent;
	}
	
	public static void uploadContent(String fileContent, String fileSha) throws URISyntaxException, IOException, InterruptedException {
		Request putRequest = new Request();
		putRequest.setMessage("Updated table of repository creation dates");
		putRequest.setContent(Base64.getEncoder().encodeToString(fileContent.getBytes()));
		putRequest.setSha(fileSha);
		
		String json = GSON.toJson(putRequest);
		HttpRequest uploadContentRequest = HttpRequest.newBuilder()
				.uri(new URI("https://api.github.com/repos/" + USER  + "/"  + USER + "/contents/README.md"))
				.PUT(BodyPublishers.ofString(json))
				.header("Authorization", "token " + AUTH_TOKEN)
				.build();
		
		System.out.println(sendRequest(uploadContentRequest).body());
	}
	
	public static HttpResponse<String> sendRequest(HttpRequest httpRequest) throws IOException, InterruptedException {
		return HTTP_CLIENT.send(httpRequest, BodyHandlers.ofString());
	}

}
