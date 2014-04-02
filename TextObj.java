import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;

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

public class TextObj extends GeneralObj {

	private int selectStatus = 0;
	private int tX,tY,tW,tH,xTemp,yTemp;
        private int selectboxLeft,selectboxRight,selectboxBottom,selectboxTop;
	private String text = null;
	private GeneralObj connectedObj = null;
	private boolean parentSelected = false;
	LinkedList<LinkedList<ObjAttribute>> globalList = null;
	
	private boolean globalLoad = true;
	private boolean globalTable = false;
	private boolean tableVis = true;
	private int space = 20;
	private Color tableColor;
	
	private Color color = Color.black;

	Font tableFont;
	
	FontMetrics fm;
	
	LinkedList<String> col1 = new LinkedList<String>();
	LinkedList<String> col2 = new LinkedList<String>();
	LinkedList<String> col3 = new LinkedList<String>();
	LinkedList<String> col4 = new LinkedList<String>();
	int col1W =0, col2W = 0, col3W = 0, col4W = 0;

	
	

	
	public TextObj(int x, int y, LinkedList<LinkedList<ObjAttribute>> global, Font font)
	{
		selectStatus = 0;
		tX = x;
		tY = y;
		myPage = 1;
		globalTable = true;
		tableFont = font;
		globalList = global;

		
	}
	
	public TextObj(int x, int y, LinkedList<LinkedList<ObjAttribute>> global, int p)
	{
		selectStatus = 0;
		tX = x;
		tY = y;
		myPage = p;
		globalList = global;
		globalTable = true;
		
		
	}



	public TextObj(String s, int x, int y, int page)
	{

		selectStatus = 0;
		text = s;
		tX = x;
		tY = y;
		myPage = page;

		
	}
	
	
	public void loadGlobalTable(Font font)
	{
		tableFont = font;
	}
	
	public void updateTableFont(Font font)
	{
		tableFont = font;
	}
	

	public void updateGlobalText(LinkedList<LinkedList<ObjAttribute>> global, Font font, boolean b, int s, Color c) {

		tableFont = font;
		tableVis = b;
		space = s;
		tableColor = c;
		
		globalList = global;
		col1.clear();
		col2.clear();
		col3.clear();
		col4.clear();
		
		
		//add titles
		col1W = 0;
		col2W = 0;
		col3W = 0;
		col4W = 0;
		
		for(int i = 0; i < globalList.size(); i++)
		{
			if(i >= 3 && globalList.get(i).size() < 2)
				continue;
			else if(i < 3 && globalList.get(i).size() < 1)
				continue;
			switch(i) {
				case 0: col1.add("STATE MACHINE"); break;
				case 1: col1.add("INPUTS"); break;
				case 2: col1.add("OUTPUTS"); break;
				case 3: col1.add("STATES"); break;
				case 4: col1.add("TRANSITIONS"); break;
			}

			col2.add(" ");
			col3.add(" ");
			col4.add(" ");
			for(int j = 0; j < globalList.get(i).size(); j++)
			{
				//skip "name" for state and transition
				if((i == 3 || i == 4) && j == 0)
					continue;
				ObjAttribute obj = globalList.get(i).get(j);
				String name = "   " + obj.getName();

				if(col1W < fm.stringWidth(name))
					col1W = fm.stringWidth(name);
				col1.add(name);
				String value = obj.getValue();
				if(col2W < fm.stringWidth(value))
					col2W = fm.stringWidth(value);
				col2.add(value);
				String type = obj.getType();
                                // rename "reg" to "statebit" in the attributes table
                                if (type.equals("reg")) type = "statebit";
				if(col3W < fm.stringWidth(type))
					col3W = fm.stringWidth(type);
				col3.add(type);
				String comm = obj.getComment();
				if(col4W < fm.stringWidth(comm))
					col4W = fm.stringWidth(comm);
				col4.add(comm);
			}
			
		}
		
		col1W += space;
		col2W += space;
		col3W += space;
		col4W += space;
		
	}

	
	public boolean getGlobalTable()
	{
		return globalTable;
	}
	


	public String getText()
	{
		return text;
	}
	
	public void setText(String str)
	{
		text = str;
	}
	
	@Override
	public void adjustShapeOrPosition(int x, int y) {
		if(myPage == currPage)
		{
			if(selectStatus == 1)
			{
				tX += x - xTemp;
				tY += y - yTemp;
	
				xTemp = x;
				yTemp = y;
			}
			
			modified = true;
		}

	}


	
	public int getSelectStatus() {
		
		return selectStatus;
	}

	
	public int getType() {
		return 3;
	}

	
	public boolean isModified() {
		return modified;
	}

	
	public boolean isParentModified() {
		if(modifiedParent)
			return true;
		else
			return false;
	}


