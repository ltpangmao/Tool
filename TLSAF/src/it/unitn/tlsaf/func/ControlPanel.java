package it.unitn.tlsaf.func;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import it.unitn.tlsaf.ds.Actor;
import it.unitn.tlsaf.ds.ActorAssociationGraph;
import it.unitn.tlsaf.ds.InfoEnum;
import it.unitn.tlsaf.ds.Element;
import it.unitn.tlsaf.ds.RequirementGraph;
import it.unitn.tlsaf.ds.RequirementElement;
import it.unitn.tlsaf.ds.RequirementLink;
import it.unitn.tlsaf.ds.SecurityGoal;

/**
 * Start point, choose desired operations
 * 
 * @author litong30
 */
public class ControlPanel {
	RequirementGraph req_bus_model;
	RequirementGraph req_app_model;
	RequirementGraph req_inf_model;
	
	ActorAssociationGraph actor_model;

	// Graph asset_model = new Graph(InfoEnum.ModelCategory.REQUIREMENT.name());
	// Graph bp_model = new Graph(InfoEnum.ModelCategory.REQUIREMENT.name());

	public ControlPanel() {
		super();
		req_bus_model = new RequirementGraph(InfoEnum.ModelCategory.REQUIREMENT.name(),
				InfoEnum.Layer.BUSINESS.name());
		req_app_model = new RequirementGraph(InfoEnum.ModelCategory.REQUIREMENT.name(),
				InfoEnum.Layer.APPLICATION.name());
		req_inf_model = new RequirementGraph(InfoEnum.ModelCategory.REQUIREMENT.name(),
				InfoEnum.Layer.PHYSICAL.name());
		
		actor_model = new ActorAssociationGraph(InfoEnum.ModelCategory.ACTOR.name());
	}

