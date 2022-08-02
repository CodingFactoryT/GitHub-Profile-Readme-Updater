import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Main {
	public static final String USER = "CodingFactoryT";
	static String authToken = ""; //insert the Authorisation token here
	static final Gson GSON = new Gson();
	static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
	static final File tokenFile = new File("token.txt");
	
	public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JTextArea authDescriptionField = new JTextArea("Do you want to change the existing Authentication Token?\nIf yes, type it in the Text Field\nIf no, let the Text Field empty");
		authDescriptionField.setEditable(false);
		JTextField authInputField = new JTextField();
		
		panel.add(authDescriptionField);
		panel.add(authInputField);
		
		JOptionPane.showMessageDialog(null, panel);
		
		String input = authInputField.getText();
		if(!input.isEmpty()) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(tokenFile));
			bw.write(input);
			bw.close();
		}
		
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(tokenFile));
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, "File cannot be read!");
			System.exit(-1);
		}
		authToken = br.readLine();
		br.close();
		
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
				.header("Authorization", "token " + authToken)
				.build();
		
		if(sendRequest(uploadContentRequest).body().contains("\"message\":\"Bad credentials\"")) {
			JOptionPane.showMessageDialog(null, "Upload didn´t work: Bad Credentials!", "Error", JOptionPane.ERROR_MESSAGE);
		}else {
			JOptionPane.showMessageDialog(null, "Upload successfull", "", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public static HttpResponse<String> sendRequest(HttpRequest httpRequest) throws IOException, InterruptedException {
		return HTTP_CLIENT.send(httpRequest, BodyHandlers.ofString());
	}

}
