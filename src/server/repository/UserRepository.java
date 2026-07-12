package server.repository;

import server.model.User;

import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {

    private final ConcurrentHashMap<String, User> users;

    public UserRepository() {

        users = new ConcurrentHashMap<>();

    }

    public boolean exists(String username) {

        return users.containsKey(username);

    }

    public User find(String username) {

        return users.get(username);

    }

    public void save(User user) {

        users.put(user.getUsername(), user);

    }

    public void remove(String username) {

        users.remove(username);

    }

    public boolean saveIfAbsent(User user) {
        return users.putIfAbsent(user.getUsername(), user) == null;
    }

}