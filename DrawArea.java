import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;
import javax.swing.*;

import java.awt.FontMetrics;

// Written by: Michael Zimmer - mike@zimmerdesignservices.com

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


public class DrawArea extends JPanel implements MouseListener, MouseMotionListener, ActionListener,Printable {

	//holds all objects to be currently drawn
	private Vector<Object> objList;
	//holds previous lists of objects
	private Vector<Vector<Object>> undoList;
	
	//temp lists
	private Vector<Object> tempList;
	
	
	//keeps track of current position in undo array
	private int currUndoIndex;
	
	//triggered when undo needs to be committed
	private boolean toCommit = false;
	
	// hold right click position
	private int rXTemp, rYTemp;
	
	//hold clicked state
	private GeneralObj tempObj;
	private GeneralObj tempOld;
	private GeneralObj tempClone;
	
	//for multiple object select
	private boolean multipleSelect = false;
	private boolean objsSelected = false;
	private int mXTemp = 0;
	private int mYTemp = 0;
	private int mX0,mY0,mX1,mY1;
	private LinkedList<Integer> selectedIndices = new LinkedList<Integer>();
	private boolean ctrlDown = false;

	//font
	private Font currFont = new Font("Arial",Font.PLAIN,11);
	private Font tableFont = new Font("Arial",Font.PLAIN,11);
	
	//global color chooser
	private JColorChooser colorChooser = new JColorChooser();
	
	private boolean loading = false;
	
	private boolean Redraw = false;

	//default settings for global table
	private boolean tableVis = true;
	private Color tableColor = Color.black;
	private Color defSC = Color.black;
	private Color defSTC = Color.black;
	private Color defLTC = Color.black;
	
	//state size
	private int StateW = 130;
	private int StateH = 130;

        // line widths
        private int LineWidth = 1;
	
	//list of global lists
	private LinkedList<LinkedList<ObjAttribute>> globalList;
	
	//parent frame
	private JFrame frame;
	
	//keeps track if file is modified since last opening/saving
	private boolean fileModified = false;
	
	// used for auto generation of state and transition names
	private int createSCounter = 0, createTCounter = 0;
	
	// pages
	private int currPage = 1;
	
	//double click settings
	private int dClickTime = 200;    // double click speed in ms
	private long lastClick = 0;
	
	//lock to grid settings
	private boolean grid = false;
	private int gridS = 25;
	
	// global table, default tab settings
	private int space = 20;
	

	
	
	
	public DrawArea(LinkedList<LinkedList<ObjAttribute>> globals)
	{
		globalList = globals;

		//create arrays to store created objects
		objList = new Vector<Object>();
		undoList = new Vector<Vector<Object>>();
		tempList = new Vector<Object>();
		this.setFocusable(true); 
		this.requestFocus();
		currUndoIndex = -1;
		
		//global attributes stored at index 0 of object array 
		objList.add(globalList);
		

		TextObj globalTable = new TextObj(10,10,globalList,tableFont);
		
		objList.add(globalTable);
		undoList.add(objList);
		currUndoIndex++;

		setBackground(Color.blue);
		addMouseListener(this);
	    addMouseMotionListener(this);

	}
	    
	public void paintComponent(Graphics g)
	{
		Graphics2D g2D = (Graphics2D) g;
		super.paintComponent(g2D);
		
		g2D.setFont(currFont);
		g2D.setStroke(new BasicStroke(getLineWidth()));
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		
		//paint all objects
		if(objList != null)
		{
			for (int i = 1; i < objList.size(); i++)
			{
				GeneralObj s = (GeneralObj) objList.elementAt(i);
				s.paintComponent(g2D,currPage);
			}
		}  
		if(multipleSelect)
		{
			g2D.setColor(Color.RED);
			g2D.drawRect(mX0, mY0, mX1-mX0, mY1-mY0);
		}
		if(loading)
		{
			updateGlobalTable();
			loading = false;
			repaint();
		}
	}
	
	


	
	public boolean canUndo()
	{
		if (currUndoIndex >= 0)
			return true;
		else
			return false;
	}
	
	@SuppressWarnings("unchecked")
	public void undo()
	{
		//store current array on undo list if it isn't already there
		if(currUndoIndex + 1 == undoList.size())
			undoList.add(objList);
		//replace objlist with a previous list
		if(currUndoIndex > 0)
		{
			objList = (Vector<Object>) undoList.elementAt(currUndoIndex);
			currUndoIndex--;
		}
		//unselect all states
		for (int i = 1; i < objList.size(); i++)
		{
			GeneralObj s = (GeneralObj) objList.elementAt(i);
			s.unselect();				
		}
		objsSelected = false;
		
		//update
		globalList = (LinkedList<LinkedList<ObjAttribute>>) objList.get(0);
		updateStates();
		updateTrans();
		updateGlobalTable();
		FizzimGui fgui = (FizzimGui) frame;
		fgui.updateGlobal(globalList);
		repaint();

	}
	
	public boolean canRedo()
	{
		if(currUndoIndex < undoList.size() - 2)
			return true;
		else
			return false;
	}
	
	@SuppressWarnings("unchecked")
	public void redo()
	{
		//if redo is possible, replace objlist with wanted list
		if(currUndoIndex < undoList.size() - 2)
		{
			objList = (Vector<Object>) undoList.elementAt(currUndoIndex + 2);
			currUndoIndex++;
		}
		globalList = (LinkedList<LinkedList<ObjAttribute>>) objList.get(0);
		updateStates();
		updateTrans();
		updateGlobalTable();
		FizzimGui fgui = (FizzimGui) frame;
		fgui.updateGlobal(globalList);
		repaint();
	}
	
	/* The following two methods create an undo point.  It is not committed to undo list yet
	 * because an undo point is not needed if the user is just clicking to select an 
	 * object (this method is triggered on mouse down)
	 * In this method, a temp list is created to hold all the pointers to objects
	 * before any modification occurs.  The objects to be modified, and all objects connected
	 * to them, are then cloned, and their clones are pointed to by objlist.
	 */
	
