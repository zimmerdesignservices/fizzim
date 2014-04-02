import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
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

public class StateTransitionObj extends TransitionObj  implements Cloneable {

	private int selectStatus = 0;
	public Point startPt, endPt, startCtrlPt,endCtrlPt;
	public int startStateIndex, endStateIndex;
	public StateObj startState = null, endState = null, oldState;
	public CubicCurve2D.Double curve;
	private Vector<Point> startBorderPts;
	private Vector<Point> endBorderPts;
	private boolean ready = false;
	int xTemp, yTemp;
	String startS, endS;
	int sPage,ePage;
	
	private boolean stub = false;
	

	
	public static final int NONE = 0;
	public static final int START = 1;
	public static final int STARTCTRL = 2;
	public static final int ENDCTRL = 3;
	public static final int END = 4;
	public static final int ALL = 5;
	public static final int TXT = 6;
	public static final int PAGES = 7;
	public static final int PAGESC = 8;
	public static final int PAGEEC = 9;
	public static final int PAGEE = 10;
	
	//pages
	public Point pageS, pageSC, pageE, pageEC;
	DrawArea drawArea;
	
	//used for calculating if redraw is needed
	Point tempStartPt;
	Point tempEndPt;
	int tempStartIndex;
	int tempEndIndex;
	int sdx;
	int sdy;
	int edx;
	int edy;
	
	//for stubs
	int len;
	double angle;
	
	

	
	public StateTransitionObj(int numb, int page, DrawArea da, Color c)
	{
		objName = "trans" + numb;
		myPage = page;
		drawArea = da;
		pageS = new Point(0,0);
		pageE = new Point(0,0);
		pageSC = new Point(0,0);
		pageEC = new Point(0,0);
		color = c;
	}
	
	public StateTransitionObj(int numb, int page, DrawArea da, StateObj start, StateObj end, Color c)
	{
		objName = "trans" + numb;
		myPage = page;
		drawArea = da;
		pageS = new Point(0,0);
		pageE = new Point(0,0);
		pageSC = new Point(0,0);
		pageEC = new Point(0,0);
		startPt = new Point(0,0);
		endPt = new Point(0,0);
		startCtrlPt = new Point(0,0);
		endCtrlPt = new Point(0,0);
		curve = new CubicCurve2D.Double();
		color = c;
		
	}
	
	public StateTransitionObj(Point sp, Point ep, Point scp, Point ecp,
			LinkedList<ObjAttribute> newList,
			String name,String start,String end, int sIndex, int eIndex, 
			int page, Point ps, Point psc, Point pe, Point pec, DrawArea da, boolean s, Color c) {
		startPt = sp;
		endPt = ep;
		startCtrlPt = scp;
		endCtrlPt = ecp;
		startStateIndex = sIndex;
		endStateIndex = eIndex;
		attrib = newList;
		objName = name;
		curve = new CubicCurve2D.Double(startPt.getX(),startPt.getY(),startCtrlPt.getX(),startCtrlPt.getY(),endCtrlPt.getX(),endCtrlPt.getY(),endPt.getX(),endPt.getY());
		ready = true;
		startS = start;
		endS = end;
		myPage = page;
		pageS = ps;
		pageSC = psc;
		pageE = pe;
		pageEC = pec;
		drawArea = da;
		stub = s;
		color = c;

		
		
	}
	
	public void setStub(boolean b)
	{
		stub = b;
		setEndPts();

	}
	
	public boolean getStub()
	{
		return stub;
	}
	
	public void makeConnections(Vector<Object> objList)
	{
		for(int i = 1; i < objList.size(); i++)
		{
			GeneralObj obj = (GeneralObj)objList.get(i);
			if(obj.getType() == 0)
			{
				if(obj.getName().equals(startS))
				{
					startState = (StateObj) obj;
				}
				if(obj.getName().equals(endS))
				{
					endState = (StateObj) obj;	
				}
			}
		}
		startBorderPts = startState.getBorderPts();
		endBorderPts = endState.getBorderPts();
		sPage = startState.getPage();
		ePage = endState.getPage();
		
		if(stub)
		{
			angle = getAngle(startPt,startState.getRealCenter(myPage));
			len = (int) startPt.distance(pageS);
		}
		
		
	}
    
