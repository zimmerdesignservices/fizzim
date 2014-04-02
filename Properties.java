import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.Vector;
import javax.swing.border.LineBorder;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.AbstractCellEditor;

//Written by: Michael Zimmer - mike@zimmerdesignservices.com

/*
Copyright 2007 Zimmer Design Services

This file is part of Fizzim.

Fizzim is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

Fizzim is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

class MyTableModel extends AbstractTableModel {
	
	boolean DEBUG = false;
// pz
//	String[] columnNames = {"Attribute Name", "Value",
//			"Visibility", "Type", "Comment", "Color" };
	String[] columnNames = {"Attribute Name", "Value",
			"Visibility", "Type", "Comment",
                        "Color","UserAtts","ResetValue" }; // for state/trans edit boxes
	boolean global = false;

    GeneralObj obj;
    LinkedList<ObjAttribute> attrib;
    LinkedList<LinkedList<ObjAttribute>> globalList;
    JDialog dialog;
    int tab;

    
	MyTableModel(GeneralObj s,JDialog dia,LinkedList<LinkedList<ObjAttribute>> global, int k)
	{
		obj = s;
		attrib = obj.getAttributeList();
		globalList = global;
		dialog = dia;
		tab = k;
	}
	
	MyTableModel(LinkedList<ObjAttribute> list,LinkedList<LinkedList<ObjAttribute>> globalL)
	{
		global = true;
		globalList = globalL;
		attrib = list;
// pz
//		columnNames = new String[] {"Attribute Name", "Default Value",
//				"Visibility", "Type","Comment", "Color"};
		columnNames = new String[] {"Attribute Name", "Default Value",
				"Visibility", "Type","Comment",
                                "Color","UserAtts","ResetValue"}; // for main att edit boxes
	}
	

	//methods that need to be implemented

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return attrib.size();
    }

    public Object getValueAt(int row, int col) {
    	Object obj = attrib.get(row).get(col);
    	if(col == 2) //pz - transition?
    	{
    		if(obj.equals(new Integer(0)))
    			obj = "No";
    		if(obj.equals(new Integer(1)))
    			obj = "Yes";
    		if(obj.equals(new Integer(2)))
    			obj = "Only non-default";	
    	}  		
        // Translate internal representation "reg" to "statebit"
    	if(col == 3) 
    	{
    		if(obj.equals(new String("reg")))
    			obj = "statebit";
    	}  		
    	return obj;		
    }


	// get type for column
    public Class getColumnClass(int col) {
    	return getValueAt(0, col).getClass();
    }
    
    public String getColumnName(int col) {
        return columnNames[col];
    }

    //GLOBAL_FIXED can only be edited in global tab, ABS can't be edited anywhere
    public boolean isCellEditable(int row, int col) {
        if ((attrib.get(row).getEditable(col) == ObjAttribute.GLOBAL_FIXED && !global) || attrib.get(row).getEditable(col) == ObjAttribute.ABS) 
            return false;
        else if(global && attrib.equals(globalList.get(1)) && (col == 2 || col == 5))
        	return false;
         else 
            return true;
        
    }

    public void setValueAt(Object value, int row, int col) {

                        // 0: Name
                        // 1: (Default) value
                        // 2: Visibility
                        // 3: Type
                        // 4: Comment
                        // 5: Color
                        // 6: UserAtts
                        // 7: Resetval

        //turn string into corresponding number
        if(col == 2)
        {
        	if(value.equals("No"))
        		value = new Integer(0);
        	if(value.equals("Yes"))
        		value = new Integer(1);
        	if(value.equals("Only non-default"))
        		value = new Integer(2);
        }

        // Translate "statebit" to internal representation "reg"
        if (col == 3 && value.equals("statebit")) {
          value = new String("reg");
        }
        
        // check for reserved words
        // Not sure if this is really necessary
        /*
        if ( col == 1 && 
          ( false 
          || value.equals("output")  
        )) {
        	JOptionPane.showMessageDialog(dialog,
                    "\"" + value + "\"" + " is a reserved word",
                    "error",
                    JOptionPane.ERROR_MESSAGE);
        	value = attrib.get(row).get(col);
        }
        */

        // only flag, regd can have reset values
        if ( false
          || (global && col == 7 && !value.equals("") && !((attrib.get(row).getType().equals("flag") || attrib.get(row).getType().equals("regdp"))) )
          || (global && col == 3 && !(value.equals("flag")|| value.equals("regdp")) && !attrib.get(row).getresetval().equals("") ) 
          )
        {
        	JOptionPane.showMessageDialog(dialog,
                    "Only regdp and flag can have a reset value",
                    "error",
                    JOptionPane.ERROR_MESSAGE);
        	value = attrib.get(row).get(col);
        }
        // flag type outputs must have null default value
        if ( false
          || (global && col == 1 && !value.equals("") && attrib.get(row).getType().equals("flag") )
          || (global && col == 3 && value.equals("flag") && !attrib.get(row).getValue().equals("") ) 
          )
        {
        	JOptionPane.showMessageDialog(dialog,
                    "Flags cannot have default values",
                    "error",
                    JOptionPane.ERROR_MESSAGE);
        	value = attrib.get(row).get(col);
        }
        
        // forces user to enter attribute name in outputs tab
        if(!global && col == 3 && value.equals("output"))
        {
        	if(!checkOutputs(attrib.get(row)))
        	{
        		value = "";
        		JOptionPane.showMessageDialog(dialog,
                        "Attribute with that name must exist in global outputs tab",
                        "error",
                        JOptionPane.ERROR_MESSAGE);
        		
        	}
        }
        
        
        //first time type being set in outputs, create corresponding attribute in state tab
        if(global && col == 3 && attrib.equals(globalList.get(2)) && (value.equals("regdp") || value.equals("comb") || value.equals("reg") || value.equals("flag"))
        		&& attrib.get(row).get(col).equals(""))
        {
	        	int[] editable = { ObjAttribute.GLOBAL_FIXED, ObjAttribute.GLOBAL_VAR,
	        	ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR};
	        	ObjAttribute newObj = new ObjAttribute(attrib.get(row).getName(),attrib.get(row).getValue(),
	        			attrib.get(row).getVisibility(),"output","",Color.black,"","",editable);
				globalList.get(3).addLast(newObj);      	
        }
        
        //if rename something of type output
        if(global && col != 3 && globalList.get(2).equals(attrib) && !attrib.get(row).get(col).equals(value))
        {
        		renameAttribute(3,attrib.get(row).getName(),col,value,row);
        }
        

        //force user to edit in outputs tab
        // changed
        if(global && 
           // can't edit anything but type in states
           (col != 3 && attrib.equals(globalList.get(3)) && attrib.get(row).getType().equals("output"))
           // can't edit default value in transitions
           // new fizzim.pl (>=4.3 uses default from outputs page)
        || (col == 1 && attrib.equals(globalList.get(4)) && attrib.get(row).getType().equals("output"))
        )
        {
        	JOptionPane.showMessageDialog(dialog,
                    "Must edit in output tab",
                    "error",
                    JOptionPane.ERROR_MESSAGE);
        	value = attrib.get(row).get(col);
        }
        
        //if changing from comb to reg
        /*
        if(attrib.equals(globalList.get(4)) && attrib.get(row).getType().equals("comb") && value.equals("reg"))
        {
        	for(int i = 0; i < globalList.get(2).size(); i++)
        	{
        		if(attrib.get(row).getName().equals(globalList.get(2).get(i).getName()))
        		{
        			globalList.get(2).remove(i);
        		}
        	}
        }
        */
        

        // dont set if nothing changes	
        if(!attrib.get(row).get(col).equals(value))
        	attrib.get(row).set(col,value);
        
        
        // set to local if different from global
        if(!global)
        {
        	if(!checkValue(row,col,value))
        	{
        		attrib.get(row).setEditable(col, ObjAttribute.LOCAL);
        	}
        	else
        		attrib.get(row).setEditable(col, ObjAttribute.GLOBAL_VAR);
        }
        
        // checks for when name being changed
        if(attrib.get(row).getName().equals("name") && col == 1 && !global)
        	obj.setName((String) value);
        

        //restore to default if empty string was entered
        if(col != 2 && value.equals("") && !global)
        {
        	obj.updateAttrib(globalList,tab);
        }
        
        fireTableCellUpdated(row, col);
        
    }

    private boolean checkValue(int row, int col, Object value)
    {
    	LinkedList<ObjAttribute> list = globalList.get(tab);
    	String name = attrib.get(row).getName();
    	Object val = attrib.get(row).get(col);
    	for (int i = 0; i < list.size(); i++)
    	{
    		ObjAttribute obj = list.get(i);
    		if(name.equals(obj.getName()) && val.equals(obj.get(col)))
    			return true;
    	}
    	return false;
    }

	private void renameAttribute(int t, String name, int col, Object value, int row) {
		// try to find cooresponding field in states tab that is in the same relative position
		// (fixes errors due to multiple fields with same names)
		int num = 0;
		boolean needed = true;
		
		for(int h = 0; h < globalList.get(t).size(); h++)
		{
			ObjAttribute obj = globalList.get(t).get(h);
			// check if field is of type output in state tab
			if(obj.getType().equals("output") && t == 3 && num <= row)
			{
				if(num == row && obj.getName().equals(name))
				{
					obj.set(col, value);
					needed = false;
					break;
				}
				else
					num++;
			}
		}
		
		if(needed)
		{
			for(int i = 0; i < globalList.get(t).size(); i++)
			{
				ObjAttribute obj = globalList.get(t).get(i);
				if(obj.getName().equals(name))
				{
					obj.set(col, value);
					break;
				}
			}
		}
		
	}

	private boolean checkOutputs(ObjAttribute objAttribute) {
		LinkedList<ObjAttribute> outputList = globalList.get(2);
		String name = objAttribute.getName();
		for(int i = 0; i < outputList.size(); i++)
		{
			ObjAttribute obj = outputList.get(i);
			if(obj.getName().equals(name))
				return true;
		}
		return false;

	}
	
    private boolean checkName(LinkedList<ObjAttribute> linkedList, String name) {
		for(int i = 0; i < linkedList.size(); i++)
		{
			ObjAttribute obj = linkedList.get(i);
			if(obj.getName().equals(name))
				return true;
		}
		return false;
	}



}