	public void paintComponent(Graphics g) {
		
		// need to set font metrics, regardless of whether the text list is on current page
		if(globalTable && tableVis)
		{
			Font tempFont = g.getFont();			
			g.setFont(tableFont);
			fm = g.getFontMetrics();
			
			
			if(globalLoad)
			{
				updateGlobalText(globalList,tableFont,tableVis,space,tableColor);
				globalLoad = false;
				
			}
			if(myPage == currPage)
			{
				g.setColor(tableColor);
				for(int i = 0; i < col1.size(); i++)
				{
					g.drawString(col1.get(i),tX,tY+i*fm.getHeight());
					g.drawString(col2.get(i),tX+col1W,tY+i*fm.getHeight());
					g.drawString(col3.get(i),tX+col1W+col2W,tY+i*fm.getHeight());
					g.drawString(col4.get(i),tX+col1W+col2W+col3W,tY+i*fm.getHeight());
				}
				tH = col1.size()*fm.getHeight();
				tW = col1W+col2W+col3W+col4W;
			}
			g.setFont(tempFont);
		}
		
		if(myPage == currPage)
		{
                        int x2Obj = 0; // to make it match ObjAttribute code
                        int y2Obj = 0; // to make it match ObjAttribute code
                        int txbase = 0;
                        int tybase = 0;
                        int yoffset = 0;

			g.setColor(Color.black);
			FontMetrics fm1 = g.getFontMetrics();

			
			if (!globalTable) {
                          tH = fm1.getHeight();
                          tW = fm1.stringWidth(text);
                          txbase = tX+x2Obj;
                          tybase = tY+y2Obj;
                          
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
                          
			}

                    // Since box corners are needed by setSelectStatus, set
                    // them here.
                    selectboxLeft = txbase-4;
                    selectboxRight = txbase+tW+3;
                    selectboxBottom = tybase+yoffset+4;
                    selectboxTop = tybase-tH+2;
  
                  //if object is selected, draw red selection box around it
                  if(selectStatus != 0 || parentSelected) {
                    g.setColor(Color.red);

	            if(!globalTable) {
                      g.drawLine(selectboxLeft,selectboxTop,selectboxRight,selectboxTop);  // Top
                      g.drawLine(selectboxLeft,selectboxBottom,selectboxRight,selectboxBottom);  // Bottom
                      g.drawLine(selectboxLeft,selectboxTop,selectboxLeft,selectboxBottom);  // Left
                      g.drawLine(selectboxRight,selectboxTop,selectboxRight,selectboxBottom);  // Right

                    } else if (tableVis) {           
                      g.drawLine(tX-4,tY+4+tH-(tH/col1.size()),tX+tW+3,tY+4+tH-(tH/col1.size()));
                      g.drawLine(tX+tW+3,tY+4+tH-(tH/col1.size()),tX+tW+3,tY-(tH/col1.size())+2);
                      g.drawLine(tX+tW+3,tY-(tH/col1.size())+2,tX-4,tY-(tH/col1.size())+2);
                      g.drawLine(tX-4,tY+4+tH-(tH/col1.size()),tX-4,tY-(tH/col1.size())+2); 
                   }

		}
              }
	}
	
	public boolean setSelectStatus(int x, int y) {
		if(myPage == currPage)
		{
			xTemp = x;
			yTemp = y;
			selectStatus = 0;
			
			// check if inside square
                        if (!globalTable) {
			  if(x >= selectboxLeft && x <= selectboxRight && y >= selectboxTop && y <= selectboxBottom) {
                            selectStatus = 1;
                          }
                        } else if (tableVis) {
			  if (x >= tX-4 && x <= tX+tW+3 && y >= tY-(tH/col1.size())+2 && y <= tY+4+tH-(tH/col1.size())) {
                            selectStatus = 1;			
                          }
                        }

			if(selectStatus == 0)
				return false;
			else
				return true;
		}
		else
			return false;

	}


	public void unselect() {
		selectStatus = 0;
	}

	
	public Point getCenter(int page)
	{
		return new Point(tX +(tW/2), tY + (tH/2));
	}



	@Override
	public Point getStart() {
		// TODO better
		Point pt = new Point(tX, tY+20);
		return pt;
	}
	

	//tX,tY,x2Obj,y2Obj,text
	@Override
	public void save(BufferedWriter writer) throws IOException {
		writer.write("<textObj>\n");
		if(text != null)
			writer.write(text + "\n");
		else
			writer.write("fzm_globalTable\n");
		writer.write(i(1) + "<x>\n" + i(1) + tX + "\n" + i(1) + "</x>\n");
		writer.write(i(1) + "<y>\n" + i(1) + tY + "\n" + i(1) + "</y>\n");
		writer.write(i(1) + "<page>\n" + i(1) + myPage + "\n" + i(1) + "</page>\n");
		writer.write("</textObj>\n");
	}


	public void paintComponent(Graphics g, int i) {
		currPage = i;
		paintComponent(g);
	}

	@Override
	public boolean containsParent(GeneralObj oldObj) {
		return false;
	}

	@Override
	public void notifyChange(GeneralObj oldObj, GeneralObj clonedObj) {
		
	}

	@Override
	public void updateObj() {
		
	}

	public void moveIfNeeded(int maxW, int maxH) {
		if(tX > maxW)
			tX = maxW-tW;
		if(tY > maxH)
			tY = maxH-tH;
		
	}

	@Override
	public boolean setBoxSelectStatus(int x0, int y0, int x1, int y1) {
		selectStatus = 0;
		if(myPage == currPage && x0 <= tX-4 && x1 >= tX+tW+3)
		{
			
			if(!globalTable && y0 <= tY-tH+2 && y1 >= tY+4)
			{
				selectStatus = 1;
				return true;	
			}
			else if(globalTable && tableVis && y0 <= tY-(tH/col1.size())+2 && y1 >= tY+4+tH-(tH/col1.size()))
			{
				selectStatus = 1;
				return true;	
			}
			return false;
		}			
		else
			return false;
	}
	
	public boolean setBoxSelectStatus(int x, int y)
	{
		xTemp = x;
		yTemp = y;
		if(myPage == currPage && x >= tX-4 && x <= tX+tW+3)
		{
			
			if(!globalTable && y >= tY-tH+2 && y <= tY+4)
				return true;			
			else if(globalTable && tableVis && y >= tY-(tH/col1.size())+2 && y <= tY+4+tH-(tH/col1.size()))
				return true;
			return false;
		}			
		else
			return false;
	}
	
	public void setSelectStatus(boolean b) {
		if(b)
			selectStatus = 1;
		else
			selectStatus = 0;
		
	}

	public Color getColor() {
		return color;
	}
	


}