	public void initTrans(StateObj start, StateObj end)
	{
		if(startState != start || endState != end)
		{
			startState = start;
			endState = end;
			setEndPts();
			ready = true;
		}			
		if(sPage != ePage)
			drawArea.pageConnUpdate(startState,endState);
	}
	
	@SuppressWarnings("unchecked")
	public Object clone () 
    throws CloneNotSupportedException
    {
		StateTransitionObj copy = (StateTransitionObj)super.clone();
		
		copy.startPt = (Point) startPt.clone();
		copy.pageS = (Point) pageS.clone();
		copy.curve = (CubicCurve2D.Double)curve.clone();
		copy.endPt = (Point) endPt.clone();
		copy.startCtrlPt = (Point) startCtrlPt.clone();
		copy.endCtrlPt = (Point) endCtrlPt.clone();
		copy.pageSC = (Point) pageSC.clone();
		copy.pageE = (Point) pageE.clone();
		copy.pageEC = (Point) pageEC.clone();
		copy.startBorderPts = (Vector<Point>) startBorderPts.clone();
		copy.endBorderPts = (Vector<Point>) endBorderPts.clone();

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
	

	

	public void setEndPts()
	{
		startBorderPts = startState.getBorderPts();
		endBorderPts = endState.getBorderPts();
		sPage = startState.getPage();
		ePage = endState.getPage();
		//TODO page connector stacking
		if(sPage != ePage && !stub)
		{

			int sOffset = drawArea.getOffset(sPage,startState, this, "start");
			int eOffset = drawArea.getOffset(ePage,endState, this, "end");
			int sBorderOffset = sOffset/20;
			if(sBorderOffset < 0)
				sBorderOffset += 36;
			if(sBorderOffset > 35)
				sBorderOffset -= 36;
				
			int eBorderOffset = -eOffset/20;
			if(eBorderOffset < -18)
				eBorderOffset += 36;
			if(eBorderOffset > 17)
				eBorderOffset -= 36;
			
			startPt = startBorderPts.get(0 + sBorderOffset);
			startCtrlPt = new Point((int)startPt.getX()+20,(int)startPt.getY());
			endPt = endBorderPts.get(18 + eBorderOffset);
			endCtrlPt = new Point((int)endPt.getX()-20,(int)endPt.getY());
			
			pageS = new Point(drawArea.getMaxW()-50,(int)startPt.getY() + sOffset);
			pageSC = new Point(drawArea.getMaxW()-70,(int)startPt.getY() + sOffset);
			pageE = new Point(50,(int)endPt.getY() + eOffset);
			pageEC = new Point(70,(int)endPt.getY() + eOffset);
			curve = new CubicCurve2D.Double();
		}
		else if(stub)
		{
			//TODO
			//System.out.println("in stub loop, setendpts");
			startPt = startBorderPts.get(0);
			startStateIndex = 0;
			pageS = new Point((int)startPt.getX()+60,(int)startPt.getY());
			len = 60;
			angle = 0;			
		}
		else
		{
			Point startCoords = startState.getRealCenter(myPage);
			Point endCoords = endState.getRealCenter(myPage);
			double temp;
			double max = 1000000;
			for(int i = 0; i < 36; i++)
			{
				temp = startCoords.distanceSq(endBorderPts.get(i));
				if (temp < max)
				{
					endStateIndex = i;
					max = temp;
				}
			}
			max = 1000000;
			for(int i = 0; i < 36; i++)
			{
				temp = endCoords.distanceSq(startBorderPts.get(i));
				if (temp < max)
				{
					startStateIndex = i;
					max = temp;
				}
			}
			//try to prevent overlapping
			if(startCoords.getX()<endCoords.getX())
			{
				if(startCoords.getY()<endCoords.getY())
				{
					
				}
				else
				{
					
				}
			}
			else
			{
				if(startCoords.getY()<endCoords.getY())
				{
					
				}
				else
				{
					
				}
			}
			startStateIndex-=1;
			if(startStateIndex==-1)
				startStateIndex=35;
			endStateIndex+=1;
			if(endStateIndex==36)
				endStateIndex=0;
			
			startPt = startBorderPts.get(startStateIndex);
			endPt = endBorderPts.get(endStateIndex);
			
			
			//SELECTION OF CONTROL POINTS
			
			// distances between states
			int dx = (int) startState.getRealCenter(myPage).getX() - (int) endState.getRealCenter(myPage).getX();
			int dy = (int) startState.getRealCenter(myPage).getY() - (int) endState.getRealCenter(myPage).getY();
			
			//scaling for distance of control point from start or end point
			int dxs = (int) (endPt.getX() - startPt.getX()) / 3;
			int dys = (int) (endPt.getY() - startPt.getY()) / 3;
			if(dxs < 0)
				dxs = -dxs;
			if(dys < 0)
				dys = -dys;
			
			//find angle between states
			double theta = 0;
			if(dx == 0)
			{
				if(dy <= 0)
					theta = Math.PI/2;
				else
					theta = 3*Math.PI/2;
			}
			else if (dx > 0 && dy > 0)
				theta = 2*Math.PI - Math.atan((double) dy/dx);
			else if (dx > 0 && dy <= 0)
			{
				if(dy == 0)
					theta = 0;
				else
					theta = -Math.atan((double) dy/(dx));	
			}
			else if (dx < 0)
				theta = Math.PI - Math.atan((double) dy/dx);
	
			//determine angle away from start point for start control point
			double adj = Math.PI/6;
			double angleStart = 0;
			if(dx >= 0 && dy >= 0)
				angleStart = -Math.PI + theta + adj;//+
	
			else if(dx >= 0 && dy < 0)
				angleStart = Math.PI + theta + adj;//+
	
			else if(dx < 0 && dy >= 0)
				angleStart = -Math.PI + theta + adj;
			
			else if(dx < 0 && dy < 0)
				angleStart = Math.PI + theta + adj;
	
			//determine angle away from end point for end control point
			double angleEnd = 0;
			if(dx >= 0)
				angleEnd = theta - adj;
			else if(dx < 0)
				angleEnd = theta - adj;
			
			//create control point
			startCtrlPt = new Point((int) (startPt.getX()+ (Math.cos(angleStart)*dxs)),(int)(startPt.getY()-(Math.sin(angleStart)*dys)));
			endCtrlPt = new Point((int) (endPt.getX()+ (Math.cos(angleEnd)*dxs)),(int)(endPt.getY()-(Math.sin(angleEnd)*dys))); 
			
			curve = new CubicCurve2D.Double(startPt.getX(),startPt.getY(),startCtrlPt.getX(),startCtrlPt.getY(),endCtrlPt.getX(),endCtrlPt.getY(),endPt.getX(),endPt.getY());
			
		}
		
	}	
	
	//will only auto-redraw under certain conditions
	private void moveEndPts()
	{
		startBorderPts = startState.getBorderPts();
		endBorderPts = endState.getBorderPts();
		sPage = startState.getPage();
		ePage = endState.getPage();
		
		if(sPage != ePage && !stub)
		{
			
			int sOffset = drawArea.getOffset(sPage,startState, this, "start");
			int eOffset = drawArea.getOffset(ePage,endState, this, "end");
			
			int sBorderOffset = sOffset/20;
			if(sBorderOffset < 0)
				sBorderOffset += 36;
			if(sBorderOffset > 35)
				sBorderOffset -= 36;
				
			int eBorderOffset = -eOffset/20;
			if(eBorderOffset < -18)
				eBorderOffset += 36;
			if(eBorderOffset > 17)
				eBorderOffset -= 36;
			startPt = startBorderPts.get(0 + sBorderOffset);
			startCtrlPt = new Point((int)startPt.getX()+20,(int)startPt.getY());
			endPt = endBorderPts.get(18 + eBorderOffset);
			endCtrlPt = new Point((int)endPt.getX()-20,(int)endPt.getY());
			
			pageS = new Point(drawArea.getMaxW()-50,(int)startPt.getY() + sOffset);
			pageSC = new Point(drawArea.getMaxW()-70,(int)startPt.getY() + sOffset);
			pageE = new Point(50,(int)endPt.getY() + eOffset);
			pageEC = new Point(70,(int)endPt.getY() + eOffset);
		}
		else if(stub)
		{
			//TODO
			//System.out.println("in stub loop, moveendpts");
			startPt = startBorderPts.get(startStateIndex);
			pageS.setLocation(new Point((int)(startPt.getX()+len*Math.cos(angle)),(int)(startPt.getY()-len*Math.sin(angle))));
		}
		else
		{					

			//or if multiple states selected, dont need to recalculate
			if((!recalcCheck()&&!drawArea.getRedraw()) || (startState.getSelectStatus() != 0 && endState.getSelectStatus() != 0))
			{
				startStateIndex = tempStartIndex;
				endStateIndex = tempEndIndex;
				startPt = startBorderPts.get(startStateIndex);
				endPt = endBorderPts.get(endStateIndex);
				//if movement hasnt left the start quadrant, don't recalulate all points
				startCtrlPt = new Point((int)startPt.getX()+sdx,(int)startPt.getY()+sdy);
				endCtrlPt = new Point((int)endPt.getX()+edx,(int)endPt.getY()+edy);
			}
			else
				setEndPts();

			
		}
		
	}	
	
	@Override
	public void paintComponent(Graphics g) {
		
		
		//check if something needs to be painted at all
		if(ready)
		{
			sPage = startState.getPage();
			myPage = sPage;
			ePage = endState.getPage();
		}
		//check if anything needs to be drawn
		if(ready && (sPage == currPage || ePage == currPage))
		{
			Graphics2D g2D = (Graphics2D) g;
				
				

		    g2D.setColor(color);
			
			//draw arrow head for non-stub transitions
			if(currPage == ePage && !stub)
			{
				// find angle between end point and end control point
				int dx = (int) endCtrlPt.getX() - (int) endPt.getX();
				int dy = (int) endCtrlPt.getY() - (int) endPt.getY();
				double alpha = 0;
				if(dx == 0)
				{
					if(dy <= 0)
						alpha = Math.PI/2;
					else
						alpha = 3*Math.PI/2;
				}
				else if (dx > 0 && dy > 0)
					alpha = 2*Math.PI - Math.atan((double) dy/dx);
				else if (dx > 0 && dy <= 0)
				{
					if(dy == 0)
						alpha = 0;
					else
						alpha = -Math.atan((double) dy/(dx));	
				}
				else if (dx < 0)
					alpha = Math.PI - Math.atan((double) dy/dx);
			
				double adj = Math.PI/6;
			    int[] xP = {(int)endPt.getX(),(int)endPt.getX() + (int)(13*Math.cos(alpha + adj)),(int)endPt.getX() + (int)(13*Math.cos(alpha - adj))};
				int[] yP = {(int)endPt.getY(),(int)endPt.getY() - (int)(13*Math.sin(alpha + adj)),(int)endPt.getY() - (int)(13*Math.sin(alpha - adj))};
				g2D.drawPolygon(xP,yP,3);
				g2D.fillPolygon(xP,yP,3);
			}
			
			//draw stub
			if(currPage == sPage && stub)
			{
				g2D.drawLine((int)startPt.getX(),(int)startPt.getY(),(int)pageS.getX(),(int)pageS.getY());
				int x = (int)pageS.getX();
				int y = (int)pageS.getY();
				double cos = Math.cos(angle);
				double sin = Math.sin(angle);

				g2D.drawLine(x-(int)Math.round(6*sin+7*cos), y-(int)Math.round(6*cos-7*sin), x, y);
				g2D.drawLine(x,y,x+(int)Math.round(6*sin-7*cos), y+(int)Math.round(6*cos+7*sin));

				FontMetrics fm = g2D.getFontMetrics();
				int width = fm.stringWidth(endState.getName());
				int height = fm.getHeight();
				g2D.drawString(endState.getName(),(int)(pageS.getX()+(12+width/2)*Math.cos(angle)-width/2),(int)(pageS.getY()-12*Math.sin(angle)+height/3));				
			
				//draw control points if needed
				if(selectStatus != NONE)
		        {
					g2D.setColor(Color.red);
		            g2D.fillRect((int)startPt.getX()-3,(int)startPt.getY()-3,7,7);
		            g2D.fillRect((int)pageS.getX()-3,(int)pageS.getY()-3,7,7);
		            g2D.setColor(color);
		        }
				
			}
			
			//draw normal transition
			if(sPage == ePage && !stub)
			{
				g2D.draw(curve);

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
			        g2D.setColor(color);
		        }
			}
			//draw page connector
			if(sPage != ePage && !stub)
			{
				//if one start page
				if(sPage == currPage)
				{
					curve.setCurve(startPt.getX(),startPt.getY(),startCtrlPt.getX(),startCtrlPt.getY(),pageSC.getX(),pageSC.getY(),pageS.getX(),pageS.getY());
					int x = (int)pageS.getX();
					int y = (int)pageS.getY();
					g2D.drawLine(x, y, x, y+10);
					g2D.drawLine(x, y+10, x+30, y+10);
					g2D.drawLine(x+30, y+10, x+40, y);
					g2D.drawLine(x+40, y, x+30, y-10);
					g2D.drawLine(x+30, y-10, x, y-10);
					g2D.drawLine(x, y-10, x, y);
					
					FontMetrics fm = g2D.getFontMetrics();
					String pageName = drawArea.getPageName(endState.getPage());
					String text = endState.getName() + " (" + pageName + ")";
					int tW = fm.stringWidth(text);
					if(tW > 40)
						g2D.drawString(text,x+40-tW,y+25);
					else
						g2D.drawString(text,x,y+25);
					
					//draw control points if needed
					if(selectStatus != NONE)
			        {
			            g2D.setColor(Color.red);
			            g2D.fillRect((int)startPt.getX()-3,(int)startPt.getY()-3,7,7);
			            g2D.fillRect((int)pageS.getX()-3,(int)pageS.getY()-3,7,7);
			            g2D.fillRect((int)startCtrlPt.getX()-3,(int)startCtrlPt.getY()-3,7,7);
			            g2D.fillRect((int)pageSC.getX()-3,(int)pageSC.getY()-3,7,7);
			            g2D.drawLine((int)startPt.getX(),(int)startPt.getY(),(int)startCtrlPt.getX(),(int)startCtrlPt.getY());
			            g2D.drawLine((int)pageS.getX(),(int)pageS.getY(),(int)pageSC.getX(),(int)pageSC.getY());	
			        }
					g2D.setColor(color);
					g2D.draw(curve);	
				}
				//in on end page
				else if(ePage == currPage)	
				{	
					curve.setCurve(pageE.getX(),pageE.getY(),pageEC.getX(),pageEC.getY(),endCtrlPt.getX(),endCtrlPt.getY(),endPt.getX(),endPt.getY());
					int x = (int)pageE.getX()-40;
					int y = (int)pageE.getY();
					g2D.drawLine(x, y, x, y+10);
					g2D.drawLine(x, y+10, x+30, y+10);
					g2D.drawLine(x+30, y+10, x+40, y);
					g2D.drawLine(x+40, y, x+30, y-10);
					g2D.drawLine(x+30, y-10, x, y-10);
					g2D.drawLine(x, y-10, x, y);
					
					g2D.drawString(startState.getName() + " (" + drawArea.getPageName(startState.getPage()) + ")",x,y+25);
					
					//control points if needed
					if(selectStatus != NONE)
			        {
			            g2D.setColor(Color.red);
			            g2D.fillRect((int)endPt.getX()-3,(int)endPt.getY()-3,7,7);
			            g2D.fillRect((int)pageE.getX()-3,(int)pageE.getY()-3,7,7);
			            g2D.fillRect((int)endCtrlPt.getX()-3,(int)endCtrlPt.getY()-3,7,7);
			            g2D.fillRect((int)pageEC.getX()-3,(int)pageEC.getY()-3,7,7);
			            g2D.drawLine((int)endPt.getX(),(int)endPt.getY(),(int)endCtrlPt.getX(),(int)endCtrlPt.getY());
			            g2D.drawLine((int)pageE.getX(),(int)pageE.getY(),(int)pageEC.getX(),(int)pageEC.getY());	
			        }	
					g2D.setColor(color);
					g2D.draw(curve);	
				}							
			}
		}
	}
	
