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
 * File:   StyleCompiler.java
 * Created on January 22, 2016, 04:01 AM
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
import dpt.statapp.compressor.CssCompressor;
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
 * This class parses the style statements and merges the
 * styles which are common to all files in the application. This is
 * stage two of the compilation process.  
 * @author Daan Pape
 */
public class StyleCompiler implements Compiler{
    /* The file path of the app */ 
    String filePath;
    
    /**
     * Construct a new StyleCompiler. 
     * @param filePath the file path of the application
     */
    public StyleCompiler(String filePath) {
        this.filePath = filePath;
    }
    
    /* A list containing all the sets of styles used in the HTML documents */
    List<Set<String>> allStyleSets = new ArrayList<>();
    
    /* A set containing all the style files used in the complete app */
    Set<String> allStyles = new HashSet<>();
    
    /* The resulting list containing all styles which are used in every single document */
    List<String> globalStyles = new ArrayList<>();
    
    /* Maps the filename of a style to the filepath */
    Map<String, Path> stylePathMap = new HashMap<>();
    
    /**
     * Execute the first round of style parsing. Check if all styles can be found 
     * and remember which styles are common to all files of the application. 
     * @param source the source HTML page file.
     * @param styles the list of available style files. 
     * @return true on successful availability and globality checks.
     */
    private boolean handleHtmlFileFirstRound(Path source, List<Path> styles)
    {  
        String sourceContent = FileHelpers.fileToString(source);
        if(sourceContent == null) {
            return false;
        }
        
        /* Set of all the styles in this HTML file */
        Set<String> fileStyles = new HashSet<>();
        
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
                
                /* Handle the style statements */
                if(stmt.getType().equals(StatementType.STYLE)) {
                    /* Try to find the style file in the list */
                    Path style = FileHelpers.getPathByFilename(stmt.getArgs()[0], styles);
                    if(style == null) {
                        ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not find style '" + stmt.getArgs()[0] + "'");
                        return false;
                    }
                    
                    /* The file is found, add it to this files unique set */
                    fileStyles.add(stmt.getArgs()[0]); 
                    allStyles.add(stmt.getArgs()[0]);
                }
            } catch(StatementParseException ex) {
                ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not parse HTML file: " + ex.getMessage());
                return false;
            }
            