class MyJColorRenderer extends JLabel implements TableCellRenderer {

	public MyJColorRenderer() {
		setOpaque(true);
	}
	public Component getTableCellRendererComponent(JTable arg0, Object arg1,
			boolean arg2, boolean arg3, int arg4, int arg5) {
		Color newColor = (Color)arg1;
        setBackground(newColor);
        return this;
	}
	
}

// http://java.sun.com/docs/books/tutorial/uiswing/components/table.html

class MyJColorEditor extends AbstractCellEditor implements ActionListener, TableCellEditor {

	

	Color currColor;
	JButton button;
	JColorChooser colorChooser;
	JDialog dialog;
	
	public MyJColorEditor(JColorChooser c)
	{
		button = new JButton();
		button.setActionCommand("edit");
		button.addActionListener(this);
		button.setBorderPainted(false);
		
		colorChooser = new JColorChooser();
		dialog = JColorChooser.createDialog(button,
		               "Pick a Color",
		               true,  //modal
		               colorChooser,
		               this,  //OK button handler
		               null); //no CANCEL button handler
		
		
	}
	
	public Object getCellEditorValue() {
		return currColor;
	}


	public void actionPerformed(ActionEvent e) {
		if ("edit".equals(e.getActionCommand())) {
			button.setBackground(currColor);
			colorChooser.setColor(currColor);
			dialog.setVisible(true);
		
			//Make the renderer reappear.
			fireEditingStopped();
	
		} else { //User pressed dialog's "OK" button.
			currColor = colorChooser.getColor();
		}
		
	}


	public Component getTableCellEditorComponent(JTable arg0, Object arg1,
			boolean arg2, int arg3, int arg4) {
		currColor = (Color)arg1;
		return button;
	}
	
}

    
class MyJComboBoxEditor extends DefaultCellEditor {
    public MyJComboBoxEditor(String[] items) {
        super(new JComboBox(items));
    }
}



class TransProperties extends javax.swing.JDialog {

	TransitionObj trans;
	DrawArea drawArea;
	StateObj start;
	StateObj end;
	StateObj pref;
	Vector<StateObj> stateObjs;
	boolean loopback = false;
	boolean stub = false;
	LinkedList<LinkedList<ObjAttribute>> globalList;
	Component window = this;
	JColorChooser colorChooser;
	