	@SuppressWarnings("unchecked")
	//this method is called whenever a global attribute is about to be modified
	public LinkedList<LinkedList<ObjAttribute>> setUndoPoint()
	{
		tempList = null;
		tempList = (Vector<Object>) objList.clone();
		LinkedList<LinkedList<ObjAttribute>> oldGlobal = (LinkedList<LinkedList<ObjAttribute>>) objList.get(0);
		LinkedList<LinkedList<ObjAttribute>> newGlobal = (LinkedList<LinkedList<ObjAttribute>>) oldGlobal.clone();
		objList.set(0,newGlobal);
		globalList = newGlobal;
		
		for(int i = 0; i < oldGlobal.size(); i++)
		{
			LinkedList<ObjAttribute> oldList = (LinkedList<ObjAttribute>)oldGlobal.get(i);
			LinkedList<ObjAttribute> newList = (LinkedList<ObjAttribute>)oldList.clone();
			for(int j = 0; j < oldList.size(); j++)
			{
				try {
					newList.set(j,(ObjAttribute)oldList.get(j).clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
			}
			newGlobal.set(i,newList);
			
		}
		return (LinkedList<LinkedList<ObjAttribute>>) objList.get(0);
		
	}
	
	//for multiple select, clone all selected objects
	private void setUndoPointMultiple() 
	{
		tempList = null;
		tempList = (Vector<Object>) objList.clone();
		
		//go through all selected objects and clone
		for(int i = 0; i < selectedIndices.size(); i++)		
		{
			GeneralObj oldObj = (GeneralObj) tempList.get(selectedIndices.get(i).intValue());
			GeneralObj clonedObj = null;
			try {
				clonedObj = (GeneralObj) oldObj.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
				
			//if a state, create clone of children transitions
			if(oldObj.getType() == 0)
			{
				for(int j = 1; j < objList.size(); j++)
				{
					GeneralObj s = (GeneralObj) objList.elementAt(j);
					//check all objects that have to be modified state as a parent
					if(s.getType() != 0 && s.containsParent(oldObj))
					{
						GeneralObj clonedObj2 = null;
						try {
							clonedObj2 = (GeneralObj) s.clone();
						} catch (CloneNotSupportedException e) {
							e.printStackTrace();
						}
								
						//replace old obj link in trans obj with new cloned one
						clonedObj2.notifyChange(oldObj, clonedObj);				
						clonedObj2.setParentModified(true);
												
								
						//add cloned child to object list
						int objListIndex = objList.indexOf(s);
						objList.set(objListIndex,clonedObj2);
					}
				}
			}
			objList.set(selectedIndices.get(i).intValue(),clonedObj);	
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setUndoPoint(int index, int type) 
	{
		tempList = null;
		tempList = (Vector<Object>) objList.clone();

		if(index != -1)
		{

			GeneralObj oldObj = (GeneralObj) tempList.elementAt(index);
			GeneralObj clonedObj = null;
			try {
					clonedObj = (GeneralObj) oldObj.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
	
			tempOld = oldObj;
			tempClone = clonedObj;
				
				
			//if a state, create clone of children transitions

			if(type == 0)
			{
				FizzimGui fgui = (FizzimGui) frame;
				fgui.updateGlobal(setUndoPoint());
				for(int i = 1; i < objList.size(); i++)
				{
					GeneralObj s = (GeneralObj) objList.elementAt(i);
					//check all objects that have to be modified state as a parent
					if(s.getType() != 0 && s.containsParent(oldObj))
					{
						GeneralObj clonedObj2 = null;
						try {
							clonedObj2 = (GeneralObj) s.clone();
						} catch (CloneNotSupportedException e) {
							e.printStackTrace();
						}
								
						//replace old obj link in trans obj with new cloned one
						clonedObj2.notifyChange(oldObj, clonedObj);				
						clonedObj2.setParentModified(true);
								
								
								
						//add cloned child to object list
						int objListIndex = objList.indexOf(s);
						objList.set(objListIndex,clonedObj2);
					}
				}
			}
			objList.set(index,clonedObj);	
		}
	}

	//used when properties is cancelled
	public void cancel()
	{
		objList = tempList;
	}
	
	// If a modification actually occurred, 
	// the temp list is stored to the undo list array
	public void commitUndo()
	{
		int size = undoList.size();
		// clears redo points ahead of current position
		if (currUndoIndex < size - 1)
		{
			
			for (int i = size - 1; i > currUndoIndex; i--)
				undoList.remove(i);
		}
		undoList.add(tempList);
		currUndoIndex++;
		fileModified = true;
		repaint();
	}
	
	public void mouseClicked(MouseEvent e) {	
		//System.out.println("mouseClicked:" + " Button:" + e.getButton() + " Modifiers:" + e.getModifiers() + " Popup Trigger:" + e.isPopupTrigger() + " ControlDown:" + e.isControlDown());
	}

	public void mouseEntered(MouseEvent e) {
		//System.out.println("mouseEntered:" + " Button:" + e.getButton() + " Modifiers:" + e.getModifiers() + " Popup Trigger:" + e.isPopupTrigger() + " ControlDown:" + e.isControlDown());
	}

	public void mouseExited(MouseEvent e) {
		//System.out.println("mouseeExited:" + " Button:" + e.getButton() + " Modifiers:" + e.getModifiers() + " Popup Trigger:" + e.isPopupTrigger() + " ControlDown:" + e.isControlDown());
	}
	
	public void mouseHandle(MouseEvent e) {
		
		
	}

	public void mousePressed(MouseEvent e) {
		
		//System.out.println("mousePressed:" + " Button:" + e.getButton() + " Modifiers:" + e.getModifiers() + " Popup Trigger:" + e.isPopupTrigger() + " ControlDown:" + e.isControlDown());
		GeneralObj bestMatch = null;
		boolean doubleClick = false;
		//check for double click
		if(e.getWhen() - lastClick < dClickTime && e.getButton() == MouseEvent.BUTTON1 && e.getModifiers() != 20)
			doubleClick = true;
		
		lastClick = e.getWhen();
	
		//check for ctrl click to select multiple
		if(e.isControlDown() && e.getButton() == MouseEvent.BUTTON1)
		{
			//store selected objects and their indices
			int numbSel = 0;
			LinkedList<Integer> tempIndices = new LinkedList<Integer>();
			for(int i = 1; i < objList.size(); i++)
			{
				GeneralObj obj = (GeneralObj) objList.get(i);
				if(obj.getType() == 0 || obj.getType() == 3)
				{
					if(obj.getSelectStatus() != 0)
					{
						// deselect if clicked on when selected
						if(obj.setSelectStatus(e.getX(),e.getY()))
						{
							obj.unselect();
						}
						else
						{
							obj.setSelectStatus(true);
							numbSel++;
							tempIndices.add(new Integer(i));
						}
					}
					else if(obj.setSelectStatus(e.getX(),e.getY()))
					{
						numbSel++;
						tempIndices.add(new Integer(i));
				}
				}
				else
					obj.unselect();
			}
			//if multiple items selected so far, update global selection list.
			if(numbSel > 1)
			{
				selectedIndices = tempIndices;
				objsSelected = true;
			}
		}
		//if multiple object selected
		else if(objsSelected)
		{
			setUndoPointMultiple();
			if(e.getButton() == MouseEvent.BUTTON1 && e.getModifiers() != 20)
			{
				boolean move = false;
			
				for(int i = 0; i < selectedIndices.size(); i++)
				{
					GeneralObj obj = (GeneralObj) objList.get(selectedIndices.get(i).intValue());
					if((obj.getType() == 0 || obj.getType() == 3) && obj.setBoxSelectStatus(e.getX(),e.getY()))
					{
						move = true;
					}
				}
				//if click outside, unselect all
				if(!move)
				{
					objsSelected = false;
					unselectObjs();
				}
			}
			else if(e.getButton() == MouseEvent.BUTTON3 || e.getModifiers() == 20)
			{
				createPopup(null,e);
			}
		}
		else
		{
			//if object already selected
			for (int i = 1; i < objList.size(); i++)
			{
				GeneralObj s = (GeneralObj) objList.elementAt(i);
				if(s.getSelectStatus() != 0 && s.setSelectStatus(e.getX(),e.getY()))
				{
					bestMatch = s;
						
					if(!doubleClick)
					{
						//if right click, create popup menu
						if(e.getButton() == MouseEvent.BUTTON3 || e.getModifiers() == 20)
						{
							setUndoPoint(i,s.getType());
							createPopup(s,e);
						}
						else
							setUndoPoint(i,s.getType());
						break;
					}
					else
					{
						if(s.getType() == 0)
						{
							new StateProperties(this,frame, true, (StateObj) s)
							.setVisible(true);
						}
						else if(s.getType() == 1)
						{
							Vector<StateObj> stateObjs = new Vector<StateObj>();
				    		for(int j = 1; j < objList.size(); j++)
				    		{
				    			GeneralObj obj = (GeneralObj)objList.get(j);
				    			if(obj.getType() == 0)
				    				stateObjs.add((StateObj)obj);
				    		}
				        	new TransProperties(this,frame, true, (StateTransitionObj) s,stateObjs,false,null)
							.setVisible(true);
						}
						else if(s.getType() == 2)
						{
							Vector<StateObj> stateObjs = new Vector<StateObj>();
				    		for(int j = 1; j < objList.size(); j++)
				    		{
				    			GeneralObj obj = (GeneralObj)objList.get(j);
				    			if(obj.getType() == 0)
				    				stateObjs.add((StateObj)obj);
				    		}
				        	new TransProperties(this,frame, true, (LoopbackTransitionObj) s,stateObjs,true,null)
							.setVisible(true);
						}
						else if(s.getType() == 3)
						{
							editText((TextObj) s);
						}
					}
				}
			}
		
	
			//check for text at mouse location
			if(bestMatch == null)
			{
				for (int i = 1; i < objList.size(); i++)
				{
					GeneralObj s = (GeneralObj) objList.elementAt(i);
					if(s.getType() == 3 && s.setSelectStatus(e.getX(),e.getY()))
					{
						bestMatch = s;
							
						if(e.getButton() == MouseEvent.BUTTON3 || e.getModifiers() == 20)
						{
							setUndoPoint(i,3);
							createPopup(s,e);
						}
						else
							setUndoPoint(i,3);
						break;
					} 
				}
			}
			
			//check for transition at mouse position
			if(bestMatch == null)
			{
				for (int i = 1; i < objList.size(); i++)
				{
					GeneralObj s = (GeneralObj) objList.elementAt(i);
					if((s.getType() == 1 || s.getType() == 2) && s.setSelectStatus(e.getX(),e.getY()))
					{
						bestMatch = s;
						int type = s.getType();
	
						if(e.getButton() == MouseEvent.BUTTON3 || e.getModifiers() == 20)
						{
							setUndoPoint(i,type);
							createPopup(s,e);
						}
						else
							setUndoPoint(i,type);
						break;
					} 
				}
			}
	
			//if no transitions found at that position, look through state objects
			if(bestMatch == null)
			{
				for (int i = 1; i < objList.size(); i++)
				{
					GeneralObj s = (GeneralObj) objList.elementAt(i);
					if(s.getType() == 0 && s.setSelectStatus(e.getX(),e.getY()))
					{
						bestMatch = s;
							
						if(e.getButton() == MouseEvent.BUTTON3 || e.getModifiers() == 20)
						{
							setUndoPoint(i,0);
							createPopup(s,e);
						}
						else
							setUndoPoint(i,0);
						break;
					}
				}
			}
			
			//if nothing is clicked on, and right click
			if(bestMatch == null && (e.getButton() == MouseEvent.BUTTON3 || e.getModifiers() == 20))
			{
				createPopup(e);
				setUndoPoint(-1,-1);
			}
			
			//now do multiple select if still nothing found
			if(bestMatch == null && e.getButton() == MouseEvent.BUTTON1 && e.getModifiers() != 20)
			{
				mXTemp = e.getX();
				mYTemp = e.getY();
				mX0 = 0;
				mY0 = 0;
				mX1 = 0;
				mY1 = 0;
				multipleSelect = true;
				objsSelected = false;
				selectedIndices.clear();
			}
			
			repaint();
		}
	}

	public void mouseReleased(MouseEvent e) {
		//System.out.println("mouseReleased:" + " Button:" + e.getButton() + " Modifiers:" + e.getModifiers() + " Popup Trigger:" + e.isPopupTrigger() + " ControlDown:" + e.isControlDown());	
	
		multipleSelect = false;
		if(!objsSelected)
		{
			selectedIndices.clear();
		}
		
		//done modifying all objects, so notify state objects that they are done changing
		//this means transition object won't have to re-calculate connection points
		
		toCommit = false;
			
		for (int i = 1; i < objList.size(); i++)
		{
			GeneralObj t = (GeneralObj) objList.elementAt(i);
			//check for modified state
			if (t.isModified() && t.getType() == 0)
			{
				toCommit = true;
				for(int j = 1; j < objList.size(); j++)
				{
					GeneralObj obj = (GeneralObj) objList.elementAt(j);
					if(obj.getType() != 0 && obj.isParentModified())
						obj.updateObj();
					obj.setParentModified(false);
				}
			}

			//check for modified line or text box
			if((t.getType() == 1 || t.getType() == 2) && t.isModified())
			{
				toCommit = true;
				for(int j = 1; j < objList.size(); j++)
				{
					GeneralObj obj = (GeneralObj) objList.elementAt(j);
					if((obj.getType() == 3) && obj.isParentModified())
						obj.updateObj();
					obj.setParentModified(false);
				}
			}
			
			if(t.getType() == 3 && t.isModified())
			{
				toCommit = true;
			}
			t.setModified(false);
		}
			
		if(toCommit)
			commitUndo();
		
		repaint();
	}
	
public void updateTransitions()
{
	for(int j = 1; j < objList.size(); j++)
	{				
		GeneralObj obj = (GeneralObj) objList.elementAt(j);
		if(obj.getType() != 0 && obj.isParentModified())
			obj.updateObj();
		
	}
	
}

	public void mouseDragged(MouseEvent arg0) {

		//keep movement within page
		int x = arg0.getX();
		int y = arg0.getY();
		if(x<0)
			x=0;
		if(y<0)
			y=0;
		FizzimGui fgui = (FizzimGui) frame;
		if(x>fgui.maxW)
			x=fgui.maxW;
		if(y>fgui.maxH)
			y=fgui.maxH;

		// move object if multiple select is off
		if(!multipleSelect && !arg0.isControlDown() && arg0.getModifiers() == 16)
		{
			for (int i = 1; i < objList.size(); i++)
			{
				GeneralObj s = (GeneralObj) objList.elementAt(i);
				if(s.getSelectStatus() != 0)
				{
					s.adjustShapeOrPosition(x,y);
					for(int j = 1; j < objList.size(); j++)
					{				
						GeneralObj obj = (GeneralObj) objList.elementAt(j);
						if(obj.getType() != 0 && obj.isParentModified())
							obj.updateObj();
						
					}
					//break;
					
				}			
			}
			repaint();
		}

		//if multiple select is on, then check if any objects inside yet
		else if(!arg0.isControlDown() && arg0.getModifiers() == 16)
		{
			// correct box coordinates
			if(x<mXTemp)
			{
				mX0 = x;
				mX1 = mXTemp;
			}
			else
			{
				mX0 = mXTemp;
				mX1 = x;
			}
			if(y<mYTemp)
			{
				mY0 = y;
				mY1 = mYTemp;
			}
			else
			{
				mY0 = mYTemp;
				mY1 = y;
			}
			
			int tempNumb = 0;
			objsSelected = false;
			selectedIndices.clear();
			for (int i = 1; i < objList.size(); i++)
			{			
				GeneralObj s = (GeneralObj) objList.elementAt(i);
				if((s.getType() == 0 || s.getType() == 3) && s.setBoxSelectStatus(mX0,mY0,mX1,mY1))
				{
					tempNumb++;
					selectedIndices.add(new Integer(i));
				}
			}
			if(tempNumb > 1)
				objsSelected = true;
			else
				selectedIndices.clear();
			repaint();
		}
	}

	public void mouseMoved(MouseEvent arg0) {
		
	}
	
	
	public void createPopup(GeneralObj obj, MouseEvent e)
	{
		// store location of click and object that is clicked on
		rXTemp = e.getX();
		rYTemp = e.getY();
		tempObj = obj;

        JMenuItem menuItem;

        //Create the popup menu.
        JPopupMenu popup = new JPopupMenu();
        
        //create submenu for moving pages
        JMenu pages = new JMenu("Move to Page...");
        FizzimGui fgui = (FizzimGui) frame;
        
        for(int i = 1; i < fgui.getPages(); i++)
        {
        	if(i != currPage)
        	{
	        	menuItem = new JMenuItem(fgui.getPageName(i));
		        menuItem.addActionListener(this);
		        pages.add(menuItem);
        	}
        }
        if(obj == null)
        	popup.add(pages);
        
        if(obj != null && obj.getType() == 0)
        {
        	menuItem = new JMenuItem("Add Loopback Transition");
        	menuItem.setMnemonic(KeyEvent.VK_L);
	        menuItem.addActionListener(this);
	        popup.add(menuItem);
	        
	        JMenu states = new JMenu("Add State Transition to...");
	        states.setMnemonic(KeyEvent.VK_T);
	        states.setDisplayedMnemonicIndex(10);
	        for(int j = 1; j < objList.size(); j++)
	        {
	        	GeneralObj obj1 = (GeneralObj)objList.get(j);
	        	if(obj1.getType() == 0 && !obj.getName().equals(obj1.getName()))
	        	{
	        		menuItem = new JMenuItem(obj1.getName());
	        		menuItem.addActionListener(this);
	        		states.add(menuItem);
	        	}
	        }
	        popup.add(states);
	        
        	menuItem = new JMenuItem("Edit State Properties");
        	menuItem.setMnemonic(KeyEvent.VK_E);
	        menuItem.addActionListener(this);
	        popup.add(menuItem);
	        popup.add(pages);
        }
        if(obj != null && obj.getType() == 1)
        {

        	menuItem = new JMenuItem("Edit State Transition Properties");
        	menuItem.setMnemonic(KeyEvent.VK_E);
	        menuItem.addActionListener(this);
	        popup.add(menuItem);
	        if(obj.getSelectStatus() == StateTransitionObj.TXT)
	        {
	        	JMenu pages2 = new JMenu("Move to Page...");
	            StateTransitionObj sobj = (StateTransitionObj) obj;
	            for(int i = 1; i < fgui.getPages(); i++)
	            {
	            	if(i != currPage && (i == sobj.getEPage() || i == sobj.getSPage()))
	            	{
		            	menuItem = new JMenuItem(fgui.getPageName(i));
		    	        menuItem.addActionListener(this);
		    	        pages2.add(menuItem);
	            	}
	            }
	        	popup.add(pages2);
	        }
	                
        }
        if(obj != null && obj.getType() == 2)
        {
	        menuItem = new JMenuItem("Edit Loopback Transition Properties");
	        menuItem.setMnemonic(KeyEvent.VK_E);
	        menuItem.addActionListener(this);
	        popup.add(menuItem);
        }
        if(obj != null && obj.getType() == 3)
        {
	        menuItem = new JMenuItem("Edit Text");
	        menuItem.setMnemonic(KeyEvent.VK_E);
	        menuItem.addActionListener(this);
	        popup.add(menuItem);
	        popup.add(pages);
        }
        
        popup.show(e.getComponent(), e.getX(),e.getY());

	}
	
	public void createPopup(MouseEvent e)
	{
		rXTemp = e.getX();
		rYTemp = e.getY();
		tempObj = null;
        JMenuItem menuItem;

        //Create the popup menu.
        JPopupMenu popup = new JPopupMenu();
        menuItem = new JMenuItem("Quick New State");
        menuItem.setMnemonic(KeyEvent.VK_Q);
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("New State");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("New State Transition");
        menuItem.setMnemonic(KeyEvent.VK_T);
        menuItem.setDisplayedMnemonicIndex(10);
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("New Loopback Transition");
        menuItem.setMnemonic(KeyEvent.VK_L);
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("New Free Text");
        menuItem.setMnemonic(KeyEvent.VK_F);
        menuItem.addActionListener(this);
        popup.add(menuItem);
        
        popup.show(e.getComponent(), e.getX(),e.getY());

	}
	
  
	// called when item on popup menu is selected
    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        
        //if cloned for undo
        if(tempObj == tempOld)
        	tempObj = tempClone;
        
        String input = source.getText();

        
        if(input == "Edit Text")
        {
        	editText((TextObj) tempObj);
        }
        else if(input == "Edit State Properties")
        {
        	new StateProperties(this,frame, true, (StateObj) tempObj)
			.setVisible(true);
        }
        else if(input == "Edit Loopback Transition Properties")
        {
        	Vector<StateObj> stateObjs = new Vector<StateObj>();
    		for(int i = 1; i < objList.size(); i++)
    		{
    			GeneralObj obj = (GeneralObj)objList.get(i);
    			if(obj.getType() == 0)
    				stateObjs.add((StateObj)obj);
    		}
        	new TransProperties(this,frame, true, (LoopbackTransitionObj) tempObj,stateObjs,true,null)
			.setVisible(true);
        }
        else if(input == "Edit State Transition Properties")
        {
        	Vector<StateObj> stateObjs = new Vector<StateObj>();
    		for(int i = 1; i < objList.size(); i++)
    		{
    			GeneralObj obj = (GeneralObj)objList.get(i);
    			if(obj.getType() == 0)
    				stateObjs.add((StateObj)obj);
    		}
        	new TransProperties(this,frame, true, (StateTransitionObj) tempObj,stateObjs,false,null)
			.setVisible(true);
        }
        else if(input == "Quick New State")
        {
    		GeneralObj state = new StateObj(rXTemp-StateW/2,rYTemp-StateH/2,rXTemp+StateW/2,rYTemp+StateH/2,createSCounter, currPage, defSC,grid, gridS);
    		createSCounter++;
    		objList.add(state);
    		state.updateAttrib(globalList,3);
    		commitUndo();

        }
        else if(input == "New State")
        {
    		GeneralObj state = new StateObj(rXTemp-StateW/2,rYTemp-StateH/2,rXTemp+StateW/2,rYTemp+StateH/2,createSCounter, currPage, defSC,grid,gridS);
    		createSCounter++;
    		objList.add(state);
    		state.updateAttrib(globalList,3);
    		new StateProperties(this,frame, true, (StateObj) state)
			.setVisible(true);
        }
        else if(input == "New State Transition")
        {
        	Vector<StateObj> stateObjs = new Vector<StateObj>();
    		for(int i = 1; i < objList.size(); i++)
    		{
    			GeneralObj obj = (GeneralObj)objList.get(i);
    			
    			if(obj.getType() == 0)
    			{
    				stateObjs.add((StateObj)obj);	
    			}
    			
    		}
    		if(stateObjs.size() > 1)
    		{
	        	GeneralObj trans = new StateTransitionObj(createTCounter,currPage,this, defSTC);
	    		createTCounter++;
	    		objList.add(trans);
	    		trans.updateAttrib(globalList,4);
	    		new TransProperties(this,frame, true, (TransitionObj) trans, stateObjs,false,null)
				.setVisible(true);
    		}
    		else
    		{
    			JOptionPane.showMessageDialog(this,
                        "Must be more than 2 states before a transition can be created",
                        "error",
                        JOptionPane.ERROR_MESSAGE);
    		}
        }
        else if(input == "Add Loopback Transition")
        {
        	Vector<StateObj> stateObjs = new Vector<StateObj>();
    		for(int i = 1; i < objList.size(); i++)
    		{
    			GeneralObj obj = (GeneralObj)objList.get(i);
    			
    			if(obj.getType() == 0)
    			{
    				stateObjs.add((StateObj)obj);	
    			}
    			
    		}
    		if(stateObjs.size() > 0)
    		{
	        	GeneralObj trans = new LoopbackTransitionObj(rXTemp,rYTemp,createTCounter,currPage, defLTC);
	    		createTCounter++;
	    		objList.add(trans);
	    		trans.updateAttrib(globalList,4);
	    		new TransProperties(this,frame, true, (TransitionObj) trans, stateObjs,true,(StateObj)tempObj)
				.setVisible(true);
    		}
    		else
    		{
    			JOptionPane.showMessageDialog(this,
                        "Must be more than 1 states before a loopback transition can be created",
                        "error",
                        JOptionPane.ERROR_MESSAGE);
    		}
        }
        else if(input == "New Loopback Transition")
        {
        	Vector<StateObj> stateObjs = new Vector<StateObj>();
    		for(int i = 1; i < objList.size(); i++)
    		{
    			GeneralObj obj = (GeneralObj)objList.get(i);
    			
    			if(obj.getType() == 0)
    			{
    				stateObjs.add((StateObj)obj);	
    			}
    			
    		}
    		if(stateObjs.size() > 0)
    		{
	        	GeneralObj trans = new LoopbackTransitionObj(rXTemp,rYTemp,createTCounter,currPage, defLTC);
	    		createTCounter++;
	    		objList.add(trans);
	    		trans.updateAttrib(globalList,4);
	    		new TransProperties(this,frame, true, (TransitionObj) trans, stateObjs,true,null)
				.setVisible(true);
    		}
    		else
    		{
    			JOptionPane.showMessageDialog(this,
                        "Must be more than 1 states before a loopback transition can be created",
                        "error",
                        JOptionPane.ERROR_MESSAGE);
    		}
        }
        else if(input == "New Free Text")
        {
        	GeneralObj text = new TextObj("",rXTemp,rYTemp,currPage);
    		objList.add(text);
        	editText((TextObj) text);
        }
        else if(checkStateName(input))
        {
        	GeneralObj trans = new StateTransitionObj(createTCounter,currPage,this,(StateObj)tempObj,getStateObj(input), defSTC);
    		createTCounter++;
    		objList.add(trans);
    		StateTransitionObj sTrans = (StateTransitionObj) trans;
    		sTrans.initTrans((StateObj)tempObj,getStateObj(input));
    		
    		trans.updateAttrib(globalList,4);
    		commitUndo();
        }
        else if(getPageIndex(input) > -1)
        {

	        int page = getPageIndex(input);
	        
	        if(page != currPage)
	        {
	        	if(!objsSelected)
	        	{
	        		tempObj.setPage(page);
		       
		        	for(int j = 1; j < objList.size(); j++)
					{
						GeneralObj obj = (GeneralObj) objList.elementAt(j);
						if(obj.getType() != 0 && obj.isParentModified())
							obj.updateObj();
						obj.setParentModified(false);
					}
		        	tempObj.setModified(false);
		        	tempObj.updateObj();
		        	commitUndo();
		        	unselectObjs();
	        	}
	        	else
	        	{
	        		//move all states
	        		for(int i = 0; i < selectedIndices.size(); i++)
					{
	        			GeneralObj obj = (GeneralObj) objList.get(selectedIndices.get(i).intValue());
						if(obj.getType() == 0 || obj.getType() == 3)
						{
							obj.setPage(page);
							if(obj.getType() == 0)
								obj.setModified(true);
						}
					}
	        		for(int j = 1; j <objList.size(); j++)
	        		{
	        			GeneralObj obj = (GeneralObj) objList.elementAt(j);
	        			if(obj.getType() != 0 && obj.isParentModified())
	        			{
							TransitionObj trans = (TransitionObj) obj;
	        				trans.updateObjPages(page);
	        			}
						obj.setParentModified(false);
					}
	        		for(int k = 0; k < selectedIndices.size(); k++)
					{
	        			GeneralObj obj = (GeneralObj) objList.get(selectedIndices.get(k).intValue());
						if(obj.getType() == 0)
						{
							obj.setModified(false);
						}
					}
	        		
	        		unselectObjs();
	        		objsSelected = false;
	        		multipleSelect = false;
	        		selectedIndices.clear();
	        		commitUndo();

	        	}
	        	
	        }
        }
        repaint();

    }
    
    private int getPageIndex(String input) {
		FizzimGui fgui = (FizzimGui) frame;
		return fgui.getPageIndex(input);
	}

	private StateObj getStateObj(String name)
    {
    	for(int j = 1; j < objList.size(); j++)
        {
        	GeneralObj obj1 = (GeneralObj)objList.get(j);
        	if(obj1.getType() == 0 && obj1.getName().equals(name))
        		return (StateObj) obj1;
        }
        return null;
    }
    
    private boolean checkStateName(String input)
    {
        for(int j = 1; j < objList.size(); j++)
        {
        	GeneralObj obj1 = (GeneralObj)objList.get(j);
        	if(obj1.getType() == 0 && obj1.getName().equals(input))
        		return true;
        }
        return false;
    }

    // update state attribute lists when global list is updated
	public void updateStates() {
		String resetName = null;
		for(int j = 0; j < globalList.get(0).size(); j++)
		{
			 if(globalList.get(0).get(j).getName().equals("reset_state"))
				 resetName = globalList.get(0).get(j).getValue();
		}
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj o = (GeneralObj) objList.elementAt(i);
			if(o.getType() == 0)
			{
				StateObj s = (StateObj) o;
				s.updateAttrib(globalList,3);
				if(s.getName().equals(resetName))
					s.setReset(true);
				else
					s.setReset(false);
			}
		}
		
	}
	
    // update transition attribute lists when global list is updated
	public void updateTrans() {
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj o = (GeneralObj) objList.elementAt(i);
			if(o.getType() == 1 || o.getType() == 2)
			{
				TransitionObj s = (TransitionObj) o;
				s.updateAttrib(globalList,4);
			}
		}
		
	}


	public void editText(TextObj obj)
	{
		//ColorChooserIcon icon = new ColorChooserIcon(obj.getColor(),colorChooser);
		//addMouseListener(icon);
		String s = (String)JOptionPane.showInputDialog(
		        frame,
		        "Edit Text:\n",
		        "Edit Text Properties",
		        JOptionPane.PLAIN_MESSAGE,
		        null,
		        null,
		        obj.getText());
			
		if(s != null)
		{
			obj.setText(s);
			commitUndo();
		}
	}



	public void setJFrame(FizzimGui fizzimGui) {
		frame = fizzimGui;
		
	}
	

	//check for duplicate names
	public boolean checkStateNames()
	{
		TreeSet<String> stateSet = new TreeSet<String>();
		int stateCounter = 0;
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj obj = (GeneralObj)objList.get(i);
			if(obj.getType() == 0)
			{
				stateSet.add(obj.getName());
				stateCounter++;
			}
		}
		if(stateSet.size() == stateCounter)
			return true;
		else
			return false;
	}
	
	//check for duplicate names
	public boolean checkTransNames()
	{
		TreeSet<String> transSet = new TreeSet<String>();
		int transCounter = 0;
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj obj = (GeneralObj)objList.get(i);
			if(obj.getType() == 1 || obj.getType() == 2)
			{
				transSet.add(obj.getName());
				transCounter++;
			}
		}
		if(transSet.size() == transCounter)
			return true;
		else
			return false;
	}
	


	public void save(BufferedWriter writer) throws IOException {
		writer.write("## START PREFERENCES\n");
		writer.write("<SCounter>\n" + createSCounter + "\n</SCounter>\n");
		writer.write("<TCounter>\n" + createTCounter + "\n</TCounter>\n");
		writer.write("<TableVis>\n" + tableVis + "\n</TableVis>\n");
		writer.write("<TableSpace>\n" + space + "\n</TableSpace>\n");
		writer.write("<TableFont>\n" + tableFont.getFontName() + "\n" + tableFont.getSize() + "\n</TableFont>\n");
		writer.write("<TableColor>\n" + tableColor.getRGB() + "\n</TableColor>\n");
		writer.write("<Font>\n" + currFont.getFontName() + "\n" + currFont.getSize() + "\n</Font>\n");
		writer.write("<Grid>\n" + grid + "\n" + gridS + "\n</Grid>\n");
		writer.write("<PageSizeW>\n" + getMaxW() + "\n</PageSizeW>\n");
		writer.write("<PageSizeH>\n" + getMaxH() + "\n</PageSizeH>\n");
		writer.write("<StateW>\n" + getStateW() + "\n</StateW>\n");
		writer.write("<StateH>\n" + getStateH() + "\n</StateH>\n");
		writer.write("<LineWidth>\n" + getLineWidth() + "\n</LineWidth>\n");
		writer.write("## END PREFERENCES\n");
		writer.write("## START OBJECTS\n");

		// tell every object to save itself
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj obj = (GeneralObj) objList.get(i);
			obj.save(writer);
		}
		writer.write("## END OBJECTS\n");

	}



