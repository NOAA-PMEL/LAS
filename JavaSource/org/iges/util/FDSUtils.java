package org.iges.util;

import java.io.*;
import java.util.*;
import java.net.*;
import java.security.*;
import javax.servlet.http.*;
import org.iges.anagram.*;
import org.iges.anagram.filter.AnalysisFilter;

/** A utility class used by FDS server for parsing input URL 
 * 
 * @author Yonghua Wei
 */

public class FDSUtils {

    /** Gets servlet path from request */
    public static String getServletPath(HttpServletRequest request) {
        if(request == null){
	    return "/";
        }

        String url = request.getRequestURI();
        if(url == null){
            return "/";
        }

        String contextPath = request.getContextPath();
        if(contextPath!=null&&!contextPath.equals("/")&&url.startsWith(contextPath)){
            url=url.substring(contextPath.length());
        }
        url = decodeURL(url);

        return url;
    }

    /** Gets a standardized URL string from a normal URL string */
    public static String decodeURL(String url) {

        if(url != null) {
            url = url.replaceAll("\\+", "%2B");
            url = URLDecoder.decode(url);
	}

        return url;
    }

    public static String encodeURL(String url){
        if(url!=null){
            int exprPos = url.indexOf("_expr_");
            if(exprPos>=0){
                 String baseURL = url.substring(0,exprPos);
                 String ce, exprURL;
                 int cePos = url.indexOf("?");
                 if(cePos<0){
                    ce = "";
                    exprURL = url.substring(exprPos);
                 }
                 else{
                    ce = url.substring(cePos);
                    exprURL =  url.substring(exprPos, cePos);
                 }
                 exprURL = URLEncoder.encode(exprURL);
                 exprURL = exprURL.replaceAll("\\+", "%20");
                 url = baseURL + exprURL + ce;
             }
         }
         return url;
    }
    /** Get the last index of a character in an expression string,
     * not including thoes characters inside '', ``, "", (), [] or {}
     * pairs
     * @return the last index of a character, -1 if not found
     */
    public static int lastIndexOf(char ch, String exp) {
        Stack stack = new Stack();
        int state = 0;
        int lastIndex = -1;

        for(int i=0; i<exp.length(); i++) {
            char curCh = exp.charAt(i);
            if(stack.empty() && state==0 
               && ch == curCh)
	       lastIndex = i;
            try{
               state = nextState(curCh, state, stack);
            } catch (Exception e) {
	       return -1;
	    }
            if(stack.empty() && state==0 
               && ch == curCh)
	       lastIndex = i;
        }
        try {
	    reportFinalState(state,stack);
        } catch (AnagramException ae) {
            return -1;
	}
        return lastIndex;
    }


    /** Strips space characters from start and 
     *  the end of a given string 
     */
    public static String stripSpacesFrom(String str) {
        int startIndex =0;
        int endIndex =str.length()-1;

        for(int i=0;i<=endIndex;i++){
	    if(str.charAt(i)!=' ') {
                startIndex=i;
                break;
	    }
	}
        for(int i=endIndex;i>=startIndex;i--) {
            if(str.charAt(i)!=' ') {
                endIndex=i;
                break;
	    }
	}
        str=str.substring(startIndex,endIndex+1);

        return str;
    }

    /** Removes redundant space charaters from a string.
     * A space charater is considered redundant if it is
     * not inside ``,'' or "" pair and it is after another
     * space character. 
     */
    public static String compactSpacesFor(String str) 
        throws AnagramException {
        StringBuffer returnVal = new StringBuffer();
        Stack stack = new Stack();
        int state = 0;
        int posLastSpace = -1;
  
        str = stripSpacesFrom(str);
        for(int i=0; i<str.length(); i++) {
            char curCh=str.charAt(i);
  
            try {
                state = nextState(curCh, state, stack);
	    } catch (Exception e) {
                throw new AnagramException(e.getMessage());
	    }

            if(state==0 && curCh==' ') {
		if(i>posLastSpace+1){
		    returnVal.append(' ');
		}
                posLastSpace = i;
	    } else {
		returnVal.append(curCh);
	    }
	}
        return returnVal.toString();
    }

    /** Parse a full string to see if its syntax is correct.
     * If not, an {@link AnagramException} will be thrown
     */
    public static void parseFullString(String fullStr) 
        throws AnagramException {
	Stack stack = new Stack();
        int state = 0;
        for(int i=0; i<fullStr.length(); i++) {
            char curCh = fullStr.charAt(i);
            try {
                state = nextState(curCh, state, stack);
	    } catch (Exception e) {
                throw new AnagramException(e.getMessage());
	    }
	}
        reportFinalState(state, stack);
    }

    /** Get the first index of a character in an expression string,
     * not including thoes characters inside '', ``, "", (), [] or {}
     * pairs
     */
    public static int firstIndexOf(char ch, String exp)
        throws AnagramException {
        return firstIndexOf(ch, exp, 0);
    }


