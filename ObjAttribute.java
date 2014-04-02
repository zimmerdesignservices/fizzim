import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;

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

public class ObjAttribute implements Cloneable {

	private String name = null;
	private String value = null;
	private int visibility;
	private String type = null;
	private String useratts = null;
	private String resetval = null;
	private Color currColor = Color.black;
	private String comment = "";
	public static int NO = 0, YES = 1, NONDEFAULT = 2;
	//global fix: fixed in global attributes
	//global var: take value from global attribute
	//local: override global attribute
	//abs: hard-coded
	public static int GLOBAL_FIXED = 3, GLOBAL_VAR = 4, LOCAL = 5, ABS = 6;
	// all field editable by default
	private int[] editable = {GLOBAL_VAR,GLOBAL_VAR,GLOBAL_VAR,GLOBAL_VAR,GLOBAL_VAR,GLOBAL_VAR,GLOBAL_VAR,GLOBAL_VAR};
	
	//for drawing
	private int x2Obj = 0,y2Obj = 0,tX,tY,tW,tH,myPage = -1,currPage;
	private int selectStatus;
	private int xTemp,yTemp;
        private int selectboxLeft,selectboxRight,selectboxBottom,selectboxTop;
	boolean outputTypeReg = false;
	boolean outputTypeFlag = false;
	
	public boolean getVisible()
	{
		if(visibility == YES)
			return true;
		if(visibility == NONDEFAULT && (editable[1] == LOCAL))
			return true;
		else
			return false;
	}
	
	
	public ObjAttribute(String na, String va, int vi, String ty,  String comm, Color color, String useratt, String resetvalue)
	{
		name = na;
		value = va;
		visibility = vi;
		type = ty;
		useratts = useratt;
		resetval = resetvalue;
		currColor = color;
		comment = comm;
	}
	
	public ObjAttribute(String na, String va, int vi, String ty,  String comm,Color color, String useratt, String resetvalue, int[] edit)
	{
		name = na;
		value = va;
		visibility = vi;
		type = ty;
		useratts = useratt;
		resetval = resetvalue;
		currColor = color;
		comment = comm;
		editable = edit;
	}
	
	
	public ObjAttribute(String name2, String nameStatus, String value2,
			String valueStatus, String vis, String visStatus, String type2,
			String typeStatus, String comm, String commStatus, Color color, String colorStatus, String useratts2, String userattsStatus, String resetval2, String resetvalStatus, int x2, int y2, int p) {
		name = name2;
		value = value2;
		visibility = Integer.parseInt(vis);
		type = type2;
		useratts = useratts2;
		resetval = resetval2;
		editable[0] = getEditableValue(nameStatus);
		editable[1] = getEditableValue(valueStatus);
		editable[2] = getEditableValue(visStatus);
		editable[3] = getEditableValue(typeStatus);
		editable[4] = getEditableValue(commStatus);
		editable[5] = getEditableValue(colorStatus);
		editable[6] = getEditableValue(userattsStatus);
		editable[7] = getEditableValue(resetvalStatus);
		x2Obj = x2;
		y2Obj = y2;
		myPage = p;
		currColor = color;
		comment = comm;
	}


	public Object clone () 
    throws CloneNotSupportedException
   {
       ObjAttribute copy = (ObjAttribute)super.clone();
       copy.editable = editable.clone();
       //TODO color?
       return copy;
   }
	
	
	
	public Object get(int i)
	{
		if(i == 0)
			return name;
		else if(i == 1)
			return value;
		else if(i == 2)
			return new Integer(visibility);
		else if(i == 3)
			return type;
		else if(i == 4)
			return comment;
		else if(i == 5)
			return currColor;
		else if(i == 6)
			return useratts;
		else if(i == 7)
			return resetval;
		else
			return null;
	}
	
