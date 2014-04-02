import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.io.BufferedWriter;
import java.io.IOException;
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

public class LoopbackTransitionObj extends TransitionObj  implements Cloneable {

	private int selectStatus = 0;
	
	public Point startPt, endPt, startCtrlPt,endCtrlPt;
	public int startStateIndex,endStateIndex;
	public StateObj state;
	public CubicCurve2D.Double loop;
	private Vector<Point> stateBorderPts;
	private int x,y;
	private boolean ready = false;
	String startS;
	

	
	public static final int NONE = 0;
	public static final int START = 1;
	public static final int STARTCTRL = 2;
	public static final int ENDCTRL = 3;
	public static final int END = 4;
	public static final int ALL = 5;
	public static final int TXT = 6;
	
private int xTemp, yTemp, tempSI,tempEI,lengthS,lengthE;

private double ctrlAngleS, ctrlAngleE;
	
	public LoopbackTransitionObj(int _x, int _y, int numb, int page, Color c)
	{
		objName = "trans" + numb;
		x = _x;
		y = _y;
		myPage = page;
		color = c;
	}
	
	public LoopbackTransitionObj(Point sp, Point ep, Point scp, Point ecp,
			LinkedList<ObjAttribute> newList,
			String name, String start, String end, int sIndex, int eIndex, int page, Color c) {
		startPt = sp;
		endPt = ep;
		startCtrlPt = scp;
		endCtrlPt = ecp;
		startStateIndex = sIndex;
		endStateIndex = eIndex;
		attrib = newList;
		objName = name;
		loop = new CubicCurve2D.Double(startPt.getX(),startPt.getY(),startCtrlPt.getX(),startCtrlPt.getY(),endCtrlPt.getX(),endCtrlPt.getY(),endPt.getX(),endPt.getY());
		ready = true;
		startS = start;
		myPage = page;
		color = c;
	}

	public void initTrans(StateObj _state)
	{
		if(_state != state)
		{
			state = _state;
			setEndPts(x,y);
			loop = new CubicCurve2D.Double(startPt.getX(),startPt.getY(),startCtrlPt.getX(),startCtrlPt.getY(),endCtrlPt.getX(),endCtrlPt.getY(),endPt.getX(),endPt.getY());
			ready = true;
		}			
	}
	
	@SuppressWarnings("unchecked")
	public Object clone () 
    throws CloneNotSupportedException
    {
		LoopbackTransitionObj copy = (LoopbackTransitionObj)super.clone();
		copy.loop = (CubicCurve2D.Double)loop.clone();
		copy.startPt = (Point) startPt.clone();
		copy.endPt = (Point) endPt.clone();
		copy.startCtrlPt = (Point) startCtrlPt.clone();
		copy.endCtrlPt = (Point) endCtrlPt.clone();
		copy.stateBorderPts = (Vector<Point>) stateBorderPts.clone();
        if(attrib != null)
		{
    		copy.attrib = (LinkedList<ObjAttribute>)copy.attrib.clone();
    		for(int i = 0; i < attrib.size(); i++)
    		{
    			copy.attrib.set(i,(ObjAttribute)attrib.get(i).clone());
    		}
		}
		return copy;	
    }
	
	private void setEndPts(int x, int y)
	{
		//find start point on oval closest to click point
		stateBorderPts = state.getBorderPts();
		Point createPt = new Point(x,y);
		double temp;
		double max = 1000000;
		for(int i = 0; i < 36; i++)
		{
			temp = createPt.distanceSq(stateBorderPts.get(i));
			if (temp < max)
			{
				startStateIndex = i;
				max = temp;
			}
		}
		
		// store two points on oval
		startPt = stateBorderPts.get(startStateIndex);
		endStateIndex = startStateIndex + 5;
		if(endStateIndex > 35)
			endStateIndex = endStateIndex - 36;
		endPt = stateBorderPts.get(endStateIndex);
		
		//angle control points are from points on oval
		double angleStart = startStateIndex*2*Math.PI/36;
		double angleEnd = endStateIndex*2*Math.PI/36;
		
		
		//find distance from points on oval to control point
		int dist = (int) Math.sqrt(state.getSize());

		//scaling
		dist = (int) (dist*.65);


		
		//set up control points
		startCtrlPt = new Point();
		endCtrlPt = new Point(); 
		startCtrlPt.setLocation((int) (dist*Math.cos(angleStart)) + startPt.getX(), dist*Math.sin(angleStart) + startPt.getY());
		endCtrlPt.setLocation((int) (dist*Math.cos(angleEnd)) + endPt.getX(), dist*Math.sin(angleEnd) + endPt.getY());
		
		
	}
	
