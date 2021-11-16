package model;

import javafx.application.Platform;
import javafx.scene.control.Label;
import main.Main;

import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.logging.Level;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class FileWatcher extends Thread {
    private final String putanja;
    private final Label brojZarazenih;
    private final Label brojIzljecenih;
    private int before = 0, after = 0, count = 0;

    public FileWatcher(String path, Label brojZarazenih, Label brojIzljecenih) {
        this.putanja = path;
        this.brojIzljecenih = brojIzljecenih;
        this.brojZarazenih = brojZarazenih;
    }

    public void run() {
        try {
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(putanja);
            path.register(watcher, ENTRY_MODIFY);
            System.out.println("Watcher registrovan");
            while (Main.isMainAppRunning()) {
                WatchKey key = null;
                try {
                    key = watcher.take();
                } catch (InterruptedException ex){
                Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
                    return;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) {
                        continue;
                    }
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();
                    //promjena
                    if (kind == ENTRY_MODIFY) {
                        Main.fileLock.lock();
                        try {
                            RandomAccessFile raf = new RandomAccessFile(Main.filePath, "rw");
                            raf.seek(66);
                            String string = raf.readLine();
                            after = Integer.parseInt(string);
                            raf.close();
                        } catch (Exception ex) {
                            Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
                        }
                        Main.fileLock.unlock();

                        if ((after - before) < 0)
                            count += (before - after);
                        before = after;

                        //ne blokira UI thread
                        Platform.runLater(() -> {
                            brojZarazenih.setText(after + "");
                            brojIzljecenih.setText(count + "");
                        });
                    }
                }
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (Exception ex) {
            Main.LOGGER.log( Level.SEVERE, ex.toString(), ex );
        }
    }
}
