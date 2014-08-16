package it.unitn.tlsaf.otherfunc;

import it.unitn.tlsaf.ds.Actor;
import it.unitn.tlsaf.ds.InfoEnum;
import it.unitn.tlsaf.ds.RequirementElement;
import it.unitn.tlsaf.ds.RequirementGraph;
import it.unitn.tlsaf.ds.SecurityGoal;
import it.unitn.tlsaf.func.Inference;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.eclipse.jface.window.ApplicationWindow;

public class TryOut {

	public static void main(String[] args) throws Throwable {
		//		AppleScript.drawRequirementLink("BUSINESS", "none", "53642", "53643", "SharpArrow", "0", "none");
		//		AppleScript.drawIsolatedRequirementElement("Model", "BUSINESS", "Rectangle", "{200,200}", "{200,200}", "0", "sfasfasdf");
		//		AppleScript.drawRequirementElement(target, reference_id, direction)

//		ArrayList<Long> result = AppleScript.getSelectedGraph();
//		for (Long l: result){
//			System.out.println(l);
//		}
		
//		testDLV();
		
//		Inference.writeFile("dlv/teset.txt", "teste\n", false);
		
		
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
	
	static void testDLV() throws IOException{
		//String refine_rule = "dlv/dlv -silent -nofacts dlv/rules/cross_layer.rule dlv/models/req_business_model.dl dlv/models/req_application_model.dl";
		String refine_rule = "dlv/dlv -silent -nofacts "
				+ "dlv/rules/simplification_phy.rule "
				+ "dlv/rules/simplification_general.rule "
//				+ "dlv/rules/temp.txt";
				+ "dlv/models/req_physical_model.dl "
				+ "dlv/models/software_architecture_model.dl "
				+ "dlv/models/deployment_model.dl "
				+ "dlv/models/asset_model.dl "
				+ "dlv/models/actor_association_model.dl ";
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(refine_rule);

		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = null;

		//LinkedList<RequirementLink> new_links = new LinkedList<RequirementLink>();
		LinkedList<RequirementElement> refined_elems = new LinkedList<RequirementElement>();

//		System.out.println(input.readLine());
		
		while ((line = input.readLine()) != null) {
			String[] temp = line.split(", "); 
			for(String result : temp){
				System.out.println(result);
			}
		}
	}

}

