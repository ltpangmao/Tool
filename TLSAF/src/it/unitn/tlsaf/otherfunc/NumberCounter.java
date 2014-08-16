package it.unitn.tlsaf.otherfunc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class NumberCounter {

	public static void main(String args[]) {
		int line_number = totalLineNumberCounter(new File(
				System.getProperty("user.dir") + "/src"));
		System.out.println("TLSAF project has "+line_number+" lines so far.");

		line_number = totalFileNumberCounter(new File(
				"/Users/litong30/Dropbox/readings"));
		System.out.println("The reading folder totally has "+line_number+" files so far.");
		
		line_number = totalLineNumberCounter(new File(
				"/Users/litong30/research/Trento/Workspace/AuthorDiagram/src"));
		System.out.println("AuthorDiagram project has "+line_number+" lines so far.");
	}

	public static int totalFileNumberCounter(File node) {
		int file_number = 0;
		// System.out.println(node.getAbsoluteFile());

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				file_number += totalFileNumberCounter(new File(node, filename));
			}
		} else {
			file_number++;
		}
		return file_number;

	}

	public static int totalLineNumberCounter(File node) {
		int line_number = 0;
		// System.out.println(node.getAbsoluteFile());

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				line_number += totalLineNumberCounter(new File(node, filename));
			}
		} else {
			try {
				FileReader fr = new FileReader(node);
				LineNumberReader lnr = new LineNumberReader(fr);
				int linenumber = 0;
				while (lnr.readLine() != null) {
					linenumber++;
				}
				lnr.close();

				line_number = linenumber;

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return line_number;

	}

}
