package server.repository;

import server.model.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository implements Serializable {

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

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public void replaceAll(List<User> restoredUsers) {
        users.clear();
        if (restoredUsers == null) {
            return;
        }
        for (User user : restoredUsers) {
            save(user);
        }
    }

}
