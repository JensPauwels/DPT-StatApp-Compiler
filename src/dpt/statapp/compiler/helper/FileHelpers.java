/* 
 * Copyright (c) 2016, Daan Pape
 * Company: DPTechnics
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *     1. Redistributions of source code must retain the above copyright 
 *        notice, this list of conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright 
 *        notice, this list of conditions and the following disclaimer in the 
 *        documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * File:   FileHelpers.java
 * Created on January 22, 2016, 00:10 AM
 */
package dpt.statapp.compiler.helper;

import dpt.statapp.compiler.config.Config;
import dpt.statapp.compiler.output.ErrorFormatter;
import dpt.statapp.compiler.output.ErrorType;
import dpt.statapp.compiler.output.OutFormatter;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Static helper functions concerning files and directories.
 * @author Daan Pape
 */
public class FileHelpers {
    
    /**
     * Open a directory and check if it exists, print an error 
     * if it doesn't and return null.
     * @param filepath the file path to open. 
     * @return the directory if it exists. 
     */
    public static Path openDirectory(String filepath) {
        Path dir = Paths.get(filepath);
        
        if(!Files.isDirectory(dir)) {
            ErrorFormatter.writeFormattedStringErrorLn(ErrorType.WARNING, "Directory '%s' expected but not found.", filepath);
            return null;
        }
        
        return dir;
    }
    
    /**
     * Create a directory if it does not yet exist.
     * @param filepath the file path to the directory. 
     * @return the new directory path on success or null on error.
     */
    public static Path createDirectoryIfNotExists(String filepath)
    {
        Path newdir = FileHelpers.openDirectory(filepath);
        if(newdir == null) {
            if(!new File(filepath).mkdir()) {
                ErrorFormatter.writeFormattedStringErrorLn(ErrorType.FATAL, "Could not create directory: %s", filepath);
                return null;
            }
            newdir = FileHelpers.openDirectory(filepath);
        }
        return newdir;
    }
    
    /**
     * Delete an entire directory and it's contents. 
     * @param path the path to the directory you want to delete. 
     * @return true on success, false on error. 
     */
    public static boolean deleteDirectoryAndContents(String path) {
        Path dir = Paths.get(path);
        
        if(!Files.exists(dir)) {
            ErrorFormatter.writeStringError(ErrorType.WARNING, "Could not delete directory '" + path + "' because the path does not exist.");
            return false;
        }
        
        if(!Files.isDirectory(dir)) {
            ErrorFormatter.writeStringError(ErrorType.WARNING, "Could not delete directory '" + path + "' because the path is not a directory.");
            return false;
        }
        
        try(DirectoryStream<Path> files = Files.newDirectoryStream(dir)) {      
            /* Resolve all script information - stage 2b */
            for(Path file :  files) {
                Files.delete(file);
            }
            
            Files.delete(dir);
        } catch (Exception ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not delete directory "  + path);
            ex.printStackTrace(System.err);
            return false;
        }
        
        return true;
    }
    
    /**
     * Read the contents of a file into a string object. 
     * @param input the inputFile to read. 
     * @return the file's contents in a string on success or null on error.
     */
    public static String fileToString(Path input) {
        try { 
            byte[] file = Files.readAllBytes(input);
            return new String(file);
        } catch (IOException ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not read file contents: ");
            ex.printStackTrace(System.err);
            return null;
        }
    }
    
    /**
     * Find a file by it's filename. 
     * @param filename the filename.extension to search.
     * @param files the list of files to search in.
     * @return the Path to the file on success, null else.
     */
    public static Path getPathByFilename(String filename, List<Path> files) {
        for(Path file : files) {
            Path fname = file.getFileName();
            if(fname != null && fname.toString().equals(filename)) {
                return file;
            }
        }
        return null;
    }
}
