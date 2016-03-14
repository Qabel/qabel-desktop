package de.qabel.desktop.ui.about;

import de.qabel.desktop.ui.AbstractController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AboutController extends AbstractController implements Initializable {
	private static final String LICENSE_DEPENDENCY_MAPPING_LOCATION = "/license-dependency.xml";
	private static final String DEPENDENCY_LICENSE_MAPPING_LOCATION = "/dependency-license.xml";
	private static final ExecutorService executor = Executors.newCachedThreadPool();

	@FXML
	Pane linkContainer;

	private Map<String, String> jarNames = new HashMap<>();
	private ResourceBundle resourceBundle;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resourceBundle = resources;
		try {
			addDependencyLicenses();
			activateLinks();
		} catch (Exception e) {
			alert("failed to load about contents: " + e.getMessage(), e);
		}
	}

	private void addDependencyLicenses() throws ParserConfigurationException, IOException, SAXException {
		indexJarNames();
		addLicenses();
	}

	private void addLicenses() throws ParserConfigurationException, IOException, SAXException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(getClass().getResourceAsStream(LICENSE_DEPENDENCY_MAPPING_LOCATION));
		doc.getDocumentElement().normalize();

		NodeList licenses = doc.getElementsByTagName("license");
		for (int i = 0; i < licenses.getLength(); i++) {
			org.w3c.dom.Node license = licenses.item(i);
			String licenseName = license.getAttributes().getNamedItem("name").getNodeValue();
			String licenseLink = license.getAttributes().getNamedItem("url").getNodeValue();
			List<String> dependencies = new LinkedList<>();


			NodeList childNodes = license.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++) {
				org.w3c.dom.Node child = childNodes.item(j);
				if (!child.getNodeName().equals("dependency")) {
					continue;
				}

				String jarName = child.getTextContent();
				String dependencyName = jarName;
				if (jarNames.containsKey(jarName)) {
					dependencyName = jarNames.get(jarName);
				}
				dependencies.add(dependencyName);
			}

			String title = getString(resourceBundle, "licensedComponents", licenseName);
			linkContainer.getChildren().add(createLabeledLink(title, licenseLink, dependencies));
		}
	}

	private void indexJarNames() throws ParserConfigurationException, IOException, SAXException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(getClass().getResourceAsStream(DEPENDENCY_LICENSE_MAPPING_LOCATION));
		doc.getDocumentElement().normalize();

		NodeList deps = doc.getElementsByTagName("dependency");
		for (int i = 0; i < deps.getLength(); i++) {
			org.w3c.dom.Node dep = deps.item(i);
			NodeList childNodes = dep.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++) {
				org.w3c.dom.Node child = childNodes.item(j);
				if (child.getNodeName().equals("file")) {
					jarNames.put(child.getTextContent(), dep.getAttributes().getNamedItem("name").getNodeValue());
				}
			}
		}
	}

	private VBox createLabeledLink(String labelText, String url, List<String> comments) {
		VBox container = new VBox();
		container.getStyleClass().add("labeled-link");
		Label label = new Label(labelText);
		Hyperlink hyperlink = new Hyperlink(url);
		container.getChildren().add(label);
		container.getChildren().add(hyperlink);
		VBox commentContainer = new VBox();
		container.getChildren().add(commentContainer);

		for (String comment : comments) {
			Label commentLabel = new Label(comment);
			commentContainer.getChildren().add(commentLabel);
		}

		return container;
	}

	private void activateLinks() {
		for (Node node : linkContainer.getChildrenUnmodifiable()) {

			if (!node.getStyleClass().contains("labeled-link") || !(node instanceof Parent)) {
				continue;
			}

			Parent linkContainer = (Parent)node;
			for (Node potentialLink : linkContainer.getChildrenUnmodifiable()) {
				if (!(potentialLink instanceof Hyperlink)) {
					continue;
				}

				Hyperlink link = (Hyperlink) potentialLink;
				link.setOnAction(event -> {
					executor.submit(() -> {
						try {
							Desktop.getDesktop().browse(new URI(link.getText()));
						} catch (Exception e) {
							alert("failed to open link: " + e.getMessage(), e);
						}
					});
				});
			}
		}
	}
}