	public void set(int col, Object val) {
		
		if(col == 0)
			name = (String) val;
		else if(col == 1)
			value = (String) val;
		else if(col == 2)
		{
			Integer i = (Integer) val;
			visibility =  i.intValue();
		}
		else if(col == 3)
			type = (String) val;
		else if(col == 4)
			comment = (String) val;
		else if(col == 5)
			currColor = (Color) val;
		else if(col == 6)
			useratts = (String) val;
		else if(col == 7)
			resetval = (String) val;

	}
	
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String str)
	{
		name = str;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public void setValue(String str)
	{
		value = str;
	}
	
	public int getVisibility()
	{
		return visibility;
	}
	
	public void setVisibility(int i)
	{
		visibility = i;
	}
	
	public String getType()
	{
		return type;
	}
	
	public void setType(String str)
	{
		type = str;
	}
	
	public String getComment()
	{
		return comment;
	}
	
	public void setComment(String str)
	{
		comment = str;
	}
	
	public String getUserAtts()
	{
		return useratts;
	}
	
	public void setUserAtts(String str)
	{
		useratts = str;
	}
	
	public String getresetval()
	{
		return resetval;
	}
	
	public void setresetval(String str)
	{
		resetval = str;
	}
	
	public int getEditable(int i)
	{
		return editable[i];
	}
	
	//set index i to variable j
	public void setEditable(int i, int j)
	{
		if(i < 8)
			editable[i] = j;
	}

	// for opening
	private int getEditableValue(String s)
	{
		if(s.equals("GLOBAL_FIXED"))
			return 3;
		else if(s.equals("GLOBAL_VAR"))
			return 4;
		else if(s.equals("LOCAL")) 
			return 5;
		else
			return 6;
	}
	
	//for saving
	private String getEditableName(int col)
	{
		int i = editable[col];
		if(i == 3)
			return "GLOBAL_FIXED";
		if(i == 4)
			return "GLOBAL_VAR";
		if(i == 5)
			return "LOCAL";
		if(i == 6)
			return "ABS";
		else
			return null;
	}
	
	//indentation (also exists in FizzimGui.java and GeneralObj.java
	public String i(int indent)
	{
		String ind = "";
		for(int i=0; i<indent; i++)
		{
			ind = ind + "   ";
		}
		return ind;
	}
	
	public void save(BufferedWriter writer, int ind) throws IOException
	{
		writer.write(i(ind+1) + "<" + name + ">\n"
				+ i(ind+3) + "<status>\n" + i(ind+3) + getEditableName(0)+ "\n" + i(ind+3) + "</status>\n"
				+ i(ind+2) + "<value>\n" + i(ind+2) + value + "\n" 
				+ i(ind+3) + "<status>\n" + i(ind+3) + getEditableName(1)+ "\n" + i(ind+3) + "</status>\n"
				+ i(ind+2) + "</value>\n"
				+ i(ind+2) + "<vis>\n" + i(ind+2) + visibility + "\n" 
				+ i(ind+3) + "<status>\n" + i(ind+3) + getEditableName(2)+ "\n" + i(ind+3) + "</status>\n"
				+ i(ind+2) + "</vis>\n"
				+ i(ind+2) + "<type>\n" + i(ind+2) + type + "\n"
				+ i(ind+3) + "<status>\n" + i(ind+3) + getEditableName(3)+ "\n" + i(ind+3) + "</status>\n"
				+ i(ind+2) + "</type>\n"
				+ i(ind+2) + "<comment>\n" + i(ind+2) + comment + "\n"
				+ i(ind+3) + "<status>\n" + i(ind+3) + getEditableName(4)+ "\n" + i(ind+3) + "</status>\n"
				+ i(ind+2) + "</comment>\n"
				+ i(ind+2) + "<color>\n" + i(ind+2) + currColor.getRGB() + "\n"
				+ i(ind+3) + "<status>\n" + i(ind+3) + getEditableName(5)+ "\n" + i(ind+3) + "</status>\n"
				+ i(ind+2) + "</color>\n"
				+ i(ind+2) + "<useratts>\n" + i(ind+2) + useratts + "\n"
				+ i(ind+3) + "<status>\n" + i(ind+3) + getEditableName(3)+ "\n" + i(ind+3) + "</status>\n"
				+ i(ind+2) + "</useratts>\n"
				+ i(ind+2) + "<resetval>\n" + i(ind+2) + resetval + "\n"
				+ i(ind+3) + "<status>\n" + i(ind+3) + getEditableName(3)+ "\n" + i(ind+3) + "</status>\n"
				+ i(ind+2) + "</resetval>\n"
                                );

		writer.write(i(ind+2) + "<x2Obj>\n" +i(ind+2) + x2Obj + "\n" + i(ind+2) + "</x2Obj>\n");//23 +12
		writer.write(i(ind+2) + "<y2Obj>\n" + i(ind+2) + y2Obj + "\n" + i(ind+2) + "</y2Obj>\n");//26
		writer.write(i(ind+2) + "<page>\n" + i(ind+2) + myPage + "\n" + i(ind+2) + "</page>\n");//29

		writer.write(i(ind+1) + "</" + name + ">\n");
	}


	public void paintComponent(Graphics g, int curr,
			Point point, int parentSelectStatus, int step) {

		currPage = curr;
		if (myPage == currPage && getVisible()) {
                  g.setColor(currColor);
                  FontMetrics fm1 = g.getFontMetrics();
                  
                  // dont show "name =" or "equation =", just show value
                  String text;
                  if(name.equals("name") || name.equals("equation")) {
                    text = value;
                  } else {
                    if(outputTypeReg || outputTypeFlag)
                      text = name + " <= " + value;
                    else
                      text = name + " = " + value;
                  }
                  
                  tH = fm1.getHeight();
                  tW = fm1.stringWidth(text);
                  tX = (int) point.getX();
                  tY = (int) point.getY() + tH*step;
                  int txbase = tX+x2Obj-tW/2;
                  int tybase = tY+y2Obj;
                  int yoffset = 0;
                  
                  if(text.indexOf("\\n")==-1) {
                    g.drawString(text,txbase,tybase);
                  } else {
                    /* split lines on \n */
                    tW = 0;
                    String tempText = text;
                  
                    while(tempText.indexOf("\\n")>-1) {
                      String line = tempText.substring(0,tempText.indexOf("\\n"));
                      g.drawString(line,txbase,tybase+yoffset);
                      if(fm1.stringWidth(line)>tW)
                      tW = fm1.stringWidth(line);
                      tempText = tempText.substring(tempText.indexOf("\\n")+2);
                      yoffset += tH;
                    }
                    g.drawString(tempText,txbase,tybase+yoffset);
                    if(fm1.stringWidth(tempText)>tW)
                      tW = fm1.stringWidth(tempText);
                  }
                  
                  // Since box corners are needed by setSelectStatus, set
                  // them here.
                  selectboxLeft = txbase-4;
                  selectboxRight = txbase+tW+3;
                  selectboxBottom = tybase+yoffset+4;
                  selectboxTop = tybase-tH+2;

                  //if object is selected, draw red selection box around it
                  if(selectStatus != 0 || parentSelectStatus != 0) {          
                    g.setColor(Color.red);

                    g.drawLine(selectboxLeft,selectboxTop,selectboxRight,selectboxTop);  // Top
                    g.drawLine(selectboxLeft,selectboxBottom,selectboxRight,selectboxBottom);  // Bottom
                    g.drawLine(selectboxLeft,selectboxTop,selectboxLeft,selectboxBottom);  // Left
                    g.drawLine(selectboxRight,selectboxTop,selectboxRight,selectboxBottom);  // Right
                  }
		}
	}

	//unselects object
	public void unselect() {
		selectStatus = 0;
		
	}


	public boolean setSelectStatus(int x, int y) {
		if(myPage == currPage && getVisible())
		{
			xTemp = x;
			yTemp = y;
			selectStatus = 0;
			// check if inside red selection square
                        /*
			if(x >= tX+x2Obj-tW/2-4 && x <= tX+x2Obj-tW/2+tW+3 && y >= tY+y2Obj-tH+2 && y <= tY+y2Obj+4)
					selectStatus = 1;
                        */
			if(x >= selectboxLeft && x <= selectboxRight && y >= selectboxTop && y <= selectboxBottom)
					selectStatus = 1;
		}
		if(selectStatus == 1)
			return true;
		else
			return false;
	}


	public int getSelectStatus() {
		return selectStatus;
	}


	public void adjustShapeOrPosition(int x, int y) {
		if(myPage == currPage && getVisible())
		{
			if(selectStatus == 1)
			{
				x2Obj += x - xTemp;
				y2Obj += y - yTemp;
	
				xTemp = x;
				yTemp = y;
			}

		}
		
	}
	
	public int getPage()
	{
		return myPage;
	}

	public void setPage(int i, String s)
	{
		if(myPage == -1)
			myPage = i;
	}

	public void setPage(int i) {
		myPage = i;
		
	}
	
	public void setOutputTypeReg(boolean b)
	{
		outputTypeReg = b;
	}
		
	public void setOutputTypeFlag(boolean b)
	{
		outputTypeFlag = b;
	}
		
	public String getIndent(int i)
	{
		char[] temp = new char[i];
		Arrays.fill(temp, ' ');
		return temp.toString();
	}
}
