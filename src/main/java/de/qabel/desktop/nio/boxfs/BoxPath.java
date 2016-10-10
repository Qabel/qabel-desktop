package de.qabel.desktop.nio.boxfs;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static de.qabel.box.storage.dto.BoxPath.*;

public class BoxPath implements Path {

    private String path;
    private String[] elements;
    private BoxFileSystem fileSystem;

    public BoxPath(BoxFileSystem fileSystem, String path) {
        this.path = path;
        this.fileSystem = fileSystem;

        String workingPath = path;
        List<String> elements = new LinkedList<>();
        String separator = fileSystem.getSeparator();
        while (workingPath.contains(separator)) {
            int index = workingPath.indexOf(separator);
            String element = workingPath.substring(0, index);
            if (!element.isEmpty()) {
                elements.add(element);
            }
            workingPath = workingPath.substring(index + 1);
        }
        if (!workingPath.isEmpty()) {
            elements.add(workingPath);
        }
        this.elements = elements.toArray(new String[elements.size()]);
    }

    public FolderLike toFilderLike() {
        FolderLike result = Root.INSTANCE;
        for (String element : elements) {
            result = result.resolveFolder(element);
        }
        return result;
    }

    @Override
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean isAbsolute() {
        return path.length() > 0 && path.startsWith(separator());
    }

    @Override
    public BoxPath getRoot() {
        return isAbsolute() ? new BoxPath(fileSystem, path.substring(0, 1)) : null;
    }

    @Override
    public Path getFileName() {
        if (path.length() == 0 || path.equals(separator())) {
            return null;
        }
        if (!path.contains(separator())) {
            return this;
        }
        String filename = elements[elements.length - 1];
        return new BoxPath(fileSystem, filename);
    }

    private String separator() {
        return fileSystem.getSeparator();
    }

    @Override
    public BoxPath getParent() {
        if (getNameCount() <= (isAbsolute() ? 0 : 1)) {
            return null;
        }
        BoxPath relativeParent = subpath(0, getNameCount() - 2);
        return isAbsolute() ? getRoot().resolve(relativeParent) : relativeParent;
    }

    @Override
    public int getNameCount() {
        return elements.length;
    }

    @Override
    public BoxPath getName(int index) {
        return new BoxPath(fileSystem, elements[index]);
    }

    @Override
    public BoxPath subpath(int beginIndex, int endIndex) {
        BoxPath result = new BoxPath(fileSystem, "");
        for (int i = beginIndex; i <= endIndex; i++) {
            result = result.resolve(elements[i]);
        }
        return result;
    }

    @Override
    public boolean startsWith(Path other) {
        if (isAbsolute() != other.isAbsolute()) {
            return false;
        }
        if (other.getNameCount() > getNameCount()) {
            return false;
        }
        for (int i = 0; i < other.getNameCount(); i++) {
            if (!getName(i).toString().equals(other.getName(i).toString())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean startsWith(String other) {
        return startsWith(new BoxPath(fileSystem, other));
    }

    @Override
    public boolean endsWith(Path other) {
        if (other.isAbsolute()) {
            return false;
        }
        int otherNameCount = other.getNameCount();
        if (otherNameCount > getNameCount()) {
            return false;
        }
        for (int i = 0; i < otherNameCount; i++) {
            String otherName = other.getName(otherNameCount - i - 1).toString();
            String ownName = getName(getNameCount() - i - 1).toString();
            if (!ownName.equals(otherName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean endsWith(String other) {
        return endsWith(new BoxPath(fileSystem, other));
    }

    @Override
    public BoxPath normalize() {
        throw new NotImplementedException();
    }

    @Override
    public BoxPath resolve(Path other) {
        String separator = separator();

        List<String> names = new LinkedList<>();
        if (!other.isAbsolute()) {
            for (int i = 0; i < getNameCount(); i++) {
                names.add(getName(i).toString());
            }
        }
        for (int i = 0; i < other.getNameCount(); i++) {
            names.add(other.getName(i).toString());
        }
        StringBuilder pathBuilder = new StringBuilder(isAbsolute() || other.isAbsolute() ? separator : "");

        boolean empty = true;
        for (String name : names) {
            if (name.isEmpty()) {
                continue;
            }
            if (empty) {
                empty = false;
            } else {
                pathBuilder.append(separator);
            }
            pathBuilder.append(name);
        }
        return new BoxPath(fileSystem, pathBuilder.toString());
    }

    @Override
    public BoxPath resolve(String other) {
        return resolve(new BoxPath(fileSystem, other));
    }

    @Override
    public BoxPath resolveSibling(Path other) {
        return null;
    }

    @Override
    public BoxPath resolveSibling(String other) {
        return null;
    }

    @Override
    public BoxPath relativize(Path other) {
        BoxPath result = new BoxPath(fileSystem, "");
        for (int i = 0; i < getNameCount(); i++) {
            if (!other.getName(i).toString().equals(getName(i).toString())) {
                result = result.resolve("..");
            }
        }
        for (int i = 0; i < getNameCount(); i++) {
            if (!other.getName(i).toString().equals(getName(i).toString())) {
                result = result.resolve(other.getName(i));
            }
        }
        for (int i = getNameCount(); i < other.getNameCount(); i++) {
            result = result.resolve(other.getName(i));
        }
        return result;
    }

    @Override
    public URI toUri() {
        return null;
    }

    @Override
    public BoxPath toAbsolutePath() {
        return isAbsolute() ? this : new BoxPath(fileSystem, fileSystem.getSeparator()).resolve(this);
    }

    @Override
    public BoxPath toRealPath(LinkOption... options) throws IOException {
        return null;
    }

    @Override
    public File toFile() {
        return null;
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) throws IOException {
        return null;
    }

    @Override
    public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) throws IOException {
        return null;
    }

    @Override
    public Iterator<Path> iterator() {
        return null;
    }

    @Override
    public int compareTo(Path other) {
        return toString().compareTo(other.toString());
    }

    @Override
    public String toString() {
        return path;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BoxPath && toString().equals(obj.toString());
    }
}
