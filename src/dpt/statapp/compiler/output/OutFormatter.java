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
 * File:   OutFormatter.java
 * Created on January 21, 2016, 10:56 PM
 */
package dpt.statapp.compiler.output;

/**
 * Wrapper class for output to stdout.
 * @author Daan Pape
 */
public class OutFormatter {
    
    /**
     * Write a string to standard output and append a newline. 
     * @param out the text to print to stderr. 
     */
    public static void printLn(String out) {
        System.out.println(out);
    }
    
    /**
     * Write a formatted string to standard output.
     * @param format the format of  the string. 
     * @param args the arguments to fill in. 
     */
    public static void printf(String format, Object... args)
    {
        System.out.printf(format, args);
    }
    
    /**
     * Write a formatted string to standard output and append a newline. 
     * @param format the format of  the string. 
     * @param args the arguments to fill in. 
     */
    public static void printfLn(String format, Object... args)
    {
        System.out.printf(format, args);
        System.out.println();
    }
}
