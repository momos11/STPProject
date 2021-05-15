package de.uniks.stp.builder;

import de.uniks.stp.model.CurrentUser;
import de.uniks.stp.model.Server;
import de.uniks.stp.model.User;

import java.util.Collections;
import java.util.List;

public class ModelBuilder {
    private Server currentServer;
    private CurrentUser personalUser;

    /////////////////////////////////////////
    //  Setter
    /////////////////////////////////////////

    public void buildPersonalUser(String name, String userKey) {
        personalUser = new CurrentUser().setName(name).setUserKey(userKey);
    }

    public User buildUser(String name, String id) {
        for (User user : personalUser.getUser()) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        User newUser = new User().setName(name).setId(id).setStatus(true);
        personalUser.withUser(newUser);
        return newUser;
    }

    public User buildServerUser(String name, String id, Boolean status) {
        for (User user : currentServer.getUser()) {
            if (user.getId().equals(id)) {
                if (user.isStatus() == status) {
                    return user;
                } else {
                    currentServer.withoutUser(user);
                    User updatedUser = new User().setName(name).setId(id).setStatus(status);
                    currentServer.withUser(updatedUser);
                    return updatedUser;
                }
            }
        }
        User newUser = new User().setName(name).setId(id).setStatus(status);
        currentServer.withUser(newUser);
        return newUser;
    }

    public Server buildServer(String name, String id) {
        for (Server server : personalUser.getServer()) {
            if (server.getId().equals(id)) {
                return server;
            }
        }
        Server newServer = new Server().setName(name).setId(id);
        personalUser.withServer(newServer);
        return newServer;
    }

    public void setCurrentServer(Server currentServer) {
        this.currentServer = currentServer;
    }


    /////////////////////////////////////////
    //  Getter
    /////////////////////////////////////////

    public List<Server> getServers() {
        return this.personalUser.getServer() != null ? Collections.unmodifiableList(this.personalUser.getServer()) : Collections.emptyList();
    }

    public CurrentUser getPersonalUser() {
        return personalUser;
    }

    public Server getCurrentServer() {
        return currentServer;
    }


}