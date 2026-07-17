package server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Club implements Serializable {
    private int id;
    private String name;
    private String ownerUsername;
    private Set<String> members;
    private Set<String> pendingRequests;
    private Fundraiser activeFundraiser;
    private List<ClubComment> comments;

    public  Club(int id, String name, String ownerUsername) {
        if (id <= 0) throw  new IllegalArgumentException("id must be positive");
        if (name == null || name.isBlank()) throw  new IllegalArgumentException("name must not be null");
        if (ownerUsername == null || ownerUsername.isBlank()) throw  new IllegalArgumentException("ownerUsername must not be null");

        this.id = id;
        this.name = name;
        this.ownerUsername = ownerUsername;
        members = new HashSet<String>();
        members.add(ownerUsername);
        pendingRequests = new HashSet<String>();
        comments = new ArrayList<>();
    }

    public boolean isOwner(String ownerUsername) {
        if (ownerUsername == null || ownerUsername.isBlank()) throw  new IllegalArgumentException("ownerUsername must not be null");
        return this.ownerUsername.equals(ownerUsername);
    }

    public boolean isMember(String memberUsername) {
        if (memberUsername == null || memberUsername.isBlank())  throw new IllegalArgumentException("memberUsername must not be null");
        return members.contains(memberUsername);
    }

    public synchronized void addJoinRequest(String memberUsername) {
        if(memberUsername == null || memberUsername.isBlank()) throw new IllegalArgumentException("memberUsername must not be null");
        if (members.contains(memberUsername)) throw new IllegalArgumentException("memberUsername is in the club");
        if (pendingRequests.contains(memberUsername)) {
            throw new IllegalArgumentException("memberUsername is already pending");
        }
        pendingRequests.add(memberUsername);
    }

    public synchronized void acceptMember(String memberUsername) {
        if(memberUsername == null || memberUsername.isBlank()) throw new IllegalArgumentException("memberUsername must not be null");
        if(members.contains(memberUsername)) return;
        if(!pendingRequests.contains(memberUsername))
            throw new IllegalArgumentException("memberUsername is not in the pending request");
        pendingRequests.remove(memberUsername);
        members.add(memberUsername);
    }

    public synchronized void denyMember(String memberUsername) {
        if(memberUsername == null || memberUsername.isBlank()) throw new IllegalArgumentException("memberUsername must not be null");
        if(!pendingRequests.contains(memberUsername))
            throw new IllegalArgumentException("memberUsername is not in the pending request");
        pendingRequests.remove(memberUsername);
    }

    public synchronized void removeMember(String memberUsername) {
        if (memberUsername == null || memberUsername.isBlank()) throw new IllegalArgumentException("memberUsername must not be null");
        if(memberUsername.equals(ownerUsername)) throw new IllegalArgumentException("you cant remove yourself");
        if (!members.contains(memberUsername)) throw new IllegalArgumentException("memberUsername is not a member");
        members.remove(memberUsername);
    }

    public synchronized void addComment(String username, String text) {
        if (!members.contains(username)) {
            throw new IllegalArgumentException("Only club members can comment");
        }
        comments.add(new ClubComment(username, text));
    }

    public synchronized void setActiveFundraiser(Fundraiser fundraiser) {
        if (fundraiser == null) {
            throw new IllegalArgumentException("Fundraiser cannot be null");
        }
        if (hasActiveFundraiser()) {
            throw new IllegalStateException("Club already has active fundraiser");
        }
        this.activeFundraiser = fundraiser;
    }

    public synchronized void clearActiveFundraiser() {
        if (activeFundraiser != null && activeFundraiser.isComplete()) {
            activeFundraiser = null;
        }
    }

    public boolean hasActiveFundraiser() {
        return activeFundraiser != null;
    }

    public Fundraiser getActiveFundraiser() {
        return activeFundraiser;
    }
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getOwnerUsername() {
        return ownerUsername;
    }
    public Set<String> getMembers() {
        return new HashSet<>(members);
    }
    public Set<String> getPendingRequests() {
        return new HashSet<>(pendingRequests);
    }

    public synchronized List<ClubComment> getComments() {
        return new ArrayList<>(comments);
    }
}
