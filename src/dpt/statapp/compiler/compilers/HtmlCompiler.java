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
 * File:   HtmlCompiler.java
 * Created on January 21, 2016, 11:51 PM
 */
package dpt.statapp.compiler.compilers;

import dpt.statapp.compiler.config.Config;
import dpt.statapp.compiler.helper.FileHelpers;
import dpt.statapp.compiler.iface.Compiler;
import dpt.statapp.compiler.output.ErrorFormatter;
import dpt.statapp.compiler.output.ErrorType;
import dpt.statapp.compiler.output.OutFormatter;
import dpt.statapp.compiler.statement.Statement;
import dpt.statapp.compiler.statement.StatementParseException;
import dpt.statapp.compiler.statement.StatementParser;
import dpt.statapp.compiler.statement.StatementType;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses every file in the HTML pages folder and
 * replaces the partial imports with the file contents. This is
 * stage one of the compilation process. 
 * @author Daan Pape
 */
public class HtmlCompiler implements Compiler {
    /* The file path of the app */ 
    String filePath;
    
    /**
     * Construct a new HtmlCompiler
     * @param filePath the file path of the application
     */
    public HtmlCompiler(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Parse a HTML page and replace all the partial statements with the partial's 
     * source code. 
     * @param source the source HTML page file.
     * @param outdir the output directory. 
     * @param partials the list of available partial files. 
     * @return true on successful replacement of the partial includes.
     */
    private boolean handleHtmlFile(Path source, Path outdir, List<Path> partials)
    {
        /* Buffer to hold the new String in */
        StringBuilder result = new StringBuilder();
        
        String sourceContent = FileHelpers.fileToString(source);
        if(sourceContent == null) {
            return false;
        }
        
        int cursor = 0;
        int eof = sourceContent.length();
        int matchIndex = sourceContent.indexOf("<-");
        
        while(matchIndex != -1) {
            
            /* Find closing tag index */
            int closing_tag = sourceContent.indexOf("->", matchIndex);
            if(closing_tag == -1) {
                ErrorFormatter.writeFormattedStringErrorLn(ErrorType.FATAL, "[SYNTAX] Could not find matching closing tag at %s", sourceContent.substring(matchIndex));
                return false;
            }
            
            try {
                String stmtString = sourceContent.substring(matchIndex + 2, closing_tag - 1).trim();
                Statement stmt = StatementParser.parseStatement(stmtString);
                
                /* Handle the partial statements */
                if(stmt.getType().equals(StatementType.PARTIAL)) {
                    /* Try to find the partial file in the list */
                    Path partial = FileHelpers.getPathByFilename(stmt.getArgs()[0], partials);
                    if(partial == null) {
                        ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not find partial '" + stmt.getArgs()[0] + "'");
                        return false;
                    }
                    
                    /* Now replace the statement with the partial's content */
                    String partialContent = FileHelpers.fileToString(partial);
                    result.append(sourceContent.substring(cursor, matchIndex - 1));
                    result.append(partialContent);
                    cursor = closing_tag + 2;
                }
            } catch(StatementParseException ex) {
                ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not parse HTML file: " + ex.getMessage());
                return false;
            }
            
            /* Search the next tag */
            matchIndex = sourceContent.indexOf("<-", closing_tag);
            
            /* Write rest of file if no match was found */
            if(matchIndex == -1) {
                result.append(sourceContent.substring(cursor, eof));
            }
        }
        
        /* Now save the file in the output directory */
        try {
            Files.write(new File(outdir.toFile(), source.getFileName().toString()).toPath(), result.toString().getBytes());
        } catch (IOException ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not save result of HTML parse to output folder:");
            ex.printStackTrace(System.err);
            return false;
        }
        
        return true;
    }
    
    /**
     * Run the HTML compiler stage. 
     * @return true on success, false on error. 
     */
    @Override
    public boolean run() {
        /* Open all relevant directories */
        Path htmldir = FileHelpers.openDirectory(filePath + Config.PAGES_DIRECTORY);
        Path partialdir = FileHelpers.openDirectory(filePath + Config.PARTIAL_DIRECTORY);
        if(htmldir == null || partialdir == null) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "HTML compilation could not complete because not all directories are present.");
            return false;
        }
        
        /* Create a temporary directory to store the HTML files in */
        Path outputdir = FileHelpers.openDirectory(filePath + Config.OUTPUT_DIRECTORY + "/" + Config.TEMP_DIRECTORY);
        if(outputdir == null) {
            OutFormatter.printf("Creating temp directory ...");
            if(!new File(filePath + Config.OUTPUT_DIRECTORY + "/" + Config.TEMP_DIRECTORY).mkdir()) {
                OutFormatter.printLn("ERR");
                ErrorFormatter.writeFormattedStringErrorLn(ErrorType.FATAL, "Could not create the temporary working directory");
                return false;
            };
            OutFormatter.printLn("OK");
            
            outputdir = FileHelpers.openDirectory(filePath + Config.OUTPUT_DIRECTORY + "/" + Config.TEMP_DIRECTORY);
        }
        
        
        /* Get a list of all partial files */
        OutFormatter.printLn("Generating list of partials...");
        List<Path> partials = new ArrayList<>();
        
        try(DirectoryStream<Path> partialStream = Files.newDirectoryStream(partialdir)) {
            for(Path partial : partialStream) {
                partials.add(partial);
                OutFormatter.printLn("Found partial: " + partial.getFileName().toString());
            }
        } catch (IOException ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not list all partial files: ");
            ex.printStackTrace(System.err);
            return false;
        }
        OutFormatter.printLn("All partials listed");
        
        /* Process all HTML page files */
        try(DirectoryStream<Path> htmlStream = Files.newDirectoryStream(htmldir)) {
            for(Path html :  htmlStream) {
                OutFormatter.printfLn("Parsing HTML page file (stage 1 - partials): %s", html.getFileName().toString());
                if(!handleHtmlFile(html, outputdir, partials)) {
                    throw new Exception("Error while parsing HTML file");
                }
            }
        } catch (Exception ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not process all HTML page files: ");
            ex.printStackTrace(System.err);
            return false;
        }
        
        OutFormatter.printLn("All partials are compiled");     
        return true;
    }
}
