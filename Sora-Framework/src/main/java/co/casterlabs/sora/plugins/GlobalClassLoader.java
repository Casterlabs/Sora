package co.casterlabs.sora.plugins;

import org.jetbrains.annotations.Nullable;

public class GlobalClassLoader extends ClassLoader {
    public static final GlobalClassLoader instance = new GlobalClassLoader();

    private static final ClassLoader parentClassLoader = GlobalClassLoader.class.getClassLoader();

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // TODO a devilishly terrible idea.
        // Have all plugin classloaders created through this class, and link them all
        // together so they can "see" eachother's classes (but not resources!).

        return null;
    }

    private @Nullable Class<?> safeLoadClass(ClassLoader loader, String name) {
        try {
            return loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
