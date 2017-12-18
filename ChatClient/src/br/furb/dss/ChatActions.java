package br.furb.dss;

public class ChatActions {

	private final int MAX_NAME_SIZE = 10;

	private MessageEncryptor encryptor;

	public ChatActions(MessageEncryptor encryptor) {
		this.encryptor = encryptor;
	}

	public void sendAction(EChatActions action, String... value) {

		switch (action) {

		case CHANGE_USERNAME:
			//sendUsername(value[1]);
			break;

		}

	}

	private void sendUsername(String value) {

		String msg = "/changeuser ";

		if (value != null && !value.isEmpty()) {
			msg += String.format("%1$" + MAX_NAME_SIZE + "s", value);
		}

		encryptor.encryptToServer(msg);
	}

}
