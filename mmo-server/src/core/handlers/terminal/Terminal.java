package core.handlers.terminal;

import java.lang.reflect.Field;
import java.util.*;

public class Terminal {
	private static final String COMMAND_NOT_FOUND_ERROR = "Command %s is not registered";

	private Map<String, Class<? extends Command>> registeredCommands;

	public Terminal(){
		registeredCommands = new HashMap<String, Class<? extends Command>>();
	}

	public void registerCommand(String name, Class<? extends Command> commandClass){
		registeredCommands.put(name, commandClass);
	}

	private static void convertAndWriteToField(Object to, Field field, String value) throws IllegalAccessException{
		Class<?> type = field.getType();

		Object converted = value;

		if (type == int.class)
			converted = Integer.valueOf(value);
		else if (type == boolean.class)
			converted = Boolean.valueOf(value);
		else if (type == double.class)
			converted = Double.valueOf(value);
		else if (type == float.class)
			converted = Float.valueOf(value);

		if (!field.isAccessible())
			field.setAccessible(true);

		field.set(to, converted);
	}

	private static void writeToField(Object to, Field field, Object value) throws IllegalAccessException{
		if (!field.isAccessible())
			field.setAccessible(true);

		field.set(to, value);
	}

	public String parseCommand(String command) throws Exception {
		String parts[] = command.split("[ \t\n\f\r]", 2);
		String commandName = parts[0];

		Class<? extends Command> commandClass = registeredCommands.get(commandName);

		if (commandClass == null)
			throw new IllegalArgumentException(String.format(COMMAND_NOT_FOUND_ERROR, commandName));

		Map<String, Argument> arguments = new HashMap<String, Argument>();
		Set<Argument> mandatoryArguments = new HashSet<Argument>();

		// Reading annotations
		for (Field field: commandClass.getDeclaredFields()){
			CommandArgument commandArgument = field.getAnnotation(CommandArgument.class);

			if (commandArgument == null)
				continue;

			boolean mandatory = commandArgument.mandatory();
			String name = commandArgument.name();

			name = name.isEmpty() ? field.getName() : name;

			Argument argument = new Argument();
			argument.setName(name);
			argument.setMandatory(mandatory);
			argument.setLinkedField(field);
			argument.setArity(commandArgument.arity());
			argument.setVariableArity(commandArgument.variableArity());

			if (mandatory)
				mandatoryArguments.add(argument);

			arguments.put(name, argument);
		}

		// Splitting into tokens
		List<String> tokens = splitStringToTokens(parts[1]);

		// Actually parsing and writing
		Command instance = commandClass.newInstance();

		Argument currentArgument = null;
		List<String> argumentValueList = new LinkedList<String>();

		for (String token: tokens){
			if (token.startsWith("-")){
				if (currentArgument != null && currentArgument.isVariableArity()){
					Field field = currentArgument.getLinkedField();

					writeToField(instance, field, argumentValueList);
					argumentValueList = new LinkedList<String>();

					mandatoryArguments.remove(currentArgument);
				}

				currentArgument = arguments.get(token.substring(1));
			} else if (currentArgument != null) {
				if (token.startsWith("\"") && token.endsWith("\""))
					token = token.substring(1, token.length() - 1);

				Field field = currentArgument.getLinkedField();

				if (currentArgument.getArity() != 0 || currentArgument.isVariableArity()){
					argumentValueList.add(token);

					if (argumentValueList.size() == currentArgument.getArity()){
						writeToField(instance, field, argumentValueList);
						argumentValueList = new LinkedList<String>();

						mandatoryArguments.remove(currentArgument);
					}
				} else {
					convertAndWriteToField(instance, field, token);
					mandatoryArguments.remove(currentArgument);

					currentArgument = null;
				}
			}
		}

		if (!mandatoryArguments.isEmpty())
			throw new IllegalArgumentException("Command requirements were not met: " + mandatoryArguments);

		return instance.execute();
	}

	private static List<String> splitStringToTokens(String part) {
		List<String> tokens = new LinkedList<String>();
		StringBuilder current = new StringBuilder();
		boolean insideQuotes = false;

		for (int i = 0; i < part.length(); i++){
			char c = part.charAt(i);

			if (insideQuotes){
				current.append(c);
				if (c == '"'){
					insideQuotes = false;
					tokens.add(current.toString());
					current.setLength(0);
				}
			} else {
				if (c == '\n' || c == ' ' || c == '\t' || c == '\f' || c == '\r'){
					if (current.length() != 0)
						tokens.add(current.toString());
					current.setLength(0);
				} else {
					current.append(c);

					if (c == '"')
						insideQuotes = true;
				}
			}
		}

		if (current.length() != 0)
			tokens.add(current.toString());
		return tokens;
	}

	public static void main(String ... args){
		try {
			Terminal terminal = new Terminal();
			terminal.registerCommand("msg", MsgCommand.class);
			terminal.parseCommand("msg -to Richard -msg Test_Information -precision 0.25 5");
			terminal.parseCommand("msg -msg \"Long message. Be careful about it. 2 - 4 = -2\" -precision 1 15.0");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class MsgCommand implements Command{
		public MsgCommand(){}

		@CommandArgument(mandatory = false)
		private String to = "self";

		@CommandArgument(arity = 2)
		private List<String> precision;

		@CommandArgument(name = "msg")
		private String message;

		@Override
		public String execute() throws Exception {
			System.out.printf("Message to %s: %s / %s\n", to, message, precision);
			return null;
		}
	}
}
