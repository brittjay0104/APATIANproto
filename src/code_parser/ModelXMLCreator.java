package code_parser;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jdt.internal.compiler.lookup.MostSpecificExceptionMethodBinding;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ModelXMLCreator {
	
	public ModelXMLCreator(){
		
	}
	
	public static void main(String argv[]){ 
		
		  try {
			  	
			  ModelXMLCreator creator = new ModelXMLCreator();
			  
			  Document doc = creator.createXMLDoc();
		 
				
		 
				// write the content into xml file
			  TransformerFactory transformerFactory = TransformerFactory.newInstance();
			  Transformer transformer = transformerFactory.newTransformer();
			  DOMSource source = new DOMSource(doc);
			  StreamResult result = new StreamResult(new File("output.xml"));
		 
				// Output to console for testing
				// StreamResult result = new StreamResult(System.out);
		 
			  transformer.transform(source, result);
		 
			  System.out.println("File saved!");
		 
			} catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			} catch (TransformerException tfe) {
				tfe.printStackTrace();
			}
		}
	
	
	public Document createXMLDoc() throws ParserConfigurationException{
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("model");
		doc.appendChild(rootElement);
 
		// developer elements
		Element developer = doc.createElement("developer");
		rootElement.appendChild(developer);
 
		// set attribute to developer element
		developer.setAttribute("name", "Brittany Johnson");
		
		
		// concepts elements
		  Element concepts = doc.createElement("concepts");
		  developer.appendChild(concepts);
	 
			// concept elements
		  Element concept = doc.createElement("concept");
		  concepts.appendChild(concept);
			
		  concept.setAttribute("id", "Null Object/Object Dereferencing");
	 
			// null concept value elements
		  Element nullConcept = doc.createElement("null");
		  nullConcept.appendChild(doc.createTextNode("4"));
		  concept.appendChild(nullConcept);
	 
			// dereferencing concept value elements
		  Element derefConcept = doc.createElement("dereferencing");
		  derefConcept.appendChild(doc.createTextNode("3"));
		  concept.appendChild(derefConcept);
			
			
			// repositories element
		  Element repositories = doc.createElement("repositories");
		  developer.appendChild(repositories);
			
			// repository element
		  Element repository = doc.createElement("repository");
		  repositories.appendChild(repository);
			
		  repository.setAttribute("name", "dummy-repo2");
							
			// commits element
		  Element commits = doc.createElement("commits");
		  commits.appendChild(doc.createTextNode("7"));
		  repository.appendChild(commits);
		
		return doc;
	}
		
}
	
