/* ===========================================================
 * TradeManager : An application to trade strategies for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Project Info:  org.trade
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Oracle, Inc.
 * in the United States and other countries.]
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Original Author:  Simon Allen;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 */
package org.trade.ui.configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.trade.core.dao.Aspect;
import org.trade.core.dao.Aspects;
import org.trade.core.factory.ClassFactory;
import org.trade.core.lookup.DBTableLookupServiceProvider;
import org.trade.core.valuetype.Decode;
import org.trade.dictionary.valuetype.DAOEntryLimit;
import org.trade.dictionary.valuetype.ReferenceTable;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.dao.CodeAttribute;
import org.trade.persistent.dao.CodeType;
import org.trade.persistent.dao.CodeValue;
import org.trade.persistent.dao.Strategy;
import org.trade.strategy.data.IndicatorSeries;
import org.trade.ui.base.BaseButton;
import org.trade.ui.base.BasePanel;
import org.trade.ui.base.BaseUIPropertyCodes;
import org.trade.ui.base.TableModel;
import org.trade.ui.base.TextDialog;
import org.trade.ui.models.AspectTableModel;
import org.trade.ui.models.CodeAttributeTableModel;
import org.trade.ui.models.IndicatorSeriesTableModel;
import org.trade.ui.tables.ConfigurationTable;
import org.trade.ui.widget.ButtonEditor;
import org.trade.ui.widget.ButtonRenderer;
import org.trade.ui.widget.DecodeComboBoxEditor;
import org.trade.ui.widget.DecodeComboBoxRenderer;

/**
 */
public class ConfigurationPanel extends BasePanel {

	private static final long serialVersionUID = 8543984162821384818L;

	private JScrollPane m_jScrollPane = null;
	private JScrollPane m_jScrollPane1 = new JScrollPane();
	private PersistentModel m_tradePersistentModel = null;
	private ConfigurationTable m_table = null;
	private AspectTableModel m_tableModel = null;
	private Aspects m_aspects = null;

	private ConfigurationTable m_tableChild = null;
	private TableModel m_tableModelChild = null;
	private BaseButton propertiesButton = null;
	private DecodeComboBoxEditor refTableEditorComboBox = null;

