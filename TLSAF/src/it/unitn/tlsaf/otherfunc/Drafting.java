package it.unitn.tlsaf.otherfunc;

import it.unitn.tlsaf.ds.InfoEnum;
import it.unitn.tlsaf.func.AppleScript;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;


/**
 * @author litong30
 * A graph generator without GUI
 * The draft address is fixed at the root directory of the project
 */
public class Drafting {

	static public void main(String args[]){
		// development version
//		String file = "drafting.txt";
		// deployment version
		String file = System.getProperty("user.dir")+"/drafting.txt";
//		String file = "/Users/litong30/Desktop/drafting.txt";
//		System.out.println(file);
		
		//calculate position
		int x=0,y=0;
		int length = 3000;
		String position = "{"+x+","+y+"}";
				
		
		BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e1) {
			System.out.println("No files found!");
			return;
		}
		
		String line="";
		String tag="";
		String shape="";
		//Assume they are input correctly. Default value is assigned.
		String canvas = "Test";
		String layer = "none";
		
		try {
			while ((line = input.readLine()) != null) {
				if(line.startsWith("%")){
					//type declaration
					tag = line.substring(1);
					continue;
				}
				else if(line.equals("")){
					//skip empty lines
					continue;
				}
				else{
					//draw elements
					shape = InfoEnum.reverse_req_elem_type_map.get(tag);
					AppleScript.drawArbitraryRequirementElement(canvas, layer, shape, InfoEnum.NORMAL_SIZE, position, "0", line);
					//adjust distance
					if(x<3000){
						x+=200;
					}
					else{
						x=0;
						y+=150;
					}
					position = "{"+x+","+y+"}";
				}
			}
			System.out.println("Graph generation successes!");
		}
		catch (Exception e){
			System.out.println("Graph generation fails!");
		}
	}
}
