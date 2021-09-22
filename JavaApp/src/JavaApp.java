import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class JavaApp {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        Path userProfilePath = Paths.get(String.format("%s\\%s", System.getenv("APPDATA"), "JetBrains"));
        List<String> appNames = Arrays.asList("IntelliJ", "PyCharm", "Rider", "WebStorm", "PhpStorm", "GoLand");

        String[] appDirectories = userProfilePath
                .toFile()
                .list((current, name) -> new File(current, name).isDirectory() && appNames.stream().anyMatch(name::contains));


        assert appDirectories != null;
        for (String appDirectory : appDirectories) {
            String appDirPath = String.format("%s\\%s", userProfilePath, appDirectory);

            // Delete eval folder with licence key
            String evalDirPath = String.format("%s\\%s", appDirPath, "eval");
            File[] keyFiles = new File(evalDirPath).listFiles();

            assert keyFiles != null;
            for (File keyFile : keyFiles) {
                Boolean deleteResult = keyFile.delete();
            }

            // Update options.xml
            String optionsFilePath = String.format("%s\\%s\\%s", appDirPath, "options", "other.xml");
            File optionsFile = new File(optionsFilePath);

            if (!optionsFile.exists()) {
                System.out.printf("%s not found.%n", optionsFilePath);
                return;
            }

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document document = dBuilder.parse(optionsFile);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/application/component[@name='PropertiesComponent']/property[contains(@name,'evlsprt')]");

            NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                node.getParentNode().removeChild(node);
            }

            try {
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer t = tf.newTransformer();
                t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                // send DOM to file
                t.transform(new DOMSource(document),
                        new StreamResult(new FileOutputStream(optionsFilePath)));

            } catch (TransformerException | IOException te) {
                System.out.println(te.getMessage());
            }
        }

        // Delete registry key
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "reg delete \"HKEY_CURRENT_USER\\Software\\JavaSoft\" /f"});
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
