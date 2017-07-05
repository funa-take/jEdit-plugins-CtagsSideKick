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

Note: The code for the ctags path and the ctags version notice
was taken from the CodeBrowser plugin by Gerd Knops.
*/

package ctags.sidekick.options;
import java.awt.Color;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;

import ctags.sidekick.Plugin;

import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.BorderLayout;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** ************************************************************************** */
public class GeneralOptionPane extends AbstractOptionPane {
	/**
	*
	*/
	private static final long serialVersionUID = 1L;
	
	/***************************************************************************
	* Vars
	**************************************************************************/
	
	private JTextField ctagsPathTF;
	private JCheckBox showGroupSelector;
	private JCheckBox showSortSelector;
	private JCheckBox showFilterSelector;
	private JCheckBox showTextProviderSelector;
	private JCheckBox showIconProviderSelector;
	private JCheckBox show_icons;
	private JCheckBox use_jcode;
	private JCheckBox use_temporary;
	private MappingModel mappingModel;
	
	static final String PREFIX = Plugin.OPTION_PREFIX;
	public static final String SHOW_GROUP_SELECTOR = PREFIX + "showGroupSelector";
	private static final String SHOW_GROUP_SELECTOR_LABEL = SHOW_GROUP_SELECTOR + ".label";
	public static final String SHOW_FILTER_SELECTOR = PREFIX + "showFilterSelector";
	private static final String SHOW_FILTER_SELECTOR_LABEL = SHOW_FILTER_SELECTOR + ".label";
	public static final String SHOW_SORT_SELECTOR = PREFIX + "showSortSelector";
	private static final String SHOW_SORT_SELECTOR_LABEL = SHOW_SORT_SELECTOR + ".label";
	public static final String SHOW_TEXT_PROVIDER_SELECTOR = PREFIX + "showTextProviderSelector";
	private static final String SHOW_TEXT_PROVIDER_SELECTOR_LABEL = SHOW_TEXT_PROVIDER_SELECTOR + ".label";
	public static final String SHOW_ICON_PROVIDER_SELECTOR = PREFIX + "showIconProviderSelector";
	private static final String SHOW_ICON_PROVIDER_SELECTOR_LABEL = SHOW_ICON_PROVIDER_SELECTOR + ".label";
	public static final String SORT = PREFIX + "sort";
	public static final String FOLDS_BEFORE_LEAFS = PREFIX + "sort_folds_first";
	public static final String SHOW_ICONS = PREFIX + "show_icons";
	public static final String USE_JCODE = PREFIX + "use_jcode";
	public static final String USE_TEMPORARY = PREFIX + "use_temporary";
	public static final String MAPPING = PREFIX + "mapping";
	static final String LABEL = "_label";
	
	public static final String ICONS = PREFIX + "icons.";
	
	public static final String PARSE_ACTION_PROP = "CtagsSideKick.parse.action";
	
