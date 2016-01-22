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
 * File:   StatementParser.java
 * Created on January 22, 2016, 01:34 AM
 */
package dpt.statapp.compiler.statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing static helper methods to parse Strings
 * into Statements.
 * @author Daan Pape
 */
public class StatementParser {
    
    /**
     * Parse the arguments of a statement. 
     * @param statementString the statementString to parse. 
     * @param openPar the index of the opening parentheses. 
     * @param closePar the index of the closing parentheses.
     * @return an array of arguments.
     */
    private static String[] parseStatementArgs(String statementString, int openPar, int closePar)
    {
        String[] args = statementString.substring(openPar, closePar).split(",");
        List<String> cleanArgs = new ArrayList<>();
        
        /* Trim whitespace */
        for(String arg : args) {
            cleanArgs.add(arg.trim());
        }
        
        return cleanArgs.toArray(new String[0]);
    }

    /**
     * Try to parse a string to a statement.
     * @param statementString the String to parse. 
     * @return the parsed statement if successful. 
     * @throws StatementParseException when the statement could not be parsed.
     */
    public static Statement parseStatement(String statementString) throws StatementParseException
    {
        Statement stmt = null;
        
        /* Find opening parentheses */
        int openParentheses = statementString.indexOf('(');
        if(openParentheses == -1) {
            throw new StatementParseException("[SYNTAX] Could not find opening parentheses", statementString);
        }
        
        /* Find closing parentheses */
        int closingParentheses = statementString.indexOf(')', openParentheses);
        if(closingParentheses == -1) {
            throw new StatementParseException("[SYNTAX] Could not find closing parentheses", statementString);
        }
        
        /* Filter action based on statement string */
        String stString = statementString.substring(0, openParentheses);
        String[] args = parseStatementArgs(statementString, openParentheses + 1, closingParentheses);
        switch(stString) {
            case "partial" :
                if(args.length != 1) {
                    throw new StatementParseException("Argument count mismatch for partial statement expected 1 got " + args.length, statementString);
                }
                stmt = new Statement(StatementType.PARTIAL, args);
                break;
            case "script" :
                if(args.length != 2) {
                    throw new StatementParseException("Argument count mismatch for partial statement expected 2 got " + args.length, statementString);
                }
                stmt = new Statement(StatementType.SCRIPT, args);
                break;
            case "style" :
                if(args.length != 1) {
                    throw new StatementParseException("Argument count mismatch for style statement expected 1 got " + args.length, statementString);
                }
                stmt = new Statement(StatementType.STYLE, args);
                break;
            default:
                throw new StatementParseException("[SYNTAX] Unknown statement '" + statementString + "'", statementString);
        }
        
        return stmt;
    }
}
