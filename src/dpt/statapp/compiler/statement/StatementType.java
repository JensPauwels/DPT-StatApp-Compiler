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
 * File:   StatementType.java
 * Created on January 22, 2016, 01:34 AM
 */
package dpt.statapp.compiler.statement;

/**
 * All the statements in the DPTStatApp language. All statements 
 * are in the form:
 * 
 *      statement('argument1, argument2, ...')
 * 
 * @author Daan Pape
 */
public enum StatementType {
    PARTIAL("partial"),     /* Includes the source code of a partial, expects 1 argument being the filename */
    SCRIPT("script"),       /* Includes a javascript file, expects 2 arguments being the filename and the sort order (0 if not given) */
    STYLE("style");         /* Includes a css file, expects 1 argument being the filename */
    
    /* The textual representation of a statement */
    private final String representation;
    
    /**
     * Construct a new StatementType with String representation. 
     * @param representation the textual representation of this statement.
     */
    private StatementType(final String representation) {
        this.representation = representation;
    }
    
    /**
     * Return the textual representation of the StatementType
     * @return the textual representation of the StatementType
     */
    @Override
    public String toString() {
        return this.representation;
    }
}