	public void paintComponent(Graphics g) {
		
		if(ready && currPage == myPage)
		{
			
			Graphics2D g2D = (Graphics2D) g;
			g2D.setColor(color);
			g2D.draw(loop);
			
			
			//draw arrow head
			
			// find angle between end point and end control point
			double alpha = getAngle(endCtrlPt,endPt);
	
			
	
			double adj = Math.PI/6;
	        //g2D.drawLine((int)endPt.getX(),(int)endPt.getY(),(int)endPt.getX() + (int)(arrowLength*Math.cos(alpha + adj)),(int)endPt.getY() - (int)(arrowLength*Math.sin(alpha + adj)));
	        //g2D.drawLine((int)endPt.getX(),(int)endPt.getY(),(int)endPt.getX() + (int)(arrowLength*Math.cos(alpha - adj)),(int)endPt.getY() - (int)(arrowLength*Math.sin(alpha - adj)));
			int[] xP = {(int)endPt.getX(),(int)endPt.getX() + (int)(13*Math.cos(alpha + adj)),(int)endPt.getX() + (int)(13*Math.cos(alpha - adj))};
			int[] yP = {(int)endPt.getY(),(int)endPt.getY() - (int)(13*Math.sin(alpha + adj)),(int)endPt.getY() - (int)(13*Math.sin(alpha - adj))};
			g2D.drawPolygon(xP,yP,3);
			g2D.fillPolygon(xP,yP,3);
			
			/*
			int eX = (int)endPt.getX();
			int eY = (int)endPt.getY();
			int dx = (int)endCtrlPt.getX() - eX;
			int dy = eY - (int)endCtrlPt.getY(); 
			double theta = 0;
			if(dx == 0)
			{
				if(dy >= 0)
					theta = Math.PI/2;
				else
					theta = 3*Math.PI/2;
			}
			else if (dx > 0 && dy >= 0)
				theta = Math.atan(dy/dx);
			else if (dx > 0 && dy <= 0)
				theta = 2*Math.PI + Math.atan(dy/dx);
			else if (dx < 0 && dy >= 0)
				theta = Math.PI + Math.atan(dy/dx);
			else if (dx < 0 && dy <= 0)
				theta = Math.PI + Math.atan(dy/dx);
			
			double adj = Math.PI/6;
	        g2D.drawLine(eX,eY,eX + (int)(20*Math.cos(theta + adj)),eY - (int)(20*Math.sin(theta + adj)));
	        g2D.drawLine(eX,eY,eX + (int)(20*Math.cos(theta - adj)),eY - (int)(20*Math.sin(theta - adj)));
			*/
			
	        //draw control points
			if(selectStatus != NONE)
	        {
	            g2D.setColor(Color.red);
	            g2D.fillRect((int)startPt.getX()-3,(int)startPt.getY()-3,7,7);
	            g2D.fillRect((int)endPt.getX()-3,(int)endPt.getY()-3,7,7);
	            g2D.fillRect((int)startCtrlPt.getX()-3,(int)startCtrlPt.getY()-3,7,7);
	            g2D.fillRect((int)endCtrlPt.getX()-3,(int)endCtrlPt.getY()-3,7,7);
	            g2D.drawLine((int)startPt.getX(),(int)startPt.getY(),(int)startCtrlPt.getX(),(int)startCtrlPt.getY());
	            g2D.drawLine((int)endPt.getX(),(int)endPt.getY(),(int)endCtrlPt.getX(),(int)endCtrlPt.getY());
	            
	            
	        }


		}
		
	}