	public static void main(String args[]) throws Throwable {
		ControlPanel cp = new ControlPanel();

		Scanner reader = new Scanner(System.in);
		// get user input for a
		String command = "";
		do {
			System.out
					.println("\nEnter the number of the task to execute\n"
							+ "0) import selected requirement model from text (overwrite the previous one)\n"
							+ "		0a) business layer\n"
							+ "		0b) application layer\n"
							+ "		0c) physical layer\n"
							+ "1) import selected requirement model from canvas (overwrite the previous one)\n"
							+ "		1a) business layer\n"
							+ "		1b) application layer\n"
							+ "		1c) physical layer\n"
							+ "2) asset-based security goal refinement (one step refinement for leaf security goals)\n"
							+ "		2a) business layer\n"
							+ "		2b) application layer\n"
							+ "		2c) physical layer\n"
							+ "3) security attribute-based security goal refinement (one step refinement for leaf security goals)\n"
							+ "		3a) business layer\n"
							+ "		3b) application layer\n"
							+ "		3c) physical layer\n"
							+ "4) interval-based security goal refinement (one step refinement for leaf security goals)\n"
							+ "		4a) business layer\n"
							+ "		4b) application layer\n"
							+ "		4c) physical layer\n"
							+ "5) security goal simplification (identify critical ones among leaf security goals)\n"
							+ "		5a) business layer\n"
							+ "		5b) application layer\n"
							+ "		5c) physical layer\n"
							+ "6) security goal operationalization\n"
							+ "		6a) business layer\n"
							+ "		6b) application layer\n"
							+ "		6c) physical layer\n"
							+ "7) generate security alternatives\n"
							+ "		7a) business layer\n"
							+ "		7b) application layer\n"
							+ "		7c) physical layer\n"
							+ "8) cross layer security analysis\n"
							+ "		8a) business layer to application layer\n"
							+ "		8b) application layer to physical layer\n"
							+ "		8c) ...\n"  
							+ "9) generate all security goal alternatives\n"
							+ "		9a0) business layer\n"
							+ "		9b) application layer\n"
							+ "		9c) physical layer\n"
							+ "10) import actor association graph\n"
							+"0)exit\n");
			

			command = reader.nextLine();
			if (command.equals("0a")) {
				cp.req_bus_model= new RequirementGraph(cp.req_bus_model.getType(), cp.req_bus_model.getLayer());
				Inference.importReqModel(cp.req_bus_model, false);
			} else if (command.equals("0b")) {
				cp.req_app_model= new RequirementGraph(cp.req_app_model.getType(), cp.req_app_model.getLayer());
				Inference.importReqModel(cp.req_app_model, false);
			} else if (command.equals("0c")) {
				cp.req_inf_model= new RequirementGraph(cp.req_inf_model.getType(), cp.req_inf_model.getLayer());
				Inference.importReqModel(cp.req_inf_model, false);
			}
			
			if (command.equals("1a")) {
				cp.req_bus_model= new RequirementGraph(cp.req_bus_model.getType(), cp.req_bus_model.getLayer());
				Inference.importReqModel(cp.req_bus_model, true);
			} else if (command.equals("1b")) {
				cp.req_app_model= new RequirementGraph(cp.req_app_model.getType(), cp.req_app_model.getLayer());
				Inference.importReqModel(cp.req_app_model, true);
			} else if (command.equals("1c")) {
				cp.req_inf_model= new RequirementGraph(cp.req_inf_model.getType(), cp.req_inf_model.getLayer());
				Inference.importReqModel(cp.req_inf_model, true);
			}

			else if (command.equals("2a")) {
				Inference.securityGoalRefine(cp.req_bus_model, "asset");
			} else if (command.equals("2b")) {
				Inference.securityGoalRefine(cp.req_app_model, "asset");
			} else if (command.equals("2c")) {
				Inference.securityGoalRefine(cp.req_inf_model, "asset");
			}

			else if (command.equals("3a")) {
				Inference.securityGoalRefine(cp.req_bus_model, "attribute");
			} else if (command.equals("3b")) {
				Inference.securityGoalRefine(cp.req_app_model, "attribute");
			} else if (command.equals("3c")) {
				Inference.securityGoalRefine(cp.req_inf_model, "attribute");
			}

			else if (command.equals("4a")) {
				Inference.securityGoalRefine(cp.req_bus_model, "interval");
			} else if (command.equals("4b")) {
				Inference.securityGoalRefine(cp.req_app_model, "interval");
			} else if (command.equals("4c")) {
				Inference.securityGoalRefine(cp.req_inf_model, "interval");
			}

			//I need to have separate simplification methods for the fully refined security goal set.
			else if (command.equals("5a")) {
				Inference.securityGoalSimplification(cp.req_bus_model, cp.actor_model);
			} else if (command.equals("5b")) {
				Inference.securityGoalSimplification(cp.req_app_model, cp.actor_model);
			} else if (command.equals("5c")) {
				Inference.securityGoalSimplification(cp.req_inf_model, cp.actor_model);
			}

			else if (command.equals("6a")) {
				Inference.securityGoalOperationalization(cp.req_bus_model);
			} else if (command.equals("6b")) {
				Inference.securityGoalOperationalization(cp.req_app_model);
			} else if (command.equals("6c")) {
				Inference.securityGoalOperationalization(cp.req_inf_model);
			}

			else if (command.equals("7a")) {
				Inference.securityAlternativeSolutions(cp.req_bus_model);
			} else if (command.equals("7b")) {
				Inference.securityAlternativeSolutions(cp.req_app_model);
			} else if (command.equals("7c")) {
				Inference.securityAlternativeSolutions(cp.req_inf_model);
			}
			
			else if (command.equals("8a")) {
				Inference.securityBusToAppTransformation(cp.req_bus_model, cp.req_app_model);
			} else if (command.equals("8b")) {
//				Inference.securityAppToInfTransformation(cp.req_app_model, cp.req_inf_model);
			} else if (command.equals("8c")) {

			}
			
			else if (command.equals("9a0")) {
				Inference.exhaustiveSecurityGoalRefineAnalysis(cp.req_bus_model, cp.actor_model, 0);
			}else if (command.equals("9a1")) {
				Inference.exhaustiveSecurityGoalRefineAnalysis(cp.req_bus_model, cp.actor_model, 1);
			}else if (command.equals("9a2")) {
				Inference.exhaustiveSecurityGoalRefineAnalysis(cp.req_bus_model, cp.actor_model, 2);
			}
			
//			else if (command.equals("9b")) {
//				Inference.exhaustiveSecurityGoalRefineAnalysis(cp.req_app_model, cp.actor_model);
//			} else if (command.equals("9c")) {
//				Inference.exhaustiveSecurityGoalRefineAnalysis(cp.req_inf_model, cp.actor_model);
//			}
			
			else if(command.equals("10")){
				Inference.importActorModel(cp.actor_model);
			}
			
			// test methods
			else if(command.equals("show actor model")){
				Inference.showActorModel(cp.actor_model);
			}
			else if(command.equals("show req bus model")){
				Inference.showReqModel(cp.req_bus_model);
			}
			else if(command.equals("show req app model")){
				Inference.showReqModel(cp.req_app_model);
			}
			else if(command.equals("show req inf model")){
				Inference.showReqModel(cp.req_inf_model);
			}

		} while (!command.equals("0"));

	}

	static void print(Object s) {
		System.out.println(s);
	}
}
