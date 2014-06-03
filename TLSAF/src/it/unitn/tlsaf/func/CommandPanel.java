package it.unitn.tlsaf.func;

import java.awt.Dimension;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unitn.tlsaf.ds.InfoEnum;
import it.unitn.tlsaf.ds.RequirementGraph;

import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

import javax.script.ScriptException;

import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Label;

public class CommandPanel extends ApplicationWindow {
	// static logger that is used over the whole project
	public static Logger logger;
	static public void setup(){
		// Get the global logger to configure it
	    logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	    logger.setLevel(Level.WARNING);
//	    logger.setLevel(Level.INFO);
	}
	
	
	private ModelSet ms;

	/**
	 * Create the application window,
	 */
	public CommandPanel() {
		super(null);
		createActions();
		addCoolBar(SWT.FLAT);
		addMenuBar();
		addStatusLine();
		ms = new ModelSet();
	}
	

	public ModelSet getMs() {
		return ms;
	}

	public void setMs(ModelSet ms) {
		this.ms = ms;
	}



	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		
		//Layer setting
		final Group grpLayer = new Group(container, SWT.NONE);
		grpLayer.setText("Layer");
		grpLayer.setBounds(10, 21, 196, 88);
		
		Button btnBusiness = new Button(grpLayer, SWT.RADIO);
		btnBusiness.setBounds(10, 33, 69, 18);
		btnBusiness.setText("Business");
		btnBusiness.setSelection(true);
		
		Button btnApplication = new Button(grpLayer, SWT.RADIO);
		btnApplication.setBounds(96, 10, 91, 18);
		btnApplication.setText("Application");
		
		Button btnPhysical = new Button(grpLayer, SWT.RADIO);
		btnPhysical.setBounds(96, 33, 82, 18);
		btnPhysical.setText("Physical");
		
		Button btnAllLayer = new Button(grpLayer, SWT.RADIO);
		btnAllLayer.setBounds(10, 10, 91, 18);
		btnAllLayer.setText("All layers");
//		btnAllLayer.setSelection(true);
		
		final Group grpObjects = new Group(container, SWT.NONE);
		grpObjects.setText("Object");
		grpObjects.setBounds(236, 21, 140, 88);
		
		Button btnSelectedElements_1 = new Button(grpObjects, SWT.RADIO);
		btnSelectedElements_1.setBounds(10, 34, 109, 18);
		btnSelectedElements_1.setText("Selected models");
		
		Button btnAllModel = new Button(grpObjects, SWT.RADIO);
		btnAllModel.setBounds(10, 10, 86, 18);
		btnAllModel.setText("All models");
		btnAllModel.setSelection(true);
		