            /* Search the next tag */
            matchIndex = sourceContent.indexOf("<-", closing_tag);
        }
        
        /* Now add this file's set to the global list of sets */
        allStyleSets.add(fileStyles);
        
        return true;
    }
    
    /**
     * Parse a HTML page and replace all the partial statements with the partial's 
     * source code. 
     * @param source the source HTML page file.
     * @param outdir the output directory. 
     * @param partials the list of available partial files. 
     * @return true on successful replacement of the partial includes.
     */
    
    /**
     * Parse a HTML page and replace all style statements with the correct
     * style imports. 
     * @param source the source HTML file to parse. 
     * @param outdir the output directory to place the file in. 
     * @return true on success, false else.
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
        boolean globalStyleImported = false;
        
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
                
                /* Handle the style statements */
                if(stmt.getType().equals(StatementType.STYLE)) {
                    if(globalStyles.contains(stmt.getArgs()[0])) {
                        /* This is a global style */
                        if(globalStyleImported) {
                            /* Global style allready imported, remove statement */
                            result.append(sourceContent.substring(cursor, matchIndex - 1));
                            cursor = closing_tag + 2;
                        } else {
                            /* Import global style */
                            String styleContent = "<link rel=\"stylesheet\" href=\"/" + Config.STYLE_DIRECTORY + "/globalstyle.css\">";
                            result.append(sourceContent.substring(cursor, matchIndex - 1));
                            result.append(styleContent);
                            cursor = closing_tag + 2;
                            globalStyleImported = true;
                        }
                    } else {
                        /* This is a page specific style, import directly */
                        String styleContent = "<link rel=\"stylesheet\" href=\"/" + Config.STYLE_DIRECTORY + "/" + stmt.getArgs()[0] + "\">";
                        result.append(sourceContent.substring(cursor, matchIndex - 1));
                        result.append(styleContent);
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
        Path htmldir = FileHelpers.openDirectory(filePath + Config.OUTPUT_DIRECTORY + "/" + Config.TEMP_DIRECTORY);
        Path styledir = FileHelpers.openDirectory(filePath + Config.STYLE_DIRECTORY);
        if(htmldir == null || styledir == null) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "HTML compilation could not complete because not all directories are present.");
            return false;
        }

        /* Get a list of all style files */
        OutFormatter.printLn("Generating list of styles...");
        List<Path> styles = new ArrayList<>();
        
        try(DirectoryStream<Path> styleStream = Files.newDirectoryStream(styledir)) {
            for(Path style : styleStream) {
                styles.add(style);
                stylePathMap.put(style.getFileName().toString(), style);
                OutFormatter.printLn("Found style: " + style.getFileName().toString());
            }
        } catch (IOException ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not list all style files: ");
            ex.printStackTrace(System.err);
            return false;
        }
        OutFormatter.printLn("All styles listed");
        
        /* Process all HTML page files */
        try(DirectoryStream<Path> htmlStream = Files.newDirectoryStream(htmldir)) {      
            /* Gather all style information - stage 2a */
            for(Path html :  htmlStream) {
                OutFormatter.printfLn("Parsing styles (stage 2b - style lookup): %s", html.getFileName().toString());
                if(!handleHtmlFileFirstRound(html, styles)) {
                    throw new Exception("Error while parsing HTML file");
                }
            }
        } catch (Exception ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not process all HTML page files: ");
            ex.printStackTrace(System.err);
            return false;
        }
        
        /* Determine global style files */
        OutFormatter.printLn("Determining global styles...");
        for(String style : allStyles) {
            boolean glbl = true;

            /* If the style is present in all sets it's global */
            for(Set<String> set : allStyleSets) {
                if(!set.contains(style)) {
                    glbl = false;
                    break;
                }
            }

            if(glbl) {
                globalStyles.add(style);
                OutFormatter.printLn("Found global style: " + style);
            }
        }
        OutFormatter.printLn("All global styles listed");

        /* Generate style documents */
        OutFormatter.printLn("Generating style documents");
        Path outdir = FileHelpers.createDirectoryIfNotExists(filePath + Config.OUTPUT_DIRECTORY + "/" + Config.STYLE_DIRECTORY);

        /* Buffer for global style document */
        StringBuilder globalStyleDoc = new StringBuilder();

        try {
            Compressor compressor = new CssCompressor();
            
            for(String style : allStyles) {
                /* Read the style file */
                String contents = FileHelpers.fileToString(stylePathMap.get(style));

                if(globalStyles.contains(style)) {
                    /* The contents of this style should go to the combined document */
                    globalStyleDoc.append(contents);
                } else {
                    /* The contents of this style should go to a separate document */
                    String compressed = compressor.compress(contents);
                    Files.write(new File(outdir.toFile(), style).toPath(), compressed.getBytes());
                }
            }

            /* Write global style file */
            String compressed = compressor.compress(globalStyleDoc.toString());
            Files.write(new File(outdir.toFile(), "globalstyle.css").toPath(), compressed.getBytes());
        } catch (IOException ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not save style to output folder:");
            ex.printStackTrace(System.err);
            return false;
        }
        OutFormatter.printLn("All style documents are generated");

        /* Replace style statements */
        try(DirectoryStream<Path> htmlStream = Files.newDirectoryStream(htmldir)) {      
            /* Resolve all style information - stage 2b */
            for(Path html :  htmlStream) {
                OutFormatter.printfLn("Resolving styles (stage 2b - style resolving): %s", html.getFileName().toString());
                if(!handleHtmlFileSecondRound(html, htmldir)) {
                    throw new Exception("Error while resolving style file");
                }
            }
        } catch (Exception ex) {
            ErrorFormatter.writeStringError(ErrorType.FATAL, "Could not process all HTML page files: ");
            ex.printStackTrace(System.err);
            return false;
        }
        
        OutFormatter.printLn("All styles are compiled");     
        return true;
    }    
}
