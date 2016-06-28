package com.oneeyedmen.okeydoke.sources;

import com.oneeyedmen.okeydoke.*;
import com.oneeyedmen.okeydoke.internal.IO;

import java.io.*;

public class FileSystemSourceOfApproval implements SourceOfApproval {

    private final Reporter<File> reporter;
    private final File approvedDir;
    private final File actualDir;
    private final String typeExtension;

    public FileSystemSourceOfApproval(File approvedDir, File actualDir, String typeExtension, Reporter<File> reporter) {
        this.approvedDir = approvedDir;
        this.actualDir = actualDir;
        this.typeExtension = typeExtension;
        this.reporter = reporter;
    }

    public FileSystemSourceOfApproval(File approvedDir, File actualDir, Reporter<File> reporter) {
        this(approvedDir, actualDir, "", reporter);
    }

    public FileSystemSourceOfApproval(File directory, Reporter<File> reporter) {
        this(directory, directory, reporter);
    }

    public FileSystemSourceOfApproval withActualDirectory(File actualDirectory) {
        return new FileSystemSourceOfApproval(approvedDir, actualDirectory, typeExtension, reporter);
    }

    public FileSystemSourceOfApproval withTypeExtension(String typeExtension) {
        return new FileSystemSourceOfApproval(approvedDir, actualDir, typeExtension, reporter);
    }

    @Override
    public Resource actualResourceFor(String testName) throws IOException {
        return new FileResource(actualFor(testName));
    }

    @Override
    public Resource approvedResourceFor(String testName) throws IOException {
        return new FileResource(approvedFor(testName));
    }

    protected InputStream inputOrNullForApproved(String testName) throws FileNotFoundException {
        return inputStreamOrNullFor(approvedFor(testName));
    }

    @Override
    public void reportFailure(String testName, AssertionError e) {
        reporter.reportFailure(actualFor(testName), approvedFor(testName), e);
    }

    @Override
    public <T> T actualContentOrNull(String testName, Serializer<T> serializer) throws IOException {
        File file = actualFor(testName);
        return file.isFile() ? read(file, serializer) : null;
    }

    @Override
    public <T> void checkActualAgainstApproved(OutputStream outputStream, String testName, Serializer<T> serializer, Checker<T> checker) throws IOException {
        checker.assertEquals(approvedContentOrNull(testName, serializer), actualContentOrNull(testName, serializer));
    }

    public File approvedFor(String testName) {
        return fileFor(approvedDir, testName, approvedExtension());
    }

    public File actualFor(String testName) {
        return fileFor(actualDir, testName, actualExtension());
    }

    protected String approvedExtension() {
        return ".approved" + typeExtension();
    }

    protected String actualExtension() {
        return ".actual" + typeExtension();
    }

    protected String typeExtension() {
        return typeExtension;
    }

    private File fileFor(File dir, String testName, String suffix) {
        return new File(dir, testName + suffix);
    }

    private InputStream inputStreamOrNullFor(File file) throws FileNotFoundException {
        return !(file.exists() && file.isFile()) ? null : inputStreamFor(file);
    }

    private BufferedInputStream inputStreamFor(File file) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    private <T> T approvedContentOrNull(String testName, Serializer<T> serializer) throws IOException {
        InputStream existing = inputOrNullForApproved(testName);
        return (existing != null) ? readAndClose(existing, serializer) : null;
    }

    private <T> T read(File file, Serializer<T> serializer) throws IOException {
        return readAndClose(inputStreamFor(file), serializer);
    }

    private <T> T readAndClose(InputStream input, Serializer<T> serializer) throws IOException {
        try {
            return serializer.readFrom(input);
        } finally {
            IO.closeQuietly(input);
        }
    }


}
