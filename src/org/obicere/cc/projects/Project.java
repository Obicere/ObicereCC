package org.obicere.cc.projects;

import org.obicere.cc.configuration.Global.Paths;
import org.obicere.cc.executor.language.Language;
import org.obicere.cc.methods.IOUtils;
import org.obicere.cc.methods.Reflection;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class Project {

    public static final String[]            DIFFICULTY = new String[]{"Beginner", "Intermediate", "Advanced", "Challenging", "Legendary"};
    public static final LinkedList<Project> DATA       = new LinkedList<>();

    private final String   name;
    private final Manifest manifest;
    private final Class<?> runner;

    public Project(final Class<?> runner, final String name) throws ClassNotFoundException {
        this.runner = runner;
        this.name = name;
        this.manifest = runner.getAnnotation(Manifest.class);
    }

    public static void loadCurrent() {
        final LinkedList<Class<?>> list = Reflection.loadClassesFrom(Paths.SOURCE);
        final Class<Runner> cls = Runner.class;
        Reflection.filterAsSubclassOf(cls, list);
        list.forEach(Project::add);
    }

    private static void add(final Class<?> cls) {
        try {
            final String name = cls.getSimpleName();
            DATA.add(new Project(cls, name.substring(0, name.length() - 6)));  // Remove Runner
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return manifest.description();
    }

    public double getVersion() {
        return manifest.version();
    }

    public String getAuthor() {
        return manifest.author();
    }

    public int getDifficulty() {
        return manifest.difficulty();
    }

    public String getSortName() {
        return manifest.difficulty() + getName();
    }

    public String getCurrentCode(final Language language) {
        try {
            final File file = getFile(language);
            if (file.exists()) {
                final byte[] data = IOUtils.readData(file);
                return new String(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return language.getSkeleton(this);
    }

    public File getFile(final Language language) {
        return new File(language.getDirectory(), name + language.getSourceExtension());
    }

    public File getFileName(final Language language) {
        return new File(language.getDirectory(), name);
    }

    public Class<?> getRunner() {
        return runner;
    }

    @Override
    public int hashCode() {
        return name.hashCode() * 31 + manifest.difficulty() * 17;
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof Project && o.hashCode() == this.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean save(final String code, final Language language) {
        try {
            IOUtils.write(getFile(language), code.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}