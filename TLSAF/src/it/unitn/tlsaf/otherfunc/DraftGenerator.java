package it.unitn.tlsaf.otherfunc;

//import it.unitn.tlsaf.func.AppleScript;
//import org.eclipse.swt.widgets.ProgressBar;
//import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
//import java.io.IOException;
//import org.eclipse.jface.dialogs.MessageDialog;
//import javax.script.ScriptException;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;


/**
 * @author litong30
 * A graph generator with GUI (ApplicationWindow)
 */
public class DraftGenerator extends ApplicationWindow {
	private Text fileAddress;
	private Text canvasText;
	private Text layerText;

	/**
	 * Create the application window.
	 */
	public DraftGenerator() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		
		Label draftLabel = new Label(container, SWT.NONE);
		draftLabel.setBounds(24, 33, 121, 14);
		draftLabel.setText("Choose the draft file");
		
		fileAddress = new Text(container, SWT.BORDER);
		fileAddress.setBounds(24, 57, 316, 19);
		
		Button btnChoose = new Button(container, SWT.NONE);
		btnChoose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedDir="";
				FileDialog directoryDialog = new FileDialog(getShell());
		        directoryDialog.setFilterPath(selectedDir);
		        directoryDialog.setText("Please select the draft to be processed");
		        
		        String dir = directoryDialog.open();
		        if(dir != null) {
		          fileAddress.setText(dir);
		          selectedDir = dir;
		        }
			}
		});
		btnChoose.setBounds(346, 53, 94, 28);
		btnChoose.setText("Choose");
		/*
		Button btnGenerate = new Button(container, SWT.NONE);
		btnGenerate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String file = fileAddress.getText();
				
				//calculate position
				int x=0,y=0;
				int length = 3000;
				String position = "{"+x+","+y+"}";
						
				
				BufferedReader input;
				try {
					input = new BufferedReader(new FileReader(file));
				} catch (FileNotFoundException e1) {
					MessageDialog.openInformation(container.getShell(), "Message", "File is not found!");
					return;
				}
				
				String line="";
				String tag="";
				String shape="";
				//Assume they are input correctly. Default value is assigned.
				String canvas = canvasText.getText();
				String layer = layerText.getText();
				
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
				} catch (IOException | ScriptException e1) {
					MessageDialog.openInformation(container.getShell(), "Message", "Graph generation fails!");
				}
				MessageDialog.openInformation(container.getShell(), "Message", "Successfully generate graphs!");
			}
		});
		btnGenerate.setBounds(346, 103, 94, 28);
		btnGenerate.setText("Generate");
		*/
		
		Label lblCanvas = new Label(container, SWT.NONE);
		lblCanvas.setBounds(24, 103, 59, 14);
		lblCanvas.setText("Canvas");
		
		canvasText = new Text(container, SWT.BORDER);
		canvasText.setText("Test");
		canvasText.setBounds(76, 103, 64, 19);
		
		Label lblLayer = new Label(container, SWT.NONE);
		lblLayer.setBounds(190, 103, 45, 14);
		lblLayer.setText("Layer");
		
		layerText = new Text(container, SWT.BORDER);
		layerText.setText("none");
		layerText.setBounds(240, 103, 64, 19);

		return container;
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager("menu");
		return menuManager;
	}

	/**
	 * Create the toolbar manager.
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);
		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			DraftGenerator window = new DraftGenerator();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("New Application");
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}
	
	
	
}

//class LongRunningOperation extends Thread {
//	  private Display display;
//
//	  private ProgressBar progressBar;
//
//	  public LongRunningOperation(Display display, ProgressBar progressBar) {
//	    this.display = display;
//	    this.progressBar = progressBar;
//	  }
//
//	  public void run() {
//	    for (int i = 0; i < 30; i++) {
//	      try {
//	        Thread.sleep(1000);
//	      } catch (InterruptedException e) {
//	      }
//	      display.asyncExec(new Runnable() {
//	        public void run() {
//	          if (progressBar.isDisposed())
//	            return;
//	          progressBar.setSelection(progressBar.getSelection() + 1);
//	        }
//	      });
//	    }
//	  }
//	}
