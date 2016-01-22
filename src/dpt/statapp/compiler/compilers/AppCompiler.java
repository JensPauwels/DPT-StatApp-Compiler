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
import dpt.statapp.compiler.helper.FileHelpers;
import dpt.statapp.compiler.iface.Compiler;

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
        
        return true;
    }
}