	@Override
	public void adjustShapeOrPosition(int x, int y) {

		if(currPage == myPage)
		{
			if(selectStatus == START)
			{
				Point currPt = new Point(x,y);
				double temp;
				double max = 1000000;
				for(int i = 0; i < 36; i++)
				{
					temp = currPt.distanceSq(stateBorderPts.get(i));
					if (temp < max)
					{
						startStateIndex = i;
						max = temp;
					}
				}
				startPt.setLocation(stateBorderPts.get(startStateIndex).getX(),stateBorderPts.get(startStateIndex).getY());
			}
			if(selectStatus == STARTCTRL)
				startCtrlPt.setLocation(x,y);
			if(selectStatus == ENDCTRL)
				endCtrlPt.setLocation(x,y);
			if(selectStatus == END)
			{
				Point currPt = new Point(x,y);
				double temp;
				double max = 1000000;
				for(int i = 0; i < 36; i++)
				{
					temp = currPt.distanceSq(stateBorderPts.get(i));
					if (temp < max)
					{
						endStateIndex = i;
						max = temp;
					}
				}
				endPt.setLocation(stateBorderPts.get(endStateIndex).getX(),stateBorderPts.get(endStateIndex).getY());
			}
			if(selectStatus == TXT)
			{
				if(attrib != null)
				{
					for (int i = 0; i < attrib.size(); i++)
					{
						ObjAttribute s = attrib.get(i);
						if(s.getSelectStatus() != 0)
						{
							s.adjustShapeOrPosition(x, y);
							break;
						}
					}
				} 
			}
			
			// allow loopback to rotate around state
			if(selectStatus == 5)
			{
				// get number of index transition should move
				Point c = state.getRealCenter(myPage);
				double initAngle = getAngle(new Point(xTemp, yTemp),c);

				double currAngle = getAngle(new Point(x,y),c);				
				int step = (int)-Math.round((currAngle-initAngle)/((2*Math.PI)/36));
					
				if(step != 0)
				{
					startStateIndex = tempSI + step;
					endStateIndex = tempEI + step;
					if(startStateIndex > 35)
						startStateIndex -= 36;
					if(endStateIndex > 35)
						endStateIndex -= 36;
					if(startStateIndex < 0)
						startStateIndex += 36;
					if(endStateIndex < 0)
						endStateIndex += 36;
					
					
					//set up points
					startPt = stateBorderPts.get(startStateIndex);
					endPt = stateBorderPts.get(endStateIndex);
					startCtrlPt.setLocation((int) (lengthS*Math.cos(ctrlAngleS-step*(2*Math.PI/36)) + startPt.getX()), (int)(-lengthS*Math.sin(ctrlAngleS-step*(2*Math.PI/36)) + startPt.getY()));
					endCtrlPt.setLocation((int) (lengthE*Math.cos(ctrlAngleE-step*(2*Math.PI/36)) + endPt.getX()), (int)(-lengthE*Math.sin(ctrlAngleE-step*(2*Math.PI/36)) + endPt.getY()));
				}
			}

			
			loop.setCurve(startPt.getX(),startPt.getY(),startCtrlPt.getX(),startCtrlPt.getY(),endCtrlPt.getX(),endCtrlPt.getY(),endPt.getX(),endPt.getY());
			

				modified = true;
		}
	}

	@Override
	public int getSelectStatus() {
		return selectStatus;
	}

