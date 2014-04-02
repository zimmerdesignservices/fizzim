import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

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

/* all the methods are dependent on the format of the saved file
 * not being changed.  Comments can be added to the saved file with ##
 * This file could be modified to make file opening much more robust.
 */

public class FileParser {
	
	private File file;
	private FizzimGui fizzim;
	ArrayList<String> tempList;
	ArrayList<String> tempList2;
	LinkedList<ObjAttribute> globalMachineAttributes;
	LinkedList<ObjAttribute> globalStateAttributes;
	LinkedList<ObjAttribute> globalTransAttributes;
	LinkedList<ObjAttribute> globalInputsAttributes;
	LinkedList<ObjAttribute> globalOutputsAttributes;
	LinkedList<LinkedList<ObjAttribute>> globalList;
	DrawArea drawArea;
	Vector<Object> objList;
;
	private int ver = 0;

	
	public FileParser(File _file, FizzimGui _fizzim, DrawArea _drawArea)
	{
		file = _file;
		fizzim = _fizzim;
		drawArea = _drawArea;
		tempList = new ArrayList<String>();
		objList = new Vector<Object>();
		parse();
	}
	


	private void parse() {
		FileReader fileReader;
		
		try {
			fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader) {
				public String readLine() throws IOException
				{
					//parse out indents
					String line = super.readLine();
					while(line != null && line.length() > 0 && line.charAt(0) == ' ')
						line = line.substring(1);
					return line;
					
				}};
			
			String line = null;
			while((line = reader.readLine()) != null)
			{
				// ignore comments
				if(line.startsWith("##"))
					continue;
			
				
				
				else if(line.equals("<version>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();

					String temp = line2;
					temp = temp.replaceAll("\\.", "");
					ver = Integer.parseInt(temp);
				}
				
				//templist holds related chunks of lines
				else if(line.equals("<globals>"))
				{
					while((line = reader.readLine()) != null && !line.equals("</globals>"))
					{
						if(line.startsWith("##"))
							continue;
						tempList.add(line);
					}
					openGlobal(tempList);
				}
				else if(line.equals("<SCounter>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					drawArea.setSCounter(line2);
				}
				else if(line.equals("<TCounter>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					drawArea.setTCounter(line2);
				}
				else if(line.equals("<TableVis>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					if(line2.equals("false"))
						drawArea.setTableVis(false);
					else
						drawArea.setTableVis(true);
				}
				else if(line.equals("<TableSpace>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					try {	
						drawArea.setSpace(Integer.parseInt(line2));
					}
					catch (NumberFormatException nfe) {
						drawArea.setSpace(20);
					}
				}
				else if(line.equals("<TableFont>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					String line3 = reader.readLine();
					while(line3.startsWith("##"))
						line3 = reader.readLine();
					
					//get available fonts
					GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			        String[] fontNames = env.getAvailableFontFamilyNames();
			        for(int i = 0; i < fontNames.length; i++)
			        {
			        	try {	
				        	if(line2.equals(fontNames[i]))
				        	{
				        		Font newFont = new Font(fontNames[i],Font.PLAIN,Integer.parseInt(line3));
				        		drawArea.setTableFont(newFont);
				        	}
			        	} catch (NumberFormatException nfe) {}	
			        }			    
				}
				else if(line.equals("<TableColor>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					try {	
						drawArea.setTableColor(new Color(Integer.parseInt(line2)));
					}
					catch (NumberFormatException nfe) {
						drawArea.setTableColor(Color.black);
					}
				}
				else if(line.equals("<Font>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					String line3 = reader.readLine();
					while(line3.startsWith("##"))
						line3 = reader.readLine();
					
					//get available fonts
					GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			        String[] fontNames = env.getAvailableFontFamilyNames();
			        for(int i = 0; i < fontNames.length; i++)
			        {
			        	try {	
				        	if(line2.equals(fontNames[i]))
				        	{
				        		Font newFont = new Font(fontNames[i],Font.PLAIN,Integer.parseInt(line3));
				        		drawArea.setFont(newFont);
				        	}
			        	} catch (NumberFormatException nfe) {}	
			        }			    
				}
				else if(line.equals("<Grid>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					String line3 = reader.readLine();
					while(line3.startsWith("##"))
						line3 = reader.readLine();
					
					boolean grid = false;
					if(line2.equals("true"))
						grid = true;
					try {	
						drawArea.setGrid(grid,Integer.parseInt(line3));
					}
					catch (NumberFormatException nfe) {
						drawArea.setGrid(grid,25);
					}
				}
				
				else if(line.equals("<PageSizeW>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					String line3 = reader.readLine();
					while(line3.startsWith("##"))
						line3 = reader.readLine();
					

					try {
						fizzim.setDASize(Integer.parseInt(line2), fizzim.getMaxH());
					}
					catch (NumberFormatException nfe) { }
				}
				
				
				else if(line.equals("<PageSizeH>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					String line3 = reader.readLine();
					while(line3.startsWith("##"))
						line3 = reader.readLine();
					

					try {
						fizzim.setDASize(fizzim.getMaxW(),Integer.parseInt(line2));
					}
					catch (NumberFormatException nfe) { }
				}
				
				else if(line.equals("<StateW>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					String line3 = reader.readLine();
					while(line3.startsWith("##"))
						line3 = reader.readLine();
					

					try {
						drawArea.setStateW(Integer.parseInt(line2));
					}
					catch (NumberFormatException nfe) { }
				}
				
				else if(line.equals("<StateH>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					String line3 = reader.readLine();
					while(line3.startsWith("##"))
						line3 = reader.readLine();
					

					try {
						drawArea.setStateH(Integer.parseInt(line2));
					}
					catch (NumberFormatException nfe) { }
				}

				else if(line.equals("<LineWidth>"))
				{
					String line2 = reader.readLine();
					while(line2.startsWith("##"))
						line2 = reader.readLine();
					String line3 = reader.readLine();
					while(line3.startsWith("##"))
						line3 = reader.readLine();
					

					try {
						drawArea.setLineWidth(Integer.parseInt(line2));
					}
					catch (NumberFormatException nfe) { }
				}
				
				
				


				else if(line.equals("<state>"))
				{
					while((line = reader.readLine()) != null && !line.equals("</state>"))
					{
						if(line.startsWith("##"))
							continue;
						tempList.add(line);
					}
					openState(tempList);
				}
				
				else if(line.equals("<transition>"))
				{
					while((line = reader.readLine()) != null && !line.equals("</transition>"))
					{
						if(line.startsWith("##"))
							continue;
						tempList.add(line);
					}
					openTrans(tempList);
				}
				
				else if(line.equals("<textObj>"))
				{
					while((line = reader.readLine()) != null && !line.equals("</textObj>"))
					{
						if(line.startsWith("##"))
							continue;
						tempList.add(line);
					}
					openText(tempList);
				}
				
				else if(line.equals("<tabs>"))
				{
					fizzim.resetTabs();
					while((line = reader.readLine()) != null && !line.equals("</tabs>"))
					{
						if(line.startsWith("##"))
							continue;
						fizzim.addNewTab(line);
					}
				}
			}
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		drawArea.open(objList);
		
		
	}

	private void openText(ArrayList<String> tempList3) {
		

		String text = tempList3.get(0);
		int x = Integer.parseInt(tempList3.get(2));
		int y = Integer.parseInt(tempList3.get(5));
		int page = Integer.parseInt(tempList3.get(8));
		
		if(!text.equals("fzm_globalTable"))
		{
			TextObj obj = new TextObj(text,x,y,page);
			objList.add(obj);
		}
		else
		{
			TextObj obj = new TextObj(x,y,globalList,page);
			objList.add(obj);
		}
		

		tempList.clear();
		
	}

	private void openTrans(ArrayList<String> tempList3) {
		int startAttributes = tempList3.indexOf("<attributes>");
		int endAttributes = tempList3.indexOf("</attributes>");
		LinkedList<ObjAttribute> newList = new LinkedList<ObjAttribute>();
		openAttributeList(tempList3,startAttributes+1,endAttributes-1,newList);
		Color currColor = Color.black;
		
		int i = endAttributes;
		String startState = tempList3.get(i+2);
		String endState = tempList3.get(i+5);

		int sX = (int) Double.parseDouble(tempList3.get(i+8));
		int sY = (int) Double.parseDouble(tempList3.get(i+11));
		int eX = (int) Double.parseDouble(tempList3.get(i+14));
		int eY = (int) Double.parseDouble(tempList3.get(i+17));
		int sCX = (int) Double.parseDouble(tempList3.get(i+20));
		int sCY = (int) Double.parseDouble(tempList3.get(i+23));
		int eCX = (int) Double.parseDouble(tempList3.get(i+26));
		int eCY = (int) Double.parseDouble(tempList3.get(i+29));
		int sStateIndex = Integer.parseInt(tempList3.get(i+32));
		int eStateIndex = Integer.parseInt(tempList3.get(i+35));
		int page = Integer.parseInt(tempList3.get(i+38));
		
		if(ver >= 80316)
		{
			currColor = new Color(Integer.parseInt(tempList3.get(i+41)));
			i += 3;
		}
		
		Point pS = new Point();
		Point pSC = new Point();
		Point pE = new Point();
		Point pEC = new Point();
		boolean stub1 = false;
		
		if(!startState.equals(endState))
		{				
				pS.setLocation(Double.parseDouble(tempList3.get(i+41)),Double.parseDouble(tempList3.get(i+44)));
				pSC.setLocation(Double.parseDouble(tempList3.get(i+47)),Double.parseDouble(tempList3.get(i+50)));
				pE.setLocation(Double.parseDouble(tempList3.get(i+53)),Double.parseDouble(tempList3.get(i+56)));
				pEC.setLocation(Double.parseDouble(tempList3.get(i+59)),Double.parseDouble(tempList3.get(i+62)));
			
				String stub  = tempList3.get(i+65);				
				if(stub.equals("true"))
					stub1 = true;
			
		}
		
		Point sP = new Point(sX,sY);
		Point eP = new Point(eX,eY);
		Point sCP = new Point(sCX,sCY);
		Point eCP = new Point(eCX,eCY);
		
				
		TransitionObj obj;
		String name = newList.get(0).getValue();
		if(startState.equals(endState))
			obj = new LoopbackTransitionObj(sP,eP,sCP,eCP,newList,name, startState, endState,sStateIndex,eStateIndex, page, currColor);
		else
			obj = new StateTransitionObj(sP,eP,sCP,eCP,newList,name, startState, 
					endState,sStateIndex,eStateIndex, page, pS, pSC, pE,pEC,drawArea,stub1,currColor);
		

		objList.add(obj);
		tempList.clear();
		
	}

	private void openState(ArrayList<String> tempList3) {
		int startAttributes = tempList3.indexOf("<attributes>");
		int endAttributes = tempList3.indexOf("</attributes>");
		LinkedList<ObjAttribute> newList = new LinkedList<ObjAttribute>();
		openAttributeList(tempList3,startAttributes+1,endAttributes-1,newList);
		Color currColor = Color.black;
		
		int i = endAttributes;
		int x0 = Integer.parseInt(tempList3.get(i+2));
		int y0 = Integer.parseInt(tempList3.get(i+5));
		int x1 = Integer.parseInt(tempList3.get(i+8));
		int y1 = Integer.parseInt(tempList3.get(i+11));
		String reset  = tempList3.get(i+14);
		boolean reset1;
		if(reset.equals("true"))
			reset1 = true;
		else
			reset1 = false;
		
		int page = Integer.parseInt(tempList3.get(i+17));
		if(ver >= 80316)
			currColor = new Color(Integer.parseInt(tempList3.get(i+20)));
		String name = newList.get(0).getValue();
		StateObj state = new StateObj(x0,y0,x1,y1,newList,name,reset1, page, currColor);


		objList.add(state);
		tempList.clear();
		
	}


	private void openGlobal(ArrayList<String> list) {
		
		//create global list
		globalList = new LinkedList<LinkedList<ObjAttribute>>();
		globalMachineAttributes = new LinkedList<ObjAttribute>();
		globalInputsAttributes = new LinkedList<ObjAttribute>();
		globalOutputsAttributes = new LinkedList<ObjAttribute>();
		globalStateAttributes = new LinkedList<ObjAttribute>();
		globalTransAttributes = new LinkedList<ObjAttribute>();
		
		globalList.add(globalMachineAttributes);
		globalList.add(globalInputsAttributes);
		globalList.add(globalOutputsAttributes);
		globalList.add(globalStateAttributes);
		globalList.add(globalTransAttributes);
		
		int mS = list.indexOf("<machine>")+1;
		int mE = list.indexOf("</machine>")-1;
		int iS = list.indexOf("<inputs>")+1;
		int iE = list.indexOf("</inputs>")-1;
		int oS = list.indexOf("<outputs>")+1;
		int oE = list.indexOf("</outputs>")-1;
		int sS = list.indexOf("<state>")+1;
		int sE = list.indexOf("</state>")-1;
		int tS = list.indexOf("<trans>")+1;
		int tE = list.indexOf("</trans>")-1;
		
		openAttributeList(list, mS, mE,globalMachineAttributes);
		openAttributeList(list, iS, iE,globalInputsAttributes);
		openAttributeList(list, oS, oE,globalOutputsAttributes);
		openAttributeList(list, sS, sE,globalStateAttributes);
		openAttributeList(list, tS, tE,globalTransAttributes);
		
		/*
		int counter = 0;
		int start = counter;
		
		//set up machine globals
		while(!list.get(counter).equals("</machine>"))
			counter++;
		openAttributeList(list, start+1, counter-1,globalMachineAttributes);
		
		//set up state globals
		start = ++counter;
		while(!list.get(counter).equals("</inputs>"))
			counter++;
		openAttributeList(list, start+1, counter-1,globalInputsAttributes);
		
		//set up trans globals
		start = ++counter;
		while(!list.get(counter).equals("</outputs>"))
			counter++;
		openAttributeList(list, start+1, counter-1,globalOutputsAttributes);
		
		//set up inputs globals
		start = ++counter;
		while(!list.get(counter).equals("</state>"))
			counter++;
		openAttributeList(list, start+1, counter-1,globalStateAttributes);
		
		//set up state globals
		start = ++counter;
		while(!list.get(counter).equals("</trans>"))
			counter++;
		openAttributeList(list, start+1, counter-1,globalTransAttributes);
		*/
		
		drawArea.updateGlobal(globalList);
		fizzim.updateGlobal(globalList);
		objList.add(globalList);
		tempList.clear();	
	
	}
	


	private void openAttributeList(ArrayList<String> list,
			int start, int end, LinkedList<ObjAttribute> newList) {
                        int pointer;

		while(start < end-2)
		{
	
                        // This is a little hokey.  Fields are grabbed using
                        // absolute offsets.  As fields are added, the
                        // pointers have to be adjusted accordingly.
                        // These easiest way to understand this is to pull up a .fzm file in a editor with the line
                        // numbers turned on.
                        pointer = start;
			String name = list.get(start).substring(1,list.get(start).length()-1);
                        pointer += 2;
			String nameStatus = list.get(pointer);
                        pointer += 2; // skip over </status>

                        pointer += 1; // go to value
			String value = list.get(pointer);
                        pointer += 2; // go to status
			String valueStatus = list.get(pointer);
                        pointer += 3; // skip to next

                        pointer += 1; // go to value
			String vis = list.get(pointer);
                        pointer += 2; // go to status
			String visStatus = list.get(pointer);
                        pointer += 3; // skip over end

                        pointer += 1; // go to value
			String type = list.get(pointer);
                        pointer += 2; // go to status
			String typeStatus = list.get(pointer);
                        pointer += 3; // skip over end


                        // version dependent fields
			String comm = "";
			String commStatus = "GLOBAL_VAR";
			Color currColor = Color.black;
			String currColorStatus = "GLOBAL_VAR";
                        String useratts = "";
                        String userattsStatus = "GLOBAL_VAR";
                        String resetval = "";
                        String resetvalStatus = "GLOBAL_VAR";

			if(ver >= 70925)
			{
	
                                pointer += 1; // go to value
				comm = list.get(pointer);
                                pointer += 2; // go to status
				commStatus = list.get(pointer);
                                pointer += 3; // skip over end

                                pointer += 1; // go to value
				currColor = new Color(Integer.parseInt(list.get(pointer)));
                                pointer += 2; // go to status
				currColorStatus = list.get(pointer);
                                pointer += 3; // skip over end
			}

			if(ver >= 110222)
			{
                                pointer += 1; // go to value
			        useratts = list.get(pointer); // includes the offset from ver changes above (while will always be there)
                                pointer += 2; // go to status
			        userattsStatus = list.get(pointer);
                                pointer += 3; // skip over end
			}
			
			if(ver >= 110302)
			{
                                pointer += 1; // go to value
                                resetval = list.get(pointer); // includes the offset from ver changes above (while will always be there)
                                pointer += 2; // go to status
                                resetvalStatus = list.get(pointer);
                                pointer += 3; // skip over end
			}
			
			
                        //System.out.println("before x stuff, pointer is " + pointer);

                        pointer += 1; // go to value
			int x2Obj = Integer.parseInt(list.get(pointer));
                        pointer += 2; // skip over end

                        pointer += 1; // go to value
			int y2Obj = Integer.parseInt(list.get(pointer));
                        pointer += 2; // skip over end

                        pointer += 1; // go to value
			int page = Integer.parseInt(list.get(pointer));
                        pointer += 2; // skip over end

	
			ObjAttribute obj = new ObjAttribute(name,nameStatus,value,valueStatus,vis,visStatus,type,typeStatus,
					comm,commStatus, currColor, currColorStatus,useratts,userattsStatus,resetval,resetvalStatus,x2Obj, y2Obj, page);

			newList.add(obj);

                        pointer += 1; // go to next
                        //System.out.println("new pointer is " + pointer);
			start = pointer;
			
		}
		
		
	}
}
