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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import org.apache.zookeeper.inspector.gui.nodeviewer.ZooInspectorNodeViewer;
import org.apache.zookeeper.inspector.logger.LoggerFactory;
import org.apache.zookeeper.inspector.manager.ZooInspectorManager;

/**
 * @author CGSmithe
 * 
 */
public class ZooInspectorPanel extends JPanel implements NodeViewersChangeListener
{
	/**
	 * {该处请说明该field的含义和作用}
	 */
	private static final long serialVersionUID = -7798270148756643627L;
	
	private final JButton refreshButton;
	private final JButton disconnectButton;
	private final JButton connectButton;
	private final ZooInspectorNodeViewersPanel nodeViewersPanel;
	private final ZooInspectorTreeViewer treeViewer;
	private final ZooInspectorManager zooInspectorManager;
	private final JButton addNodeButton;
	private final JButton deleteNodeButton;
	private final JButton nodeViewersButton;
	private final JButton aboutButton;

	private final JButton importButton;
	private final JButton exportButton;
	private final JButton searchButton;
	
	private final List<NodeViewersChangeListener> listeners = new ArrayList<NodeViewersChangeListener>();
	{
		listeners.add(this);
	}

	/**
	 * @param zooInspectorManager
	 */
	public ZooInspectorPanel(final ZooInspectorManager zooInspectorManager)
	{
		this.zooInspectorManager = zooInspectorManager;
		final ArrayList<ZooInspectorNodeViewer> nodeViewers = new ArrayList<ZooInspectorNodeViewer>();
		try
		{
			List<String> defaultNodeViewersClassNames = this.zooInspectorManager
					.getDefaultNodeViewerConfiguration();
			for (String className : defaultNodeViewersClassNames)
			{
				nodeViewers.add((ZooInspectorNodeViewer) Class.forName(className).newInstance());
			}
		}
		catch (Exception ex)
		{
			LoggerFactory.getLogger().error("Error loading default node viewers.", ex);
			JOptionPane.showMessageDialog(ZooInspectorPanel.this,
					"Error loading default node viewers: " + ex.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		nodeViewersPanel = new ZooInspectorNodeViewersPanel(zooInspectorManager, nodeViewers);
		treeViewer = new ZooInspectorTreeViewer(zooInspectorManager, nodeViewersPanel);
		this.setLayout(new BorderLayout());
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		connectButton = new JButton(ZooInspectorIconResources.getConnectIcon());
		disconnectButton = new JButton(ZooInspectorIconResources.getDisconnectIcon());
		refreshButton = new JButton(ZooInspectorIconResources.getRefreshIcon());
		addNodeButton = new JButton(ZooInspectorIconResources.getAddNodeIcon());
		deleteNodeButton = new JButton(ZooInspectorIconResources.getDeleteNodeIcon());
		nodeViewersButton = new JButton(ZooInspectorIconResources.getChangeNodeViewersIcon());
		aboutButton = new JButton(ZooInspectorIconResources.getInformationIcon());
		
		importButton = new JButton(ZooInspectorIconResources.getImportIcon());
		exportButton = new JButton(ZooInspectorIconResources.getExportIcon());
		searchButton = new JButton(ZooInspectorIconResources.getSearchIcon());
		
		toolbar.add(connectButton);
		toolbar.add(disconnectButton);
		toolbar.add(refreshButton);
		toolbar.add(addNodeButton);
		toolbar.add(deleteNodeButton);
		toolbar.add(nodeViewersButton);
		toolbar.add(aboutButton);
		
		toolbar.add(importButton);
		toolbar.add(exportButton);
		toolbar.add(searchButton);
		
		aboutButton.setEnabled(true);
		connectButton.setEnabled(true);
		disconnectButton.setEnabled(false);
		refreshButton.setEnabled(false);
		addNodeButton.setEnabled(false);
		deleteNodeButton.setEnabled(false);
		nodeViewersButton.setEnabled(true);
		
		importButton.setEnabled(false);
		exportButton.setEnabled(false);
		searchButton.setEnabled(false);
		
		nodeViewersButton.setToolTipText("Change Node Viewers");
		aboutButton.setToolTipText("About ZooInspector");
		connectButton.setToolTipText("Connect");
		disconnectButton.setToolTipText("Disconnect");
		refreshButton.setToolTipText("Refresh");
		addNodeButton.setToolTipText("Add Node");
		deleteNodeButton.setToolTipText("Delete Node");
		
		importButton.setToolTipText("Import Node");
		exportButton.setToolTipText("Export Node");
		searchButton.setToolTipText("Search Node");
		
		connectButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ZooInspectorConnectionPropertiesDialog zicpd = new ZooInspectorConnectionPropertiesDialog(
						zooInspectorManager.getConnectionPropertiesTemplate(),
						ZooInspectorPanel.this);
				zicpd.setVisible(true);
			}
		});
		disconnectButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				disconnect();
			}
		});
		refreshButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				treeViewer.refreshView();
			}
		});
		addNodeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final List<String> selectedNodes = treeViewer.getSelectedNodes();
				if (selectedNodes.size() == 1)
				{
					final String nodeName = JOptionPane.showInputDialog(ZooInspectorPanel.this,
							"Please Enter a name for the new node", "Create Node",
							JOptionPane.INFORMATION_MESSAGE);
					if (nodeName != null && nodeName.length() > 0)
					{
						SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>()
						{

							@Override
							protected Boolean doInBackground() throws Exception
							{
								return ZooInspectorPanel.this.zooInspectorManager.createNode(
										selectedNodes.get(0), nodeName);
							}

							@Override
							protected void done()
							{
								treeViewer.refreshView();
							}
						};
						worker.execute();
					}
				}
				else
				{
					JOptionPane.showMessageDialog(ZooInspectorPanel.this,
							"Please select 1 parent node for the new node.");
				}
			}
		});
		deleteNodeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final List<String> selectedNodes = treeViewer.getSelectedNodes();
				if (selectedNodes.size() == 0)
				{
					JOptionPane.showMessageDialog(ZooInspectorPanel.this,
							"Please select at least 1 node to be deleted");
				}
				else
				{
					int answer = JOptionPane.showConfirmDialog(ZooInspectorPanel.this,
							"Are you sure you want to delete the selected nodes?"
									+ "(This action cannot be reverted)", "Confirm Delete",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (answer == JOptionPane.YES_OPTION)
					{
						SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>()
						{

							@Override
							protected Boolean doInBackground() throws Exception
							{
								for (String nodePath : selectedNodes)
								{
									ZooInspectorPanel.this.zooInspectorManager.deleteNode(nodePath);
								}
								return true;
							}

							@Override
							protected void done()
							{
								treeViewer.refreshView();
							}
						};
						worker.execute();
					}
				}
			}
		});
		nodeViewersButton.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				ZooInspectorNodeViewersDialog nvd = new ZooInspectorNodeViewersDialog(JOptionPane
						.getRootFrame(), nodeViewers, listeners, zooInspectorManager);
				nvd.setVisible(true);
			}
		});
		aboutButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ZooInspectorAboutDialog zicpd = new ZooInspectorAboutDialog(JOptionPane
						.getRootFrame());
				zicpd.setVisible(true);
			}
		});
		
		importButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				// 打开文件选择窗口
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("import file");
				chooser.setDialogType(JFileChooser.OPEN_DIALOG);
				chooser.setCurrentDirectory(new File("."));
				chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
					public boolean accept(File f) {
						return f.getName().toLowerCase().endsWith(".properties") || f.isDirectory();
					}

					public String getDescription() {
						return "properties file";
					}
				});

				int r = chooser.showOpenDialog(new JFrame());
				if (r == JFileChooser.APPROVE_OPTION) {
					int answer = JOptionPane.showConfirmDialog(ZooInspectorPanel.this,
							"Are you sure you want to import the selected file?"
									+ "(This action cannot be reverted)", "Confirm Import",
							JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (answer == JOptionPane.YES_OPTION)
					{
						File f = chooser.getSelectedFile();
						final Map<String, String> nodeMap = new TreeMap<String, String>();
						
						// 读取文件
						LineNumberReader lnr = null;
						try {
							lnr = new LineNumberReader(new FileReader(f));
							String line = null;
							do {
								line = lnr.readLine();
								if (line != null && line.trim().length() > 0) {
									// 过滤注释行
									if ("#".equals(line.trim().substring(0, 1))) {
										continue;
									}
									
									int pos = line.indexOf("=");
									if (pos > 0) {
										String key = line.substring(0, pos);
										String value = line.substring(pos + 1, line.length());
										nodeMap.put(key, value);
									}
								}
							} while (line != null);
						} catch (FileNotFoundException e1) {
							LoggerFactory.getLogger().error("Error read file:" + f.getName(), e1);
						} catch (IOException e1) {
							LoggerFactory.getLogger().error("Error read file:" + f.getName(), e1);
						} finally {
							try {
								lnr.close();
							} catch (IOException e1) {
								LoggerFactory.getLogger().error("Error close file:" + f.getName(), e1);
							}
						}
						
						SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>()
						{

							@Override
							protected Boolean doInBackground() throws Exception
							{
								ZooInspectorPanel.this.zooInspectorManager.importNode(nodeMap);
								return true;
							}

							@Override
							protected void done()
							{
								JOptionPane.showMessageDialog(null, "Import success!", "Message", JOptionPane.INFORMATION_MESSAGE);
								treeViewer.refreshView();
							}
						};
						worker.execute();
					}
				} else {
					// 没有选择文件
				}
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		
		exportButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				final List<String> selectedNodes = treeViewer.getSelectedNodes();
				if (selectedNodes.size() == 0)
				{
					JOptionPane.showMessageDialog(ZooInspectorPanel.this,
							"Please select at least 1 node to be export");
				}
				else
				{
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					// 打开文件选择窗口
					JFileChooser chooser = new JFileChooser();
					chooser.setDialogTitle("export file");
					chooser.setDialogType(JFileChooser.SAVE_DIALOG);
					chooser.setCurrentDirectory(new File("."));
					chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
						public boolean accept(File f) {
							return f.getName().toLowerCase().endsWith(".properties") || f.isDirectory();
						}

						public String getDescription() {
							return "properties file";
						}
					});

					int r = chooser.showOpenDialog(new JFrame());
					if (r == JFileChooser.APPROVE_OPTION) {
						final File f = chooser.getSelectedFile();
						
						SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>()
						{

							@Override
							protected Boolean doInBackground() throws Exception
							{
								final Map<String, String> map = new TreeMap<String, String>();
								for (String nodePath : selectedNodes) {
									ZooInspectorPanel.this.zooInspectorManager.exportNode(nodePath, map);
								}
								
								PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
								Set<String> keySet = map.keySet();
								for (String key : keySet) {
									out.println(key + "=" + map.get(key));
								}
								out.flush();
								out.close();
								
								return true;
							}

							@Override
							protected void done()
							{
								JOptionPane.showMessageDialog(null, "Export success!", "Message", JOptionPane.INFORMATION_MESSAGE);
							}
						};
						worker.execute();
					} else {
						// 没有选择文件
					}
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
		
		searchButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ZooInspectorSearchDialog zicpd = new ZooInspectorSearchDialog(
						zooInspectorManager.getSearchTemplate(),
						ZooInspectorPanel.this);
				zicpd.setVisible(true);
			}
		});

		JScrollPane treeScroller = new JScrollPane(treeViewer);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroller,
				nodeViewersPanel);
		splitPane.setResizeWeight(0.25);
		this.add(splitPane, BorderLayout.CENTER);
		this.add(toolbar, BorderLayout.NORTH);
	}

	/**
	 * @param connectionProps
	 */
	public void connect(final Properties connectionProps)
	{
		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>()
		{

			@Override
			protected Boolean doInBackground() throws Exception
			{
				return zooInspectorManager.connect(connectionProps);
			}

			@Override
			protected void done()
			{
				try
				{
					if (get())
					{
						treeViewer.refreshView();
						connectButton.setEnabled(false);
						disconnectButton.setEnabled(true);
						refreshButton.setEnabled(true);
						addNodeButton.setEnabled(true);
						deleteNodeButton.setEnabled(true);
						
						importButton.setEnabled(true);
						exportButton.setEnabled(true);
						searchButton.setEnabled(false);
					}
					else
					{
						JOptionPane.showMessageDialog(ZooInspectorPanel.this,
								"Unable to connect to zookeeper", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
				catch (InterruptedException e)
				{
					LoggerFactory.getLogger().error(
							"Error occurred while connecting to ZooKeeper server", e);
				}
				catch (ExecutionException e)
				{
					LoggerFactory.getLogger().error(
							"Error occurred while connecting to ZooKeeper server", e);
				}
			}

		};
		worker.execute();
	}

	/**
	 * 
	 */
	public void disconnect()
	{
		disconnect(false);
	}

	/**
	 * @param wait
	 */
	public void disconnect(boolean wait)
	{
		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>()
		{

			@Override
			protected Boolean doInBackground() throws Exception
			{
				return ZooInspectorPanel.this.zooInspectorManager.disconnect();
			}

			@Override
			protected void done()
			{
				try
				{
					if (get())
					{
						treeViewer.clearView();
						connectButton.setEnabled(true);
						disconnectButton.setEnabled(false);
						refreshButton.setEnabled(false);
						addNodeButton.setEnabled(false);
						deleteNodeButton.setEnabled(false);
						
						importButton.setEnabled(false);
						exportButton.setEnabled(false);
						searchButton.setEnabled(false);
					}
				}
				catch (InterruptedException e)
				{
					LoggerFactory.getLogger().error(
							"Error occurred while disconnecting from ZooKeeper server", e);
				}
				catch (ExecutionException e)
				{
					LoggerFactory.getLogger().error(
							"Error occurred while disconnecting from ZooKeeper server", e);
				}
			}

		};
		worker.execute();
		if (wait)
		{
			while (!worker.isDone())
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					LoggerFactory.getLogger().error(
							"Error occurred while disconnecting from ZooKeeper server", e);
				}
			}
		}
	}
	
	public void search(final Properties searchProps) {
		SwingWorker<String, String> worker = new SwingWorker<String, String>()
		{

			@Override
			protected String doInBackground() throws Exception
			{
				return zooInspectorManager.searchNode(searchProps);
			}

			@Override
			protected void done()
			{
				try
				{
					get();
				}
				catch (InterruptedException e)
				{
					LoggerFactory.getLogger().error(
							"Error occurred while connecting to ZooKeeper server", e);
				}
				catch (ExecutionException e)
				{
					LoggerFactory.getLogger().error(
							"Error occurred while connecting to ZooKeeper server", e);
				}
			}

		};
		worker.execute();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.apache.zookeeper.inspector.gui.NodeViewersChangeListener#
	 * nodeViewersChanged(java.util.List)
	 */
	public void nodeViewersChanged(List<ZooInspectorNodeViewer> newViewers)
	{
		this.nodeViewersPanel.setNodeViewers(newViewers);
	}
}
