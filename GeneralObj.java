import java.awt.*;
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

public abstract class GeneralObj implements Cloneable {
	
	public abstract void paintComponent(Graphics g);
	
	

	public abstract boolean setSelectStatus(int x, int y);
	
	//for select multiple
	public abstract boolean setBoxSelectStatus(int x0, int y0, int x1, int y1);
	
	public abstract void adjustShapeOrPosition(int x, int y);
	
	public abstract int getSelectStatus();
	
	public Object clone () 
    throws CloneNotSupportedException
   {
       return super.clone();
   }
	
	public abstract Point getStart();

	public abstract boolean containsParent(GeneralObj oldObj);
	
	public abstract int getType();
	
	public abstract boolean isModified();

	public boolean modified = false;
	public void setModified(boolean b)
	{
		modified = b;
	}

	
	public abstract void updateObj();
	
	public abstract void unselect();
	
	public boolean isParentModified()
	{
		return modifiedParent;
	}

	public boolean modifiedParent = false;
	
	public void setParentModified(boolean b)
	{
		modifiedParent = b;
	}


	public abstract Point getCenter(int page);

	public abstract void notifyChange(GeneralObj oldObj, GeneralObj clonedObj);
	
	String objName;
	
	//list of obj attributes for the object
	LinkedList<ObjAttribute> attrib = new LinkedList<ObjAttribute>();
	LinkedList<ObjAttribute> global;
	LinkedList<LinkedList<ObjAttribute>> allGlobal;

	
	
	public LinkedList<ObjAttribute> getAttributeList()
	{
		return attrib;
	}
	

	
	public int jumpCount = 0;
	

	
	// updates the attribute list for a particular object by reading in the global list
	public void updateAttrib(LinkedList<LinkedList<ObjAttribute>> glist, int a) {
		global = glist.get(a);
		allGlobal = glist;
		for(int i = 0; i < global.size();i++)
		{
			ObjAttribute obj = global.get(i);
		}
		
		for(int i = 0; i < global.size(); i++)
		{
			ObjAttribute g = global.get(i);
			ObjAttribute l = null;
			if(attrib.size() > i)
				l = attrib.get(i);
			
			// if names match for current index
			if(l != null && g.getName().equals(l.getName()))
			{
				//global attribute name can't be local
				if(l.getEditable(0) == l.LOCAL)
					l.setEditable(0,l.GLOBAL_FIXED);
				//if blank string, make global variable
				if(l.get(1).equals(""))
					l.setEditable(1,l.GLOBAL_VAR);
				if(l.get(3).equals(""))
					l.setEditable(3,l.GLOBAL_VAR);

				for(int j = 0; j < 7; j++)
				{
					// if value in field isn't locally set variable, replace with global
					if(l.getEditable(j) != l.LOCAL)
						l.set(j,g.get(j));
				}
				
				// checking for changed non-default value
				if(l.getEditable(1) == ObjAttribute.LOCAL && g.getValue().equals(l.getValue()))
					l.setEditable(1, ObjAttribute.GLOBAL_VAR);
			}
			//TODO rerun check on swapped lists
			else
			{
				boolean breaker = false;
				// look for attribute in wrong place
				for(int k = 0; k < attrib.size(); k++)
				{
					ObjAttribute l2 = attrib.get(k);
					//if one is found, fix it
					if(g.getName().equals(l2.getName()))
					{
						attrib.addLast((ObjAttribute) attrib.get(i));
						attrib.set(i,l2);
						attrib.remove(k);
						breaker = true;
						break;
					}
				}
				// if wasnt fixed, then it doesnt exist and needs to be created
				if(!breaker)
				{
					ObjAttribute cloned = null;
					try {
						cloned = (ObjAttribute) g.clone();
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					if(cloned.getName().equals("name"))
					{
						cloned.setValue(objName);
						cloned.setEditable(1, ObjAttribute.LOCAL);
					}
				    attrib.add(i,cloned);
				}
			}
		}
		
		//delete attributes that were removed in global
		if(attrib.size() > global.size())
		{
			for(int i = attrib.size()-1; i > global.size()-1; i--)
			{
				ObjAttribute obj = attrib.get(i);
				if(obj.getEditable(0) != ObjAttribute.LOCAL)
					attrib.remove(i);
			}
		}
		
		// set up correct pages
		for(int i = 0; i < attrib.size();i++)
		{
			ObjAttribute l = attrib.get(i);
			l.setPage(myPage,"update");
//			l.setOutputTypeFlag(true);
			if(l.getType().equals("output"))
			{
				boolean b = false;
				boolean f = false;
				for(int j = 0; j < allGlobal.get(2).size(); j++)
				{
					ObjAttribute obj = allGlobal.get(2).get(j);
					if(obj.getName().equals(l.getName()))
					{
						if(obj.getType().equals("reg") || obj.getType().equals("regdp"))
							b = true;
						if(obj.getType().equals("flag"))
							f = true;
						break;
					}
				}
				l.setOutputTypeReg(b);
				l.setOutputTypeFlag(f);
			}
			else
                        {
				l.setOutputTypeReg(false);
				l.setOutputTypeFlag(false);
                        }
		}
		
	}
	
	
	public String getOutputType(String name)
	{
		LinkedList<ObjAttribute> list = allGlobal.get(2);
		/// find output with matching name, and return its type
		for(int i = 0; i < list.size(); i++)
		{
			ObjAttribute obj = list.get(i);
			if(obj.getName().equals(name))
				return obj.getType();		
		}
		return "";
	}
	
	public String toString()
	{
		return objName;
	}
	
	public String getName()
	{
		return objName;
	}
	
	public void setName(String str)
	{
		objName = str;
	}

	public abstract void save(BufferedWriter writer) throws IOException;
	
	//pages
	int currPage = 1;
	int myPage = 1;
	public void paintComponent(Graphics g, int i)
	{
		currPage = i;
		paintComponent(g);
		if(attrib != null)
		{
			int step = -1;
			for (int j = 0; j < attrib.size(); j++)
			{
				ObjAttribute obj = attrib.get(j);
				if(obj.getVisible())
					step++;
				obj.paintComponent(g,currPage,getCenter(currPage),getSelectStatus(),step);
			}
		}  
	}
	
	public int getPage()
	{
		return myPage;
	}

	public void setPage(int i)
	{
		myPage = i;
		if(attrib != null)
		{
			for(int j = 0; j < attrib.size(); j++)
			{
				ObjAttribute obj = attrib.get(j);
				if(getType() != 1)
					obj.setPage(i);
			}
		}
	}


	public void decrementPage() {
		myPage = myPage - 1;
		if(attrib != null)
		{
			for(int i = 0; i < attrib.size(); i++)
			{
				ObjAttribute obj = attrib.get(i);
				obj.setPage(myPage);
			}
		}
		
	}



	public boolean setBoxSelectStatus(int x, int y) {

		return false;
	}



	public void setSelectStatus(boolean b) {
		
	}

	//indentation (also exits in FizzimGui.java and ObjAttribute.java
	public String i(int indent)
	{
		String ind = "";
		for(int i=0; i<indent; i++)
		{
			ind = ind + "   ";
		}
		return ind;
	}

	

	
	
	
	
	

}
