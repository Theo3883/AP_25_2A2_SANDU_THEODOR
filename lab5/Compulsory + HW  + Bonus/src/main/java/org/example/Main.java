package org.example;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class Main {
    public static void main(String[] args) throws IOException {
        Main app = new Main();
        //app.testRepo();
        app.shell();

    }

    /*private void testRepo() throws IOException {
        var repo = new Repository();
        //repo.add(new Image("Duke", LocalTime.of(11,0), "C:\\Users\\teosa\\Desktop\\repotest\\test1.png" ));
        //repo.add(new Image("Hatz", LocalTime.of(11,30), "C:\\Users\\teosa\\Desktop\\repotest\\test2.png" ));
        repo.addAll("C:\\Users\\teosa\\Desktop");
        //repo.printImages();
    }*/

    private void shell() throws IOException {
        var shell = new Shell();
        shell.run();
    }
}