	/***************************************************************************
	* Factory methods
	**************************************************************************/
	public GeneralOptionPane()
	{
		super("CtagsSideKick-general");
		setBorder(new EmptyBorder(5, 5, 5, 5));
		
		JTextArea ta = new JTextArea(jEdit
		  .getProperty(PREFIX + "ctags_path_note"), 0, 60);
		ta.setEditable(false);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setBackground(Color.yellow);
		
		addComponent("Note:", ta);
		
		addSeparator();
		
		addComponent(jEdit
		  .getProperty(PREFIX + "ctags_path_label"),
		  ctagsPathTF = new JTextField(jEdit
		    .getProperty(PREFIX + "ctags_path"), 40));
		
		addSeparator();
		
		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setBorder(new TitledBorder("Show in dockable toolbar:"));
		showGroupSelector = new JCheckBox(
		  jEdit.getProperty(SHOW_GROUP_SELECTOR_LABEL),
		  jEdit.getBooleanProperty(SHOW_GROUP_SELECTOR, true));
		toolBarPanel.add(showGroupSelector);
		showSortSelector = new JCheckBox(
		  jEdit.getProperty(SHOW_SORT_SELECTOR_LABEL),
		  jEdit.getBooleanProperty(SHOW_SORT_SELECTOR, true));
		toolBarPanel.add(showSortSelector);
		showFilterSelector = new JCheckBox(
		  jEdit.getProperty(SHOW_FILTER_SELECTOR_LABEL),
		  jEdit.getBooleanProperty(SHOW_FILTER_SELECTOR, true));
		toolBarPanel.add(showFilterSelector);
		showTextProviderSelector = new JCheckBox(
		  jEdit.getProperty(SHOW_TEXT_PROVIDER_SELECTOR_LABEL),
		  jEdit.getBooleanProperty(SHOW_TEXT_PROVIDER_SELECTOR, true));
		toolBarPanel.add(showTextProviderSelector);
		showIconProviderSelector = new JCheckBox(
		  jEdit.getProperty(SHOW_ICON_PROVIDER_SELECTOR_LABEL),
		  jEdit.getBooleanProperty(SHOW_ICON_PROVIDER_SELECTOR, true));
		toolBarPanel.add(showIconProviderSelector);
		
		addComponent(toolBarPanel);
		show_icons = new JCheckBox(
		  jEdit.getProperty(SHOW_ICONS + LABEL),
		  jEdit.getBooleanProperty(SHOW_ICONS, false));
		addComponent(show_icons);
		use_jcode = new JCheckBox(
		  jEdit.getProperty(USE_JCODE + LABEL),
		  jEdit.getBooleanProperty(USE_JCODE, true));
		addComponent(use_jcode);
		use_temporary = new JCheckBox(
		  jEdit.getProperty(USE_TEMPORARY + LABEL),
		  jEdit.getBooleanProperty(USE_TEMPORARY, false));
		addComponent(use_temporary);
		
		mappingModel = new MappingModel();
		JTable table = new JTable(mappingModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);
		JPanel tablePanel = new JPanel();
		MappingListener listener = new MappingListener(table, mappingModel);
		
		Vector<Vector<String>> mapping = getMapping();
		for(int i = 0; i < mapping.size(); i++) {
		  mappingModel.addRow(mapping.get(i));
		}
		
		tablePanel.setLayout(new BorderLayout());
		tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Language Mapping"));
		
		JPanel buttonPanel = new JPanel();
    JButton addButton = new JButton("Add");
    addButton.addActionListener(listener);
    buttonPanel.add(addButton);
    JButton removeButton = new JButton("Remove");
    removeButton.addActionListener(listener);
    buttonPanel.add(removeButton);
    tablePanel.add(buttonPanel, BorderLayout.SOUTH);
    
    JScrollPane scrollPane = new JScrollPane(table);
		tablePanel.add(scrollPane, BorderLayout.CENTER);
		Dimension d = tablePanel.getPreferredSize();
		d.height = 200;
		tablePanel.setPreferredSize(d);
		
		addComponent(tablePanel, GridBagConstraints.BOTH);
		
		// テーブル部の高さ自動調整も可能だが、
		// そこまでする必要もないかな。とりあえず、高さ固定
		// setLayout(new BorderLayout())をしても良い
		// GridBagConstraints cons = new GridBagConstraints();
    // cons.gridy = (this.y++);
    // cons.gridheight = 1;
    // cons.gridwidth = 0;
    // cons.fill = 1;
    // cons.weightx = 1.0D;
    // cons.weighty = 1.0D;
    // this.gridBag.setConstraints(tablePanel, cons);
    // add(tablePanel);
	}
	