	@Override
	public boolean setSelectStatus(int x, int y) {
		
		if(currPage == myPage)
		{
			if(selectStatus != 5 || !loop.contains(x,y))
			{
				selectStatus = NONE;
				xTemp = x;
				yTemp = y;
				tempSI = startStateIndex;
				tempEI = endStateIndex;
				//ctrlAngleS = getAngle(startCtrlPt,startPt)-(36-startStateIndex)*2*Math.PI/36;
				//ctrlAngleE = getAngle(endCtrlPt,endPt)-(36-endStateIndex)*2*Math.PI/36;
				ctrlAngleS = getAngle(startCtrlPt,startPt);
				ctrlAngleE = getAngle(endCtrlPt,endPt);
				lengthS = (int)Math.round(startCtrlPt.distance(startPt));
				lengthE = (int)Math.round(endCtrlPt.distance(endPt));

			}

			//check for txt
	        if(attrib != null)
			{
				for(int j = 0; j < attrib.size(); j++)
				{
					ObjAttribute s = attrib.get(j);
					s.unselect();
				}
	        	for (int i = 0; i < attrib.size(); i++)
				{
	        		ObjAttribute s = attrib.get(i);
					if(s.setSelectStatus(x,y))
					{
						selectStatus = TXT;
						break;
					}
				}
			}
	        if(selectStatus != TXT)
	        {
				
				
				//check control points
				if(startPt.getX()-x <= 4 && startPt.getX()-x >= -4 && startPt.getY()-y <= 4 && startPt.getY()-y >= -4)
					selectStatus = START;
				if(startCtrlPt.getX()-x <= 4 && startCtrlPt.getX()-x >= -4 && startCtrlPt.getY()-y <= 4 && startCtrlPt.getY()-y >= -4)
		        	selectStatus = STARTCTRL;
		        if(endCtrlPt.getX()-x <= 4 && endCtrlPt.getX()-x >= -4 && endCtrlPt.getY()-y <= 4 && endCtrlPt.getY()-y >= -4)
		        	selectStatus = ENDCTRL;
		        if(endPt.getX()-x <= 4 && endPt.getX()-x >= -4 && endPt.getY()-y <= 4 && endPt.getY()-y >= -4)
		        	selectStatus = END;
				// if not a control point, search around line
		        if(selectStatus == NONE)
			//	for(int i = -4; i < 5; i++)
				//{
					//for(int j = -4; j < 5; j++)
					//{
						if(loop.contains(x,y)) //+i+j
						{
							selectStatus = 5;
							//break;
						}
					//}
				//}
	        }
			if(selectStatus == NONE)
				return false;
			else
				return true;
		}
		else
			return false;
	}
	
	public int getType()
	{
		return 2;
	}
	public boolean isModified()
	{
		if(modified)
			return true;
		else
			return false;
		
	}

	//sets modified back to false
	public void setModifiedFalse()
	{

		modified = false;
	}
	public void setModifiedTrue()
	{
		modified = true;
	}
	
	public void updateObj()
	{
		int newPage = state.getPage();

		//moeve all related objects to new page
		if(newPage != myPage)
		{
			for(int i = 0; i < attrib.size();i++)
			{
				ObjAttribute obj = attrib.get(i);
				if(obj.getPage() == myPage)
					obj.setPage(newPage);
			}
			myPage = state.getPage();
		}
			
		
		if(state.getSelectStatus() != StateObj.TXT)
		{

			double angleS = getAngle(startCtrlPt,startPt);
			double angleE = getAngle(endCtrlPt,endPt);
			int lenS = (int)Math.round(startCtrlPt.distance(startPt));
			int lenE = (int)Math.round(endCtrlPt.distance(endPt));
			stateBorderPts = state.getBorderPts();
			startPt = stateBorderPts.get(startStateIndex);
			endPt = stateBorderPts.get(endStateIndex);
			
			
			startCtrlPt.setLocation((int) (lenS*Math.cos(angleS)) + startPt.getX(), -lenS*Math.sin(angleS) + startPt.getY());
			endCtrlPt.setLocation((int) (lenE*Math.cos(angleE)) + endPt.getX(), -lenE*Math.sin(angleE) + endPt.getY());
			
			
			loop.setCurve(startPt.getX(),startPt.getY(),startCtrlPt.getX(),startCtrlPt.getY(),endCtrlPt.getX(),endCtrlPt.getY(),endPt.getX(),endPt.getY());
		}
	}
	
	public void notifyChange(GeneralObj old, GeneralObj clone) {
		if(old.equals(state))
		{
			state = (StateObj) clone;
			
		}
		
	}
	
	public boolean isParentModified()
	{
		if(modifiedParent)
			return true;
		else
			return false;
		
	}


	
	public void unselect()
	{
		selectStatus = NONE;
	}
	
