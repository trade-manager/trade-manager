package org.trade.ui.configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JFormattedTextField.AbstractFormatter;

import org.trade.core.factory.ClassFactory;
import org.trade.core.valuetype.Decode;
import org.trade.persistent.dao.CodeAttribute;
import org.trade.persistent.dao.CodeType;
import org.trade.persistent.dao.CodeValue;
import org.trade.ui.widget.DecodeComboBoxEditor;
import org.trade.ui.widget.DecodeComboBoxRenderer;

public class CodeAttributePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5972331201407363985L;
	private Hashtable<String, JComponent> fields = new Hashtable<String, JComponent>();
	private CodeType codeType = null;
	private List<CodeValue> currentCodeValues = null;

	/**
	 * Constructor for CodeAttributesPanel.
	 * 
	 * @param aspects
	 *            Aspects
	 * @param series
	 *            IndicatorSeries
	 * @throws Exception
	 */
	public CodeAttributePanel(CodeType codeType,
			List<CodeValue> currentCodeValues) throws Exception {

		this.codeType = codeType;
		this.currentCodeValues = currentCodeValues;
		GridBagLayout gridBagLayout1 = new GridBagLayout();
		JPanel jPanel1 = new JPanel(gridBagLayout1);
		this.setLayout(new BorderLayout());

		int i = 0;
		for (CodeAttribute codeAttribute : this.codeType.getCodeAttribute()) {
			JLabel jLabel = new JLabel(codeAttribute.getName() + ": ");
			jLabel.setToolTipText(codeAttribute.getDescription());
			jLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
			jLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			JComponent field = null;
			if (null == codeAttribute.getEditorClassName()) {
				field = new JFormattedTextField();
				field.setInputVerifier(new FormattedTextFieldVerifier());
				for (CodeValue value : this.currentCodeValues) {
					if (value.getCodeAttribute().getName()
							.equals(codeAttribute.getName())) {
						((JFormattedTextField) field).setValue(CodeValue
								.getValueCode(codeAttribute.getName(),
										this.currentCodeValues));
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
					((DecodeComboBoxEditor) field).setRenderer(codeRenderer);
					for (CodeValue value : this.currentCodeValues) {
						if (value.getCodeAttribute().getName()
								.equals(codeAttribute.getName())) {

							((Decode) decode).setValue(CodeValue.getValueCode(
									codeAttribute.getName(),
									this.currentCodeValues));
							((DecodeComboBoxEditor) field)
									.setItem((Decode) decode);
							valueSet = true;
							break;
						}
					}
					if (!valueSet) {
						((Decode) decode).setValue(codeAttribute
								.getDefaultValue());
						((DecodeComboBoxEditor) field).setItem((Decode) decode);
					}
				} else {
					continue;
				}
			}

			fields.put(codeAttribute.getName(), field);
			jPanel1.add(jLabel, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.NONE,
					new Insets(2, 2, 2, 2), 20, 5));
			jPanel1.add(field, new GridBagConstraints(1, i, 1, 1, 1.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
					new Insets(2, 2, 2, 20), 20, 5));
			i++;
		}
		this.add(jPanel1);
	}

	public List<CodeValue> getCodeValues() {

		for (CodeAttribute codeAttribute : this.codeType.getCodeAttribute()) {
			if (((FormattedTextFieldVerifier) this.fields.get(
					codeAttribute.getName()).getInputVerifier()).isValid()) {

				JComponent field = this.fields.get(codeAttribute.getName());
				String newValue = null;
				if (field instanceof JFormattedTextField) {
					newValue = (((JFormattedTextField) this.fields
							.get(codeAttribute.getName())).getText());
				} else if (field instanceof DecodeComboBoxEditor) {
					newValue = ((Decode) ((DecodeComboBoxEditor) this.fields
							.get(codeAttribute.getName())).getSelectedItem())
							.getCode();
				}
				/*
				 * Add code values or updated the current ones. Note there
				 * should always be an equal number for attributes and values.
				 */
				if (this.currentCodeValues.size() < this.codeType
						.getCodeAttribute().size()) {
					this.currentCodeValues.add(new CodeValue(codeAttribute,
							newValue));
				} else {
					for (CodeValue codeValue : this.currentCodeValues) {
						if (codeValue.getCodeAttribute().getName()
								.equals(codeAttribute.getName())) {
							codeValue.setCodeValue(newValue);
						}
					}
				}
			}
		}
		return this.currentCodeValues;
	}

	class FormattedTextFieldVerifier extends InputVerifier {

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