	/***************************************************************************
	* Implementation
	**************************************************************************/
	@Override
	protected void _save()
	{
		jEdit.setProperty(PREFIX + "ctags_path", ctagsPathTF.getText());
		jEdit.setBooleanProperty(SHOW_GROUP_SELECTOR, showGroupSelector.isSelected());
		jEdit.setBooleanProperty(SHOW_SORT_SELECTOR, showSortSelector.isSelected());
		jEdit.setBooleanProperty(SHOW_FILTER_SELECTOR, showFilterSelector.isSelected());
		jEdit.setBooleanProperty(SHOW_TEXT_PROVIDER_SELECTOR, showTextProviderSelector.isSelected());
		jEdit.setBooleanProperty(SHOW_ICON_PROVIDER_SELECTOR, showIconProviderSelector.isSelected());
		jEdit.setBooleanProperty(SHOW_ICONS, show_icons.isSelected());
		jEdit.setBooleanProperty(USE_JCODE, use_jcode.isSelected());
		jEdit.setBooleanProperty(USE_TEMPORARY, use_temporary.isSelected());
		jEdit.getAction(jEdit.getProperty(PARSE_ACTION_PROP)).invoke(jEdit.getActiveView());
		
		clearMappingProperties();
		int count = mappingModel.getRowCount();
		int index = 0;
		for(int i = 0; i < count; i++) {
		  String mode = mappingModel.getValueAt(i, MappingModel.COL.MODE.ordinal()).toString();
		  String regex = mappingModel.getValueAt(i, MappingModel.COL.FILE_REGEX.ordinal()).toString();
		  String lang = mappingModel.getValueAt(i, MappingModel.COL.LANG.ordinal()).toString();
		  if ("".equals(lang)) {
		    continue;
		  }
		  if ("".equals(mode) && "".equals(regex)) {
		    continue;
		  }
		  
		  jEdit.setProperty(MAPPING+"."+index+".mode", mode);
		  jEdit.setProperty(MAPPING+"."+index+".regex", regex);
		  jEdit.setProperty(MAPPING+"."+index+".lang", lang);
		  index++;
		}
	}
	public static Vector<Vector<String>> getMapping() {
	  int index = 0;
	  Vector<Vector<String>> mapping = new Vector<Vector<String>>();
		while(true) {
		  String value = null;
		  Vector<String> row = new Vector<String>();
		  value = jEdit.getProperty(MAPPING+"."+index+".mode");
		  if (value == null) {
		    break;
		  }
		  row.add(value);
		  value = jEdit.getProperty(MAPPING+"."+index+".regex");
		  if (value == null) {
		    break;
		  }
		  row.add(value);
		  value = jEdit.getProperty(MAPPING+"."+index+".lang");
		  if (value == null) {
		    break;
		  }
		  row.add(value);
		  mapping.add(row);
		  index++;
		}
	  
		return mapping;
	}
	
	
	private void clearMappingProperties() {
	  int index = 0;
	  while(true) {
		  String value = null;
		  value = jEdit.getProperty(MAPPING+"."+index+".mode");
		  if (value == null) {
		    break;
		  }
		  jEdit.unsetProperty(MAPPING+"."+index+".mode");
		  
		  value = jEdit.getProperty(MAPPING+"."+index+".regex");
		  if (value == null) {
		    break;
      }
		  jEdit.unsetProperty(MAPPING+"."+index+".regex");
		  
		  value = jEdit.getProperty(MAPPING+"."+index+".lang");
		  if (value == null) {
		    break;
		  }
		  jEdit.unsetProperty(MAPPING+"."+index+".regex");
		  
		  index++;
		}
	}
	
	static class MappingListener implements ActionListener {
	  private JTable table;
	  private MappingModel model;
	  private MappingListener(JTable table, MappingModel model) {
	    this.table = table;
	    this.model = model;
	  }
	  
	  public void actionPerformed(ActionEvent evt) {
	    String command = evt.getActionCommand();
	    
	    if ("Add".equals(command)) {
	      Object[] data = new Object[]{"","",""};
	      model.addRow(data);
	    } else if ("Remove".equals(command)) {
	      int modelIndex = table.convertRowIndexToModel(table.getSelectedRow());
	      if (modelIndex >= 0) {
	        model.removeRow(modelIndex);
	      }
	    }
	  }
	}
	
	public static class MappingModel extends DefaultTableModel {
	  public enum COL {
	    MODE,
	    FILE_REGEX,
	    LANG
	  };
	  
	  public MappingModel() {
	    super(new Object[][]{}, new Object[]{"Edit Mode", "File Regex", "Ctags Language"});
	  }
	  
	  public boolean isCellEditable(int row, int col) {
	    return true;
	  }
	  
	}
}
/** ***********************************************************************EOF */

