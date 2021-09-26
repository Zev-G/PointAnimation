package application;

import java.util.Objects;

public class Res {

    public static String css(String name) {
        return Objects.requireNonNull(Res.class.getClassLoader().getResource(name.endsWith(".css") ? name : name + ".css")).toExternalForm();
    }

}
