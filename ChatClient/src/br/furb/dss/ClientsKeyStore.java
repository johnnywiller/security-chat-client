package br.furb.dss;

import java.util.HashMap;

public class ClientsKeyStore {

	private HashMap<String, DestinationUser> users = new HashMap<>();

	private static ClientsKeyStore instance;

	private ClientsKeyStore() {

	}

	public static ClientsKeyStore getInstance() {

		if (instance == null)
			instance = new ClientsKeyStore();

		return instance;
	}

	public void addUser(DestinationUser user) {
		users.put(user.getName(), user);
	}

	public DestinationUser getUser(String name) {
		return users.get(name);
	}

}
