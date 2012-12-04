/*
Copyright (C) 2006  Shlomy Reinstein

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Note: The ctags invocation code was taken from the CodeBrowser
plugin by Gerd Knops.
*/

package ctags.sidekick;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.jEdit;
import sidekick.SideKickParsedData;
import sidekick.SideKickParser;
import ctags.sidekick.options.GeneralOptionPane;
import ctags.sidekick.options.SideKickModeOptionsPane;
import errorlist.DefaultErrorSource;
import errorlist.ErrorSource;
import errorlist.DefaultErrorSource.DefaultError;


public class Parser extends SideKickParser {
  
	private static final String SPACES = "\\s+";
	private ToolBar toolbar = null;
	
	public Parser(String serviceName)
	{
		super(serviceName);
	}
	
	public JPanel getPanel() {
		toolbar = new ToolBar();
		return toolbar;
	}
	
	@Override
	public SideKickParsedData parse(Buffer buffer,
	  DefaultErrorSource errorSource)
	{		
		if (toolbar != null)
			toolbar.update();
		ParsedData data =
		new ParsedData(buffer, buffer.getMode().getName());
		runctags(buffer, errorSource, data);
		return data;
	}
	
	private File createTempFile(Buffer buffer)
	{
		String prefix = buffer.getName();
		String suffix = null;
		int idx = prefix.indexOf(".");
		if (idx > 0)
		{
			suffix = prefix.substring(idx);
			prefix = prefix.substring(0,idx);
		}
		File f = null;
		try
		{
			f = File.createTempFile(prefix, suffix);
			FileWriter fw = new FileWriter(f);
			int size = buffer.getLength();
			int offset = 0;
			while (size > 0)
			{
				int c = 16 * 1024;
				if (c > size)
					c = size;
				fw.write(buffer.getText(offset, c));
				offset += c;
				size -= c;
			}
			fw.close();
		}
		catch(Exception e)
		{
			return null;
		}
		return f;
	}
	
	private void runctags(Buffer buffer, DefaultErrorSource errorSource,
	  ParsedData data)
	{
		String ctagsExe = jEdit.getProperty("options.CtagsSideKick.ctags_path");
		String path = buffer.getPath();
		File f = null;
		if (MiscUtilities.isURL(path)) // A remote file (URL)
		{
			f = createTempFile(buffer);
			if (f == null)
				return;
			path = f.getAbsolutePath();
		}
		String mode = buffer.getMode().getName();
		String options = SideKickModeOptionsPane.getProperty(mode, Plugin.CTAGS_MODE_OPTIONS);
		if (options == null)
			options = "";
		Vector<String> cmdLine = new Vector<String>();
		cmdLine.add(ctagsExe);
		
		// funa edit
		if (jEdit.getBooleanProperty(GeneralOptionPane.USE_JCODE, true)){
		  String encoding = buffer.getStringProperty(Buffer.ENCODING);
		  String upperEncoding = encoding.toUpperCase();
		  String ctagsEncoding = "";
		  
		  if (upperEncoding.indexOf("UTF-8") >= 0){
		    ctagsEncoding = "utf8";
		  } else if (upperEncoding.indexOf("MS932") >= 0 || upperEncoding.indexOf("SJIS") >= 0){
		    ctagsEncoding = "sjis";
		  } else if (upperEncoding.indexOf("EUC") >= 0){
		    ctagsEncoding = "euc";
		  } else {
		    ctagsEncoding = "";
		  }
		  if (!ctagsEncoding.equals("")){
		    cmdLine.add("--jcode="+ctagsEncoding);
		  }
		}
		
		cmdLine.add("--fields=KsSz");
		cmdLine.add("--excmd=pattern");
		cmdLine.add("--sort=no");
		cmdLine.add("--fields=+n");
		cmdLine.add("--extra=-q");
		cmdLine.add("-f");
		cmdLine.add("-");
		if (path.endsWith("build.xml"))
			cmdLine.add("--language-force=ant");
		
		String [] customOptions = options.split(SPACES);
		for (int i = 0; i < customOptions.length; i++)
			cmdLine.add(customOptions[i]);
		cmdLine.add(path);
		String [] args = new String[cmdLine.size()]; 
		cmdLine.toArray(args);
		Process p;
		BufferedReader in = null;
		try {
			p = Runtime.getRuntime().exec(args);
			in = new BufferedReader(
			  new InputStreamReader(p.getInputStream()));
			String line;
			Tag prevTag = null;
			while ((line=in.readLine()) != null)
			{
				Hashtable<String, String> info =
				new Hashtable<String, String>();
				if (line.endsWith("\n") || line.endsWith("\r"))
					line = line.substring(0, line.length() - 1);
				String fields[] = line.split("\t");
				if (fields.length < 3)
					continue;
				info.put("k_tag".intern(), fields[0].intern());
				info.put("k_pat".intern(), fields[2].intern());
				// extensions
				for (int i = 3; i < fields.length; i++)
				{
					String pair[] = fields[i].split(":", 2);
					if (pair.length != 2)
						continue;
					info.put(pair[0].intern(), pair[1].intern());
				}
				Tag curTag = new Tag(buffer, info);
				if (prevTag != null)
				{	// Set end position of previous tag and add it to the tree
					// (If both tags are on the same line, make the previous tag
					// end at the name of the current one.)
					LinePosition prevEnd;
					int curLine = curTag.getLine();
					if (curLine == prevTag.getLine())
					{
						String def = buffer.getLineText(curLine);
						Pattern pat = Pattern.compile("\\b" + curTag.getName() + "\\b");
						Matcher mat = pat.matcher(def);
						int pos = mat.find() ? mat.start() : -1;
						if (pos == -1) // nothing to do, share assets...
							prevEnd = new LinePosition(buffer, curLine, false); 
						else
						{
							prevEnd = new LinePosition(buffer, curLine, pos);
							curTag.setStart(
							  new LinePosition(buffer, curLine, pos));
						}
					}
					else
						prevEnd = new LinePosition(buffer, curLine - 1, false);
					prevTag.setEnd(prevEnd);
					data.add(prevTag);
				}
				prevTag = curTag;
			}
			if (prevTag != null)
			{
				prevTag.setEnd(new LinePosition(buffer));
				data.add(prevTag);
			}
			data.done();
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			try {
				if (in == null)	// Probably could not run ctags
				{
					errorSource.addError(new DefaultError(errorSource, ErrorSource.ERROR,
						ctagsExe, 0, 0, 0, jEdit.getProperty("messages.CtagsSideKick.badCtagsPath")));
					return;
					
				}
				else
					in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (f != null)
			f.delete();
	}
}