	/** Creates new form TransP */
	public TransProperties(DrawArea DA,java.awt.Frame parent, boolean modal, TransitionObj t, Vector<StateObj> states,boolean b,StateObj state) {
		super(parent, modal);
		trans = t;
		drawArea = DA;
		stateObjs = states;
		loopback = b;
		pref = state;
		globalList = drawArea.getGlobalList();
		colorChooser = drawArea.getColorChooser();
		if(trans.getType() == 1)
		{
			StateTransitionObj t1 = (StateTransitionObj) t;
			stub = t1.getStub();
		}
		
		initComponents();
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	//GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc=" Generated Code ">
	private void initComponents() {
		TPLabel = new javax.swing.JLabel();
		TPScroll = new javax.swing.JScrollPane();
		TPTable = new javax.swing.JTable();
		TPNew = new javax.swing.JButton();
		TPDelete = new javax.swing.JButton();
		TPCancel = new javax.swing.JButton();
		TPOK = new javax.swing.JButton();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jComboBox1 = new javax.swing.JComboBox();
		jComboBox2 = new javax.swing.JComboBox();
		jCheckBox1 = new javax.swing.JCheckBox();

		
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		if(!loopback)
		{
			setTitle("Edit State Transition Properties");
			TPLabel.setText("Edit the properties of the selected state transition:");
		}
		else
		{
			setTitle("Edit Loopback Transition Properties");
			TPLabel.setText("Edit the properties of the selected loopback transition:");
		}
		TPTable.setModel(new MyTableModel(trans,this,globalList,4));
		TPTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		
		//use dropdown boxes
		String[] options = new String[]{"No", "Yes", "Only non-default"};
		TableColumn column = TPTable.getColumnModel().getColumn(2);
		column.setCellEditor(new MyJComboBoxEditor(options));
		
		column = TPTable.getColumnModel().getColumn(5);
		column.setPreferredWidth(TPTable.getRowHeight());
		column.setCellEditor(new MyJColorEditor(colorChooser));
		column.setCellRenderer(new MyJColorRenderer());
		TPNew.setVisible(false);
		TPDelete.setVisible(false);


		TPScroll.setViewportView(TPTable);

		TPNew.setText("New");
		TPNew.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				TPNewActionPerformed(evt);
			}
		});

		TPDelete.setText("Delete");
		TPDelete.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				TPDeleteActionPerformed(evt);
			}
		});

		TPCancel.setText("Cancel");
		TPCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				TPCancelActionPerformed(evt);
			}
		});

		TPOK.setText("OK");
		TPOK.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				TPOKActionPerformed(evt);
			}
		});
		



		if(!loopback)
		{
			jLabel1.setText("Start State:");
			jLabel2.setText("End State:");
		}
		else
		{
			jLabel1.setText("State:");
			jLabel2.setVisible(false);
			jCheckBox1.setVisible(false);
		}
		
		
		jLabel3.setPreferredSize(new Dimension(50,20));
		jLabel3.setMinimumSize(new Dimension(50,20));
		jLabel3.setOpaque(true);
		jLabel3.setVisible(true);
		
		//set background color to color of transition and add action listener
		jLabel3.setBackground(trans.getColor());
		jLabel3.setBorder(new LineBorder(Color.black, 1));
		jLabel3.addMouseListener(new MouseListener() {
			

			ActionListener colorSel = new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					jLabel3.setBackground(colorChooser.getColor());
					trans.setColor(colorChooser.getColor());
				}	
			};	
			
			public void mouseClicked(MouseEvent e)
			{
				JDialog dialog;		
				dialog = JColorChooser.createDialog(window, "Choose Color", true, colorChooser, colorSel, null);
				dialog.setVisible(true);	
			}		
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		
		int size = stateObjs.size();
		
		jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(stateObjs));
		if(trans.getStartState() == null)
		{
			
			if(!loopback)
			{
				jComboBox1.setSelectedIndex(size-2);
				start = stateObjs.get(size-2);
			}
			else
			{
				if(pref == null)
				{
					jComboBox1.setSelectedIndex(size-1);
					start = stateObjs.get(size-1);
				}
				else
				{
					int index = 0;
					for(int i = 0; i < stateObjs.size(); i++)
					{
						if(stateObjs.get(i).equals(pref))
						{
							index = i;
							break;
						}
					}
					jComboBox1.setSelectedIndex(index);
					start = pref;
				}
					
			}
				
		}
		else
		{
			start = trans.getStartState();
			jComboBox1.setSelectedIndex(stateObjs.indexOf(start));
		}
		if(!loopback)
		{
			jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(stateObjs));
			if(trans.getEndState() == null)
			{
				jComboBox2.setSelectedIndex(size-1);
				end = stateObjs.get(size-1);
			}
			else
			{
				end = trans.getEndState();
				jComboBox2.setSelectedIndex(stateObjs.indexOf(end));
			}
		}
		else
			jComboBox2.setVisible(false);
		
		jComboBox1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				StartStateActionPerformed(evt);
			}
		});
		
		if(!loopback)
		{
			jComboBox2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					EndStateActionPerformed(evt);
				}
			});
		}
		
		if(!loopback)
		{
			jCheckBox1.setText("Stub?");
			jCheckBox1.setSelected(stub);
			jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0,
					0, 0));
			jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
		}
		
		
		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout
				.setHorizontalGroup(layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								layout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																TPScroll,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																480,
																Short.MAX_VALUE)
														.add(TPLabel)
														.add(
																layout
																		.createSequentialGroup()
																		.add(
																				layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.TRAILING,
																								false)
																						.add(
																								layout
																										.createSequentialGroup()
																										.add(
																												jLabel2)
																										.addPreferredGap(
																												org.jdesktop.layout.LayoutStyle.RELATED,
																												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																												Short.MAX_VALUE)
																										.add(
																												jComboBox2,
																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
																						.add(
																								org.jdesktop.layout.GroupLayout.LEADING,
																								layout
																										.createSequentialGroup()
																										.add(
																												jLabel1)
																										.addPreferredGap(
																												org.jdesktop.layout.LayoutStyle.RELATED,
																												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																												Short.MAX_VALUE)
																										.add(
																												jComboBox1,
																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
																						.add(
																								org.jdesktop.layout.GroupLayout.LEADING,
																								layout
																										.createSequentialGroup()
																										.add(
																												TPNew)
																										.addPreferredGap(
																												org.jdesktop.layout.LayoutStyle.RELATED)
																										.add(
																												TPDelete)))
																		.add(
																				layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING)
																						.add(
																								layout
																										.createSequentialGroup()
																										.add(
																												240,
																												240,
																												240)
																										.add(
																												TPOK)
																										.addPreferredGap(
																												org.jdesktop.layout.LayoutStyle.RELATED)
																										.add(
																												TPCancel))
																						.add(
																								layout
																										.createSequentialGroup()
																										.add(
																												42,
																												42,
																												42)
																										.add(
																												jLabel3))
																						.add(
																								layout
																										.createSequentialGroup()
																										.add(
																												42,
																												42,
																												42)
																										.add(
																												jCheckBox1)))))
										.addContainerGap()));
		layout
				.setVerticalGroup(layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								layout
										.createSequentialGroup()
										.addContainerGap()
										.add(TPLabel)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												TPScroll,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												151,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(TPNew).add(
																TPDelete))
										.add(22, 22, 22)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(jLabel1)
														.add(
																jComboBox1,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
														.add(jLabel3)
														)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																layout
																		.createSequentialGroup()
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED,
																				11,
																				Short.MAX_VALUE)
																		.add(
																				layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.BASELINE)
																						.add(
																								TPCancel)
																						.add(
																								TPOK))
																		.addContainerGap())
														.add(
																layout
																		.createSequentialGroup()
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.BASELINE)
																						.add(
																								jLabel2)
																						.add(
																								jComboBox2,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																						.add(jCheckBox1))
																		.addContainerGap()))));
		pack();
	}// </editor-fold>//GEN-END:initComponents

	
	private void StartStateActionPerformed(ActionEvent evt) {
		JComboBox cb = (JComboBox)evt.getSource();
        StateObj selState = (StateObj)cb.getSelectedItem();
        start = selState;
		
	}
	
	private void EndStateActionPerformed(ActionEvent evt) {
		JComboBox cb = (JComboBox)evt.getSource();
        StateObj selState = (StateObj)cb.getSelectedItem();
        end = selState;
	}
	
	//GEN-FIRST:event_TPCancelActionPerformed
	private void TPCancelActionPerformed(java.awt.event.ActionEvent evt) {
		drawArea.cancel();
		dispose();
	}//GEN-LAST:event_TPCancelActionPerformed

	//GEN-FIRST:event_TPOKActionPerformed
	private void TPOKActionPerformed(java.awt.event.ActionEvent evt) {
		TPTable.editCellAt(0,0);
		if(drawArea.checkTransNames())
		{
			if(!loopback)
			{
				if(start != end)
					trans.initTrans(start,end);
				boolean b = jCheckBox1.isSelected();
				if(b != stub)
				{
					if(trans.getType() == 1)
					{
						StateTransitionObj t1 = (StateTransitionObj) trans;
						t1.setStub(b);
					}
				}
					
				if(start != end)
					
				{
			
					drawArea.commitUndo();
					dispose();
				}
				else
				{
					JOptionPane.showMessageDialog(this,
		                    "'Start State' and 'End State' must be different.",
		                    "error",
		                    JOptionPane.ERROR_MESSAGE);
				}
			}
			else
			{
				trans.initTrans(start);
				drawArea.commitUndo();
				dispose();
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this,
                    "Two different transitions cannot have the same name.",
                    "error",
                    JOptionPane.ERROR_MESSAGE);
		}
	}//GEN-LAST:event_TPOKActionPerformed

	//GEN-FIRST:event_TPDeleteActionPerformed
	private void TPDeleteActionPerformed(java.awt.event.ActionEvent evt) {
		// delete selected rows
		int[] rows = TPTable.getSelectedRows();
		for(int i = rows.length - 1; i > -1; i--)
		{
			int type = trans.getAttributeList().get(rows[i]).getEditable(0);
			if(type != ObjAttribute.GLOBAL_FIXED && type != ObjAttribute.ABS)
			{
				trans.getAttributeList().remove(rows[i]);
				TPTable.revalidate();
			}
			else
			{
				JOptionPane.showMessageDialog(this,
                        "Cannot delete a global attribute.\n"
						+ "Must be removed from global attribute properties.",
                        "error",
                        JOptionPane.ERROR_MESSAGE);
			}
		}
	}//GEN-LAST:event_TPDeleteActionPerformed

	//GEN-FIRST:event_TPNewActionPerformed
	private void TPNewActionPerformed(java.awt.event.ActionEvent evt) {
		ObjAttribute newObj = new ObjAttribute("","",ObjAttribute.NO,"","",Color.black,"","");
		trans.getAttributeList().addLast(newObj);
		TPTable.revalidate();
	}//GEN-LAST:event_TPNewActionPerformed


	//GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JButton TPCancel;
	private javax.swing.JButton TPDelete;
	private javax.swing.JLabel TPLabel;
	private javax.swing.JButton TPNew;
	private javax.swing.JButton TPOK;
	private javax.swing.JScrollPane TPScroll;
	private javax.swing.JTable TPTable;
	private javax.swing.JComboBox jComboBox1;
	private javax.swing.JComboBox jComboBox2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JCheckBox jCheckBox1;
	// End of variables declaration//GEN-END:variables

}

