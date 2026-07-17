package server.club;

import common.Result;
import server.model.Club;
import server.model.User;
import server.notif.NotificationService;
import server.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClubService {

    private final ConcurrentHashMap<Integer, Club> clubs;
    private final AtomicInteger nextClubId;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ClubService(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.clubs = new ConcurrentHashMap<>();
        this.nextClubId = new AtomicInteger(1);
    }

    public Result<Club> createClub(User owner, String name) {
        if (owner == null) {
            return Result.error("Unauthorized.");
        }
        if (name == null || name.isBlank()) {
            return Result.error("Club name is required.");
        }

        int id = nextClubId.getAndIncrement();
        Club club = new Club(id, name.trim(), owner.getUsername());
        clubs.put(id, club);
        owner.joinClub(id);

        return Result.success("Club created.", club);
    }

    public Result<List<String>> listMyClubs(User user) {
        if (user == null) {
            return Result.error("Unauthorized.");
        }

        List<String> result = new ArrayList<>();
        for (Integer clubId : user.getClubIds()) {
            Club club = clubs.get(clubId);
            if (club != null) {
                result.add(toDisplay(club));
            }
        }
        result.sort(String::compareTo);

        return Result.success("Clubs loaded.", result);
    }

    public Result<List<String>> listAllClubs(User user) {
        if (user == null) {
            return Result.error("Unauthorized.");
        }

        List<Club> sorted = new ArrayList<>(clubs.values());
        sorted.sort(Comparator.comparingInt(Club::getId));

        List<String> result = new ArrayList<>();
        for (Club club : sorted) {
            result.add(toDisplay(club));
        }

        return Result.success("All clubs loaded.", result);
    }

    public Result<Void> requestJoin(User user, int clubId) {
        if (user == null) {
            return Result.error("Unauthorized.");
        }

        Club club = clubs.get(clubId);
        if (club == null) {
            return Result.error("Club not found.");
        }

        try {
            club.addJoinRequest(user.getUsername());
            notificationService.sendToUser(
                    club.getOwnerUsername(),
                    user.getUsername() + " requested to join club " + club.getName());
            return Result.success("Join request sent.");
        } catch (RuntimeException exception) {
            return Result.error(exception.getMessage());
        }
    }

    public Result<Void> acceptJoin(User owner, int clubId, String username) {
        return answerJoin(owner, clubId, username, true);
    }

    public Result<Void> denyJoin(User owner, int clubId, String username) {
        return answerJoin(owner, clubId, username, false);
    }

    public Result<Club> viewClub(User user, int clubId) {
        if (user == null) {
            return Result.error("Unauthorized.");
        }

        Club club = clubs.get(clubId);
        if (club == null) {
            return Result.error("Club not found.");
        }
        if (!club.isMember(user.getUsername())) {
            return Result.error("You are not a member of this club.");
        }

        return Result.success("Club opened.", club);
    }

    public Result<List<String>> listMembers(User user, int clubId) {
        Club club = clubs.get(clubId);
        if (club == null) {
            return Result.error("Club not found.");
        }
        if (user == null || !club.isMember(user.getUsername())) {
            return Result.error("You are not a member of this club.");
        }

        List<String> members = new ArrayList<>();
        for (String member : club.getMembers()) {
            if (club.isOwner(member)) {
                members.add(member + " (owner)");
            } else {
                members.add(member);
            }
        }
        members.sort(String::compareTo);

        return Result.success("Members loaded.", members);
    }

    public Result<Void> removeMember(User owner, int clubId, String username) {
        Club club = clubs.get(clubId);
        if (club == null) {
            return Result.error("Club not found.");
        }
        if (owner == null || !club.isOwner(owner.getUsername())) {
            return Result.error("Only club owner can remove members.");
        }

        User removed = userRepository.find(username);
        if (removed == null) {
            return Result.error("User not found.");
        }

        try {
            club.removeMember(username);
            removed.leaveClub(clubId);
            notificationService.sendToUser(username, "You were removed from club " + club.getName());
            return Result.success("Member removed.");
        } catch (RuntimeException exception) {
            return Result.error(exception.getMessage());
        }
    }

    public Result<Void> addComment(User user, int clubId, String text) {
        Club club = clubs.get(clubId);
        if (club == null) {
            return Result.error("Club not found.");
        }
        if (user == null || !club.isMember(user.getUsername())) {
            return Result.error("You are not a member of this club.");
        }

        try {
            club.addComment(user.getUsername(), text);
            return Result.success("Comment added.");
        } catch (RuntimeException exception) {
            return Result.error(exception.getMessage());
        }
    }

    public Result<List<String>> listComments(User user, int clubId) {
        Club club = clubs.get(clubId);
        if (club == null) {
            return Result.error("Club not found.");
        }
        if (user == null || !club.isMember(user.getUsername())) {
            return Result.error("You are not a member of this club.");
        }

        List<String> result = new ArrayList<>();
        for (server.model.ClubComment comment : club.getComments()) {
            result.add(comment.toDisplayString());
        }

        return Result.success("Comments loaded.", result);
    }

    public Club findClub(int clubId) {
        return clubs.get(clubId);
    }

    public List<Club> getAllClubs() {
        return new ArrayList<>(clubs.values());
    }

    public void replaceAll(List<Club> restoredClubs) {
        clubs.clear();
        int maxId = 0;
        if (restoredClubs != null) {
            for (Club club : restoredClubs) {
                clubs.put(club.getId(), club);
                if (club.getId() > maxId) {
                    maxId = club.getId();
                }
            }
        }
        nextClubId.set(maxId + 1);
    }

    private Result<Void> answerJoin(User owner, int clubId, String username, boolean accepted) {
        if (owner == null) {
            return Result.error("Unauthorized.");
        }
        if (username == null || username.isBlank()) {
            return Result.error("Username is required.");
        }

        Club club = clubs.get(clubId);
        if (club == null) {
            return Result.error("Club not found.");
        }
        if (!club.isOwner(owner.getUsername())) {
            return Result.error("Only club owner can answer join requests.");
        }

        User requester = userRepository.find(username);
        if (requester == null) {
            return Result.error("User not found.");
        }

        try {
            if (accepted) {
                club.acceptMember(username);
                requester.joinClub(clubId);
                notificationService.sendToUser(username, "Your join request for club " + club.getName() + " was accepted.");
                return Result.success("Join request accepted.");
            }

            club.denyMember(username);
            notificationService.sendToUser(username, "Your join request for club " + club.getName() + " was denied.");
            return Result.success("Join request denied.");
        } catch (RuntimeException exception) {
            return Result.error(exception.getMessage());
        }
    }

    private String toDisplay(Club club) {
        return "ID: " + club.getId()
                + " / Name: " + club.getName()
                + " / Owner: " + club.getOwnerUsername()
                + " / Members: " + club.getMembers().size();
    }
}