	public void unselect()
	{
		selectStatus = NONE;
	}

	@Override
	public void adjustShapeOrPosition(int x, int y) {
		if(selectStatus == START)
		{
			Point currPt = new Point(x,y);
			double temp;
			double max = 1000000;
			for(int i = 0; i < 36; i++)
			{
				temp = currPt.distanceSq(startBorderPts.get(i));
				if (temp < max)
				{	
					startStateIndex = i;
					max = temp;
				}
			}
			startPt.setLocation(startBorderPts.get(startStateIndex).getX(),startBorderPts.get(startStateIndex).getY());
			
			if(stub)
			{
				angle = getAngle(startPt,startState.getRealCenter(myPage));
				pageS.setLocation(new Point((int)(startPt.getX()+len*Math.cos(angle)),(int)(startPt.getY()-len*Math.sin(angle))));
			}
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
				temp = currPt.distanceSq(endBorderPts.get(i));
				if (temp < max)
				{
					endStateIndex = i;
					max = temp;
				}
			}
			endPt.setLocation(endBorderPts.get(endStateIndex).getX(),endBorderPts.get(endStateIndex).getY());
		}
		if(selectStatus == PAGES)
		{
			pageS.setLocation(pageS.getX() + x - xTemp,pageS.getY() + y - yTemp);
			xTemp = x;
			yTemp = y;
			if(stub)
			{
				angle = getAngle(pageS,startState.getRealCenter(myPage));
				int index = 36 - (int)Math.round((angle/(Math.PI*2))*36);
				if(index > 35)
					index -= 36;
				
				startStateIndex = index;
				startPt = startBorderPts.get(index);
				len = (int) startPt.distance(pageS);
			}
		}
		if(selectStatus == PAGESC)
			pageSC.setLocation(x,y);
		if(selectStatus == PAGEEC)
			pageEC.setLocation(x,y);
		if(selectStatus == PAGEE)
		{
			pageE.setLocation(pageE.getX() + x - xTemp,pageE.getY() + y - yTemp);
			xTemp = x;
			yTemp = y;
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
		
		if(startState.getPage() == endState.getPage())
			curve.setCurve(startPt.getX(),startPt.getY(),startCtrlPt.getX(),startCtrlPt.getY(),endCtrlPt.getX(),endCtrlPt.getY(),endPt.getX(),endPt.getY());

		if(selectStatus != ALL)
			modified = true;
	}

