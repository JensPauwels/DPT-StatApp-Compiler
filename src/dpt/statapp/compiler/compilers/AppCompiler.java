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
 * File:   AppCompiler.java
 * Created on January 22, 2016, 04:04 AM
 */
package dpt.statapp.compiler.compilers;

import dpt.statapp.compiler.config.Config;
import dpt.statapp.compiler.helper.DirCopyVisitor;
import dpt.statapp.compiler.helper.FileHelpers;
import dpt.statapp.compiler.iface.Compiler;
import dpt.statapp.compiler.output.ErrorFormatter;
import dpt.statapp.compiler.output.ErrorType;
import dpt.statapp.compiler.output.OutFormatter;
import dpt.statapp.compressor.Compressor;
import dpt.statapp.compressor.JavascriptCompressor;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class compiles the complete application to static
 * HTML. 
 * @author Daan Pape
 */
public class AppCompiler {
    /* The working directory of the DPTStatApp */
    protected String filepath;
    
    /**
     * Construct a new AppCompiler instance. 
     * @param filepath the filepath to work in. 
     */
    public AppCompiler(String filepath) {
        this.filepath = filepath;
    }
    
    /**
     * Compile the static HTML application. 
     * @return true on compilation success.
     */
    public boolean compileApp() {
        
        /* Execute stage 1 of the compiler and parse partials */
        Compiler htmlComp = new HtmlCompiler(filepath); 
        if(!htmlComp.run()) {
            return false;
        }
        
        /* Execute stage 2 of the compiler and parse styles */
        Compiler styleComp = new StyleCompiler(filepath);
        if(!styleComp.run()) {
            return false;
        }
        
        /* Execute stage 3 of the compiler and parse scripts */
        Compiler scriptComp = new ScriptCompiler(filepath);
        if(!scriptComp.run()) {
            return false;
        }
        
        /* Delete the temporary directory */
        FileHelpers.deleteDirectoryAndContents(filepath + Config.OUTPUT_DIRECTORY + "/" + Config.TEMP_DIRECTORY);
        
        /* Copy image and licence folders */
        try {
            Path dir = FileHelpers.createDirectoryIfNotExists(filepath + Config.OUTPUT_DIRECTORY + "/" + Config.IMAGE_DIRECTORY);
            Files.walkFileTree(Paths.get(filepath + Config.IMAGE_DIRECTORY), new DirCopyVisitor(dir));
            
            dir = FileHelpers.createDirectoryIfNotExists(filepath + Config.OUTPUT_DIRECTORY + "/" + Config.LICENCE_DIRECTORY);
            Files.walkFileTree(Paths.get(filepath + Config.LICENCE_DIRECTORY), new DirCopyVisitor(dir));
            
            dir = FileHelpers.createDirectoryIfNotExists(filepath + Config.OUTPUT_DIRECTORY + "/" + Config.FONT_DIRECTORY);
            Files.walkFileTree(Paths.get(filepath + Config.FONT_DIRECTORY), new DirCopyVisitor(dir));
            
            /* Copy and compress locales */
            dir = FileHelpers.createDirectoryIfNotExists(filepath + Config.OUTPUT_DIRECTORY + "/" + Config.LOCALES_DIRECTORY);
            
            Compressor compressor = new JavascriptCompressor();
            
            try(DirectoryStream<Path> localeStream = Files.newDirectoryStream(Paths.get(filepath + Config.LOCALES_DIRECTORY))) {      
                /* Copy and compress all javascript locales */
                for(Path locale :  localeStream) {
                    OutFormatter.printfLn("Compressing locale: %s", locale.getFileName().toString());
                    String localeContents = FileHelpers.fileToString(locale);
                    localeContents = compressor.compress(localeContents);
                    Files.write(new File(dir.toFile(), locale.getFileName().toString()).toPath(), localeContents.getBytes());
                }
            } catch (Exception ex) {
                ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not copy all locale files.");
                ex.printStackTrace(System.err);
                return false;
            }
            
            Files.walkFileTree(Paths.get(filepath + Config.LOCALES_DIRECTORY), new DirCopyVisitor(Paths.get(filepath + Config.OUTPUT_DIRECTORY + "/" + Config.LOCALES_DIRECTORY)));
        } catch (IOException ex) {
            ErrorFormatter.writeStringError(ErrorType.WARNING, "Could not copy static content directories to app:");
            ex.printStackTrace(System.err);
        }
         
        return true;
    }
}
