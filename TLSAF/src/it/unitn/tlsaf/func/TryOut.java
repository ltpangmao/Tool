package it.unitn.tlsaf.func;

import it.unitn.tlsaf.ds.Actor;
import it.unitn.tlsaf.ds.InfoEnum;
import it.unitn.tlsaf.ds.RequirementElement;
import it.unitn.tlsaf.ds.SecurityGoal;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class TryOut {

	public static void main(String[] args) throws Throwable {

//		String refine_rule = "dlv/dlv -silent -nofacts dlv/rules/sec_goal_ownership.rule dlv/models/temp_fact_app.dl dlv/models/asset_model.dl dlv/models/req_application_model.dl";

		//draw pdf figure for the corresponding graph
		//		String draw_graphviz = "graphviz/dot -Tpdf graphviz/sec_goal.gv -o graphviz/sec_goal.pdf";
		
		String inference_rule = "dlv/dlv -silent  "
				+ "dlv/rules/simplification_bus.rule "
				+ "dlv/rules/simplification_general.rule "
//				+ "dlv/rules/sec_goal_ownership.rule "
//				+ "dlv/models/software_architecture_model.dl "
				+ "dlv/models/business_process_model.dl "
				+ "dlv/models/asset_model.dl "
				+ "dlv/models/actor_association_model.dl "
//				+ "dlv/models/temp_app_fact.dl "
				+ "dlv/models/ex_req_business_model.dl ";
		
		inference_rule = "dlv/dlv -silent  -nofacts "
				+ "dlv/rules/refine_all.rule "
				+ "dlv/models/asset_model.dl "
				+ "dlv/models/security_model_business.dl "
				+ "dlv/models/req_business_model.dl ";
		
		
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(inference_rule);

		BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line = null;

		while ((line = input.readLine()) != null) {
//			System.out.println(line);
			
			line = line.substring(1, line.length() - 1);
			
			String[] result = line.split(", ");

			for (String s : result) {
				System.out.println(s);	
			}
		}

	}

}
