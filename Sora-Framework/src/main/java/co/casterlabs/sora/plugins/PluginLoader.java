package co.casterlabs.sora.plugins;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
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
                URL url = file.toURI().toURL();

                URLClassLoader classLoader = new URLClassLoader(new URL[] {
                        url
                }, PluginLoader.class.getClassLoader());

                Reflections reflections = new Reflections(classLoader);
                Set<Class<?>> types = reflections.getTypesAnnotatedWith(PluginImplementation.class);

                List<SoraPlugin> plugins = new ArrayList<>();

                if (types.isEmpty()) {
                    throw new IOException("No implementations are present");
                } else {
                    for (Class<?> clazz : types) {
                        if (SoraPlugin.class.isAssignableFrom(clazz)) {
                            try {
                                SoraPlugin plugin = (SoraPlugin) clazz.newInstance();

                                Field field = SoraPlugin.class.getDeclaredField("classLoader");

                                AccessHelper.makeAccessible(field);

                                field.set(plugin, classLoader);

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
