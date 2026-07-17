package server.backup;

import server.club.ClubService;
import server.model.Club;
import server.model.User;
import server.repository.UserRepository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupService {

    private final UserRepository userRepository;
    private final ClubService clubService;
    private final Path backupPath;
    private final ScheduledExecutorService scheduler;
    private final Object lock;

    public BackupService(UserRepository userRepository, ClubService clubService, Path backupPath) {
        this.userRepository = userRepository;
        this.clubService = clubService;
        this.backupPath = backupPath;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.lock = new Object();
    }

    public void restoreIfExists() {
        if (!Files.exists(backupPath)) {
            return;
        }

        synchronized (lock) {
            try (ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(backupPath))) {
                BackupState state = (BackupState) inputStream.readObject();
                userRepository.replaceAll(state.users);
                clubService.replaceAll(state.clubs);
                System.out.println("Backup restored from " + backupPath);
            } catch (IOException | ClassNotFoundException exception) {
                System.out.println("Could not restore backup: " + exception.getMessage());
            }
        }
    }

    public void startScheduledBackup(long minutes) {
        scheduler.scheduleAtFixedRate(this::backupNow, minutes, minutes, TimeUnit.MINUTES);
    }

    public void backupNow() {
        synchronized (lock) {
            try {
                Files.createDirectories(backupPath.getParent());
                Path tempPath = backupPath.resolveSibling(backupPath.getFileName() + ".tmp");
                BackupState state = new BackupState(userRepository.findAll(), clubService.getAllClubs());

                try (ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(tempPath))) {
                    outputStream.writeObject(state);
                }

                Files.move(tempPath, backupPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException exception) {
                System.out.println("Backup failed: " + exception.getMessage());
            }
        }
    }

    public void shutdown() {
        backupNow();
        scheduler.shutdownNow();
    }

    private static class BackupState implements Serializable {
        private final List<User> users;
        private final List<Club> clubs;

        private BackupState(List<User> users, List<Club> clubs) {
            this.users = new ArrayList<>(users);
            this.clubs = new ArrayList<>(clubs);
        }
    }
}
