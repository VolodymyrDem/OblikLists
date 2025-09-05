package com.work.oblikpodorojlist.utils;


import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;

public class ResourceUtils {

    /**
     * Повертає папку, в якій лежать ресурси:
     *  • в режимі розробки — src/main/resources
     *  • в запечененому JAR — шукає поруч із JAR-ником папку resources
     *  • інакше — корінь classpath
     */
    public static Path getResourcesFolder() {
        // 1) Спроба визначити, чи ми запускаємося з JAR
        try {
            CodeSource src = ResourceUtils.class.getProtectionDomain().getCodeSource();
            if (src != null) {
                URL location = src.getLocation();
                Path path = Paths.get(location.toURI());
                if (path.toString().endsWith(".jar")) {
                    // знаходимо директорію, де лежить JAR, і припускаємо, що поруч є папка resources
                    Path jarDir = path.getParent();
                    Path external = jarDir.resolve("resources");
                    if (Files.isDirectory(external)) {
                        return external;
                    }
                }
            }
        } catch (URISyntaxException ignored) { }

        // 2) Якщо це IDE / dev, шукаємо src/main/resources
        Path dev = Paths.get(System.getProperty("user.dir"), "src", "main", "resources");
        if (Files.isDirectory(dev)) {
            return dev;
        }

        // 3) Фолбек на classpath root
        URL classpathRoot = Thread.currentThread()
                .getContextClassLoader()
                .getResource("");
        if (classpathRoot != null) {
            try {
                Path cp = Paths.get(classpathRoot.toURI());
                if (Files.isDirectory(cp)) {
                    return cp;
                }
            } catch (URISyntaxException ignored) { }
        }

        // 4) Останній фолбек — робоча директорія
        return Paths.get(System.getProperty("user.dir"));
    }
}
