package studio.avis.keylogger.storage;

import studio.avis.keylogger.storage.data.Data;

import java.io.*;
import java.util.HashSet;
import java.util.Optional;

public class KeyLogStorage {

    public static Optional<Data> data = Optional.empty();

    public static void save(String filename, Data record) {
        try(FileOutputStream fileOutputStream = new FileOutputStream(filename);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

            objectOutputStream.writeObject(record);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Optional<Data> load(String filename) {
        Optional<Data> data = Optional.empty();
        try(FileInputStream fileInputStream = new FileInputStream(filename);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            Data object = (Data) objectInputStream.readObject();
            object.setDownKeys(new HashSet<>());
            object.setDownButtons(new HashSet<>());

            data = Optional.ofNullable(object);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

}
