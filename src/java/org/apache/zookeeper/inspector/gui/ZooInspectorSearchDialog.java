/*
 * ZooInspector
 * 
 * Copyright 2010 Colin Goodheart-Smithe

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.apache.zookeeper.inspector.gui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.zookeeper.inspector.manager.Pair;

/**
 * @author CGSmithe
 * 
 */
public class ZooInspectorSearchDialog extends JDialog
{

	/**
	 * {该处请说明该field的含义和作用}
	 */
	private static final long serialVersionUID = 3584500036338056203L;

	/**
	 * @param searchTemplateAndLabels
	 * @param zooInspectorPanel
	 */
	public ZooInspectorSearchDialog(
			Pair<Map<String, List<String>>, Map<String, String>> searchTemplateAndLabels,
			final ZooInspectorPanel zooInspectorPanel)
	{
		final Map<String, List<String>> searchTemplate = searchTemplateAndLabels
				.getKey();
		final Map<String, String> searchLabels = searchTemplateAndLabels
				.getValue();
		
		this.setLayout(new BorderLayout());
		this.setTitle("Search node");
		this.setModal(true);
		this.setAlwaysOnTop(true);
		final JPanel options = new JPanel();
		int numRows = searchTemplate.size() * 2 + 1;
		double[] rows = new double[numRows];
		for (int i = 0; i < numRows; i++)
		{
			if (i % 2 == 0)
			{
				rows[i] = 5;
			}
			else
			{
				rows[i] = TableLayout.PREFERRED;
			}
		}
		options.setLayout(new TableLayout(new double[] { 10, TableLayout.PREFERRED, 5,
				TableLayout.PREFERRED, 10 }, rows));
		int i = 0;
		final Map<String, JComponent> components = new HashMap<String, JComponent>();
		for (Entry<String, List<String>> entry : searchTemplate.entrySet())
		{
			int rowPos = 2 * i + 1;
			JLabel label = new JLabel(searchLabels.get(entry.getKey()));
			options.add(label, "1," + rowPos);
			if (entry.getValue().size() == 0)
			{
				JTextField text = new JTextField();
				options.add(text, "3," + rowPos);
				components.put(entry.getKey(), text);
			}
			else if (entry.getValue().size() == 1)
			{
				JTextField text = new JTextField(entry.getValue().get(0));
				options.add(text, "3," + rowPos);
				components.put(entry.getKey(), text);
			}
			i++;
		}
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new TableLayout(new double[] { 10, TableLayout.PREFERRED, 5,
				TableLayout.FILL, TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10 },
				new double[] { TableLayout.PREFERRED }));
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Properties searchProps = new Properties();
				for (Entry<String, JComponent> entry : components.entrySet())
				{
					String value = null;
					JComponent component = entry.getValue();
					if (component instanceof JTextField)
					{
						value = ((JTextField) component).getText();
					}
					searchProps.put(entry.getKey(), value);
				}
				zooInspectorPanel.search(searchProps);
			}
		});
		buttonsPanel.add(searchButton, "4,0");
		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
			}
		});
		buttonsPanel.add(saveButton, "6,0");
		this.add(options, BorderLayout.CENTER);
		this.add(buttonsPanel, BorderLayout.SOUTH);
		this.pack();
	}

}
