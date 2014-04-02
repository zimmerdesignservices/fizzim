import java.io.File;
import javax.swing.filechooser.FileFilter;

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

class FzmFilter extends FileFilter{
	
	public boolean accept(File f) {
		boolean accept = f.isDirectory();
		if(!accept) {
			String suffix = getSuffix(f);
			if(suffix != null)
				accept = suffix.equals("fzm");
		}
		return accept;
	}
	public String getDescription() {
		return "Fizzim Files (*.fzm)";
	}
	public String getSuffix(File f) {
		String s = f.getPath(), suffix = null;
		int i = s.lastIndexOf('.');
		
		if(i>0 && i < s.length() - 1)
			suffix = s.substring(i+1).toLowerCase();
		
		return suffix;
	}


}