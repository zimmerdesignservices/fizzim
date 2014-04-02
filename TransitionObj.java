import java.awt.Color;
import java.awt.Point;
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

public abstract class TransitionObj extends GeneralObj implements Cloneable {




	public abstract void setModifiedTrue();
	
	public abstract void updateObj();
	
	public Object clone () 
    throws CloneNotSupportedException
   {
       return super.clone();
   }

public abstract StateObj getStartState();
public StateObj getEndState()
{
	return null;
}
public void initTrans(StateObj start, StateObj end){}
public void initTrans(StateObj start){}

public abstract void makeConnections(Vector<Object> objList);

public double getAngle(Point outer, Point inner)
{
	int dx = (int) outer.getX() - (int) inner.getX();
	int dy = -((int) outer.getY() - (int) inner.getY());
	double alpha = 0;
	if(dx == 0)
	{
		if(dy <= 0)
			alpha = 3*Math.PI/2;
		else
			alpha = Math.PI/2;
	}
	//1st q
	else if (dx > 0 && dy > 0)
		alpha = Math.atan((double) dy/dx);
	//4th q
	else if (dx > 0 && dy <= 0)
	{
		if(dy == 0)
			alpha = 0;
		else
			alpha = 2*(Math.PI) + Math.atan((double) dy/(dx));	
	}
	//2nd, 3rd q
	else if (dx < 0)
		alpha = (Math.PI)+ Math.atan((double) dy/dx);
	return alpha;
}

public Color color = Color.BLACK;

public void setColor(Color c)
{
	color = c;
}

public Color getColor()
{
	return color;
}

public abstract void updateObjPages(int page);

}
