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
 * File:   AppGenerator.java
 * Created on January 21, 2016, 10:16 PM
 */
package dpt.statapp.compiler.config;

import dpt.statapp.compiler.output.ErrorFormatter;
import dpt.statapp.compiler.output.ErrorType;
import dpt.statapp.compiler.output.OutFormatter;
import java.io.File;

/**
 * Generate a new app directory structure. 
 * @author Daan Pape
 */
public class AppGenerator {
    protected String filepath;
    
    /**
     * Construct the appGenerator object. 
     * @param filepath the filepath where to create the app. 
     */
    public AppGenerator(String filepath) {
        this.filepath = filepath;
    }
    
    /**
     * Create a directory for an DPT-StatApp project.
     * @param name the name of the document type. 
     * @param dirname the name of the directory.
     * @return true on success, false on error.
     */
    private boolean createDirectory(String name, String dirname) {
        OutFormatter.printf("Creating %s directory '%s%s'...", name, filepath, dirname);
        if(!new File(filepath + dirname).mkdir()) {
            OutFormatter.printLn("ERR");
            ErrorFormatter.writeFormattedStringErrorLn(ErrorType.FATAL, "Could not create the %s directory '%s%s'", name, filepath, dirname);
            return false;
        };
        OutFormatter.printLn("OK");
        return true;
    }
    
    /**
     * Generate the necessary folders for the application.  
     * @return true on success, false on failure.  
     */
    public boolean generateApp()
    {
        /* Create pages directory */
        if(!createDirectory("HTML pages", Config.PAGES_DIRECTORY)) {
            return false;
        }
        
        /* Create partials directory */
        if(!createDirectory("HTML partials", Config.PARTIAL_DIRECTORY)) {
            return false;
        }
        
        /* Create app directory */
        if(!createDirectory("app", Config.OUTPUT_DIRECTORY)) {
            return false;
        }
        
        /* Create styles directory */
        if(!createDirectory("styles", Config.STYLE_DIRECTORY)) {
            return false;
        }
        
        /* Create images directory */
        if(!createDirectory("images", Config.IMAGE_DIRECTORY)) {
            return false;
        }
        
        /* Create locales directory */
        if(!createDirectory("locales", Config.LOCALES_DIRECTORY)) {
            return false;
        }
        
        /* Create licences directory */
        if(!createDirectory("licences", Config.LICENCE_DIRECTORY)) {
            return false;
        }
        
        /* Create scripts directory */
        if(!createDirectory("scripts", Config.SCRIPT_DIRECTORY)) {
            return false;
        }
        
        OutFormatter.printfLn("DPT-StatApp created in '%s'", filepath);
        return true;
    }
}