		//Auxiliary functions
		Button btnDeleteAll = new Button(container, SWT.NONE);
		btnDeleteAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ms.req_bus_model = new RequirementGraph(ms.req_bus_model.getType(), ms.req_bus_model.getLayer());
				ms.req_app_model = new RequirementGraph(ms.req_app_model.getType(), ms.req_app_model.getLayer());
				ms.req_phy_model = new RequirementGraph(ms.req_phy_model.getType(), ms.req_phy_model.getLayer());
				MessageDialog.openInformation(container.getShell(), "Message", "Delete all models!");
			}
		});
		btnDeleteAll.setBounds(428, 32, 94, 26);
		btnDeleteAll.setText("Delete all");

		Button btnPrintModel = new Button(container, SWT.NONE);
		btnPrintModel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String layer_choice = getCommand(grpLayer);
				if(layer_choice.equals(InfoEnum.Layer.ALL.name())){
					ms.req_bus_model.printModel();
					ms.req_app_model.printModel();
					ms.req_phy_model.printModel();
				}
				else if(layer_choice.equals(InfoEnum.Layer.BUSINESS.name())){
					ms.req_bus_model.printModel();
				}
				else if(layer_choice.equals(InfoEnum.Layer.APPLICATION.name())){
					ms.req_app_model.printModel();
				}
				else if(layer_choice.equals(InfoEnum.Layer.PHYSICAL.name())){
					ms.req_phy_model.printModel();
				}
				else {
					CommandPanel.logger.severe("Layer selection error!");
				}
			}
		});
		btnPrintModel.setBounds(428, 64, 94, 26);
		btnPrintModel.setText("Print model");
		
		//import function
		final Group grpImport = new Group(container, SWT.NONE);
		grpImport.setText("Import Source");
		grpImport.setBounds(10, 129, 151, 88);
		
		Button btnSelectedElements = new Button(grpImport, SWT.RADIO);
		btnSelectedElements.setBounds(10, 10, 132, 18);
		btnSelectedElements.setText("Selected elements");
		btnSelectedElements.setSelection(true);
		
		Button btnFromFiles = new Button(grpImport, SWT.RADIO);
		btnFromFiles.setBounds(10, 33, 91, 18);
		btnFromFiles.setText("From files");
		
		final Group grpModel = new Group(container, SWT.NONE);
		grpModel.setText("Model Type");
		grpModel.setBounds(167, 129, 168, 86);
		
		Button btnRequirementModel = new Button(grpModel, SWT.RADIO);
		btnRequirementModel.setBounds(10, 10, 134, 18);
		btnRequirementModel.setText("Requirement model");
		btnRequirementModel.setSelection(true);
		
		Button btnTrustModel = new Button(grpModel, SWT.RADIO);
		btnTrustModel.setBounds(10, 33, 91, 18);
		btnTrustModel.setText("Trust model");
		
		final Button btnImport = new Button(container, SWT.NONE);
		btnImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//for import, we don't distinguish layers,
				//String layer = getLayer(grpLayer);
				String model = getCommand(grpModel);
				String command = getCommand(grpImport);
				Boolean canvas=null;
				if (command.equals(InfoEnum.Commands.IMP_SELECTION.name())){
					canvas = true;
				}
				else if (command.equals(InfoEnum.Commands.IMP_FILE.name())){
					canvas = false;
				}
				else {
					logger.warning("Import command error!");
				}
				
				try {
					if (model.equals(InfoEnum.ModelCategory.REQUIREMENT.name())) {
						Inference.importReqModel(ms, canvas);
						//TODO: customize the size of the dialog
						MessageDialog.openInformation(container.getShell(), "Message",
								"Finish importing requirement models!");
					}else if(model.equals(InfoEnum.ModelCategory.ACTOR.name())){
						Inference.importActorModel(ms.actor_model, canvas);
						//TODO: customize the size of the dialog
						MessageDialog.openInformation(container.getShell(), "Message", "Finish importing trust models!");
					}
					else {
						logger.warning("Command error!");
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ScriptException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnImport.setBounds(428, 172, 94, 26);
		btnImport.setText("Import");
		
		//Refinement function
		final Group grpRefinementMode = new Group(container, SWT.NONE);
		grpRefinementMode.setText("Refinement Mode");
		grpRefinementMode.setBounds(156, 223, 118, 98);
		
		Button btnOnestep = new Button(grpRefinementMode, SWT.RADIO);
		btnOnestep.setBounds(10, 10, 91, 18);
		btnOnestep.setText("One-step");
		btnOnestep.setSelection(true);
		
		Button btnExhaustive = new Button(grpRefinementMode, SWT.RADIO);
		btnExhaustive.setBounds(10, 34, 91, 18);
		btnExhaustive.setText("Exhaustive");
		
		final Group grpRefinementDimension = new Group(container, SWT.NONE);
		grpRefinementDimension.setText("Refinement Dimension");
		grpRefinementDimension.setBounds(10, 223, 140, 98);
		
		Button btnAttribute = new Button(grpRefinementDimension, SWT.RADIO);
		btnAttribute.setBounds(10, 10, 91, 18);
		btnAttribute.setText("Attribute");
		btnAttribute.setSelection(true);
		
		Button btnAsset = new Button(grpRefinementDimension, SWT.RADIO);
		btnAsset.setBounds(10, 34, 91, 18);
		btnAsset.setText("Asset");
		
		Button btnInterval = new Button(grpRefinementDimension, SWT.RADIO);
		btnInterval.setBounds(10, 58, 91, 18);
		btnInterval.setText("Interval");
		
		final Group grpVisualization = new Group(container, SWT.NONE);
		grpVisualization.setBounds(280, 221, 131, 98);
		grpVisualization.setText("Visualization");
//		grpVisualization.setVisible(false);

		Button btnOmnigraffle = new Button(grpVisualization, SWT.RADIO);
		btnOmnigraffle.setBounds(10, 10, 91, 18);
		btnOmnigraffle.setText("OmniGraffle");
		btnOmnigraffle.setSelection(true);

		Button btnGraphviz = new Button(grpVisualization, SWT.RADIO);
		btnGraphviz.setBounds(10, 34, 91, 18);
		btnGraphviz.setText("Graphviz");
		
		
		Button btnRefine = new Button(container, SWT.NONE);
		btnRefine.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String layer_choice = getCommand(grpLayer);
				String mode_choice = getCommand(grpRefinementMode);
				String visualization = getCommand(grpVisualization);
				String object_choice = getCommand(grpObjects);
				//TODO: this dimension choice is defined as a standard command, we simply use its lower-case content here
				String dimension_choice = getCommand(grpRefinementDimension);
				try {
					if (mode_choice.equals(InfoEnum.Commands.REF_ALL_ONE_STEP.name())) {
						if(layer_choice.equals(InfoEnum.Layer.ALL.name())){
							Inference.securityGoalRefine(ms.req_bus_model, dimension_choice, Integer.valueOf(object_choice));
							Inference.securityGoalRefine(ms.req_app_model, dimension_choice, Integer.valueOf(object_choice));
							Inference.securityGoalRefine(ms.req_phy_model, dimension_choice, Integer.valueOf(object_choice));
						}
						else if(layer_choice.equals(InfoEnum.Layer.BUSINESS.name())){
							Inference.securityGoalRefine(ms.req_bus_model, dimension_choice, Integer.valueOf(object_choice));
						}
						else if(layer_choice.equals(InfoEnum.Layer.APPLICATION.name())){
							Inference.securityGoalRefine(ms.req_app_model, dimension_choice, Integer.valueOf(object_choice));
						}
						else if(layer_choice.equals(InfoEnum.Layer.PHYSICAL.name())){
							Inference.securityGoalRefine(ms.req_phy_model, dimension_choice, Integer.valueOf(object_choice));
						}
						else {
							CommandPanel.logger.severe("Layer selection error!");
						}
						//TODO: customize the size of the dialog
						MessageDialog.openInformation(container.getShell(), "Message", "Finish one-step refinement!");
					}
					else if(mode_choice.equals(InfoEnum.Commands.REF_ALL_EXHAUSTIVE.name())){
						if(layer_choice.equals(InfoEnum.Layer.ALL.name())){
							Inference.exhaustiveSecurityGoalRefineAnalysis(ms.req_bus_model, ms.actor_model, Integer.valueOf(visualization), Integer.valueOf(object_choice));
							Inference.exhaustiveSecurityGoalRefineAnalysis(ms.req_app_model, ms.actor_model, Integer.valueOf(visualization), Integer.valueOf(object_choice));
							Inference.exhaustiveSecurityGoalRefineAnalysis(ms.req_phy_model, ms.actor_model, Integer.valueOf(visualization), Integer.valueOf(object_choice));
						}
						else if(layer_choice.equals(InfoEnum.Layer.BUSINESS.name())){
							Inference.exhaustiveSecurityGoalRefineAnalysis(ms.req_bus_model, ms.actor_model, Integer.valueOf(visualization), Integer.valueOf(object_choice));
						}
						else if(layer_choice.equals(InfoEnum.Layer.APPLICATION.name())){
							Inference.exhaustiveSecurityGoalRefineAnalysis(ms.req_app_model, ms.actor_model, Integer.valueOf(visualization), Integer.valueOf(object_choice));
						}
						else if(layer_choice.equals(InfoEnum.Layer.PHYSICAL.name())){
							Inference.exhaustiveSecurityGoalRefineAnalysis(ms.req_phy_model, ms.actor_model, Integer.valueOf(visualization), Integer.valueOf(object_choice));
						}
						else {
							CommandPanel.logger.severe("Layer selection error!");
						}
						//TODO: customize the size of the dialog
						MessageDialog.openInformation(container.getShell(), "Message", "Finish exhaustive refinement!");
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ScriptException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnRefine.setBounds(428, 251, 94, 26);
		btnRefine.setText("Refine");
		
		//simplification
		Button btnSimplify = new Button(container, SWT.NONE);
		btnSimplify.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String layer_choice = getCommand(grpLayer);
				String object_choice = getCommand(grpObjects);
				try {
					if (layer_choice.equals(InfoEnum.Layer.ALL.name())) {
						Inference.securityGoalSimplification(ms.req_bus_model, ms.actor_model, Integer.valueOf(object_choice));
						Inference.securityGoalSimplification(ms.req_app_model, ms.actor_model, Integer.valueOf(object_choice));
						Inference.securityGoalSimplification(ms.req_phy_model, ms.actor_model, Integer.valueOf(object_choice));
					} else if (layer_choice.equals(InfoEnum.Layer.BUSINESS.name())) {
						Inference.securityGoalSimplification(ms.req_bus_model, ms.actor_model, Integer.valueOf(object_choice));
					} else if (layer_choice.equals(InfoEnum.Layer.APPLICATION.name())) {
						Inference.securityGoalSimplification(ms.req_app_model, ms.actor_model, Integer.valueOf(object_choice));
					} else if (layer_choice.equals(InfoEnum.Layer.PHYSICAL.name())) {
						Inference.securityGoalSimplification(ms.req_phy_model, ms.actor_model, Integer.valueOf(object_choice));
					} else {
						CommandPanel.logger.severe("Layer selection error!");
					}
					//TODO: customize the size of the dialog
					MessageDialog.openInformation(container.getShell(), "Message", "Identify critical security goals!");
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ScriptException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnSimplify.setBounds(428, 283, 94, 26);
		btnSimplify.setText("Simplify");
		
		
		Button btnOperationalize = new Button(container, SWT.NONE);
		btnOperationalize.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String layer_choice = getCommand(grpLayer);
				String object_choice = getCommand(grpObjects);
				try {
					if (layer_choice.equals(InfoEnum.Layer.ALL.name())) {
						Inference.securityGoalOperationalization(ms.req_bus_model, Integer.valueOf(object_choice));
						Inference.securityGoalOperationalization(ms.req_app_model, Integer.valueOf(object_choice));
						Inference.securityGoalOperationalization(ms.req_phy_model, Integer.valueOf(object_choice));
					} else if (layer_choice.equals(InfoEnum.Layer.BUSINESS.name())) {
						Inference.securityGoalOperationalization(ms.req_bus_model, Integer.valueOf(object_choice));
					} else if (layer_choice.equals(InfoEnum.Layer.APPLICATION.name())) {
						Inference.securityGoalOperationalization(ms.req_app_model, Integer.valueOf(object_choice));
					} else if (layer_choice.equals(InfoEnum.Layer.PHYSICAL.name())) {
						Inference.securityGoalOperationalization(ms.req_phy_model, Integer.valueOf(object_choice));
					} else {
						CommandPanel.logger.severe("Layer selection error!");
					}
					//TODO: customize the size of the dialog
					MessageDialog.openInformation(container.getShell(), "Message", "Finish Operationalization of critical security goals!");
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ScriptException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnOperationalize.setBounds(428, 361, 94, 42);
		btnOperationalize.setText("Operationalize");
		
		final List alternative_list = new List(container, SWT.V_SCROLL|SWT.H_SCROLL);
		alternative_list.setBounds(10, 361, 378, 177);
		 ScrollBar sb = alternative_list.getVerticalBar();
		
//		JScrollPane listScroller = new JScrollPane(alternative_list);
//		listScroller.setPreferredSize(new Dimension(250, 80));
		
		
		Button btnCalculateAlternatives = new Button(container, SWT.NONE);
		btnCalculateAlternatives.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String layer_choice = getCommand(grpLayer);
				String object_choice = getCommand(grpObjects);
				LinkedList<String> alternatives = null;
				if (layer_choice.equals(InfoEnum.Layer.ALL.name())) {
					//TODO: we currently analyze and show alternatives only within one layer.
				} else if (layer_choice.equals(InfoEnum.Layer.BUSINESS.name())) {
					alternatives = Inference.securityAlternativeSolutions(ms.req_bus_model,
							Integer.valueOf(object_choice));
				} else if (layer_choice.equals(InfoEnum.Layer.APPLICATION.name())) {
					alternatives = Inference.securityAlternativeSolutions(ms.req_app_model,
							Integer.valueOf(object_choice));
				} else if (layer_choice.equals(InfoEnum.Layer.PHYSICAL.name())) {
					alternatives = Inference.securityAlternativeSolutions(ms.req_phy_model,
							Integer.valueOf(object_choice));
				} else {
					CommandPanel.logger.severe("Layer selection error!");
				}
				
				for (String s: alternatives){
					alternative_list.add(s);
				}
				
				//TODO: customize the size of the dialog
				MessageDialog.openInformation(container.getShell(), "Message", "Identify alternative security solutions!");
			}
		});
		btnCalculateAlternatives.setBounds(428, 423, 94, 42);
		btnCalculateAlternatives.setText("Alternative\n Solutions");
		
		
		Button btnTransferSecurityConcerns = new Button(container, SWT.NONE);
		btnTransferSecurityConcerns.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String layer_choice = getCommand(grpLayer);
				String object_choice = getCommand(grpObjects);
				LinkedList<String> alternatives = null;
				try {
					if (layer_choice.equals(InfoEnum.Layer.ALL.name())) {
						//TODO: we currently transfer security concerns only within one layer.
					} else if (layer_choice.equals(InfoEnum.Layer.BUSINESS.name())) {
						Inference.securityBusToAppTransformation(ms.req_bus_model, ms.req_app_model,
								Integer.valueOf(object_choice));
					} else if (layer_choice.equals(InfoEnum.Layer.APPLICATION.name())) {
						Inference.securityBusToAppTransformation(ms.req_bus_model, ms.req_app_model,
								Integer.valueOf(object_choice));
					} else if (layer_choice.equals(InfoEnum.Layer.PHYSICAL.name())) {
					} else {
						CommandPanel.logger.severe("Layer selection error!");
					}
				} catch (NumberFormatException | ScriptException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				MessageDialog.openInformation(container.getShell(), "Message", "Transfer security concerns to the application layer!");
			}
		});
		btnTransferSecurityConcerns.setBounds(428, 492, 94, 42);
		btnTransferSecurityConcerns.setText("Transfer Security\n Concerns");
		
		
		
		
		
		
		/**
		 * additional functions
		 */
		// Enable or disable the visualization group
		btnExhaustive.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control[] layer_list = grpRefinementMode.getChildren();
				for(Control c: layer_list){
					Button temp = (Button)c;
					if (temp.getText().equals("Exhaustive")) {
						grpRefinementDimension.setVisible(false);
						grpVisualization.setVisible(true);
					}
				}
			}
		});
		btnOnestep.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Control[] layer_list = grpRefinementMode.getChildren();
				for(Control c: layer_list){
					Button temp = (Button)c;
					if (temp.getText().equals("One-step")) {
						grpVisualization.setVisible(false);
						grpRefinementDimension.setVisible(true);
					}
				}
			}
		});
		
		
		return container;
	}
	
	/*
	 * Interpret commands according to current settings
	 */
	private String getCommand(Group grpImport) {
		Control[] layer_list = grpImport.getChildren();
		for(Control c: layer_list){
			Button temp = (Button)c;
			if(temp.getSelection()==true){
				//layers
				if (grpImport.getText().equals("Layer")) {
					if (temp.getText().equals("All layers")) {
						return InfoEnum.Layer.ALL.name();
					} else if (temp.getText().equals("Business")) {
						return InfoEnum.Layer.BUSINESS.name();
					} else if (temp.getText().equals("Application")) {
						return InfoEnum.Layer.APPLICATION.name();
					} else if (temp.getText().equals("Physical")) {
						return InfoEnum.Layer.PHYSICAL.name();
					}
				}
				//objects
				if (grpImport.getText().equals("Object")) {
					if (temp.getText().equals("All models")) {
						return String.valueOf(InfoEnum.ALL_MODELS);
					} else if (temp.getText().equals("Selected models")) {
						return String.valueOf(InfoEnum.SELECTED_MODELS);
					}
				}
				//import commands
				else if (grpImport.getText().equals("Import Source")) {
					if (temp.getText().equals("Selected elements")) {
						return InfoEnum.Commands.IMP_SELECTION.name();
					} else if (temp.getText().equals("From files")) {
						return InfoEnum.Commands.IMP_FILE.name();
					}
				}
				//model type commands
				else if (grpImport.getText().equals("Model Type")) {
					if (temp.getText().equals("Requirement model")) {
						return InfoEnum.ModelCategory.REQUIREMENT.name();
					} else if (temp.getText().equals("Trust model")) {
						return InfoEnum.ModelCategory.ACTOR.name();
					}
				}
				//refinement commands
				else if (grpImport.getText().equals("Refinement Mode")) {
					if (temp.getText().equals("One-step")) {
						return InfoEnum.Commands.REF_ALL_ONE_STEP.name();
					} else if (temp.getText().equals("Exhaustive")) {
						return InfoEnum.Commands.REF_ALL_EXHAUSTIVE.name();
					}
				}
				else if (grpImport.getText().equals("Refinement Dimension")) {
					if (temp.getText().equals("Attribute")) {
						return "attribute";
					} else if (temp.getText().equals("Asset")) {
						return "asset";
					} else if (temp.getText().equals("Interval")) {
						return "interval";
					}
				}
				else if (grpImport.getText().equals("Visualization")) {
					if (temp.getText().equals("OmniGraffle")) {
						return String.valueOf(InfoEnum.CANVAS);
					} else if (temp.getText().equals("Graphviz")) {
						return String.valueOf(InfoEnum.GRAPHVIZ);
					}
				}
				else{
					logger.warning("Command error!");
				}
			}
		}
		return null;
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
	 * Create the coolbar manager.
	 * @return the coolbar manager
	 */
	@Override
	protected CoolBarManager createCoolBarManager(int style) {
		CoolBarManager coolBarManager = new CoolBarManager(style);
		return coolBarManager;
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
			//first initalize the global logger
			CommandPanel.setup();
			//then run the graphic interface
			CommandPanel window = new CommandPanel();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
			//TODO: the size of button will be resized when running the applciation. 
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
		newShell.setText("MUSER Control Panel");
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(731, 689);
	}
}
