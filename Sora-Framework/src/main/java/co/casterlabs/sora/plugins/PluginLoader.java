package co.casterlabs.sora.plugins;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.reflections.Reflections;

import co.casterlabs.sora.Sora;
import co.casterlabs.sora.api.PluginImplementation;
import co.casterlabs.sora.api.SoraPlugin;
import lombok.NonNull;
import xyz.e3ndr.reflectionlib.helpers.AccessHelper;

public class PluginLoader {

    public static List<SoraPlugin> loadFile(@NonNull Sora sora, @NonNull File file) throws IOException {
        if (file.isFile()) {
            try {
                URLClassLoader classLoader = GlobalClassLoader.create(file.toURI().toURL());

                Reflections reflections = new Reflections(classLoader);

                Set<Class<?>> types = reflections.getTypesAnnotatedWith(PluginImplementation.class);

                List<SoraPlugin> plugins = new LinkedList<>();

                // Free an ungodly amount of ram, Reflections seems to be inefficient.
                reflections = null;
                System.gc();

                // TODO just iterate over all of the files (ZipEntry) and scan for the
                // annotations, it's so much better that way.
                // Steal the code from casterlabs-caffeinated, lol.

                if (types.isEmpty()) {
                    classLoader.close();

                    classLoader = null;

                    throw new IOException("No implementations are present");
                } else {
                    for (Class<?> clazz : types) {
                        if (SoraPlugin.class.isAssignableFrom(clazz)) {
                            try {
                                SoraPlugin plugin = (SoraPlugin) clazz.newInstance();
                                ServiceLoader<Driver> sqlDrivers = ServiceLoader.load(java.sql.Driver.class, classLoader);

                                Field classLoaderField = SoraPlugin.class.getDeclaredField("classLoader");
                                Field sqlDriversField = SoraPlugin.class.getDeclaredField("sqlDrivers");

                                AccessHelper.makeAccessible(classLoaderField);
                                AccessHelper.makeAccessible(sqlDriversField);

                                classLoaderField.set(plugin, classLoader);
                                sqlDriversField.set(plugin, sqlDrivers);

                                // Load them in
                                for (Driver driver : sqlDrivers) {
                                    driver.getClass().toString();
                                }

                                plugins.add(plugin);
                            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException | NoSuchFieldException e) {
                                throw new IOException("Unable to load plugin", e);
                            }
                        }
                    }
                }

                return plugins;
            } catch (MalformedURLException e) {
                throw new IOException("Unable to load file", e);
            }
        } else {
            throw new IOException("Target plugin must be a valid file");
        }
    }

}
