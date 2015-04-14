package org.trade.ui.configuration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.util.ArrayList;
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
				for (CodeValue value : currentCodeValues) {
					if (value.getCodeAttribute().getName()
							.equals(codeAttribute.getName())) {
						((JFormattedTextField) field).setValue(getValueCode(
								codeAttribute.getName(), currentCodeValues));
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
					for (CodeValue value : currentCodeValues) {
						if (value.getCodeAttribute().getName()
								.equals(codeAttribute.getName())) {

							((Decode) decode)
									.setValue(getValueCode(
											codeAttribute.getName(),
											currentCodeValues));
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

		List<CodeValue> codeValues = new ArrayList<>();
		for (CodeAttribute codeAttribute : this.codeType.getCodeAttribute()) {

			if (((FormattedTextFieldVerifier) this.fields.get(
					codeAttribute.getName()).getInputVerifier()).isValid()) {

				JComponent field = this.fields.get(codeAttribute.getName());
				if (field instanceof JFormattedTextField) {
					codeValues.add(new CodeValue(codeAttribute,
							(((JFormattedTextField) this.fields
									.get(codeAttribute.getName())).getText())));
				} else if (field instanceof DecodeComboBoxEditor) {
					codeValues.add(new CodeValue(codeAttribute,
							((Decode) ((DecodeComboBoxEditor) this.fields
									.get(codeAttribute.getName()))
									.getSelectedItem()).getCode()));
				}
			}
		}
		return codeValues;
	}

	/**
	 * Returns the value associated with for the this name attribute name. For
	 * String data types you should define an classEditorName in the
	 * CodeAttribute table, this should be a
	 * org.trade.dictionary.valuetype.Decode These are presented as a combo box
	 * in the UI for editing. all other data types use JFormattedField.
	 * 
	 * @param name
	 *            the name of the attribute.
	 * 
	 * 
	 * @return The value of the attribute.
	 * @throws Exception
	 */

	public Object getValueCode(String name, List<CodeValue> codeValues)
			throws Exception {
		Object codeValue = null;
		for (CodeValue value : codeValues) {
			if (name.equals(value.getCodeAttribute().getName())) {
				Vector<Object> parm = new Vector<Object>();
				parm.add(value.getCodeValue());
				codeValue = ClassFactory.getCreateClass(value
						.getCodeAttribute().getClassName(), parm, this);
				return codeValue;
			}
		}
		return codeValue;
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