class StateProperties extends javax.swing.JDialog {
	StateObj state;
	DrawArea drawArea;
	Component window = this;
	JColorChooser colorChooser;
	LinkedList<LinkedList<ObjAttribute>> globalList;
		
	
	/** Creates new form StateProperties 
	 * @param drawArea */

	public StateProperties(DrawArea DA, java.awt.Frame parent, boolean modal, StateObj s) {
		super(parent, modal);
		state = s;
		oldName = new String(state.getName());
		drawArea = DA;
		globalList = drawArea.getGlobalList();
		colorChooser = drawArea.getColorChooser();
		initComponents();
	}
	
	private void initComponents() {
		SPLabel = new javax.swing.JLabel();
		SPScroll = new javax.swing.JScrollPane();
		SPTable = new javax.swing.JTable();
		SPW = new javax.swing.JLabel();
		SPH = new javax.swing.JLabel();
		SPC = new javax.swing.JLabel();
		SPWField = new javax.swing.JFormattedTextField(NumberFormat.getIntegerInstance());
		SPHField = new javax.swing.JFormattedTextField(NumberFormat.getIntegerInstance());
		SPCancel = new javax.swing.JButton();
		SPOK = new javax.swing.JButton();
		SPNew = new javax.swing.JButton();
		SPDelete = new javax.swing.JButton();

		setResizable(false);

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Edit State Properties");
		SPLabel.setText("Edit the properties of the selected state:");

                // Type column
		SPTable.setModel(new MyTableModel(state,this,globalList,3));
		SPTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		//use dropdown boxes
		String[] options = new String[]{"No", "Yes", "Only non-default"};
		TableColumn column = SPTable.getColumnModel().getColumn(2);
		column.setCellEditor(new MyJComboBoxEditor(options));
		
                // Color column
		column = SPTable.getColumnModel().getColumn(5);
		column.setPreferredWidth(SPTable.getRowHeight());
		column.setCellEditor(new MyJColorEditor(colorChooser));
		column.setCellRenderer(new MyJColorRenderer());

		SPNew.setVisible(false);
		SPDelete.setVisible(false);


		SPScroll.setViewportView(SPTable);

		SPW.setText("Width:");
		SPH.setText("Height:");
		
		SPC.setPreferredSize(new Dimension(50,20));
		SPC.setMinimumSize(new Dimension(50,20));
		SPC.setOpaque(true);
		SPC.setVisible(true);
		
		//set background color to color of transition and add action listener
		SPC.setBackground(state.getColor());
		SPC.setBorder(new LineBorder(Color.black, 1));
		SPC.addMouseListener(new MouseListener() {
			
			ActionListener colorSel = new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					SPC.setBackground(colorChooser.getColor());
					state.setColor(colorChooser.getColor());
				}	
			};	
			
			public void mouseClicked(MouseEvent e)
			{
				JDialog dialog;		
				dialog = JColorChooser.createDialog(window, "Choose Color", true, colorChooser, colorSel, null);
				dialog.setVisible(true);	
			}		
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});

			

		SPWField.setValue(new Integer(state.getWidth()));
		SPWField.setColumns(10);
		SPHField.setValue(new Integer(state.getHeight()));
		SPHField.setColumns(10);

		
		SPCancel.setText("Cancel");
		SPCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				SPCancelActionPerformed(evt);
			}
		});

		SPOK.setText("OK");
		SPOK.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				SPOKActionPerformed(evt);
			}
		});

		SPNew.setText("New");
		SPNew.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				SPNewActionPerformed(evt);
			}
		});

		SPDelete.setText("Delete");
		SPDelete.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				SPDeleteActionPerformed(evt);
			}
		});

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				getContentPane());
		getContentPane().setLayout(layout);
		layout
				.setHorizontalGroup(layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								layout
										.createSequentialGroup()
										.addContainerGap()
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																SPScroll,
																org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																480,
																Short.MAX_VALUE)
														.add(SPLabel)
														.add(
																layout
																		.createSequentialGroup()
																		.add(
																				layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING)
																						.add(
																								SPW)
																						.add(
																								SPH)
																						)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.LEADING)
																						.add(
																								SPHField,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																						.add(
																								SPWField,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
																		.add(
																												42,
																												42,
																												42)
																								.add(SPC)
																		.add(
																				259,
																				259,
																				259)
																		.add(
																				SPOK)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				SPCancel))
														.add(
																layout
																		.createSequentialGroup()
																		.add(
																				SPNew)
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				SPDelete)))
										.addContainerGap()));
		layout
				.setVerticalGroup(layout
						.createParallelGroup(
								org.jdesktop.layout.GroupLayout.LEADING)
						.add(
								layout
										.createSequentialGroup()
										.addContainerGap()
										.add(SPLabel)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												SPScroll,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
												151,
												org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(
												org.jdesktop.layout.LayoutStyle.RELATED)
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.BASELINE)
														.add(SPNew).add(
																SPDelete))
										.add(
												layout
														.createParallelGroup(
																org.jdesktop.layout.GroupLayout.LEADING)
														.add(
																layout
																		.createSequentialGroup()
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED,
																				55,
																				Short.MAX_VALUE)
																		.add(
																				layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.BASELINE)
																						.add(
																								SPCancel)
																						.add(
																								SPOK))

																		.addContainerGap())
														.add(
																layout
																		.createSequentialGroup()
																		.add(
																				22,
																				22,
																				22)
																		.add(
																				layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.BASELINE)
																						.add(
																								SPW)
																						.add(
																								SPWField,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
																								
																						.add(
																								SPC))
																		.addPreferredGap(
																				org.jdesktop.layout.LayoutStyle.RELATED)
																		.add(
																				layout
																						.createParallelGroup(
																								org.jdesktop.layout.GroupLayout.BASELINE)
																						.add(
																								SPH)
																						.add(
																								SPHField,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
																								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																								org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
																		.addContainerGap()))));
		pack();
	}// </editor-fold>//GEN-END:initComponents

	//GEN-FIRST:event_SPNewActionPerformed
	private void SPNewActionPerformed(java.awt.event.ActionEvent evt) {
		ObjAttribute newObj = new ObjAttribute("","",ObjAttribute.NO,"","",Color.black,"","");
		state.getAttributeList().addLast(newObj);
		SPTable.revalidate();
		}//GEN-LAST:event_SPNewActionPerformed

	//GEN-FIRST:event_SPDeleteActionPerformed
	private void SPDeleteActionPerformed(java.awt.event.ActionEvent evt) {
		// delete selected rows
		int[] rows = SPTable.getSelectedRows();
		for(int i = rows.length - 1; i > -1; i--)
		{
			int type = state.getAttributeList().get(rows[i]).getEditable(0);
			if(type != ObjAttribute.GLOBAL_FIXED && type != ObjAttribute.ABS)
			{
				state.getAttributeList().remove(rows[i]);
				SPTable.revalidate();
			}
			else
			{
				JOptionPane.showMessageDialog(this,
                        "Cannot delete a global attribute.\n"
						+ "Must be removed from global attribute properties.",
                        "error",
                        JOptionPane.ERROR_MESSAGE);
			}
		}
		//notify attribute list?
	}//GEN-LAST:event_SPDeleteActionPerformed

	//GEN-FIRST:event_SPOKActionPerformed
	private void SPOKActionPerformed(java.awt.event.ActionEvent evt) {
		SPTable.editCellAt(0,0);
		if(drawArea.checkStateNames())
		{
			//temp
			try {
				SPWField.commitEdit();
				SPHField.commitEdit();
			} catch (ParseException e) {
				// TODsO Auto-generated catch block
				e.printStackTrace();
			}
			for(int j = 0; j < globalList.get(0).size(); j++)
			{
				if(globalList.get(0).get(j).getName().equals("reset_state") && globalList.get(0).get(j).getValue().equals(oldName))
				 {
					globalList.get(0).get(j).setValue(state.getName());
				 }
			}
			int width = ((Number) SPWField.getValue()).intValue();
			int height = ((Number) SPHField.getValue()).intValue();
			state.setSize(width,height);
			//make transitions redraw
			state.setStateModifiedTrue();
			drawArea.updateTransitions();
			drawArea.updateStates();
			drawArea.updateGlobalTable();
			drawArea.commitUndo();
			dispose();
		}
		else
		{
			JOptionPane.showMessageDialog(this,
                    "Two different states cannot have the same name.",
                    "error",
                    JOptionPane.ERROR_MESSAGE);
		}
	}//GEN-LAST:event_SPOKActionPerformed

	//GEN-FIRST:event_SPCancelActionPerformed
	private void SPCancelActionPerformed(java.awt.event.ActionEvent evt) {
		drawArea.cancel();
		dispose();
	}//GEN-LAST:event_SPCancelActionPerformed

	/**
	 * @param args the command line arguments
	 */


	//GEN-BEGIN:variables
	// Variables declaration - do not modify
	private javax.swing.JButton SPCancel;
	private javax.swing.JButton SPDelete;
	private javax.swing.JLabel SPH;
	private javax.swing.JFormattedTextField SPHField;
	private javax.swing.JLabel SPLabel;
	private javax.swing.JButton SPNew;
	private javax.swing.JButton SPOK;
	private javax.swing.JScrollPane SPScroll;
	private javax.swing.JTable SPTable;
	private javax.swing.JLabel SPW;
	private javax.swing.JLabel SPC;
	private javax.swing.JFormattedTextField SPWField;
	private String oldName;
	
	// End of variables declaration//GEN-END:variables


	
}