	public void delete() {
		
		setUndoPoint(-1,-1);
		//indices of transitions to delete
		LinkedList<Integer> trans = new LinkedList<Integer>();
		// find indices of all transitions to delete
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj obj = (GeneralObj) objList.get(i);
			// for deleteing multiple selected objects, find transitions
			boolean toDel = false;
			if((obj.getType() == 1 || obj.getType() == 2) && objsSelected)
			{
				for(int j = 0; j < selectedIndices.size(); j++)
				{
					GeneralObj state = (GeneralObj) objList.get(selectedIndices.get(j).intValue());
					if(obj.containsParent(state))
						toDel = true;
				}
			}
			// make sure global table isnt removed
			if(obj.getType() == 3 && objsSelected)
			{
				TextObj txt = (TextObj) obj;
				if(txt.getGlobalTable() && txt.getSelectStatus() != 0)
				{
					String error = "To remove global table, go to 'File->Preferences'";
					JOptionPane.showMessageDialog(frame,
	                        error,
	                        "error",
	                        JOptionPane.ERROR_MESSAGE);
				}
				// remove from selected indices
				for(int j = 0; j < selectedIndices.size(); j++)
				{
					Integer tempInt = selectedIndices.get(j);
					if(tempInt.intValue() == i)
					{
						selectedIndices.remove(j);
						break;
					}
				}
			}
			
			
			//for delteing single object, if it is a state remove the transitions
			if(!objsSelected && obj.getSelectStatus() != 0)
			{
				//if transition, just delete
				if((obj.getType() == 1 || obj.getType() == 2))
				{
				objList.remove(i);
					commitUndo();
					break;
				}
				// stop delete of global table, otherwise delete
			
				if(obj.getType() == 3)
				{
					TextObj txt = (TextObj) obj;
					if(txt.getGlobalTable() && txt.getSelectStatus() != 0)
					{
						String error = "To remove global table, go to 'File->Preferences'";
						JOptionPane.showMessageDialog(frame,
		                        error,
	                        "error",
		                        JOptionPane.ERROR_MESSAGE);
					}
					else
					{
						objList.remove(i);
						commitUndo();
						break;
					}			
				}
	
			//if state, add transitions to delete
				if(obj.getType() == 0)
				{
					for(int j = 1; j < objList.size(); j++)
					{
						GeneralObj t = (GeneralObj) objList.elementAt(j);
						if(t.getType() == 1 || t.getType() == 2)
						{
							TransitionObj tran = (TransitionObj) t;
							if(tran.containsParent(obj))
								trans.add(new Integer(j));
						}
					}
					// make sure state gets deleted at correct time
					selectedIndices.add(new Integer(i));
				}
			}
			if(toDel)
				trans.add(new Integer(i));	
			
			
		}
		//delete all selected

	
		while(selectedIndices.size() > 0 || trans.size() > 0)
		{
			int i1 = -1;
			int i2 = -1;
			if(selectedIndices.size() > 0)
				i1 = selectedIndices.get((selectedIndices.size()-1)).intValue();
			if(trans.size() > 0)
				i2 = trans.get((trans.size()-1)).intValue();
			if(i1>i2)
			{
				objList.remove(i1);
				selectedIndices.removeLast();
			}
			else
			{
				objList.remove(i2);
				trans.removeLast();
			}
		}
		