	@Override
	public int getSelectStatus() {
		return selectStatus;
	}

	@Override
	public boolean setSelectStatus(int x, int y) {
		
		selectStatus = NONE;
		xTemp = x;
		yTemp = y;
		//check for text
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
        	if(!stub)
        	{
        		if(startCtrlPt.getX()-x <= 3 && startCtrlPt.getX()-x >= -3 && startCtrlPt.getY()-y <= 3 && startCtrlPt.getY()-y >= -3)
    	        	selectStatus = STARTCTRL;
    	        if(endCtrlPt.getX()-x <= 3 && endCtrlPt.getX()-x >= -3 && endCtrlPt.getY()-y <= 3 && endCtrlPt.getY()-y >= -3)
    	        	selectStatus = ENDCTRL;
    	        if(endPt.getX()-x <= 3 && endPt.getX()-x >= -3 && endPt.getY()-y <= 3 && endPt.getY()-y >= -3)
    	        	selectStatus = END;
    	        if(pageSC.getX()-x <= 3 && pageSC.getX()-x >= -3 && pageSC.getY()-y <= 3 && pageSC.getY()-y >= -3)
    	        	selectStatus = PAGESC;
    	        if(pageEC.getX()-x <= 3 && pageEC.getX()-x >= -3 && pageEC.getY()-y <= 3 && pageEC.getY()-y >= -3)
    	        	selectStatus = PAGEEC;
    	        if(pageE.getX()-x <= 3 && pageE.getX()-x >= -3 && pageE.getY()-y <= 3 && pageE.getY()-y >= -3)
    	        	selectStatus = PAGEE;
    	        //check page connecter icon
    	        if(pageS.getX()-x <= 0 && pageS.getX()-x >= -40 && pageS.getY()-y <= 10 && pageS.getY()-y >= -10)
    	        	selectStatus = PAGES;
    	        if(pageE.getX()-x <= 40 && pageE.getX()-x >= 0 && pageE.getY()-y <= 10 && pageE.getY()-y >= -10)
    	        	selectStatus = PAGEE;
        	}
			if(startPt.getX()-x <= 3 && startPt.getX()-x >= -3 && startPt.getY()-y <= 3 && startPt.getY()-y >= -3)
				selectStatus = START;
	        if(pageS.getX()-x <= 3 && pageS.getX()-x >= -3 && pageS.getY()-y <= 3 && pageS.getY()-y >= -3)
	        	selectStatus = PAGES;
	        
			// if not a control point, search around line
	        if(selectStatus == NONE)
	        {
		/*	for(int i = -4; i < 5; i++)
			{
				for(int j = -4; j < 5; j++)
				{
					if(curve.contains(x+i,y+j))
					{
						selectStatus = 5;
						break;
					}
				}
			}*/

	        	   BasicStroke bs = new BasicStroke(10); // sets width of transition "grab handle" lines
	        	   if(!stub)
	        	   {
	        		   if(bs.createStrokedShape(curve).contains(new Point2D.Double(x,y)))
	        			   selectStatus = 5;
	        	   }
	        	   else
	        	   {
	        		   if(bs.createStrokedShape(new Line2D.Double(startPt,pageS)).contains(new Point2D.Double(x,y)))
		        		   selectStatus = 5;
	        	   }
	        }	

	    }
		if(selectStatus == NONE)
			return false;
		else
			return true;
	}
	
	



	public int getType()
	{
		return 1;
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
		int oldS = sPage;
		int oldE = ePage;
		
		sPage = startState.getPage();
		myPage = sPage;
		ePage = endState.getPage();
		
		if(oldS != sPage && oldS != oldE)
		{
			for(int i = 0; i < attrib.size();i++)
			{
				ObjAttribute obj = attrib.get(i);
				if(obj.getPage() == oldS)
					obj.setPage(sPage);
			}
		}
		if(oldE != ePage && oldS != oldE)
		{
			for(int i = 0; i < attrib.size();i++)
			{
				ObjAttribute obj = attrib.get(i);
				if(obj.getPage() == oldE)
					obj.setPage(ePage);
			}
		}
		if(sPage != ePage && oldS == oldE)
		{
			for(int i = 0; i < attrib.size();i++)
			{
				ObjAttribute obj = attrib.get(i);
					obj.setPage(sPage);
			}
		}


		if((startState.getSelectStatus() != StateObj.TXT && startState.getSelectStatus() != StateObj.NONE)
				|| (endState.getSelectStatus() != StateObj.TXT && endState.getSelectStatus() != StateObj.NONE))
		{
			if(oldS != oldE && sPage == ePage)
				setEndPts();
			else
				moveEndPts();
			curve.setCurve(startPt.getX(),startPt.getY(),startCtrlPt.getX(),startCtrlPt.getY(),endCtrlPt.getX(),endCtrlPt.getY(),endPt.getX(),endPt.getY());
		}

	}
	
	public void notifyChange(GeneralObj old, GeneralObj clone) {
		if(old.equals(startState))
		{
			startState = (StateObj) clone;
		}
		if(old.equals(endState))
		{
			endState = (StateObj) clone;
		}
		
	}
	
	public void setPage(int i)
	{
		if(selectStatus == TXT)
		{
			if(attrib != null)
			{
				for (int j = 0; j < attrib.size(); j++)
				{
					ObjAttribute s = attrib.get(j);
					if(s.getSelectStatus() != 0)
					{
						s.setPage(i);
						break;
					}
				}
			} 
		}
		else
		{
			super.setPage(i);
		}
	}
	
	public boolean containsParent(GeneralObj oldObj)
	{
		if(oldObj.equals(startState) || oldObj.equals(endState))
			return true;
		else
			return false;
	}

	
	public Point getCenter(int page)
	{
		if(!ready)
			return new Point(0,0);
		sPage = startState.getPage();
		myPage = sPage;
		ePage = endState.getPage();
		
		if(sPage == ePage)
		{
			if(!stub)
			{
				int x = (int) ((endPt.getX() - startPt.getX())/2 + startPt.getX());
				int y = (int) ((endPt.getY() - startPt.getY())/2 + startPt.getY());
				return new Point(x,y);
			}
			else
			{
				return new Point((int)(startPt.getX()+len*.5*Math.cos(angle)),(int)(startPt.getY()-len*.5*Math.sin(angle)));
			}
		}
		else
		{
			if(page == sPage)
			{
				int x = (int) ((pageS.getX() - startPt.getX())/2 + startPt.getX());
				int y = (int) ((pageS.getY() - startPt.getY())/2 + startPt.getY());
				return new Point(x,y);
			}
			else
			{
				int x = (int) ((pageE.getX() - endPt.getX())/2 + endPt.getX());
				int y = (int) ((pageE.getY() - endPt.getY())/2 + endPt.getY());
				return new Point(x,y);
			}
		}
	}



	@Override
	public Point getStart() {
		return getCenter(myPage);
	}
	
	public StateObj getStartState()
	{
		return startState;
	}
	public StateObj getEndState()
	{
		return endState;
	}

	@Override
	public void save(BufferedWriter writer) throws IOException {
		
		writer.write("## START STATE TRANSITION OBJECT\n");
		writer.write("<transition>\n");
		
		writer.write(i(1) + "<attributes>\n");
		for(int i = 0; i < attrib.size(); i++)
		{
			ObjAttribute obj = attrib.get(i);
			obj.save(writer,1);
		}
		writer.write(i(1) + "</attributes>\n");

		writer.write(i(1) + "<startState>\n" + i(1) + startState.getName() + "\n" + i(1) + "</startState>\n");
		writer.write(i(1) + "<endState>\n" + i(1) + endState.getName() + "\n" + i(1) + "</endState>\n");
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
		writer.write(i(1) + "<pageSX>\n" + i(1) + pageS.getX() + "\n" + i(1) + "</pageSX>\n");
		writer.write(i(1) + "<pageSY>\n" + i(1) + pageS.getY() + "\n" + i(1) + "</pageSY>\n");
		writer.write(i(1) + "<pageSCX>\n" + i(1) + pageSC.getX() + "\n" + i(1) + "</pageSCX>\n");
		writer.write(i(1) + "<pageSCY>\n" + i(1) + pageSC.getY() + "\n" + i(1) + "</pageSCY>\n");
		writer.write(i(1) + "<pageEX>\n" + i(1) + pageE.getX() + "\n" + i(1) + "</pageEX>\n");
		writer.write(i(1) + "<pageEY>\n" + i(1) + pageE.getY() + "\n" + i(1) + "</pageEY>\n");
		writer.write(i(1) + "<pageECX>\n" + i(1) + pageEC.getX() + "\n" + i(1) + "</pageECX>\n");
		writer.write(i(1) + "<pageECY>\n" + i(1) + pageEC.getY() + "\n" + i(1) + "</pageECY>\n");
		writer.write(i(1) + "<stub>\n" + i(1) + stub + "\n" + i(1) + "</stub>\n");
		
		
		writer.write("</transition>\n");
		writer.write("## END STATE TRANSITION OBJECT\n");
	}

	public int getEPage() {		
		return ePage;
	}

	public int getSPage() {
		return sPage;
	}

	public void decrementSPage() {
		sPage = sPage - 1;
		myPage = sPage;
		for(int i = 0; i < attrib.size();i++)
		{
			ObjAttribute obj = attrib.get(i);
			if(obj.getPage() == sPage+1)
				obj.setPage(sPage);
		}
	}

	public void decrementEPage() {
		ePage = ePage - 1;
		for(int i = 0; i < attrib.size();i++)
		{
			ObjAttribute obj = attrib.get(i);
			if(obj.getPage() == ePage+1)
				obj.setPage(ePage);
		}
	}
	
	public void setParentModified(boolean b)
	{
		super.setParentModified(b);
	    if(b)
		{
			tempStartPt = (Point) startPt.clone();
			tempEndPt = (Point) endPt.clone();
			tempStartIndex = startStateIndex;
			tempEndIndex = endStateIndex;
			sdx = (int)startCtrlPt.getX()-(int)startPt.getX();
			sdy = (int)startCtrlPt.getY()-(int)startPt.getY();
			edx = (int)endCtrlPt.getX()-(int)endPt.getX();
			edy = (int)endCtrlPt.getY()-(int)endPt.getY();
		}

	}
	
	private boolean recalcCheck()
	{
		double dx1 = tempStartPt.getX()-tempEndPt.getX();
		double dy1 = tempStartPt.getY()-tempEndPt.getY();
		double dx2 = startPt.getX()-endPt.getX();
		double dy2 = startPt.getY()-endPt.getY();
		if((dx1>=0&&dx2>=-20 || dx1<0&&dx2<20) && (dy1>=0&&dy2>=-20 || dy1<0&&dy2<20))
			return false;
		else
			return true;


	}
	
	public boolean pageConnectorExists(int page, StateObj state, String type)
	{
		//force redraw for all connecors
		
		
		if((sPage != ePage) && 
				((startState.equals(state) && sPage == page && type.equals("start")) || 
						(endState.equals(state) && ePage == page && type.equals("end"))))
			return true;
		else
			return false;
	}
	
	public boolean setBoxSelectStatus(int x0, int y0, int x1, int y1) {
		return false;
	}

	@Override
	public void updateObjPages(int page) {
		if(startState.getPage() == endState.getPage())
		{
			if(attrib != null)
			{
				for(int i = 0; i < attrib.size(); i++)
				{
					ObjAttribute obj = attrib.get(i);
					obj.setPage(page);
				}
			}
			myPage = sPage = ePage = page;
			//updateObj();
		}
		else
			updateObj();
		
	}
	

	
}