    /** Get the first index of the specified character in an expression 
     * string starting from a specified index, not including thoes 
     * characters inside '', ``, "", (), [] or {} pairs.
     * @return the first index of the specified character, -1 if not found
     */
    public static int firstIndexOf(char ch, String exp, int startIndex) 
        throws AnagramException {
        Stack stack = new Stack();
        int state = 0;
        for(int i=0; i<exp.length(); i++) {
            char curCh = exp.charAt(i);
            if(stack.empty() && state==0 
               && ch == curCh && i>=startIndex)
	       return i;
            try{
               state = nextState(curCh, state, stack);
            } catch (Exception e) {
	       throw new AnagramException(e.getMessage());
	    }

            if(stack.empty() && state==0 
               && ch == curCh && i>=startIndex)
	       return i;
        }

	reportFinalState(state,stack);
        return -1; 
    }
    
    /** Runs the state machine to next state, driven by a character 
     */
    protected static int nextState(char curCh, int state, Stack stack)
             throws Exception {
        if(state==2||state==4||state==6){
	    if(state==2) {
                state=1;
	    } else if (state==4) {
                state=3;
	    } else {
                state=5;
	    }
	} else {
            switch(curCh) {
	       case '{': 
                        if(state==0)
			    stack.push("{");
                        break;
	       case '}':
		        if(state==0) {
                            String popCh=(String)stack.pop();
                            if(!popCh.equals("{"))
				throw new AnagramException("" + popCh + " is not matched.");
			}
                        break;
	       case '[': 
                        if(state==0)
			    stack.push("[");
                        break;
	       case ']':
		        if(state==0) {
                            String popCh=(String)stack.pop();
                            if(!popCh.equals("["))
                                throw new AnagramException("" + popCh + " is not matched.");
			}
                        break;
	       case '(': 
                        if(state==0)
			    stack.push("(");
                        break;
	       case ')':
		        if(state==0) {
                            String popCh=(String)stack.pop();
                            if(!popCh.equals("("))
                                throw new AnagramException("" + popCh + " is not matched.");
			}
                        break;
	       case '"':
		        if(state==0) {
			    state=1;
                        } else if(state==1) {
                            state=0;
			}
                        break;
	       case '\'':
		        if(state==0) {
			    state=3;
                        } else if(state==3) {
                            state=0;
			}
                        break; 
	       case '`':
		        if(state==0) {
			    state=5;
                        } else if(state==5) {
                            state=0;
			}
                        break;
               case '\\':
   		        if(state==1){
			    state=2;
                        } else if(state==3){
                            state=4;
			} else if(state==5){
                            state=6;
			}
                        break;
               default:
		        break;
	   } 
        }
        return state;
    }

    /** Reports if the final state of the state machine is correct. 
     *  If the final state is not correct, then an {@link AnagramException} 
     *  will be thrown.
     */
    protected static void reportFinalState(int state, Stack stack)
	 throws AnagramException {
	if(state!=0) {
	    if(state==1||state==2){
                throw new AnagramException("\"\" pairs are not matched.");
	    } else if(state==3||state==4){
                throw new AnagramException("'' pairs are not matched.");
	    } else {
                throw new AnagramException("`` pairs are not matched.");
	    }
	} else if(!stack.empty()){
            String peepCh = (String)stack.peek();
            if(peepCh.equals("{")){
	        throw new AnagramException("{ is not matched.");
	    } else if(peepCh.equals("[")){
	        throw new AnagramException("[ is not matched.");
	    } else {
	        throw new AnagramException("( is not matched.");
	    }
	}
    }

    /** Judges if a expression is a Ferret command */
    public static boolean isFerretCommand(String exp) {
         StringTokenizer st = new StringTokenizer(exp, " /");
         if(st.hasMoreTokens()){
             String next = st.nextToken();
             if(ferretCommandsSet.contains(next))
                 return true;
             else
                 return false;
	 } else {
	     return false;
	 }
    }

    public static String MD5Encode(String str){
        String returnVal = null;
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte mdArr[]=md.digest(str.getBytes("UTF-16"));
            returnVal = toHexString(mdArr);
        }
        catch(Exception e){
            returnVal = URLEncoder.encode(str.substring(AnalysisFilter.ANALYSIS_PREFIX.length())).replaceAll("\\*","x");
        }
        return returnVal;
    }

    protected static String toHexString(byte bytes[])
    {
        char chars[] = new char[bytes.length*2];

        for (int i = 0 ; i < bytes.length ; ++ i)
        {
             chars[2*i] = HEXCODE[(bytes[i] & 0xF0) >>> 4];
             chars[2*i+1] = HEXCODE[bytes[i] & 0x0F];
        }
        return new String(chars);
    }

    protected static final char HEXCODE[] = {
      '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    /** All the commands used by Ferret server */
    protected static String [] ferretCommands 
                             = new String[]{
                                 "let",
                                 "define",
                                 "set",
                                 "use",
                                 "list",
                                 "show",
                                 "save",
                                 "cancel",
                                 "go",
                                 "file",
                                 "frame",
                                 "fill",
                                 "exit",
                                 "contour",
                                 "alias",
                                 "help",
                                 "label",
                                 "load",
                                 "load",
                                 "message",
                                 "plot",
                                 "pplus",
                                 "quit",
                                 "repeat",
                                 "shade",
                                 "spawn",
                                 "spaw",
                                 "spa",
                                 "sp",
                                 "statistics",
                                 "unalias",
                                 "user",
                                 "vector",
                                 "wire"
			       };
        
        protected static Set ferretCommandsSet;
        static {
            ferretCommandsSet = new HashSet();
            for(int i=0; i<ferretCommands.length; i++) {
                ferretCommandsSet.add(ferretCommands[i]);
            }
        }

}
