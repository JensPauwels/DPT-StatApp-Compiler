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
 * File:   ScriptCompiler.java
 * Created on January 22, 2016, 05:41 AM
 */
package dpt.statapp.compiler.compilers;

import dpt.statapp.compiler.config.Config;
import dpt.statapp.compiler.helper.FileHelpers;
import dpt.statapp.compiler.output.ErrorFormatter;
import dpt.statapp.compiler.output.ErrorType;
import dpt.statapp.compiler.statement.Statement;
import dpt.statapp.compiler.statement.StatementParseException;
import dpt.statapp.compiler.statement.StatementParser;
import dpt.statapp.compiler.statement.StatementType;
import dpt.statapp.compiler.iface.Compiler;
import dpt.statapp.compiler.output.OutFormatter;
import dpt.statapp.compressor.Compressor;
import dpt.statapp.compressor.HtmlCompressor;
import dpt.statapp.compressor.JavascriptCompressor;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class parses all the script elements and 
 * combines them where possible. This is
 * stage three of the compilation process.  
 * @author Daan Pape
 */
public class ScriptCompiler implements Compiler{
    /* The file path of the app */ 
    String filePath;
    
    /**
     * Construct a new ScriptCompiler. 
     * @param filePath the file path of the application
     */
    public ScriptCompiler(String filePath) {
        this.filePath = filePath;
    }
    
    /* A list containing all the sets of scripts used in the HTML documents */
    List<Set<String>> allScriptSets = new ArrayList<>();
    
    /* A set containing all the script files used in the complete app */
    Set<String> allScripts = new HashSet<>();
    
    /* The resulting list containing all scripts which are used in every single document */
    List<String> globalScripts = new ArrayList<>();
    
    /* Maps the filename of a script to the filepath and required order, the lowest number is first in the list */
    Map<String, Path> scriptPathMap = new HashMap<>();
    Map<String, Integer> scriptOrderMap = new HashMap<>();
    
    /**
     * Execute the first round of script parsing. Check if all scripts can be found 
     * and remember which scripts are common to all files of the application. 
     * @param source the source HTML page file.
     * @param scripts the list of available script files. 
     * @return true on successful availability and globality checks.
     */
    private boolean handleHtmlFileFirstRound(Path source, List<Path> scripts)
    {  
        String sourceContent = FileHelpers.fileToString(source);
        if(sourceContent == null) {
            return false;
        }
        
        /* Set of all the scripts in this HTML file */
        Set<String> fileScripts = new HashSet<>();
        
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
                
                /* Handle the script statements */
                if(stmt.getType().equals(StatementType.SCRIPT)) {
                    /* Try to find the script file in the list */
                    Path script = FileHelpers.getPathByFilename(stmt.getArgs()[0], scripts);
                    if(script == null) {
                        ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not find script '" + stmt.getArgs()[0] + "'");
                        return false;
                    }
                    
                    /* The file is found, add it to this files unique set */
                    fileScripts.add(stmt.getArgs()[0]); 
                    allScripts.add(stmt.getArgs()[0]);
                    
                    /* Keep the lowest order */
                    int order = Integer.parseInt(stmt.getArgs()[1]);
                    if(scriptOrderMap.containsKey(stmt.getArgs()[0])) {
                        if(scriptOrderMap.get(stmt.getArgs()[0]) > order) {
                            scriptOrderMap.put(stmt.getArgs()[0], order);
                        }
                    } else {
                        scriptOrderMap.put(stmt.getArgs()[0], order);
                    }
                    
                }
            } catch(StatementParseException ex) {
                ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not parse HTML file: " + ex.getMessage());
                return false;
            }
            