	public boolean containsParent(GeneralObj oldObj)
	{
		if(oldObj.equals(state))
			return true;
		else
			return false;
	}

	
	public Point getCenter(int page)
	{
		if(!ready)
			return new Point(0,0);
		int large;
		int small;
		int index;
		if(startStateIndex > endStateIndex)
		{
			large = startStateIndex;
			small = endStateIndex;
		}
		else
		{
			small = startStateIndex;
			large = endStateIndex;	
		}
		
		if(large-small > 18)
		{
			index = small-(36-large+small)/2;
			if(index < 0)
				index += 36;
		}
		
		else
			index = (large-small)/2 + small;
		
		
		
		double len = startPt.distance(startCtrlPt);
		Point bord = stateBorderPts.get(index);
		
	
		return new Point((int) (len*Math.cos(index*2*Math.PI/36) + bord.getX()), (int) (len*Math.sin(index*2*Math.PI/36) + bord.getY()));
		
		
			
			
		
	}


	@Override
	public Point getStart() {
		// TODO make better
		return startCtrlPt;
	}
	public StateObj getStartState()
	{
		return state;
	}
	
	
	public void save(BufferedWriter writer) throws IOException {
		
		writer.write("## START LOOPBACK TRANSITION OBJECT\n");
		writer.write("<transition>\n");
		
		writer.write(i(1) + "<attributes>\n");
		for(int i = 0; i < attrib.size(); i++)
		{
			ObjAttribute obj = attrib.get(i);
			obj.save(writer,1);
		}
		writer.write(i(1) + "</attributes>\n");

		writer.write(i(1) + "<startState>\n" + i(1) + state.getName() + "\n" + i(1) + "</startState>\n");
		writer.write(i(1) + "<endState>\n" + i(1) + state.getName() + "\n" + i(1) + "</endState>\n");
		writer.write(i(1) + "<startPtX>\n" + i(1) + startPt.getX() + "\n" + i(1) + "</startPtX>\n");
		writer.write(i(1) + "<startPtY>\n" + i(1) + startPt.getY() + "\n" + i(1) + "</startPtY>\n");
		writer.write(i(1) + "<endPtX>\n" + i(1) + endPt.getX() + "\n" + i(1) + "</endPtX>\n");
		writer.write(i(1) + "<endPtY>\n" + i(1) + endPt.getY() + "\n" + i(1) + "</endPtY>\n");
		writer.write(i(1) + "<startCtrlPtX>\n" + i(1) + startCtrlPt.getX() + "\n" + i(1) + "</startCtrlPtX>\n");
		writer.write(i(1) + "<startCtrlPtY>\n" + i(1) + startCtrlPt.getY() + "\n" + i(1) + "</startCtrlPtY>\n");
		writer.write(i(1) + "<endCtrlPtY>\n" + i(1) + endCtrlPt.getX() + "\n" + i(1) + "</endCtrlPtY>\n");
		writer.write(i(1) + "<endCtrlPtY>\n" + i(1) + endCtrlPt.getY() + "\n" + i(1) + "</endCtrlPtY>\n");
		writer.write(i(1) + "<startStateIndex>\n" + i(1) + startStateIndex + "\n" + i(1) + "</startStateIndex>\n");
		writer.write(i(1) + "<endStateIndex>\n" + i(1) + endStateIndex + "\n" + i(1) + "</endStateIndex>\n");
		writer.write(i(1) + "<page>\n" + i(1) + myPage + "\n" + i(1) + "</page>\n");
		writer.write(i(1) + "<color>\n" + i(1) + color.getRGB() + "\n" + i(1) + "</color>\n");

		
		
		writer.write("</transition>\n");
		writer.write("## START LOOPBACK TRANSITION OBJECT\n");
	}

	public void makeConnections(Vector<Object> objList)
	{
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj obj = (GeneralObj)objList.get(i);
			if(obj.getType() == 0)
			{
				if(obj.getName().equals(startS))
					state = (StateObj) obj;		
			}
		}
		stateBorderPts = state.getBorderPts();

	}

	@Override
	public boolean setBoxSelectStatus(int x0, int y0, int x1, int y1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateObjPages(int page) {
		myPage = page;
		if(attrib != null)
		{
			for(int i = 0; i < attrib.size(); i++)
			{
				ObjAttribute obj = attrib.get(i);
				obj.setPage(page);
			}
		}
		
		
	}

	

	
}