/**
*
* @author  __USER__
*/
class GlobalProperties extends javax.swing.JDialog {

	LinkedList<LinkedList<ObjAttribute>> globalLists;
	DrawArea drawArea;
	
	String[] options = new String[]{"No", "Yes", "Only non-default"};
        // pz
	//String[] outputOptions = new String[] {"reg","comb","regdp"};
	//String[] outputOptions = new String[] {"reg","comb","regdp","flag"};
	String[] outputOptions = new String[] {"statebit","comb","regdp","flag"};
	String[] reset_signal = new String[] {"posedge","negedge","positive","negative"};
	MyJComboBoxEditor reset_signal_editor = new MyJComboBoxEditor(reset_signal);
	String[] clock = new String[] {"posedge","negedge"};
	MyJComboBoxEditor clock_editor = new MyJComboBoxEditor(clock);
	String[] resetType = new String[] {"allzeros","allones","anyvalue"};
	MyJComboBoxEditor resetType_editor = new MyJComboBoxEditor(resetType);
	String[] stateObjs;
	MyJComboBoxEditor stateSelect_editor;
	private JTable currTable = null;
	private int currTab = 0;
	int[] editable = { ObjAttribute.GLOBAL_FIXED, ObjAttribute.GLOBAL_VAR,
			ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR};
	int[] editable2 = { ObjAttribute.ABS, ObjAttribute.GLOBAL_VAR,
			ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR, ObjAttribute.GLOBAL_VAR};

	JColorChooser colorChooser;
	
	/** Creates new form GlobalP */
	public GlobalProperties(DrawArea DA, java.awt.Frame parent, boolean modal,LinkedList<LinkedList<ObjAttribute>> globals, int tab) {
		super(parent, modal);
		globalLists = globals;
		drawArea = DA;
		stateObjs = drawArea.getStateNames();
		stateSelect_editor = new MyJComboBoxEditor(stateObjs);
		colorChooser = drawArea.getColorChooser();
		initComponents();
		GPTabbedPane.setSelectedIndex(tab);
		currTab = tab;
		setTable(tab);
		
	}
	
                private void setcolumnwidths(JTable table) {
			TableColumn column;

                        // Name
			column = table.getColumnModel().getColumn(0);
			column.setPreferredWidth(40); 
                        // Default value
			column = table.getColumnModel().getColumn(1);
			column.setPreferredWidth(15); 
                        // Visibility
			column = table.getColumnModel().getColumn(2);
			column.setPreferredWidth(30);
                        // Type
			column = table.getColumnModel().getColumn(3);
			column.setPreferredWidth(10); 
                        // Comment
			column = table.getColumnModel().getColumn(4);
			column.setPreferredWidth(100); 
                        // Color
			column = table.getColumnModel().getColumn(5);
			column.setPreferredWidth(5); 
                        // UserAtts
			column = table.getColumnModel().getColumn(6);
			column.setPreferredWidth(100); 
                        // Resetval
			column = table.getColumnModel().getColumn(7);
			column.setPreferredWidth(15); 
                }