            /* Search the next tag */
            matchIndex = sourceContent.indexOf("<-", closing_tag);
        }
        
        /* Now add this file's set to the global list of sets */
        allScriptSets.add(fileScripts);
        
        return true;
    }
    
    /**
     * Handle the HTML files for a second time, now replacing the statements
     * with the correct script imports. 
     * @param source the source file to handle. 
     * @param outdir the output directory. 
     * @return true on success, false on error.
     */
    private boolean handleHtmlFileSecondRound(Path source, Path outdir)
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
        boolean globalScriptImported = false;
        
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
                
                /* Handle the script statements */
                if(stmt.getType().equals(StatementType.SCRIPT)) {
                    if(globalScripts.contains(stmt.getArgs()[0])) {
                        /* This is a global script */
                        if(globalScriptImported) {
                            /* Global script allready imported, remove statement */
                            result.append(sourceContent.substring(cursor, matchIndex - 1));
                            cursor = closing_tag + 2;
                        } else {
                            /* Import global script */
                            String scriptContent = "<script src=\"" + Config.URI_FIRST_SLASH + Config.SCRIPT_DIRECTORY + "/globalscript.js\"></script>";
                            result.append(sourceContent.substring(cursor, matchIndex - 1));
                            result.append(scriptContent);
                            cursor = closing_tag + 2;
                            globalScriptImported = true;
                        }
                    } else {
                        /* This is a page specific script, import directly */
                        String scriptContent = "<script src=\"" + Config.URI_FIRST_SLASH + Config.SCRIPT_DIRECTORY + "/" + stmt.getArgs()[0] + "\"></script>";
                        result.append(sourceContent.substring(cursor, matchIndex - 1));
                        result.append(scriptContent);
                        cursor = closing_tag + 2;
                    }
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
            /* Compress HTML */
            HtmlCompressor comp = new HtmlCompressor();
            OutFormatter.printLn("Compressing HTML " + source.getFileName().toString());
            String compressed = comp.compress(result.toString());
            
            Files.write(new File(outdir.toFile(), source.getFileName().toString()).toPath(), compressed.getBytes());
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
        Path htmldir = FileHelpers.openDirectory(filePath + Config.OUTPUT_DIRECTORY + "/" + Config.TEMP_DIRECTORY);
        Path scriptdir = FileHelpers.openDirectory(filePath + Config.SCRIPT_DIRECTORY);
        Path outputdir = FileHelpers.openDirectory(filePath + Config.OUTPUT_DIRECTORY);
        if(htmldir == null || scriptdir == null || outputdir == null) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "HTML compilation could not complete because not all directories are present.");
            return false;
        }

        /* Get a list of all script files */
        OutFormatter.printLn("Generating list of scripts...");
        List<Path> scripts = new ArrayList<>();
        
        try(DirectoryStream<Path> scriptStream = Files.newDirectoryStream(scriptdir)) {
            for(Path script : scriptStream) {
                scripts.add(script);
                scriptPathMap.put(script.getFileName().toString(), script);
                OutFormatter.printLn("Found script: " + script.getFileName().toString());
            }
        } catch (IOException ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not list all script files: ");
            ex.printStackTrace(System.err);
            return false;
        }
        OutFormatter.printLn("All scripts listed");
        
        /* Process all HTML page files */
        try(DirectoryStream<Path> htmlStream = Files.newDirectoryStream(htmldir)) {      
            /* Gather all script information - stage 2a */
            for(Path html :  htmlStream) {
                OutFormatter.printfLn("Parsing scripts (stage 3a - script lookup): %s", html.getFileName().toString());
                if(!handleHtmlFileFirstRound(html, scripts)) {
                    throw new Exception("Error while parsing HTML file");
                }
            }
        } catch (Exception ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not process all HTML page files: ");
            ex.printStackTrace(System.err);
            return false;
        }
        
        /* Determine global script files */
        OutFormatter.printLn("Determining global scripts...");
        for(String script : allScripts) {
            boolean glbl = true;

            /* If the script is present in all sets it's global */
            for(Set<String> set : allScriptSets) {
                if(!set.contains(script)) {
                    glbl = false;
                    break;
                }
            }

            if(glbl) {
                globalScripts.add(script);
                OutFormatter.printLn("Found global script: " + script);
            }
        }
        OutFormatter.printLn("All global scripts listed");

        /* Generate script documents */
        OutFormatter.printLn("Generating script documents");
        Path outdir = FileHelpers.createDirectoryIfNotExists(filePath + Config.OUTPUT_DIRECTORY + "/" + Config.SCRIPT_DIRECTORY);

        /* Buffer for global script document */
        StringBuilder globalScriptDocument = new StringBuilder();

        try {
            List<String> sortedScripts = new ArrayList<>();
            
            /* Sort the global javascripts */
            while(!scriptOrderMap.isEmpty()) {
                int smallestkey = Integer.MAX_VALUE;
                String smallestscript = null;
                
                /* Search for the smallest script */
                for(String script : scriptOrderMap.keySet()) {
                    int order = scriptOrderMap.get(script);
                    if(smallestkey >=  order) {
                        smallestkey = order;
                        smallestscript = script;
                    }
                }
                
                /* Add the smallest global script to the sortedScripts list and remove it from the map */
                scriptOrderMap.remove(smallestscript);
                if(globalScripts.contains(smallestscript)) {
                    sortedScripts.add(smallestscript);
                    allScripts.remove(smallestscript);
                    OutFormatter.printfLn("Added script '%s' with order '%d' to the global script file", smallestscript, smallestkey);
                }
            }
            
            // Build the global script document 
            for(String script : sortedScripts) {
                /* Read the script file */
                String contents = FileHelpers.fileToString(scriptPathMap.get(script));
                globalScriptDocument.append(contents);
            }
            
            Compressor comp = new JavascriptCompressor();
            
            for(String script : allScripts) {
                /* The contents of this script should go to a separate document */
                String contents = FileHelpers.fileToString(scriptPathMap.get(script));
                
                /* Compress Javascript */
                OutFormatter.printLn("Compressing Javascript " + script);
                String compressed = comp.compress(contents);
                
                Files.write(new File(outdir.toFile(), script).toPath(), compressed.getBytes());
            }

            /* Write global script file */
            OutFormatter.printLn("Compressing Javascript globalscript.js");
            String compressed = comp.compress(globalScriptDocument.toString());
            Files.write(new File(outdir.toFile(), "globalscript.js").toPath(), compressed.getBytes());
        } catch (IOException ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not save script to output folder:");
            ex.printStackTrace(System.err);
            return false;
        }
        OutFormatter.printLn("All script documents are generated");

        /* Replace script statements */
        try(DirectoryStream<Path> htmlStream = Files.newDirectoryStream(htmldir)) {      
            /* Resolve all script information - stage 2b */
            for(Path html :  htmlStream) {
                OutFormatter.printfLn("Resolving scripts (stage 3b - script resolving): %s", html.getFileName().toString());
                if(!handleHtmlFileSecondRound(html, outputdir)) {
                    throw new Exception("Error while resolving script file");
                }
            }
        } catch (Exception ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not process all HTML page files: ");
            ex.printStackTrace(System.err);
            return false;
        }
        
        OutFormatter.printLn("All scripts are compiled");     
        return true;
    }    
}
