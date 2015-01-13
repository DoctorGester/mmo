package core.handlers.terminal;

import java.lang.reflect.Field;

public class Argument {
	private String name;
	private Field linkedField;
	private boolean mandatory;
	private boolean variableArity;
	private int arity;

	public boolean isVariableArity() {
		return variableArity;
	}

	public void setVariableArity(boolean variableArity) {
		this.variableArity = variableArity;
	}

	public int getArity() {
		return arity;
	}

	public void setArity(int arity) {
		this.arity = arity;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Field getLinkedField() {
		return linkedField;
	}

	public void setLinkedField(Field linkedField) {
		this.linkedField = linkedField;
	}

	@Override
	public String toString() {
		return name;
	}
}