		commitUndo();
		objsSelected = false;
		
		
	}

	

	public static void disableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(false);
	}

	public static void enableDoubleBuffering(Component c) {
		RepaintManager currentManager = RepaintManager.currentManager(c);
		currentManager.setDoubleBufferingEnabled(true);
	}

	public int print(Graphics g, PageFormat pageFormat, int pageIndex) 
	throws PrinterException {
	    if (pageIndex > 0) {
	    	return(NO_SUCH_PAGE);
	    } else {
	      		for (int i = 1; i < objList.size(); i++)
	  		{
	  			GeneralObj s = (GeneralObj) objList.elementAt(i);
	  			s.unselect();				
	  		}
	        Graphics2D g2d = (Graphics2D)g;
	        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
	        g2d.scale(pageFormat.getImageableWidth()/this.getWidth(),pageFormat.getImageableHeight()/this.getHeight());
	       
	        disableDoubleBuffering((Component)this);
	        this.paint(g2d);
	        enableDoubleBuffering((Component)this);
	        return(PAGE_EXISTS);
	    }
	}

	public void open(Vector<Object> objList2) {
		loading = true;
		currPage = 1;
		objList = objList2;
		undoList.clear();
		tempList.clear();
		currUndoIndex = 0;
		undoList.add(objList);
		toCommit = false;
		tempObj = null;
		tempOld = null;
		tempClone = null;
		fileModified = false;
		for (int i = 1; i < objList.size(); i++)
		{
			GeneralObj s = (GeneralObj) objList.elementAt(i);
			if(s.getType() == 1 || s.getType() == 2)
			{
				TransitionObj t = (TransitionObj) s;
				t.makeConnections(objList);
			}
			if(s.getType() == 3)
			{
				TextObj textObj = (TextObj) s;
				if(textObj.getGlobalTable())
				{
					textObj.loadGlobalTable(tableFont);
				}
			}
		}
		updateStates();
		updateTrans(); // Added by pz, but no sure why (initial paint of flags is incorrect without it)
		setGrid(grid,gridS);
		repaint();


	}

	
	public void open(LinkedList<LinkedList<ObjAttribute>> global) {
		loading = true;
		currPage = 1;
		globalList = global;
		objList.clear();
		objList.add(globalList);
		TextObj globalTable = new TextObj(10,10,globalList,tableFont);
		
		objList.add(globalTable);
		undoList.clear();
		undoList.add(objList);
		tempList.clear();
		currUndoIndex = 0;
		toCommit = false;
		tempObj = null;
		tempOld = null;
		tempClone = null;
		fileModified = false;
		createSCounter = 0;
		createTCounter = 0;
		updateStates();
		updateTrans(); // Added by pz, but no sure why (initial paint of flags is incorrect without it)
		repaint();

	}



	public void setSCounter(String readLine) {
		createSCounter = Integer.parseInt(readLine);
		
	}



	public void setTCounter(String readLine) {
		createTCounter = Integer.parseInt(readLine);
		
	}



	public void updateGlobal(LinkedList<LinkedList<ObjAttribute>> globalList2) {
		globalList = globalList2;
		
	}
	
	public LinkedList<LinkedList<ObjAttribute>> getGlobalList()
	{
		return globalList;
	}

	public void setFileModifed(boolean b)
	{
		fileModified = b;
	}
	
	public boolean getFileModifed()
	{
		return fileModified;
	}



	public String[] getStateNames() {
		ArrayList<String> names = new ArrayList<String>();
		names.add("null");
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj s = (GeneralObj) objList.get(i);
			if(s.getType() == 0)
				names.add(s.getName());
		}
		String names1[] = names.toArray(new String[names.size()]);
		return names1;
	}
	
	public void setCurrPage(int i)
	{
		currPage = i;
	}
	
	public int getMaxH()
	{
		FizzimGui fgui = (FizzimGui) frame;
		return fgui.getMaxH();
	}
	public int getMaxW()
	{
		FizzimGui fgui = (FizzimGui) frame;
		return fgui.getMaxW();
	}

	public void removePage(int tab) {
		for(int i = objList.size()-1; i > 0; i--)
		{
			GeneralObj obj = (GeneralObj) objList.get(i);
			if(obj.getType() != 1)
			{
				if(obj.getPage() == tab)
				{
					objList.remove(i);
				}
				if(obj.getPage() > tab)
					obj.decrementPage();
			}
			else
			{
				StateTransitionObj obj1 = (StateTransitionObj) obj;
				if(obj1.getSPage() == tab || obj1.getEPage() == tab)
					objList.remove(i);
				if(obj1.getSPage() > tab)
					obj1.decrementSPage();
				if(obj1.getEPage() > tab)
					obj1.decrementEPage();
			}
			
		}
		
	}
	
	public void unselectObjs()
	{
		for (int i = 1; i < objList.size(); i++)
		{
			GeneralObj s = (GeneralObj) objList.elementAt(i);
			s.unselect();				
		}
	}

	public void resetUndo() {
		undoList.clear();
		undoList.add(objList);
		tempList.clear();
		currUndoIndex = 0;
		
	}

	public String getPageName(int page) {
		FizzimGui fgui = (FizzimGui) frame;
		return fgui.getPageName(page);
	}

	public void updateGlobalTable() {
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj obj = (GeneralObj) objList.get(i);
			if(obj.getType() == 3)
			{
				TextObj textObj = (TextObj) obj;
				if(textObj.getGlobalTable())
					textObj.updateGlobalText(globalList,tableFont,tableVis,space,tableColor);
			}
		}
		repaint();
		
	}
	
	public void setFont(Font font)
	{
		currFont = font;
	}
	
	public Font getFont()
	{
		return currFont;
	}
	
	public void setTableFont(Font font)
	{
		tableFont = font;
		
	}
	
	public Font getTableFont()
	{
		return tableFont;
	}
	

	
	public void setSpace(int i)
	{
		space = i;
	}
	
	public int getSpace()
	{
		return space;
	}
	
	public boolean getTableVis()
	{
		return tableVis;
	}
	
	public void setTableVis(boolean b)
	{
		tableVis = b;
	}
	
	public Color getTableColor()
	{
		return tableColor;
	}
	
	public void setTableColor(Color c)
	{
		tableColor = c;
	}
	
	public void setGrid(boolean b, int i)
	{
		grid = b;
		gridS = i;
		for(int j = 1; j < objList.size(); j++)
		{
			GeneralObj obj = (GeneralObj) objList.get(j);
			if(obj.getType() == 0)
			{
				StateObj obj1 = (StateObj) obj;
				obj1.setGrid(b,i);
			}
		}
	}
	
	public boolean getGrid()
	{
		return grid;
	}
	
	public int getGridSpace()
	{
		return gridS;
	}

	//generate pixel offset for page connectors
	public int getOffset(int page, StateObj startState, StateTransitionObj transObj, String type) {
		int totalNumb = 0;
		int numb = 0;
		for(int i = 1; i < objList.size(); i++)
		{
			
			GeneralObj obj = (GeneralObj) objList.get(i);
			// find number that transition is out of total number
			if(obj.getType() == 1)
			{
				StateTransitionObj trans = (StateTransitionObj) obj;
				if(trans.pageConnectorExists(page,startState,type))
				{
					totalNumb++;
					if(trans.equals(transObj))
						numb = totalNumb;
					
				}
			}
		}
		//find number for cener position
		int avg;
		if(totalNumb % 2 != 0)
			avg = (int)((totalNumb+1)/2);
		else
			avg = (int)(totalNumb/2);
		int finalOffset = (numb-avg)*40;
		return finalOffset;
	}

	public void pageConnUpdate(StateObj startState, StateObj endState) {
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj obj = (GeneralObj) objList.get(i);
			if(obj.getType() == 1)
			{
				StateTransitionObj trans = (StateTransitionObj) obj;
				if(trans.pageConnectorExists(startState.getPage(),startState,"start") ||
						trans.pageConnectorExists(endState.getPage(),endState,"end"))
				{
					trans.setEndPts();
					
				
					
				}
			}
		}
	}
	
	//for redrawing all page connectors when page is resized
	public void updatePageConn()
	{
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj obj = (GeneralObj) objList.get(i);
			if(obj.getType() == 1)
			{
				StateTransitionObj trans = (StateTransitionObj) obj;
				if(trans.getSPage() != trans.getEPage())
					trans.setEndPts();
			}
		}
	}

	public void moveOnResize(int maxW, int maxH)
	{
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj obj = (GeneralObj) objList.get(i);
			//move text objects onto page
			if(obj.getType() == 3)
			{
				TextObj text = (TextObj) obj;
				text.moveIfNeeded(maxW,maxH);
			}
			if(obj.getType() == 0)
			{
				StateObj state = (StateObj) obj;
				state.moveIfNeeded(maxW,maxH);
			}
		}
	}

	public Color getDefSC() {
		return defSC;
	}

	public void setDefSC(Color defSC) {
		this.defSC = defSC;
	}

	public Color getDefSTC() {
		return defSTC;
	}

	public void setDefSTC(Color defSTC) {
		this.defSTC = defSTC;
	}

	public Color getDefLTC() {
		return defLTC;
	}

	public void setDefLTC(Color defLTC) {
		this.defLTC = defLTC;
	}
	
	public JColorChooser getColorChooser()
	{
		return colorChooser;
	}

	public int getStateW()
	{
		return StateW;
	}
	
	public int getStateH()
	{
		return StateH;
	}
	
	public int getLineWidth()
	{
		return LineWidth;
	}
	
	public void setStateW(int w)
	{
		StateW = w;
	}
	
	public void setStateH(int h)
	{
		StateH = h;
	}

	public void setLineWidth(int w)
	{
		LineWidth = w;
	}
	
	
	public boolean getRedraw()
	{
		return Redraw;
	}
	

}

/* class ColorChooserIcon implements Icon, MouseListener {
	  
	  private Color color;
	  private JColorChooser colorChooser;

	  public ColorChooserIcon(Color color, JColorChooser colorChooser) {
		  this.color = color;
		  this.colorChooser = colorChooser;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
	    Color old_color = g.getColor();
	    g.setColor(color);
	    g.fillRect(x,y,15,15);
	    g.setColor(old_color);
	  }

	  public int getIconWidth() {
	    return 15;
	  }

	  public int getIconHeight() {
	    return 15;
	  }

	public void mouseClicked(MouseEvent e) {
		System.out.println("test");
		color = JColorChooser.showDialog(null, "Choose Color", color);
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	  
	}
*/
