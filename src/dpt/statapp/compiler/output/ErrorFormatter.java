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
 * File:   ErrorFormatter.java
 * Created on January 21, 2016, 10:16 PM
 */
package dpt.statapp.compiler.output;

/**
 * Write output to stderr on a formatted way. 
 * @author Daan Pape
 */
public class ErrorFormatter {
    
    /**
     * Print a single error string. 
     * @param type the type of error.
     * @param message the message to print.
     */
    public static void writeStringError(ErrorType type, String message)
    {
        StringBuilder builder = new StringBuilder("[");
        builder.append(type.toString());
        builder.append("] ");
        builder.append(message);
        System.err.println(builder.toString());
    }
    
    /**
     * Write a formatted error and append a newline. 
     * @param type the type of error.
     * @param format the format of  the string.
     * @param args the arguments to fill in.
     */
    public static void writeFormattedStringErrorLn(ErrorType type, String format, Object... args)
    {
        StringBuilder builder = new StringBuilder("[");
        builder.append(type.toString());
        builder.append("] ");
        System.err.print(builder.toString());
        
        System.err.printf(format, args);
        System.err.println();
    }
}
