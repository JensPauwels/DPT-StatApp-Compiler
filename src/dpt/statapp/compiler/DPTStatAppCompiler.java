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
 * File:   DPTStatAppCompiler.java
 * Created on January 21, 2016, 09:58 PM
 */
package dpt.statapp.compiler;

import dpt.statapp.compiler.compilers.AppCompiler;
import dpt.statapp.compiler.config.AppGenerator;
import dpt.statapp.compiler.config.Config;
import dpt.statapp.compiler.output.ErrorFormatter;
import dpt.statapp.compiler.output.ErrorType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main application entry point. 
 * @author Daan
 */
public class DPTStatAppCompiler {

    /**
     * Run the compiler. The compiler expects two arguments, namely
     * the command and secondly a file path. 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length != 2) {
            printUsage();
            System.exit(1);
        } else {
            String filepath = checkAndFormatFilePath(args[1]);
            if(filepath == null) {
                System.exit(1);
            }
            
            switch(args[0]) {
                case "generate" :
                    AppGenerator gen = new AppGenerator(filepath);
                    System.exit(gen.generateApp() ? 0 : 1);
                    break;
                case "compile" :
                    AppCompiler comp = new AppCompiler(filepath);
                    System.exit(comp.compileApp() ? 0 : 1);
                    break;
                case "clean" : 
                    System.out.println("Just delete the contents of the app folder for now.");
                    break;
                default:
                    printUsage();
                    System.exit(1);
            }
        }
        
        System.exit(0);
    }
    
    /**
     * Check and format a file path. This function checks if the file path
     * exists and ensures a slash is added to the end. null is returned when
     * this path is non existent or is not a directory. 
     * @param filePath the file path to check. 
     * @return the clean file path on success 
     */
    public static String checkAndFormatFilePath(String filePath)
    {
        /* Quick and dirty windows to java filepath conversion */
        filePath = filePath.replace("\\","/");
        
        if(filePath.charAt(filePath.length() - 1) != '/') {
            filePath += "/";
        }
        
        Path path = Paths.get(filePath);
        if(!Files.exists(path)) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "The file path '" + filePath + "' does not exist.");
            return null;
        }
        
        if(!Files.isDirectory(path)) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "The file path '" + filePath + "' is not a directory.");
            return null;
        }
        
        return filePath;
    }
    
    public static void printUsage() {
        System.out.println("DPTStatAppCompiler version " + Config.VERSION_STRING);
        System.out.println("");
        System.out.println("Usage: DPTStatAppCompiler <command> <path-to-directory>");
        System.out.println("Possible commands:");
        System.out.println("\t generate: generate the folder structure for a new project");
        System.out.println("");
        System.out.println("\t compile: compile all project files to the static HTML app");
        System.out.println("");
        System.out.println("\t clean: clean the complete app directory");
    }
}
