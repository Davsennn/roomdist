import me.davsennn.FileIO;
import me.davsennn.algorithm.PreferenceGroupEngine;
import me.davsennn.algorithm.Person;
import me.davsennn.algorithm.Room;

import javax.naming.SizeLimitExceededException;

import static me.davsennn.GUI.ROOT;

void main() {
    testPreferenceMerging();
}

List<Person> testSubjects;
List<Room> testChambers;

@SuppressWarnings("SameParameterValue")
void loadPeople(String fileID) {
    File peopleFile = new File(ROOT + "\\src\\test\\resources\\"+fileID+".csv");
    testSubjects = FileIO.parsePeople(peopleFile);
}

void loadRooms() {
    File roomsFile = new File(ROOT + "\\src\\test\\resources\\rooms.csv");
    testChambers = FileIO.parseRooms(roomsFile);
}

void testPreferenceMerging() {
    loadPeople("shuffledpeople");
    loadRooms();

    Person.addPeople(testSubjects);
    try {
        PreferenceGroupEngine.execute();
    } catch (SizeLimitExceededException e) {
        throw new RuntimeException(e);
    }
}
