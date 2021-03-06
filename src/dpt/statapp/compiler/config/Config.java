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
 * File:   Config.java
 * Created on January 21, 2016, 10:05 PM
 */
package dpt.statapp.compiler.config;

/**
 * The DPTechnics templating engine configuration
 * @author Daan Pape
 */
public class Config {
    public static String VERSION_STRING = "0.1 alpha";
    public static String SCRIPT_DIRECTORY = "dist/js";
    public static String STYLE_DIRECTORY = "dist/css";
    public static String IMAGE_DIRECTORY = "assets/images";
    public static String FONT_DIRECTORY = "assets/fonts";
    public static String OUTPUT_DIRECTORY = "app";
    public static String LOCALES_DIRECTORY = "assets/locales";
    public static String PAGES_DIRECTORY = "html";
    public static String PARTIAL_DIRECTORY = "partials";
    public static String TEMP_DIRECTORY = "tempOutputDirectory";
    public static String URI_FIRST_SLASH = "";
    
    /**
     * Handle locale files as Javascript files and include
     * them in the global script file. Used in the DPTechnics
     * products.
     */
    public static boolean LOCALES_AS_GLOBAL_JS = true;
    
}