		private void initComponents() {
			
			
			GPLabel = new javax.swing.JLabel();
			GPLabel2 = new javax.swing.JLabel();
			GPTabbedPane = new javax.swing.JTabbedPane();
			GPScrollMachine = new javax.swing.JScrollPane();
			GPTableMachine = new javax.swing.JTable()
			{
				public TableCellEditor getCellEditor(int row, int column)
				{
					int modelColumn = convertColumnIndexToModel( column );
					String name = (String) this.getValueAt(row,0);
					if (modelColumn == 3 && name.equals("reset_signal"))
						return (TableCellEditor)reset_signal_editor;
					else if (modelColumn == 3 && name.equals("clock"))
						return (TableCellEditor)clock_editor;
					else if (modelColumn == 3 && name.equals("reset_state"))
						return (TableCellEditor)resetType_editor;
					else if (modelColumn == 1 && name.equals("reset_state"))
						return (TableCellEditor)stateSelect_editor;
					else
						return super.getCellEditor(row, column);
				}
			};
			GPScrollState = new javax.swing.JScrollPane();
			GPTableState = new javax.swing.JTable();
			GPScrollTrans = new javax.swing.JScrollPane();
			GPTableTrans = new javax.swing.JTable();
			GPScrollInputs = new javax.swing.JScrollPane();
			GPTableInputs = new javax.swing.JTable();
			GPScrollOutputs = new javax.swing.JScrollPane();
			GPTableOutputs = new javax.swing.JTable();
			GPCancel = new javax.swing.JButton();
			GPOK = new javax.swing.JButton();
			GPOption1 = new javax.swing.JButton();
			GPOption2 = new javax.swing.JButton();
			GPOption3 = new javax.swing.JButton();
			GPOption4 = new javax.swing.JButton();
			GPOption5 = new javax.swing.JButton();
			GPOption6 = new javax.swing.JButton();
			
			setTitle("Edit Global Properties");
			setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
			setResizable(true); // controls resizability on global attributes table
                        setPreferredSize(new java.awt.Dimension(900,400)); // sets default size of global attributes table

			TableColumn column;

			GPLabel
					.setText("Here you can change the global attributes of all objects.  Once an attribute is added, its default");

			GPLabel2
					.setText("value can be overridden by right clicking on an object and selecting to 'Edit Properties.'");

			GPTableMachine.setModel(new MyTableModel((LinkedList<ObjAttribute>)globalLists.get(0),globalLists));
			GPTableMachine.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			column = GPTableMachine.getColumnModel().getColumn(2);
			column.setCellEditor(new MyJComboBoxEditor(options));
			column = GPTableMachine.getColumnModel().getColumn(5);
			//column.setPreferredWidth(GPTableMachine.getRowHeight());
			column.setCellEditor(new MyJColorEditor(colorChooser));
			column.setCellRenderer(new MyJColorRenderer());
			GPScrollMachine.setViewportView(GPTableMachine);
			GPTabbedPane.addTab("State Machine", GPScrollMachine);

			GPTableInputs.setModel(new MyTableModel((LinkedList<ObjAttribute>)globalLists.get(1),globalLists));
			GPTableInputs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			column = GPTableInputs.getColumnModel().getColumn(2);
			column.setCellEditor(new MyJComboBoxEditor(options));
			column = GPTableInputs.getColumnModel().getColumn(5);
			//column.setPreferredWidth(GPTableInputs.getRowHeight());
			column.setCellEditor(new MyJColorEditor(colorChooser));
			column.setCellRenderer(new MyJColorRenderer());
			GPScrollInputs.setViewportView(GPTableInputs);
			GPTabbedPane.addTab("Inputs", GPScrollInputs);
			
			GPTableOutputs.setModel(new MyTableModel((LinkedList<ObjAttribute>)globalLists.get(2),globalLists));
			GPTableOutputs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                        // Visibility
			column = GPTableOutputs.getColumnModel().getColumn(2);
			column.setCellEditor(new MyJComboBoxEditor(options));
                        // Type
			column = GPTableOutputs.getColumnModel().getColumn(3);
			column.setCellEditor(new MyJComboBoxEditor(outputOptions));
                        // Color
			column = GPTableOutputs.getColumnModel().getColumn(5);
			//column.setPreferredWidth(GPTableOutputs.getRowHeight());
			column.setCellEditor(new MyJColorEditor(colorChooser));
			column.setCellRenderer(new MyJColorRenderer());
			GPScrollOutputs.setViewportView(GPTableOutputs);
			GPTabbedPane.addTab("Outputs", GPScrollOutputs);

			GPTableState.setModel(new MyTableModel((LinkedList<ObjAttribute>)globalLists.get(3),globalLists));
			GPTableState.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			column = GPTableState.getColumnModel().getColumn(2);
			column.setCellEditor(new MyJComboBoxEditor(options));
			column = GPTableState.getColumnModel().getColumn(5);
			//column.setPreferredWidth(GPTableState.getRowHeight());
			column.setCellEditor(new MyJColorEditor(colorChooser));
			column.setCellRenderer(new MyJColorRenderer());
			GPScrollState.setViewportView(GPTableState);
			GPTabbedPane.addTab("States", GPScrollState);
			
			GPTableTrans.setModel(new MyTableModel((LinkedList<ObjAttribute>)globalLists.get(4),globalLists));
			GPTableTrans.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			column = GPTableTrans.getColumnModel().getColumn(2);
			column.setCellEditor(new MyJComboBoxEditor(options));
			column = GPTableTrans.getColumnModel().getColumn(5);
			column.setCellEditor(new MyJColorEditor(colorChooser));
			column.setCellRenderer(new MyJColorRenderer());
			GPScrollTrans.setViewportView(GPTableTrans);
			GPTabbedPane.addTab("Transitions", GPScrollTrans);
			
			
                        // set default column widths
                        setcolumnwidths(GPTableMachine);
                        setcolumnwidths(GPTableInputs);
                        setcolumnwidths(GPTableOutputs);
                        setcolumnwidths(GPTableState);
                        setcolumnwidths(GPTableTrans);

			
			GPTabbedPane.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e)
				{
					GPTabbedPaneActionPerformed(e);
				}
			});

			GPCancel.setText("Cancel");
			GPCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					GPCancelActionPerformed(evt);
				}
			});

			GPOK.setText("OK");
			GPOK.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					GPOKActionPerformed(evt);
				}
			});

			GPOption1.setText("Delete");
			GPOption1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					GPOption1ActionPerformed(evt);
				}
			});

			GPOption2.setText("User");
			GPOption2.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					GPOption2ActionPerformed(evt);
				}
			});
			
			GPOption3.setText("Option3");
			GPOption3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					GPOption3ActionPerformed(evt);
				}
			});
			
			GPOption4.setText("Option4");
			GPOption4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					GPOption4ActionPerformed(evt);
				}
			});
			
			GPOption5.setText("Option5");
			GPOption5.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					GPOption5ActionPerformed(evt);
				}
			});
			
			GPOption6.setText("Option6");
			GPOption6.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					GPOption6ActionPerformed(evt);
				}
			});
			GPOption6.setVisible(false);

			org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
					getContentPane());
			getContentPane().setLayout(layout);
			layout
					.setHorizontalGroup(layout
							.createParallelGroup(
									org.jdesktop.layout.GroupLayout.LEADING)
							.add(
									layout
											.createSequentialGroup()
											.addContainerGap()
											.add(
													layout
															.createParallelGroup(
																	org.jdesktop.layout.GroupLayout.LEADING)
															.add(
																	org.jdesktop.layout.GroupLayout.TRAILING,
																	layout
																			.createSequentialGroup()
																			.add(
																					GPOK)
																			.addPreferredGap(
																					org.jdesktop.layout.LayoutStyle.RELATED)
																			.add(
																					GPCancel))
															.add(
																	GPTabbedPane,
																	org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
																	480,
																	Short.MAX_VALUE)
															.add(GPLabel)
															.add(GPLabel2)
															.add(
																	layout
																			.createSequentialGroup()
																			.add(
																					GPOption1)
																			.addPreferredGap(
																					org.jdesktop.layout.LayoutStyle.RELATED)
																			.add(
																					GPOption2)
																					.addPreferredGap(
																							org.jdesktop.layout.LayoutStyle.RELATED)
																			.add(
																					GPOption3)
																			.addPreferredGap(
																					org.jdesktop.layout.LayoutStyle.RELATED)
																			.add(
																					GPOption4)
																					.addPreferredGap(
																							org.jdesktop.layout.LayoutStyle.RELATED)
																							.add(
																									GPOption5)
																									.addPreferredGap(
																											org.jdesktop.layout.LayoutStyle.RELATED)
																											.add(
																													GPOption6)))
											.addContainerGap()));
			layout
					.setVerticalGroup(layout
							.createParallelGroup(
									org.jdesktop.layout.GroupLayout.LEADING)
							.add(
									layout
											.createSequentialGroup()
											.addContainerGap()
											.add(GPLabel)
											.addPreferredGap(
													org.jdesktop.layout.LayoutStyle.RELATED)
											.add(GPLabel2)
											.addPreferredGap(
													org.jdesktop.layout.LayoutStyle.RELATED)
											.add(
													GPTabbedPane,
													org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
													179,
													org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(
													org.jdesktop.layout.LayoutStyle.RELATED)
											.add(
													layout
															.createParallelGroup(
																	org.jdesktop.layout.GroupLayout.BASELINE)
															.add(GPOption1).add(
																	GPOption2).add(
																			GPOption3).add(GPOption4).add(GPOption5).add(GPOption6))
											.addPreferredGap(
													org.jdesktop.layout.LayoutStyle.RELATED,
													40, Short.MAX_VALUE)
											.add(
													layout
															.createParallelGroup(
																	org.jdesktop.layout.GroupLayout.BASELINE)
															.add(GPCancel)
															.add(GPOK))
											.addContainerGap()));
			pack();
		}// </editor-fold>//GEN-END:initComponents
		
		protected void GPTabbedPaneActionPerformed(ChangeEvent e) {
			int tab = GPTabbedPane.getSelectedIndex();
			currTab = tab;
			setTable(tab);
			currTable.revalidate();

		}
		
		private void setTable(int tab)
		{
			if(tab == 0)
			{
				currTable = GPTableMachine;
				GPOption3.setVisible(true);
				GPOption3.setText("Reset");
				if(checkNames(currTable,"reset_state") && checkNames(currTable,"reset_signal"))
					GPOption3.setEnabled(false);
				else
					GPOption3.setEnabled(true);
				GPOption4.setVisible(false);
				GPOption5.setVisible(false);
				GPOption6.setVisible(false);
			}
			if(tab == 1)
			{
				currTable = GPTableInputs;
				GPOption3.setVisible(true);
				GPOption3.setEnabled(true);
				GPOption3.setText("Input");
				GPOption4.setVisible(true);
				GPOption4.setText("Multibit Input");
				GPOption5.setVisible(false);
				GPOption6.setVisible(false);
			}
			if(tab == 2)
			{
				currTable = GPTableOutputs;
				GPOption3.setVisible(true);
				GPOption3.setEnabled(true);
				GPOption3.setText("Output");
				GPOption4.setVisible(true);
				GPOption4.setText("Multibit Output");
				GPOption5.setVisible(true);
				GPOption5.setText("Flag");
				GPOption6.setVisible(false);
			}
			if(tab == 3)
			{
				currTable = GPTableState;
				GPOption3.setVisible(false);
				GPOption4.setVisible(false);
				GPOption5.setVisible(false);
				GPOption6.setVisible(false);
			}
			if(tab == 4)
			{
				currTable = GPTableTrans;
				GPOption3.setVisible(true);
				GPOption3.setText("Graycode");
				if(checkNames(currTable,"graycode"))
					GPOption3.setEnabled(false);
				else
					GPOption3.setEnabled(true);
				GPOption4.setVisible(true);
				GPOption4.setText("Output");
				GPOption5.setVisible(true);
				GPOption5.setText("Priority");
				if(checkNames(currTable,"priority"))
					GPOption5.setEnabled(false);
				else
					GPOption5.setEnabled(true);
				GPOption6.setVisible(false);
				
			}
		}


		//GEN-FIRST:event_GPNewActionPerformed
		private void GPOption1ActionPerformed(java.awt.event.ActionEvent evt) {
			
			
			int[] rows = currTable.getSelectedRows();
			int tab1 = GPTabbedPane.getSelectedIndex();
			LinkedList<ObjAttribute> list = globalLists.get(tab1);
			for(int i = rows.length - 1; i > -1; i--)
			{
				ObjAttribute obj = list.get(rows[i]);
				if(obj.getEditable(0) != ObjAttribute.ABS)
				{
					if((obj.getName().equals("reset_signal") || obj.getName().equals("reset_state")) && currTab == 0)
						GPOption3.setEnabled(true);
					if(obj.getName().equals("graycode") && currTab == 4)
						GPOption3.setEnabled(true);
					if(obj.getName().equals("priority") && currTab == 4)
						GPOption5.setEnabled(true);
					
					//if output being deleted, delete in states and trans
					if(currTab == 2 && obj.getType().equals("reg"))
					{
						removeAttribute(3,obj.getName());
					}
					if(currTab == 2 && obj.getType().equals("regdp"))
					{
						removeAttribute(3,obj.getName());
					}
					if(currTab == 2 && obj.getType().equals("comb"))
					{
						removeAttribute(3,obj.getName());
						//removeAttribute(2,obj.getName());
					}
					if(currTab == 2 && obj.getType().equals("flag"))
					{
						removeAttribute(3,obj.getName());
					}
					if(obj.getType().equals("output") && checkNames(GPTableOutputs,obj.getName()) && currTab != 2)
					{
						JOptionPane.showMessageDialog(this,
		                        "Must remove from outputs tab",
		                        "error",
		                        JOptionPane.ERROR_MESSAGE);
					}
					else
						list.remove(rows[i]);
				}
				else
				{
					JOptionPane.showMessageDialog(this,
	                        "Row cannot be removed",
	                        "error",
	                        JOptionPane.ERROR_MESSAGE);
				}
				currTable.revalidate();	
			}
				

		}//GEN-LAST:event_GPNewActionPerformed

		//GEN-FIRST:event_GPDeleteActionPerformed
		private void GPOption2ActionPerformed(java.awt.event.ActionEvent evt) {
			
			ObjAttribute newObj = new ObjAttribute("","",ObjAttribute.NO,"","",Color.black,"","",editable);
			int tab1 = GPTabbedPane.getSelectedIndex();
			globalLists.get(tab1).addLast(newObj);
			if(currTab == 2)
				currTable.setValueAt("reg", globalLists.get(2).size()-1, 3);
			
			currTable.revalidate();


		}//GEN-LAST:event_GPDeleteActionPerformed
		
		private void GPOption3ActionPerformed(java.awt.event.ActionEvent evt) {
			if(currTab == 0)
			{
				
				if(!checkNames(currTable,"reset_signal"))
				{
					globalLists.get(0).add(new ObjAttribute("reset_signal", "resetN", 0, "negedge","",Color.black,"","",
                                        editable2));
				}
				if(!checkNames(currTable,"reset_state"))
				{
					globalLists.get(0).add(new ObjAttribute("reset_state", "state0", 0, "","",Color.black,"","",
							editable2));
				}
				
				GPOption3.setEnabled(false);
				currTable.revalidate();
			}
			if(currTab == 1)
			{
				globalLists.get(1).add(new ObjAttribute("in", "", 0, "","",Color.black,"","",
					editable));

				currTable.revalidate();
			}
			if(currTab == 2)
			{
				globalLists.get(2).add(new ObjAttribute("out", "", 2, "","",Color.black,"","",
					editable));
				currTable.setValueAt("reg", globalLists.get(2).size()-1, 3);

				currTable.revalidate();
			}	
			if(currTab == 4)
			{
				if(!checkNames(currTable,"graycode"))
				{
					globalLists.get(4).add(new ObjAttribute("graycode", "", 1,
							"","",Color.black,"","",
                                                        editable));
				}
				GPOption3.setEnabled(false);
				currTable.revalidate();
			}
		
		
		}
		

		private void removeAttribute(int tab, String name)
		{
			for(int i = 0; i < globalLists.get(tab).size(); i++)
			{
				ObjAttribute obj = globalLists.get(tab).get(i);
				if(obj.getName().equals(name) && obj.getType().equals("output"))
					globalLists.get(tab).remove(i);
					
			}
		}

		private void GPOption4ActionPerformed(java.awt.event.ActionEvent evt) {
			

			if(currTab == 1)
			{
				globalLists.get(1).add(new ObjAttribute("in[1:0]", "", 0, "","",Color.black,"","",
					editable));

				currTable.revalidate();
			}
			if(currTab == 2)
			{
				globalLists.get(2).add(new ObjAttribute("out[1:0]", "", 2, "","",Color.black,"","",
					editable));
				currTable.setValueAt("reg", globalLists.get(2).size()-1, 3);

				currTable.revalidate();
			}	
			if(currTab == 4)
			{
				globalLists.get(4).add(new ObjAttribute("", "", 1,"output", "",Color.black,"","",
                                editable));
				
				currTable.revalidate();
			}
			
		}
		
		private void GPOption5ActionPerformed(java.awt.event.ActionEvent evt) {
			
			if(currTab == 2)
			{
				globalLists.get(2).add(new ObjAttribute("flag", "", 2, "","",Color.black,"suppress_portlist","",
					editable));
				currTable.setValueAt("flag", globalLists.get(2).size()-1, 3);

				currTable.revalidate();
			}	
			if(currTab == 4)
			{
				if(!checkNames(currTable,"priority"))
				{
					globalLists.get(4).add(new ObjAttribute("priority", "1000", 1, "", "",Color.black,"","",
                                        editable));
				}
				GPOption5.setEnabled(false);
				currTable.revalidate();
			}	
		}

		private void GPOption6ActionPerformed(java.awt.event.ActionEvent evt) {
			
			
		}
		
		
		private boolean checkNames(JTable currTable2, String string) {
			for(int i = 0; i < currTable2.getRowCount(); i++)
			{
				if(currTable2.getValueAt(i,0).equals(string))
					return true;
			}
			return false;
		}
		
		
		//GEN-FIRST:event_GPOKActionPerformed
		private void GPOKActionPerformed(java.awt.event.ActionEvent evt) {
			GPTableMachine.editCellAt(0,0);
			GPTableState.editCellAt(0,0);
			GPTableTrans.editCellAt(0,0);
			GPTableInputs.editCellAt(0,0);
			GPTableOutputs.editCellAt(0,0);
			int error = 0;
			for(int i = 0; i < globalLists.size(); i++)
			{
				for(int j = 0; j < globalLists.get(i).size(); j++)
				{
					if(i == 2 && !globalLists.get(i).get(j).getType().equals("reg") && !globalLists.get(i).get(j).getType().equals("comb") && !globalLists.get(i).get(j).getType().equals("regdp") && !globalLists.get(i).get(j).getType().equals("flag"))
						error = 2;
					for(int k = j+1; k < globalLists.get(i).size(); k++)
					{
						if(globalLists.get(i).get(j).getName().equals(globalLists.get(i).get(k).getName()))
							error = 1;
					}
				}
			}
			if(error == 0)
			{
			drawArea.updateStates();
			drawArea.updateTrans();
			drawArea.updateGlobalTable();
			drawArea.commitUndo();
			dispose();
			}
			else if(error == 1)
			{
				JOptionPane.showMessageDialog(this,
                        "Two rows cannot contain the same name",
                        "error",
                        JOptionPane.ERROR_MESSAGE);
			}
			else if(error == 2)
			{
				JOptionPane.showMessageDialog(this,
                        "An output must have a type set",
                        "error",
                        JOptionPane.ERROR_MESSAGE);
			}
	
		}//GEN-LAST:event_GPOKActionPerformed

		//GEN-FIRST:event_GPCancelActionPerformed
		private void GPCancelActionPerformed(java.awt.event.ActionEvent evt) {
			drawArea.cancel();
			dispose();
		}//GEN-LAST:event_GPCancelActionPerformed



		//GEN-BEGIN:variables
		// Variables declaration - do not modify
		private javax.swing.JButton GPCancel;
		private javax.swing.JButton GPOption1;
		private javax.swing.JButton GPOption2;
		private javax.swing.JButton GPOption3;
		private javax.swing.JButton GPOption4;
		private javax.swing.JButton GPOption5;
		private javax.swing.JButton GPOption6;
		private javax.swing.JLabel GPLabel;
		private javax.swing.JLabel GPLabel2;
		private javax.swing.JButton GPOK;
		private javax.swing.JScrollPane GPScrollMachine;
		private javax.swing.JScrollPane GPScrollState;
		private javax.swing.JScrollPane GPScrollTrans;
		private javax.swing.JScrollPane GPScrollInputs;
		private javax.swing.JScrollPane GPScrollOutputs;
		private javax.swing.JTabbedPane GPTabbedPane;
		private javax.swing.JTable GPTableMachine;
		private javax.swing.JTable GPTableState;
		private javax.swing.JTable GPTableTrans;
		private javax.swing.JTable GPTableInputs;
		private javax.swing.JTable GPTableOutputs;
		// End of variables declaration//GEN-END:variables
		
	}

public class Properties extends javax.swing.JDialog {


        

}