	/**
	 * Constructor
	 * 
	 * @param tradePersistentModel
	 *            PersistentModel
	 */
	@SuppressWarnings("unchecked")
	public ConfigurationPanel(PersistentModel tradePersistentModel) {
		try {
			getMenu().addMessageListener(this);
			this.setLayout(new BorderLayout());
			/*
			 * Initialize the ValueType decode tables. This caused the tables to
			 * be cached.
			 */

			DAOEntryLimit.newInstance();
			m_tradePersistentModel = tradePersistentModel;
			m_jScrollPane = new JScrollPane();
			propertiesButton = new BaseButton(this,
					BaseUIPropertyCodes.PROPERTIES, 0);
			propertiesButton.setEnabled(false);
			JLabel refTable = new JLabel("Configuration:");
			refTableEditorComboBox = new DecodeComboBoxEditor(ReferenceTable
					.newInstance().getCodesDecodes());
			DecodeComboBoxRenderer refTableRenderer = new DecodeComboBoxRenderer();
			refTableEditorComboBox.setRenderer(refTableRenderer);
			refTableEditorComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (null != m_tableChild) {
							m_jScrollPane1.getViewport().remove(m_tableChild);
						}
						addReferenceTablePanel(((ReferenceTable) e.getItem())
								.getCode());
					}
				}
			});

			JPanel jPanel2 = new JPanel(new BorderLayout());
			JPanel jPanel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JPanel jPanel4 = new JPanel(new BorderLayout());

			jPanel3.add(refTable, null);
			jPanel3.add(refTableEditorComboBox, null);
			jPanel3.setBorder(new BevelBorder(BevelBorder.RAISED));
			JToolBar jToolBar = new JToolBar();
			jToolBar.setLayout(new BorderLayout());
			jToolBar.add(jPanel3, BorderLayout.WEST);

			jPanel4.add(m_jScrollPane, BorderLayout.CENTER);
			JScrollPane jScrollPane1 = new JScrollPane();
			jScrollPane1.getViewport().add(jPanel4, BorderLayout.NORTH);
			jScrollPane1.setBorder(new BevelBorder(BevelBorder.LOWERED));

			jPanel2.add(m_jScrollPane1, BorderLayout.CENTER);
			JSplitPane jSplitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					true, jPanel4, jPanel2);
			jSplitPane1.setResizeWeight(0.2d);
			jSplitPane1.setOneTouchExpandable(true);
			this.add(jToolBar, BorderLayout.NORTH);
			this.add(jSplitPane1, BorderLayout.CENTER);

		} catch (Exception ex) {
			this.setErrorMessage("Error During Initialization.",
					ex.getMessage(), ex);
		}
	}

	public void doWindowActivated() {
	}

	/**
	 * Method doWindowDeActivated.
	 * 
	 * @return boolean
	 */
	public boolean doWindowDeActivated() {
		if (m_aspects.isDirty()) {
			setStatusBarMessage(
					"Please Save or Refresh as changed are pending",
					BasePanel.WARNING);
			return false;
		}
		return true;
	}

	/**
	 * Method doWindowClose.
	 * 
	 */

	public void doWindowClose() {
	}

	/**
	 * Method doWindowOpen.
	 * 
	 */
	public void doWindowOpen() {
		try {
			this.addReferenceTablePanel(ReferenceTable.newInstance().getCode());
		} catch (Exception ex) {
			this.setErrorMessage("Error during initiaization.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * Method doSave.This is fired when the Save button is pressed.
	 * 
	 */

	public void doSave() {
		try {
			this.setStatusBarMessage("Save in progress ...",
					BasePanel.INFORMATION);
			int selectedRow = m_table.getSelectedRow();
			String className = "org.trade.persistent.dao."
					+ ((ReferenceTable) refTableEditorComboBox
							.getSelectedItem()).getCode();

			for (ListIterator<Aspect> itemIter = m_aspects.getAspect()
					.listIterator(); itemIter.hasNext();) {
				Aspect item = itemIter.next();
				if (item.isDirty()) {
					item = m_tradePersistentModel.persistAspect(item);
				}

				/*
				 * Replace the aspect with the mergedAspect then update the
				 * tables and select the row for the saved data.
				 */
				itemIter.set(item);
			}
			m_aspects.setDirty(false);
			Aspects aspects = m_tradePersistentModel
					.findAspectsByClassName(className);
			for (Aspect currAspect : aspects.getAspect()) {
				boolean exists = false;
				for (Aspect aspect : m_aspects.getAspect()) {
					if (aspect.getId().equals(currAspect.getId())) {
						exists = true;
					}
				}
				if (!exists)
					m_tradePersistentModel.removeAspect(currAspect);
			}
			DBTableLookupServiceProvider.clearLookup();
			doRefresh();
			if (selectedRow == -1)
				selectedRow = 0;
			m_table.setRowSelectionInterval(selectedRow, selectedRow);
		} catch (Exception ex) {
			this.setErrorMessage("Error saving item.", ex.getMessage(), ex);
		}
	}

	/**
	 * Method doSearch This is fired when the Search button is pressed.
	 * 
	 */
	public void doSearch() {
		doRefresh();
	}

	/**
	 * Method doRefresh This is fired when the Refresh button is pressed.
	 * 
	 */
	public void doRefresh() {
		try {
			this.addReferenceTablePanel(((ReferenceTable) refTableEditorComboBox
					.getSelectedItem()).getCode());
		} catch (Exception ex) {
			this.setErrorMessage("Error finding item.", ex.getMessage(), ex);
		} finally {
			clearStatusBarMessage();
		}
	}

	/**
	 * Method doOpen This is fired when the tool-bar File open button is pressed
	 * or the main menu Open File.
	 * 
	 * 
	 */
	public void doOpen() {

	}

	/**
	 * Method doProperties.
	 * 
	 * @param series
	 *            IndicatorSeries
	 */
	public void doProperties(IndicatorSeries series) {
		try {
			this.clearStatusBarMessage();
			String indicatorName = series.getType().substring(0,
					series.getType().indexOf("Series"));
			Aspects aspects = m_tradePersistentModel
					.findAspectsByClassNameFieldName(CodeType.class.getName(),
							"name", indicatorName);
			if (aspects.getAspect().isEmpty()) {
				this.setStatusBarMessage(
						"There are no properties for this Indicator ...\n",
						BasePanel.INFORMATION);
			} else {

				CodeAttributesPanel codeAttributePanel = new CodeAttributesPanel(
						aspects, series);
				if (null != codeAttributePanel) {
					TextDialog dialog = new TextDialog(this.getFrame(),
							"Indicator Properties", true, codeAttributePanel);
					dialog.setLocationRelativeTo(this);
					dialog.setVisible(true);
					if (!dialog.getCancel()) {
						/*
						 * If there are no code values set them up
						 */

						if (series.getCodeValues().isEmpty()) {
							for (Aspect aspect : aspects.getAspect()) {
								CodeType codeType = (CodeType) aspect;
								for (CodeAttribute codeAttribute : codeType
										.getCodeAttribute()) {
									CodeValue codeValue = new CodeValue(
											codeAttribute, null);
									codeValue.setIndicatorSeries(series);
									series.addCodeValue(codeValue);
								}
							}
						}
						/*
						 * Populate the code values from the fields.
						 */
						for (CodeValue codeValue : series.getCodeValues()) {
							series.setDirty(true);
							if (((FormattedTextFieldVerifier) codeAttributePanel
									.getFields()
									.get(codeValue.getCodeAttribute().getName())
									.getInputVerifier()).isValid()) {

								JComponent field = codeAttributePanel
										.getFields().get(
												codeValue.getCodeAttribute()
														.getName());
								if (field instanceof JFormattedTextField) {
									codeValue
											.setCodeValue(((JFormattedTextField) codeAttributePanel
													.getFields()
													.get(codeValue
															.getCodeAttribute()
															.getName()))
													.getText());
								} else if (field instanceof DecodeComboBoxEditor) {
									codeValue
											.setCodeValue(((Decode) ((DecodeComboBoxEditor) codeAttributePanel
													.getFields()
													.get(codeValue
															.getCodeAttribute()
															.getName()))
													.getSelectedItem())
													.getCode());
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			setErrorMessage("Error getting Indicator properties.",
					ex.getMessage(), ex);
		} finally {
			this.getFrame().setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 */
	private class TableRowListener implements ListSelectionListener {

		/**
		 * Method valueChanged.
		 * 
		 * @param event
		 *            ListSelectionEvent
		 * @see javax.swing.event.ListSelectionListener#valueChanged(ListSelectionEvent)
		 */

		public void valueChanged(ListSelectionEvent event) {
			if (!event.getValueIsAdjusting()) {

				ListSelectionModel model = (ListSelectionModel) event
						.getSource();
				if (model.getLeadSelectionIndex() > -1) {

					Aspect transferObject = m_tableModel
							.getData()
							.getAspect()
							.get(m_table.convertRowIndexToModel(model
									.getLeadSelectionIndex()));
					propertiesButton.setEnabled(false);
					setChildPanel(transferObject);
				}
			}
		}
	}

	/**
	 * Method addReferenceTablePanel.
	 * 
	 * @param refTableClass
	 *            String
	 */
	private void addReferenceTablePanel(String refTableClass) {

		try {
			m_aspects = m_tradePersistentModel
					.findAspectsByClassName("org.trade.persistent.dao."
							+ refTableClass);
			Vector<Object> parm = new Vector<Object>();
			m_tableModel = (AspectTableModel) ClassFactory.getCreateClass(
					"org.trade.ui.models." + refTableClass + "TableModel",
					parm, this);

			m_tableModel.setData(m_aspects);
			m_table = new ConfigurationTable(m_tableModel);
			m_table.setFont(new Font("Monospaced", Font.PLAIN, 12));
			m_table.setPreferredScrollableViewportSize(new Dimension(300, 200));
			m_table.setFillsViewportHeight(true);
			m_table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			m_table.getSelectionModel().addListSelectionListener(
					new TableRowListener());
			m_jScrollPane.getViewport().add(m_table, BorderLayout.CENTER);
			m_jScrollPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
			m_jScrollPane.addMouseListener(m_table);
			if (m_aspects.getAspect().size() > 0) {
				m_table.setRowSelectionInterval(0, 0);
			}

		} catch (Exception ex) {
			this.setErrorMessage("Error deleting Strategy.", ex.getMessage(),
					ex);
		}
	}

	/**
	 */
	private class IndicatorSeriesTableRowListener implements
			ListSelectionListener {
		/**
		 * Method valueChanged.
		 * 
		 * @param event
		 *            ListSelectionEvent
		 * @see javax.swing.event.ListSelectionListener#valueChanged(ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent event) {
			if (!event.getValueIsAdjusting()) {

				ListSelectionModel model = (ListSelectionModel) event
						.getSource();
				if (model.getLeadSelectionIndex() > -1) {

					IndicatorSeries transferObject = ((IndicatorSeriesTableModel) m_tableModelChild)
							.getData()
							.getIndicatorSeries()
							.get(m_tableChild.convertRowIndexToModel(model
									.getLeadSelectionIndex()));
					propertiesButton.setTransferObject(transferObject);
					propertiesButton.setEnabled(true);
				}
			}
		}
	}

	/**
	 * Method setChildPanel.
	 * 
	 * @param aspect
	 *            Aspect
	 */
	private void setChildPanel(Aspect aspect) {
		try {
			if (aspect instanceof Strategy) {
				m_tableModelChild = new IndicatorSeriesTableModel();
				((IndicatorSeriesTableModel) m_tableModelChild)
						.setData((Strategy) aspect);
				m_tableChild = new ConfigurationTable(m_tableModelChild);
				m_tableChild.setFont(new Font("Monospaced", Font.PLAIN, 12));
				m_tableChild.setPreferredScrollableViewportSize(new Dimension(
						300, 200));
				m_tableChild.setFillsViewportHeight(true);
				m_tableChild.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
				m_tableChild.getSelectionModel().addListSelectionListener(
						new IndicatorSeriesTableRowListener());
				m_tableChild.setDefaultRenderer(Aspects.class,
						new ButtonRenderer(BaseUIPropertyCodes.PROPERTIES));
				m_tableChild.setDefaultEditor(Aspects.class, new ButtonEditor(
						propertiesButton));
				m_jScrollPane1.getViewport().add(m_tableChild,
						BorderLayout.CENTER);
				m_jScrollPane1.setBorder(new BevelBorder(BevelBorder.LOWERED));
			}
			if (aspect instanceof CodeType) {

				m_tableModelChild = new CodeAttributeTableModel();
				((CodeAttributeTableModel) m_tableModelChild)
						.setData((CodeType) aspect);
				m_tableChild = new ConfigurationTable(m_tableModelChild);
				m_tableChild.setFont(new Font("Monospaced", Font.PLAIN, 12));
				m_tableChild.setPreferredScrollableViewportSize(new Dimension(
						300, 200));
				m_tableChild.setFillsViewportHeight(true);
				m_tableChild.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

				m_jScrollPane1.getViewport().add(m_tableChild,
						BorderLayout.CENTER);
				m_jScrollPane1.setBorder(new BevelBorder(BevelBorder.LOWERED));
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error deleting Strategy.", ex.getMessage(),
					ex);
		}
	}

	/**
	 */
	class CodeAttributesPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5972331201407363985L;
		private Hashtable<String, JComponent> fields = new Hashtable<String, JComponent>();

		/**
		 * Constructor for CodeAttributesPanel.
		 * 
		 * @param aspects
		 *            Aspects
		 * @param series
		 *            IndicatorSeries
		 * @throws Exception
		 */
		@SuppressWarnings("unchecked")
		public CodeAttributesPanel(Aspects aspects, IndicatorSeries series)
				throws Exception {

			for (Aspect aspect : aspects.getAspect()) {
				CodeType codeType = (CodeType) aspect;
				GridBagLayout gridBagLayout1 = new GridBagLayout();
				this.setLayout(gridBagLayout1);
				int i = 0;
				for (CodeAttribute codeAttribute : codeType.getCodeAttribute()) {
					JLabel jLabel = new JLabel(codeAttribute.getName());
					JComponent field = null;
					if (null == codeAttribute.getEditorClassName()) {
						field = new JFormattedTextField();
						field.setInputVerifier(new FormattedTextFieldVerifier());
						for (CodeValue value : series.getCodeValues()) {
							if (value.getCodeAttribute().getName()
									.equals(codeAttribute.getName())) {
								((JFormattedTextField) field).setValue(series
										.getValueCode(codeAttribute.getName()));
								break;
							}
						}
						if (null == ((JFormattedTextField) field).getValue()) {
							Vector<Object> parm = new Vector<Object>();
							parm.add(codeAttribute.getDefaultValue());
							Object codeValue = ClassFactory.getCreateClass(
									codeAttribute.getClassName(), parm, this);
							((JFormattedTextField) field).setValue(codeValue);
						}
					} else {
						Vector<Object> parm = new Vector<Object>();
						Object decode = ClassFactory.getCreateClass(
								codeAttribute.getEditorClassName(), parm, this);
						boolean valueSet = false;
						if (decode instanceof Decode) {
							field = new DecodeComboBoxEditor(
									((Decode) decode).getCodesDecodes());
							field.setInputVerifier(new FormattedTextFieldVerifier());
							DecodeComboBoxRenderer codeRenderer = new DecodeComboBoxRenderer();
							((DecodeComboBoxEditor) field)
									.setRenderer(codeRenderer);
							for (CodeValue value : series.getCodeValues()) {
								if (value.getCodeAttribute().getName()
										.equals(codeAttribute.getName())) {

									((Decode) decode).setValue(series
											.getValueCode(codeAttribute
													.getName()));
									((DecodeComboBoxEditor) field)
											.setItem((Decode) decode);
									valueSet = true;
									break;
								}
							}
						}
						if (!valueSet) {
							((Decode) decode).setValue(codeAttribute
									.getDefaultValue());
							((DecodeComboBoxEditor) field)
									.setItem((Decode) decode);
						}
					}

					fields.put(codeAttribute.getName(), field);
					this.add(jLabel, new GridBagConstraints(0, i, 1, 1, 0.0,
							0.0, GridBagConstraints.WEST,
							GridBagConstraints.NONE, new Insets(1, 1, 0, 0),
							20, 5));
					this.add(field, new GridBagConstraints(1, i, 1, 1, 1.0,
							0.0, GridBagConstraints.WEST,
							GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0,
									43), 196, 0));
					i++;
				}
			}
		}

		/**
		 * Method getFields.
		 * 
		 * @return Hashtable<String,JComponent>
		 */
		public Hashtable<String, JComponent> getFields() {
			return this.fields;
		}
	}

	/**
	 */
	public class FormattedTextFieldVerifier extends InputVerifier {

		private boolean valid = true;

		/**
		 * Method verify.
		 * 
		 * @param input
		 *            JComponent
		 * @return boolean
		 */
		public boolean verify(JComponent input) {
			if (input instanceof JFormattedTextField) {
				JFormattedTextField ftf = (JFormattedTextField) input;
				AbstractFormatter formatter = ftf.getFormatter();
				if (formatter != null) {
					String text = ftf.getText();
					try {
						formatter.stringToValue(text);
						ftf.setBackground(null);
						valid = true;
					} catch (ParseException pe) {
						ftf.setBackground(Color.red);
						valid = false;
					}
				}
			}
			return valid;
		}

		/**
		 * Method shouldYieldFocus.
		 * 
		 * @param input
		 *            JComponent
		 * @return boolean
		 */
		public boolean shouldYieldFocus(JComponent input) {
			return verify(input);
		}

		/**
		 * Method isValid.
		 * 
		 * @return boolean
		 */
		public boolean isValid() {
			return valid;
		}
	}
}
