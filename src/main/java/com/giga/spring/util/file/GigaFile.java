package com.giga.spring.util.file;

import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

public class GigaFile {

    private Part part;
    private String fileName;
    private String extension;
    private byte[] content;

    // TODO: not making this constructor public
    public GigaFile(Part part) throws IOException {
        this.part = part;
        this.fileName = extractFileName(part);
        this.extension = extractExtension(getSubmittedFileName(part));
        try (InputStream in = part.getInputStream()) {
            this.content = in.readAllBytes();
        }
    }

    public GigaFile(String fileName, String extension, byte[] content) {
        this.fileName = fileName;
        this.extension = extension;
        this.content = content;
    }


    private static String extractFileName(Part part) {
        String name = getSubmittedFileName(part);

        if (name == null) {
            return null;
        }

        int index = name.lastIndexOf('.');
        if (index <= 0) {
            return name;
        } else {
            return name.substring(0, index);
        }
    }

    private static String extractExtension(String filename) {
        if (filename == null) {
            return null;
        }

        int index = filename.lastIndexOf('.');
        if (index == -1 || index == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(index + 1);
    }

    
    private static String getSubmittedFileName(Part part) {
        String name = null;
        try {
            String submitted = part.getSubmittedFileName();
            if (submitted != null && !submitted.isBlank()) {
                name = submitted;
            }
        } catch (NoSuchMethodError ignored) {
            // Older servlet versions may not have getSubmittedFileName
        }

        if (name == null) {
            String header = part.getHeader("content-disposition");
            if (header == null) {
                return null;
            }

            for (String cd : header.split(";")) {
                cd = cd.trim();
                if (cd.startsWith("filename=")) {
                    String candidate = cd.substring("filename=".length()).trim();
                    if (candidate.startsWith("\"") && candidate.endsWith("\"")) {
                        candidate = candidate.substring(1, candidate.length() - 1);
                    }
                    name = candidate;
                    break;
                }
            }
        }

        return name;
    }
    
    // NOT EXPOSING THE PART OBJECT

    public String getFileName() {
        return fileName;
    }

    public String getExtension() {
        return extension;
    }

    public byte[] getContent() {
        return content;
    }
}
